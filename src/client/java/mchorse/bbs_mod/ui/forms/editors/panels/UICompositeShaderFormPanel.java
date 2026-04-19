package mchorse.bbs_mod.ui.forms.editors.panels;

import mchorse.bbs_mod.forms.forms.CompositeShaderForm;
import mchorse.bbs_mod.ui.UIKeys;
import mchorse.bbs_mod.ui.forms.editors.forms.UIForm;
import mchorse.bbs_mod.ui.framework.elements.buttons.UIToggle;

public class UICompositeShaderFormPanel extends UIShaderFormPanel<CompositeShaderForm> {
    public UIToggle pingpong;

    public UICompositeShaderFormPanel(UIForm<CompositeShaderForm> editor) {
        super(editor);
        this.pingpong = new UIToggle(UIKeys.FORMS_EDITOR_SHADER_PINGPONG, (t) -> this.setPingpong(t.getValue()));

        this.options.add(this.pingpong);
    }

    private void setPingpong(boolean value) {
        this.form.pingpong.set(value);
        this.form.markDirty();
    }

    @Override
    public void startEdit(CompositeShaderForm form) {
        super.startEdit(form);

        this.pingpong.setValue(form.pingpong.get());
    }
}
