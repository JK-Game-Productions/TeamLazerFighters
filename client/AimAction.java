package client;

import net.java.games.input.Event;
import tage.input.action.AbstractInputAction;

public class AimAction extends AbstractInputAction {
    private MyGame game;

    public AimAction(MyGame g) {
        game = g;
    }

    @Override
    public void performAction(float time, Event evt) {
        game.setLazergunAim(true);
    }
}
