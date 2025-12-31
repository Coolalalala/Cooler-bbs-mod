package mchorse.bbs_mod.ui.forms.editors.panels;

import mchorse.bbs_mod.forms.forms.SSBOForm;
import mchorse.bbs_mod.ui.UIKeys;
import mchorse.bbs_mod.ui.forms.editors.forms.UIForm;
import mchorse.bbs_mod.ui.framework.elements.buttons.UICirculate;
import mchorse.bbs_mod.ui.framework.elements.buttons.UIToggle;
import mchorse.bbs_mod.ui.framework.elements.input.UITrackpad;
import mchorse.bbs_mod.ui.framework.elements.input.text.UITextbox;
import mchorse.bbs_mod.ui.utils.UI;
import org.lwjgl.opengl.GL43;

public class UISSBOFormPanel extends UIFormPanel<SSBOForm> {
    public UITextbox name;
    public UITrackpad binding;
    public UIToggle dynamic;
    public UITrackpad scaleX;
    public UITrackpad scaleY;
    public UITrackpad size;
    public UITrackpad capacity;
    public UICirculate clearType;
    public UITrackpad clearValue;

    public UISSBOFormPanel(UIForm<SSBOForm> editor) {
        super(editor);

        this.name = new UITextbox(10000, (t) -> this.form.name.set(t));
        this.binding = new UITrackpad((value) -> this.form.binding.set(value.intValue()));
        this.binding.limit(0, GL43.glGetInteger(GL43.GL_MAX_SHADER_STORAGE_BUFFER_BINDINGS), true).increment(1);
        this.dynamic = new UIToggle(UIKeys.FORMS_EDITOR_SSBO_DYNAMIC, (b) -> this.form.dynamic.set(b.getValue()));
        this.scaleX = new UITrackpad((value) -> this.form.scaleX.set(value.floatValue()));
        this.scaleY = new UITrackpad((value) -> this.form.scaleY.set(value.floatValue()));
        this.size = new UITrackpad((value) -> this.form.size.set(value.intValue()));
        this.size.limit(1, GL43.glGetInteger(GL43.GL_MAX_SHADER_STORAGE_BLOCK_SIZE), true).increment(1);
        this.capacity = new UITrackpad((value) -> this.form.capacity.set(value.intValue()));
        this.capacity.limit(1, Integer.MAX_VALUE, true).increment(1); // uhhh
        this.clearType = new UICirculate((c) -> this.form.clearType.set(c.getValue()));
        this.clearType.tooltip(UIKeys.FORMS_EDITOR_SSBO_CLEARTYPE_TOOLTIP);
        this.clearType.addLabel(UIKeys.FORMS_EDITOR_SSBO_CLEARTYPE_NONE);
        this.clearType.addLabel(UIKeys.FORMS_EDITOR_SSBO_CLEARTYPE_FRAME);
        this.clearType.addLabel(UIKeys.FORMS_EDITOR_SSBO_CLEARTYPE_DISPATCH);
        this.clearValue = new UITrackpad((value) -> this.form.clearValue.set(value.floatValue()));

        this.options.add(UI.label(UIKeys.FORMS_EDITOR_SSBO_NAME), this.name);
        this.options.add(UI.label(UIKeys.FORMS_EDITOR_SSBO_BINDING), this.binding);
        this.options.add(this.dynamic);
        this.options.add(UI.label(UIKeys.FORMS_EDITOR_SSBO_SCALE), this.scaleX, this.scaleY);
        this.options.add(UI.label(UIKeys.FORMS_EDITOR_SSBO_SIZE), this.size);
        this.options.add(UI.label(UIKeys.FORMS_EDITOR_SSBO_CAPACITY), this.capacity);
        this.options.add(UI.label(UIKeys.FORMS_EDITOR_SSBO_CLEARTYPE), this.clearType);
        this.options.add(UI.label(UIKeys.FORMS_EDITOR_SSBO_CLEARVALUE), this.clearValue);
    }

    @Override
    public void startEdit(SSBOForm form) {
        super.startEdit(form);

        this.name.setText(form.name.toString());
        this.binding.setValue(form.binding.get());
        this.dynamic.setValue(form.dynamic.get());
        this.size.setValue(form.size.get());
        this.capacity.setValue(form.capacity.get());
        this.clearType.setValue(form.clearType.get());
        this.clearValue.setValue(form.clearValue.get());
    }
}
