package mchorse.bbs_mod.forms.renderers;

import coolaa.util.iris.ImageTextureUtils;
import coolaa.util.iris.SSBOUtils;
import mchorse.bbs_mod.forms.forms.ImageTextureForm;
import mchorse.bbs_mod.graphics.texture.Texture;
import mchorse.bbs_mod.resources.Link;
import mchorse.bbs_mod.ui.framework.UIContext;
import net.irisshaders.iris.gl.texture.InternalTextureFormat;
import net.irisshaders.iris.gl.texture.PixelType;
import net.irisshaders.iris.gl.texture.TextureType;
import org.lwjgl.opengl.GL45C;

import java.nio.FloatBuffer;
import java.util.Arrays;

public class ImageTextureFormRenderer extends FormRenderer<ImageTextureForm> {
    public static final Link ICON_TEXTURE = Link.assets("textures/imagetexture.png");

    public ImageTextureFormRenderer(ImageTextureForm form) {
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
        if (form.image == null || form.isDirty()) {
            initializeImage();
        }
    }

    public void initializeImage() {

        InternalTextureFormat internalFormat = InternalTextureFormat.fromString(form.format.get()).orElse(InternalTextureFormat.RGBA8);
        PixelType pixelType = PixelType.fromString(form.pixelType.get()).orElse(PixelType.FLOAT);
        TextureType textureType = switch (form.type.get()) {
            case 0 -> TextureType.TEXTURE_1D;
            case 1 -> TextureType.TEXTURE_2D;
            case 2 -> TextureType.TEXTURE_3D;
            default -> null;
        };

        if (form.dynamic.get()) {
            form.image = ImageTextureUtils.createRelativeImage(form.name.get(), internalFormat.getPixelFormat(), internalFormat, pixelType, form.clear.get(), form.scaleX.get(), form.scaleY.get());
        } else {
            form.image = ImageTextureUtils.createImage(form.name.get(), textureType, internalFormat.getPixelFormat(), internalFormat, pixelType, form.clear.get(), form.width.get(), form.height.get(), form.depth.get());
        }
    }
}
