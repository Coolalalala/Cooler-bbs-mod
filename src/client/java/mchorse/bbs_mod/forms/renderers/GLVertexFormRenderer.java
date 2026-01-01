package mchorse.bbs_mod.forms.renderers;

import mchorse.bbs_mod.forms.forms.GLVertexForm;
import mchorse.bbs_mod.graphics.texture.Texture;
import mchorse.bbs_mod.resources.Link;
import mchorse.bbs_mod.ui.framework.UIContext;

public class GLVertexFormRenderer extends FormRenderer<GLVertexForm> {
    public static final Link ICON_TEXTURE = Link.assets("textures/gl_vertex.png");

    public GLVertexFormRenderer(GLVertexForm form) {
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
}
