package client;

import net.java.games.input.Event;
import tage.Camera;
import tage.input.action.AbstractInputAction;

public class ZoomCameraAction extends AbstractInputAction {
    private Camera cam;
    private MyGame game;

    public ZoomCameraAction(MyGame g) {
        game = g;

    }

    @Override
    public void performAction(float time, Event evt) {
        cam = MyGame.getSmallCamera();
        String direction = evt.getComponent().toString();
        float keyValue = evt.getValue();
        // System.out.println(direction);
        if (direction.equals(".") || keyValue < 0)
            cam.zoom(game.getFrameDiff(), true);
        else
            cam.zoom(game.getFrameDiff(), false);
    }
}
