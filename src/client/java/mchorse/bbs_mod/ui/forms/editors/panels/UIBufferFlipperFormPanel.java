package mchorse.bbs_mod.ui.forms.editors.panels;

import com.google.common.collect.ImmutableSet;
import mchorse.bbs_mod.forms.ShaderManager;
import mchorse.bbs_mod.forms.forms.BufferFlipperForm;
import mchorse.bbs_mod.ui.UIKeys;
import mchorse.bbs_mod.ui.forms.editors.forms.UIForm;
import mchorse.bbs_mod.ui.framework.elements.buttons.UIButton;
import mchorse.bbs_mod.ui.framework.elements.buttons.UICirculate;
import mchorse.bbs_mod.ui.framework.elements.buttons.UIToggle;
import mchorse.bbs_mod.ui.framework.elements.input.UITrackpad;
import mchorse.bbs_mod.ui.framework.elements.input.text.UITextbox;
import mchorse.bbs_mod.ui.utils.UI;

import java.util.Set;

public class UIBufferFlipperFormPanel extends UIFormPanel<BufferFlipperForm> {
    public UITextbox name;
    public UIToggle flip0;
    public UIToggle flip1;
    public UIToggle flip2;
    public UIToggle flip3;
    public UIToggle flip4;
    public UIToggle flip5;
    public UIToggle flip6;
    public UIToggle flip7;
    public UITrackpad priority;
    public UICirculate renderStage;
    public UIButton recompileAll;

    public UIBufferFlipperFormPanel(UIForm<BufferFlipperForm> editor) {
        super(editor);

        // Create options
        this.name = new UITextbox(1024, (str) -> this.form.name.set(str));
        this.flip0 = new UIToggle(UIKeys.FORMS_EDITOR_SHADER_FLIP_0, (b) -> this.toggleBuffer(0, b.getValue()));
        this.flip1 = new UIToggle(UIKeys.FORMS_EDITOR_SHADER_FLIP_1, (b) -> this.toggleBuffer(1, b.getValue()));
        this.flip2 = new UIToggle(UIKeys.FORMS_EDITOR_SHADER_FLIP_2, (b) -> this.toggleBuffer(2, b.getValue()));
        this.flip3 = new UIToggle(UIKeys.FORMS_EDITOR_SHADER_FLIP_3, (b) -> this.toggleBuffer(3, b.getValue()));
        this.flip4 = new UIToggle(UIKeys.FORMS_EDITOR_SHADER_FLIP_4, (b) -> this.toggleBuffer(4, b.getValue()));
        this.flip5 = new UIToggle(UIKeys.FORMS_EDITOR_SHADER_FLIP_5, (b) -> this.toggleBuffer(5, b.getValue()));
        this.flip6 = new UIToggle(UIKeys.FORMS_EDITOR_SHADER_FLIP_6, (b) -> this.toggleBuffer(6, b.getValue()));
        this.flip7 = new UIToggle(UIKeys.FORMS_EDITOR_SHADER_FLIP_7, (b) -> this.toggleBuffer(7, b.getValue()));
        this.priority = new UITrackpad((v) -> this.form.priority.set(v.intValue()));
        this.priority.integer().limit(1);
        this.priority.tooltip(UIKeys.FORMS_EDITOR_SHADER_PRIORITY_TOOLTIP);
        this.renderStage = new UICirculate((c) -> this.form.renderStage.set(c.getValue()));
        this.renderStage.tooltip(UIKeys.FORMS_EDITOR_SHADER_RENDER_STAGE_TOOLTIP);
        this.renderStage.addLabel(UIKeys.FORMS_EDITOR_SHADER_RENDER_STAGE_BEGIN);
        this.renderStage.addLabel(UIKeys.FORMS_EDITOR_SHADER_RENDER_STAGE_PREPARE);
        this.renderStage.addLabel(UIKeys.FORMS_EDITOR_SHADER_RENDER_STAGE_DEFERRED);
        this.renderStage.addLabel(UIKeys.FORMS_EDITOR_SHADER_RENDER_STAGE_COMPOSITE);
        this.renderStage.addLabel(UIKeys.FORMS_EDITOR_SHADER_RENDER_STAGE_END);
        this.recompileAll = new UIButton(UIKeys.FORMS_EDITOR_SHADER_RECOMPILE, (b) -> ShaderManager.recompile());

        this.options.add(UI.label(UIKeys.FORMS_EDITOR_SHADER_NAME), this.name);
        this.options.add(this.flip0, this.flip1, this.flip2, this.flip3, this.flip4, this.flip5, this.flip6, this.flip7);
        this.options.add(UI.label(UIKeys.FORMS_EDITOR_SHADER_PRIORITY), this.priority);
        this.options.add(UI.label(UIKeys.FORMS_EDITOR_SHADER_RENDER_STAGE), this.renderStage);
        this.options.add(this.recompileAll);
    }

    @Override
    public void startEdit(BufferFlipperForm form) {
        super.startEdit(form);

        this.name.setText(form.name.toString());
        this.priority.setValue(form.priority.get());
        this.renderStage.setValue(form.renderStage.get());
    }

    private void toggleBuffer(int buffer, boolean state) {
        Set<Integer> current = this.form.get();
        boolean contains = current.contains(buffer);

        if (contains == state) return; // No change needed

        Set<Integer> result = state ?
                ImmutableSet.<Integer>builder().addAll(current).add(buffer).build() :
                ImmutableSet.copyOf(current.stream()
                        .filter(b -> b != buffer)
                        .collect(java.util.stream.Collectors.toSet()));

        this.form.set(ImmutableSet.copyOf(result));
    }

}
