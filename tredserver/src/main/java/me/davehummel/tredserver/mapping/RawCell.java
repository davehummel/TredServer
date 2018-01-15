package me.davehummel.tredserver.mapping;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import me.davehummel.tredserver.mapping.serialize.CompoundCellJson;
import me.davehummel.tredserver.mapping.serialize.RawCellJson;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.io.IOException;

/**
 * Created by dmhum on 6/25/2016.
 */
@JsonSerialize(using = RawCellJson.class)
public class RawCell implements Cell {

    public final static int PARTITIONS = 3;
    public final static int DEGRADE_THRESH = 15;

    public byte[] voxels = new byte[PARTITIONS * PARTITIONS * PARTITIONS];

    public final int[] origin;

    public RawCell(int[] origin) {
        this.origin = origin;
        for (int i = 0 ; i < voxels.length ; i++){
            voxels[i] = Byte.MIN_VALUE;
        }
    }



    @Override
    public boolean update(int[] point) {
        int index = getIndex(point);
        voxels[index]++;
        return true;
    }

    @Override
    public int[] getOrigin(){
        return origin;
    }

    @Override
    public int getSize(){
        return PARTITIONS;
    }

    @Override
    public PathQuality getPathQuality(Vector3D pathStart, Vector3D pathDir, Vector2D pathW_H) {
        //TODO - eval;
        return PathQuality.SMOOTH;
    }

    @Override
    public boolean degrade() {
        boolean hasContent = false;
        for (int i = 0; i < voxels.length; i++) {
            if (voxels[0]<DEGRADE_THRESH)
                hasContent |= ((--voxels[i]) > 0);
            else
                hasContent = true;
        }
        return hasContent;
    }

    @Override
    public int getValue(int[] point) {
        return (int)voxels[getIndex(point)] - (int)Byte.MIN_VALUE;
    }

    private int getIndex(int[] point) {

        return (point[0] - origin[0])  +
                (point[1] - origin[1])  * PARTITIONS +
                (point[2] - origin[2])  * PARTITIONS * PARTITIONS;
    }

    public void jsonSerialize(JsonGenerator jsonGenerator) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeFieldName("o");
        jsonGenerator.writeArray(origin,0,3);
        jsonGenerator.writeArrayFieldStart("v");
        for (int i = 0; i < voxels.length; i++)
            jsonGenerator.writeNumber(((int)voxels[i])-Byte.MIN_VALUE);
        jsonGenerator.writeEndArray();
        jsonGenerator.writeEndObject();
    }

}
