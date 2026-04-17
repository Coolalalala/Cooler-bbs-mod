package mchorse.bbs_mod.forms.renderers;

import mchorse.bbs_mod.forms.FormUtilsClient;
import mchorse.bbs_mod.forms.ShaderManager;
import mchorse.bbs_mod.forms.entities.IEntity;
import mchorse.bbs_mod.forms.forms.*;
import mchorse.bbs_mod.graphics.texture.Texture;
import mchorse.bbs_mod.resources.Link;
import mchorse.bbs_mod.ui.framework.UIContext;
import mchorse.bbs_mod.utils.MatrixStackUtils;

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

        Form childrenForm  = part.getForm();
        if (childrenForm != null)
        {
            context.stack.push();
            MatrixStackUtils.applyTransform(context.stack, part.transform.get());

            // Register for custom rendering
            if (childrenForm instanceof ModelForm modelForm) {

                ShaderManager.addModelGroup(this.form, modelForm, context);

            } else if (childrenForm instanceof GLVertexForm vertexForm) {

                ShaderManager.addGLVertex(this.form, vertexForm, context);

            }

            if (this.form.renderChildren.get()) {
                // Render normally
                FormUtilsClient.render(childrenForm, context);
            }

            context.stack.pop();
        }

        context.entity = oldEntity;
    }
}
