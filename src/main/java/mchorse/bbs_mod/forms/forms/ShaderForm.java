package mchorse.bbs_mod.forms.forms;

import com.google.common.collect.ImmutableSet;
import com.mojang.logging.LogUtils;
import mchorse.bbs_mod.settings.values.core.ValueString;
import mchorse.bbs_mod.settings.values.numeric.ValueBoolean;
import net.irisshaders.iris.gl.program.Program;
import net.irisshaders.iris.gl.program.ProgramBuilder;
import net.irisshaders.iris.gl.shader.ShaderCompileException;
import net.irisshaders.iris.gl.state.FogMode;
import net.irisshaders.iris.uniforms.CommonUniforms;

import javax.annotation.Nullable;

public class ShaderForm extends Form {
    private Program shaderProgram = null;
    private boolean shaderDirty = true;

    public final ValueString name = new ValueString("name", "");
    public final ValueString vertex = new ValueString("vertex", "");
    public final ValueString fragment = new ValueString("fragment", "");
    public final ValueString geometry = new ValueString("geometry", "");
    public final ValueBoolean sendTransforms = new ValueBoolean("sendTransforms", false);
    public final ValueBoolean sendParents = new ValueBoolean("sendParents", false);
    public final ValueBoolean sendChildren = new ValueBoolean("sendChildren", false);


    public ShaderForm() {
        super();

        this.add(this.name);
        this.add(this.vertex);
        this.add(this.fragment);
        this.add(this.geometry);
        this.add(this.sendTransforms);
        this.add(this.sendParents);
        this.add(this.sendChildren);
    }

    @Override
    protected String getDefaultDisplayName() {
        return "Shader";
    }

    @Nullable
    private String stringOrNull(ValueString value) {
        String string = value.toString();
        return string.isBlank() ? null : string;
    }

    public Program getProgram() {
        return this.shaderProgram;
    }

    public void setProgram(Program program) {
        this.shaderProgram = program;
        this.shaderDirty = false;
    }

    public void destroyProgram() {
        if (this.shaderProgram != null) {
            this.shaderProgram.destroy();
            this.shaderProgram = null;
        }
    }

    public void markDirty() {
        this.shaderDirty = true;
    }

    public boolean isDirty() {
        return this.shaderDirty;
    }

    // Getters for shader sources

    public String getName() {
        return this.name.toString();
    }

    public String getVertexSource() {
        return this.vertex.toString();
    }

    public String getFragmentSource() {
        return this.fragment.toString();
    }

    @Nullable
    public String getGeometrySource() {
        return stringOrNull(this.geometry);
    }
}
