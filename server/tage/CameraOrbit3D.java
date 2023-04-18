package tage;

import org.joml.*;

import tage.input.*;
import tage.input.action.*;
import net.java.games.input.*;
import net.java.games.input.Component.Identifier.*;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;

import java.lang.Math;

/**
 * A CameraOrbit3D is a camera operator that uses spherical coordinates to
 * controls a specific camera's location,
 * and angle looking at a specified GameObject
 * 
 * @author John Wishek
 */
public class CameraOrbit3D {
    private InputManager im;
    private Engine engine;
    private Camera camera; // the camera being controlled
    private GameObject avatar; // the target avatar the camera looks at
    private float cameraAzimuth; // rotation around target Y axis
    private float cameraElevation; // elevation of camera above target
    private float cameraRadius; // distance between camera and target

    /**
     * Creates a CameraOrbit3D with a Camera, GameObject, InputManager and Engine as
     * specified as well it takes
     * the InputManager to set up inputs and then updates camera position to the
     * default settings
     */
    public CameraOrbit3D(Camera cam, GameObject av, String gpName, Engine e) {
        engine = e;
        camera = cam;
        avatar = av;
        cameraAzimuth = 180.0f; // start BEHIND and ABOVE the target
        cameraElevation = 20.0f; // elevation is in degrees
        cameraRadius = 5.0f; // distance from camera to avatar
        setupInputs(gpName);
        updateCameraPosition();
    }

    /**
     * This is called by the constructor using the Input Manager to define inputs
     * for keyboards and game pads
     */
    private void setupInputs(String gp) {
        OrbitAzimuthAction azmAction = new OrbitAzimuthAction();
        OrbitElevationAction elevAction = new OrbitElevationAction();
        OrbitRadiusAction zoomAction = new OrbitRadiusAction();
        im = engine.getInputManager();

        // if controller can be identified, use these inputs
        if (gp != null) {
            // move camera lef or right
            im.associateAction(gp, net.java.games.input.Component.Identifier.Axis.Z, azmAction,
                    InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            // move camera up or down
            im.associateAction(gp, net.java.games.input.Component.Identifier.Axis.RZ, elevAction,
                    InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            im.associateAction(gp, net.java.games.input.Component.Identifier.Button._4, zoomAction,
                    InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            im.associateAction(gp, net.java.games.input.Component.Identifier.Button._5, zoomAction,
                    InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        } else {
            System.out.print("** Notice: controller cannot be detected or may not be connected **\n");
            System.out.print("      - using keyboard controls instead\n");
            im.associateActionWithAllKeyboards(Component.Identifier.Key.RIGHT, azmAction,
                    InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            im.associateActionWithAllKeyboards(Component.Identifier.Key.LEFT, azmAction,
                    InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            im.associateActionWithAllKeyboards(Component.Identifier.Key.UP, elevAction,
                    InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            im.associateActionWithAllKeyboards(Component.Identifier.Key.DOWN, elevAction,
                    InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            im.associateActionWithAllKeyboards(Component.Identifier.Key.Z, zoomAction,
                    InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            im.associateActionWithAllKeyboards(Component.Identifier.Key.X, zoomAction,
                    InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        }
    }

    /**
     * This function is called by the constructor for the initial camera position
     * and is called by the inner classes
     * depending on the changes made to the camera position
     */
    public void updateCameraPosition() {
        Vector3f avatarRot = avatar.getWorldForwardVector();
        double avatarAngle = Math
                .toDegrees((double) avatarRot.angleSigned(new Vector3f(0, 0, -1), new Vector3f(0, 1, 0)));
        float totalAz = cameraAzimuth - (float) avatarAngle;
        double theta = Math.toRadians(cameraAzimuth);
        double phi = Math.toRadians(cameraElevation);
        float x = cameraRadius * (float) (Math.cos(phi) * Math.sin(theta));
        float y = cameraRadius * (float) (Math.sin(phi));
        float z = cameraRadius * (float) (Math.cos(phi) * Math.cos(theta));
        camera.setLocation(new Vector3f(x, y, z).add(avatar.getWorldLocation()));
        camera.lookAt(avatar);
    }

    /**
     * This class is used as an action input manager that determines the change in
     * the cameras Azimuth angle (left/right)
     */
    private class OrbitAzimuthAction extends AbstractInputAction {
        public void performAction(float time, Event event) {
            float rotAmount;
            String direction = event.getComponent().toString();
            float keyValue = event.getValue();

            if (keyValue > -.2 && keyValue < .2)
                return; // deadzone

            if (event.getValue() < -0.5 || direction.equals("Left")) {
                rotAmount = 0.2f;
            } else {
                if (event.getValue() > 0.5 || direction.equals("Right")) {
                    rotAmount = -0.2f;
                } else {
                    rotAmount = 0.0f;
                }
            }
            cameraAzimuth += rotAmount;
            cameraAzimuth = cameraAzimuth % 360;
            updateCameraPosition();
        }
    }

    /**
     * This class is used as an action input manager that determines the change in
     * the cameras Elevation angle (up/down)
     */
    private class OrbitElevationAction extends AbstractInputAction {
        public void performAction(float time, Event event) {
            float elevateAmount;
            String direction = event.getComponent().toString();
            float keyValue = event.getValue();

            if (keyValue > -.2 && keyValue < .2)
                return; // deadzone

            if (event.getValue() < -0.2 || direction.equals("Up")) {
                elevateAmount = -0.2f;
            } else {
                if (event.getValue() > 0.2 || direction.equals("Down")) {
                    elevateAmount = 0.2f;
                } else {
                    elevateAmount = 0.0f;
                }
            }

            // stop camera from moving past the point directly above dolphin
            // or below the ground
            if (cameraElevation + elevateAmount >= 90.0f) {
                elevateAmount = 0.0f;
            } else if (cameraElevation + elevateAmount <= 1) {
                elevateAmount = 0.0f;
            }

            cameraElevation += elevateAmount;
            cameraElevation = cameraElevation % 360;
            updateCameraPosition();
        }
    }

    /**
     * This class is used as an action input manager that determines the change in
     * the cameras radius distance (zoom in/out)
     */
    private class OrbitRadiusAction extends AbstractInputAction {
        public void performAction(float time, Event event) {
            float zoomAmount;
            String button = event.getComponent().toString();

            if (button.equals("Button 7") || button.equals("X")) {
                zoomAmount = 0.1f;
            } else {
                zoomAmount = -0.1f;
            }

            // stop camera from zooming too far out or in
            if (cameraRadius + zoomAmount >= 30 || cameraRadius + zoomAmount <= 3) {
                zoomAmount = 0.0f;
            }
            cameraRadius += zoomAmount;
            updateCameraPosition();
        }
    }
}
