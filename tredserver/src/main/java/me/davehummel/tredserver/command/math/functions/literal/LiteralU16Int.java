package me.davehummel.tredserver.command.math.functions.literal;

import me.davehummel.tredserver.command.DataType;
import me.davehummel.tredserver.command.math.MathFunc;

/**
 * Created by dmhum_000 on 5/7/2016.
 */
public class LiteralU16Int implements MathFunc {

    private static final String HEADER = "#"+DataType.UINT_16.LETTER;

    public int value;

    public LiteralU16Int(int value) {
        this.value = +value;
    }


    @Override
    public void _toString(StringBuilder builder) {
        builder.append(HEADER);
        builder.append(value);
    }
}
