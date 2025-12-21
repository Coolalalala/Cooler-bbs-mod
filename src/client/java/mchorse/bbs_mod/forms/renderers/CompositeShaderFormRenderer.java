package mchorse.bbs_mod.forms.renderers;

import mchorse.bbs_mod.forms.ShaderManager;
import mchorse.bbs_mod.forms.forms.CompositeShaderForm;
import mchorse.bbs_mod.graphics.texture.Texture;
import mchorse.bbs_mod.resources.Link;
import mchorse.bbs_mod.ui.framework.UIContext;

public class CompositeShaderFormRenderer extends FormRenderer<CompositeShaderForm> {
    public static final Link ICON_TEXTURE = Link.assets("textures/composite_shader.png");

    public CompositeShaderFormRenderer(CompositeShaderForm form) {
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
}
