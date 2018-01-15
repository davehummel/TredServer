package me.davehummel.tredserver.command;

/**
 * Created by dmhum on 4/23/2016.
 */
public interface InstructionBody {
    InstructionType getType();
    void _toString(StringBuilder builder);

}
