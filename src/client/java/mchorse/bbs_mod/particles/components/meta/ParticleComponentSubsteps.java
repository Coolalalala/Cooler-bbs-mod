package mchorse.bbs_mod.particles.components.meta;

import mchorse.bbs_mod.data.types.BaseType;
import mchorse.bbs_mod.data.types.MapType;
import mchorse.bbs_mod.math.molang.MolangException;
import mchorse.bbs_mod.math.molang.MolangParser;
import mchorse.bbs_mod.math.molang.expressions.MolangExpression;
import mchorse.bbs_mod.particles.components.IComponentParticleInitialize;
import mchorse.bbs_mod.particles.components.IComponentParticleUpdate;
import mchorse.bbs_mod.particles.components.ParticleComponentBase;
import mchorse.bbs_mod.particles.components.motion.ParticleComponentMotion;
import mchorse.bbs_mod.particles.emitter.Particle;
import mchorse.bbs_mod.particles.emitter.ParticleEmitter;

public class ParticleComponentSubsteps extends ParticleComponentMotion implements IComponentParticleInitialize, IComponentParticleUpdate {
    public MolangExpression substeps = MolangParser.ONE;

    @Override
    protected void toData(MapType data) {
        if (!MolangExpression.isOne(this.substeps)) data.put("substeps", this.substeps.toData());
    }

    public ParticleComponentBase fromData(BaseType data, MolangParser parser) throws MolangException
    {
        if (!data.isMap())
        {
            return super.fromData(data, parser);
        }

        MapType map = data.asMap();

        if (map.has("substeps")) this.substeps = parser.parseDataSilently(map.get("substeps"), MolangParser.ONE);

        return super.fromData(map, parser);
    }

    @Override
    public void apply(ParticleEmitter emitter, Particle particle) {
        particle.substeps = Integer.max((int) this.substeps.get(), 1);
    }

    @Override
    public void update(ParticleEmitter emitter, Particle particle) {
        particle.substeps = Integer.max((int) this.substeps.get(), 1);
    }
}
