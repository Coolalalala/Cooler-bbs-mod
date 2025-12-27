package mchorse.bbs_mod.ui.forms.editors.panels;

import mchorse.bbs_mod.forms.ShaderManager;
import mchorse.bbs_mod.forms.forms.ShaderForm;
import mchorse.bbs_mod.ui.UIKeys;
import mchorse.bbs_mod.ui.forms.editors.forms.UIForm;
import mchorse.bbs_mod.ui.framework.elements.buttons.UIButton;
import mchorse.bbs_mod.ui.framework.elements.buttons.UICirculate;
import mchorse.bbs_mod.ui.framework.elements.buttons.UIIcon;
import mchorse.bbs_mod.ui.framework.elements.input.UITrackpad;
import mchorse.bbs_mod.ui.framework.elements.input.text.UITextEditor;
import mchorse.bbs_mod.ui.framework.elements.input.text.UITextbox;
import mchorse.bbs_mod.ui.utils.UI;
import mchorse.bbs_mod.ui.utils.icons.Icons;

import java.util.function.Consumer;

public class UIShaderFormPanel<T extends ShaderForm> extends UIFormPanel<T> {
    public UITextEditor textEditor;
    public UITextbox name;
    public UIButton vertex;
    public UIButton fragment;
    public UIButton geometry;
//    public UIButton tessellationControl;
//    public UIButton tessellationEvaluation;
//    public UIButton compute;
    public UITrackpad priority;
    public UICirculate renderStage;
    public UIButton recompileAll;

    public UIShaderFormPanel(UIForm<T> editor) {
        super(editor);

        // Create text editor
        this.textEditor = new UITextEditor(null);
        this.textEditor.background().relative(editor).y(1F, -200).w(1F).h(200).setVisible(false);
        editor.add(this.textEditor);
        // Add close button to editor
        UIIcon close = new UIIcon(Icons.CLOSE, (b) -> this.editProgramSrc(null, null));
        close.relative(this.textEditor).x(1F, -20);
        this.textEditor.add(close);

        // Create options
        this.name = new UITextbox(1024, (str) -> this.form.name.set(str));
        this.vertex = new UIButton(UIKeys.FORMS_EDITOR_SHADER_VERTEX, (v) -> this.editProgramSrc(this::setVertex, this.form.vertex.toString()));
        this.fragment = new UIButton(UIKeys.FORMS_EDITOR_SHADER_FRAGMENT, (v) -> this.editProgramSrc(this::setFragment, this.form.fragment.toString()));
        this.geometry = new UIButton(UIKeys.FORMS_EDITOR_SHADER_GEOMETRY, (v) -> this.editProgramSrc(this::setGeometry, this.form.geometry.toString()));
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
        this.recompileAll = new UIButton(UIKeys.FORMS_EDITOR_SHADER_RECOMPILE, (b) -> ShaderManager.reCompile());

        this.options.add(UI.label(UIKeys.FORMS_EDITOR_SHADER_NAME), this.name);
        this.options.add(this.vertex, this.geometry, this.fragment);
        this.options.add(UI.label(UIKeys.FORMS_EDITOR_SHADER_PRIORITY), this.priority);
        this.options.add(UI.label(UIKeys.FORMS_EDITOR_SHADER_RENDER_STAGE), this.renderStage);
        this.options.add(this.recompileAll);
    }

    @Override
    public void startEdit(T form) {
        super.startEdit(form);

        this.name.setText(form.name.toString());
        this.priority.setValue(form.priority.get());
        this.renderStage.setValue(form.renderStage.get());
    }

    private void setVertex(String str) {
        this.form.vertex.set(str);
        this.form.markDirty();
    }
    private void setFragment(String str) {
        this.form.fragment.set(str);
        this.form.markDirty();
    }
    private void setGeometry(String str) {
        this.form.geometry.set(str);
        this.form.markDirty();
    }

    public void editProgramSrc(Consumer<String> callback, String src)
    {
        this.textEditor.callback = callback;
        this.textEditor.setText(src == null ? "" : src);
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
