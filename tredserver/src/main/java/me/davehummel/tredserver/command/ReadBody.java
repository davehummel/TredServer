package me.davehummel.tredserver.command;

/**
 * Created by dmhum on 4/23/2016.
 */
public class ReadBody implements InstructionBody {

    public final DataType dataType;
    public final String[] addr1Array;
    public final int addr2Offset;
    public final int addr2Length;

    public ReadBody(DataType datatype,String[] addr1Array, int addr2Length,int addr2Offset){
        this.addr1Array = addr1Array;
        this.addr2Length = addr2Length;
        this.addr2Offset = addr2Offset;
        this.dataType = datatype;
    }

    public ReadBody(DataType datatype,String addr1, int addr2Length,int addr2Offset){
        this.addr1Array = new String[]{addr1};
        this.addr2Length = addr2Length;
        this.addr2Offset = addr2Offset;
        this.dataType = datatype;
    }


    public ReadBody(DataType datatype,String addr1){
        this.addr1Array = new String[]{addr1};
        this.addr2Length = 1;
        this.addr2Offset = 0;
        this.dataType = datatype;
    }

    @Override
    public InstructionType getType() {
        return InstructionType.Read;
    }

    @Override
    public void _toString(StringBuilder builder) {
        builder.append(dataType.LETTER);
        builder.append(' ');
        builder.append(addr1Array[0]);
        for (int i = 1; i < addr1Array.length; i++){
            builder.append(',');
            builder.append(addr1Array[i]);
        }
        builder.append(' ');
        builder.append(addr2Length);
        builder.append(' ');
        builder.append(addr2Offset);
    }
}
