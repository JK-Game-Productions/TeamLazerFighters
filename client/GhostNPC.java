package client;

import org.joml.*;
import tage.*;

public class GhostNPC extends GameObject {
    private int id;

    public GhostNPC(int id, ObjShape s, TextureImage t, Vector3f p) {
        super(GameObject.root(), s, t);
        this.setLocalScale((new Matrix4f()).scaling(0.45f));
        this.id = id;
        setPosition(p);
    }

    public void setSize(boolean big) {
        if (!big) {
            this.setLocalScale((new Matrix4f()).scaling(0.2f));// 0.3
        } else {
            this.setLocalScale((new Matrix4f()).scaling(1.0f));
        }
    }

    public void setPosition(Vector3f p) {
        this.setLocalLocation(p);
    }

    public void getLocation() {
        this.getLocalLocation();
    }

    public int getNPCid() {
        return id;
    }
}
