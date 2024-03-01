package mchorse.bbs_mod.graphics.text.format;

import mchorse.bbs_mod.graphics.text.FontRendererContext;
import mchorse.bbs_mod.utils.colors.Colors;

public class RainbowFontFormat extends AmountFontFormat
{
    public RainbowFontFormat()
    {}

    public RainbowFontFormat(char control)
    {
        super(control);
    }

    @Override
    public void process(FontRendererContext context)
    {
        int factor = this.amount > 1 ? (this.amount % 2 == 0 ? context.index : -context.index) : 0;
        float v = (context.time * 50 + factor * 100) % 1500 / 1500F;

        Colors.HSVtoRGB(context.color, v, 1F, 1F);
    }
}