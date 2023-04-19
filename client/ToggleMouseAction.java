package client;

import net.java.games.input.Event;
import tage.input.action.AbstractInputAction;

public class ToggleMouseAction extends AbstractInputAction {
    private MyGame game;

    public ToggleMouseAction(MyGame g) {
        game = g;
    }

    @Override
    public void performAction(float time, Event evt) {
        game.setMouseVisible(true);
    }
}
