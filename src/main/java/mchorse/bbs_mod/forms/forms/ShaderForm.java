package mchorse.bbs_mod.forms.forms;

import com.google.common.collect.ImmutableSet;
import mchorse.bbs_mod.settings.values.core.ValueString;
import mchorse.bbs_mod.settings.values.numeric.ValueInt;
import net.irisshaders.iris.gl.framebuffer.GlFramebuffer;
import net.irisshaders.iris.gl.program.Program;

import javax.annotation.Nullable;

public class ShaderForm extends Form {
    public static final int TYPE = 3;

    public static final int BEGIN_STAGE = 0;
    public static final int PREPARE_STAGE = 1;
    public static final int DEFERRED_STAGE = 2;
    public static final int COMPOSITE_STAGE = 3;
    public static final int FINAL_STAGE = 4;

    private Program shaderProgram = null;
    private ImmutableSet<Integer> flippedBuffers = ImmutableSet.of();
    boolean shaderDirty = true;
    private int[] drawBuffers = new int[]{};
    private GlFramebuffer framebuffer = null;

    public final ValueString name = new ValueString("name", "");
    public final ValueString vertex = new ValueString("vertex", "");
    public final ValueString fragment = new ValueString("fragment", "");
    public final ValueString geometry = new ValueString("geometry", "");
    public final ValueInt renderStage = new ValueInt("renderStage", 0);
    public final ValueInt priority = new ValueInt("priority", 1, 1, 32767);


    public ShaderForm() {
        super();

        this.add(this.name);
        this.add(this.vertex);
        this.add(this.fragment);
        this.add(this.geometry);
        this.add(this.priority);
        this.add(this.renderStage);
    }

    @Override
    protected String getDefaultDisplayName() {
        return "Shader " + (!this.name.get().isBlank() ? ": " + this.name.get() : "");
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

    public ImmutableSet<Integer> getFlippedBuffers() {
        if (this.flippedBuffers != null) return ImmutableSet.copyOf(this.flippedBuffers);
        else return ImmutableSet.of();
    }

    public void setFlippedBuffers(ImmutableSet<Integer> flippedBuffers) {
        this.flippedBuffers = flippedBuffers;
    }

    public void destroyProgram() {
        if (this.shaderProgram != null) {
            this.shaderProgram.destroy();
            this.shaderProgram = null;
        }
        this.flippedBuffers = ImmutableSet.of();
    }

    public void markDirty() {
        this.shaderDirty = true;
    }

    public boolean isDirty() {
        return this.shaderDirty;
    }

    /**
     * Binds the framebuffer if they exist
     * @return true if framebuffer was successfully bound, false if no framebuffer exists
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean bindFramebuffer() {
        if (this.framebuffer == null) {
            return false;
        }

        try {
            this.framebuffer.bind();
            return true;
        } catch (IllegalStateException e) { // Shader reloaded
            return false;
        }
    }

    // Get/setters for shader sources

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

    public int[] getDrawBuffers() {
        return this.drawBuffers.clone();
    }

    public void setDrawBuffers(int[] drawBuffers) {
        this.drawBuffers = drawBuffers != null ? drawBuffers.clone() : new int[]{};
    }

    public GlFramebuffer getFramebuffer() {
        return this.framebuffer;
    }

    public void setFramebuffer(@Nullable GlFramebuffer framebuffer) {
        this.framebuffer = framebuffer;
    }
}
