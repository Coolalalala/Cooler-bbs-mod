package mchorse.bbs_mod.forms.renderers;

import mchorse.bbs_mod.forms.forms.LightForm;
import mchorse.bbs_mod.graphics.texture.Texture;
import mchorse.bbs_mod.resources.Link;
import mchorse.bbs_mod.ui.framework.UIContext;

public class LightFormRenderer extends FormRenderer<LightForm> {
    public static final Link LIGHT_PREVIEW = Link.assets("textures/light.png");

    public LightFormRenderer(LightForm form) {
        super(form);
    }

    @Override
    protected void renderInUI(UIContext context, int x1, int y1, int x2, int y2) {
        Texture texture = context.render.getTextures().getTexture(LIGHT_PREVIEW);

        int w = texture.width;
        int h = texture.height;
        int x = (x1 + x2) / 2;
        int y = (y1 + y2) / 2;

        context.batcher.fullTexturedBox(texture, x - w/2f, y - h/2f, w, h);
    }

    @Override
    protected void render3D(FormRenderingContext context) {
        return; // TODO: pass SSBO to shader
    }
}
