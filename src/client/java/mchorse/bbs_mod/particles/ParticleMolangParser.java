package mchorse.bbs_mod.particles;

import mchorse.bbs_mod.math.Variable;
import mchorse.bbs_mod.math.molang.MolangException;
import mchorse.bbs_mod.math.molang.MolangParser;
import mchorse.bbs_mod.math.molang.expressions.MolangAssignment;
import mchorse.bbs_mod.math.molang.expressions.MolangExpression;
import mchorse.bbs_mod.math.molang.expressions.MolangValue;
import mchorse.bbs_mod.particles.emitter.Particle;
import mchorse.bbs_mod.particles.functions.GetParticleVariable;
import mchorse.bbs_mod.particles.functions.SetParticleVariable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParticleMolangParser extends MolangParser
{
    public final ParticleScheme scheme;
    public final Map<Map<String, Variable>, Particle> particleLocals = new HashMap<>();

    public ParticleMolangParser(ParticleScheme scheme)
    {
        this.scheme = scheme;

        this.functions.put("v.set", SetParticleVariable.class);
        this.functions.put("v.get", GetParticleVariable.class);
    }

    @Override
    protected Variable getVariable(String name)
    {
        if (scheme.parallel) {
            if (name.startsWith("v.particle") || name.startsWith("variable.particle")) {
                return new ParticleVariable(name); // bro i spent so much time figuring out i can just do this
            }
        }

        return super.getVariable(name);
    }

    @Override
    protected MolangExpression parseOneLine(String expression) throws MolangException
    {
        if (!scheme.parallel) super.parseOneLine(expression);

        expression = expression.trim();

        if (expression.startsWith(RETURN))
        {
            try
            {
                return new MolangValue(this, this.parse(expression.substring(RETURN.length()))).addReturn();
            }
            catch (Exception e)
            {
                throw new MolangException("Couldn't parse return '" + expression + "' expression!");
            }
        }

        try
        {
            List<Object> symbols = this.breakdownChars(this.breakdown(expression));

            /* Assignment it is */
            if (symbols.size() >= 3 && symbols.get(0) instanceof String && this.isVariable(symbols.get(0)) && symbols.get(1).equals("="))
            {
                String name = (String) symbols.get(0);
                symbols = symbols.subList(2, symbols.size());

                Variable variable = null;

                if (!this.registerAsGlobals && !this.variables.containsKey(name) && !this.currentStatement.locals.containsKey(name))
                {
                    variable = new ParticleMultithreadVariable(name, 0);
                    this.currentStatement.locals.put(name, variable);
                }
                else
                {
                    variable = this.getVariable(name);
                }

                return new MolangAssignment(this, variable, this.parseSymbolsMolang(symbols));
            }

            return new MolangValue(this, this.parseSymbolsMolang(symbols));
        }
        catch (Exception e)
        {
            throw new MolangException("Couldn't parse '" + expression + "' expression!");
        }
    }
}