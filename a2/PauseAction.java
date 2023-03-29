package a2;

import net.java.games.input.Event;
import tage.input.action.AbstractInputAction;

public class PauseAction extends AbstractInputAction {
    private MyGame game;
    public PauseAction(MyGame g) {
        game = g;
    }
    @Override
    public void performAction(float time, Event e) {
        game.togglePause();
    }
}
