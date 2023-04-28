package client;

import org.joml.*;
import tage.GameObject;
import net.java.games.input.Event;
import tage.input.action.AbstractInputAction;

public class MoveAction extends AbstractInputAction {
    private MyGame game;
    private Vector4f mov;
    private float frameDiff;
    private GameObject avatar;
    private Vector3f oldPos, newPos;
    private String lastComponent;

    public MoveAction(MyGame g) {
        game = g;
        lastComponent = "Left Shift";
    }

    @Override
    public void performAction(float time, Event e) {
        avatar = game.getAvatar();
        frameDiff = game.getFrameDiff();
        oldPos = avatar.getWorldLocation();
        float keyValue = e.getValue();
        String currentComponent = e.getComponent().toString();

        System.out.println(currentComponent);

        // controller dead zones
        if (keyValue > -.2 && keyValue < .2)
            return;
        // run time
        if (currentComponent.equals("Left Shift"))
            game.setMoveSpeed(6.0f);
        else
            game.setMoveSpeed(3.0f);
        // forward and back
        if (currentComponent.equals("W")
                || currentComponent.equals("S")
                || lastComponent.equals("W") && currentComponent.equals("Left Shift")
                || lastComponent.equals("S") && currentComponent.equals("Left Shift")) {
            mov = new Vector4f(0f, 0f, 1f, 1f);
            mov.mul(avatar.getWorldRotation());
            mov.mul(game.getMoveSpeed() * frameDiff);
        }
        // left and right
        if (currentComponent.equals("A")
                || currentComponent.equals("D")
                || lastComponent.equals("A") && currentComponent.equals("Left Shift")
                || lastComponent.equals("D") && currentComponent.equals("Left Shift")) {
            mov = new Vector4f(1f, 0f, 0f, 1f);
            mov.mul(avatar.getWorldRotation());
            mov.mul(game.getMoveSpeed() * frameDiff);
        }

        // forward and left
        if (currentComponent.equals("W")
                || currentComponent.equals("A")
                || lastComponent.equals("W") && currentComponent.equals("Left Shift")
                || lastComponent.equals("A") && currentComponent.equals("Left Shift")
                || keyValue < 0) {
            newPos = oldPos.add(mov.x(), mov.y(), mov.z());
        }
        // back and right
        else if (currentComponent.equals("S") 
                || currentComponent.equals("D") 
                || lastComponent.equals("S") && currentComponent.equals("Left Shift")
                || lastComponent.equals("D") && currentComponent.equals("Left Shift")
                ) {
            newPos = oldPos.sub(mov.x(), mov.y(), mov.z());
        } else {
            newPos = oldPos;
        }
        lastComponent = currentComponent;

        avatar.setLocalLocation(newPos);
        game.getProtocolClient().sendMoveMessage(newPos);
    }
}
