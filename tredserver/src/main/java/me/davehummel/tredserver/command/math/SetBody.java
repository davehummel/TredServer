package me.davehummel.tredserver.command.math;

import me.davehummel.tredserver.command.InstructionBody;
import me.davehummel.tredserver.command.InstructionType;
import me.davehummel.tredserver.command.math.functions.Variable;
import me.davehummel.tredserver.command.math.functions.literal.LiteralByte;
import me.davehummel.tredserver.command.math.functions.ops.WriteFunc;

/**
 * Created by dmhum_000 on 5/7/2016.
 */

public class SetBody implements InstructionBody {


    private final WriteFunc function;

    public SetBody(WriteFunc function) {
        this.function = function;
    }


    @Override
    public InstructionType getType() {
        return InstructionType.Command;
    }

    @Override
    public void _toString(StringBuilder builder) {

        builder.append("SET ");
        function.altString(builder);

    }


}
