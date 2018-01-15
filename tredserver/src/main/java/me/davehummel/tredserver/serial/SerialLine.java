package me.davehummel.tredserver.serial;

import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Created by dmhum_000 on 9/13/2015.
 */
public interface SerialLine {
    Charset CHARSET = Charset.forName("UTF-8");
    SerialLine PING = new SerialLine() {    };

    static String convert(byte[] line, int i, int length) {
        return new String(Arrays.copyOfRange(line,i,i+length),CHARSET);
    }

    static byte[] convert(String in){
        return in.getBytes(CHARSET);
    }
}
