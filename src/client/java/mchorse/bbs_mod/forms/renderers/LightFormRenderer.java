package mchorse.bbs_mod.forms.renderers;

import coolaa.util.iris.SSBOUtils;
import mchorse.bbs_mod.forms.forms.LightForm;
import mchorse.bbs_mod.graphics.texture.Texture;
import mchorse.bbs_mod.resources.Link;
import mchorse.bbs_mod.ui.framework.UIContext;
import net.irisshaders.iris.gl.buffer.ShaderStorageBuffer;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL45C;

import java.nio.FloatBuffer;

public class LightFormRenderer extends FormRenderer<LightForm> {
    public static final Link LIGHT_PREVIEW = Link.assets("textures/light.png");

    private static final boolean enabled = isIrisInstalled();
    public static ShaderStorageBuffer lightsSSBO = null;

    private static boolean isIrisInstalled() {
        try {
            Class.forName("net.irisshaders.iris.Iris");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public LightFormRenderer(LightForm form) {
        super(form);
        if (!enabled) return;
        initializeBuffer();
    }

    private void initializeBuffer() {
        try {
            lightsSSBO = SSBOUtils.setupRelativeBuffer(8, 1, 3, 2); // max index 96
        } catch (Throwable e) {
            // Handle case where buffer creation fails
            lightsSSBO = null;
        }
    }

    @Override
    protected void renderInUI(UIContext context, int x1, int y1, int x2, int y2) {
        Texture texture = context.render.getTextures().getTexture(LIGHT_PREVIEW);

        int w = texture.width;
        int h = texture.height;
        int x = (x1 + x2) / 2;
        int y = (y1 + y2) / 2;

        context.batcher.fullTexturedBox(texture, x - w / 2f, y - h / 2f, w, h);
    }

    @Override
    protected void render3D(FormRenderingContext context) {
        if (!enabled) return;
        if (lightsSSBO == null) initializeBuffer();
        if (lightsSSBO == null) return;

        // get data
        LightForm form = this.form;
        int color = form.color.get().getRGBColor();
        Vector3f pos = context.stack.peek().getPositionMatrix().getTranslation(new Vector3f());
        Vector3f dir = context.stack.peek().getNormalMatrix().getRotation(new AxisAngle4f()).transform(new Vector3f(0,0,-1));


        // init buffer
        lightsSSBO.bind();
        FloatBuffer buffer = org.lwjgl.BufferUtils.createFloatBuffer(13);
        // pack data
        buffer.put(new float[]{pos.x, pos.y, pos.z, color, dir.x, dir.y, dir.z, form.type.get(),
                               form.intensity.get(), form.angle.get(), form.spread.get(), form.radius.get(),
                               form.attenuation.get()
        });
        buffer.flip();

        GL45C.glBufferData(GL45C.GL_SHADER_STORAGE_BUFFER, buffer, GL45C.GL_STATIC_DRAW);
    }
}