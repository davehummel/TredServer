package me.davehummel.tredserver;

import me.davehummel.tredserver.command.*;
import me.davehummel.tredserver.command.math.*;
import me.davehummel.tredserver.command.math.functions.Variable;
import me.davehummel.tredserver.command.math.functions.literal.LiteralByte;
import me.davehummel.tredserver.command.math.functions.literal.LiteralFloat;
import me.davehummel.tredserver.command.math.functions.ops.InterpolateFunc;
import me.davehummel.tredserver.command.math.functions.ops.WriteFunc;
import me.davehummel.tredserver.serial.jsscserial.JsscSerialBridge;
import me.davehummel.tredserver.services.CommandBridge;
import me.davehummel.tredserver.services.ServiceManager;


/**
 * Created by dmhum_000 on 9/13/2015.
 */
public class InterpGlowTest {

    private static final ImmediateInstruction READ = new ImmediateInstruction('V', 101,
            new ReadBody(DataType.UINT_16, "CUR"));

    static public void main1(String[] args) {
        String portName = "/dev/ttyMFD1";
        int portSpeed = 3500000;
        if (args.length > 0) {
            System.out.println(args[0]);
            String[] split = args[0].split("_");
            portName = split[0];
            portSpeed = Integer.parseInt(split[1]);
        }
        System.out.println("Serial using :" + portName + "_" + portSpeed);

        //  CommandBridge bridge = new CommandBridge(new MraaSerialBridge(0,3500000));
        CommandBridge bridge = new CommandBridge(new JsscSerialBridge(portName, portSpeed));

        ServiceManager manager = new ServiceManager(bridge);

//        HeadingService headingService = new HeadingService();
//
//        manager.addService(headingService);

        manager.start();

        InterpolationBody interp = new InterpolationBody(12);
        interp.xVals.add(new LiteralFloat(0));
        interp.yVals.add(new LiteralByte(255));
        interp.interpTypes.add(InterpType.SMOOTH);
        interp.xVals.add(new LiteralFloat(180));
        interp.yVals.add(new LiteralByte(0));
        interp.interpTypes.add(InterpType.SMOOTH);
        interp.xVals.add(new LiteralFloat(360));
        interp.yVals.add(new LiteralByte(255));

        bridge.writeKill(211);

        ImmediateInstruction interpInst = new ImmediateInstruction('C',101,interp);
        bridge.writeInstruction(interpInst);
        SetBody setter = new SetBody(new WriteFunc('B',DataType.BYTE,"GGG",new InterpolateFunc(12,new Variable('F',"HED",0,DataType.FLOAT))));//);
        ScheduledInstruction scheduledInstruction = new ScheduledInstruction('C',211,10,100,10000,setter);
        bridge.writeInstruction(scheduledInstruction);
         setter = new SetBody(new WriteFunc('B',DataType.BYTE,"RRR",new InterpolateFunc(12,new Variable('F',"HED",1,DataType.FLOAT))));//);
         scheduledInstruction = new ScheduledInstruction('C',212,10,100,10000,setter);
        bridge.writeInstruction(scheduledInstruction);
        setter = new SetBody(new WriteFunc('B',DataType.BYTE,"BBB",new InterpolateFunc(12,new Variable('F',"HED",2,DataType.FLOAT))));//);
        scheduledInstruction = new ScheduledInstruction('C',212,10,100,10000,setter);
        bridge.writeInstruction(scheduledInstruction);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }




    }
}
