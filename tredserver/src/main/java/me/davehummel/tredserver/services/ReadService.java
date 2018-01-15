package me.davehummel.tredserver.services;

import me.davehummel.tredserver.command.*;
import me.davehummel.tredserver.serial.SerialConversionUtil;
import me.davehummel.tredserver.serial.StandardLine;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dmhum_000 on 2/7/2017.
 */
@Service
public class ReadService extends CommandService{

    public static final long INST_ID_START = 71876;

    private final List<CommandListener> listeners = new ArrayList<>();

    private ReadValue value = null;
    private boolean success = false;

    public ReadService() {
        listeners.add(new CommandListener() {
            @Override
            public boolean matches(StandardLine line) {
                return line.instructionID == INST_ID_START;
            }

            @Override
            protected void processData(StandardLine data) {
                if (value==null)
                    return;
                synchronized (value){
                    value.setValue(data);
                    success = true;
                    value.notify();
                }
            }
        });
    }

    @Override
    public List<CommandListener> getListeners() {
        return listeners;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    public ReadValue getValueNow(DataType type, Character component, String address,int timeout ){
        return getValueNow(type,component,address,0,1,timeout);
    }

    synchronized public ReadValue getValueNow(DataType type, Character module, String address,int offset, int length,int timeout ){
        value = new ReadValue(type);
        synchronized (value){
            success = false;
            ReadBody function = new ReadBody(type,address);
            ImmediateInstruction instruction = new ImmediateInstruction(module, INST_ID_START, function);
            bridge.writeInstruction(instruction);
            try {
                value.wait(timeout);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (success == false)
                return null;
        }

        return value;
    }

    public class ReadValue {
        public DataType getType() {
            return type;
        }

        private final DataType type;

        public Long getLongValue() {
            return longValue;
        }

        private Long longValue;

        public Double getDoubleValue() {
            return doubleValue;
        }


        private Double doubleValue;

        public ReadValue(DataType type) {
            this.type = type;
        }

        public void setValue(StandardLine value) {
            switch (type){
                case BYTE:
                     longValue = (long)SerialConversionUtil.getU8Int(value.raw,5);
                     doubleValue = doubleValue.doubleValue();
                     return;
                case UINT_16:
                    longValue = (long)SerialConversionUtil.getU16Int(value.raw,5);
                    doubleValue = doubleValue.doubleValue();
                    return;
                case INT_16:
                    longValue = (long)SerialConversionUtil.get16Int(value.raw,5);
                    doubleValue = doubleValue.doubleValue();
                    return;
                case INT_32:
                    longValue = (long)SerialConversionUtil.get32Int(value.raw,5);
                    doubleValue = doubleValue.doubleValue();
                    return;
                case UINT_32:
                    longValue = (long)SerialConversionUtil.getU32Int(value.raw,5);
                    doubleValue = doubleValue.doubleValue();
                    return;
                case FLOAT:
                    doubleValue = (double)SerialConversionUtil.getFloat(value.raw,5);
                    longValue = doubleValue.longValue();
                    return;
                case DOUBLE:
                    doubleValue = SerialConversionUtil.getDouble(value.raw,5);
                    longValue = doubleValue.longValue();
                    return;
            }

        }

        @Override
        public String toString() {
            return "ReadValue{" +
                    "type=" + type +
                    ", longValue=" + longValue +
                    ", doubleValue=" + doubleValue +
                    '}';
        }
    }
}
