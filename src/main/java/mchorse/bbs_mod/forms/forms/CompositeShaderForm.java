package mchorse.bbs_mod.forms.forms;


import mchorse.bbs_mod.settings.values.numeric.ValueBoolean;

public class CompositeShaderForm extends ShaderForm {
    public CompositeShaderForm() {
        super();

        this.pingpong.set(true);
        this.add(this.pingpong);
    }

    @Override
    public int[] getDrawBuffers() {
        if (pingpong.get()) return super.getDrawBuffers();
        else return new int[]{};
    }

    public int[] getDrawBuffersForReal() {
        return super.getDrawBuffers();
    }

    @Override
    protected String getDefaultDisplayName() {
        if (this.name.get().isBlank()) return "Composite Shader Program";
        else return "Composite: " + this.name.get();
    }

}