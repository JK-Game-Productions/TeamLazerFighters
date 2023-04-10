package client;

import net.java.games.input.Event;
import tage.Camera;
import tage.input.action.AbstractInputAction;

public class PanCameraAction extends AbstractInputAction {
    private Camera cam;
    private MyGame game;

    public PanCameraAction(MyGame g) {
        game = g;
        // cam = game.getSmallCamera();
    }

    @Override
    public void performAction(float time, Event evt) {
        String direction = evt.getComponent().toString();
        // System.out.println(direction);
        cam = MyGame.getSmallCamera();
        float diff = game.getFrameDiff();
        if (direction.equals("L") || direction.equals("Button 1"))
            cam.panRight(diff);
        else if (direction.equals("J") || direction.equals("Button 2"))
            cam.panLeft(diff);
        else if (direction.equals("I") || direction.equals("Button 3"))
            cam.panUp(diff);
        else
            cam.panDown(diff);
    }
}
