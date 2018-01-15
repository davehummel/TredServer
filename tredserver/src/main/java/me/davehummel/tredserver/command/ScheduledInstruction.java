package me.davehummel.tredserver.command;

/**
 * Created by dmhum_000 on 9/22/2015.
 */
public class ScheduledInstruction extends Instruction {


    public int delay;
    public int interval;
    public int runCount;
    public InstructionBody body;


    public ScheduledInstruction(char module, int libID, int delay,
                                int interval, int runCount, InstructionBody body) {
        super( module, libID);
        this.delay = delay;
        this.interval = interval;
        this.runCount = runCount;
        this.body = body;
    }


    @Override
    protected char getPrefix() {
        return 'S';
    }

    public void _toString(StringBuilder builder) {
        builder.append(' ');
        builder.append(this.libID);
        builder.append(' ');
        builder.append(this.module);
        builder.append(' ');
        builder.append(this.delay);
        builder.append(' ');
        builder.append(this.interval);
        builder.append(' ');
        builder.append(this.runCount);
        builder.append(' ');
        body._toString(builder);
    }

    @Override
    protected InstructionType getType() {
        return body.getType();
    }
}
