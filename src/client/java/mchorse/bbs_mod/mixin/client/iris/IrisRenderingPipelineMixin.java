package mchorse.bbs_mod.mixin.client.iris;

import mchorse.bbs_mod.forms.ShaderManager;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to inject custom shader rendering into the Iris pipeline
 * <p>
 * This injects after composite rendering but before the final pass,
 * allowing custom shaders to run with full access to Iris render targets.
 */
@Mixin(IrisRenderingPipeline.class)
public class IrisRenderingPipelineMixin
{
    /**
     * Inject custom shader rendering after prepare passes
     */
    @Inject(
            method = "renderShadows",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/irisshaders/iris/pipeline/CompositeRenderer;renderAll()V",
                    shift = At.Shift.AFTER
            ),
            remap = false
    )
    private void bbs$injectCustomPrepare(CallbackInfo ci)
    {
        // Render custom shaders in the prepare stage
        ShaderManager.renderPrepareStage();
    }

    /**
     * Inject custom shader rendering after Gbuffer passes but before deferred passes
     */
    @Inject(
            method = "beginTranslucents()V",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/irisshaders/iris/pipeline/IrisRenderingPipeline;isBeforeTranslucent:Z",
                    shift = At.Shift.AFTER,
                    opcode = Opcodes.PUTFIELD
            ),
            remap = false
    )
    private void bbs$injectCustomGBuffer(CallbackInfo ci)
    {
        // Render custom shaders in the opaque stage
        ShaderManager.renderGBufferStage();
    }

    /**
     * Inject custom shader rendering after deferred passes
     */
    @Inject(
            method = "beginTranslucents()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/irisshaders/iris/targets/RenderTargets;copyPreTranslucentDepth()V",
                    shift = At.Shift.AFTER
            ),
            remap = false
    )
    private void bbs$injectCustomDeferred(CallbackInfo ci)
    {
        // Render custom shaders in the translucent stage
        ShaderManager.renderDeferredStage();
    }

    /**
     * Inject custom shader rendering after composite passes but before final pass
     */
    @Inject(
            method = "finalizeLevelRendering()V",
            at = @At(
                value = "FIELD",
                target = "Lnet/irisshaders/iris/pipeline/IrisRenderingPipeline;isRenderingWorld:Z",
                shift = At.Shift.AFTER,
                opcode = Opcodes.PUTFIELD
            ),
            remap = false
    )
    private void bbs$injectCustomComposite(CallbackInfo ci)
    {
        // Render custom shaders in the composite stage
        ShaderManager.renderCompositeStage();
    }

    /**
     * Inject custom shader rendering after final passes
     */
    @Inject(
            method = "finalizeLevelRendering()V",
            at = @At(
                    value = "TAIL"
            ),
            remap = false
    )
    private void bbs$injectCustomFinal(CallbackInfo ci)
    {
        // Render custom shaders in the composite stage
        ShaderManager.renderFinalStage();
    }

    /**
     * Hook into destroy function so all stuff gets cleaned up
     */
    @Inject(
            method = "destroy()V",
            at = @At("TAIL"),
            remap = false
    )
    private void bbs$destroy(CallbackInfo ci) {
        ShaderManager.destroy();
    }
}