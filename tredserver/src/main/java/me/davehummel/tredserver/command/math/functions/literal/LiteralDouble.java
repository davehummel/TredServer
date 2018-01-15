package me.davehummel.tredserver.command.math.functions.literal;

import me.davehummel.tredserver.command.DataType;
import me.davehummel.tredserver.command.math.MathFunc;

/**
 * Created by dmhum_000 on 5/7/2016.
 */
public class LiteralDouble implements MathFunc {

    private static final String HEADER = "#"+DataType.DOUBLE.LETTER;

    public double value;

    public LiteralDouble(double value) {
        this.value = +value;
    }


    @Override
    public void _toString(StringBuilder builder) {
        builder.append(HEADER);
        builder.append(value);
    }
}
