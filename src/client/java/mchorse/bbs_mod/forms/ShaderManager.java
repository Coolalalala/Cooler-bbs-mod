package mchorse.bbs_mod.forms;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import mchorse.bbs_mod.forms.forms.ShaderForm;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.gl.blending.BlendModeOverride;
import net.irisshaders.iris.gl.framebuffer.GlFramebuffer;
import net.irisshaders.iris.gl.program.Program;
import net.irisshaders.iris.pathways.FullScreenQuadRenderer;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.targets.RenderTarget;
import net.irisshaders.iris.targets.RenderTargets;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL45C;

import java.util.HashMap;
import java.util.Map;

public class ShaderManager {
    private static final ShaderManager instance = new ShaderManager();
    private static final boolean enabled = isIrisInstalled();
    public static Map<ShaderForm, Boolean> programEnabled = new HashMap<>();

    private static IrisRenderingPipeline pipeline = null;
    private static RenderTargets renderTargets = null;

    public ShaderManager() {
        if (!enabled) return;

        init();
    }

    private void init() {
        try {
            pipeline = (IrisRenderingPipeline) Iris.getPipelineManager().getPipelineNullable();
        } catch (ClassCastException e) {
            LogUtils.getLogger().debug("Iris rendering is disabled");
            return;
        }
        if (pipeline == null) return;

        // Get render targets using reflection
        try {
            Class<?> clazz = Class.forName("net.irisshaders.iris.pipeline.IrisRenderingPipeline");
            renderTargets = (RenderTargets) clazz.getDeclaredField("renderTargets").get(pipeline);
        } catch (Exception e) {
            LogUtils.getLogger().error("Failed to retrieve renderTargets from iris render pipeline: " + e);
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

    public static void render() {
        if (!enabled) return;
        if (pipeline == null || renderTargets == null) return;



        for (ShaderForm shaderForm : programEnabled.keySet()) {
            if (programEnabled.get(shaderForm)) {
                programEnabled.put(shaderForm, false);
                Program program = shaderForm.getProgram();
                if (program == null) continue;
                FullScreenQuadRenderer.INSTANCE.begin();

                // Bind frame buffer
                renderTargets.getDepthTextureNoTranslucents().bind();
                // Use the program
                program.use();

                // Push custom uniforms if we have them
                // shaderForm.pushUniforms(program);

                // Render a full screen quad to execute the shader
                FullScreenQuadRenderer.INSTANCE.renderQuad();
                // Ensure shader running is complete
                IrisRenderSystem.memoryBarrier(GL45C.GL_ALL_BARRIER_BITS);
                FullScreenQuadRenderer.INSTANCE.end();
            } else {
                remove(shaderForm);
                shaderForm.destroyProgram();
            }
        }

        // Clean up state
        Program.unbind();
        GlStateManager._glUseProgram(0);
    }

}
