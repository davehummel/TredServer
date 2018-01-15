package me.davehummel.tredserver.serial.jniserial;

/**
 * Created by dmhum_000 on 9/7/2015.
 */
public class JNISerial {
    static{
        System.load("/home/root/TredServer/tredserver/JNI/JNISerial.so");
    }

    static public native String setup();

    static public native void close();

    static public native byte[] readLine();

    static public native String write(String text);
}
