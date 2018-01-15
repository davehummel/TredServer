package me.davehummel.tredserver.mapping;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.io.Serializable;

/**
 * Created by dmhum on 6/25/2016.
 */
public interface Cell extends Serializable {

    int[] getOrigin();

    int getSize();

    boolean update(int[] point);

    PathQuality getPathQuality(Vector3D pathStart, Vector3D pathDir, Vector2D pathW_H);

    /**
     * The degrade function is executed when a recent observation sees nothing in this
     * cell after that obsercation is merged.  This reduces confidence in the existance
     * of objects in the cell and will lead to deletion.  Degrade returns true as long
     * as there is still content in the cell
     *
     *
     * @return boolean true if the cell is still not empty, false if no content left
     */
    boolean degrade();

    int getValue(int[] point);

}
