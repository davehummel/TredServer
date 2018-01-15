package me.davehummel.tredserver.command.math.functions.ops;

import me.davehummel.tredserver.command.math.MathFunc;

/**
 * Created by dmhum_000 on 5/10/2016.
 */
public class SqrtFunc implements MathFunc {


    public MathFunc func;

    public SqrtFunc(MathFunc func) {
        this.func = func;

    }

    @Override
    public void _toString(StringBuilder builder) {
        builder.append('r');
        func._toString(builder);
    }


}
