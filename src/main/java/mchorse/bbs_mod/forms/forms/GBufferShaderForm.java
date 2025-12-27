package mchorse.bbs_mod.forms.forms;


import mchorse.bbs_mod.settings.values.numeric.ValueBoolean;

public class GBufferShaderForm extends ShaderForm {
    public final ValueBoolean renderChildren = new ValueBoolean("renderChildren", false);

    public GBufferShaderForm() {
        super();
    }

    @Override
    protected String getDefaultDisplayName() {
        if (!this.name.get().isBlank()) return "GBuffer Shader Program";
        else return "GBuffer: " + this.name.get();
    }
}
