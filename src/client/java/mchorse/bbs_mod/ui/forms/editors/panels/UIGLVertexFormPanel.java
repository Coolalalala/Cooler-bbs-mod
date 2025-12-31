package mchorse.bbs_mod.ui.forms.editors.panels;

import mchorse.bbs_mod.forms.forms.GLVertexForm;
import mchorse.bbs_mod.ui.UIKeys;
import mchorse.bbs_mod.ui.forms.editors.forms.UIForm;
import mchorse.bbs_mod.ui.framework.elements.input.UIColor;
import mchorse.bbs_mod.ui.framework.elements.input.UITrackpad;
import mchorse.bbs_mod.ui.utils.UI;
import mchorse.bbs_mod.utils.colors.Color;

public class UIGLVertexFormPanel extends UIFormPanel<GLVertexForm> {
    public UIColor color;
    public UITrackpad count;
    public UITrackpad size;

    public UIGLVertexFormPanel(UIForm<GLVertexForm> editor) {
        super(editor);

        this.color = new UIColor((c) -> this.form.color.set(Color.rgb(c)));
        this.count = new UITrackpad((v) -> this.form.count.set(v.intValue()));
        this.count.limit(0, Integer.MAX_VALUE);
        this.size = new UITrackpad((v) -> this.form.size.set(v.floatValue()));
        this.size.limit(0, Float.MAX_VALUE);
    }

    @Override
    public void startEdit(GLVertexForm form) {
        super.startEdit(form);

        this.color.setColor(form.color.get().getRGBColor());
        this.count.setValue(form.count.get());
        this.size.setValue(form.size.get());

        this.options.add(UI.label(UIKeys.FORMS_EDITOR_VERTEX_COLOR), this.color);
        this.options.add(UI.label(UIKeys.FORMS_EDITOR_VERTEX_COUNT), this.count);
        this.options.add(UI.label(UIKeys.FORMS_EDITOR_VERTEX_SIZE), this.size);
    }
}
