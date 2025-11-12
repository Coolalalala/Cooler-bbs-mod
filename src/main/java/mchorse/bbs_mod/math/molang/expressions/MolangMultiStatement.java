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

    public MolangExpression conditionalExpression = null;
    public boolean looping = false;

    public MolangMultiStatement(MolangParser context)
    {
        super(context);
    }

    public MolangMultiStatement setCondition(MolangExpression condition)
    {
        this.conditionalExpression = condition;
        return this;
    }

    public MolangMultiStatement setWhileLoop(MolangExpression condition)
    {
        this.looping = true;
        this.conditionalExpression = condition;
        return this;
    }

    @Override
    public double get()
    {
        if (conditionalExpression == null)
        {
            return this.evaluate();
        }

        if (conditionalExpression.get() != 1D)
        { // if failed condition
            return 0;
        }

        if (looping)
        {
            int i = 0;
            double value = 0;
            while(conditionalExpression.get() == 1D) {
                value = this.evaluate();
                i++;
                if (i > 1000) {
                    System.out.println("[BBS Snowstorm] Infinite loop detected (i > 1000)");
                    this.conditionalExpression = null;
                    this.looping = false;
                    return 0;
                }
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
        if (this.conditionalExpression != null)
        {
            control = "(" + this.conditionalExpression.toString() + ") ";
            if (this.looping)
            {
                control = "while " + control;
            }
            else
            {
                control = "if " + control;
            }
        }

        return control + builder.toString().replace("};", "}");
    }
}
