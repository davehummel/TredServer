package me.davehummel.tredserver.command.math.functions.ops;

import me.davehummel.tredserver.command.math.MathFunc;

/**
 * Created by dmhum_000 on 5/10/2016.
 */
public class StandardFunc implements MathFunc {


    public String opType;
    public MathFunc leftFunc,rightFunc;

    public StandardFunc(String opType, MathFunc leftFunc, MathFunc rightFunc) {
        this.opType = opType;
        this.leftFunc = leftFunc;
        this.rightFunc = rightFunc;
    }

    @Override
    public void _toString(StringBuilder builder) {
        builder.append('{');
        leftFunc._toString(builder);
        builder.append(opType);
        rightFunc._toString(builder);
        builder.append('}');
    }


}
