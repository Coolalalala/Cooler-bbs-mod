package mchorse.bbs_mod.math.molang.expressions;

import mchorse.bbs_mod.data.types.BaseType;
import mchorse.bbs_mod.data.types.StringType;
import mchorse.bbs_mod.math.Variable;
import mchorse.bbs_mod.math.molang.MolangParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class MolangMultiStatement extends MolangExpression
{
    public List<MolangExpression> expressions = new ArrayList<>();
    public Map<String, Variable> locals = new HashMap<>();

    public MolangExpression initialExpression = null;
    public MolangExpression conditionExpression = null;
    public MolangExpression incrementExpression = null;
    public boolean looping = false;

    public MolangMultiStatement(MolangParser context)
    {
        super(context);
    }

    public MolangMultiStatement setCondition(MolangExpression condition)
    {
        this.conditionExpression = condition;
        return this;
    }

    public MolangMultiStatement setWhileLoop(MolangExpression condition)
    {
        this.looping = true;
        this.conditionExpression = condition;
        return this;
    }

    public MolangMultiStatement setForLoop(MolangExpression init, MolangExpression condition, MolangExpression increment)
    {
        this.looping = true;
        this.initialExpression = init;
        this.conditionExpression = condition;
        this.incrementExpression = increment;
        return this;
    }


    @Override
    public double get()
    {
        if (conditionExpression == null)
        {
            return this.evaluate();
        }

        if (initialExpression != null) {
            initialExpression.get();
        }

        if (conditionExpression.get() != 1D)
        { // if failed condition
            return 0;
        }

        if (looping)
        {
            int i = 0;
            double value = 0;
            while(conditionExpression.get() == 1D && i < 2147483646) {
                value = this.evaluate();
                i++;

                // increment
                if (incrementExpression != null) {
                    incrementExpression.get();
                }
            }

            if (i > 2147483646) {
                System.out.println("[BBS Snowstorm] Maximum loop depth exceeded (i > 2^31)");
                this.conditionExpression = null;
                this.looping = false;
                return 0;
            }
            return value;
        }
        return this.evaluate();
    }

    private double evaluate() {
        double value = 0;

        for (MolangExpression expression : this.expressions)
        {
            value = expression.get();

            if (expression instanceof MolangValue && ((MolangValue) expression).returns)
            {
                break;
            }
        }

        return value;
    }

    @Override
    public BaseType toData()
    {
        return new StringType(this.content());
    }

    public String content()
    {
        return this.toString().substring(1, this.toString().length() - 1);
    }

    @Override
    public String toString()
    {
        StringJoiner builder = new StringJoiner("; ", "{", "}");

        for (MolangExpression expression : this.expressions)
        {
            builder.add(expression.toString());
        }

        String control = "";
        if (this.conditionExpression != null)
        {
            control = "(";
            control += initialExpression != null ? initialExpression + ", " : "";
            control += conditionExpression.toString();
            control += incrementExpression != null ? ", " + incrementExpression : "";
            control += ") ";
            if (this.looping)
            {
                if (this.initialExpression != null)
                {
                    control = "for " + control;
                }
                else {
                    control = "while " + control;
                }
            }
            else
            {
                control = "if " + control;
            }
        }

        return control + builder.toString().replace("};", "}");
    }
}
