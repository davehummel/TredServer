package me.davehummel.tredserver.command.math;

import me.davehummel.tredserver.command.DataType;
import me.davehummel.tredserver.command.ImmediateInstruction;
import me.davehummel.tredserver.command.ReadBody;
import me.davehummel.tredserver.command.math.functions.TimeFunc;
import me.davehummel.tredserver.command.math.functions.Variable;
import me.davehummel.tredserver.command.math.functions.literal.LiteralFloat;
import me.davehummel.tredserver.command.math.functions.literal.LiteralU16Int;
import me.davehummel.tredserver.command.math.functions.ops.*;

import static org.junit.Assert.assertEquals;

/**
 * Created by dmhum_000 on 2/4/2017.
 */
public class ReadBodyTest {

    @org.junit.Test
    public void _toString() throws Exception {

        ReadBody function = new ReadBody(DataType.BYTE,"ZZZ");
        ImmediateInstruction instruction = new ImmediateInstruction('Z', 897, function);
        assertEquals("IR 897 Z B ZZZ 1 0", instruction.toString());
    }
}
//IR 897 Z S LST 1 0