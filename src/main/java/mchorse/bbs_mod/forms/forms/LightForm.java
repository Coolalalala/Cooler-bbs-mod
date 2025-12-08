package mchorse.bbs_mod.forms.forms;

import mchorse.bbs_mod.settings.values.core.ValueColor;
import mchorse.bbs_mod.settings.values.numeric.ValueBoolean;
import mchorse.bbs_mod.settings.values.numeric.ValueFloat;
import mchorse.bbs_mod.settings.values.numeric.ValueInt;
import mchorse.bbs_mod.utils.colors.Color;

public class LightForm extends Form {
    public static final int LIGHT_TYPE_POINT = 0;
    public static final int LIGHT_TYPE_AREA = 1;

    public final ValueColor color = new ValueColor("color", Color.white());
    public final ValueFloat intensity = new ValueFloat("intensity", 100F);
    public final ValueInt type = new ValueInt("type", 0, 0, 1);
    public final ValueFloat angle = new ValueFloat("angle", 180F);
    public final ValueFloat spread = new ValueFloat("spread", 0F);
    public final ValueFloat attenuation = new ValueFloat("attenuation", 5F);
    public final ValueFloat radius = new ValueFloat("radius", 1F);
    public final ValueBoolean indirect = new ValueBoolean("indirect", false);

    public LightForm() {
        this.add(this.color);
        this.add(this.intensity);
        this.add(this.type);
        this.add(this.angle);
        this.add(this.spread);
        this.add(this.attenuation);
        this.add(this.radius);
        this.add(this.indirect);
    }

    @Override
    protected String getDefaultDisplayName()
    {
        String angle;
        float angleValue = this.angle.get();
        if (angleValue > 175F) {
            angle = "";
        } else if (angleValue > 90F) {
            angle = "Wide ";
        } else if (angleValue > 25F) {
            angle = "Medium ";
        } else {
            angle = "Narrow ";
        }

        String spread;
        float spreadValue = this.spread.get();
        if (spreadValue < 2F) {
            spread = "";
        } else if (spreadValue < 0.5F * angleValue) {
            spread = "Sharp ";
        } else if (spreadValue <= angleValue) {
            spread = "Soft ";
        } else {
            spread = "Ambient ";
        }
        String light = switch (this.type.get()) {
            case LIGHT_TYPE_POINT -> "Point Light";
            case LIGHT_TYPE_AREA -> "Area Light";
            default -> "Light";
        };
        return spread + angle + light + ": " + this.color.get().stringify(false);
    }
}
