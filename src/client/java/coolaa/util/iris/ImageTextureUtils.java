package coolaa.util.iris;


import mchorse.bbs_mod.forms.ShaderManager;
import net.irisshaders.iris.gl.image.GlImage;
import net.irisshaders.iris.gl.texture.InternalTextureFormat;
import net.irisshaders.iris.gl.texture.PixelFormat;
import net.irisshaders.iris.gl.texture.PixelType;
import net.irisshaders.iris.gl.texture.TextureType;

import static mchorse.bbs_mod.forms.ShaderManager.customImages;

public class ImageTextureUtils {
    public static GlImage createImage(String name, TextureType textureType,
                                      PixelFormat format, InternalTextureFormat internalFormat, PixelType pixelType,
                                      boolean clear, int width, int height, int depth) {
        GlImage customImage = new GlImage(
                name,                       // name for shader access
                name,                       // name for sampler
                textureType,                // dimensions
                format,                     // color format
                internalFormat,             // color bit depth
                pixelType,                  // pixel datatype
                clear,                      // clear
                width, height, depth        // size
        );

        customImages.add(customImage);
        return customImage;
    }

    public static GlImage createRelativeImage(String name,
                                              PixelFormat format, InternalTextureFormat internalFormat, PixelType pixelType,
                                              boolean clear, float relativeWidth, float relativeHeight) {

        // Get current screen dimensions from render targets
        int currentWidth = ShaderManager.renderTargets.getCurrentWidth();
        int currentHeight = ShaderManager.renderTargets.getCurrentHeight();

        GlImage.Relative customImage = new GlImage.Relative(
                name,                       // name for shader access
                name,                       // name for sampler
                format,                     // color format
                internalFormat,             // color bit depth
                pixelType,                  // pixel datatype
                clear,                      // clear
                relativeWidth,              // relative width (0.0-1.0)
                relativeHeight,             // relative height (0.0-1.0)
                currentWidth,               // current screen width
                currentHeight               // current screen height
        );

        ShaderManager.customImages.add(customImage);
        return customImage;
    }

}
