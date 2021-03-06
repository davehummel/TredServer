package me.davehummel.tredserver.mapping.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import me.davehummel.tredserver.mapping.BoundingBox;

import java.io.IOException;

/**
 * Created by dmhum on 7/3/2016.
 */
public class BoundingBoxJson extends JsonSerializer<BoundingBox>{

    @Override
    public void serialize(BoundingBox boundingBox, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
        boundingBox.jsonSerialize(jsonGenerator);
    }
}
