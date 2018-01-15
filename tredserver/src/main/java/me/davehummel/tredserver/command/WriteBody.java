package me.davehummel.tredserver.command;

/**
 * Created by dmhum on 4/23/2016.
 */
public class WriteBody implements InstructionBody{

    public final DataType dataType;
    public final String addr1;
    public final Object data;

    public WriteBody(DataType datatype, String addr1, Object data){
        this.addr1 = addr1;
        this.data = data;
        this.dataType = datatype;
    }

    @Override
    public InstructionType getType() {
        return InstructionType.Write;
    }

    @Override
    public void _toString(StringBuilder builder) {
        builder.append(dataType.LETTER);
        builder.append(' ');
        builder.append(addr1);
        builder.append(' ');
        builder.append(data);
    }
}
