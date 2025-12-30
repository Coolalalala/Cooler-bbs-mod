package mchorse.bbs_mod.forms.forms;

import com.google.common.collect.ImmutableSet;
import mchorse.bbs_mod.settings.values.numeric.ValueInt;

public class BufferFlipperForm extends CompositeShaderForm {
    public final ValueInt priority = new ValueInt("priority", 0, 0, 0);
    private ImmutableSet<Integer> buffers = ImmutableSet.of();

    public BufferFlipperForm(int stage, String name) {
        super();
        this.renderStage.set(stage);
        this.name.set(name);
    }

    /**
     * Set the buffers to be flipped
     * The designated buffers will have its current texture copied to the alternate texture when rendered
     * @param buffers to be flipped
     */
    public void setBuffers(ImmutableSet<Integer> buffers) {
        this.buffers = buffers;
        this.setVertexSource();
        this.setFragmentSource();
        this.setDrawBuffers(buffers.stream().mapToInt(Integer::intValue).toArray());
    }

    public void set(ImmutableSet<Integer> buffers) {
        if (this.buffers.equals(buffers)) return;
        this.setBuffers(buffers);
        this.markDirty();
    }

    public ImmutableSet<Integer> get() {
        return this.buffers;
    }

    private void setVertexSource() {
        String source = """
                        #version 330 compatibility
                        out vec2 texcoord;
                        void main() {
                          gl_Position = ftransform();
                          texcoord = (gl_TextureMatrix[0] * gl_MultiTexCoord0).xy;
                        }""";

        this.vertex.set(source);
    }

    private void setFragmentSource() {
        StringBuilder source = new StringBuilder("""
                                                 #version 330 compatibility
                                                 in vec2 texcoord;
                                                 """);
        for (int i : buffers) {
            source.append("uniform sampler2D colortex").append(i).append(";\n");
            source.append("layout(location = ").append(i).append(") ")
                    .append("out vec4 FragColor").append(i).append(";\n");
        }
        source.append("void main() {\n");
        for (int i : buffers) {
            source.append("  FragColor").append(i).append(" = texture(colortex").append(i).append(", texcoord);\n");
        }
        source.append("}");

        this.fragment.set(source.toString());
    }
}
