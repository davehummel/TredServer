package me.davehummel.tredserver.command.math;

import me.davehummel.tredserver.command.InstructionBody;
import me.davehummel.tredserver.command.InstructionType;
import me.davehummel.tredserver.command.math.functions.literal.LiteralByte;

import java.util.List;

/**
 * Created by dmhum_000 on 5/7/2016.
 */

public class FunctionListBody implements InstructionBody {
    private final static MathFunc NULLFUNCTION = new LiteralByte(0);

    public final int funcID;
    public final char funcLetter;
    public List<MathFunc> functionList;


    public FunctionListBody(char funcLetter, int funcID, List<MathFunc> functionList) {
        this.funcID = funcID;
        this.funcLetter = funcLetter;
        if (funcID<0 || funcID>19)
            throw new NullPointerException("Function ID must be 0-19");
        if (funcLetter<'A' || funcLetter>'Z')
            throw new NullPointerException(("Function letter must be A-Z"));
        this.functionList = functionList;
    }

    @Override
    public InstructionType getType() {
        return InstructionType.Command;
    }

    @Override
    public void _toString(StringBuilder builder) {

        builder.append("FUN ");
        builder.append(funcLetter);
        builder.append(funcID);
        builder.append(" ");
        if (functionList == null){
            NULLFUNCTION._toString(builder);
        }else{
            boolean first = true;
            for (MathFunc function:functionList) {
                if (!first) {
                    builder.append(',');
                } else {
                    first = false;
                }
                if (function == null){
                    NULLFUNCTION._toString(builder);
                }else {
                    function._toString(builder);
                }
            }
        }
    }


}
