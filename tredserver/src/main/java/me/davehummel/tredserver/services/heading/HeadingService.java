package me.davehummel.tredserver.services.heading;

import com.google.common.collect.EvictingQueue;
import me.davehummel.tredserver.command.*;
import me.davehummel.tredserver.serial.SerialConversionUtil;
import me.davehummel.tredserver.serial.StandardLine;
import me.davehummel.tredserver.services.CommandListener;
import me.davehummel.tredserver.services.CommandService;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationConvention;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dmhum_000 on 9/20/2015.
 */
public class HeadingService extends CommandService {

    private static final int HEADINGPINGID = 200;

    private static final int MAXVELSAMPLES = 5;

    private float heading[] = new float[3];
    private float gyro[] = new float[3];
    private float accel[] = new float[3];
    private float grav[] = new float[3];

    private Rotation rotation = null;

    private float xRotVel = 0;
    private EvictingQueue<Float> xHist = EvictingQueue.create(MAXVELSAMPLES);
    private boolean xHistCalc = false;


    private final List<CommandListener> listeners = new ArrayList<>();
    private final ScheduledInstruction headingRead = new ScheduledInstruction(
            'F', HEADINGPINGID, 10, 10, 0, new ReadBody(DataType.FLOAT, new String[]{"HED", "GYR", "ACC", "GRV"}, 3, 0));

    public static final HeadingService INSTANCE = new HeadingService();

    private HeadingService() {
        listeners.add(new CommandListener() {

            @Override
            public boolean matches(StandardLine line) {
                return (line.module == 'F' && line.instructionID == HEADINGPINGID);
            }


            @Override
            protected void processData(StandardLine line) {
                heading[0] = SerialConversionUtil.getFloat(line.raw, 5);
                heading[1] = SerialConversionUtil.getFloat(line.raw, 9);
                heading[2] = SerialConversionUtil.getFloat(line.raw, 13);

                rotation = null;

                gyro[0] = SerialConversionUtil.getFloat(line.raw, 17);
                gyro[1] = SerialConversionUtil.getFloat(line.raw, 21);
                gyro[2] = SerialConversionUtil.getFloat(line.raw, 25);


                accel[0] = SerialConversionUtil.getFloat(line.raw, 29);
                accel[1] = SerialConversionUtil.getFloat(line.raw, 33);
                accel[2] = SerialConversionUtil.getFloat(line.raw, 37);

                grav[0] = SerialConversionUtil.getFloat(line.raw, 41);
                grav[1] = SerialConversionUtil.getFloat(line.raw, 45);
                grav[2] = SerialConversionUtil.getFloat(line.raw, 49);
            }
        });
    }

    @Override
    public void restartEmbedded() {

    }

    @Override
    public List<CommandListener> getListeners() {
        return listeners;
    }

    public float[] getHeading() {
        return heading;
    }

    public float calcXRotVel() {
        if (xHistCalc)
            return xRotVel;
        xHistCalc = true;
        xRotVel = 0;
        float weight = 0;
        int count = xHist.size();
        for (float x : xHist) {
            float temp = (float) Math.sqrt(count);
            xRotVel += x * temp;
            weight += temp;
            count--;
        }
        xRotVel /= weight;
        return xRotVel;
    }


    public void start() {

        xHist.clear();
        xHistCalc = false;
        bridge.writeKill(HEADINGPINGID);
        bridge.writeInstruction(headingRead);
    }

    public void stop() {
        bridge.writeKill(HEADINGPINGID);
    }


    public Rotation getRotation(){
        if (rotation==null){
            rotation =   new Rotation(RotationOrder.XYZ, RotationConvention.VECTOR_OPERATOR,heading[2]*0.0174532925,heading[1]*0.0174532925,heading[0]*0.0174532925);
        }
        return rotation;
    }

}
