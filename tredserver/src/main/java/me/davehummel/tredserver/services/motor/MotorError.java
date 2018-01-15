package me.davehummel.tredserver.services.motor;

/**
 * Created by dmhum_000 on 10/3/2015.
 */
public class MotorError {

    public boolean motor1Fault;
    public boolean motor2Fault;
    public boolean motor1OC;
    public boolean motor2OC;
    public boolean serialError;
    public boolean crcError;
    public boolean formatError;
    public boolean timeout;

    public void update(int error){
        motor1Fault = ((error & 0b1) >0);
        motor2Fault = ((error & 0b10)>0);
        motor1OC =    ((error & 0b100)>0);
        motor2OC =    ((error & 0b1000)>0);
        serialError = ((error & 0b10000)>0);
        crcError =    ((error & 0b100000)>0);
        formatError = ((error & 0b1000000)>0);
        timeout =     ((error & 0b10000000)>0);
    }

}
