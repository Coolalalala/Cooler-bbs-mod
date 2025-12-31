package mchorse.bbs_mod.forms.renderers;

import coolaa.util.iris.SSBOUtils;
import mchorse.bbs_mod.forms.forms.CompositeShaderForm;
import mchorse.bbs_mod.forms.forms.SSBOForm;
import mchorse.bbs_mod.graphics.texture.Texture;
import mchorse.bbs_mod.resources.Link;
import mchorse.bbs_mod.ui.framework.UIContext;
import org.lwjgl.opengl.GL45C;

import java.nio.FloatBuffer;
import java.util.Arrays;

public class SSBOFormRenderer extends FormRenderer<SSBOForm> {
    public static final Link ICON_TEXTURE = Link.assets("textures/ssbo.png");

    public SSBOFormRenderer(SSBOForm form) {
        super(form);
    }

    @Override
    protected void renderInUI(UIContext context, int x1, int y1, int x2, int y2) {
        Texture texture = context.render.getTextures().getTexture(ICON_TEXTURE);

        int w = texture.width;
        int h = texture.height;
        int x = (x1 + x2) / 2;
        int y = (y1 + y2) / 2;

        context.batcher.fullTexturedBox(texture, x - w / 2f, y - h / 2f, w, h);
    }

    @Override
    protected void render3D(FormRenderingContext context) {
        SSBOForm form = this.form;
        if (form.ssbo == null) {
            initializeBuffer();
            return;
        }

        form.ssbo.bind();
        if (form.clearType.get() == SSBOForm.CLEAR_NONE) return;
        else if (form.clearType.get() == SSBOForm.CLEAR_ON_FRAME) clearBuffer();
        else if (form.clearType.get() == SSBOForm.CLEAR_ON_DISPATCH) {
            // register to shader manager
        }
    }

    public void clearBuffer() {
        int capacity = form.size.get()*form.capacity.get();
        FloatBuffer buffer = org.lwjgl.BufferUtils.createFloatBuffer(capacity);
        float[] clearArray = new float[capacity];
        Arrays.fill(clearArray, form.clearValue.get());
        buffer.put(clearArray);
        buffer.flip();
        // Push buffer
        GL45C.glBufferData(GL45C.GL_SHADER_STORAGE_BUFFER, buffer, GL45C.GL_DYNAMIC_DRAW);
    }

    public void initializeBuffer() {
        if (form.dynamic.get()) {
            form.ssbo = SSBOUtils.setupRelativeBuffer(form.binding.get(), form.size.get(), form.scaleX.get(), form.scaleY.get());
        } else {
            form.ssbo = SSBOUtils.setupBuffer(form.binding.get(), form.size.get());
        }

        // Initialize buffer data
        clearBuffer();
    }
}
