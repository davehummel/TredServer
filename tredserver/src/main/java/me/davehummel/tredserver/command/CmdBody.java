package me.davehummel.tredserver.command;

/**
 * Created by dmhum on 4/23/2016.
 */
public class CmdBody implements InstructionBody {

    private final String command;

    public CmdBody(String command) {
        this.command = command;
    }

    @Override
    public InstructionType getType() {
        return InstructionType.Command;
    }

    @Override
    public void _toString(StringBuilder builder) {
        builder.append(command);
    }
}
