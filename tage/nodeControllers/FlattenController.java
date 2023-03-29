package tage.nodeControllers;

import tage.*;
import org.joml.*;

/**
 * A FlattenController is a node controller that, when enabled, causes any object
 * it is attached to, to flatten in scale for a certain amount of time and disable itself.
 * @author John Wishek
 */
public class FlattenController extends NodeController {

    private boolean isFlat;
    private Matrix4f curScale, newScale;
    private float unflattenScale;
    private float totalTime = 0.0f;
    private float cycleTime = 10.0f;

    /** Creates a flatten controller with the original unflattened scale as specified*/
    public FlattenController(float scale) {
        super();
        unflattenScale = scale;
        newScale = new Matrix4f();
        isFlat = false;
        //System.out.println("scale const: " +scale);
    }

    /** This is called automatically by the RenderSystem, once per frame during display().
     * It is for engine use and should not be called by the application*/
    public void apply(GameObject go) {
       //apply flatten
        float elapsedTime = super.getElapsedTime();
        totalTime += elapsedTime/1000.0f;
        if(!isFlat) {
            curScale = go.getLocalScale();
            newScale.scaling(curScale.m00(), 0.5f, curScale.m22());
            go.setLocalScale(newScale);
            isFlat = true;
        }
        //time passes
        if(totalTime > cycleTime) { // unflatten
            curScale = go.getLocalScale();
            newScale.scaling(curScale.m00(), unflattenScale, curScale.m22());
            go.setLocalScale(newScale);
            totalTime = 0.0f;
            isFlat = false;
            this.disable();//disable
            //System.out.println("Disabled and unflat");
        }

    }
}
