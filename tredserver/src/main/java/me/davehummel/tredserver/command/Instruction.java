package me.davehummel.tredserver.command;

/**
 * Created by dmhum_000 on 9/22/2015.
 */
public abstract class Instruction {

    public final char module;
    public final long libID;

    public Instruction( char module, long libID) {
        this.libID = libID;
        this.module = module;
    }

    final public String toString() {
        StringBuilder sb = new StringBuilder();
        toString(sb);
        return sb.toString();
    }

    final public void toString(StringBuilder builder) {
        builder.append(getPrefix());
        builder.append(getType().header);
        _toString(builder);
    }

    protected abstract char getPrefix();


    abstract protected void _toString(StringBuilder builder);

    abstract protected InstructionType getType();
}
