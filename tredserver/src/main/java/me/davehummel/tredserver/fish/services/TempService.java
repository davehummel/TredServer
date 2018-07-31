package me.davehummel.tredserver.fish.services;

import me.davehummel.tredserver.command.*;
import me.davehummel.tredserver.fish.history.HistoryService;
import me.davehummel.tredserver.fish.history.ResettingSupplier;
import me.davehummel.tredserver.fish.temperature.readonly.TemperatureReadings;
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
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by dmhum_000 on 9/20/2015.
 */

@Controller
public class TempService extends CommandService {

    private static final int TEMPINSTRUCTIONID = 11620;

    private final List<CommandListener> listeners = new ArrayList<>();

    private final ScheduledInstruction levelRead = new ScheduledInstruction(
            'T', TEMPINSTRUCTIONID, 1000, 10000, 0, new ReadBody(DataType.FLOAT, new String[]{"AAA", "BBB", "CCC"}, 1, 0));


    private final ImmediateInstruction[] setupOffset = new ImmediateInstruction[]{
            new ImmediateInstruction('T', 2, new CmdBody("Enable")),
    };


    private double topTemp, bottomTemp, outTemp;

    private DescriptiveStatistics topTenMinutes = new DescriptiveStatistics(60), bottomTenMinutes = new DescriptiveStatistics(60), outTenMinutes = new DescriptiveStatistics(60);

    @Autowired
    private AlertService alertService;

    @Autowired
    private SMSSender smsSender;

    @Autowired
    private HistoryService historyService;


    public TempService() {

        listeners.add(new CommandListener() {

            @Override
            public boolean matches(StandardLine line) {
                return line.instructionID == TEMPINSTRUCTIONID;
            }


            @Override
            protected void processData(StandardLine line) {
                float temp = SerialConversionUtil.getFloat(line.raw, 5);
                if (temp > 75 && temp < 100) {
                    topTemp = temp;
                    topTenMinutes.addValue(topTemp);
                }

                temp = SerialConversionUtil.getFloat(line.raw, 9);
                if (temp > 75 && temp < 100) {
                    bottomTemp = temp;
                    bottomTenMinutes.addValue(bottomTemp);
                }
                temp = SerialConversionUtil.getFloat(line.raw, 13);
                if (temp > 32 && temp < 100) {
                    outTemp = temp;
                    outTenMinutes.addValue(outTemp);
                }
            }
        });
    }


    @Override
    public void restartEmbedded() {
        // Clearing stats

        bridge.writeKill(TEMPINSTRUCTIONID);


        for (ImmediateInstruction instruction : this.setupOffset) {
            bridge.writeInstruction(instruction);
        }

        bridge.writeInstruction(levelRead);

        System.out.println("Temp Service Restarted!");
    }

    @Override
    public List<CommandListener> getListeners() {
        return listeners;
    }


    public void start() {

        historyService.addSupplier("Top Temperature", new ResettingSupplier() {
            @Override
            public void resetState() {

            }

            @Override
            public Double get() {
                return topTemp;
            }
        });

        historyService.addSupplier("Bottom Temperature", new ResettingSupplier() {
            @Override
            public void resetState() {

            }

            @Override
            public Double get() {
                return bottomTemp;
            }
        });

        historyService.addSupplier("Outside Temperature", new ResettingSupplier() {
            @Override
            public void resetState() {

            }

            @Override
            public Double get() {
                return outTemp;
            }
        });

        Alert alert = new Alert() {

            private AlertStatus status = AlertStatus.Safe;

            private int alertCountdown = 0;

            @Override
            public Trigger getTimingTrigger() {
                return new PeriodicTrigger(1, TimeUnit.MINUTES);
            }

            @Override
            public void validate() {
                status = AlertStatus.Safe;

                if (topTemp > 81) {
                    status = AlertStatus.Critical;
                } else if (topTemp < 78) {
                    status = AlertStatus.Critical;
//                } else if (bottomTemp > 82) {
//                    status = AlertStatus.Critical;
                } else if (bottomTemp < 78) {
                    status = AlertStatus.Critical;
                }


                if (topTemp > 81.8) {
                    status = AlertStatus.Alerting;
                } else if (topTemp < 77) {
                    status = AlertStatus.Alerting;
                } else if (bottomTemp > 82.5) {
                    status = AlertStatus.Alerting;
                } else if (bottomTemp < 77) {
                    status = AlertStatus.Alerting;
                }
            }

            @Override
            public String getDescription() {
                return "The temperature is within bounds";
            }

            @Override
            public String getName() {
                return "Temperature";
            }

            @Override
            public AlertStatus getStatus() {
                if (status == AlertStatus.Alerting) {
                    alertCountdown = 30;
                    return AlertStatus.Alerting;
                } else {
                    if (alertCountdown > 0) {
                        alertCountdown--;
                        return AlertStatus.Alerting;
                    }
                }

                if (status == AlertStatus.Critical) {
                    return AlertStatus.Critical;
                }
                return AlertStatus.Safe;
            }

            @Override
            public String getStatusDetails() {
                return "The temperature is (top) " + topTemp + " (bot) " + bottomTemp + " (out) " + outTemp;
            }

            @Override
            public int getMinimumNotifyDurationMS() {
                return 1000 * 60 * 90;
            }

            @Override
            public List<NotifyAction> getNotifyActions() {
                List<NotifyAction> list = new ArrayList<>();
                list.add(new NotifyAction() {
                    @Override
                    public void alert(Alert parent) {
                        System.out.println("Temperature Alert:" + getStatusDetails());
                        smsSender.sendSMS("Temperature Alert:" + getStatusDetails());
                    }

                    @Override
                    public void endAlert(Alert parent) {
                        System.out.println("Temperature OK:" + getStatusDetails());
                        smsSender.sendSMS("Temperature OK:" + getStatusDetails());
                    }

                    @Override
                    public void critical(Alert parent) {
                    }
                });

                return list;
            }
        };

        topTemp = 79.15f;
        bottomTemp = 79.111f;
        outTemp = 72.111f;

        alertService.loadAlert(alert);

    }


    public void stop() {

    }

    public TemperatureReadings getReadings() {
        TemperatureReadings readings = new TemperatureReadings((float)topTemp, (float)bottomTemp, (float)outTemp, (float) topTenMinutes.getMean(), (float) bottomTenMinutes.getMean(), (float) outTenMinutes.getMean());
        return readings;
    }
}
