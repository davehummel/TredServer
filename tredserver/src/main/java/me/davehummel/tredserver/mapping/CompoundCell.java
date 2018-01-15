package me.davehummel.tredserver.mapping;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import me.davehummel.tredserver.mapping.serialize.BoundingBoxJson;
import me.davehummel.tredserver.mapping.serialize.CompoundCellJson;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.io.IOException;


/**
 * Created by dmhum on 6/25/2016.
 */

@JsonSerialize(using = CompoundCellJson.class)
public class CompoundCell implements Cell {

    public final static int PARTITIONS = 3;

    public final int size;

    private Cell[] cells = new Cell[PARTITIONS * PARTITIONS * PARTITIONS];

    private final int[] origin;


    public CompoundCell(CompoundCell centerCell) {
        int shift = (PARTITIONS - 1) / 2 * centerCell.getSize();
        origin = new int[]{centerCell.getOrigin()[0] - shift, centerCell.getOrigin()[1] - shift, centerCell.getOrigin()[2] - shift};

        size = centerCell.getSize()*PARTITIONS;

        cells[getIndex(centerCell.getOrigin())]=centerCell;

    }

    public CompoundCell(int[] centerPoint) {

        size = RawCell.PARTITIONS * PARTITIONS;
        this.origin = new int[]{centerPoint[0] - size / 2, centerPoint[1] - size / 2, centerPoint[2] - size / 2};
        // Ensure all possible first raw cell stays aligned to the same origin

    }

    private CompoundCell(int[] origin,int size){
        this.origin = origin;
        this.size = size;

    }

    @Override
    public int[] getOrigin() {
        return origin;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public boolean update(int[] point) {

        int index = getIndex(point);

        if (cells[index] == null) {
            int innerSize = size/PARTITIONS;
            if (innerSize == RawCell.PARTITIONS){
                cells[index] = new RawCell(getSubOrigin(index));
            }else{
                cells[index] = new CompoundCell(getSubOrigin(index),innerSize);
            }

        }
        cells[index].update(point);
        return true;
    }

    @Override
    public PathQuality getPathQuality(Vector3D pathStart, Vector3D pathDir, Vector2D pathW_H) {
        return null;
    }

    @Override
    public boolean degrade() {
        boolean hasContent = false;
        for (int i = 0; i < cells.length; i++) {

            if (cells[i] == null)
                continue;

            if (cells[i].degrade()) {
                hasContent = true;
            } else {
                cells[i] = null;
            }

        }
        return hasContent;
    }

    @Override
    public int getValue(int[] point) {
        Cell cell = cells[getIndex(point)];
        if (cell == null)
            return 0;
        else
            return cell.getValue(point);
    }

    private int getIndex(int[] point) {
        int cellSize = size / PARTITIONS;
        return (point[0] - origin[0]) / cellSize +
                (point[1] - origin[1]) / cellSize * PARTITIONS +
                (point[2] - origin[2]) / cellSize * PARTITIONS * PARTITIONS;
    }

    private int[] getSubOrigin(int index){
        int cellSize = size / PARTITIONS;
        int[] val = new int[3];
        val[0] = origin[0] + index%PARTITIONS * cellSize;
        index = index / PARTITIONS;
        val[1] = origin[1] + index%PARTITIONS * cellSize;
        index = index / PARTITIONS;
        val[2] = origin[2] + index * cellSize;
        return val;
    }

    public void jsonSerialize(JsonGenerator jsonGenerator) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("s",size);
        jsonGenerator.writeFieldName("o");
        jsonGenerator.writeArray(origin,0,3);
        jsonGenerator.writeArrayFieldStart("c");
        for(Cell cell:cells){
            if (cell == null)
                jsonGenerator.writeNumber(0);
            else
                jsonGenerator.writeObject(cell);
        }
        jsonGenerator.writeEndArray();
        jsonGenerator.writeEndObject();
    }
}
