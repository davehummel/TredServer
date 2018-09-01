package me.davehummel.tredserver.gpio;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Created by dmhum_000 on 3/18/2017.
 */
public class TurbotGpio {

    static public void setPinValue(int pinNum, boolean high) throws IOException {
        ProcessBuilder pb = new ProcessBuilder("./pin_set.sh", Integer.toString(pinNum) , high?"1":"0");
        Map<String, String> env = pb.environment();
        pb.directory(new File("/home/turbot/embedded/embedded_linux/"));

            Process p = pb.start();

    }

}
