package mchorse.bbs_mod.ui.particles.sections;

import mchorse.bbs_mod.l10n.keys.IKey;
import mchorse.bbs_mod.math.molang.MolangParser;
import mchorse.bbs_mod.particles.ParticleScheme;
import mchorse.bbs_mod.particles.components.motion.ParticleComponentMotionCollision;
import mchorse.bbs_mod.ui.UIKeys;
import mchorse.bbs_mod.ui.framework.elements.buttons.UIToggle;
import mchorse.bbs_mod.ui.framework.elements.input.UITrackpad;
import mchorse.bbs_mod.ui.particles.UIParticleSchemePanel;

public class UIParticleSchemeCollisionSection extends UIParticleSchemeComponentSection<ParticleComponentMotionCollision>
{
    public UIToggle enabled;
    public UITrackpad drag;
    public UITrackpad bounciness;
    public UITrackpad collisionFriction;
    public UITrackpad radius;
    public UIToggle expire;

    private boolean wasPresent;

    public UIParticleSchemeCollisionSection(UIParticleSchemePanel parent)
    {
        super(parent);

        this.enabled = new UIToggle(UIKeys.SNOWSTORM_COLLISION_ENABLED, (b) ->
        {
            this.component.enabled = b.getValue();
            this.editor.dirty();
        });
        this.drag = new UITrackpad((value) ->
        {
            this.component.collisionDrag = value.floatValue();
            this.editor.dirty();
        });
        this.drag.tooltip(UIKeys.SNOWSTORM_COLLISION_DRAG);
        this.bounciness = new UITrackpad((value) ->
        {
            this.component.bounciness = value.floatValue();
            this.editor.dirty();
        });
        this.bounciness.tooltip(UIKeys.SNOWSTORM_COLLISION_BOUNCINESS);
        this.collisionFriction = new UITrackpad((value) ->
        {
            this.component.collisionFriction = value.floatValue();
            this.editor.dirty();
        });
        this.collisionFriction.tooltip(UIKeys.SNOWSTORM_COLLISION_FRICTION);
        this.radius = new UITrackpad((value) ->
        {
            this.component.radius = value.floatValue();
            this.editor.dirty();
        });
        this.radius.tooltip(UIKeys.SNOWSTORM_COLLISION_RADIUS);
        this.expire = new UIToggle(UIKeys.SNOWSTORM_COLLISION_EXPIRE, (b) ->
        {
            this.component.expireOnImpact = b.getValue();
            this.editor.dirty();
        });

        this.fields.add(this.enabled, this.drag, this.bounciness, this.collisionFriction, this.radius, this.expire);
    }

    @Override
    public IKey getTitle()
    {
        return UIKeys.SNOWSTORM_COLLISION_TITLE;
    }

    @Override
    protected ParticleComponentMotionCollision getComponent(ParticleScheme scheme)
    {
        this.wasPresent = this.scheme.get(ParticleComponentMotionCollision.class) != null;

        return scheme.getOrCreate(ParticleComponentMotionCollision.class);
    }

    @Override
    protected void fillData()
    {
        this.enabled.setValue(this.component.enabled);
        this.drag.setValue(this.component.collisionDrag);
        this.bounciness.setValue(this.component.bounciness);
        this.collisionFriction.setValue(this.component.collisionFriction);
        this.radius.setValue(this.component.radius);
        this.expire.setValue(this.component.expireOnImpact);
    }
}
