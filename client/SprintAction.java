package client;

import net.java.games.input.Event;
import tage.input.action.AbstractInputAction;

public class SprintAction extends AbstractInputAction {
    private MyGame game;

    public SprintAction(MyGame g){
        game = g;
    }

    @Override
    public void performAction(float time, Event evt) {
       game.setMoveSpeed(6.0f);
    }
    
}
