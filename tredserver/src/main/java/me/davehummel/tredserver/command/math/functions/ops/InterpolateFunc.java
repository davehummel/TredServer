package me.davehummel.tredserver.command.math.functions.ops;

import me.davehummel.tredserver.command.math.MathFunc;

/**
 * Created by dmhum_000 on 5/10/2016.
 */
public class InterpolateFunc implements MathFunc {


    public MathFunc func;
    public int interpID;

    public InterpolateFunc(int interpID,MathFunc func) {
        this.func = func;
        this.interpID = interpID;
    }

    @Override
    public void _toString(StringBuilder builder) {
        builder.append('i');
        builder.append(interpID);
        builder.append('[');
        func._toString(builder);
        builder.append(']');
    }


}
