package mchorse.bbs_mod.forms.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import mchorse.bbs_mod.BBSModClient;
import mchorse.bbs_mod.forms.forms.ExtrudedForm;
import mchorse.bbs_mod.graphics.texture.Texture;
import mchorse.bbs_mod.resources.Link;
import mchorse.bbs_mod.ui.framework.UIContext;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.GameRenderer;

public class ExtrudedFormRenderer extends FormRenderer<ExtrudedForm>
{
    public ExtrudedFormRenderer(ExtrudedForm form)
    {
        super(form);
    }

    @Override
    public void renderUI(UIContext context, int x1, int y1, int x2, int y2)
    {
        Link t = this.form.texture.get(context.getTransition());

        if (t == null)
        {
            return;
        }

        Texture texture = context.render.getTextures().getTexture(t);

        float min = Math.min(texture.width, texture.height);
        int ow = (x2 - x1) - 4;
        int oh = (y2 - y1) - 4;

        int w = (int) ((texture.width / min) * ow);
        int h = (int) ((texture.height / min) * ow);

        int x = x1 + (ow - w) / 2 + 2;
        int y = y1 + (oh - h) / 2 + 2;

        context.batcher.fullTexturedBox(texture, x, y, w, h);
    }

    @Override
    protected void render3D(FormRenderingContext context)
    {
        Link texture = this.form.texture.get(context.getTransition());
        VertexBuffer vertexBuffer = BBSModClient.getTextures().getExtruder().get(texture);

        if (vertexBuffer != null)
        {
            RenderSystem.setShaderTexture(0, BBSModClient.getTextures().getTexture(texture).id);

            vertexBuffer.bind();
            vertexBuffer.draw(context.stack.peek().getPositionMatrix(), RenderSystem.getProjectionMatrix(), GameRenderer.getPositionTexColorProgram());
            VertexBuffer.unbind();
        }
    }
}