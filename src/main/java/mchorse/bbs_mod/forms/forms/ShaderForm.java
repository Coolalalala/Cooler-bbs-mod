package mchorse.bbs_mod.forms.forms;

import com.google.common.collect.ImmutableSet;
import mchorse.bbs_mod.settings.values.core.ValueString;
import mchorse.bbs_mod.settings.values.numeric.ValueBoolean;
import mchorse.bbs_mod.settings.values.numeric.ValueInt;
import net.irisshaders.iris.gl.program.Program;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class ShaderForm extends Form {
    public static final int BEGIN_STAGE = 0;
    public static final int PREPARE_STAGE = 1;
    public static final int DEFERRED_STAGE = 2;
    public static final int COMPOSITE_STAGE = 3;
    public static final int FINAL_STAGE = 4;

    private Program shaderProgram = null;
    private Supplier<ImmutableSet<Integer>> buffers = null;
    private boolean shaderDirty = true;

    public final ValueString name = new ValueString("name", "");
    public final ValueString vertex = new ValueString("vertex", "");
    public final ValueString fragment = new ValueString("fragment", "");
    public final ValueString geometry = new ValueString("geometry", "");
    public final ValueBoolean sendTransforms = new ValueBoolean("sendTransforms", false);
    public final ValueBoolean sendParents = new ValueBoolean("sendParents", false);
    public final ValueBoolean sendChildren = new ValueBoolean("sendChildren", false);
    public final ValueInt renderStage = new ValueInt("renderStage", 0);
    public final ValueInt priority = new ValueInt("priority", 0, 0, 32767);


    public ShaderForm() {
        super();

        this.add(this.name);
        this.add(this.vertex);
        this.add(this.fragment);
        this.add(this.geometry);
        this.add(this.sendTransforms);
        this.add(this.sendParents);
        this.add(this.sendChildren);
        this.add(this.priority);
        this.add(this.renderStage);
    }

    @Override
    protected String getDefaultDisplayName() {
        return "Shader " + this.name;
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

    public Supplier<ImmutableSet<Integer>> getBuffers() {
        return this.buffers;
    }

    public void setBuffers(Supplier<ImmutableSet<Integer>> buffers) {
        this.buffers = buffers;
    }

    public void destroyProgram() {
        if (this.shaderProgram != null) {
            this.shaderProgram.destroy();
            this.shaderProgram = null;
        }
        this.buffers = null;
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
