package me.davehummel.tredserver.command.math.functions.ops;

import me.davehummel.tredserver.command.DataType;
import me.davehummel.tredserver.command.math.MathFunc;

/**
 * Created by dmhum_000 on 5/10/2016.
 */
public class CastFunc implements MathFunc {


    private final DataType type;
    public MathFunc func;

    public CastFunc(DataType type, MathFunc func) {
        this.func = func;
        this.type = type;

    }

    @Override
    public void _toString(StringBuilder builder) {
        builder.append('c');
        builder.append(type.LETTER);
        builder.append('[');
        func._toString(builder);
        builder.append(']');
    }


}
