package mchorse.bbs_mod.forms.forms;


public class CompositeShaderForm extends ShaderForm {
    public CompositeShaderForm() {
        super();
    }

    @Override
    protected String getDefaultDisplayName() {
        return "Composite Shader Program" + (!this.name.get().isBlank() ? ": " + this.name.get() : "");
    }

}