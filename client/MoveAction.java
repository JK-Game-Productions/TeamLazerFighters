package client;

import org.joml.*;
import tage.GameObject;
import net.java.games.input.Event;
import tage.input.action.AbstractInputAction;
//import tage.physics.PhysicsObject;
//import tage.shapes.AnimatedShape;

public class MoveAction extends AbstractInputAction {
    private MyGame game;
    private Vector4f mov;
    private float frameDiff;
    private GameObject avatar;
    private String lastComponent;
    // private PhysicsObject pObject;
    private Vector3f oldPos, newPos;
    // private float vals[] = new float[16];

    public MoveAction(MyGame g) {
        game = g;
        lastComponent = "Left Shift";
    }

    @Override
    public void performAction(float time, Event e) {
        avatar = game.getAvatar();
        // pObject = avatar.getPhysicsObject();
        frameDiff = game.getFrameDiff();
        oldPos = avatar.getWorldLocation();

        float keyValue = e.getValue();
        String currentComponent = e.getComponent().toString();
        // System.out.println(currentComponent);

        // if (game.paused())
        // return;

        // controller dead zones
        if (keyValue > -.2 && keyValue < .2)
            return;

        // run
        if (currentComponent.equals("Left Shift")) {
            game.setMoveSpeed(10.0f);
            game.setAvatarRunning(true);
            game.setAvatarWalking(false);
        }
        // walk
        else {
            game.setMoveSpeed(5.0f);
            game.setAvatarRunning(false);
            game.setAvatarWalking(true);
        }
        // forward and back
        if (currentComponent.equals("W")
                || currentComponent.equals("S")
                || lastComponent.equals("W") && currentComponent.equals("Left Shift")
                || lastComponent.equals("S") && currentComponent.equals("Left Shift")) {
            mov = new Vector4f(0f, 0f, 1f, 1f);
            mov.mul(avatar.getWorldRotation());
            mov.mul(game.getMoveSpeed() * frameDiff);
            // System.out.println("Mov values: " + mov.x()+
            // ","+mov.y()+","+mov.z()+","+mov.w());
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
                || lastComponent.equals("D") && currentComponent.equals("Left Shift")) {
            newPos = oldPos.sub(mov.x(), mov.y(), mov.z());
        } else {
            newPos = oldPos;
        }

        lastComponent = currentComponent;
        // game.getPhysicsEngine().removeObject(avatar.getPhysicsObject().getUID());
        // game.mapHeight(avatar);
        avatar.setLocalLocation(newPos);
        // game.buildAvatar();
        game.getProtocolClient().sendMoveMessage(newPos, avatar.getWorldRotation(), game.getTeam());
    }
}
