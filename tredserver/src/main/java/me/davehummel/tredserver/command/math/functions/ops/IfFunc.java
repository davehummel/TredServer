package me.davehummel.tredserver.command.math.functions.ops;

import me.davehummel.tredserver.command.DataType;
import me.davehummel.tredserver.command.math.MathFunc;

/**
 * Created by dmhum_000 on 5/10/2016.
 */
public class IfFunc implements MathFunc {


    public MathFunc evalFunc;
    public MathFunc trueFunc,falseFunc;

    public IfFunc(MathFunc evalFunc, MathFunc trueFunc, MathFunc falseFunc) {
        this.evalFunc = evalFunc;
        this.trueFunc = trueFunc;
        this.falseFunc = falseFunc;
    }

    @Override
    public void _toString(StringBuilder builder) {
        builder.append("?");
        evalFunc._toString(builder);
        builder.append("[");
        trueFunc._toString(builder);
        builder.append(',');
        falseFunc._toString(builder);
        builder.append(']');
    }


}
