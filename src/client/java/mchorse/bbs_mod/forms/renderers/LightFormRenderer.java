package mchorse.bbs_mod.forms.renderers;

import mchorse.bbs_mod.forms.LightManager;
import mchorse.bbs_mod.forms.forms.LightForm;
import mchorse.bbs_mod.graphics.texture.Texture;
import mchorse.bbs_mod.resources.Link;
import mchorse.bbs_mod.ui.framework.UIContext;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;
import java.util.List;

public class LightFormRenderer extends FormRenderer<LightForm> {
    public static final Link LIGHT_PREVIEW = Link.assets("textures/light.png");

    public LightFormRenderer(LightForm form) {
        super(form);
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
        // get data
        LightForm form = this.form;
        int color = form.color.get().getRGBColor();
        Vector3f pos = context.stack.peek().getPositionMatrix().getTranslation(new Vector3f());
        Vector3f dir = context.stack.peek().getNormalMatrix().getRotation(new AxisAngle4f()).transform(new Vector3f(0,0,-1));

        LightManager.get().add(List.of(
                pos.x, pos.y, pos.z, (float) color,
                dir.x, dir.y, dir.z, (float) form.type.get() + (form.indirect.get() ? 0.5F : 0F),
                form.intensity.get(), form.angle.get(), form.spread.get(), form.radius.get(),
                form.attenuation.get(), 0F, 0f, 0f
        ));
        LightManager.get().pushBuffer();

        // TODO: render as ui element in f3
    }
}