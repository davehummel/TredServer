package me.davehummel.tredserver.serial;

/**
 * Created by dmhum_000 on 4/2/2017.
 */
public class TimeLine extends StandardLine {
    private static final byte[] emptyData = new byte[0];

    public TimeLine(long offset) {
        super(' ',0,emptyData);
    }
}
