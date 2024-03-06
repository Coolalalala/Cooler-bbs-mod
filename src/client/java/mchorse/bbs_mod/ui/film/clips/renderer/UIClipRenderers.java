package mchorse.bbs_mod.ui.film.clips.renderer;

import mchorse.bbs_mod.camera.clips.misc.AudioClip;
import mchorse.bbs_mod.camera.clips.misc.VoicelineClip;
import mchorse.bbs_mod.utils.clips.Clip;

import java.util.HashMap;
import java.util.Map;

public class UIClipRenderers
{
    private UIClipRenderer defaultRenderer;

    private Map<Class, IUIClipRenderer> renderers = new HashMap<>();

    public UIClipRenderers()
    {
        this.defaultRenderer = new UIClipRenderer();

        this.register(AudioClip.class, new UIAudioClipRenderer());
        this.register(VoicelineClip.class, new UIVoicelineClipRenderer());
    }

    public void register(Class key, IUIClipRenderer renderer)
    {
        this.renderers.put(key, renderer);
    }

    public <T extends Clip> IUIClipRenderer<T> get(T clip)
    {
        IUIClipRenderer renderer = this.renderers.get(clip.getClass());

        return renderer == null ? this.defaultRenderer : renderer;
    }
}