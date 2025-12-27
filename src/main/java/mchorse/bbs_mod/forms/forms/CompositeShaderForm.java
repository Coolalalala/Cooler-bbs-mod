package mchorse.bbs_mod.forms.forms;


public class CompositeShaderForm extends ShaderForm {
    public CompositeShaderForm() {
        super();
    }

    @Override
    protected String getDefaultDisplayName() {
        if (!this.name.get().isBlank()) return "Composite Shader Program";
        else return "Composite: " + this.name.get();
    }

}