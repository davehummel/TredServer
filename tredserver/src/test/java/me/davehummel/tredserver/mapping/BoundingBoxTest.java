package me.davehummel.tredserver.mapping;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.junit.Test;

import java.io.*;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.*;

/**
 * Created by dmhum on 7/1/2016.
 */
public class BoundingBoxTest {

    @Test
    public void addData_getValue() throws Exception {

        BoundingBox bb = new BoundingBox(3);

        Vector3D point1 = new Vector3D(1,2,0);

        bb.addData(point1);

        assertEquals(1,bb.getValue(new int[]{1,1,1}));

        assertEquals(1,bb.getValue(new int[]{2,2,2}));

        assertEquals(1,bb.getValue(new int[]{0,0,0}));



        assertEquals(0,bb.getValue(new int[]{-1,0,0}));

        assertEquals(0,bb.getValue(new int[]{3,0,0}));

        assertEquals(0,bb.getValue(new int[]{0,3,0}));

        assertEquals(0,bb.getValue(new int[]{0,0,3}));

        assertEquals(0,bb.getValue(new int[]{10,10,11}));

        point1 = new Vector3D(0,.1,0);

        bb.addData(point1);

        assertEquals(2,bb.getValue(new int[]{1,1,1}));

        assertEquals(2,bb.getValue(new int[]{2,2,2}));

        assertEquals(2,bb.getValue(new int[]{0,0,0}));



        assertEquals(0,bb.getValue(new int[]{-1,0,0}));

        assertEquals(0,bb.getValue(new int[]{3,0,0}));

        assertEquals(0,bb.getValue(new int[]{0,3,0}));

        assertEquals(0,bb.getValue(new int[]{0,0,3}));

        assertEquals(0,bb.getValue(new int[]{10,10,11}));

        point1 = new Vector3D(15,0,0);

        bb.addData(point1);

        assertEquals(1,bb.getValue(new int[]{15,0,0}));

    }

    @Test
    public void merge() throws Exception {
    }

    @Test
    public void getPathQuality() throws Exception {

    }

    @Test
    public void binaryStream() throws Exception {
        PipedInputStream in = new PipedInputStream();
        PipedOutputStream out = new PipedOutputStream(in);

        BoundingBox bb = new BoundingBox(3);

        Vector3D point1 = new Vector3D(1,2,0);
        bb.addData(point1);

        point1 = new Vector3D(0,0,0);
        bb.addData(point1);

        point1 = new Vector3D(0,4,4);
        bb.addData(point1);

        point1 = new Vector3D(15,4,4);
        bb.addData(point1);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        bb.jsonObjectStream(stream);

        String original = stream.toString();

        bb.binaryStream(out);

        BoundingBox deBB = BoundingBox.FromBinary(in);

         stream = new ByteArrayOutputStream();

        deBB.jsonObjectStream(stream);

        String copy = stream.toString();

        assertEquals(original,copy);

    }

    @Test
    public void jsonStream() throws Exception {

        BoundingBox bb = new BoundingBox(3);

        Vector3D point1 = new Vector3D(1,2,0);
        bb.addData(point1);

        point1 = new Vector3D(0,0,0);
        bb.addData(point1);

        point1 = new Vector3D(0,4,4);
        bb.addData(point1);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        bb.jsonObjectStream(stream);

        assertEquals("{\"voxelSize\":3,\"cell\":{\"s\":9,\"o\":[-4,-4,-4],\"c\":[0,0,0,0,0,0,0,0,0,0,0,0,0,{\"o\":[-1,-1,-1],\"v\":[0,0,0,0,0,0,0,0,0,0,0,0,0,2,0,0,0,0,0,0,0,0,0,0,0,1,0]},0,0,0,0,0,0,0,0,0,0,0,0,0]}}",stream.toString());



    }

}