package mchorse.bbs_mod.particles.functions;

import com.google.common.util.concurrent.AtomicDouble;
import mchorse.bbs_mod.math.IExpression;
import mchorse.bbs_mod.math.MathBuilder;
import mchorse.bbs_mod.math.functions.SNFunction;
import mchorse.bbs_mod.particles.ParticleMolangParser;
import mchorse.bbs_mod.particles.emitter.ParticleEmitter;

import java.util.ArrayList;

public class GetListVariable extends SNFunction
{
    public GetListVariable(MathBuilder builder, IExpression[] expressions, String name) throws Exception
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
            int index = (int) this.args[1].doubleValue();
            ParticleEmitter emitter = parser.scheme.emitter;

            if (emitter == null)
            {
                return 0D;
            }

            ArrayList<AtomicDouble> list = emitter.listVariables.get(name);
            if (list != null && index >= 0 && index < list.size())
            {
                return list.get(index).get();
            }
        }

        return 0;
    }
}