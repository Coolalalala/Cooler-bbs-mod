package mchorse.bbs_mod.particles;

import mchorse.bbs_mod.math.Variable;
import mchorse.bbs_mod.math.molang.MolangParser;
import mchorse.bbs_mod.particles.functions.GetParticleVariable;
import mchorse.bbs_mod.particles.functions.SetParticleVariable;

public class ParticleMolangParser extends MolangParser
{
    public final ParticleScheme scheme;

    public ParticleMolangParser(ParticleScheme scheme)
    {
        this.scheme = scheme;

        this.functions.put("v.set", SetParticleVariable.class);
        this.functions.put("v.get", GetParticleVariable.class);
    }

    @Override
    protected Variable getVariable(String name)
    {
        if (scheme.parallel && (name.startsWith("v.") || name.startsWith("variable."))) {
            return new ParticleVariable(name); // bro i spent so much time figuring out i can just do this
        }

        return super.getVariable(name);
    }
}