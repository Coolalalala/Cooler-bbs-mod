package mchorse.bbs_mod.forms.forms;

import mchorse.bbs_mod.settings.values.core.ValueColor;
import mchorse.bbs_mod.settings.values.numeric.ValueFloat;
import mchorse.bbs_mod.settings.values.numeric.ValueInt;
import mchorse.bbs_mod.utils.colors.Color;

public class LightForm extends Form {
    public static final int LIGHT_TYPE_POINT = 0;
    public static final int LIGHT_TYPE_AREA = 1;

    public final ValueColor color = new ValueColor("color", Color.white());
    public final ValueFloat intensity = new ValueFloat("intensity", 1F);
    public final ValueInt type = new ValueInt("type", 0, 0, 1);
    public final ValueFloat angle = new ValueFloat("angle", 180F);
    public final ValueFloat spread = new ValueFloat("spread", 0F);
    public final ValueFloat attenuation = new ValueFloat("attenuation", 20F);
    public final ValueFloat radius = new ValueFloat("radius", 1F);

    public LightForm() {
        this.add(this.color);
        this.add(this.intensity);
        this.add(this.type);
        this.add(this.angle);
        this.add(this.spread);
        this.add(this.attenuation);
        this.add(this.radius);
    }

    @Override
    protected String getDefaultDisplayName()
    {
        String light = switch (this.type.get()) {
            case LIGHT_TYPE_POINT -> "Point Light";
            case LIGHT_TYPE_AREA -> "Area Light";
            default -> "Unknown Light";
        };
        return light + ": " + this.color.get().stringify(false);
    }
}
