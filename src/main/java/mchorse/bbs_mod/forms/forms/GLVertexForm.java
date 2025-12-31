package mchorse.bbs_mod.forms.forms;

import mchorse.bbs_mod.settings.values.core.ValueColor;
import mchorse.bbs_mod.settings.values.numeric.ValueFloat;
import mchorse.bbs_mod.settings.values.numeric.ValueInt;
import mchorse.bbs_mod.utils.colors.Color;

public class GLVertexForm extends Form {
    public final ValueColor color = new ValueColor("color", Color.white());
    public final ValueInt count = new ValueInt("count", 1, 0, Integer.MAX_VALUE);
    public final ValueFloat size = new ValueFloat("size", 1F, 0F, Float.MAX_VALUE);

    public GLVertexForm() {
        super();

        this.add(this.color);
        this.add(this.count);
    }

    @Override
    protected String getDefaultDisplayName() {
        if (count.get() == 1) return "GL Vertex";
        else return count.get().toString() + " GL Vertices";
    }
}
