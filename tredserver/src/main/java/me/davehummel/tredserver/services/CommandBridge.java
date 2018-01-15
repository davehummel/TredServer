package me.davehummel.tredserver.services;

import ch.qos.logback.core.net.SyslogOutputStream;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import me.davehummel.tredserver.command.Instruction;
import me.davehummel.tredserver.serial.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by dmhum_000 on 9/5/2015.
 */
public class CommandBridge {
    private static final int CHAR_MAX_SEND_BUFFER = 62;  // Seems to drop characters after the 63rd .. 62 is safe?

    private SendResponse requiredResponse;
    private boolean oneLineMode = true;
    private int unprocessedSent = 0;
    private Object oneLineSync = new Object();

    public boolean isSimulation() {
        return serial.getSimulation();
    }

    synchronized public boolean isLastInstrunctionFailed() {
        return lastInstrunctionFailed;
    }

    public void setLastInstrunctionFailed(boolean lastInstrunctionFailed, long time, String error) {
        this.lastInstrunctionFailed = lastInstrunctionFailed;
        if (lastInstrunctionFailed) {
            System.out.println("Last Instruction Failed!");
        } else {
            System.out.println("Last Instruction Success!");
        }
        if (requiredResponse != null) {
            synchronized (requiredResponse) {
                System.out.println("Notifying response");
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

    private final SerialBridge serial;
    private final List<CommandListener> listeners = new ArrayList<>();
    private final LoadingCache<InstPair, Instruction> runInstructions = CacheBuilder.newBuilder().maximumSize(32).build(new CacheLoader<InstPair, Instruction>() {
        @Override
        public Instruction load(InstPair instPair) throws Exception {
            return null;
        }
    });

    private boolean lastInstrunctionFailed = false;

    private boolean active = false;
    private long offset = 0; // Add offset to input time


    private Thread commandMonitor;
    private Runnable runner = () -> {
        while (active) {
            try {
                read();
                if (commandMonitor.isInterrupted())
                    active = false;
            } catch (Throwable t) {
                t.printStackTrace();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };


    private void read() {
        try {
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
            System.out.print(">");
            System.out.print(line);
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
                System.out.println("< Matched " + matched + " listeners");
            }
        } catch (
                SerialBridgeException e)

        {
            e.printStackTrace();
        }

    }

    private void markInstruction(char module, long instructionID) {
        System.out.println("Mrk!??");
    }

    public CommandBridge(SerialBridge serial) {
        this.serial = serial;
    }

    public void addCommandListener(CommandListener listener) {
        listeners.add(listener);
    }

    public boolean removeCommandListener(CommandListener listener) {
        return listeners.remove(listener);
    }

    public void start() {
//        try {
//       //     serial.start();
//        } catch (SerialBridgeException e) {
//            e.printStackTrace();
//
//        }
        commandMonitor = new Thread(runner, "CommandMonitor");
        commandMonitor.setDaemon(true);
        active = true;
        commandMonitor.start();

    }

    public void stop() {
        active = false;
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
                System.out.println("Throttling Sends!");
                oneLineSync.wait(2000);
                if (unprocessedSent >= CHAR_MAX_SEND_BUFFER) {
                    System.out.println("Line Sync Timed Out!");
                    unprocessedSent = 0;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                return "";
            }
        }
        int remaining = CHAR_MAX_SEND_BUFFER - unprocessedSent;
        if (remaining > text.length())
            remaining = text.length();

        String leftOver = text.substring(remaining);
        String toSend = text.substring(0, remaining);

        unprocessedSent += toSend.length();
        System.out.println(unprocessedSent);
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
                e.printStackTrace();
            }
            synchronized (requiredResponse) {
                meteredWrite("IC 0 Z SE\n");
                try {
                    requiredResponse.wait(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    synchronized public boolean writeInstruction(Instruction command) {

        try {
            StringBuilder builder = new StringBuilder();
            command.toString(builder);
            builder.append('\n');
            // System.out.println(">>"+builder.toString());
            write(builder.toString());

        } catch (SerialBridgeException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    synchronized public boolean writeKill(int id) {
        try {
            write("K " + id + '\n');
        } catch (SerialBridgeException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    synchronized public boolean writeRestart() {
        try {
            write("KR\n");
        } catch (SerialBridgeException e) {
            e.printStackTrace();
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
            //    System.out.println(">>"+builder.toString());
            write(builder.toString());
        } catch (SerialBridgeException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    synchronized public boolean runProgram(int id) {
        try {
            write("R " + id + '\n');
        } catch (SerialBridgeException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    synchronized public boolean writeDirect(String text) {
        try {
            write(text + '\n');
        } catch (SerialBridgeException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    synchronized public boolean togglePureInput() {
        try {
            write("`");
        } catch (SerialBridgeException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public long getOffset() {
        return offset;
    }
}
