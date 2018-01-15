package me.davehummel.tredserver.command;

/**
 * Created by dmhum_000 on 4/23/2016.
 */
public enum DataType {
    BYTE('B'),INT_16('I'),UINT_16('U'),INT_32('L'),UINT_32('T'), FLOAT('F'), DOUBLE('D'),STRING('S');

    public final char LETTER;


    DataType(char letter){
        LETTER = letter;
    }


}
