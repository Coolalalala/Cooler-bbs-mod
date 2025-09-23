package mchorse.bbs_mod.particles.functions;

import mchorse.bbs_mod.math.IExpression;
import mchorse.bbs_mod.math.MathBuilder;
import mchorse.bbs_mod.math.functions.NNFunction;
import mchorse.bbs_mod.particles.ParticleMolangParser;
import mchorse.bbs_mod.particles.emitter.ParticleEmitter;

import java.util.ArrayList;

public class SetListVariable extends NNFunction {
    public SetListVariable(MathBuilder builder, IExpression[] expressions, String name) throws Exception {
        super(builder, expressions, name);
    }

    @Override
    protected void verifyArgument(int index, IExpression expression)
    {}

    @Override
    public int getRequiredArguments()
    {
        return 3;
    }

    @Override
    public double doubleValue() {
        if (this.builder instanceof ParticleMolangParser parser)
        {
            String name = this.args[0].stringValue();
            int index = (int) this.args[1].doubleValue();
            Double value = this.args[2].doubleValue();

            ParticleEmitter emitter = parser.scheme.emitter;

            if (emitter == null)
            {
                return 0D;
            }

            ArrayList<Double> list = emitter.listVariables.computeIfAbsent(name, k -> new ArrayList<>());
            while (list.size() <= index)
            {
                list.add(0D);
            }

            list.set(index, value);
        }

        return 0;
    }
}
