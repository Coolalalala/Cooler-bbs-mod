package mchorse.bbs_mod.forms.forms;

import mchorse.bbs_mod.settings.values.core.ValueString;
import mchorse.bbs_mod.settings.values.numeric.ValueBoolean;

public class ShaderForm extends Form {
    public final ValueString vertex = new ValueString("vertex", "");
    public final ValueString fragment = new ValueString("fragment", "");
    public final ValueString geometry = new ValueString("geometry", "");
    public final ValueString tessellationControl = new ValueString("tessellationControl", "");
    public final ValueString tessellationEvaluation = new ValueString("tessellationEvaluation", "");
    public final ValueString compute = new ValueString("compute", "");
    public final ValueBoolean sendTranslate = new ValueBoolean("sendTranslate", false);
    public final ValueBoolean sendRotate = new ValueBoolean("sendRotate", false);
    public final ValueBoolean sendScale = new ValueBoolean("sendScale", false);
    public final ValueBoolean sendParents = new ValueBoolean("sendParents", false);
    public final ValueBoolean sendChildren = new ValueBoolean("sendChildren", false);


    public ShaderForm() {
        super();

        this.add(this.vertex);
        this.add(this.fragment);
        this.add(this.geometry);
        this.add(this.tessellationControl);
        this.add(this.tessellationEvaluation);
        this.add(this.compute);
        this.add(this.sendTranslate);
        this.add(this.sendRotate);
        this.add(this.sendScale);
        this.add(this.sendParents);
        this.add(this.sendChildren);
    }

    @Override
    protected String getDefaultDisplayName() {
        return "Shader";
    }
}
