import javax.vecmath.Vector3f;

import tage.GameObject;

public class NPC {
    double locationX, locationY, locationZ;
    double dir = 0.1;
    double size = 1.0;


    public NPC() {
        locationX = 0.0;
        locationY = 0.0;
        locationZ = 0.0;
    }

    public void randomizeLocation(int seedX, int seedZ) {
        locationX = ((double) seedX) / 4.0 - 5.0;
        locationY = 0;
        locationZ = -2;
    }

    public void setLocation(Vector3f newLocation) {
        locationX = newLocation.getX();
        locationY = newLocation.getY();
        locationZ = newLocation.getZ();
    }

    public double getX() {
        return locationX;
    }

    public double getY() {
        return locationY;
    }

    public double getZ() {
        return locationZ;
    }

    public Vector3f getLocation() {
        return new Vector3f((float) locationX, (float) locationY, (float) locationZ);
    }

    public void getBig() {
        size = 2.0;// 2.0
    }

    public void getSmall() {
        size = 0.2;// 1.0
    }

    public double getSize() {
        return size;
    }

    public void updateLocation() {
        if (locationX > 10)
            dir = -0.1;
        if (locationX < -10)
            dir = 0.1;
        locationX = locationX + dir;
    }
}