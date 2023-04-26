package client;

import org.joml.*;
import tage.GameObject;
import net.java.games.input.Event;
import tage.input.action.AbstractInputAction;

public class StrafeAction extends AbstractInputAction {
    private MyGame game;
    private Vector4f mov;
    private float frameDiff;
    private GameObject avatar;
    private Vector3f oldPos, newPos;

    public StrafeAction(MyGame g) {
        game = g;
    }

    @Override
    public void performAction(float time, Event e) {
        avatar = game.getAvatar();
        frameDiff = game.getFrameDiff();

        float keyValue = e.getValue();
        String componentValue = e.getComponent().toString();

        // dead zones
        if (keyValue > -.2 && keyValue < .2)
            return;

        oldPos = avatar.getWorldLocation();
        mov = new Vector4f(1f, 0f, 0f, 1f);
        mov.mul(avatar.getWorldRotation());
        mov.mul(game.getMoveSpeed() * frameDiff);

        // move left
        if (componentValue.equals("A") || keyValue < 0) {
            newPos = oldPos.add(mov.x(), mov.y(), mov.z());
        }
        // move Right
        else {
            newPos = oldPos.sub(mov.x(), mov.y(), mov.z());
        }
        avatar.setLocalLocation(newPos);
        game.getProtocolClient().sendMoveMessage(newPos);
    }
}