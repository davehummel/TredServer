package me.davehummel.tredserver.command.math.functions.ops;

import me.davehummel.tredserver.command.DataType;
import me.davehummel.tredserver.command.math.MathFunc;

/**
 * Created by dmhum_000 on 5/10/2016.
 */
public class WriteFunc implements MathFunc {

    public char module;
    public String var1;
    public MathFunc func;
    public DataType type;

    public WriteFunc(char module, DataType type, String var1, MathFunc func) {
        this.module = module;
        this.var1 = var1;
        this.func = func;
        this.type = type;
    }

    @Override
    public void _toString(StringBuilder builder) {
        builder.append("w[$");
        builder.append(type.LETTER);
        builder.append(module);
        builder.append(':');
        builder.append(var1);
        builder.append('=');
        func._toString(builder);
        builder.append(']');
    }

    public void altString(StringBuilder builder) {
        builder.append(type.LETTER);
        builder.append(module);
        builder.append(':');
        builder.append(var1);
        builder.append(' ');
        func._toString(builder);
    }


}
