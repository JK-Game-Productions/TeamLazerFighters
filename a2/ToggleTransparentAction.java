package a2;

import net.java.games.input.Event;
import tage.GameObject;
import tage.input.action.AbstractInputAction;

public class ToggleTransparentAction extends AbstractInputAction {
    //private MyGame game;
    private GameObject Xaxis, Yaxis, Zaxis;
    private boolean transparent = false;

    public ToggleTransparentAction(MyGame g, GameObject x, GameObject y, GameObject z) {
        //game = g;
        Xaxis = x;
        Yaxis = y;
        Zaxis = z;
    }

    @Override
    public void performAction(float time, Event evt) {
        if(transparent){
            transparent = false;
            Xaxis.getRenderStates().enableRendering();
            Yaxis.getRenderStates().enableRendering();
            Zaxis.getRenderStates().enableRendering();
        } else {
            transparent = true;
            Xaxis.getRenderStates().disableRendering();
            Yaxis.getRenderStates().disableRendering();
            Zaxis.getRenderStates().disableRendering();
        }
    }
}
