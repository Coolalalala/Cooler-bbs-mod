package mchorse.bbs_mod.forms;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import mchorse.bbs_mod.forms.forms.ShaderForm;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.gl.framebuffer.GlFramebuffer;
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
import net.irisshaders.iris.uniforms.FrameUpdateNotifier;
import net.irisshaders.iris.uniforms.custom.CustomUniforms;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.opengl.GL45C;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Supplier;

public class ShaderManager {
    private static final ShaderManager instance = new ShaderManager();
    private static final boolean enabled = isIrisInstalled();
    public static Map<ShaderForm, Boolean> programEnabled = new HashMap<>();

    private static IrisRenderingPipeline pipeline = null;
    private static RenderTargets renderTargets = null;
    private static CustomUniforms customUniforms = null;
    private static FrameUpdateNotifier updateNotifier = null;
    private static CenterDepthSampler centerDepthSampler = null;
    private static Object2ObjectMap<String, ?> customTextureIds = Object2ObjectMaps.emptyMap();
    private static Object2ObjectMap<String, ?> irisCustomTextures = Object2ObjectMaps.emptyMap();
    private static Set<GlImage> customImages = Collections.emptySet();
    private static Supplier<ImmutableSet<Integer>> flippedAfterTranslucent = () -> ImmutableSet.of();

    public ShaderManager() {
        if (!enabled) return;
        init();
    }

    @SuppressWarnings("unchecked")
    private static void init() {
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

            // Get frame update notifier
            Field updateNotifierField = IrisRenderingPipeline.class.getDeclaredField("updateNotifier");
            updateNotifierField.setAccessible(true);
            updateNotifier = (FrameUpdateNotifier) updateNotifierField.get(pipeline);

            // Get center depth sampler
            Field centerDepthSamplerField = IrisRenderingPipeline.class.getDeclaredField("centerDepthSampler");
            centerDepthSamplerField.setAccessible(true);
            centerDepthSampler = (CenterDepthSampler) centerDepthSamplerField.get(pipeline);

            // Get flipped buffers after translucent
            Field flippedAfterTranslucentField = IrisRenderingPipeline.class.getDeclaredField("flippedAfterTranslucent");
            flippedAfterTranslucentField.setAccessible(true);
            ImmutableSet<Integer> flipped = (ImmutableSet<Integer>) flippedAfterTranslucentField.get(pipeline);
            flippedAfterTranslucent = () -> flipped;

            // Get custom textures and images
            Field customImagesField = IrisRenderingPipeline.class.getDeclaredField("customImages");
            customImagesField.setAccessible(true);
            customImages = (Set<GlImage>) customImagesField.get(pipeline);

            // Note: customTextureIds and irisCustomTextures are left as empty maps
            // They will be populated by Iris when the shader pack loads
            // For now, we rely on IrisSamplers.addRenderTargetSamplers to set up the basic samplers

            LogUtils.getLogger().info("Successfully initialized ShaderManager with Iris pipeline access");
            LogUtils.getLogger().info("Render targets: " + (renderTargets != null));
            LogUtils.getLogger().info("Custom uniforms: " + (customUniforms != null));
            LogUtils.getLogger().info("Custom images: " + customImages.size());
            LogUtils.getLogger().info("Custom texture IDs: " + ((Object2ObjectMap<?, ?>) customTextureIds).size());
            LogUtils.getLogger().info("Iris custom textures: " + ((Object2ObjectMap<?, ?>) irisCustomTextures).size());

        } catch (Exception e) {
            LogUtils.getLogger().error("Failed to retrieve fields from Iris render pipeline: " + e);
            e.printStackTrace();
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

    public static ShaderManager get() {
        return instance;
    }

    public static void register(ShaderForm program) {
        if (!enabled) return;
        programEnabled.put(program, true);
    }

    public static void remove(ShaderForm program) {
        if (!enabled) return;
        programEnabled.remove(program);
    }

    public static void clear() {
        if (!enabled) return;
        programEnabled.clear();
    }

    /**
     * Render custom shaders in the composite stage
     * This should be called after the main composite renderer but before final pass
     */
    public static void renderCompositeStage() {
        if (!enabled) return;
        if (pipeline == null) init();
        if (pipeline == null) return;

        // Get all active shader forms
        List<ShaderForm> activeShaders = new ArrayList<>();
        for (Map.Entry<ShaderForm, Boolean> entry : programEnabled.entrySet()) {
            if (entry.getValue()) {
                activeShaders.add(entry.getKey());
            } else {
                // Clean up inactive shaders
                ShaderForm form = entry.getKey();
                remove(form);
                form.destroyProgram();
            }
        }

        if (activeShaders.isEmpty()) return;

        // Begin fullscreen quad rendering
        RenderSystem.disableBlend();
        FullScreenQuadRenderer.INSTANCE.begin();

        for (ShaderForm shaderForm : activeShaders) {
            try {
                // Get or create the shader program using Iris integration
                Program program = shaderForm.getProgram();
                if (program == null) {
                    // Try to create Iris-integrated program
                    program = createIrisIntegratedProgram(shaderForm);
                    if (program != null) {
                        shaderForm.setProgram(program);
                    } else {
                        LogUtils.getLogger().warn("Shader program is null for form: " + shaderForm.getName());
                        continue;
                    }
                }

                // Create a proper framebuffer for this shader
                // We use colortex0 as the primary target (same as final pass)
                int[] drawBuffers = new int[]{0}; // colortex0
                GlFramebuffer framebuffer = renderTargets.createColorFramebuffer(
                    flippedAfterTranslucent.get(),
                    drawBuffers
                );

                // Set up viewport (full screen)
                RenderTarget target = renderTargets.get(0);
                RenderSystem.viewport(0, 0, target.getWidth(), target.getHeight());

                // Bind framebuffer
                framebuffer.bind();

                // Use the program
                program.use();

                // Push custom uniforms if available
                // Note: The program already has Iris uniforms from ProgramBuilder
                // We could add custom uniforms here if needed

                // Render fullscreen quad
                FullScreenQuadRenderer.INSTANCE.renderQuad();

                // Memory barrier to ensure shader completion
                IrisRenderSystem.memoryBarrier(GL45C.GL_ALL_BARRIER_BITS);

                // Destroy framebuffer
                renderTargets.destroyFramebuffer(framebuffer);

            } catch (IllegalStateException e) {
                // Pipeline has been deleted
                destroy();
            } catch (Exception e) {
                LogUtils.getLogger().error("Failed to render shader form: " + shaderForm.getName(), e);
            }
        }

        // End fullscreen quad rendering
        FullScreenQuadRenderer.INSTANCE.end();

        // Clean up state
        Program.unbind();
        GlStateManager._glUseProgram(0);

        // Restore main framebuffer
        MinecraftClient.getInstance().getFramebuffer().beginWrite(true);
    }

    /**
     * Enhanced program creation with full Iris pipeline integration
     * This creates programs with access to all Iris samplers and uniforms
     */
    public static Program createIrisIntegratedProgram(ShaderForm shaderForm) {
        if (pipeline == null || renderTargets == null || customUniforms == null) {
            LogUtils.getLogger().warn("Iris pipeline not ready, using fallback program creation");
            // Fallback to basic program creation
            try {
                return shaderForm.createProgram();
            } catch (Exception e) {
                LogUtils.getLogger().error("Failed to create fallback program", e);
                return null;
            }
        }

        try {
            LogUtils.getLogger().debug("Creating Iris-integrated program for: " + shaderForm.getName());

            // Create program builder with shader sources
            ProgramBuilder builder = ProgramBuilder.begin(
                shaderForm.getName(),
                shaderForm.getVertexSource(),
                shaderForm.getGeometrySource(),
                shaderForm.getFragmentSource(),
                IrisSamplers.COMPOSITE_RESERVED_TEXTURE_UNITS
            );

            // Add dynamic uniforms (time, view matrix, etc.)
            CommonUniforms.addDynamicUniforms(builder, FogMode.OFF);
            LogUtils.getLogger().debug("Added dynamic uniforms");

            // Assign custom uniforms from shader pack
            customUniforms.assignTo(builder);
            LogUtils.getLogger().debug("Assigned custom uniforms");

            // Set up samplers - this is crucial for accessing Iris render targets
            Object2ObjectMap<String, TextureAccess> textureIds = (Object2ObjectMap<String, TextureAccess>) customTextureIds;
            Object2ObjectMap<String, TextureAccess> irisTextures = (Object2ObjectMap<String, TextureAccess>) irisCustomTextures;

            LogUtils.getLogger().debug("Custom texture IDs size: " + textureIds.size());
            LogUtils.getLogger().debug("Iris custom textures size: " + irisTextures.size());

            ProgramSamplers.CustomTextureSamplerInterceptor interceptor =
                ProgramSamplers.customTextureSamplerInterceptor(builder, textureIds, ImmutableSet.of());

            // Add render target samplers (colortex0-7, depth, etc.)
            IrisSamplers.addRenderTargetSamplers(
                interceptor,
                flippedAfterTranslucent,
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
            IrisImages.addRenderTargetImages(builder, flippedAfterTranslucent, renderTargets);
            LogUtils.getLogger().debug("Added render target images");

            // Add custom images
            IrisImages.addCustomImages(builder, customImages);

            // Add noise sampler - try to get the actual noise texture
            try {
                Field noiseTextureField = IrisRenderingPipeline.class.getDeclaredField("noiseTexture");
                noiseTextureField.setAccessible(true);
                TextureAccess noiseTexture = (TextureAccess) noiseTextureField.get(pipeline);
                IrisSamplers.addNoiseSampler(interceptor, noiseTexture);
                LogUtils.getLogger().debug("Added noise sampler");
            } catch (Exception e) {
                LogUtils.getLogger().warn("Could not get noise texture: " + e.getMessage());
                IrisSamplers.addNoiseSampler(interceptor, null);
            }

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

        } catch (Exception e) {
            LogUtils.getLogger().error("Failed to create Iris-integrated program for: " + shaderForm.getName(), e);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get the current flipped state for render targets
     */
    public static ImmutableSet<Integer> getFlippedBuffers() {
        return flippedAfterTranslucent.get();
    }

    /**
     * Check if Iris pipeline is available
     */
    public static boolean isPipelineReady() {
        return pipeline != null && renderTargets != null && customUniforms != null;
    }

    public static void destroy() {
        ShaderManager.clear();
        pipeline = null;
        renderTargets = null;
        customUniforms = null;
        customImages = null;
        customTextureIds = null;
        irisCustomTextures = null;
        centerDepthSampler = null;
        flippedAfterTranslucent = null;
    }
}
