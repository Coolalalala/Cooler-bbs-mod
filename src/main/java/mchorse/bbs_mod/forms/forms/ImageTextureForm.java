package mchorse.bbs_mod.forms.forms;


import mchorse.bbs_mod.settings.values.core.ValueString;
import mchorse.bbs_mod.settings.values.numeric.ValueBoolean;
import mchorse.bbs_mod.settings.values.numeric.ValueFloat;
import mchorse.bbs_mod.settings.values.numeric.ValueInt;
import net.irisshaders.iris.gl.image.GlImage;

public class ImageTextureForm extends Form {

    public final ValueString name = new ValueString("name", "");
    public final ValueString format = new ValueString("format", "RGBA8");
    public final ValueString pixelType = new ValueString("pixelType", "UNSIGNED_BYTE");
    public final ValueBoolean dynamic = new ValueBoolean("dynamic", false);
    public final ValueFloat scaleX = new ValueFloat("scaleX", 1F);
    public final ValueFloat scaleY = new ValueFloat("scaleY", 1F);
    public final ValueInt type = new ValueInt("type", 1); // Dimension
    public final ValueInt width = new ValueInt("width", 0, 1, Integer.MAX_VALUE);
    public final ValueInt height = new ValueInt("height", 0, 1, Integer.MAX_VALUE);
    public final ValueInt depth = new ValueInt("depth", 0, 1, Integer.MAX_VALUE);
    public final ValueBoolean clear = new ValueBoolean("clear", false);

    public GlImage image = null;

    public ImageTextureForm() {
        super();

        this.add(this.name);
        this.add(this.format);
        this.add(this.pixelType);
        this.add(this.dynamic);
        this.add(this.scaleX);
        this.add(this.scaleY);
        this.add(this.type);
        this.add(this.width);
        this.add(this.height);
        this.add(this.depth);
        this.add(this.clear);
    }

    @Override
    protected String getDefaultDisplayName() {
        if (this.name.get().isBlank()) return "Image Texture";
        return "ImageTexture: " + this.name.get();
    }
}
