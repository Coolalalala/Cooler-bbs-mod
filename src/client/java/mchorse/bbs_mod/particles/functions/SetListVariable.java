package mchorse.bbs_mod.particles.functions;

import com.google.common.util.concurrent.AtomicDouble;
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
    synchronized public double doubleValue() {
        if (this.builder instanceof ParticleMolangParser parser)
        {
            String name = this.args[0].stringValue();
            int index = (int) this.args[1].doubleValue();
            double value = this.args[2].doubleValue();

            ParticleEmitter emitter = parser.scheme.emitter;

            if (emitter == null)
            {
                return 0D;
            }

            ArrayList<AtomicDouble> list = emitter.listVariables.computeIfAbsent(name, k -> new ArrayList<>());
            while (list.size() <= index)
            {
                list.add(new AtomicDouble(0D));
            }

            list.get(index).set(value);
        }

        return 0;
    }
}
