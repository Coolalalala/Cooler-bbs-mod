package mchorse.bbs_mod.ui.forms.editors.panels;

import mchorse.bbs_mod.forms.forms.LightForm;
import mchorse.bbs_mod.ui.UIKeys;
import mchorse.bbs_mod.ui.forms.editors.forms.UIForm;
import mchorse.bbs_mod.ui.framework.elements.buttons.UICirculate;
import mchorse.bbs_mod.ui.framework.elements.buttons.UIToggle;
import mchorse.bbs_mod.ui.framework.elements.input.UIColor;
import mchorse.bbs_mod.ui.framework.elements.input.UITrackpad;
import mchorse.bbs_mod.ui.utils.UI;
import mchorse.bbs_mod.utils.colors.Color;

public class UILightFormPanel extends UIFormPanel<LightForm> {
    public UIColor color;
    public UITrackpad intensity;
    public UICirculate type;
    public UITrackpad angle;
    public UITrackpad spread;
    public UITrackpad attenuation;
    public UITrackpad radius;
    public UIToggle indirect;
    public UILightFormPanel(UIForm<LightForm> editor)
    {
        super(editor);

        this.color = new UIColor((c) -> this.form.color.set(Color.rgb(c)));
        this.intensity = new UITrackpad((v) -> this.form.intensity.set(v.floatValue()));
        this.type = new UICirculate((b) -> this.form.type.set(b.getValue()));
        this.type.addLabel(UIKeys.FORMS_EDITORS_LIGHT_TYPE_POINT);
        this.type.addLabel(UIKeys.FORMS_EDITORS_LIGHT_TYPE_AREA);
        this.angle = new UITrackpad((v) -> this.form.angle.set(v.floatValue()));
        this.spread = new UITrackpad((v) -> this.form.spread.set(v.floatValue()));
        this.attenuation = new UITrackpad((v) -> this.form.attenuation.set(v.floatValue()));
        this.radius = new UITrackpad((v) -> this.form.radius.set(v.floatValue()));
        this.indirect = new UIToggle(UIKeys.FORMS_EDITORS_LIGHT_INDIRECT, (b) -> this.form.indirect.set(b.getValue()));


        this.options.add(UI.label(UIKeys.FORMS_EDITORS_LIGHT_COLOR), this.color,
                         UI.label(UIKeys.FORMS_EDITORS_LIGHT_INTENSITY), this.intensity,
                         UI.label(UIKeys.FORMS_EDITORS_LIGHT_TYPE), this.type,
                         UI.label(UIKeys.FORMS_EDITORS_LIGHT_ANGLE), this.angle,
                         UI.label(UIKeys.FORMS_EDITORS_LIGHT_SPREAD), this.spread,
                         UI.label(UIKeys.FORMS_EDITORS_LIGHT_ATTENUATION), this.attenuation,
                         UI.label(UIKeys.FORMS_EDITORS_LIGHT_RADIUS), this.radius,
                         this.indirect);
    }

    @Override
    public void startEdit(LightForm form) {
        super.startEdit(form);

        this.color.setColor(form.color.get().getRGBColor());
        this.intensity.setValue(form.intensity.get());
        this.type.setValue(form.type.get());
        this.angle.setValue(form.angle.get());
        this.spread.setValue(form.spread.get());
        this.attenuation.setValue(form.attenuation.get());
        this.radius.setValue(form.radius.get());
        this.indirect.setValue(form.indirect.get());
    }
}
