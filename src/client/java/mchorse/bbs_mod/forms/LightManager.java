package mchorse.bbs_mod.forms;

import coolaa.util.iris.SSBOUtils;
import mchorse.bbs_mod.client.BBSRendering;
import net.irisshaders.iris.gl.buffer.ShaderStorageBuffer;
import org.lwjgl.opengl.GL45C;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static mchorse.bbs_mod.client.BBSRendering.isIrisShadersEnabled;

public class LightManager {
    private static final LightManager instance = new LightManager();
    public static ShaderStorageBuffer lightsSSBO = null;
    private final List<Float> lightAttribs = new ArrayList<>();


    public LightManager() {
        if (!isIrisShadersEnabled()) return;
        initializeBuffer();
    }

    private void initializeBuffer() {
        try {
            lightsSSBO = SSBOUtils.setupRelativeBuffer(8, 13, 1, 1);
        } catch (Throwable e) {
            // Handle case where buffer creation fails
            lightsSSBO = null;
        }
    }

    public static LightManager get() {
        return instance;
    }

    public void add(List<Float> attribs) {
        this.lightAttribs.addAll(attribs);
    }

    public void clear() {
        if (lightAttribs.isEmpty()) pushBuffer();
        else this.lightAttribs.clear();
    }

    public void pushBuffer() {
        {
            if (!isIrisShadersEnabled()) return;
            if (lightsSSBO == null) initializeBuffer();
            if (lightsSSBO == null) return;

            // init buffer
            lightsSSBO.bind();
            FloatBuffer buffer = org.lwjgl.BufferUtils.createFloatBuffer(1024 + this.lightAttribs.size() + 4);

            // pack data
            buffer.put(new float[]{(float) this.lightAttribs.size() /16, 0f, 0f, 0f});
            buffer.put(new float[1024]);
            for (float val : this.lightAttribs) {
                buffer.put(val);
            }
            buffer.flip();

            // Push buffer
            GL45C.glBufferData(GL45C.GL_SHADER_STORAGE_BUFFER, buffer, GL45C.GL_STATIC_DRAW);
        }
    }
}
