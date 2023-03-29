package a2;

import net.java.games.input.Event;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import tage.Camera;
import tage.GameObject;
import tage.input.action.AbstractInputAction;

public class PitchAction extends AbstractInputAction {
    private MyGame game;
    private GameObject dol;
    private Camera c;
    private Vector3f right, up, fwd;
    private Matrix4f oldRot, newRot, rotAroundDolRight;
    private Vector4f oldRight;
    private boolean angle; //true=up false=down
    public PitchAction(MyGame g) {
        game = g;
    }

    @Override
    public void performAction(float time, Event e) {
        //test for controller input +deadzonecode
        //System.out.println(e.getComponent().toString());
        float keyValue = e.getValue();
        String componentValue = e.getComponent().toString();
        if(componentValue.equals("Up") || keyValue < 0 ) {
            //up
            if (keyValue > -.2 && keyValue < .2) return;
             //weirdness when pitch with this and turn doesn't pitch correctly
                dol = game.getDolphin();
                dol.pitch(game.getFrameDiff(),true);
                /*
                oldRot = new Matrix4f(dol.getWorldRotation());
                oldRight = new Vector4f(1f,0f,0f,1f).mul(oldRot);
                rotAroundDolRight = new Matrix4f().rotation(.005f, new Vector3f(oldRight.x(),oldRight.y(),oldRight.z()));
                newRot = oldRot;
                newRot.mul(rotAroundDolRight);
                dol.setLocalRotation(newRot);

                 */

        }
        //down
        else {
            if (keyValue > -.2 && keyValue < .2) return;
                dol = game.getDolphin();
                dol.pitch(game.getFrameDiff(),false);
                /*
                oldRot = new Matrix4f(dol.getWorldRotation());
                oldRight = new Vector4f(1f, 0f, 0f, 1f).mul(oldRot);
                rotAroundDolRight = new Matrix4f().rotation(-.005f, new Vector3f(oldRight.x(), oldRight.y(), oldRight.z()));
                newRot = oldRot;
                newRot.mul(rotAroundDolRight);
                dol.setLocalRotation(newRot);

                 */
            }
        }

}
