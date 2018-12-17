package me.davehummel.tredserver.fish.services;

import me.davehummel.tredserver.command.*;
import me.davehummel.tredserver.command.math.InterpType;
import me.davehummel.tredserver.fish.history.HistoryService;
import me.davehummel.tredserver.fish.history.ResettingSupplier;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public static final int PUMPINSTRUCTIONID = 17250;
    public static final int CONNECTIONPINGID = 17251;
    private static final int PUMPPOWERPINGID = 17246;
    private static final int PUMPLEVELPINGID = 17248;
    private static final int POWERHEADPINGID = 17249;
    public static final String RUNTINST = "run";
    private static final String OFFINST = "off";
    private static final String LEVELSINST = "levels";
    private static final String DECIDEINST = "gyres";
    private static final String PUMPSETINST = "pumps";
    private static final String HEADINST = "heads";
    private final List<CommandListener> listeners = new ArrayList<>();

    private final ScheduledInstruction levelRead = new ScheduledInstruction(
            'R', PUMPLEVELPINGID, 1000, 1000, 0, new ReadBody(DataType.FLOAT, new String[]{"VRA", "VRB", "ABA", "ACA", "BBA", "BCA"}, 1, 0));
    private final ScheduledInstruction pumpRead = new ScheduledInstruction(
            'R', PUMPPOWERPINGID, 1000, 500, 0, new ReadBody(DataType.FLOAT, new String[]{"CAA", "CBB", "CCC"}, 1, 0));
    private final ScheduledInstruction connectionQualityRead = new ScheduledInstruction(
            'R', CONNECTIONPINGID, 1000, 5000, 0, new ReadBody(DataType.BYTE, new String[]{"AAA", "BBB", "CCC"}, 1, 0));
    private final ScheduledInstruction powerHeadRead = new ScheduledInstruction(
            'P', POWERHEADPINGID, 1000, 499, 0, new ReadBody(DataType.BYTE, new String[]{"CCC", "DDD", "EEE", "FFF"}, 1, 0));


    private final ImmediateInstruction allPumpsOff = new ImmediateInstruction('P', POWERHEADPINGID, new WriteBody(DataType.BYTE, "VRX", 1));
    private final ScheduledInstruction allPumpsOffEnd = new ScheduledInstruction('P', POWERHEADPINGID, 300000, 0, 1, new WriteBody(DataType.BYTE, "VRX", 0));
    private final ImmediateInstruction allPumpsOn = new ImmediateInstruction('P', POWERHEADPINGID, new WriteBody(DataType.BYTE, "VRX", 0));

    private final ImmediateInstruction topoffOn = new ImmediateInstruction('R', POWERHEADPINGID, new WriteBody(DataType.DOUBLE, "CCC", 1));
    //  private final ScheduledInstruction topoffOnEnd = new ScheduledInstruction('R', POWERHEADPINGID, 2000, 0, 1, new WriteBody(DataType.DOUBLE, "CCC", 0));
    private final ImmediateInstruction topoffOff = new ImmediateInstruction('R', POWERHEADPINGID, new WriteBody(DataType.DOUBLE, "CCC", 0));

    private final ImmediateInstruction power3Off = new ImmediateInstruction('P',2,new WriteBody(DataType.BYTE, "GGG", 1));
    private final ImmediateInstruction power3On = new ImmediateInstruction('P',2,new WriteBody(DataType.BYTE, "HHH", 1));
//    private final ScheduledInstruction power3KeepOn = new ScheduledInstruction('P',PUMPPOWERPINGID,100,60000, 0,new WriteBody(DataType.BYTE, "HHH", 1));

    private final ImmediateInstruction[] setupPWM = new ImmediateInstruction[]{
            new ImmediateInstruction('P', 2, new CmdBody("BLK C 14 500")),
            new ImmediateInstruction('P', 2, new CmdBody("BLK D 15 500")),
            new ImmediateInstruction('P', 2, new CmdBody("BLK E 39 500")),
            new ImmediateInstruction('P', 2, new CmdBody("BLK F 38 500")),
            new ImmediateInstruction('P', 2, new CmdBody("BLK G 33 400")),
            new ImmediateInstruction('P', 2, new CmdBody("BLK H 34 400")),
            new ImmediateInstruction('R', 2, new WriteBody(DataType.DOUBLE, "CAA", 120)),
            new ImmediateInstruction('R', 2, new WriteBody(DataType.DOUBLE, "CBB", 120)),
    };

    private final ScheduledInstruction[] powerheadOffOnSet = new ScheduledInstruction[]{
            new ScheduledInstruction('P', PUMPINSTRUCTIONID, 0, 0, 1, new WriteBody(DataType.BYTE, "DDD", 1)),
            new ScheduledInstruction('P', PUMPINSTRUCTIONID, 1000, 0, 1, new WriteBody(DataType.BYTE, "FFF", 1)),
            new ScheduledInstruction('P', PUMPINSTRUCTIONID, 300000, 0, 1, new WriteBody(DataType.BYTE, "CCC", 1)),
            new ScheduledInstruction('P', PUMPINSTRUCTIONID, 310000, 0, 1, new WriteBody(DataType.BYTE, "EEE", 1))
    };

    Logger logger = LoggerFactory.getLogger(PumpLevelService.class);

    @Autowired
    SMSSender smsSender;
    @Autowired
    LevelInterpolationRepository levelInterpolationRepository;
    @Autowired
    GyreInterpolationRepository gyreInterpolationRepository;
    @Autowired
    PumpInstructionRepository pumpInstructionRepository;
    private float leftLevel, rightLevel, leftFloat1, leftFloat2, rightFloat1, rightFloat2;
    private float leftPower, rightPower, topoffPower;
    private boolean leftHeadOn, rightHeadOn;
    private Date leftHeadChangeTime, rightHeadChangeTime;
    private float totalDepth = 0;
    private DescriptiveStatistics depthFiveMin = new DescriptiveStatistics(30);
    private int leftLevelLoss, rightLevelLoss, pumpControlLoss;
    private int topoffStatCount = 0;
    private long topoffDisableTime = 0;

    private float targetDepth = 14;
    @Autowired
    private AlertService alertService;

    @Autowired
    private HistoryService historyService;

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
                if (0 <= temp && temp <= 40)
                    leftLevel = temp;

                temp = SerialConversionUtil.getFloat(line.raw, 9);
                if (0 <= temp && temp <= 40)
                    rightLevel = temp;

                leftFloat1 = SerialConversionUtil.getFloat(line.raw, 13);
                leftFloat2 = SerialConversionUtil.getFloat(line.raw, 17);
                rightFloat1 = SerialConversionUtil.getFloat(line.raw, 21);
                rightFloat2 = SerialConversionUtil.getFloat(line.raw, 25);

                temp = rightLevel + leftLevel;
                if (temp > 0 && temp < 999)
                    totalDepth = temp;
                depthFiveMin.addValue(totalDepth);
                if (topoffDisableTime <= System.currentTimeMillis()) {
                    if ((System.currentTimeMillis() / 1000 % 10 == 1)) {
                        if (totalDepth > targetDepth && totalDepth < 20) {
                            topoffOn();
                            logger.info("Topping Off");
                            topoffCount++;
                        } else {
                            topoffCount = 0;
                        }
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
                    logger.info("Left on!!!!!!!!!!!!!!!!");
                }
                temp = SerialConversionUtil.getU8Int(line.raw, 6);
                if (temp != 0) {
                    leftHeadOn = false;
                    leftHeadChangeTime = new Date();
                    logger.info("Left off!!!!!!!!!!!!!!!");
                }
                temp = SerialConversionUtil.getU8Int(line.raw, 7);
                if (temp != 0) {
                    logger.info("Right on!!!!!!!!!!!!!!!!");
                    rightHeadOn = true;
                    rightHeadChangeTime = new Date();
                }
                temp = SerialConversionUtil.getU8Int(line.raw, 8);
                if (temp != 0) {
                    logger.info("Right off!!!!!!!!!!!!!!!!");
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
        logger.info("Loading level interp curves ");
        Iterable<LevelInterpolation> levelIter = levelInterpolationRepository.findAll();
        levelIter.forEach(interp -> {
            ImmediateInstruction inst;
            inst = LevelInterpUtility.createInterpInstruction(interp.getSide(), new HashMap<>(interp.getLevels()));
            bridge.writeInstruction(inst);
        });
        logger.info("Loading gyre interp curves ");


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

        for (ScheduledInstruction instruction : this.powerheadOffOnSet) {
            bridge.writeInstruction(instruction);
        }

        logger.info("Pump Service Restarted!");
    }

    @Override
    public List<CommandListener> getListeners() {
        return listeners;
    }


    public void start() {

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
                if ((topoffCount > 20) || totalDepth > 18) { // TODO include float levels in this alert
                    return AlertStatus.Alerting;
                } else {
                    return AlertStatus.Safe;
                }
            }

            @Override
            public String getStatusDetails() {
                return "Depth is " + totalDepth + " top off count is " + topoffCount + " and top float is " + ((leftFloat2 == 1) ? "up" : "down") + ", bottom is " + ((leftFloat1 == 1) ? "up" : "down");
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
                        logger.info("Top off Alert:" + getStatusDetails());
                        smsSender.sendSMS("Top off Alert:" + getStatusDetails());
                    }

                    @Override
                    public void endAlert(Alert parent) {
                        smsSender.sendSMS("Top off OK:" + getStatusDetails());
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
                if ((rightFloat1 == 1) || rightFloat2 == 1) { // TODO include float levels in this alert
                    return AlertStatus.Alerting;
                } else {
                    return AlertStatus.Safe;
                }
            }

            @Override
            public String getStatusDetails() {
                return "Tank full float was tripped : " + (rightFloat1 == 1 ? "Top " : " ") + (rightFloat2 == 1 ? "Bottom" : " ");
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
                        smsSender.sendSMS("TANK FLOOD Alert:" + getStatusDetails());
                    }

                    @Override
                    public void endAlert(Alert parent) {
                        smsSender.sendSMS("TANK FLOOD OK.  No floats tripped");
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
                return zeroPumpCounter >= 10 ? AlertStatus.Alerting : AlertStatus.Safe;
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
                        logger.info("Pumps off for " + zeroPumpCounter + " minutes. Attempting to turn on...");
                        smsSender.sendSMS("Pumps off for " + zeroPumpCounter + " minutes.  Attempting to turn on...");
                        allPumpsOn();
                        restartRadioDevice();
                    }

                    @Override
                    public void endAlert(Alert parent) {
                        smsSender.sendSMS("Pumps back online");
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


        PumpInstruction inst = pumpInstructionRepository.findOne(OFFINST);
        if (inst == null || inst.getValue() == "")

        {
            inst = new PumpInstruction(OFFINST);
            inst.setValue("IC 2 C FUN P1 ?{$BP:VRX==#B0}[$FC:FNP.3,$FC:FNP.9]");
            pumpInstructionRepository.save(inst);
        }

        inst = pumpInstructionRepository.findOne(LEVELSINST);
        if (inst == null || inst.getValue() == "")

        {
            inst = new PumpInstruction(LEVELSINST);
            inst.setValue("IC 2 C FUN P2 w[$FR:VRA=i0[$FR:AAA]],w[$FR:VRB=i1[$FR:BAA]],w[$FR:VRO={{$FR:VRO*#F0.95}+{$FR:VRA-$FR:VRB}}]");
            pumpInstructionRepository.save(inst);
        }

        inst = pumpInstructionRepository.findOne(DECIDEINST);
        if (inst == null || inst.getValue() == "")

        {
            inst = new PumpInstruction(DECIDEINST);
            inst.setValue("IC 2 C FUN P9 w[$DR:CAA=#F0],w[$DR:CBB=#F0]");
            pumpInstructionRepository.save(inst);
        }

        inst = pumpInstructionRepository.findOne(PUMPSETINST);
        if (inst == null || inst.getValue() == "")

        {
            inst = new PumpInstruction(PUMPSETINST);
            inst.setValue("IC 2 C FUN P3 w[$DR:CAA={#F125-$FR:VRO}],w[$DR:CBB={#F125+$FR:VRO}]");
            pumpInstructionRepository.save(inst);
        }

        inst = pumpInstructionRepository.findOne(RUNTINST);
        if (inst == null || inst.getValue() == "")

        {
            inst = new PumpInstruction(RUNTINST);
            inst.setValue("SC " + PUMPINSTRUCTIONID + "SC 17250 C 1000 1000 0 EXE P2 P1");
            pumpInstructionRepository.save(inst);
        }

        historyService.addSupplier("Left Pump Level", new ResettingSupplier() {
            @Override
            public void resetState() {

            }

            @Override
            public Double get() {
                return (double) leftLevel;
            }
        });

        historyService.addSupplier("Right Pump Level", new ResettingSupplier() {
            @Override
            public void resetState() {

            }

            @Override
            public Double get() {
                return (double) rightLevel;
            }
        });

        historyService.addSupplier("Left Pump Power", new ResettingSupplier() {
            @Override
            public void resetState() {

            }

            @Override
            public Double get() {
                return (double) leftPower;
            }
        });

        historyService.addSupplier("Right Pump Power", new ResettingSupplier() {
            @Override
            public void resetState() {

            }

            @Override
            public Double get() {
                return (double) rightPower;
            }
        });

        historyService.addSupplier("Topoff Count", new ResettingSupplier() {
            @Override
            public void resetState() {
                topoffStatCount = 0;
            }

            @Override
            public Double get() {
                return (double) topoffStatCount;
            }
        });
    }


    public void stop() {

    }

    public PumpLevels getLevels() {
        PumpLevels levels = new PumpLevels(new float[]{leftLevel, rightLevel, leftFloat1, leftFloat2, rightFloat1, rightFloat2},
                new float[]{leftPower, rightPower, topoffPower}, new float[]{0, 0}, totalDepth,
                (float) depthFiveMin.getMean(), topoffStatCount, new Date(topoffDisableTime), new String[]{leftHeadOn ? "On" : "Off",
                rightHeadOn ? "On" : "Off"}, new Date[]{leftHeadChangeTime, rightHeadChangeTime}, new int[]{leftLevelLoss, rightLevelLoss, pumpControlLoss});
        return levels;
    }


    public void allPumpsOff() {
        bridge.writeInstruction(allPumpsOff);
        bridge.writeInstruction(allPumpsOffEnd);
        for (ScheduledInstruction instruction:powerheadOffOnSet){
            bridge.writeInstruction(instruction);
        }
    }

    public void allPumpsOn() {
        bridge.writeInstruction(allPumpsOn);
    }

    public void topoffOn() {
        bridge.writeInstruction(topoffOn);
        topoffStatCount++;
    }

    public void topoffOff() {
        bridge.writeInstruction(topoffOff);
    }

    public void restartRadioDevice(){
        bridge.writeInstruction(power3Off);
        try {
            Thread.sleep(1800);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        bridge.writeInstruction(power3On);
    }

    public void disableTopOff(long disableDurationMS) {
        this.topoffDisableTime = System.currentTimeMillis() + disableDurationMS;
    }

    public float getTargetDepth() {
        return targetDepth;
    }

    public void setTargetDepth(float targetDepth) {
        this.targetDepth = targetDepth;
    }
}
