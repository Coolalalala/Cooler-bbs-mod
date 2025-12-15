package mchorse.bbs_mod.forms.renderers;

import mchorse.bbs_mod.forms.forms.ShaderForm;
import mchorse.bbs_mod.resources.Link;
import mchorse.bbs_mod.ui.framework.UIContext;

public class ShaderFormRenderer <T extends ShaderForm> extends FormRenderer<T> {
    public static final Link ICON_TEXTURE = Link.assets("textures/shader.png");

    public ShaderFormRenderer(T form) {
        super(form);
    }

    @Override
    protected void renderInUI(UIContext context, int x1, int y1, int x2, int y2) {
    }
}
