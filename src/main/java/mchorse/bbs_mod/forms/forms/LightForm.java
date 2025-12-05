package mchorse.bbs_mod.forms.forms;

import mchorse.bbs_mod.settings.values.core.ValueColor;
import mchorse.bbs_mod.settings.values.numeric.ValueInt;
import mchorse.bbs_mod.utils.colors.Color;

public class LightForm extends Form {
    public static final int LIGHT_TYPE_POINT = 0;
    public static final int LIGHT_TYPE_AREA = 1;
    public static final int LIGHT_TYPE_SPOT = 2;

    public final ValueColor color = new ValueColor("color", Color.white());
    public final ValueInt type = new ValueInt("type", 0, 0, 2);

    public LightForm() {
        this.add(this.color);
    }

    @Override
    protected String getDefaultDisplayName()
    {
        return switch (this.type.get()) {
            case LIGHT_TYPE_POINT -> "Point Light";
            case LIGHT_TYPE_AREA -> "Area Light";
            case LIGHT_TYPE_SPOT -> "Spot Light";
            default -> "Unknown Light";
        };
    }
}
