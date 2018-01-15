package me.davehummel.tredserver.mapping.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import me.davehummel.tredserver.mapping.CompoundCell;

import java.io.IOException;

/**
 * Created by dmhum on 7/3/2016.
 */
public class CompoundCellJson extends JsonSerializer<CompoundCell> {


    @Override
    public void serialize(CompoundCell compoundCell, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
        compoundCell.jsonSerialize(jsonGenerator);
    }
}
