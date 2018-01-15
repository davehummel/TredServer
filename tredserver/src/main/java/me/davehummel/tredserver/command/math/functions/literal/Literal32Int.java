package me.davehummel.tredserver.command.math.functions.literal;

import me.davehummel.tredserver.command.DataType;
import me.davehummel.tredserver.command.math.MathFunc;

/**
 * Created by dmhum_000 on 5/7/2016.
 */
public class Literal32Int implements MathFunc {

    private static final String HEADER = "#"+DataType.INT_32.LETTER;

    public int value;

    public Literal32Int(int value) {
        this.value = +value;
    }


    @Override
    public void _toString(StringBuilder builder) {
        builder.append(HEADER);
        builder.append(value);
    }
}
