package mchorse.bbs_mod.mixin.client.iris;

import mchorse.bbs_mod.forms.ShaderManager;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to inject custom shader rendering into the Iris pipeline
 *
 * This injects after composite rendering but before the final pass,
 * allowing custom shaders to run with full access to Iris render targets.
 */
@Mixin(IrisRenderingPipeline.class)
public class IrisRenderingPipelineMixin
{
    /**
     * Inject custom shader rendering after composite passes but before final pass
     */
    @Inject(
        method = "finalizeLevelRendering()V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/irisshaders/iris/pipeline/FinalPassRenderer;renderFinalPass()V",
            shift = At.Shift.BEFORE
        ),
        remap = false
    )
    private void bbs$injectCustomShaders(CallbackInfo ci)
    {
        // Render custom shaders in the composite stage
        ShaderManager.renderCompositeStage();
    }
}
