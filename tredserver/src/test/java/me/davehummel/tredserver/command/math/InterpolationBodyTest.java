package me.davehummel.tredserver.command.math;

import me.davehummel.tredserver.command.ImmediateInstruction;
import me.davehummel.tredserver.command.math.functions.literal.LiteralByte;
import me.davehummel.tredserver.command.math.functions.literal.LiteralFloat;

import static org.junit.Assert.*;

/**
 * Created by dmhum_000 on 5/21/2016.
 */
public class InterpolationBodyTest {
    @org.junit.Test
    public void _toString() throws Exception {
        InterpolationBody interp = new InterpolationBody(12);
        interp.xVals.add(new LiteralFloat(-180));
        interp.yVals.add(new LiteralByte(0));
        interp.interpTypes.add(InterpType.SMOOTH);
        interp.xVals.add(new LiteralFloat(0));
        interp.yVals.add(new LiteralByte(255));
        interp.interpTypes.add(InterpType.SMOOTH);
        interp.xVals.add(new LiteralFloat(180));
        interp.yVals.add(new LiteralByte(0));

        ImmediateInstruction instruction = new ImmediateInstruction('C',101,interp);


        assertEquals("IC 101 C INT 12 #F-180.0,#B0~#F0.0,#B255~#F180.0,#B0", instruction.toString());

    }
}