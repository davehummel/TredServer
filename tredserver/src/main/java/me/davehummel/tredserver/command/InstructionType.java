package me.davehummel.tredserver.command;

/**
 * Created by dmhum_000 on 4/23/2016.
 */
public enum InstructionType {
    Write("W"),
    Read("R"),  Command("C") ;

    InstructionType(String header) {
        this.header = header;
    }

    public final String header;
}
