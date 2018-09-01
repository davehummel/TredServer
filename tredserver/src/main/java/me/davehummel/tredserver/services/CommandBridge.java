package me.davehummel.tredserver.services;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import me.davehummel.tredserver.command.Instruction;
import me.davehummel.tredserver.serial.SerialBridge;
import me.davehummel.tredserver.serial.SerialBridgeException;
import me.davehummel.tredserver.serial.SerialConversionUtil;
import me.davehummel.tredserver.serial.StandardLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by dmhum_000 on 9/5/2015.
 */
public class CommandBridge {
    private static final int CHAR_MAX_SEND_BUFFER = 62;  // Seems to drop characters after the 63rd .. 62 is safe?
    Logger logger = LoggerFactory.getLogger(CommandBridge.class);

    private final SerialBridge serial;
    private final List<CommandListener> listeners = new ArrayList<>();
    private final LoadingCache<InstPair, Instruction> runInstructions = CacheBuilder.newBuilder().maximumSize(32).build(new CacheLoader<InstPair, Instruction>() {
        @Override
        public Instruction load(InstPair instPair) throws Exception {
            return null;
        }
    });
    private SendResponse requiredResponse;
    private boolean oneLineMode = true;
    private int unprocessedSent = 0;
    private Object oneLineSync = new Object();
    private boolean lastInstrunctionFailed = false;
    volatile private boolean active = false;
    private long offset = 0; // Add offset to input time
    private Thread commandMonitor;
    private Runnable runner = () -> {
        while (active) {
            try {
                read();
                synchronized (commandMonitor) {
                    if (commandMonitor.isInterrupted())
                        active = false;
                }
            } catch (SerialBridgeException sbe) {
                logger.error("Serial connection failure",sbe);
                active = false;
            } catch (Exception t) {
                logger.error("Command monitor failed to read",t);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    logger.error("sleep for command bridge runner interrupted",e);
                }
            }
        }
        logger.info("CommandMonitor exiting!");
    };

    public CommandBridge(SerialBridge serial) {
        this.serial = serial;
    }

    public boolean isSimulation() {
        return serial.getSimulation();
    }

    synchronized public boolean isLastInstrunctionFailed() {
        return lastInstrunctionFailed;
    }

    public void setLastInstrunctionFailed(boolean lastInstrunctionFailed, long time, String error) {
        this.lastInstrunctionFailed = lastInstrunctionFailed;
        if (lastInstrunctionFailed) {
            logger.info("Last Instruction Failed!");
        } else {
            logger.info("Last Instruction Success!");
        }
        if (requiredResponse != null) {
            synchronized (requiredResponse) {
                logger.info("Notifying response");
                requiredResponse.setSuccess(!lastInstrunctionFailed);
                requiredResponse.setExecTime(time);
                requiredResponse.setError(error);
                requiredResponse.notify();
                requiredResponse = null;
            }
        }
    }

    public boolean isOneLineMode() {
        return oneLineMode;
    }

    public void setOneLineMode(boolean oneLineMode) {
        this.oneLineMode = oneLineMode;
    }

    private void read() throws SerialBridgeException {

            byte[] data = serial.readLine();
            StandardLine line;
            if (data.length == 0) {
                markReceived();
                return;
            } else if (data.length == 4) {
                offset = SerialConversionUtil.get32Int(data, 0);
                line = StandardLine.timeLine(offset);
            } else {
                line = StandardLine.createLine(data);
            }
            logger.debug(line.toString());
            if (data.length == 5) {
                markInstruction(line.module, line.instructionID);
            } else if (line.instructionID == 0 && line.module == 'Z') {

                long eventTime = SerialConversionUtil.getU32Int(data, 5);

                boolean failed = eventTime != 0;

                if (failed) {
                    byte[] subData = Arrays.copyOfRange(data, 9, data.length);
                    setLastInstrunctionFailed(true, eventTime, new String(subData, StandardCharsets.UTF_8));
                } else {
                    setLastInstrunctionFailed(false, 0, null);
                }

            } else {
                int matched = 0;
                for (CommandListener cl : listeners) {
                    if (cl.matches((StandardLine) line)) {
                        cl.processData((StandardLine) line);
                        matched++;
                    }
                }
                logger.debug("< Matched " + matched + " listeners");
            }


    }

    private void markInstruction(char module, long instructionID) {
        logger.error("Mark instruction received??");
    }

    public void addCommandListener(CommandListener listener) {
        listeners.add(listener);
    }

    public boolean removeCommandListener(CommandListener listener) {
        return listeners.remove(listener);
    }


    public void start() throws SerialBridgeException {
        logger.info("Starting CommandMonitor");

        if (!serial.isOpen()){
            logger.info("Reopening serial");
            serial.start();
        }

        commandMonitor = new Thread(runner, "CommandMonitor");
        commandMonitor.setDaemon(true);
        active = true;
        commandMonitor.start();

    }

    public void stop() {
        logger.info("Trying to kill commandMonitor.");
        active = false;
        synchronized ((commandMonitor)) {
            commandMonitor.notify();
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            logger.error("Stop interrupted",e);
        }


        serial.end();
    }

    public void demandResponse(SendResponse response) {
        this.requiredResponse = response;
    }

    private void markReceived() {
        if (oneLineMode) {
            unprocessedSent = 0;
            synchronized (oneLineSync) {
                oneLineSync.notify();
            }
        }
    }

    private void meteredWrite(String text) throws SerialBridgeException {
        if (oneLineMode) {
            synchronized (oneLineSync) {
                while (text.length() > 0) {
                    text = partialSend(text);
                }
            }
        } else {
            serial.write(text);
        }

    }

    private String partialSend(String text) throws SerialBridgeException {
        if (unprocessedSent >= CHAR_MAX_SEND_BUFFER) {
            try {
                logger.error("Throttling Sends!");
                oneLineSync.wait(2000);
                if (unprocessedSent >= CHAR_MAX_SEND_BUFFER) {
                    logger.error("Line Sync Timed Out!");
                    unprocessedSent = 0;
                }
            } catch (InterruptedException e) {
                logger.error("Interrupted",e);
                return "";
            }
        }
        int remaining = CHAR_MAX_SEND_BUFFER - unprocessedSent;
        if (remaining > text.length())
            remaining = text.length();

        String leftOver = text.substring(remaining);
        String toSend = text.substring(0, remaining);

        unprocessedSent += toSend.length();
        logger.info("Partial sent, remaining = "+unprocessedSent);
        serial.write(toSend);
        return leftOver;

    }

    private void write(String text) throws SerialBridgeException {

        if (requiredResponse != null) {
            meteredWrite("IC 0 Z CE\n");
        }
        meteredWrite(text);
        if (requiredResponse != null) {
            try {
                Thread.currentThread().sleep(500);
            } catch (InterruptedException e) {
                logger.error("Interrupted",e);
            }
            synchronized (requiredResponse) {
                meteredWrite("IC 0 Z SE\n");
                try {
                    requiredResponse.wait(500);
                } catch (InterruptedException e) {
                    logger.error("Interrupted",e);
                }
            }
        }
    }

    synchronized public boolean writeInstruction(Instruction command) {

        try {
            StringBuilder builder = new StringBuilder();
            command.toString(builder);
            builder.append('\n');
            logger.debug(">>"+builder.toString());
            write(builder.toString());

        } catch (SerialBridgeException e) {
            logger.error("Write failed",e);
            return false;
        }
        return true;
    }

    synchronized public boolean writeKill(int id) {
        try {
            write("K " + id + '\n');
        } catch (SerialBridgeException e) {
            logger.error("Write failed",e);
            return false;
        }
        return true;
    }

    synchronized public boolean writeRestart() {
        try {
            write("KR\n");
        } catch (SerialBridgeException e) {
            logger.error("Write failed",e);
            return false;
        }
        return true;
    }

    synchronized public boolean writeProgram(int pID, List<Instruction> commands) {
        StringBuilder builder = new StringBuilder("P ");
        builder.append(pID);
        builder.append(' ');
        boolean first = true;
        for (Instruction command : commands) {
            if (!first) {
                builder.append(';');
            }
            first = false;
            command.toString(builder);
        }
        builder.append('\n');
        try {
            logger.debug(">>"+builder.toString());
            write(builder.toString());
        } catch (SerialBridgeException e) {
            logger.error("Write failed",e);
            return false;
        }
        return true;
    }

    synchronized public boolean runProgram(int id) {
        try {
            write("R " + id + '\n');
        } catch (SerialBridgeException e) {
            logger.error("Write failed",e);
            return false;
        }
        return true;
    }

    synchronized public boolean writeDirect(String text) {
        try {
            write(text + '\n');
        } catch (SerialBridgeException e) {
            logger.error("Write failed",e);
            return false;
        }
        return true;
    }

    synchronized public boolean togglePureInput() {
        try {
            write("`");
        } catch (SerialBridgeException e) {
            logger.error("Write failed",e);
            return false;
        }
        return true;
    }

    public long getOffset() {
        return offset;
    }

    public class InstPair {
        public final char modID;
        public final long instID;

        public InstPair(char modID, long instID) {
            this.modID = modID;
            this.instID = instID;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            InstPair instPair = (InstPair) o;

            if (modID != instPair.modID) return false;
            return instID == instPair.instID;

        }

        @Override
        public int hashCode() {
            int result = (int) modID;
            result = 31 * result + (int) (instID ^ (instID >>> 32));
            return result;
        }
    }
}
