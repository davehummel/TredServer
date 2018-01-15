package me.davehummel.tredserver.mapping;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import me.davehummel.tredserver.mapping.serialize.BoundingBoxJson;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

import java.io.*;


/**
 * Created by dmhum on 6/26/2016.
 */

@JsonSerialize(using = BoundingBoxJson.class)
public class BoundingBox implements Serializable{


    private static final ObjectMapper jsonMapper = new ObjectMapper();

    private static final FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();

    {
        conf.registerClass(BoundingBox.class,RawCell.class,CompoundCell.class);
    }

    private CompoundCell mainCell;

    private final int voxelSize;

    public static BoundingBox FromBinary(InputStream stream) throws IOException {
        FSTObjectInput in = new FSTObjectInput(stream);
        BoundingBox result = null;
        try {
            result = (BoundingBox)in.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        in.close(); // required !
        return result;
    }


    public BoundingBox(int voxelSize) {
        this.voxelSize = voxelSize;
    }


    public void addData(Vector3D vector) {
        int[] point = convertToVoxelCoord((int) vector.getX(), (int) vector.getY(), (int) vector.getZ());
        if (mainCell == null) {
            int[] firstOrigin = new int[]{
                    (point[0] / RawCell.PARTITIONS) * RawCell.PARTITIONS,
                    (point[1] / RawCell.PARTITIONS) * RawCell.PARTITIONS,
                    (point[2] / RawCell.PARTITIONS) * RawCell.PARTITIONS
            };
            mainCell = new CompoundCell(firstOrigin);
        } else {
            while (!fits(point)) {
                mainCell = new CompoundCell(mainCell);
            }
        }

        mainCell.update(point);

    }


    public int getValue(int[] point) {
        if (mainCell == null)
            return 0;
        point = convertToVoxelCoord(point[0], point[1], point[2]);
        if (!fits(point))
            return 0;
        return mainCell.getValue(point);
    }


    public void merge(BoundingBox other) {
        if (other.voxelSize != voxelSize)
            throw new NullPointerException("Cant merge bounding boxes with different voxel size");


    }

    public PathQuality getPathQuality(Vector3D pathStart, Vector3D pathDir, Vector2D pathW_H) {
        return null;
    }

    public void binaryStream(OutputStream stream) throws IOException {

        FSTObjectOutput out = new FSTObjectOutput(stream);
        out.writeObject( this );
        out.close(); // required !

    }

    synchronized public void jsonObjectStream(OutputStream stream) throws IOException {

        jsonMapper.writeValue(stream,this);

    }

    synchronized public void jsonVoxelStream(OutputStream stream, int[] origin, int[] size) throws IOException {

        jsonMapper.writeValue(stream,this);

    }

    private int[] convertToVoxelCoord(int x, int y, int z) {
        int[] out = new int[]{x / voxelSize, y / voxelSize, z / voxelSize};

        if (x < 0)
            out[0]--;
        if (y < 0)
            out[1]--;
        if (z < 0)
            out[2]--;

        return out;
    }

    private boolean fits(int[] point) {
        int[] origin = mainCell.getOrigin();
        int size = mainCell.getSize();
        if (point[0] < origin[0] || point[1] < origin[1] || point[2] < origin[2])
            return false;
        else if (point[0] >= origin[0] + size || point[1] >= origin[1] + size || point[2] >= origin[2] + size)
            return false;
        return true;
    }

    public void jsonSerialize(JsonGenerator jsonGenerator) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("voxelSize",voxelSize);
        jsonGenerator.writeObjectField("cell",mainCell);
        jsonGenerator.writeEndObject();
    }
}
