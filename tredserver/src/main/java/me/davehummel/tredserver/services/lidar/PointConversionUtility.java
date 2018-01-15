package me.davehummel.tredserver.services.lidar;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationConvention;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Created by dmhum_000 on 6/18/2016.
 */
public class PointConversionUtility {

  //  public static final double[] LASEROFFSET = {4,2,6};
  public static final double[] LASEROFFSET = {0,0,0};
    public static final double[] TILTOFFSET = {0,0,5};
    public static final double[] BASEOFFSET = {0,0,20};

    private static final Vector3D TILTAXIS = new Vector3D(0,-1,0);
    private static final Vector3D PANAXIS = new Vector3D(0,0,1);

    public static Vector3D convertPanTiltPoint(Vector3D origin, Rotation orient, double pan, double tilt, int distance){


// TODO first make the vector of the laser hit point + laser offst from top servo, then add tilt, tilt servo offset, pan, height offset, orientation,body offset
        Vector3D out = new Vector3D(distance+LASEROFFSET[0],LASEROFFSET[1],LASEROFFSET[2]);
        Rotation rot;
//        // TODO might need to negate angle[1]
        rot = new Rotation(TILTAXIS,tilt,RotationConvention.VECTOR_OPERATOR);
        out = rot.applyTo(out);

        out = new Vector3D(out.getX()+TILTOFFSET[0],out.getY()+TILTOFFSET[1],out.getZ()+TILTOFFSET[2]);
//        // TODO might need to negate angle[0]
        rot = new Rotation(PANAXIS,pan,RotationConvention.VECTOR_OPERATOR);
        out = rot.applyTo(out);

        out = new Vector3D(out.getX()+BASEOFFSET[0],out.getY()+BASEOFFSET[1],out.getZ()+BASEOFFSET[2]);

        out = orient.applyTo(out);

        out= out.add(origin);


        return out;
    }
}
