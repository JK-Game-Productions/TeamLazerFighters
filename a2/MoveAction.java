package a2;

import org.joml.*;
import tage.Camera;
import tage.GameObject;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;

public class MoveAction extends AbstractInputAction {
    private MyGame game;
    private GameObject dol;
    private Camera c;
    private Vector3f oldPos, newPos, up, oldCamLoc;
    private Vector4f mov;
    private float frameDiff;
    private boolean movement; //true=fwd false=rev


    public MoveAction(MyGame g) {
        game = g;
    }
    @Override
    public void performAction(float time, Event e) {
        //test for controller input +deadzonecode
        dol = game.getDolphin();
        frameDiff = game.getFrameDiff();
        c = game.getSmallCamera();
        float keyValue = e.getValue();
        String componentValue = e.getComponent().toString();
        //System.out.println(componentValue + ": " + keyValue);
        if (componentValue.equals("W") || keyValue < 0) {
            //forward
            if (keyValue > -.2 && keyValue < .2) return;
                //dol = game.getDolphin();
                oldPos = dol.getWorldLocation();
                mov = new Vector4f(0f, 0f, 1f, 1f);
                mov.mul(dol.getWorldRotation());
                mov.mul(3.0f * frameDiff);
                newPos = oldPos.add(mov.x(), mov.y(), mov.z());
                dol.setLocalLocation(newPos);
                // move camera down V
                //c.moveWithDolphin(dol);
        }
        //reverse
        else {
            if (keyValue > -.2 && keyValue < .2) return;
                //dol = game.getDolphin();
                oldPos = dol.getWorldLocation();
                mov = new Vector4f(0f, 0f, 1f, 1f);
                mov.mul(dol.getWorldRotation());
                mov.mul(3.0f * frameDiff);
                newPos = oldPos.sub(mov.x(), mov.y(), mov.z());
                dol.setLocalLocation(newPos);
                //c.moveWithDolphin(dol);
        }
    }
}
