package mchorse.bbs_mod.ui.forms.editors.panels;

import mchorse.bbs_mod.forms.forms.GBufferShaderForm;
import mchorse.bbs_mod.ui.UIKeys;
import mchorse.bbs_mod.ui.forms.editors.forms.UIForm;
import mchorse.bbs_mod.ui.framework.elements.buttons.UIToggle;

public class UIGBufferShaderFormPanel extends UIShaderFormPanel<GBufferShaderForm>   {
    public UIToggle culling;
    public UIToggle depthTest;
    public UIToggle depthWrite;
    public UIToggle sendChildren;
    public UIToggle pingpong;

    public UIGBufferShaderFormPanel(UIForm<GBufferShaderForm> editor) {
        super(editor);
        this.culling = new UIToggle(UIKeys.FORMS_EDITOR_SHADER_CULLING, (t) -> this.form.culling.set(t.getValue()));
        this.depthTest = new UIToggle(UIKeys.FORMS_EDITOR_SHADER_DEPTHTEST, (t) -> this.form.depthTest.set(t.getValue()));
        this.depthWrite = new UIToggle(UIKeys.FORMS_EDITOR_SHADER_DEPTHWRITE, (t) -> this.setDepthWrite(t.getValue()));
        this.sendChildren = new UIToggle(UIKeys.FORMS_EDITOR_SHADER_SEND_CHILDREN, (t) -> this.form.renderChildren.set(t.getValue()));
        this.pingpong = new UIToggle(UIKeys.FORMS_EDITOR_SHADER_PINGPONG, (t) -> this.setPingpong(t.getValue()));

        this.options.add(this.culling, this.depthTest, this.depthWrite, this.sendChildren, this.pingpong);
    }

    private void setDepthWrite(boolean value) {
        this.form.depthWrite.set(value);
        this.form.markDirty();
    }

    private void setPingpong(boolean value) {
        this.form.pingpong.set(value);
        this.form.markDirty();
    }

    @Override
    public void startEdit(GBufferShaderForm form) {
        super.startEdit(form);

        this.culling.setValue(form.culling.get());
        this.depthTest.setValue(form.depthTest.get());
        this.depthWrite.setValue(form.depthWrite.get());
        this.sendChildren.setValue(form.renderChildren.get());
        this.pingpong.setValue(form.pingpong.get());
    }
}
