package mchorse.bbs_mod.ui.forms.editors.panels;

import mchorse.bbs_mod.forms.forms.ShaderForm;
import mchorse.bbs_mod.ui.UIKeys;
import mchorse.bbs_mod.ui.forms.editors.forms.UIForm;
import mchorse.bbs_mod.ui.framework.elements.buttons.UIButton;
import mchorse.bbs_mod.ui.framework.elements.buttons.UIIcon;
import mchorse.bbs_mod.ui.framework.elements.buttons.UIToggle;
import mchorse.bbs_mod.ui.framework.elements.input.text.UITextEditor;
import mchorse.bbs_mod.ui.utils.icons.Icons;

import java.util.function.Consumer;

public class UIShaderPanel <T extends ShaderForm> extends UIFormPanel<T> {
    public UITextEditor textEditor;
    public UIButton vertex;
    public UIButton fragment;
    public UIButton geometry;
    public UIButton tessellationControl;
    public UIButton tessellationEvaluation;
    public UIButton compute;
    public UIToggle sendTranslate;
    public UIToggle sendRotate;
    public UIToggle sendScale;
    public UIToggle sendParents;
    public UIToggle sendChildren;

    public UIShaderPanel(UIForm<T> editor) {
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
        this.vertex = new UIButton(UIKeys.FORMS_EDITORS_SHADER_VERTEX, (v) -> editProgramSrc((str) -> this.form.vertex.set(str), this.form.vertex.toString()));
        this.fragment = new UIButton(UIKeys.FORMS_EDITORS_SHADER_FRAGMENT, (v) -> editProgramSrc((str) -> this.form.fragment.set(str), this.form.fragment.toString()));
        this.geometry = new UIButton(UIKeys.FORMS_EDITORS_SHADER_GEOMETRY, (v) -> editProgramSrc((str) -> this.form.geometry.set(str), this.form.geometry.toString()));
        this.tessellationControl = new UIButton(UIKeys.FORMS_EDITORS_SHADER_TESSELLATION_CONTROL, (v) -> editProgramSrc((str) -> this.form.tessellationControl.set(str), this.form.tessellationControl.toString()));
        this.tessellationEvaluation = new UIButton(UIKeys.FORMS_EDITORS_SHADER_TESSELLATION_EVALUATION, (v) -> editProgramSrc((str) -> this.form.tessellationEvaluation.set(str), this.form.tessellationEvaluation.toString()));
        this.compute = new UIButton(UIKeys.FORMS_EDITORS_SHADER_COMPUTE, (v) -> editProgramSrc((str) -> this.form.compute.set(str), this.form.compute.toString()));
        this.sendTranslate = new UIToggle(UIKeys.FORMS_EDITORS_SHADER_SEND_TRANSLATE, (t) -> this.form.sendTranslate.set(t.getValue()));
        this.sendRotate = new UIToggle(UIKeys.FORMS_EDITORS_SHADER_SEND_ROTATE, (t) -> this.form.sendRotate.set(t.getValue()));
        this.sendScale = new UIToggle(UIKeys.FORMS_EDITORS_SHADER_SEND_SCALE, (t) -> this.form.sendScale.set(t.getValue()));
        this.sendParents = new UIToggle(UIKeys.FORMS_EDITORS_SHADER_SEND_PARENTS, (t) -> this.form.sendParents.set(t.getValue()));
        this.sendChildren = new UIToggle(UIKeys.FORMS_EDITORS_SHADER_SEND_CHILDREN, (t) -> this.form.sendChildren.set(t.getValue()));


        this.options.add(this.vertex, this.fragment, this.geometry, this.tessellationControl, this.tessellationEvaluation, this.compute,
                         this.sendTranslate, this.sendRotate, this.sendScale, this.sendParents, this.sendChildren);
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
