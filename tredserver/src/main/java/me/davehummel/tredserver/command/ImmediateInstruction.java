package me.davehummel.tredserver.command;

/**
 * Created by dmhum_000 on 9/22/2015.
 */
public class ImmediateInstruction extends Instruction {


    public final InstructionBody body;


    public ImmediateInstruction(char module, long libID, InstructionBody body) {
        super( module, libID);

        this.body = body;
    }


    @Override
    protected char getPrefix() {
        return 'I';
    }

    public void _toString(StringBuilder builder) {
        builder.append(' ');
        builder.append(this.libID);
        builder.append(' ');
        builder.append(this.module);
        builder.append(' ');
        body._toString(builder);
    }

    @Override
    protected InstructionType getType() {
        return body.getType();
    }
}
