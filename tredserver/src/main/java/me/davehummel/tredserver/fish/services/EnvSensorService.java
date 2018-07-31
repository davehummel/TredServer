package me.davehummel.tredserver.fish.services;

import me.davehummel.tredserver.command.*;
import me.davehummel.tredserver.fish.environment.readonly.EnvironmentReadings;
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
public class EnvSensorService extends CommandService {

    private static final int ENVINSTRUCTIONID = 28900;
    private static final int ENVPINGID = 28901;

    private final List<CommandListener> listeners = new ArrayList<>();

    private final ScheduledInstruction levelRead = new ScheduledInstruction(
            'R', ENVINSTRUCTIONID, 1000, 10000, 0, new ReadBody(DataType.FLOAT, new String[]{"DAA", "DBB", "DCC" , "DDD", "DEE", "DFF" , "DGG", "DHH","DII"}, 1, 0));


    private final ScheduledInstruction connectionQualityRead = new ScheduledInstruction(
            'R', ENVPINGID, 1000, 5000, 0, new ReadBody(DataType.BYTE, new String[]{"DDD"}, 1, 0));


    private double ph, orp, salinity, disolvedO, co2, tvoc, pressure,humidity,temperature;

    int radioLoss = 0;

    private DescriptiveStatistics phTenMinutes = new DescriptiveStatistics(60), orpTenMinutes = new DescriptiveStatistics(60), salinityTenMinutes = new DescriptiveStatistics(60), disolvedOTenMinutes = new DescriptiveStatistics(60);
    private DescriptiveStatistics co2TenMinutes = new DescriptiveStatistics(60), tvocTenMinutes = new DescriptiveStatistics(60), pressureTenMinutes = new DescriptiveStatistics(60), humidityTenMinutes = new DescriptiveStatistics(60), tempTenMinutes = new DescriptiveStatistics(60);

    @Autowired
    private AlertService alertService;

    @Autowired
    private SMSSender smsSender;

    @Autowired
    private HistoryService historyService;


    public EnvSensorService() {

        listeners.add(new CommandListener() {

            @Override
            public boolean matches(StandardLine line) {
                return line.instructionID == ENVINSTRUCTIONID;
            }


            @Override
            protected void processData(StandardLine line) {
                double temp = SerialConversionUtil.getFloat(line.raw, 5);
                ph = temp;
                phTenMinutes.addValue(ph);

                temp = SerialConversionUtil.getFloat(line.raw, 9);
                salinity = temp;
                salinityTenMinutes.addValue(salinity);

                temp = SerialConversionUtil.getFloat(line.raw, 13);
                orp = temp;
                orpTenMinutes.addValue(orp);

                temp = SerialConversionUtil.getFloat(line.raw, 17);
                disolvedO = temp;
                disolvedOTenMinutes.addValue(disolvedO);

                temp = SerialConversionUtil.getFloat(line.raw, 21);
                co2 = temp;
                co2TenMinutes.addValue(co2);

                temp = SerialConversionUtil.getFloat(line.raw, 25);
                tvoc = temp;
                tvocTenMinutes.addValue(tvoc);

                temp = SerialConversionUtil.getFloat(line.raw, 29);
                pressure = temp;
                pressureTenMinutes.addValue(pressure);

                temp = SerialConversionUtil.getFloat(line.raw, 33);
                humidity = temp;
                humidityTenMinutes.addValue(humidity);

                temp = SerialConversionUtil.getFloat(line.raw, 37);
                temperature = temp;
                tempTenMinutes.addValue(temperature);
            }
        });
        listeners.add(new CommandListener() {

            @Override
            public boolean matches(StandardLine line) {
                return line.instructionID == ENVPINGID;
            }


            @Override
            protected void processData(StandardLine line) {
                radioLoss = SerialConversionUtil.getU8Int(line.raw, 5);

            }
        });
    }




    @Override
    public void restartEmbedded() {
        // Clearing stats

        bridge.writeKill(ENVINSTRUCTIONID);
        bridge.writeKill(ENVPINGID);


        bridge.writeInstruction(levelRead);

        bridge.writeInstruction(connectionQualityRead);

        System.out.println("Temp Service Restarted!");
    }

    @Override
    public List<CommandListener> getListeners() {
        return listeners;
    }


    public void start() {

        historyService.addSupplier("PH", new ResettingSupplier() {
            @Override
            public void resetState() {

            }

            @Override
            public Double get() {
                return phTenMinutes.getMean();
            }
        });

        historyService.addSupplier("ORP", new ResettingSupplier() {
            @Override
            public void resetState() {

            }

            @Override
            public Double get() {
                return orpTenMinutes.getMean();
            }
        });

        historyService.addSupplier("Salinity", new ResettingSupplier() {
            @Override
            public void resetState() {

            }

            @Override
            public Double get() {
                return salinityTenMinutes.getMean();
            }
        });

        historyService.addSupplier("DO", new ResettingSupplier() {
            @Override
            public void resetState() {

            }

            @Override
            public Double get() {
                return disolvedOTenMinutes.getMean();
            }
        });

        historyService.addSupplier("CO2", new ResettingSupplier() {
            @Override
            public void resetState() {

            }

            @Override
            public Double get() {
                return co2TenMinutes.getMean();
            }
        });

        historyService.addSupplier("TVOC", new ResettingSupplier() {
            @Override
            public void resetState() {

            }

            @Override
            public Double get() {
                return tvocTenMinutes.getMean();
            }
        });

        historyService.addSupplier("Pressure", new ResettingSupplier() {
            @Override
            public void resetState() {

            }

            @Override
            public Double get() {
                return pressureTenMinutes.getMean();
            }
        });

        historyService.addSupplier("Humidity", new ResettingSupplier() {
            @Override
            public void resetState() {

            }

            @Override
            public Double get() {
                return humidityTenMinutes.getMean();
            }
        });

        historyService.addSupplier("EnvTemp", new ResettingSupplier() {
            @Override
            public void resetState() {

            }

            @Override
            public Double get() {
                return tempTenMinutes.getMean();
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

            }

            @Override
            public String getDescription() {
                return "The PH is within bounds";
            }

            @Override
            public String getName() {
                return "PH";
            }

            @Override
            public AlertStatus getStatus() {
                status = AlertStatus.Safe;

                if (ph > 8.4) {
                    status = AlertStatus.Alerting;
                } else if (ph < 7.7) {
                    status = AlertStatus.Alerting;
                }

                return status;
            }

            @Override
            public String getStatusDetails() {
                return "The PH level is "+ph+". Ten minute average is:"+phTenMinutes.getMean();
            }

            @Override
            public int getMinimumNotifyDurationMS() {
                return 1000 * 60 * 90;
            }

            @Override
            public List<NotifyAction> getNotifyActions() {
                List<NotifyAction> list = new ArrayList<>();
//                list.add(new NotifyAction() {
//                    @Override
//                    public void alert(Alert parent) {
//                        System.out.println("PH Alert:" + getStatusDetails());
//                        smsSender.sendSMS("PH Alert:" + getStatusDetails());
//                    }
//
//                    @Override
//                    public void endAlert(Alert parent) {
//                        System.out.println("PH OK:" + getStatusDetails());
//                        smsSender.sendSMS("PH OK:" + getStatusDetails());
//                    }
//
//                    @Override
//                    public void critical(Alert parent) {
//                    }
//                });

                return list;
            }
        };

        ph = 8;
        phTenMinutes.addValue(8);

      //  alertService.loadAlert(alert);

    }


    public void stop() {

    }

    public EnvironmentReadings getReadings() {
        EnvironmentReadings readings = new EnvironmentReadings(ph,orp,salinity,disolvedO,co2,tvoc,pressure,humidity,temperature, radioLoss);
        return readings;
    }
}
