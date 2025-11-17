package mchorse.bbs_mod.particles.functions;

import mchorse.bbs_mod.math.IExpression;
import mchorse.bbs_mod.math.MathBuilder;
import mchorse.bbs_mod.math.functions.SNFunction;
import mchorse.bbs_mod.particles.ParticleMolangParser;
import mchorse.bbs_mod.particles.emitter.Particle;
import mchorse.bbs_mod.particles.emitter.ParticleEmitter;

public class GetParticleVariable extends SNFunction
{
    public GetParticleVariable(MathBuilder builder, IExpression[] expressions, String name) throws Exception
    {
        super(builder, expressions, name);
    }

    @Override
    protected void verifyArgument(int index, IExpression expression)
    {}

    @Override
    public int getRequiredArguments()
    {
        return 1;
    }

    @Override
    public double doubleValue()
    {
        if (this.builder instanceof ParticleMolangParser parser)
        {
            String name = this.args[this.args.length > 1 ? 1 : 0].stringValue();
            Particle particle;
            if (parser.scheme.parallel) {
                particle = ParticleEmitter.evaluationParticle.get();
            } else {
                particle = parser.scheme.particle;
            }

            if (this.args.length > 1)
            {
                 particle = parser.scheme.emitter.getParticleByIndex((int) this.args[0].doubleValue());
            }

            if (particle == null)
            {
                return 0D;
            }

            if (name.startsWith("variable.") || name.startsWith("v.")) {
                return parser.getOrCreateVariable(name).doubleValue();
            }
            return particle.localValues.getOrDefault(name, 0D);
        }

        return 0;
    }
}