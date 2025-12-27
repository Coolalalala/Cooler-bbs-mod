package mchorse.bbs_mod.forms.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import mchorse.bbs_mod.cubic.ModelInstance;
import mchorse.bbs_mod.cubic.data.model.Model;
import mchorse.bbs_mod.cubic.data.model.ModelGroup;
import mchorse.bbs_mod.cubic.render.ICubicRenderer;
import mchorse.bbs_mod.cubic.render.vao.ModelVAO;
import mchorse.bbs_mod.forms.FormUtilsClient;
import mchorse.bbs_mod.forms.ShaderManager;
import mchorse.bbs_mod.forms.entities.IEntity;
import mchorse.bbs_mod.forms.forms.BodyPart;
import mchorse.bbs_mod.forms.forms.GBufferShaderForm;
import mchorse.bbs_mod.graphics.texture.Texture;
import mchorse.bbs_mod.resources.Link;
import mchorse.bbs_mod.ui.framework.UIContext;
import mchorse.bbs_mod.utils.MatrixStackUtils;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

import java.util.Map;

public class GBufferShaderFormRenderer extends FormRenderer<GBufferShaderForm> {
    public static final Link ICON_TEXTURE = Link.assets("textures/gbuffer_shader.png");



    public GBufferShaderFormRenderer(GBufferShaderForm form) {
        super(form);
    }

    @Override
    protected void renderInUI(UIContext context, int x1, int y1, int x2, int y2) {
        Texture texture = context.render.getTextures().getTexture(ICON_TEXTURE);

        int w = texture.width;
        int h = texture.height;
        int x = (x1 + x2) / 2;
        int y = (y1 + y2) / 2;

        context.batcher.fullTexturedBox(texture, x - w / 2f, y - h / 2f, w, h);
    }

    protected void render3D(FormRenderingContext context) {
        ShaderManager.register(this.form, this.form.renderStage.get());
    }

    @Override
    protected void renderBodyPart(BodyPart part, FormRenderingContext context)
    {
        IEntity oldEntity = context.entity;

        context.entity = part.useTarget.get() ? oldEntity : part.getEntity();

        if (part.getForm() != null)
        {
            context.stack.push();
            MatrixStackUtils.applyTransform(context.stack, part.transform.get());

            // Register for custom rendering
            FormRenderer<?> childRenderer = FormUtilsClient.getRenderer(part.getForm());
            if (childRenderer instanceof ModelFormRenderer modelRenderer) {
                ModelInstance modelInstance = modelRenderer.getModel();
                if  (modelInstance != null && modelInstance.getModel() instanceof Model model) {
                    Map<ModelGroup, ModelVAO> vaos = modelInstance.getVaos();

                    for (ModelGroup topGroup : model.topGroups) {
                        registerVAORecursive(context.stack, vaos, topGroup, RenderSystem.getProjectionMatrix(), context);
                    }
                }
            }

            if (this.form.sendChildren.get()) {
                // Regular rendering as usual
                FormUtilsClient.render(part.getForm(), context);
            }

            context.stack.pop();
        }

        context.entity = oldEntity;
    }

    /**
     * Register all modelInstance groups VAO data to the ShaderManager recursively
     */
    private void registerVAORecursive(MatrixStack stack, Map<ModelGroup, ModelVAO> vaos, ModelGroup group, Matrix4f projMatrix, FormRenderingContext context)
    {
        if  (!group.visible) return;

        stack.push();
        ICubicRenderer.aapplyGroupTransformations(stack, group);
        ModelVAO vao = vaos.get(group);
        if (vao != null) ShaderManager.addVAO(this.form, vao, projMatrix.get(new Matrix4f()).mul(context.stack.peek().getPositionMatrix()));

        for (ModelGroup childGroup : group.children) {
            registerVAORecursive(stack, vaos, childGroup, projMatrix, context);
        }

        stack.pop();
    }
}