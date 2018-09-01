package me.davehummel.tredserver.services.lidar;

import me.davehummel.tredserver.command.*;
import me.davehummel.tredserver.command.math.*;
import me.davehummel.tredserver.command.math.functions.Variable;
import me.davehummel.tredserver.command.math.functions.literal.LiteralU16Int;
import me.davehummel.tredserver.command.math.functions.ops.InterpolateFunc;
import me.davehummel.tredserver.command.math.functions.ops.StandardFunc;
import me.davehummel.tredserver.command.math.functions.ops.WriteFunc;
import me.davehummel.tredserver.serial.SerialConversionUtil;
import me.davehummel.tredserver.serial.StandardLine;
import me.davehummel.tredserver.services.CommandListener;
import me.davehummel.tredserver.services.CommandService;
import me.davehummel.tredserver.services.heading.HeadingService;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dmhum_000 on 9/20/2015.
 */
public class LidarService extends CommandService {

    private static final int LIDARPINGID = 201;
    private static final int PANPINGID = 202;

    private static final int MAXSAMPLES = 100000;

    private static final int PANINTERPID = 11;
    private static final int TILTINTERPID = 12;

    private static final short MAXSERVOPOINTS = 1024;
    private static final double MAXSERVOANGLE = 5.23599;
    private static final double MAXREALANGLE = 6.28318530718;
    private static final Vector3D ZEROORGIN = new Vector3D(0,0,0);

    private final short[] lidarDist = new short[MAXSAMPLES];
    private final double[] pans = new double[MAXSAMPLES];
    private final double[] tilts = new double[MAXSAMPLES];

    private final Vector3D[] measure = new Vector3D[MAXSAMPLES];

    private int count = 0;
    private long startTime = 0;
    private int rate = 0;

    private final List<CommandListener> listeners = new ArrayList<>();

    private final ImmediateInstruction setPanInterp = new ImmediateInstruction('C',PANPINGID,new InterpolationBody(PANINTERPID));
    private final ImmediateInstruction setTiltInterp = new ImmediateInstruction('C',PANPINGID,new InterpolationBody(TILTINTERPID));

    private final ImmediateInstruction lidarActivate = new ImmediateInstruction('L',LIDARPINGID,new WriteBody(DataType.BYTE,"PWR",1));
    private final ImmediateInstruction panActivate = new ImmediateInstruction('P',PANPINGID,new WriteBody(DataType.BYTE,"ENA",1));
    private final ImmediateInstruction vrpReset = new ImmediateInstruction('C',PANPINGID,new WriteBody(DataType.UINT_16,"VRP",0));

    private  ScheduledInstruction panPlace = new ScheduledInstruction('P',PANPINGID,0,0,1,null);

    private  ScheduledInstruction lidarRead = new ScheduledInstruction('C',LIDARPINGID,70,5,MAXSAMPLES,new ReadBody(DataType.UINT_16,"FNP",1,0));
    private  ScheduledInstruction panTiltRead = new ScheduledInstruction('P',PANPINGID,250,5,0,new ReadBody(DataType.UINT_16,"POS",2,0));

    private FunctionListBody funcList = new FunctionListBody('P',0,new ArrayList<>());

    private ImmediateInstruction setLidarFunction = new ImmediateInstruction('C',PANPINGID,funcList);

    private final StandardFunc panMod = new StandardFunc("%",new Variable('C',"VRP",0,DataType.UINT_16),null);

    private final ImmediateInstruction lidarDeactivate = new ImmediateInstruction('L',LIDARPINGID,new WriteBody(DataType.BYTE,"PWR",0));
    private final ImmediateInstruction panDeactivate = new ImmediateInstruction('P',PANPINGID,new WriteBody(DataType.BYTE,"ENA",0));

    public LidarService() {

        funcList.functionList.add(new Variable('L',"DST",0,DataType.UINT_16));
        funcList.functionList.add(new WriteFunc('C',DataType.UINT_16,"VRP",
                new StandardFunc("+",new LiteralU16Int(1),new Variable('C',"VRP",0,DataType.UINT_16))));
        funcList.functionList.add(new WriteFunc('P',DataType.UINT_16,"PAN",new StandardFunc("+",new LiteralU16Int(10),
                new InterpolateFunc(PANINTERPID,panMod))));
        funcList.functionList.add(new WriteFunc('P',DataType.UINT_16,"TLT",
                new InterpolateFunc(TILTINTERPID,new Variable('C',"VRP",0,DataType.UINT_16))));

        listeners.add(new CommandListener() {

            @Override
            public boolean matches(StandardLine line) {
                return (line.module == 'C' && line.instructionID == LIDARPINGID) ||(line.module == 'P' && line.instructionID == PANPINGID) ;
            }


            @Override
            protected void processData(StandardLine line) {
                if (line.module == 'P'){
                    pans[count] = servoAngleToRads(SerialConversionUtil.getU16Int(line.raw, 5));
                    tilts[count] = servoAngleToRads(SerialConversionUtil.getU16Int(line.raw, 7));
                }else if (line.module == 'C') {
                    if (count == 0) {
                        startTime = bridge.getOffset();
                    }

                    lidarDist[count] = (short) SerialConversionUtil.getU16Int(line.raw, 5);

                    measure[count] = PointConversionUtility.convertPanTiltPoint(getOrigin(),getOrientation(),pans[count],tilts[count],lidarDist[count]);

                    count++;

                    if (count >= lidarRead.runCount) {
                        bridge.writeInstruction(lidarDeactivate);
                        bridge.writeInstruction(panDeactivate);
                        printData();
                        return;
                    }
                }
            }
        });
    }


    private void printData() {
//        System.out.println(panTiltRead.toString());
//        System.out.print("Lidar{");
//        System.out.print("["+pans[0]+" "+tilts[0]+"]");
//        System.out.print(lidarDist[0]);
//        for(int i = 1; i < lidarRead.runCount ; i++){
//            System.out.print("["+pans[i]+" "+tilts[i]+"]");
//            System.out.print(lidarDist[i]);
//        }
//        System.out.println("}");

//        for(int i = 1; i < lidarRead.runCount ; i++){
//            System.out.println(measure[i].getX()+" "+measure[i].getY()+" "+measure[i].getZ());
//        }

    }

    @Override
    public void restartEmbedded() {

    }

    @Override
    public List<CommandListener> getListeners() {
        return listeners;
    }


    public void start() {
     //   System.out.println("Lidar Service Start!");

        bridge.writeKill(LIDARPINGID);
        bridge.writeKill(PANPINGID);
        bridge.writeInstruction(lidarDeactivate);
        bridge.writeInstruction(panDeactivate);


    }


    public void scan(int readCount, int readIntervalMS,int startPan,int startTilt, int endPan, int endTilt, int rows) {
        if (readCount>MAXSAMPLES){
            readCount = MAXSAMPLES;
        }

        bridge.writeKill(LIDARPINGID);
        bridge.writeKill(PANPINGID);

        buildPanInterp((InterpolationBody)setPanInterp.body,startPan,endPan,rows,readCount);
        buildTiltInterp((InterpolationBody)setTiltInterp.body,startTilt,endTilt,rows,readCount);
        panMod.rightFunc = new LiteralU16Int(readCount/rows*2);
        bridge.writeInstruction(setPanInterp);
        bridge.writeInstruction(setTiltInterp);
        bridge.writeInstruction(setLidarFunction);
        count = 0;
        lidarRead.runCount = readCount;
        panTiltRead.runCount = 0;
        rate = lidarRead.interval = readIntervalMS;
        panTiltRead.interval = rate;
        bridge.writeInstruction(lidarActivate);
        bridge.writeInstruction(panActivate);
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        panPlace.body = new WriteBody(DataType.UINT_16,"PAN",startPan);
        bridge.writeInstruction(panPlace);
        panPlace.body = new WriteBody(DataType.UINT_16,"TLT",startTilt);
        bridge.writeInstruction(panPlace);
        bridge.writeInstruction(panTiltRead);
        for (int i = 0; i < 100 ; i ++){
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
            double dif = Math.abs(pans[count] - servoAngleToRads(startPan));
            if (dif < 0.0174533){
                dif = Math.abs(tilts[count] - servoAngleToRads(startTilt));
                if (dif < 0.0174533){
             //       System.out.println("GO!"+pans[count]);
                    break;
                }
            }
            if (i == 99){
                throw new NullPointerException("Failed to place pan tilt initial movement");
            }
        }
        panPlace.body = new WriteBody(DataType.UINT_16,"PAN",startPan+10);
        bridge.writeInstruction(panPlace);

        bridge.writeInstruction(vrpReset);
        bridge.writeInstruction(lidarRead);

        //TODO        bridge.writeInstruction(tiltMove);
    }

    private void buildPanInterp(InterpolationBody body, int startPan, int endPan, int rows, int readCount) {
        body.interpTypes.clear();
        body.xVals.clear();
        body.yVals.clear();

        int passCount = readCount/rows;

        body.xVals.add(new LiteralU16Int(0));
        body.yVals.add(new LiteralU16Int(startPan));
        body.interpTypes.add(InterpType.LINEAR);
        body.xVals.add(new LiteralU16Int(passCount));
        body.yVals.add(new LiteralU16Int(endPan));
        body.interpTypes.add(InterpType.LINEAR);
        body.xVals.add(new LiteralU16Int(passCount*2));
        body.yVals.add(new LiteralU16Int(startPan));

    }

    private void buildTiltInterp(InterpolationBody body, int startTilt, int endTilt, int rows,int readCount) {
        body.interpTypes.clear();
        body.xVals.clear();
        body.yVals.clear();

        body.xVals.add(new LiteralU16Int(0));
        body.yVals.add(new LiteralU16Int(startTilt));

            body.interpTypes.add(InterpType.LINEAR);
            body.xVals.add(new LiteralU16Int(readCount));
            body.yVals.add(new LiteralU16Int(endTilt));


    }



    public void stop() {
        bridge.writeKill(LIDARPINGID);
        bridge.writeInstruction(lidarDeactivate);
        bridge.writeInstruction(panDeactivate);
    }

    private Vector3D getOrigin() {
        return ZEROORGIN;
    }


    private Rotation getOrientation() {
        return HeadingService.INSTANCE.getRotation();
    }

    static final private double servoAngleToRads(int val) {
        return ((double)MAXSERVOPOINTS/2-val)/(MAXSERVOPOINTS)*MAXSERVOANGLE ;
    }
}
