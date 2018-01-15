package me.davehummel.tredserver.fish.services;

import me.davehummel.tredserver.command.*;
import me.davehummel.tredserver.command.math.InterpType;
import me.davehummel.tredserver.fish.waterlevel.LevelInterpUtility;
import me.davehummel.tredserver.fish.waterlevel.persisted.gyre.GyreInterpolation;
import me.davehummel.tredserver.fish.waterlevel.persisted.gyre.GyreInterpolationRepository;
import me.davehummel.tredserver.fish.waterlevel.persisted.instructions.PumpInstruction;
import me.davehummel.tredserver.fish.waterlevel.persisted.instructions.PumpInstructionRepository;
import me.davehummel.tredserver.fish.waterlevel.persisted.levelinterpolation.LevelInterpolation;
import me.davehummel.tredserver.fish.waterlevel.persisted.levelinterpolation.LevelInterpolationRepository;
import me.davehummel.tredserver.fish.waterlevel.readonly.PumpLevels;
import me.davehummel.tredserver.serial.SerialConversionUtil;
import me.davehummel.tredserver.serial.StandardLine;
import me.davehummel.tredserver.services.CommandListener;
import me.davehummel.tredserver.services.CommandService;
import me.davehummel.tredserver.services.alert.*;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by dmhum_000 on 9/20/2015.
 */

@Controller
public class PumpLevelService extends CommandService {

    private static final int PUMPPOWERPINGID = 17246;

    private static final int PUMPLEVELPINGID = 17248;
    private static final int POWERHEADPINGID = 17249;
    public static final int PUMPINSTRUCTIONID = 17250;
    public static final int CONNECTIONPINGID = 17251;

    private static final String OFFINST = "off";
    private static final String LEVELSINST = "levels";
    private static final String DECIDEINST = "gyres";
    private static final String PUMPSETINST = "pumps";
    private static final String HEADINST = "heads";
    public static final String RUNTINST = "run";


    private final List<CommandListener> listeners = new ArrayList<>();

    private final ScheduledInstruction levelRead = new ScheduledInstruction(
            'R', PUMPLEVELPINGID, 1000, 1000, 0, new ReadBody(DataType.FLOAT, new String[]{"VRA","VRB","ABA","ACA","BBA","BCA"}, 1, 0));
    private final ScheduledInstruction pumpRead = new ScheduledInstruction(
            'R', PUMPPOWERPINGID, 1000, 500, 0, new ReadBody(DataType.FLOAT, new String[]{"CAA","CBB","CCC"}, 1, 0));
    private final ScheduledInstruction connectionQualityRead = new ScheduledInstruction(
            'R', CONNECTIONPINGID, 1000, 5000, 0, new ReadBody(DataType.BYTE, new String[]{"AAA","BBB","CCC"}, 1, 0));
    private final ScheduledInstruction powerHeadRead = new ScheduledInstruction(
            'P', POWERHEADPINGID, 1000, 499, 0, new ReadBody(DataType.BYTE, new String[]{"CCC","DDD", "EEE", "FFF"}, 1, 0));


    private final ImmediateInstruction allPumpsOff = new ImmediateInstruction('P', POWERHEADPINGID, new WriteBody(DataType.BYTE, "VRX", 1));
    private final ScheduledInstruction allPumpsOffEnd = new ScheduledInstruction('P', POWERHEADPINGID, 300000, 0, 1, new WriteBody(DataType.BYTE, "VRX", 0));
    private final ImmediateInstruction allPumpsOn = new ImmediateInstruction('P', POWERHEADPINGID, new WriteBody(DataType.BYTE, "VRX", 0));

    private final ImmediateInstruction topoffOn = new ImmediateInstruction('R', POWERHEADPINGID, new WriteBody(DataType.DOUBLE, "CCC", 1));
  //  private final ScheduledInstruction topoffOnEnd = new ScheduledInstruction('R', POWERHEADPINGID, 2000, 0, 1, new WriteBody(DataType.DOUBLE, "CCC", 0));
    private final ImmediateInstruction topoffOff = new ImmediateInstruction('R', POWERHEADPINGID, new WriteBody(DataType.DOUBLE, "CCC", 0));

    private final ImmediateInstruction[] setupPWM = new ImmediateInstruction[]{
            new ImmediateInstruction('P', 2, new CmdBody("DIN C 18")),
            new ImmediateInstruction('P', 2, new CmdBody("DIN D 19")),
            new ImmediateInstruction('P', 2, new CmdBody("DIN E 21")),
            new ImmediateInstruction('P', 2, new CmdBody("DIN F 20")),
            new ImmediateInstruction('R', 2, new WriteBody(DataType.DOUBLE, "CAA", 120)),
            new ImmediateInstruction('R', 2, new WriteBody(DataType.DOUBLE, "CBB", 120)),
    };

    private float leftLevel, rightLevel, leftFloat1, leftFloat2, rightFloat1,rightFloat2;
    private float leftPower, rightPower, topoffPower;

    private boolean leftHeadOn, rightHeadOn;
    private Date leftHeadChangeTime, rightHeadChangeTime;
    private float totalDepth = 0;
    private DescriptiveStatistics depthFiveMin = new DescriptiveStatistics(30);
    private int leftLevelLoss,rightLevelLoss,pumpControlLoss;


    @Autowired
    LevelInterpolationRepository levelInterpolationRepository;

    @Autowired
    GyreInterpolationRepository gyreInterpolationRepository;

    @Autowired
    PumpInstructionRepository pumpInstructionRepository;

    @Autowired
    private AlertService alertService;

    private int topoffCount;


    public PumpLevelService() {

        listeners.add(new CommandListener() {

            @Override
            public boolean matches(StandardLine line) {
                return line.instructionID == PUMPLEVELPINGID;
            }


            @Override
            protected void processData(StandardLine line) {
                float temp = 0;
                temp = SerialConversionUtil.getFloat(line.raw, 5);
                if (0<= temp && temp <= 40)
                    leftLevel = temp;

                temp = SerialConversionUtil.getFloat(line.raw, 9);
                if (0<= temp && temp <= 40)
                    rightLevel = temp;

                leftFloat1 = SerialConversionUtil.getFloat(line.raw, 13);
                leftFloat2  = SerialConversionUtil.getFloat(line.raw, 17);
                rightFloat1 = SerialConversionUtil.getFloat(line.raw, 21);
                rightFloat2  = SerialConversionUtil.getFloat(line.raw, 25);

                totalDepth = rightLevel + leftLevel;
                depthFiveMin.addValue(totalDepth);
                if ((System.currentTimeMillis()/1000%10==1)) {
                    if (totalDepth > 12 && leftFloat1 == 1) {
                        topoffOn();
                        System.out.println("Topping Off");
                        topoffCount++;
                    } else {
                        topoffCount = 0;
                    }
                }
            }
        });

        listeners.add(new CommandListener() {

            @Override
            public boolean matches(StandardLine line) {
                return line.instructionID == CONNECTIONPINGID;
            }


            @Override
            protected void processData(StandardLine line) {
                leftLevelLoss = SerialConversionUtil.getU8Int(line.raw, 5);
                rightLevelLoss = SerialConversionUtil.getU8Int(line.raw, 6);
                pumpControlLoss = SerialConversionUtil.getU8Int(line.raw, 7);
            }
        });

        listeners.add(new CommandListener() {

            @Override
            public boolean matches(StandardLine line) {
                return line.instructionID == POWERHEADPINGID;
            }


            @Override
            protected void processData(StandardLine line) {
                int temp = SerialConversionUtil.getU8Int(line.raw, 5);
                if (temp != 0) {
                    leftHeadOn = true;
                    leftHeadChangeTime = new Date();
                    System.out.println("Left on!!!!!!!!!!!!!!!!");
                }
                temp = SerialConversionUtil.getU8Int(line.raw, 6);
                if (temp != 0) {
                    leftHeadOn = false;
                    leftHeadChangeTime = new Date();
                    System.out.println("Left off!!!!!!!!!!!!!!!");
                }
                temp = SerialConversionUtil.getU8Int(line.raw, 7);
                if (temp != 0) {
                    System.out.println("Right on!!!!!!!!!!!!!!!!");
                    rightHeadOn = true;
                    rightHeadChangeTime = new Date();
                }
                temp = SerialConversionUtil.getU8Int(line.raw, 8);
                if (temp != 0) {
                    System.out.println("Right off!!!!!!!!!!!!!!!!");
                    rightHeadOn = false;
                    rightHeadChangeTime = new Date();
                }
            }
        });

        listeners.add(new CommandListener() {

            @Override
            public boolean matches(StandardLine line) {
                return line.instructionID == PUMPPOWERPINGID;
            }


            @Override
            protected void processData(StandardLine line) {
                leftPower = SerialConversionUtil.getFloat(line.raw, 5);
                rightPower = SerialConversionUtil.getFloat(line.raw, 9);
                topoffPower = SerialConversionUtil.getFloat(line.raw, 13);
            }
        });
    }


    @Override
    public void restartEmbedded() {
        // Clearing stats

        depthFiveMin.clear();


        //Set water interp levels
        System.out.println("Loading level interp curves ");
        Iterable<LevelInterpolation> levelIter = levelInterpolationRepository.findAll();
        levelIter.forEach(interp -> {
            ImmediateInstruction inst;
            inst = LevelInterpUtility.createInterpInstruction(interp.getSide(), new HashMap<>(interp.getLevels()));
            bridge.writeInstruction(inst);
        });
        System.out.println("Loading gyre interp curves ");
        GyreInterpolation gyre;// = gyreInterpolationRepository.findOne("pump");

//        bridge.writeInstruction(LevelInterpUtility.createGyreInterpInstruction(4, gyre.getLevels(), InterpType.LINEAR));

        gyre = gyreInterpolationRepository.findOne("head");
//
        bridge.writeInstruction(LevelInterpUtility.createGyreInterpInstruction(5, gyre.getLevels(), InterpType.STEP));

        bridge.writeKill(PUMPLEVELPINGID);
        bridge.writeKill(PUMPPOWERPINGID);
        bridge.writeKill(PUMPINSTRUCTIONID);
        bridge.writeKill(CONNECTIONPINGID);
//
        for (ImmediateInstruction instruction : this.setupPWM) {
            bridge.writeInstruction(instruction);
        }

        PumpInstruction inst = pumpInstructionRepository.findOne(OFFINST);
        bridge.writeDirect(inst.getValue());

        inst = pumpInstructionRepository.findOne(LEVELSINST);
        bridge.writeDirect(inst.getValue());

        inst = pumpInstructionRepository.findOne(DECIDEINST);
        bridge.writeDirect(inst.getValue());

        inst = pumpInstructionRepository.findOne(PUMPSETINST);
        bridge.writeDirect(inst.getValue());

        inst = pumpInstructionRepository.findOne(RUNTINST);
        bridge.writeDirect(inst.getValue());

        inst = pumpInstructionRepository.findOne(HEADINST);
        bridge.writeDirect(inst.getValue());

        bridge.writeInstruction(levelRead);
        bridge.writeInstruction(pumpRead);
        bridge.writeInstruction(powerHeadRead);
        bridge.writeInstruction(connectionQualityRead);


        System.out.println("Pump Service Restarted!");
    }

    @Override
    public List<CommandListener> getListeners() {
        return listeners;
    }


    public void start() {
        if (alertService == null) {
            throw new NullPointerException();
        } else {

            Alert topoffAlert = new Alert() {

                @Override
                public Trigger getTimingTrigger() {
                    return new PeriodicTrigger(1, TimeUnit.MINUTES);
                }

                @Override
                public void validate() {

                }

                @Override
                public String getDescription() {
                    return "Top off tank has water remaining";
                }

                @Override
                public String getName() {
                    return "Top off level";
                }

                @Override
                public AlertStatus getStatus() {
                    if ((topoffCount>20) || leftFloat1 == 0 || totalDepth > 14) { // TODO include float levels in this alert
                        return AlertStatus.Alerting;
                    } else {
                        return AlertStatus.Safe;
                    }
                }

                @Override
                public String getStatusDetails() {
                    return "Depth is "+totalDepth + " top off count is " + topoffCount +" and top float is "+((leftFloat2==1)?"up":"down")+", bottom is "+((leftFloat1==1)?"up":"down");
                }

                @Override
                public int getMinimumNotifyDurationMS() {
                    return 1000 * 60 * 60;
                }

                @Override
                public List<NotifyAction> getNotifyActions() {
                    List<NotifyAction> list = new ArrayList<>();
                    list.add(new NotifyAction() {
                        @Override
                        public void alert(Alert parent) {
                            System.out.println("Top off Alert:" + getStatusDetails());
                            SMSSender.sendSMS("Top off Alert:" + getStatusDetails());
                        }

                        @Override
                        public void endAlert(Alert parent) {
                            SMSSender.sendSMS("Top off OK:" + getStatusDetails());
                        }

                        @Override
                        public void critical(Alert parent) {
                        }
                    });

                    return list;
                }
            };

            Alert floodAlert = new Alert() {

                @Override
                public Trigger getTimingTrigger() {
                    return new PeriodicTrigger(1, TimeUnit.MINUTES);
                }

                @Override
                public void validate() {

                }

                @Override
                public String getDescription() {
                    return "Warn if top or bottom tank are over full using floats.";
                }

                @Override
                public String getName() {
                    return "Flood check";
                }

                @Override
                public AlertStatus getStatus() {
                    if ((rightFloat1==1) || rightFloat2 == 1) { // TODO include float levels in this alert
                        return AlertStatus.Alerting;
                    } else {
                        return AlertStatus.Safe;
                    }
                }

                @Override
                public String getStatusDetails() {
                    return "Tank full float was tripped : "+(rightFloat1==1?"Top ":" ")+(rightFloat2==1?"Bottom":" ");
                }

                @Override
                public int getMinimumNotifyDurationMS() {
                    return 1000 * 60 * 10;
                }

                @Override
                public List<NotifyAction> getNotifyActions() {
                    List<NotifyAction> list = new ArrayList<>();
                    list.add(new NotifyAction() {
                        @Override
                        public void alert(Alert parent) {
                            System.out.println("TANK FLOOD Alert:" + getStatusDetails());
                            SMSSender.sendSMS("TANK FLOOD Alert:" + getStatusDetails());
                        }

                        @Override
                        public void endAlert(Alert parent) {
                            SMSSender.sendSMS("TANK FLOOD OK.  No floats tripped");
                        }

                        @Override
                        public void critical(Alert parent) {
                        }
                    });

                    return list;
                }
            };




            Alert pumpOnAlert = new Alert() {


                private int zeroPumpCounter = 0;

                @Override
                public Trigger getTimingTrigger() {
                    return new PeriodicTrigger(1, TimeUnit.MINUTES);
                }

                @Override
                public void validate() {

                    if (leftPower == 0 || rightPower == 0) {
                        zeroPumpCounter++;
                    } else {
                        zeroPumpCounter = 0;
                    }

                }

                @Override
                public String getDescription() {
                    return "Pumps levels are not zero for more than 10 minutes";
                }

                @Override
                public String getName() {
                    return "Pumps not zero";
                }

                @Override
                public AlertStatus getStatus() {
                    return zeroPumpCounter>=10?AlertStatus.Alerting : AlertStatus.Safe;
                }

                @Override
                public String getStatusDetails() {
                    return "Pumps off for  " + zeroPumpCounter + " minutes";
                }

                @Override
                public int getMinimumNotifyDurationMS() {
                    return 1000 * 60 * 60;
                }

                @Override
                public List<NotifyAction> getNotifyActions() {
                    List<NotifyAction> list = new ArrayList<>();
                    list.add(new NotifyAction() {
                        @Override
                        public void alert(Alert parent) {
                            System.out.println("Pumps off for " + zeroPumpCounter + " minutes. Attempting to turn on...");
                            SMSSender.sendSMS("Pumps off for " + zeroPumpCounter + " minutes.  Attempting to turn on...");
                            allPumpsOn();
                        }

                        @Override
                        public void endAlert(Alert parent) {
                            SMSSender.sendSMS("Pumps back online");
                        }

                        @Override
                        public void critical(Alert parent) {
                        }
                    });

                    return list;
                }
            };
            alertService.loadAlert(topoffAlert);
            alertService.loadAlert(pumpOnAlert);
            alertService.loadAlert(floodAlert);
        }




        PumpInstruction inst = pumpInstructionRepository.findOne(OFFINST);
        if (inst == null || inst.getValue() == "")

        {
            inst = new PumpInstruction(OFFINST);
            inst.setValue("IC 2 C FUN P9 w[$BP:AAA=#B0],w[$BP:BBB=#B0],w[$BP:CCC=#B0],w[$BP:DDD={{t/#T200}%#T2}],w[$BP:EEE=#B0],w[$BP:FFF={{{t/#T200}+#T1}%#T2}],w[$BP:GGG=#B0],w[$BP:HHH=#B0],w[$BP:III=#B0],w[$BP:JJJ=#B0]");
            pumpInstructionRepository.save(inst);
        }

        inst = pumpInstructionRepository.findOne(LEVELSINST);
        if (inst == null || inst.getValue() == "")

        {
            inst = new PumpInstruction(LEVELSINST);
            inst.setValue("IC 2 C FUN P2 w[$FP:VRA=i4[{t%#T300000}]],w[$FP:VRB=i4[{#T300000-{t%#T300000}}]],w[$FP:VRC=i5[{{t/#T5}%#T80000}]],w[$FW:VRA=i0[$UW:AAA]],w[$FW:VRB=i1[$UW:BBB]],w[$FW:VRC=i2[$UW:CCC]]");
            pumpInstructionRepository.save(inst);
        }

        inst = pumpInstructionRepository.findOne(DECIDEINST);
        if (inst == null || inst.getValue() == "")

        {
            inst = new PumpInstruction(DECIDEINST);
            inst.setValue("IC 2 C FUN P1 $BC:FNP.2,w[$BP:DDD=?{$FP:VRC==#B2}[#B1,#B0]],w[$BP:FFF=?{$FP:VRC==#B4}[#B1,#B0]],?{$BP:VRX==#B0}[$BC:FNP.3,$BC:FNP.9]");
            pumpInstructionRepository.save(inst);
        }

        inst = pumpInstructionRepository.findOne(PUMPSETINST);
        if (inst == null || inst.getValue() == "")

        {
            inst = new PumpInstruction(PUMPSETINST);
            inst.setValue("IC 2 C FUN P3 w[$BP:CCC=?{$FP:VRC==#B1}[#B1,#B0]],w[$BP:EEE=?{$FP:VRC==#B3}[#B1,#B0]],w[$BP:AAA={$FP:VRA*{#F24+{{$FW:VRB-$FW:VRA}*#F10}}}],w[$BP:BBB={$FP:VRB*{#F20+{{$FW:VRA-$FW:VRB}*#F10}}}]");
            pumpInstructionRepository.save(inst);
        }

        inst = pumpInstructionRepository.findOne(RUNTINST);
        if (inst == null || inst.getValue() == "")

        {
            inst = new PumpInstruction(RUNTINST);
            inst.setValue("SC " + PUMPINSTRUCTIONID + "C 1000 100 0 EXE P1");
            pumpInstructionRepository.save(inst);
        }

    }


    public void stop() {

    }

    public PumpLevels getLevels() {
        PumpLevels levels = new PumpLevels(new float[]{leftLevel, rightLevel, leftFloat1,leftFloat2,rightFloat1,rightFloat2}, new float[]{leftPower, rightPower, topoffPower}, new float[]{0,0}, totalDepth, (float) depthFiveMin.getMean(),topoffCount,
                new String[]{leftHeadOn ? "On" : "Off", rightHeadOn ? "On" : "Off"}, new Date[]{leftHeadChangeTime, rightHeadChangeTime},new int[]{leftLevelLoss,rightLevelLoss,pumpControlLoss});
        return levels;
    }


    public void allPumpsOff() {
        bridge.writeInstruction(allPumpsOff);
        bridge.writeInstruction(allPumpsOffEnd);
    }

    public void allPumpsOn() {
        bridge.writeInstruction(allPumpsOn);
    }

    public void topoffOn() {
        bridge.writeInstruction(topoffOn);
    //    bridge.writeInstruction(topoffOnEnd);
    }

    public void topoffOff() {
        bridge.writeInstruction(topoffOff);
    }
}
