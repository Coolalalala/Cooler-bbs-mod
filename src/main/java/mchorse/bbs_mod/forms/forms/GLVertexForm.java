package mchorse.bbs_mod.forms.forms;

import mchorse.bbs_mod.settings.values.core.ValueColor;
import mchorse.bbs_mod.settings.values.core.ValueLink;
import mchorse.bbs_mod.settings.values.numeric.ValueInt;
import mchorse.bbs_mod.utils.colors.Color;
import org.lwjgl.opengl.GL43;

public class GLVertexForm extends Form {
    public final ValueLink texture = new ValueLink("texture", null);
    public final ValueColor color = new ValueColor("color", Color.white());
    public final ValueInt count = new ValueInt("count", 1, 0, Integer.MAX_VALUE);
    public final ValueInt instances = new ValueInt("instances", 1, 0, Integer.MAX_VALUE);

    private boolean isDirty = true;
    public int vao = -1;
    public int vbo = -1;

    public GLVertexForm() {
        super();

        this.add(this.texture);
        this.add(this.color);
        this.add(this.count);
        this.add(this.instances);
    }

    @Override
    protected String getDefaultDisplayName() {
        if (count.get() == 1) return "GL Vertex";
        else return count.get().toString() + " GL Vertices";
    }

    public void markDirty() {
        this.isDirty = true;
    }

    private void genBuffer() {
        this.vao = GL43.glGenVertexArrays();
        GL43.glBindVertexArray(this.vao);

        this.vbo = GL43.glGenBuffers();
        GL43.glBindBuffer(GL43.GL_ARRAY_BUFFER, this.vbo);
        GL43.glBufferData(GL43.GL_ARRAY_BUFFER, new float[count.get()*8], GL43.GL_STATIC_DRAW);
    }

    public void bind() {
        if (this.vao == -1 || this.vbo == -1 || this.isDirty) {
            this.genBuffer();
            this.isDirty = false;
            return;
        }
        GL43.glBindVertexArray(this.vao);
        GL43.glBindBuffer(GL43.GL_ARRAY_BUFFER, this.vbo);
    }
}
