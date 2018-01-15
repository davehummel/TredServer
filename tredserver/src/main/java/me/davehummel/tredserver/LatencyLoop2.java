package me.davehummel.tredserver;

import me.davehummel.tredserver.serial.jniserial.JNISerial;


public class LatencyLoop2
{

    public static void main1( String[] args )
    {

        JNISerial.setup();
        while (true) {

            byte[] x = JNISerial.readLine();
            if (x == null){
                JNISerial.write("Null\n");
                continue;
            }else{
                System.out.println(x);
            }
            JNISerial.write("Cows\n");
        }

    }



}
