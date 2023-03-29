package a2;

import net.java.games.input.Event;
import org.joml.Vector3f;
import tage.Camera;
import tage.GameObject;
import tage.input.action.AbstractInputAction;

public class MountAction extends AbstractInputAction {
    private MyGame game;
    private GameObject dol;
    private Camera c;
    private Vector3f right,up, oldPos, newPos;

    public MountAction(MyGame g) {
        game = g;
    }
    @Override
    public void performAction(float time, Event e)
    {   /*
        if(game.getRiding()) { //get off dolphin nearby currently crashes
            game.toggleRiding();
            //c = (game.getEngine().getRenderSystem()).getViewport("MAIN").getCamera();
            c = game.getMainCamera();
            oldPos =c.getLocation();
            right = c.getU();
            right.mul(1.0f);
            newPos = oldPos.add(right.x(),right.y(),right.z());
            up = c.getV();
            up.mul(-1.5f);
            newPos = newPos.add(up.x(),up.y(),up.z());
            c.setLocation(newPos);
        }
        else {
            game.toggleRiding();
        }
        */
    }
}
