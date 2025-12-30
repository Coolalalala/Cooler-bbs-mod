package mchorse.bbs_mod.forms;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import mchorse.bbs_mod.cubic.render.vao.ModelVAO;
import mchorse.bbs_mod.forms.forms.BufferFlipperForm;
import mchorse.bbs_mod.forms.forms.CompositeShaderForm;
import mchorse.bbs_mod.forms.forms.GBufferShaderForm;
import mchorse.bbs_mod.forms.forms.ShaderForm;
import mchorse.bbs_mod.graphics.texture.Texture;
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
import net.irisshaders.iris.shaderpack.ShaderPack;
import net.irisshaders.iris.shaderpack.materialmap.NamespacedId;
import net.irisshaders.iris.shaderpack.programs.ProgramSet;
import net.irisshaders.iris.targets.RenderTarget;
import net.irisshaders.iris.targets.RenderTargets;
import net.irisshaders.iris.uniforms.*;
import net.irisshaders.iris.uniforms.custom.CustomUniforms;
import net.irisshaders.iris.pipeline.CustomTextureManager;
import net.irisshaders.iris.shaderpack.texture.TextureStage;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.pipeline.transform.TransformPatcher;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexFormats;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL13;

import java.lang.reflect.Field;
import java.nio.FloatBuffer;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static mchorse.bbs_mod.client.BBSRendering.isIrisShadersEnabled;
import static mchorse.bbs_mod.forms.forms.ShaderForm.*;

public class ShaderManager {
    public static Map<ShaderForm, Integer> activeDeferredShaders = new HashMap<>();
    public static Map<ShaderForm, Integer> activeCompositeShaders = new HashMap<>();
    public static Map<GBufferShaderForm, List<GBufferGroupData>> activeGBufferShaders = new HashMap<>();
    private static ImmutableSet<Integer> flipState = ImmutableSet.of();
    private static BufferFlipperForm compositeFlipper;
    private static BufferFlipperForm deferredFlipper;
    private static boolean isFullScreen = false;

    private static IrisRenderingPipeline pipeline = null;
    private static RenderTargets renderTargets = null;
    private static CustomUniforms customUniforms = null;
    private static CenterDepthSampler centerDepthSampler = null;
    private static Object2ObjectMap<String, TextureAccess> customTextureIds = Object2ObjectMaps.emptyMap();
    private static Object2ObjectMap<String, TextureAccess> irisCustomTextures = Object2ObjectMaps.emptyMap();
    private static Set<GlImage> customImages = Collections.emptySet();
    private static CustomTextureManager customTextureManager = null;
    private static FrameUpdateNotifier updateNotifier = new FrameUpdateNotifier();
    private static ShaderPack currentPack = null;


    @SuppressWarnings("unchecked")
    private static void init() {
        if (!isIrisShadersEnabled()) return;
        try {
            pipeline = (IrisRenderingPipeline) Iris.getPipelineManager().getPipelineNullable();
        } catch (ClassCastException e) {
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

            // Get custom textures and images
            Field customImagesField = IrisRenderingPipeline.class.getDeclaredField("customImages");
            customImagesField.setAccessible(true);
            customImages = (Set<GlImage>) customImagesField.get(pipeline);

            // Get custom texture manager
            Field customTextureManagerField = IrisRenderingPipeline.class.getDeclaredField("customTextureManager");
            customTextureManagerField.setAccessible(true);
            customTextureManager = (CustomTextureManager) customTextureManagerField.get(pipeline);

            // Get notifier
            Field updateNotifierField = IrisRenderingPipeline.class.getDeclaredField("updateNotifier");
            updateNotifierField.setAccessible(true);
            updateNotifier = (FrameUpdateNotifier) updateNotifierField.get(pipeline);

            // Get current pack
            Field currentPackField = Iris.class.getDeclaredField("currentPack");
            currentPackField.setAccessible(true);
            currentPack = (ShaderPack) currentPackField.get(Iris.class);

            LogUtils.getLogger().info("Successfully initialized ShaderManager with Iris pipeline access");
            LogUtils.getLogger().info("Render targets: {}", (renderTargets));
            LogUtils.getLogger().info("Custom uniforms: {}", (customUniforms));
            LogUtils.getLogger().info("Custom images: {}", customImages.size());
            LogUtils.getLogger().info("Custom texture manager: {}", (customTextureManager));

            // Set up flipper forms
            compositeFlipper = new BufferFlipperForm(COMPOSITE_STAGE, "composite_flipper");
            deferredFlipper = new BufferFlipperForm(DEFERRED_STAGE, "deferred_flipper");
        } catch (Exception e) {
            LogUtils.getLogger().error("Failed to retrieve fields from Iris render pipeline: {}", e.toString());
        }
    }

    private static boolean isPipelineNuhuh() {
        if (!isIrisShadersEnabled()) return true;
        if (pipeline == null) init();
        return pipeline == null;
    }

    public static void register(ShaderForm program, int type) {
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

    public static void addModelGroup(GBufferShaderForm program, ModelVAO modelVAO, Matrix4f transform, Texture texture) {
        if (isPipelineNuhuh()) return;
        activeGBufferShaders.putIfAbsent(program, new ArrayList<>());
        GBufferGroupData data = new GBufferGroupData(modelVAO, transform, texture);
        if (!activeGBufferShaders.get(program).contains(data)) {
            activeGBufferShaders.get(program).add(data);
        }
    }

    public static void remove(ShaderForm program) {
        activeCompositeShaders.remove(program);
        activeDeferredShaders.remove(program);
    }

    public static void clear() {
        activeCompositeShaders.clear();
        activeDeferredShaders.clear();
        activeGBufferShaders.clear();
    }

    /**
     * Render 3D geometry with a custom shader program
     */
    public static void renderGeometry(GBufferShaderForm shaderForm, List<GBufferGroupData> gBufferDatas) {
        if (renderTargets == null) return;

        if (isFullScreen) {
            FullScreenQuadRenderer.INSTANCE.end();
            isFullScreen = false;
        }
        try {
            // Get or create the shader program
            Program program;
            if (shaderForm.isDirty()) {
                shaderForm.destroyProgram();
                program = createGBufferProgram(shaderForm);
                shaderForm.setProgram(program);
                if (program == null) {
                    LogUtils.getLogger().warn("Geometry shader program is null for form: {}", shaderForm.getName());
                    return;
                }
            } else {
                program = shaderForm.getProgram();
            }

            if (program == null) return;
            if (gBufferDatas == null) return;

            // Unbind any previous program
            Program.unbind();

            // Get draw buffers from shader form or default to colortex0
            int[] drawBuffers = shaderForm.getDrawBuffers();
            if (drawBuffers == null || drawBuffers.length == 0) {
                drawBuffers = new int[]{0};
            }

            // Bind framebuffer
            if (!shaderForm.bindFramebuffer()) {
                shaderForm.setFramebuffer(renderTargets.createColorFramebufferWithDepth(
                        shaderForm.getFlippedBuffers(),
                        drawBuffers
                ));
            }

            // Use the program
            program.use();

            // Push custom uniforms
            customUniforms.push(program);

            // Render
            int progID = program.getProgramId();
            int poseLocation = GlStateManager._glGetUniformLocation(progID, "modelPoseMatrix");
            int textureLocation = GlStateManager._glGetUniformLocation(progID, "modelTexture");
            // Upload texture unit
            int modelTextureUnit = 8; // TODO: properly manage texture units
            RenderSystem.glUniform1i(textureLocation, modelTextureUnit);

            for (GBufferGroupData data : gBufferDatas) {
                // Upload transform as an uniform
                FloatBuffer buffer = org.lwjgl.BufferUtils.createFloatBuffer(16);
                data.poseTransform.get(buffer);
                RenderSystem.glUniformMatrix4(poseLocation, false, buffer);
                
                // Upload texture to the assigned texture unit
                data.texture.bind(GL13.GL_TEXTURE0 + modelTextureUnit);
                
                // Draw group
                data.vao.render(VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, 1, 0, 1, 1, 0, 0); // TODO: get light level
            }

            // Clean up
            Program.unbind();
            activeGBufferShaders.remove(shaderForm);

        } catch (Exception e) {
            LogUtils.getLogger().error("Failed to render geometry with shader: {}", shaderForm.getName(), e);
        }
    }

    public static void renderFullScreenQuad(CompositeShaderForm shaderForm) {
        if (renderTargets == null) return;

        if (!isFullScreen) {
            FullScreenQuadRenderer.INSTANCE.begin();
            isFullScreen = true;
        }
        try {
            // Get or create the shader program using Iris integration
            Program program;
            if (shaderForm.isDirty()) { // Recompile if dirty
                shaderForm.destroyProgram();
                // Try to create Iris-integrated program
                program = createCompositeProgram(shaderForm);
                shaderForm.setProgram(program);
                if (program == null) {
                    LogUtils.getLogger().warn("Composite shader program is null for form: {}", shaderForm.getName());
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
                        shaderForm.getFlippedBuffers(),
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

    private static void renderShaderForm(ShaderForm shaderForm) {
        if (shaderForm instanceof GBufferShaderForm) renderGeometry((GBufferShaderForm) shaderForm, activeGBufferShaders.get(shaderForm));
        else renderFullScreenQuad((CompositeShaderForm) shaderForm);
    }

    /**
     * Render custom shaders
     */
    private static void renderPrograms(Map<ShaderForm, Integer> shaderMap, BufferFlipperForm bufferFlipper) {
        if (isPipelineNuhuh()) return;

        // Get all active shader forms
        List<ShaderForm> activeShaders = new ArrayList<>();
        Iterator<Map.Entry<ShaderForm, Integer>> iterator = new ArrayList<>(shaderMap.entrySet()).iterator();
        while (iterator.hasNext()) {
            Map.Entry<ShaderForm, Integer> entry = iterator.next();
            if (entry.getValue() >= 0) {
                activeShaders.add(entry.getKey());
            } else {
                // Clean up inactive shaders
                remove(entry.getKey());
                iterator.remove();
            }
        }
        if (activeShaders.isEmpty()) return;

        // Sort by priority and buffer usage to avoid conflicts
        activeShaders.sort(Comparator
                .comparingInt((ShaderForm s) -> shaderMap.get(s))
                .thenComparing(s -> Arrays.hashCode(s.getDrawBuffers())));

        // Begin fullscreen quad rendering
        RenderSystem.disableBlend();
        if (isFullScreen) {
            FullScreenQuadRenderer.INSTANCE.begin();
        }

        // Initialize flip state
        flipState = activeShaders.get(0).getFlippedBuffers();
        
        // Render each shader
        for (ShaderForm shaderForm : activeShaders) {
            // Wait for next dispatch
            shaderMap.replace(shaderForm, -1);
            // Render
            renderShaderForm(shaderForm);
            // Toggle flip state
            flipState = getFlipped(flipState, Arrays.stream(shaderForm.getDrawBuffers()).boxed().collect(Collectors.toSet()));
        }
        // Flip the buffers so that it matches the expected input from iris
        if (!flipState.equals(ImmutableSet.of())) { // If the flip state is not empty
            bufferFlipper.set(flipState);
            renderFullScreenQuad(bufferFlipper);
        }

        // End fullscreen quad rendering
        if (isFullScreen) {
            FullScreenQuadRenderer.INSTANCE.end();
        }
        
        // Clean up state
        Program.unbind();
        GlStateManager._glUseProgram(0);
        
        // Restore main framebuffer
        MinecraftClient.getInstance().getFramebuffer().beginWrite(true);
    }

    public static void renderCompositeStage() {
        renderPrograms(activeCompositeShaders, compositeFlipper);
    }

    public static void renderDeferredStage() {
        renderPrograms(activeDeferredShaders, deferredFlipper);
    }


    public static Program createCompositeProgram(ShaderForm shaderForm) {
        return createProgramInternal(shaderForm, true, IrisSamplers.COMPOSITE_RESERVED_TEXTURE_UNITS, FogMode.OFF);
    }

    public static Program createGBufferProgram(GBufferShaderForm shaderForm) {
        return createProgramInternal(shaderForm, false, IrisSamplers.WORLD_RESERVED_TEXTURE_UNITS, FogMode.PER_VERTEX);
    }


    private static Program createProgramInternal(ShaderForm shaderForm, boolean isComposite, ImmutableSet<java.lang.Integer> reservedTextureUnits, FogMode fogMode) {
        if (isPipelineNuhuh()) return null;

        try {
            LogUtils.getLogger().debug("Creating Iris-integrated program for: {}", shaderForm.getName());

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

            // Create program builder with transformed shader sources
            ProgramBuilder builder = ProgramBuilder.begin(
                shaderForm.getName(),
                vertex,
                geometry,
                fragment,
                reservedTextureUnits
            );

            // Find draw buffers
            shaderForm.setDrawBuffers(findBufferLocations(fragment));

            // Add dynamic uniforms (time, view matrix, etc.)
            NamespacedId dimension = new NamespacedId("minecraft:overworld");
            ProgramSet currentSet = currentPack.getProgramSet(dimension).getPack().getProgramSet(dimension);
            CommonUniforms.addCommonUniforms(builder, currentPack.getIdMap(), currentSet.getPackDirectives(), updateNotifier, fogMode);

            // Assign custom uniforms from shader pack
            customUniforms.assignTo(builder);

            // Set up samplers
            // Use CustomTextureManager to get the actual texture maps for the appropriate stage
            Object2ObjectMap<String, TextureAccess> textureIds = customTextureManager != null
                ? customTextureManager.getCustomTextureIdMap(textureStage)
                : customTextureIds;
            Object2ObjectMap<String, TextureAccess> irisTextures = customTextureManager != null
                ? customTextureManager.getIrisCustomTextures()
                : irisCustomTextures;

            ProgramSamplers.CustomTextureSamplerInterceptor interceptor =
                ProgramSamplers.customTextureSamplerInterceptor(builder, textureIds, ImmutableSet.of());

            // Find the buffer set required for that stage
            Supplier<ImmutableSet<Integer>> flippedBuffers = switch (shaderForm.renderStage.get()) {
                case BEGIN_STAGE, PREPARE_STAGE -> pipeline::getFlippedBeforeShadow;
                case DEFERRED_STAGE -> pipeline::getFlippedAfterPrepare;
                case COMPOSITE_STAGE, FINAL_STAGE -> pipeline::getFlippedAfterTranslucent;
                default -> throw new IllegalArgumentException("Invalid render stage: " + shaderForm.renderStage.get());
            };
            shaderForm.setFlippedBuffers(getFlipped(flippedBuffers.get(), flipState));

            // Add render target samplers (colortex0-7, depth, etc.)
            IrisSamplers.addRenderTargetSamplers(
                interceptor,
                shaderForm::getFlippedBuffers,
                renderTargets,
                isComposite,
                pipeline
            );

            // Add custom textures from shader pack
            IrisSamplers.addCustomTextures(builder, irisTextures);

            // Add custom images
            IrisSamplers.addCustomImages(interceptor, customImages);

            // Add render target images
            IrisImages.addRenderTargetImages(builder, shaderForm::getFlippedBuffers, renderTargets);

            // Add custom images
            IrisImages.addCustomImages(builder, customImages);

            // Add noise sampler - try to get the actual noise texture
            TextureAccess noiseTexture = customTextureManager.getNoiseTexture();
            IrisSamplers.addNoiseSampler(interceptor, noiseTexture);

            // Add composite samplers
            IrisSamplers.addCompositeSamplers(interceptor, renderTargets);

            // Add center depth sampler
            if (centerDepthSampler != null) {
                centerDepthSampler.setUsage(
                    builder.addDynamicSampler(
                        centerDepthSampler::getCenterDepthTexture,
                        "iris_centerDepthSmooth"
                    )
                );
            }

            // Build the program
            Program program = builder.build();

            // Map custom uniforms to this program
            customUniforms.mapholderToPass(builder, program);

            LogUtils.getLogger().info("Successfully created {} shader program for: {}",
                        isComposite ? "composite" : "geometry", shaderForm.getName());
            return program;

        } catch (IllegalStateException e) {
            destroy();
        } catch (Exception e) {
            LogUtils.getLogger().error("Failed to create {} program for: {}\n{}",
                    isComposite ? "composite" : "geometry", shaderForm.getName(), e.getMessage());
        }
        return null;
    }


    public static void reCompile() {
        LogUtils.getLogger().info("Recompiling shader forms...");
        activeCompositeShaders.keySet().forEach(ShaderForm::markDirty);
        activeDeferredShaders.keySet().forEach(ShaderForm::markDirty);
        compositeFlipper.setBuffers(ImmutableSet.of());
        deferredFlipper.setBuffers(ImmutableSet.of());
    }

    public static void destroy() {
        // Clean up any active shader programs first
        activeCompositeShaders.keySet().forEach(ShaderForm::destroyProgram);
        activeDeferredShaders.keySet().forEach(ShaderForm::destroyProgram);

        clear();

        // Nullify all references
        pipeline = null;
        renderTargets = null;
        customUniforms = null;
        customImages = null;
        customTextureIds = null;
        irisCustomTextures = null;
        centerDepthSampler = null;
        customTextureManager = null;
    }

    /**
     * Get the set that is flipped for every buffer in the given set
     */
    public static ImmutableSet<Integer> getFlipped(Set<Integer> buffers, Set<Integer> toFlip) {
        if (buffers == null) return ImmutableSet.copyOf(toFlip);
        // Essentially XOR
        return IntStream.range(0, 8)
                .filter(i -> toFlip.contains(i) != buffers.contains(i))
                .boxed()
                .collect(ImmutableSet.toImmutableSet());
    }


    /**
     * Find written buffer locations in the given glsl source code
     *
     * @param src The glsl source code
     * @return The buffer locations that are written into
     */
    public static int[] findBufferLocations(String src) {
        List<Integer> locations = new ArrayList<>();

        // Pattern to match layout(location = X) with "out vec4"
        String pattern = "layout\\s*\\([^)]*?location\\s*=\\s*(\\d+)[^)]*\\)\\s+out\\s+vec4";

        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(src);

        while (matcher.find()) {
            try {
                // Extract the location number from the match
                String match = matcher.group(0);
                Pattern numPattern = Pattern.compile("location\\s*=\\s*(\\d+)");
                Matcher numMatcher = numPattern.matcher(match);

                if (numMatcher.find()) {
                    int location = Integer.parseInt(numMatcher.group(1));
                    locations.add(location);
                }
            } catch (NumberFormatException e) {
                LogUtils.getLogger().error("Failed to parse location, enabling all buffers");
                return new int[]{0,1,2,3,4,5,6,7};
            }
        }

        return locations.stream().mapToInt(Integer::intValue).toArray();
    }


    public static class GBufferGroupData {
        public ModelVAO vao;
        public Matrix4f poseTransform;
        public Texture texture;

        public GBufferGroupData(ModelVAO modelVAO, Matrix4f transform, Texture texture) {
            this.vao = modelVAO;
            this.poseTransform = transform;
            this.texture = texture;
        }
    }
}
