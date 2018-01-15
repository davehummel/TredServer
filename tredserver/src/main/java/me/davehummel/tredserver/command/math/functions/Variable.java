package me.davehummel.tredserver.command.math.functions;

import me.davehummel.tredserver.command.DataType;
import me.davehummel.tredserver.command.math.MathFunc;

/**
 * Created by dmhum_000 on 5/10/2016.
 */
public class Variable implements MathFunc {
    public char module;
    public String var1;
    public int var2;
    public DataType type;

    public Variable(char module, String var1, int var2, DataType type) {
        this.module = module;
        this.var1 = var1;
        this.var2 = var2;
        this.type = type;
    }

    @Override
    public void _toString(StringBuilder builder) {
        builder.append('$');
        builder.append(type.LETTER);
        builder.append(module);
        builder.append(':');
        builder.append(var1);
        builder.append('.');
        builder.append(var2);
    }
}
