package mchorse.bbs_mod.forms;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import mchorse.bbs_mod.forms.forms.CompositeShaderForm;
import mchorse.bbs_mod.forms.forms.ShaderForm;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.image.GlImage;
import net.irisshaders.iris.gl.program.Program;
import net.irisshaders.iris.gl.program.ProgramBuilder;
import net.irisshaders.iris.gl.program.ProgramSamplers;
import net.irisshaders.iris.gl.state.FogMode;
import net.irisshaders.iris.gl.texture.TextureAccess;
import net.irisshaders.iris.pathways.CenterDepthSampler;
import net.irisshaders.iris.pathways.FullScreenQuadRenderer;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.samplers.IrisImages;
import net.irisshaders.iris.samplers.IrisSamplers;
import net.irisshaders.iris.targets.RenderTarget;
import net.irisshaders.iris.targets.RenderTargets;
import net.irisshaders.iris.uniforms.CommonUniforms;
import net.irisshaders.iris.uniforms.custom.CustomUniforms;
import net.irisshaders.iris.pipeline.CustomTextureManager;
import net.irisshaders.iris.shaderpack.texture.TextureStage;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.pipeline.transform.TransformPatcher;
import net.irisshaders.iris.pipeline.transform.ShaderPrinter;
import net.minecraft.client.MinecraftClient;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Supplier;

import static mchorse.bbs_mod.forms.forms.ShaderForm.*;

public class ShaderManager {
    private static final boolean enabled = isIrisInstalled();
    public static Map<CompositeShaderForm, Integer> activeDeferredShaders = new HashMap<>();
    public static Map<CompositeShaderForm, Integer> activeCompositeShaders = new HashMap<>();

    private static IrisRenderingPipeline pipeline = null;
    private static RenderTargets renderTargets = null;
    private static CustomUniforms customUniforms = null;
    private static CenterDepthSampler centerDepthSampler = null;
    private static Object2ObjectMap<String, TextureAccess> customTextureIds = Object2ObjectMaps.emptyMap();
    private static Object2ObjectMap<String, TextureAccess> irisCustomTextures = Object2ObjectMaps.emptyMap();
    private static Set<GlImage> customImages = Collections.emptySet();
    private static Supplier<ImmutableSet<Integer>> flippedBeforeShadow;
    private static Supplier<ImmutableSet<Integer>> flippedAfterTranslucent;
    private static Supplier<ImmutableSet<Integer>> flippedAfterPrepare;
    private static CustomTextureManager customTextureManager = null;

    @SuppressWarnings("unchecked")
    private static void init() {
        if (!enabled) return;
        try {
            pipeline = (IrisRenderingPipeline) Iris.getPipelineManager().getPipelineNullable();
        } catch (ClassCastException e) {
            LogUtils.getLogger().debug("Iris rendering is disabled");
            return;
        }
        if (pipeline == null) return;

        // Get private fields using reflection
        try {
            // Get render targets
            Field renderTargetsField = IrisRenderingPipeline.class.getDeclaredField("renderTargets");
            renderTargetsField.setAccessible(true);
            renderTargets = (RenderTargets) renderTargetsField.get(pipeline);

            // Get custom uniforms
            Field customUniformsField = IrisRenderingPipeline.class.getDeclaredField("customUniforms");
            customUniformsField.setAccessible(true);
            customUniforms = (CustomUniforms) customUniformsField.get(pipeline);

            // Get center depth sampler
            Field centerDepthSamplerField = IrisRenderingPipeline.class.getDeclaredField("centerDepthSampler");
            centerDepthSamplerField.setAccessible(true);
            centerDepthSampler = (CenterDepthSampler) centerDepthSamplerField.get(pipeline);

            // Get flipped buffer states
            flippedBeforeShadow = pipeline::getFlippedBeforeShadow;
            flippedAfterTranslucent = pipeline::getFlippedAfterTranslucent;
            flippedAfterPrepare = pipeline::getFlippedAfterPrepare;

            // Get custom textures and images
            Field customImagesField = IrisRenderingPipeline.class.getDeclaredField("customImages");
            customImagesField.setAccessible(true);
            customImages = (Set<GlImage>) customImagesField.get(pipeline);

            // Get custom texture manager
            Field customTextureManagerField = IrisRenderingPipeline.class.getDeclaredField("customTextureManager");
            customTextureManagerField.setAccessible(true);
            customTextureManager = (CustomTextureManager) customTextureManagerField.get(pipeline);

            LogUtils.getLogger().info("Successfully initialized ShaderManager with Iris pipeline access");
            LogUtils.getLogger().info("Render targets: {}", (renderTargets));
            LogUtils.getLogger().info("Custom uniforms: {}", (customUniforms));
            LogUtils.getLogger().info("Custom images: {}", customImages.size());
            LogUtils.getLogger().info("Custom texture manager: {}", (customTextureManager));

            // Set up flipper forms
        } catch (Exception e) {
            LogUtils.getLogger().error("Failed to retrieve fields from Iris render pipeline: {}", e.toString());
        }
    }

    private static boolean isIrisInstalled() {
        try {
            Class.forName("net.irisshaders.iris.Iris");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static boolean isPipelineNuhuh() {
        if (!enabled) return true;
        if (pipeline == null) init();
        return pipeline == null;
    }

    public static void registerComposite(CompositeShaderForm program, int type) {
        if (isPipelineNuhuh()) return;
        switch (type) {
            case 2:
                activeDeferredShaders.put(program, program.priority.get());
                break;
            case 3:
                activeCompositeShaders.put(program, program.priority.get());
                break;
            default:
                break;
        }
    }

    public static void remove(ShaderForm program) {
        if (program instanceof CompositeShaderForm) {
            activeCompositeShaders.remove(program);
            activeDeferredShaders.remove(program);
        }
    }

    public static void clear() {
        activeCompositeShaders.clear();
        activeDeferredShaders.clear();
    }

    public static void renderFullScreenQuad(CompositeShaderForm shaderForm, CompositeShaderForm previous) {
        if (renderTargets == null) return;
        try {
            // Get or create the shader program using Iris integration
            Program program;
            if (shaderForm.isDirty()) { // Recompile if dirty
                shaderForm.destroyProgram();
                // Try to create Iris-integrated program
                program = createCompositeProgram(shaderForm, previous);
                shaderForm.setProgram(program);
                if (program == null) {
                    LogUtils.getLogger().warn("Shader program is null for form: " + shaderForm.getName());
                    return;
                }
            }
            else program = shaderForm.getProgram();
            if (program == null) return;

            // Unbind any previous program
            Program.unbind();

            // Get draw buffers from shader form or default to colortex0
            int[] drawBuffers = shaderForm.getDrawBuffers();
            if (drawBuffers == null || drawBuffers.length == 0) {
                drawBuffers = new int[]{0};
            }

            // Bind framebuffer
            if (!shaderForm.bindFramebuffer()) {
                shaderForm.setFramebuffer(renderTargets.createColorFramebuffer(
                        shaderForm.getFlippedBuffers().get(),
                        drawBuffers
                ));
            }

            // Calculate viewport based on first draw buffer
            RenderTarget target = renderTargets.get(drawBuffers[0]);
            RenderSystem.viewport(0, 0, target.getWidth(), target.getHeight());
            // Use the program
            program.use();

            // Push custom uniforms
            customUniforms.push(program);

            // Render fullscreen quad
            FullScreenQuadRenderer.INSTANCE.renderQuad();
        } catch (Exception e) {
            LogUtils.getLogger().error("Failed to render shader form: " + shaderForm.getName(), e);
        }
    }

    /**
     * Render custom shaders in the composite style (full screen quad)
     */
    private static void renderCompositeStyle(Map<CompositeShaderForm, Integer> shaderMap) {
        if (isPipelineNuhuh()) return;

        // Get all active shader forms
        List<CompositeShaderForm> activeShaders = new ArrayList<>();
        Iterator<Map.Entry<CompositeShaderForm, Integer>> iterator = new ArrayList<>(shaderMap.entrySet()).iterator();
        while (iterator.hasNext()) {
            Map.Entry<CompositeShaderForm, Integer> entry = iterator.next();
            if (entry.getValue() >= 0) {
                activeShaders.add(entry.getKey());
            } else {
                // Clean up inactive shaders
                remove(entry.getKey());
                iterator.remove();
            }
        }
        if (activeShaders.isEmpty()) return;
        activeShaders.sort(Comparator.comparingInt(shaderMap::get)); // Large number runs first

        // Begin fullscreen quad rendering
        RenderSystem.disableBlend();
        FullScreenQuadRenderer.INSTANCE.begin();
        // Render each shader
        CompositeShaderForm previous = null;
        for (CompositeShaderForm shaderForm : activeShaders) {
            // Wait for next dispatch
            shaderMap.replace(shaderForm, -1);
            renderFullScreenQuad(shaderForm, previous);
            previous = shaderForm;
        }
        // End fullscreen quad rendering
        FullScreenQuadRenderer.INSTANCE.end();
        // Clean up state
        Program.unbind();
        GlStateManager._glUseProgram(0);
        // Restore main framebuffer
        MinecraftClient.getInstance().getFramebuffer().beginWrite(true);
    }

    public static void renderCompositeStage() {
        renderCompositeStyle(activeCompositeShaders);
    }

    public static void renderDeferredStage() {
        renderCompositeStyle(activeDeferredShaders);
    }


    /**
     * Enhanced program creation with full Iris pipeline integration
     * This creates programs with access to all Iris samplers and uniforms
     * Uses TransformPatcher to transform shaders for compatibility with Iris's FullScreenQuadRenderer
     */
    public static Program createCompositeProgram(ShaderForm shaderForm, ShaderForm parent) {
        if (isPipelineNuhuh()) {
            LogUtils.getLogger().debug("Iris pipeline not ready, cannot create shader program");
            return null;
        }

        try {
            LogUtils.getLogger().debug("Creating Iris-integrated program for: " + shaderForm.getName());

            // Transform shaders using Iris's TransformPatcher (like CompositeRenderer does)

            // Use appropriate texture stage based on render stage
            TextureStage textureStage = switch (shaderForm.renderStage.get()) {
                case BEGIN_STAGE -> TextureStage.BEGIN;
                case PREPARE_STAGE -> TextureStage.PREPARE;
                case DEFERRED_STAGE -> TextureStage.DEFERRED;
                case COMPOSITE_STAGE, FINAL_STAGE -> TextureStage.COMPOSITE_AND_FINAL;
                default -> throw new IllegalArgumentException("Invalid render stage: " + shaderForm.renderStage.get());
            };

            Map<PatchShaderType, String> transformed = TransformPatcher.patchComposite(
                shaderForm.getName(),
                shaderForm.getVertexSource(),
                shaderForm.getGeometrySource(),
                shaderForm.getFragmentSource(),
                textureStage,
                pipeline.getTextureMap()
            );

            String vertex = transformed.get(PatchShaderType.VERTEX);
            String geometry = transformed.get(PatchShaderType.GEOMETRY);
            String fragment = transformed.get(PatchShaderType.FRAGMENT);

            // Print transformed shaders for debugging
            ShaderPrinter.printProgram(shaderForm.getName()).addSources(transformed).print();

            // Create program builder with transformed shader sources
            ProgramBuilder builder = ProgramBuilder.begin(
                shaderForm.getName(),
                vertex,
                geometry,
                fragment,
                IrisSamplers.COMPOSITE_RESERVED_TEXTURE_UNITS
            );

            // Add dynamic uniforms (time, view matrix, etc.)
            CommonUniforms.addDynamicUniforms(builder, FogMode.OFF);
            LogUtils.getLogger().debug("Added dynamic uniforms");

            // Assign custom uniforms from shader pack
            customUniforms.assignTo(builder);
            LogUtils.getLogger().debug("Assigned custom uniforms");

            // Set up samplers
            // Use CustomTextureManager to get the actual texture maps for the appropriate stage
            Object2ObjectMap<String, TextureAccess> textureIds = customTextureManager != null
                ? customTextureManager.getCustomTextureIdMap(textureStage)
                : customTextureIds;
            Object2ObjectMap<String, TextureAccess> irisTextures = customTextureManager != null
                ? customTextureManager.getIrisCustomTextures()
                : irisCustomTextures;

            LogUtils.getLogger().debug("Custom texture IDs size: " + textureIds.size());
            LogUtils.getLogger().debug("Iris custom textures size: " + irisTextures.size());

            ProgramSamplers.CustomTextureSamplerInterceptor interceptor =
                ProgramSamplers.customTextureSamplerInterceptor(builder, textureIds, ImmutableSet.of());

            // Find the buffer set required for that stage
            Supplier<ImmutableSet<Integer>> flippedBuffers = switch (shaderForm.renderStage.get()) {
                case BEGIN_STAGE -> flippedBeforeShadow;
                case PREPARE_STAGE -> flippedBeforeShadow;
                case DEFERRED_STAGE -> flippedAfterPrepare;
                case COMPOSITE_STAGE, FINAL_STAGE -> flippedAfterTranslucent;
                default -> throw new IllegalArgumentException("Invalid render stage: " + shaderForm.renderStage.get());
            };
            if (shaderForm.priority.get() % 2 != 1) { // Even priority
                shaderForm.setFlippedBuffers(() -> getFlipped(flippedBuffers));
            } else {
                shaderForm.setFlippedBuffers(flippedBuffers);
            }
            // Add render target samplers (colortex0-7, depth, etc.)
            IrisSamplers.addRenderTargetSamplers(
                interceptor,
                shaderForm.getFlippedBuffers(),
                renderTargets,
                true,  // isComposite
                pipeline
            );
            LogUtils.getLogger().debug("Added render target samplers");

            // Add custom textures from shader pack
            IrisSamplers.addCustomTextures(builder, irisTextures);
            LogUtils.getLogger().debug("Added custom textures");

            // Add custom images
            IrisSamplers.addCustomImages(interceptor, customImages);
            LogUtils.getLogger().debug("Added custom images: " + customImages.size());

            // Add render target images
            IrisImages.addRenderTargetImages(builder, shaderForm.getFlippedBuffers(), renderTargets);
            LogUtils.getLogger().debug("Added render target images");

            // Add custom images
            IrisImages.addCustomImages(builder, customImages);

            // Add noise sampler - try to get the actual noise texture
            TextureAccess noiseTexture = customTextureManager.getNoiseTexture();
            IrisSamplers.addNoiseSampler(interceptor, noiseTexture);
            LogUtils.getLogger().debug("Added noise sampler");

            // Add composite samplers
            IrisSamplers.addCompositeSamplers(interceptor, renderTargets);
            LogUtils.getLogger().debug("Added composite samplers");

            // Add center depth sampler
            if (centerDepthSampler != null) {
                centerDepthSampler.setUsage(
                    builder.addDynamicSampler(
                        centerDepthSampler::getCenterDepthTexture,
                        "iris_centerDepthSmooth"
                    )
                );
                LogUtils.getLogger().debug("Added center depth sampler");
            }

            // Build the program
            Program program = builder.build();
            LogUtils.getLogger().debug("Built program");

            // Map custom uniforms to this program
            customUniforms.mapholderToPass(builder, program);
            LogUtils.getLogger().debug("Mapped uniforms to program");

            LogUtils.getLogger().info("Successfully created Iris-integrated program for: " + shaderForm.getName());
            return program;

        } catch (IllegalStateException e) {
            // Pipeline has been deleted
            destroy();
        } catch (Exception e) {
            LogUtils.getLogger().error("Failed to create Iris-integrated program for: " + shaderForm.getName(), e);
        }
        return null;
    }

    public static void destroy() {
        // Clean up any active shader programs first
        activeCompositeShaders.keySet().forEach(CompositeShaderForm::destroyProgram);
        activeDeferredShaders.keySet().forEach(CompositeShaderForm::destroyProgram);

        clear();

        // Nullify all references
        pipeline = null;
        renderTargets = null;
        customUniforms = null;
        customImages = null;
        customTextureIds = null;
        irisCustomTextures = null;
        centerDepthSampler = null;
        flippedAfterTranslucent = null;
        flippedAfterPrepare = null;
        customTextureManager = null;
    }

    public static ImmutableSet<Integer> getFlipped(Supplier<ImmutableSet<Integer>> buffers) {
        ImmutableSet<Integer> bufferInt = buffers.get();
        ImmutableSet.Builder<Integer> flipped =  ImmutableSet.builder();
        for (int i = 0; i <8; i++) {
            if (!bufferInt.contains(i)) {
                flipped.add(i);
            }
        }
        return flipped.build();
    }
}
