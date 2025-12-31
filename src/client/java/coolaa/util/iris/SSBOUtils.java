package coolaa.util.iris;

import net.irisshaders.iris.gl.buffer.ShaderStorageBuffer;
import net.irisshaders.iris.gl.buffer.ShaderStorageInfo;
import net.irisshaders.iris.gl.sampler.SamplerLimits;
import mchorse.bbs_mod.utils.iris.IrisUtils;

public class SSBOUtils {
    public static ShaderStorageBuffer setupRelativeBuffer(int index, int size, float scaleX, float scaleY) {
        if (!IrisUtils.isShaderPackEnabled()) return null;

        ShaderStorageInfo info = generateInfo(size, true, scaleX, scaleY);
        return setupBuffer(index, info);
    }

    public static ShaderStorageBuffer setupBuffer(int index, int size) {
        if (!IrisUtils.isShaderPackEnabled()) return null;

        ShaderStorageInfo info = generateInfo(size, false, 1, 1);
        return setupBuffer(index, info);
    }

    private static ShaderStorageBuffer setupBuffer(int index, ShaderStorageInfo info) {
        try {
            if (index > getMaximumSSBOUnits()) {
                throw new IllegalArgumentException("Shader storage buffer index " + index + " is too large. (max: " + getMaximumSSBOUnits() + ")");
            } else if (index < 0) {
                throw new IllegalArgumentException("Shader storage buffer index " + index + " is negative.");
            }

            ShaderStorageBuffer buffer = new ShaderStorageBuffer(index, info);
            buffer.bind();
            return buffer;
        } catch (NoClassDefFoundError | Exception e) {
            // Iris is not available or not properly loaded
            return null;
        }
    }

    private static int getMaximumSSBOUnits() {
        try {
            return SamplerLimits.get().getMaxShaderStorageUnits();
        } catch (NoClassDefFoundError | Exception e) {
            return -1;
        }
    }

    private static ShaderStorageInfo generateInfo(int size, boolean screenSized, float scaleX, float scaleY) {
        return new ShaderStorageInfo(size, screenSized, scaleX, scaleY);
    }
}
