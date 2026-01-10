package mchorse.bbs_mod.ui.particles.sections;

import mchorse.bbs_mod.BBSModClient;
import mchorse.bbs_mod.graphics.texture.Texture;
import mchorse.bbs_mod.l10n.keys.IKey;
import mchorse.bbs_mod.particles.ParticleMaterial;
import mchorse.bbs_mod.particles.ParticleScheme;
import mchorse.bbs_mod.particles.components.appearance.ParticleComponentAppearanceBillboard;
import mchorse.bbs_mod.resources.Link;
import mchorse.bbs_mod.ui.UIKeys;
import mchorse.bbs_mod.ui.framework.elements.buttons.UIButton;
import mchorse.bbs_mod.ui.framework.elements.buttons.UICirculate;
import mchorse.bbs_mod.ui.framework.elements.buttons.UIToggle;
import mchorse.bbs_mod.ui.framework.elements.input.UITexturePicker;
import mchorse.bbs_mod.ui.framework.elements.input.UITrackpad;
import mchorse.bbs_mod.ui.framework.elements.input.text.UITextbox;
import mchorse.bbs_mod.ui.particles.UIParticleSchemePanel;
import mchorse.bbs_mod.ui.utils.UI;

public class UIParticleSchemeGeneralSection extends UIParticleSchemeSection
{
    public UITextbox identifier;
    public UIButton pick;
    public UICirculate material;
    public UIToggle parallel;
    public UICirculate integrator;
    public UITrackpad timeScale;

    public UIParticleSchemeGeneralSection(UIParticleSchemePanel parent)
    {
        super(parent);

        this.identifier = new UITextbox(100, (str) ->
        {
            this.scheme.identifier = str;
            this.editor.dirty();
        });
        this.identifier.tooltip(UIKeys.SNOWSTORM_GENERAL_IDENTIFIER);

        this.pick = new UIButton(UIKeys.SNOWSTORM_GENERAL_PICK, (b) ->
        {
            UITexturePicker.open(this.getContext(), this.scheme.texture, (link) ->
            {
                if (link == null)
                {
                    link = ParticleScheme.DEFAULT_TEXTURE;
                }

                this.setTextureSize(link);
                this.scheme.texture = link;
                this.editor.dirty();
            });
        });

        this.material = new UICirculate((b) ->
        {
            this.scheme.material = ParticleMaterial.values()[this.material.getValue()];
            this.editor.dirty();
        });
        this.material.addLabel(UIKeys.SNOWSTORM_GENERAL_PARTICLES_OPAQUE);
        this.material.addLabel(UIKeys.SNOWSTORM_GENERAL_PARTICLES_ALPHA);
        this.material.addLabel(UIKeys.SNOWSTORM_GENERAL_PARTICLES_BLEND);

        this.fields.add(this.identifier, UI.row(5, 0, 20, this.pick, this.material));

        this.parallel = new UIToggle(UIKeys.SNOWSTORM_MULTITHREADING, (b) ->
        {
            this.scheme.parallel = b.getValue();
            this.editor.dirty();
        });
        this.parallel.tooltip(UIKeys.SNOWSTORM_MULTITHREADING_TOOLTIP);

        this.integrator = new UICirculate((b) -> {
            this.scheme.integrator = b.getValue();
            this.editor.dirty();
        });
        this.integrator.addLabel(UIKeys.SNOWSTORM_INTEGRATOR_VERLET);
        this.integrator.addLabel(UIKeys.SNOWSTORM_INTEGRATOR_RK4);

        this.timeScale = new UITrackpad((v) ->
        {
            this.scheme.timeScale = v.floatValue();
            this.editor.dirty();
        });
        this.timeScale.limit(0.0F);
        this.fields.add(this.parallel, this.integrator);
        this.fields.add(UI.label(UIKeys.SNOWSTORM_GENERAL_TIME_SCALE), this.timeScale);
    }

    private void setTextureSize(Link link)
    {
        ParticleComponentAppearanceBillboard component = this.scheme.get(ParticleComponentAppearanceBillboard.class);

        if (component == null)
        {
            return;
        }

        Texture texture = BBSModClient.getTextures().getTexture(link);

        component.textureWidth = texture.width;
        component.textureHeight = texture.height;
    }

    @Override
    public IKey getTitle()
    {
        return UIKeys.SNOWSTORM_GENERAL_TITLE;
    }

    @Override
    public void setScheme(ParticleScheme scheme)
    {
        super.setScheme(scheme);

        this.identifier.setText(scheme.identifier);
        this.material.setValue(scheme.material.ordinal());
        this.parallel.setValue(scheme.parallel);
        this.integrator.setValue(scheme.integrator);
        this.timeScale.setValue(scheme.timeScale);
    }
}