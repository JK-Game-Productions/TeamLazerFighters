package client;

import net.java.games.input.Event;
import tage.GameObject;
import tage.input.action.AbstractInputAction;

public class TurnAction extends AbstractInputAction {
    private MyGame game;
    private GameObject dol;

    // private Matrix4f oldRot, newRot, rotAroundDolUp;
    // private Vector3f right, up, fwd;
    // private Vector4f oldUp;
    // private float fd;
    // private boolean direction; //true=left false=right
    public TurnAction(MyGame g) {
        game = g;

    }

    @Override
    public void performAction(float time, Event e) {
        float keyValue = e.getValue();
        float fd = game.getFrameDiff();

        if (game.paused())
            return;

        // dead zones
        if (keyValue > -.2 && keyValue < .2)
            return;

        // left
        if (e.getComponent().toString().equals("A") || keyValue < 0) {
            dol = game.getAvatar();
            dol.yaw(fd, true);
            // dol.yaw(false);
            /*
             * oldRot = new Matrix4f(dol.getWorldRotation());
             * oldUp = new Vector4f(0f, 1f, 0f, 1f).mul(oldRot);
             * rotAroundDolUp = new Matrix4f().rotation(.005f, new Vector3f(oldUp.x(),
             * oldUp.y(), oldUp.z()));
             * newRot = oldRot;
             * newRot.mul(rotAroundDolUp);
             * dol.setLocalRotation(newRot);
             */
            // rotate camera around N
            // c.turnWithDolphin(dol);
        }
        // right
        else {
            dol = game.getAvatar();
            dol.yaw(fd, false);
            // c.turnWithDolphin(dol);

            // dol.yaw(true);
            /*
             * 
             * oldRot = new Matrix4f(dol.getWorldRotation());
             * oldUp = new Vector4f(0f, 1f, 0f, 1f).mul(oldRot);
             * rotAroundDolUp = new Matrix4f().rotation(-.005f, new Vector3f(oldUp.x(),
             * oldUp.y(), oldUp.z()));
             * newRot = oldRot;
             * newRot.mul(rotAroundDolUp);
             * dol.setLocalRotation(newRot);
             * 
             */
        }
    }
}
