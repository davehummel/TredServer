package me.davehummel.tredserver.serial;

import java.util.Arrays;

/**
 * Created by dmhum_000 on 9/13/2015.
 */
public class StandardLine implements SerialLine {

    public static StandardLine createLine(byte[] line) {
        return new StandardLine((char)line[0],SerialConversionUtil.getU32Int(line,1),line);
    }

    public static StandardLine timeLine(long offset) {
        return new TimeLine(offset);
    }

    public final char module;
    public final long instructionID;
    public final byte[] raw;

    protected StandardLine(char module,long instructionID,byte[] raw){
        this.module = module;
        this.instructionID = instructionID;
        this.raw = raw;
    }

    @Override
    public String toString() {
        return "StandardLine{" +
                "module=" + module +
                ", instructionID=" + instructionID +
                ", raw=" + Arrays.toString(raw) +
                '}';
    }


}
