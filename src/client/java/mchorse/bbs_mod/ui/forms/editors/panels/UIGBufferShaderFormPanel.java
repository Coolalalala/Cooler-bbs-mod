package mchorse.bbs_mod.ui.forms.editors.panels;

import mchorse.bbs_mod.forms.forms.GBufferShaderForm;
import mchorse.bbs_mod.ui.UIKeys;
import mchorse.bbs_mod.ui.forms.editors.forms.UIForm;
import mchorse.bbs_mod.ui.framework.elements.buttons.UIToggle;

public class UIGBufferShaderFormPanel extends UIShaderFormPanel<GBufferShaderForm>   {
    public UIToggle sendChildren;
    public UIToggle pingpong;

    public UIGBufferShaderFormPanel(UIForm<GBufferShaderForm> editor) {
        super(editor);
        this.sendChildren = new UIToggle(UIKeys.FORMS_EDITOR_SHADER_SEND_CHILDREN, (t) -> this.form.renderChildren.set(t.getValue()));
        this.pingpong = new UIToggle(UIKeys.FORMS_EDITOR_SHADER_PINGPONG, (t) -> this.form.pingpong.set(t.getValue()));

        this.options.add(this.sendChildren, this.pingpong);
    }

    @Override
    public void startEdit(GBufferShaderForm form) {
        super.startEdit(form);

        this.sendChildren.setValue(form.renderChildren.get());
        this.pingpong.setValue(form.pingpong.get());
    }
}
