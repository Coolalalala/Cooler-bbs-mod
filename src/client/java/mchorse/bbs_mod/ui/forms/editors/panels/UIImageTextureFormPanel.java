package mchorse.bbs_mod.ui.forms.editors.panels;

import mchorse.bbs_mod.forms.forms.ImageTextureForm;
import mchorse.bbs_mod.ui.UIKeys;
import mchorse.bbs_mod.ui.forms.editors.forms.UIForm;
import mchorse.bbs_mod.ui.framework.elements.buttons.UICirculate;
import mchorse.bbs_mod.ui.framework.elements.buttons.UIToggle;
import mchorse.bbs_mod.ui.framework.elements.input.UITrackpad;
import mchorse.bbs_mod.ui.framework.elements.input.text.UITextbox;
import mchorse.bbs_mod.ui.utils.UI;

public class UIImageTextureFormPanel extends UIFormPanel<ImageTextureForm> {
    public UITextbox name;
    public UITextbox format;
    public UITextbox pixelType;
    public UIToggle dynamic;
    public UITrackpad scaleX;
    public UITrackpad scaleY;
    public UICirculate type;
    public UITrackpad width;
    public UITrackpad height;
    public UITrackpad depth;
    public UIToggle clear;

    public UIImageTextureFormPanel(UIForm<ImageTextureForm> editor) {
        super(editor);

        this.name = new UITextbox(10000, (t) -> this.form.name.set(t));
        this.format = new UITextbox(10000, (t) -> this.form.format.set(t));
        this.pixelType = new UITextbox(10000, (t) -> this.form.pixelType.set(t));
        this.dynamic = new UIToggle(UIKeys.FORMS_EDITOR_IMAGETEXTURE_DYNAMIC, (b) -> this.form.dynamic.set(b.getValue()));
        this.scaleX = new UITrackpad((value) -> this.form.scaleX.set(value.floatValue()));
        this.scaleY = new UITrackpad((value) -> this.form.scaleY.set(value.floatValue()));
        this.type = new UICirculate((b) -> this.form.type.set(b.getValue()));
        this.type.addLabel(UIKeys.FORMS_EDITOR_IMAGETEXTURE_TYPE_1D);
        this.type.addLabel(UIKeys.FORMS_EDITOR_IMAGETEXTURE_TYPE_2D);
        this.type.addLabel(UIKeys.FORMS_EDITOR_IMAGETEXTURE_TYPE_3D);
        this.width = new UITrackpad((value) -> this.form.width.set(value.intValue()));
        this.width.limit(1, Integer.MAX_VALUE, true).increment(1);
        this.height = new UITrackpad((value) -> this.form.height.set(value.intValue()));
        this.height.limit(1, Integer.MAX_VALUE, true).increment(1);
        this.depth = new UITrackpad((value) -> this.form.depth.set(value.intValue()));
        this.depth.limit(1, Integer.MAX_VALUE, true).increment(1);
        this.clear = new UIToggle(UIKeys.FORMS_EDITOR_IMAGETEXTURE_CLEAR, (b) -> this.form.clear.set(b.getValue()));
        this.clear.tooltip(UIKeys.FORMS_EDITOR_IMAGETEXTURE_CLEAR_TOOLTIP);

        this.options.add(UI.label(UIKeys.FORMS_EDITOR_IMAGETEXTURE_NAME), this.name);
        this.options.add(UI.label(UIKeys.FORMS_EDITOR_IMAGETEXTURE_FORMAT), this.format);
        this.options.add(UI.label(UIKeys.FORMS_EDITOR_IMAGETEXTURE_PIXELTYPE), this.pixelType);
        this.options.add(this.dynamic);
        this.options.add(UI.label(UIKeys.FORMS_EDITOR_IMAGETEXTURE_SCALE), this.scaleX, this.scaleY);
        this.options.add(UI.label(UIKeys.FORMS_EDITOR_IMAGETEXTURE_TYPE), this.type);
        this.options.add(UI.label(UIKeys.FORMS_EDITOR_IMAGETEXTURE_SIZE), this.width, this.height, this.depth);
        this.options.add(this.clear);
    }

    @Override
    public void startEdit(ImageTextureForm form) {
        super.startEdit(form);

        this.name.setText(form.name.toString());
        this.format.setText(form.format.toString());
        this.pixelType.setText(form.pixelType.toString());
        this.dynamic.setValue(form.dynamic.get());
        this.scaleX.setValue(form.scaleX.get());
        this.scaleY.setValue(form.scaleY.get());
        this.type.setValue(form.type.get());
        this.width.setValue(form.width.get());
        this.height.setValue(form.height.get());
        this.depth.setValue(form.depth.get());
        this.clear.setValue(form.clear.get());
    }
}
