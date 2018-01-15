package me.davehummel.tredserver.command.math;

/**
 * Created by dmhum_000 on 5/7/2016.
 */
public enum InterpType {
    STEP('|'),LINEAR('/'),SMOOTH('~');

    public final char charVal;

    InterpType(char in){
        charVal = in;
    }
}
