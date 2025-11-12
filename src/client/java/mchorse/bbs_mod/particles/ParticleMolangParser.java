package mchorse.bbs_mod.particles;

import mchorse.bbs_mod.math.Variable;
import mchorse.bbs_mod.math.molang.MolangException;
import mchorse.bbs_mod.math.molang.MolangParser;
import mchorse.bbs_mod.math.molang.expressions.MolangAssignment;
import mchorse.bbs_mod.math.molang.expressions.MolangExpression;
import mchorse.bbs_mod.math.molang.expressions.MolangMultiStatement;
import mchorse.bbs_mod.math.molang.expressions.MolangValue;
import mchorse.bbs_mod.particles.functions.*;

import java.util.ArrayList;
import java.util.List;

public class ParticleMolangParser extends MolangParser
{
    public final ParticleScheme scheme;

    public ParticleMolangParser(ParticleScheme scheme)
    {
        this.scheme = scheme;

        this.functions.put("v.set", SetParticleVariable.class);
        this.functions.put("v.get", GetParticleVariable.class);
        this.functions.put("v.put_ref", SetEmitterReference.class);
        this.functions.put("v.get_ref", GetEmitterReference.class);
        this.functions.put("list.set", SetListVariable.class);
        this.functions.put("list.get", GetListVariable.class);
    }

    @Override
    protected Variable getVariable(String name)
    {
        if (scheme.parallel) {
            if (name.startsWith("v.particle") || name.startsWith("variable.particle")) {
                return new ParticleVariable(name); // bro i spent so much time figuring out i can just do this
            }
            return getVariableParallel(name);
        }

        return super.getVariable(name);
    }

    private Variable getVariableParallel(String name)
    {
        if (name.startsWith("v.")) {
            name = name.replace("v.", "variable.");
        }

        MolangMultiStatement currentStatement = this.currentStatement;
        Variable variable = currentStatement == null ? null : currentStatement.locals.get(name);

        if (variable == null)
        {
            variable = this.variables.get(name);
        }

        if (variable == null)
        {
            variable = new ParticleMultithreadVariable(name, 0);
            this.register(variable);
        }

        return variable;
    }

    private static ArrayList<String> splitExpressions(String expressions)
    {
        ArrayList<String> lines = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int depth = 0;
        for (char c : expressions.toLowerCase().trim().toCharArray())
        {
            if (c == '{')
            {
                depth++;
            }
            else if (c == '}')
            {
                depth--;
                if (depth == 0)
                {
                    current.append(c);
                    if (!current.toString().trim().isEmpty())
                    {
                        lines.add(current.toString().trim());
                    }
                    current = new StringBuilder();
                    continue;
                }
            }
            else if (c == ';' && depth == 0)
            {
                if (!current.toString().trim().isEmpty())
                {
                    lines.add(current.toString().trim());
                }
                current = new StringBuilder();
                continue;
            }
            current.append(c);
        }
        if (!current.toString().trim().isEmpty())
        {
            lines.add(current.toString().trim());
        }
        return lines;
    }

    @Override
    public MolangExpression parseExpression(String expression) throws MolangException
    {
        List<String> lines = splitExpressions(expression);

        if (lines.isEmpty())
        {
            throw new MolangException("Molang expression cannot be blank!");
        }

        MolangMultiStatement parentStatement = this.currentStatement;
        MolangMultiStatement result = new MolangMultiStatement(this);
        if (parentStatement != null) result.locals.putAll(parentStatement.locals);
        this.currentStatement = result;

        try
        {
            for (String line : lines)
            {
                result.expressions.add(this.parseOneLine(line));
            }
        }
        catch (Exception e)
        {
            this.currentStatement = null;

            throw e;
        }

        this.currentStatement = parentStatement;

        return result;
    }

    @Override
    protected MolangExpression parseOneLine(String expression) throws MolangException
    {
        expression = expression.trim();

        if (expression.startsWith("if"))
        {
            int scopeStart = expression.indexOf('{');
            int conditionStart = expression.indexOf('(');
            int conditionEnd = expression.lastIndexOf(')', scopeStart - 1);
            MolangMultiStatement subStatement = (MolangMultiStatement) parseExpression(expression.substring(scopeStart + 1, expression.length() - 1));
            return subStatement.setCondition(parseOneLine(expression.substring(conditionStart + 1, conditionEnd)));
        }
        else if (expression.startsWith("while"))
        {
            int scopeStart = expression.indexOf('{');
            int conditionStart = expression.indexOf('(');
            int conditionEnd = expression.lastIndexOf(')', scopeStart - 1);
            MolangMultiStatement subStatement = (MolangMultiStatement) parseExpression(expression.substring(scopeStart + 1, expression.length() - 1));
            return subStatement.setWhileLoop(parseOneLine(expression.substring(conditionStart + 1, conditionEnd)));
        }
        else if (expression.startsWith("{"))
        {
            return parseExpression(expression.substring(1, expression.length() - 1));
        }
        else if (expression.startsWith("//")) // comment
        {
            int index = expression.indexOf("\n");
            if (index != -1)
            {
                return parseOneLine(expression.substring(index + 1));
            }
            return MolangParser.ZERO;
        }

        if (!scheme.parallel) return super.parseOneLine(expression);

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

    @Override
    public Variable getOrCreateVariable(String key)
    {
        if (!scheme.parallel) return super.getOrCreateVariable(key);

        Variable variable = this.variables.get(key);

        if (variable == null)
        {
            variable = new ParticleMultithreadVariable(key, 0);
            this.register(variable);
        } else if (!(variable instanceof ParticleMultithreadVariable)) {
            variable = new ParticleMultithreadVariable(variable);
            this.variables.remove(key);
            this.register(variable);
        }

        return variable;
    }
}
