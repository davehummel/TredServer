package me.davehummel.tredserver.command.math.functions.ops;

import me.davehummel.tredserver.command.math.MathFunc;

/**
 * Created by dmhum_000 on 5/10/2016.
 */
public class ColorFunc implements MathFunc{


    public MathFunc r,g,b;

    public ColorFunc(MathFunc r, MathFunc g, MathFunc b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    @Override
    public void _toString(StringBuilder builder) {
        builder.append('[');
        r._toString(builder);
        builder.append(',');
        g._toString(builder);
        builder.append(',');
        b._toString(builder);
        builder.append(']');
    }


}
