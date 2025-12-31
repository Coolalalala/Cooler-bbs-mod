package mchorse.bbs_mod.forms.forms;


import mchorse.bbs_mod.settings.values.core.ValueString;
import mchorse.bbs_mod.settings.values.numeric.ValueBoolean;
import mchorse.bbs_mod.settings.values.numeric.ValueFloat;
import mchorse.bbs_mod.settings.values.numeric.ValueInt;
import net.irisshaders.iris.gl.buffer.ShaderStorageBuffer;

public class SSBOForm extends Form {
    public final static int CLEAR_NONE = 0;
    public final static int CLEAR_ON_FRAME = 1;
    public final static int CLEAR_ON_DISPATCH = 2;

    public final ValueString name = new ValueString("name", "");
    public final ValueInt binding = new ValueInt("binding", 0, 0, Integer.MAX_VALUE);
    public final ValueBoolean dynamic = new ValueBoolean("dynamic", false);
    public final ValueFloat scaleX = new ValueFloat("scaleX", 1F);
    public final ValueFloat scaleY = new ValueFloat("scaleY", 1F);
    public final ValueInt size = new ValueInt("size", 0, 1, Integer.MAX_VALUE);
    public final ValueInt capacity = new ValueInt("capacity", 0, 1, Integer.MAX_VALUE);
    public final ValueInt clearType = new ValueInt("clearType", 0, 0, 2);
    public final ValueFloat clearValue = new ValueFloat("clearValue", 0F);

    public ShaderStorageBuffer ssbo = null;

    public SSBOForm() {
        super();

        this.add(this.name);
        this.add(this.binding);
        this.add(this.dynamic);
        this.add(this.scaleX);
        this.add(this.scaleY);
        this.add(this.size);
        this.add(this.capacity);
        this.add(this.clearType);
        this.add(this.clearValue);
    }

    @Override
    protected String getDefaultDisplayName() {
        if (this.name.get().isBlank()) return "Shader Storage Buffer Object";
        return "SSBO: " + this.name.get();
    }
}
