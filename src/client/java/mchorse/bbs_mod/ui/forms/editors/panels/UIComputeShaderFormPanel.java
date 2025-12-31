package mchorse.bbs_mod.ui.forms.editors.panels;

import mchorse.bbs_mod.forms.ShaderManager;
import mchorse.bbs_mod.forms.forms.ComputeShaderForm;
import mchorse.bbs_mod.ui.UIKeys;
import mchorse.bbs_mod.ui.forms.editors.forms.UIForm;
import mchorse.bbs_mod.ui.framework.elements.buttons.UIButton;
import mchorse.bbs_mod.ui.framework.elements.buttons.UICirculate;
import mchorse.bbs_mod.ui.framework.elements.buttons.UIIcon;
import mchorse.bbs_mod.ui.framework.elements.buttons.UIToggle;
import mchorse.bbs_mod.ui.framework.elements.input.UITrackpad;
import mchorse.bbs_mod.ui.framework.elements.input.text.UITextEditor;
import mchorse.bbs_mod.ui.framework.elements.input.text.UITextbox;
import mchorse.bbs_mod.ui.utils.UI;
import mchorse.bbs_mod.ui.utils.icons.Icons;
import org.lwjgl.opengl.GL43;

import java.util.function.Consumer;

public class UIComputeShaderFormPanel extends UIFormPanel<ComputeShaderForm> {
    public UITextEditor textEditor;
    public UITextbox name;
    public UIButton source;
    public UIToggle relative;
    public UITrackpad relativeX;
    public UITrackpad relativeY;
    public UITrackpad workGroupsX;
    public UITrackpad workGroupsY;
    public UITrackpad workGroupsZ;
    public UITrackpad priority;
    public UICirculate renderStage;
    public UIButton recompileAll;

    public UIComputeShaderFormPanel(UIForm<ComputeShaderForm> editor) {
        super(editor);

        // Create text editor
        this.textEditor = new UITextEditor(null);
        this.textEditor.background().relative(editor).y(1F, -200).w(1F).h(200).setVisible(false);
        editor.add(this.textEditor);
        // Add close button to editor
        UIIcon close = new UIIcon(Icons.CLOSE, (b) -> this.editProgramSrc(null));
        close.relative(this.textEditor).x(1F, -20);
        this.textEditor.add(close);

        // Create options
        this.name = new UITextbox(1024, (str) -> this.form.name.set(str));
        this.source = new UIButton(UIKeys.FORMS_EDITOR_SHADER_COMPUTE, (v) -> this.editProgramSrc(this::setSource));
        this.relative = new UIToggle(UIKeys.FORMS_EDITOR_SHADER_RELATIVE, (b) -> this.form.relative.set(b.getValue()));
        this.relativeX = new UITrackpad((v) -> this.form.relativeX.set(v.floatValue()));
        this.relativeY = new UITrackpad((v) -> this.form.relativeY.set(v.floatValue()));
        this.workGroupsX = new UITrackpad((v) -> this.form.workGroupsX.set(v.intValue()));
        this.workGroupsY = new UITrackpad((v) -> this.form.workGroupsY.set(v.intValue()));
        this.workGroupsZ = new UITrackpad((v) -> this.form.workGroupsZ.set(v.intValue()));
        this.workGroupsX.integer().limit(1, GL43.glGetIntegeri(GL43.GL_MAX_COMPUTE_WORK_GROUP_SIZE, 0));
        this.workGroupsY.integer().limit(1, GL43.glGetIntegeri(GL43.GL_MAX_COMPUTE_WORK_GROUP_SIZE, 1));
        this.workGroupsZ.integer().limit(1, GL43.glGetIntegeri(GL43.GL_MAX_COMPUTE_WORK_GROUP_SIZE, 2));
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
        this.options.add(this.source);
        this.options.add(this.relative, UI.label(UIKeys.FORMS_EDITOR_SHADER_RELATIVE_SCALE), this.relativeX, this.relativeY);
        this.options.add(UI.label(UIKeys.FORMS_EDITOR_SHADER_WORKGROUPS), this.workGroupsX, this.workGroupsY, this.workGroupsZ);
        this.options.add(UI.label(UIKeys.FORMS_EDITOR_SHADER_PRIORITY), this.priority);
        this.options.add(UI.label(UIKeys.FORMS_EDITOR_SHADER_RENDER_STAGE), this.renderStage);
        this.options.add(this.recompileAll);
    }

    @Override
    public void startEdit(ComputeShaderForm form) {
        super.startEdit(form);

        this.name.setText(form.name.toString());
        this.relative.setValue(form.relative.get());
        this.relativeX.setValue(form.relativeX.get());
        this.relativeY.setValue(form.relativeY.get());
        this.workGroupsX.setValue(form.workGroupsX.get());
        this.workGroupsY.setValue(form.workGroupsY.get());
        this.workGroupsZ.setValue(form.workGroupsZ.get());
        this.priority.setValue(form.priority.get());
        this.renderStage.setValue(form.renderStage.get());
    }

    private void setSource(String str) {
        this.form.computeSource.set(str);
        this.form.markDirty();
    }

    public void editProgramSrc(Consumer<String> callback)
    {
        this.textEditor.callback = callback;
        this.textEditor.setText(this.form.computeSource.get());
        this.textEditor.setVisible(callback != null);

        if (callback != null)
        {
            this.options.hTo(this.textEditor.area);
        }
        else
        {
            this.options.h(1F);
        }

        this.options.resize();
    }
}
