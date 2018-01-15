package me.davehummel.tredserver.command.math;

import me.davehummel.tredserver.command.DataType;
import me.davehummel.tredserver.command.ImmediateInstruction;
import me.davehummel.tredserver.command.math.functions.TimeFunc;
import me.davehummel.tredserver.command.math.functions.Variable;
import me.davehummel.tredserver.command.math.functions.literal.LiteralFloat;
import me.davehummel.tredserver.command.math.functions.literal.LiteralU16Int;
import me.davehummel.tredserver.command.math.functions.ops.*;

import static org.junit.Assert.*;

/**
 * Created by dmhum_000 on 5/21/2016.
 */
public class FunctionBodyTest {
    @org.junit.Test
    public void _toString() throws Exception {

        FunctionBody function = new FunctionBody('A', 1, null);
        ImmediateInstruction instruction = new ImmediateInstruction('M', 101, function);
        assertEquals("IC 101 M FUN A1 #B0", instruction.toString());

        function = new FunctionBody('B', 2, new WriteFunc('M', DataType.UINT_16, "XYZ",
                new IfFunc(new StandardFunc("==", new Variable('G', "HED", 0, DataType.FLOAT), new LiteralFloat(10)),
                        new CastFunc(DataType.UINT_16, new SqrtFunc(new TimeFunc())), new LiteralU16Int(0))));
        instruction = new ImmediateInstruction('M', 101, function);
        assertEquals("IC 101 M FUN B2 w[$UM:XYZ=?{$FG:HED.0==#F10.0}[cU[rt],#U0]]", instruction.toString());
    }

}