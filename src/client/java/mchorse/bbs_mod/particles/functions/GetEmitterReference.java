package mchorse.bbs_mod.particles.functions;

import mchorse.bbs_mod.math.Constant;
import mchorse.bbs_mod.math.IExpression;
import mchorse.bbs_mod.math.MathBuilder;
import mchorse.bbs_mod.math.functions.SNFunction;
import mchorse.bbs_mod.particles.ParticleMolangParser;
import mchorse.bbs_mod.particles.emitter.ParticleEmitter;

public class GetEmitterReference extends SNFunction
{
    public GetEmitterReference(MathBuilder builder, IExpression[] expressions, String name) throws Exception
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
            String name = this.args[0].stringValue();
            if (name == null) return 0;
            ParticleEmitter emitter = parser.scheme.emitter;

            if (emitter == null)
            {
                return 0D;
            }

            return emitter.variables.getOrDefault(name, new Constant(0D)).doubleValue();
        }

        return 0;
    }
}