package mchorse.bbs_mod.forms.forms;


import mchorse.bbs_mod.settings.values.core.ValueString;
import mchorse.bbs_mod.settings.values.numeric.ValueBoolean;
import mchorse.bbs_mod.settings.values.numeric.ValueFloat;
import mchorse.bbs_mod.settings.values.numeric.ValueInt;
import net.irisshaders.iris.gl.buffer.ShaderStorageBuffer;
import net.irisshaders.iris.gl.program.ComputeProgram;
import net.irisshaders.iris.gl.program.Program;

import java.util.ArrayList;
import java.util.List;

public class ComputeShaderForm extends ShaderForm {
    public static final int TYPE = 0;

    public final ValueBoolean relative = new ValueBoolean("relative", false);
    public final ValueFloat relativeX = new ValueFloat("relativeX", 1F);
    public final ValueFloat relativeY = new ValueFloat("relativeY", 1F);
    public final ValueInt workGroupsX = new ValueInt("workGroupsX", 1, 1, 1024);
    public final ValueInt workGroupsY = new ValueInt("workGroupsY", 1, 1, 1024);
    public final ValueInt workGroupsZ = new ValueInt("workGroupsZ", 1, 1, 1024);
    public final ValueString computeSource = new ValueString("compute", "");

    private ComputeProgram shaderProgram = null;
    private List<ShaderStorageBuffer> ssbos = new ArrayList<>();


    public ComputeShaderForm() {
        super();

        this.add(this.relative);
        this.add(this.relativeX);
        this.add(this.relativeY);
        this.add(this.workGroupsX);
        this.add(this.workGroupsY);
        this.add(this.workGroupsZ);
        this.add(this.computeSource);
    }

    @Override
    protected String getDefaultDisplayName() {
        if (this.name.get().isBlank()) return "Compute Shader Program";
        else return "Compute: " + this.name.get();
    }

    public String getComputeSource() {
        return this.computeSource.toString();
    }

    public ComputeProgram getComputeProgram() {
        return this.shaderProgram;
    }

    public void setComputeProgram(ComputeProgram program) {
        this.shaderProgram = program;
        this.shaderDirty = false;
    }
}