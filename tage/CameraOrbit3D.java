package tage;

import net.java.games.input.Component;
//import net.java.games.input.Controller;
import net.java.games.input.Event;
import org.joml.Vector3f;
//import tage.input.IInputManager;
import tage.input.InputManager;
import tage.input.action.AbstractInputAction;

/**
 * A CameraOrbit3D is a camera operator that uses spherical coordinates to controls a specific camera's location,
 * and angle looking at a specified GameObject
 * @author John Wishek
 */
public class CameraOrbit3D {
    //private Engine engine;
    private Camera camera;
    private GameObject player;
    private float cameraAzimuth;
    private float cameraElevation;
    private float cameraRadius;

    /** Creates a CameraOrbit3D with a Camera, GameObject, InputManager and Engine as specified as well it takes
     * the InputManager to set up inputs and then updates camera position to the default settings */
    public CameraOrbit3D(Camera cam, GameObject pl, InputManager im, Engine e)
    {
        //engine = e;
        camera = cam;
        player = pl;
        cameraAzimuth = 0.0f;
        cameraElevation = 20.0f;
        cameraRadius = 3.0f;
        setupInputs(im);
        updateCameraPosition();
    }

    /** This is called by the constructor using the Input Manager to define inputs for keyboards and game pads*/
    private void setupInputs(InputManager im) { //findout what sting is gamepad vs keyboard
        OrbitAzimuthAction      azmAction = new OrbitAzimuthAction(); //RX left/right
        OrbitElevationAction    elevAction = new OrbitElevationAction();//RY up/down
        OrbitRadiusAction       radiAction = new OrbitRadiusAction(); //Left/right bummpers z\x Can i assign this action to two different buttons if the class can determine the input

        im.associateActionWithAllKeyboards(Component.Identifier.Key.RIGHT, azmAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateActionWithAllKeyboards(Component.Identifier.Key.LEFT, azmAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateActionWithAllKeyboards(Component.Identifier.Key.UP, elevAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateActionWithAllKeyboards(Component.Identifier.Key.DOWN, elevAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateActionWithAllKeyboards(Component.Identifier.Key.Z, radiAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateActionWithAllKeyboards(Component.Identifier.Key.X, radiAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        //Controller Camera
        im.associateActionWithAllGamepads(Component.Identifier.Axis.RX, azmAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateActionWithAllGamepads(Component.Identifier.Axis.RY, elevAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateActionWithAllGamepads(Component.Identifier.Button._4, radiAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateActionWithAllGamepads(Component.Identifier.Button._5, radiAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    }

    /** This function is called by the constructor for the initial camera position and is called by the inner classes
     * depending on the changes made to the camera position*/
    public void updateCameraPosition() {
        Vector3f playerRot = player.getWorldForwardVector();
        double playerAngle = Math.toDegrees((double)playerRot.angleSigned(new Vector3f(0,0,-1),new Vector3f(0,1,0)));
        float totalAz = cameraAzimuth - (float)playerAngle;// ?? use?
        double theta = Math.toRadians(totalAz);
        double phi = Math.toRadians(cameraElevation);
        float x = cameraRadius * (float)(Math.cos(phi) * Math.sin(theta));
        float y = cameraRadius * (float)(Math.sin(phi));
        float z = cameraRadius * (float)(Math.cos(phi) * Math.cos(theta));
        if(y < 0.0f)
            y = 0.0f;
        camera.setLocation(new Vector3f(x,y,z).add(player.getWorldLocation()));
        camera.lookAt(player);
    }

    /** This class is used as an action input manager that determines the change in the cameras Azimuth angle */
    private class OrbitAzimuthAction extends AbstractInputAction {
        @Override
        public void performAction(float time, Event evt) {
            float rotAmount;
            String direction = evt.getComponent().toString();
            //System.out.println(direction);
            if(evt.getValue() < -0.2 || direction.equals("Left"))
                rotAmount = -0.2f;
            else {
                if(evt.getValue() > 0.2 || direction.equals("Right"))
                    rotAmount = 0.2f;
                else
                    rotAmount = 0.0f;
            }
            cameraAzimuth += rotAmount;
            cameraAzimuth = cameraAzimuth % 360;
            updateCameraPosition();
        }
    }

    /** This class is used as an action input manager that determines the change in the cameras Elevation angle*/
    private class OrbitElevationAction extends AbstractInputAction{
        @Override
        public void performAction(float time, Event evt) {
            float rotAmount;
            String direction = evt.getComponent().toString();
            if(evt.getValue() < -0.2 || direction.equals("Up"))
                rotAmount = 0.2f;
            else {
                if(evt.getValue() > 0.2 || direction.equals("Down"))
                    rotAmount = -0.2f;
                else
                    rotAmount = 0.0f;
            }
            cameraElevation += rotAmount;
            cameraElevation = cameraElevation % 360;
            updateCameraPosition();
        }
    }

    /** This class is used as an action input manager that determines the change in the cameras radius distance*/
    private class OrbitRadiusAction extends AbstractInputAction {
        @Override
        public void performAction(float time, Event evt) {
            //String componet = evt.getComponent().toString();
            // Button 4 = Left Bumper Button 5 = Right Bumper
            //System.out.println("radius action" + componet);
            float zoomAmt;
            String direction = evt.getComponent().toString();
            if (direction.equals("Button 4") || direction.equals("Z"))
                zoomAmt = -0.1f;
            else {
                if(direction.equals("Button 5") || direction.equals("X"))
                    zoomAmt = 0.1f;
                else
                    zoomAmt = 0.0f;
            }
            cameraRadius += zoomAmt;
            updateCameraPosition();
        }
    }
}
