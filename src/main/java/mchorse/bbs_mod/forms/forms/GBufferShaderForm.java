package mchorse.bbs_mod.forms.forms;


import mchorse.bbs_mod.settings.values.numeric.ValueBoolean;

public class GBufferShaderForm extends ShaderForm {
    public final ValueBoolean culling = new ValueBoolean("culling", true);
    public final ValueBoolean depthTest = new ValueBoolean("depthTest", true);
    public final ValueBoolean renderChildren = new ValueBoolean("renderChildren", false);
    public final ValueBoolean pingpong = new ValueBoolean("pingpong", false);

    public GBufferShaderForm() {
        super();

        this.add(this.culling);
        this.add(this.depthTest);
        this.add(this.renderChildren);
        this.add(this.pingpong);
    }

    @Override
    protected String getDefaultDisplayName() {
        if (this.name.get().isBlank()) return "GBuffer Shader Program";
        else return "GBuffer: " + this.name.get();
    }

    @Override
    public int[] getDrawBuffers() {
        if (pingpong.get()) return super.getDrawBuffers();
        else return new int[]{};
    }

    public int[] getDrawBuffersForReal() {
        return super.getDrawBuffers();
    }
}
