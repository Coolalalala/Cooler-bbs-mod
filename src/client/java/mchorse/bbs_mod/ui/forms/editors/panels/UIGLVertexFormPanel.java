package mchorse.bbs_mod.ui.forms.editors.panels;

import mchorse.bbs_mod.forms.forms.GLVertexForm;
import mchorse.bbs_mod.resources.Link;
import mchorse.bbs_mod.ui.UIKeys;
import mchorse.bbs_mod.ui.forms.editors.forms.UIForm;
import mchorse.bbs_mod.ui.framework.elements.buttons.UIButton;
import mchorse.bbs_mod.ui.framework.elements.input.UIColor;
import mchorse.bbs_mod.ui.framework.elements.input.UITexturePicker;
import mchorse.bbs_mod.ui.framework.elements.input.UITrackpad;
import mchorse.bbs_mod.ui.utils.UI;
import mchorse.bbs_mod.utils.colors.Color;

public class UIGLVertexFormPanel extends UIFormPanel<GLVertexForm> {
    public UIButton texture;
    public UIColor color;
    public UITrackpad count;
    public UITrackpad instances;

    public UIGLVertexFormPanel(UIForm<GLVertexForm> editor) {
        super(editor);

        this.color = new UIColor((c) -> this.form.color.set(Color.rgba(c))).withAlpha();
        this.count = new UITrackpad((v) -> this.setCount(v.intValue()));
        this.count.limit(0, Integer.MAX_VALUE, true);
        this.instances = new UITrackpad((v) -> this.setInstances(v.intValue()));
        this.instances.limit(0, Integer.MAX_VALUE, true);
    }

    @Override
    public void startEdit(GLVertexForm form) {
        super.startEdit(form);

        this.texture = new UIButton(UIKeys.FORMS_EDITOR_MODEL_PICK_TEXTURE, (b) ->
        {
            Link link = this.form.texture.get();
            UITexturePicker.open(this.getContext(), link, (l) -> this.form.texture.set(l));
        });
        this.color.withAlpha().setColor(form.color.get().getARGBColor());
        this.count.setValue(form.count.get());
        this.instances.setValue(form.instances.get());

        this.options.add(this.texture);
        this.options.add(UI.label(UIKeys.FORMS_EDITOR_VERTEX_COLOR), this.color);
        this.options.add(UI.label(UIKeys.FORMS_EDITOR_VERTEX_COUNT), this.count);
        this.options.add(UI.label(UIKeys.FORMS_EDITOR_VERTEX_INSTANCES), this.instances);
    }

    private void setCount(int count) {
        this.form.count.set(count);
        this.form.markDirty();
    }

    private void setInstances(int instances) {
        this.form.instances.set(instances);
        this.form.markDirty();
    }
}
