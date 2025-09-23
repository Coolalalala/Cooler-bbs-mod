package mchorse.bbs_mod.particles.functions;

import mchorse.bbs_mod.math.IExpression;
import mchorse.bbs_mod.math.MathBuilder;
import mchorse.bbs_mod.math.functions.NNFunction;
import mchorse.bbs_mod.particles.ParticleMolangParser;
import mchorse.bbs_mod.particles.emitter.ParticleEmitter;

public class SetEmitterReference extends NNFunction
{
    public SetEmitterReference(MathBuilder builder, IExpression[] expressions, String name) throws Exception
    {
        super(builder, expressions, name);
    }

    @Override
    protected void verifyArgument(int index, IExpression expression)
    {}

    @Override
    public int getRequiredArguments()
    {
        return 2;
    }

    @Override
    public double doubleValue()
    {
        if (this.builder instanceof ParticleMolangParser parser)
        {
            String name = this.args[0].stringValue();
            IExpression value = this.args[1];

            ParticleEmitter emitter = parser.scheme.emitter;

            if (emitter == null || value == null)
            {
                return 0D;
            }

            emitter.variables.put(name, value.get());

            return value.doubleValue();
        }

        return 0;
    }
}