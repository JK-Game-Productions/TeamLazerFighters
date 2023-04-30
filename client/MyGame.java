package client;

import org.joml.*;

import com.bulletphysics.collision.broadphase.Dispatcher;
import com.bulletphysics.collision.narrowphase.ManifoldPoint;
import com.bulletphysics.collision.narrowphase.PersistentManifold;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;

import tage.*;
import tage.input.*;
import tage.shapes.*;
import tage.input.action.*;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.Robot;
import java.awt.event.MouseEvent;
import java.util.Random;
import java.net.InetAddress;
import java.awt.AWTException;
import java.net.UnknownHostException;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.ScriptEngineManager;
import net.java.games.input.Component;
import tage.nodeControllers.FlattenController;
import tage.nodeControllers.RotationController;
import tage.physics.PhysicsEngine;
import tage.physics.PhysicsEngineFactory;
import tage.physics.PhysicsObject;
import tage.physics.JBullet.JBulletPhysicsEngine;
import tage.physics.JBullet.JBulletPhysicsObject;
import tage.networking.IGameConnection.ProtocolType;

public class MyGame extends VariableFrameRateGame {

	// Static Variables
	private static Engine engine;
	private static Camera camMain;
	private static Random rand = new Random();

	// Tage Class Variables
	private Light light1;
	private InputManager im;
	private GhostManager gm;
	private PhysicsEngine ps;
	private Robot robot;
	private File scriptFile1;
	private String serverAddress;
	private CameraOrbit3D orbitCam;
	private Vector3f lastCamLocation;
	private ProtocolClient protClient;
	private Vector<GameObject> objects;
	private NodeController rc1, rc2, rc3, rc4, fc;

	// network & script variables
	private ScriptEngine jsEngine;
	private ProtocolType serverProtocol;

	// Basic Variables
	private int width;
	private int score = 0;
	private int serverPort;
	private float vals[] = new float[16];
	private int lakeIslands;
	private boolean isRecentering;
	private boolean paused = false;
	private boolean endGame = false;
	private float trailLength = -2.0f;
	private float moveSpeed = 3.0f;
	private boolean mouseVisible = true;
	private boolean lazergunAimed = false;
	private boolean isClientConnected = false;
	private float distToP1, distToP2, distToP3, distToP4;
	private double lastFrameTime, currFrameTime, elapsTime, frameDiff;
	private float curMouseX, curMouseY, prevMouseX, prevMouseY, centerX, centerY;
	// private int fluffyClouds;

	// object variables
	private GameObject avatar, lazergun, prize1, prize2, prize3, prize4, ground, x, y, z;
	private ObjShape ghostS, avatarS, lazergunS, prize1S, prize2S, prize3S, prize4S, linxS, linyS, linzS, terrS;
	private TextureImage ghostT, avatartx, lazerguntx, johntx, p1tx, p2tx, p4tx, groundtx, river;
	private PhysicsObject groundP, prize1P, avatarP;
	private boolean cameraSetUp = false;

	// ----------------------------------------------------------------------------

	public MyGame(String serverAddress, int serverPort, String protocol) {
		super();

		// Script Engine initializer
		ScriptEngineManager factory = new ScriptEngineManager();
		jsEngine = factory.getEngineByName("js");

		// ghost manager and server initialization
		gm = new GhostManager(this);
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
		if (protocol.toUpperCase().compareTo("TCP") == 0)
			this.serverProtocol = ProtocolType.TCP;
		else
			this.serverProtocol = ProtocolType.UDP;
	}

	// ----------------------------------------------------------------------------

	public static void main(String[] args) {
		MyGame game = new MyGame(args[0], Integer.parseInt(args[1]), args[2]);
		engine = new Engine(game);
		game.initializeSystem();
		game.game_loop();
	}

	// ----------------------------------------------------------------------------

	// VariableFrameRate Game Overrides
	@Override
	public void loadShapes() {
		avatarS = new ImportedModel("man4.obj");
		ghostS = avatarS;

		lazergunS = new ImportedModel("lazergun.obj");
		prize1S = new Torus();
		prize2S = new Cube();
		prize3S = new Sphere();
		prize4S = new HexBlock();
		linxS = new Line(new Vector3f(0f, 0f, 0f), new Vector3f(50f, 0f, 0f));
		linyS = new Line(new Vector3f(0f, 0f, 0f), new Vector3f(0f, 50f, 0f));
		linzS = new Line(new Vector3f(0f, 0f, 0f), new Vector3f(0f, 0f, 50f));
		terrS = new TerrainPlane(1000);
	}

	@Override
	public void loadTextures() {
		avatartx = new TextureImage("man4.png");
		ghostT = avatartx;

		lazerguntx = new TextureImage("lazergun.png");
		p2tx = new TextureImage("gold_energy.jpg");
		p1tx = new TextureImage("tex_Water.jpg");
		p4tx = new TextureImage("rooffrance.jpg");
		johntx = new TextureImage("galt_cow.jpg");
		groundtx = new TextureImage("brown_mud_leaves_01_diff_1k.jpg");
		river = new TextureImage("river.jpg");
	}

	@Override
	public void buildObjects() {
		Matrix4f initialTranslation, initialScale;
		jsEngine.put("rand", rand);
		scriptFile1 = new File("assets/scripts/RandomTranslation.js");

		// build the ground
		ground = new GameObject(GameObject.root(), terrS, groundtx);
		ground.setLocalTranslation(new Matrix4f().translation(0f, 0, 0f));
		ground.setLocalScale((new Matrix4f()).scaling(500.0f, 50.0f, 500.0f));
		ground.getRenderStates().setTiling(1);
		ground.setHeightMap(river);

		// build avatar in the center of the window
		avatar = new GameObject(GameObject.root(), avatarS, avatartx);
		avatar.setLocalTranslation((new Matrix4f()).translation(0f, 4f, 0f));
		avatar.setLocalScale((new Matrix4f()).scaling(0.33f));

		// build lazergun object
		lazergun = new GameObject(GameObject.root(), lazergunS, lazerguntx);
		lazergun.setLocalTranslation((new Matrix4f()).translation(4f, 4f, 4f));
		lazergun.setLocalScale((new Matrix4f()).scaling(0.20f));
		lazergun.setParent(avatar);
		lazergun.propagateRotation(true);
		lazergun.propagateTranslation(true);

		// build prize 1
		prize1 = new GameObject(GameObject.root(), prize1S, p1tx);
		jsEngine.put("object", prize1);
		this.runScript(scriptFile1);
		prize1.setLocalScale((new Matrix4f()).scaling(3.0f));
		prize1.getRenderStates().setTiling(1);

		// build prize 2
		prize2 = new GameObject(GameObject.root(), prize2S, p2tx);
		jsEngine.put("object", prize2);
		this.runScript(scriptFile1);
		prize2.setLocalScale((new Matrix4f()).scaling(2.0f));

		// build prize 3
		prize3 = new GameObject(GameObject.root(), prize3S, johntx);
		jsEngine.put("object", prize3);
		this.runScript(scriptFile1);
		prize3.setLocalScale((new Matrix4f()).scaling(3.5f, 2.0f, 3.5f));

		// build prize 4
		prize4 = new GameObject(GameObject.root(), prize4S, p4tx);
		jsEngine.put("object", prize4);
		this.runScript(scriptFile1);
		prize4.setLocalScale((new Matrix4f()).scaling(4.0f, 2.0f, 4.0f));

		// build world axes
		x = new GameObject(GameObject.root(), linxS);
		y = new GameObject(GameObject.root(), linyS);
		z = new GameObject(GameObject.root(), linzS);
		(x.getRenderStates()).setColor(new Vector3f(1f, 0f, 0f));
		(y.getRenderStates()).setColor(new Vector3f(0f, 1f, 0f));
		(z.getRenderStates()).setColor(new Vector3f(0f, 0f, 1f));

		// add objects to vector
		objects = new Vector<>();
		objects.add(avatar);
		objects.add(prize1);
		objects.add(prize2);
		objects.add(prize3);
		objects.add(prize4);
	}

	@Override
	public void initializeLights() {
		Light.setGlobalAmbient(0.5f, 0.5f, 0.5f);
		light1 = new Light();
		light1.setLocation(new Vector3f(0.0f, 50.0f, 0.0f));
		(engine.getSceneGraph()).addLight(light1);
	}

	@Override
	public void loadSkyBoxes() {
		// fluffyClouds = (engine.getSceneGraph()).loadCubeMap("fluffyClouds");
		lakeIslands = (engine.getSceneGraph()).loadCubeMap("lakeIslands");
		(engine.getSceneGraph()).setActiveSkyBoxTexture(lakeIslands);
		(engine.getSceneGraph()).setSkyBoxEnabled(true);
	}

	@Override
	public void createViewports() {
		(engine.getRenderSystem()).addViewport("LEFT", 0, 0, 1f, 1f);

		Viewport leftVp = (engine.getRenderSystem()).getViewport("LEFT");
		Camera leftCamera = leftVp.getCamera();

		leftCamera.setLocation(new Vector3f(-2, 0, 2));
		leftCamera.setU(new Vector3f(1, 0, 0));
		leftCamera.setV(new Vector3f(0, 1, 0));
		leftCamera.setN(new Vector3f(0, 0, -1));
	}

	@Override
	public void initializeGame() {

		// elapsTime = ((Double)(jsEngine.get("time")));

		// ----------------- set window size ----------------- //
		(engine.getRenderSystem()).setWindowDimensions(1920, 1080);

		// -------------------- variables -------------------- //
		score = 0;
		elapsTime = 0.0;
		im = engine.getInputManager();
		lastFrameTime = System.currentTimeMillis();
		currFrameTime = System.currentTimeMillis();

		// add a toggle function that can toggle between using
		// a regular camera and an orbit camera

		// ---------------- initialize camera ---------------- //
		camMain = (engine.getRenderSystem().getViewport("LEFT").getCamera());
		positionCameraBehindAvatar();

		// ------------------- orbit camera ------------------ //
		// String gpName = im.getFirstGamepadName();
		// orbitCam = new CameraOrbit3D(camMain, avatar, gpName, engine);

		// --------------- initialize network ---------------- //
		setupNetworking();

		// -------------------- game logic ------------------- //
		distToP1 = distanceToObject(prize1);
		distToP2 = distanceToObject(prize2);
		distToP3 = distanceToObject(prize3);
		distToP4 = distanceToObject(prize4);

		// Rotational Controllers
		rc1 = new RotationController(engine, new Vector3f(0, 1, 0), 0.001f);
		rc1.addTarget(prize1);
		rc2 = new RotationController(engine, new Vector3f(0, 1, 0), 0.001f);
		rc2.addTarget(prize2);
		rc3 = new RotationController(engine, new Vector3f(0, 1, 0), 0.001f);
		rc3.addTarget(prize3);
		rc4 = new RotationController(engine, new Vector3f(0, 1, 0), 0.001f);
		rc4.addTarget(prize4);

		(engine.getSceneGraph()).addNodeController(rc1);
		(engine.getSceneGraph()).addNodeController(rc2);
		(engine.getSceneGraph()).addNodeController(rc3);
		(engine.getSceneGraph()).addNodeController(rc4);

		// Flatten Controller
		fc = new FlattenController(avatar.getLocalScale().m11());
		fc.addTarget(avatar);
		(engine.getSceneGraph()).addNodeController(fc);

		// ------------------- Input Setup ------------------- //

		initMouseMode();

		AimAction aimAction = new AimAction(this);
		MoveAction moveAction = new MoveAction(this);
		PauseAction pauseAction = new PauseAction(this);
		ZoomCameraAction zoomAction = new ZoomCameraAction(this);
		ToggleMouseAction mouseAction = new ToggleMouseAction(this);

		// Keyboard Actions ---------------------------------------------------
		im.associateActionWithAllKeyboards(Component.Identifier.Key.R, aimAction,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		// im.associateActionWithAllKeyboards(Component.Identifier.Key.A,
		// turnAction,InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		// im.associateActionWithAllKeyboards(Component.Identifier.Key.D,
		// turnAction,InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(Component.Identifier.Key.W, moveAction,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(Component.Identifier.Key.S, moveAction,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(Component.Identifier.Key.A, moveAction,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(Component.Identifier.Key.D, moveAction,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(Component.Identifier.Key.COMMA, zoomAction,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(Component.Identifier.Key.PERIOD, zoomAction,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(Component.Identifier.Key.LALT, mouseAction,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.P, pauseAction,
				InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		im.associateActionWithAllKeyboards(Component.Identifier.Key.LSHIFT, moveAction,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

		// Gamepad Actions ----------------------------------------------------
		im.associateActionWithAllGamepads(Component.Identifier.Axis.X, moveAction,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllGamepads(Component.Identifier.Axis.Y, moveAction,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllGamepads(Component.Identifier.Axis.Z, zoomAction,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllGamepads(Component.Identifier.Button._6, mouseAction,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllGamepads(Component.Identifier.Button._7, pauseAction,
				InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		// add strafe

		// ---------------PHYSICS ENGINE---------------------------//
		// init physics engine
		String engine = "tage.physics.JBullet.JBulletPhysicsEngine";
		float[] gravity = { 0f, -10f, 0f };
		ps = PhysicsEngineFactory.createPhysicsEngine(engine);
		ps.initSystem();
		ps.setGravity(gravity);
		// creating physics world
		float mass = 1.0f;
		float up[] = { 0, 1, 0 };
		double[] tempTransform;
		// -----------------Physics Objects---------------------//
		Matrix4f translation = new Matrix4f(prize1.getLocalTranslation());
		tempTransform = toDoubleArray(translation.get(vals));
		prize1P = ps.addCapsuleObject(ps.nextUID(), mass, tempTransform, prize1.getScale(), prize1.getScale());
		prize1P.setBounciness(0.5f);
		prize1.setPhysicsObject(prize1P);
		/* 
		translation = new Matrix4f(avatar.getLocalTranslation());
		tempTransform = toDoubleArray(translation.get(vals));
		avatarP = ps.addSphereObject(ps.nextUID(), mass, tempTransform, 0.33f);
		//avatarP.setFriction(0f);
		avatar.setPhysicsObject(avatarP);
		*/
		translation = new Matrix4f(ground.getLocalTranslation());
		tempTransform = toDoubleArray(translation.get(vals));
		groundP = ps.addStaticPlaneObject(ps.nextUID(), tempTransform, up, 0.0f);
		groundP.setBounciness(1.0f);
		ground.setPhysicsObject(groundP);
	}

	@Override
	public void update() {
		lastFrameTime = currFrameTime;
		currFrameTime = System.currentTimeMillis();
		frameDiff = (currFrameTime - lastFrameTime) / 1000.0;
		width = (engine.getRenderSystem()).getWidth();

		// will need to find a way to make the map height
		// translate to the ghost avatars
		mapHeight(avatar);
		mapHeight(prize1);
		mapHeight(prize2);
		mapHeight(prize3);
		mapHeight(prize4);

		if (paused)
			im.update((float) elapsTime);
		if (!paused && !endGame) {
			elapsTime += frameDiff;
			im.update((float) elapsTime);
			// System.out.println(this.getMoveSpeed());

			// orbitCam.updateCameraPosition();
			positionCameraBehindAvatar();

			// update lazergun position and aim
			lazergun.applyParentRotationToPosition(true);
			if (lazergunAimed) {
				lazergun.setLocalTranslation(new Matrix4f().translation(-0.222f, 0.8f, 0.9f));
			} else {
				lazergun.setLocalTranslation(new Matrix4f().translation(-0.4f, 0.8f, 0.9f));
			}
			// setMoveSpeed(3.0f);
			setLazergunAim(false);

			// process the networking functions
			processNetworking((float) elapsTime);

			// show/hide mouse logic
			checkMouse();

			distToP1 = distanceToDolphin(prize1);
			distToP2 = distanceToDolphin(prize2);
			distToP3 = distanceToDolphin(prize3);
			distToP4 = distanceToDolphin(prize4);

			// Player logic
			/* 
			if (distToP1 <= prize1.getScale() && !prize1.isCollected()) {
				score++;
				prize1.collect();
				rc1.toggle();
				prize1.setParent(avatar);
				prize1.propagateRotation(false);
				prize1.setLocalScale(new Matrix4f().scaling(0.25f));
				prize1.applyParentRotationToPosition(true);
				prize1.setLocalTranslation(new Matrix4f().translation(0f, 0f, trailLength));
				trailLength += -1.5f;
			}
			*/
			if (distToP2 <= prize2.getScale() && !prize2.isCollected()) {
				score++;
				prize2.collect();
				rc2.toggle();
				prize2.setParent(avatar);
				prize2.propagateRotation(false);
				prize2.setLocalScale(new Matrix4f().scaling(0.25f));
				prize2.applyParentRotationToPosition(true);
				prize2.setLocalTranslation(new Matrix4f().translation(0f, 0f, trailLength));
				trailLength += -1.5f;
			}
			if (distToP3 <= prize3.getScale() && !prize3.isCollected()) {
				score++;
				prize3.collect();
				rc3.toggle();
				prize3.setParent(avatar);
				prize3.propagateRotation(false);
				prize3.setLocalScale(new Matrix4f().scaling(0.25f));
				prize3.applyParentRotationToPosition(true);
				prize3.setLocalTranslation(new Matrix4f().translation(0f, 0f, trailLength));
				trailLength += -1.5f;
			}
			if (distToP4 <= prize4.getScale() && !prize4.isCollected()) {
				score++;
				prize4.collect();
				rc4.toggle();
				prize4.setParent(avatar);
				prize4.propagateRotation(false);
				prize4.setLocalScale(new Matrix4f().scaling(0.25f));
				prize4.applyParentRotationToPosition(true);
				prize4.setLocalTranslation(new Matrix4f().translation(0f, 0f, trailLength));
				trailLength += -1.5f;
			}
		} // pause scope and end game cutoff

		// ---------------------PHYSICS LOGIC--------------------------//
		Matrix4f currentTranslation, currentRotation;
		// if(running){
			Matrix4f matrix = new Matrix4f();
			Matrix4f rotMatrix = new Matrix4f();
			AxisAngle4f aAngle = new AxisAngle4f();
			Matrix4f identityMatrix = new Matrix4f().identity();
		checkCollisions();
		ps.update((float) elapsTime);
		for (GameObject go : engine.getSceneGraph().getGameObjects()) {
			if (go.getPhysicsObject() != null) {
				matrix.getRotation(aAngle);
				matrix.set(toFloatArray(go.getPhysicsObject().getTransform()));
				identityMatrix.set(3, 0, matrix.m30());
				identityMatrix.set(3, 1, matrix.m31());
				identityMatrix.set(3, 2, matrix.m32());
				go.setLocalTranslation(identityMatrix);
				rotMatrix.rotation(aAngle);
			}
		}
		// } If condition for running physics with scripts

		// build and set HUD
		/*
		 * time
		 * int elapsTimeSec = Math.round((float)elapsTime);
		 * int minutes = (elapsTimeSec/60) % 60;
		 * int seconds = elapsTimeSec % 60;
		 * String secondsStr;
		 * if(seconds <= 9)
		 * secondsStr = "0" + seconds;
		 * else
		 * secondsStr = Integer.toString(seconds);
		 * String clock = "Time = " + Integer.toString(minutes) + ":" + secondsStr;
		 * Vector3f clockcolor = new Vector3f(1,1,1);
		 */

		// update HUD variables
		String scoreStr = "Score: " + Integer.toString(score);
		String dolLocStr = "X: " + avatar.getWorldLocation().x() + "  Z: " + avatar.getWorldLocation().z();
		Vector3f dolLocColor = new Vector3f(1, 1, 1);
		Vector3f scoreColor = new Vector3f(0, 1, 0);
		String winStr = "You Have Beaten The Blob";
		Vector3f winColor = new Vector3f(0, 1, 0);
		if (prize1.isCollected() && prize2.isCollected() && prize3.isCollected() && prize4.isCollected()) {
			(engine.getHUDmanager()).setHUD1(winStr, winColor, (int) (width * 0.75f), 15);
			endGame = true;
		} else
			(engine.getHUDmanager()).setHUD1(dolLocStr, dolLocColor, (int) (width * 0.75f), 15);
		(engine.getHUDmanager()).setHUD2(scoreStr, scoreColor, 15, 15);

	} // END Update
		// END VariableFrameRate Game Overrides

	// --------------------- MOUSE MANAGEMENT -------------------- //
	private void initMouseMode() {
		RenderSystem rs = engine.getRenderSystem();
		Viewport vw = rs.getViewport("LEFT");
		float left = vw.getActualLeft();
		float bottom = vw.getActualBottom();
		float width = vw.getActualWidth();
		float height = vw.getActualHeight();

		centerX = (int) (left + width / 2);
		centerY = (int) (bottom - height / 2);

		isRecentering = false;

		try {
			robot = new Robot();
		} catch (AWTException ex) {
			throw new RuntimeException("couldn't create robot");
		}

		recenterMouse();
		prevMouseX = centerX;
		prevMouseY = centerY;

		// rs.imageUpdate(ch, 1, (int)centerX, (int)centerY, ch.getWidth(null),
		// ch.getHeight(null));

	}

	private void recenterMouse() {
		RenderSystem rs = engine.getRenderSystem();
		Viewport vw = rs.getViewport("LEFT");
		float left = vw.getActualLeft();
		float bottom = vw.getActualBottom();
		float width = vw.getActualWidth();
		float height = vw.getActualHeight();
		centerX = (int) (left + width / 2.0f);
		centerY = (int) (bottom - height / 2.0f);

		isRecentering = true;
		robot.mouseMove((int) centerX, (int) centerY);
	}

	@Override
	public void mouseMoved(java.awt.event.MouseEvent e) {
		if (isRecentering && centerX == e.getXOnScreen() && centerY == e.getYOnScreen()) {
			isRecentering = false;
		} else {
			curMouseX = e.getXOnScreen();
			curMouseY = e.getYOnScreen();
			float mouseDeltaX = prevMouseX - curMouseX;
			float mouseDeltaY = prevMouseY - curMouseY;

			avatar.gyaw(getFrameDiff(), mouseDeltaX);
			// camMain.yaw(mouseDeltaX);
			avatar.pitch(getFrameDiff(), mouseDeltaY);
			prevMouseX = curMouseX;
			prevMouseY = curMouseY;

			recenterMouse();
			prevMouseX = centerX;
			prevMouseY = centerY;
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// System.out.println(e.getButton());
	}

	@Override
	public void mousePressed(java.awt.event.MouseEvent e) {
		System.out.println(e.getButton());
	}
	// END Mouse Management

	// -------------------------- NETWORKING SECTION --------------------------

	private void setupNetworking() {
		isClientConnected = false;
		try {
			protClient = new ProtocolClient(InetAddress.getByName(serverAddress), serverPort, serverProtocol, this);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (protClient == null) {
			System.out.println("missing protocol host");
		} else { // Send the initial join message with a unique identifier for this client
			System.out.println("sending join message to protocol host");
			protClient.sendJoinMessage();
		}
	}

	// network custom functions ------------
	protected void processNetworking(float elapsTime) { // Process packets received by the client from the server
		if (protClient != null)
			protClient.processPackets();
	}

	// network getters ---------------------
	public Vector3f getPlayerPosition() {
		return avatar.getWorldLocation();
	}

	public ProtocolClient getProtocolClient() {
		return protClient;
	}

	// network setters ---------------------
	public void setIsConnected(boolean value) {
		this.isClientConnected = value;
	}

	private class SendCloseConnectionPacketAction extends AbstractInputAction {
		@Override
		public void performAction(float time, net.java.games.input.Event evt) {
			if (protClient != null && isClientConnected == true) {
				protClient.sendByeMessage();
			}
		}
	}// END Networking Section

	// --------------------------- CUSTOM FUNCTIONS ---------------------------

	/**
	 * returns distance of any inputted GameObject to the current/main camera
	 * 
	 * @deprecated current version of game no longer needs camera distance to
	 *             objects
	 */
	@Deprecated
	public float distanceToObject(GameObject obj) {
		return camMain.getLocation().distance(obj.getWorldLocation());
	}

	/** Returns distance of a GameObject to the dolphin player */
	public float distanceToDolphin(GameObject obj) {
		return avatar.getWorldLocation().distance(obj.getWorldLocation());
	}

	/** returns distance from a vector3f point to a GameObject */
	public float distanceTo(Vector3f newPos, GameObject obj) {
		return newPos.distance(obj.getWorldLocation());
	}

	/** returns distance from one GameObject to another GameObject */
	public float distanceTo(GameObject obj1, GameObject obj2) {
		return obj1.getWorldLocation().distance(obj2.getWorldLocation());
	}

	/**
	 * checks to see if the camera has moved since last update()
	 * 
	 * @deprecated
	 */
	@Deprecated
	public boolean changedDistance(Camera cam) {
		if (cam.getLocation().equals(lastCamLocation, 0))
			return false;
		return true;
	}

	private void mapHeight(GameObject object) {
		Vector3f loc = object.getWorldLocation();
		float height = ground.getHeight(loc.x(), loc.z());
		object.setLocalLocation(new Vector3f(loc.x(), (height + 2.0f), loc.z()));
	}

	private void positionCameraBehindAvatar() {
		// Matrix4f w = avatar.getWorldTranslation();
		Vector4f u = new Vector4f(-1f, 0f, 0f, 1f);
		Vector4f v = new Vector4f(0f, 1f, 0f, 1f);
		Vector4f n = new Vector4f(0f, 0f, 1f, 1f);
		// Vector3f position = new Vector3f(w.m30(), w.m31(), w.m32());
		Vector3f location = avatar.getWorldLocation();
		// System.out.println("position:" + position);
		// System.out.println("location" + location);
		/*
		 * u.mul(avatar.getWorldRotation());
		 * v.mul(avatar.getWorldRotation());
		 * n.mul(avatar.getWorldRotation());
		 */
		camMain.setU(avatar.getWorldRightVector());
		camMain.setV(avatar.getWorldUpVector());
		camMain.setN(avatar.getWorldForwardVector());
		// location.add(n.x() * 0.3f, n.y() * 0.3f, n.z() * 0.3f);
		// location.add(v.x() * .95f, v.y() * .95f, v.z() * .95f);
		location.add(camMain.getV().mul(.95f));
		location.add(camMain.getN().mul(.3f));
		cameraSetUp = true;
		if (!cameraSetUp) {
			camMain.setU(new Vector3f(u.x(), u.y(), u.z()));
			camMain.setV(new Vector3f(v.x(), v.y(), v.z()));
			camMain.setN(new Vector3f(n.x(), n.y(), n.z()));
			cameraSetUp = true;
		}
		camMain.setLocation(location);
	}

	// checks if mouse is hidden or shown and sets the cursor icon
	private void checkMouse() {
		RenderSystem rs = engine.getRenderSystem();
		Toolkit tk = Toolkit.getDefaultToolkit();
		Canvas canvas = rs.getGLCanvas();

		Cursor clear = tk.createCustomCursor(tk.getImage(""), new Point(), "ClearCursor");
		Cursor crosshair = tk.createCustomCursor(tk.getImage("./assets/textures/Blue-Crosshair-1.png"), new Point(),
				"Crosshair");

		if (!mouseVisible) {
			canvas.setCursor(clear);
		} else {
			// set mouse back to default
			canvas.setCursor(null);
		}
		setMouseVisible(false);
	}

	private void checkCollisions() {
		DynamicsWorld dw;
		Dispatcher dist;
		PersistentManifold pm;
		RigidBody object1, object2;
		ManifoldPoint contactPoint;

		dw = ((JBulletPhysicsEngine) ps).getDynamicsWorld();
		dist = dw.getDispatcher();
		int mCount = dist.getNumManifolds();
		for (int i = 0; i < mCount; i++) {
			pm = dist.getManifoldByIndexInternal(i);
			object1 = (RigidBody) pm.getBody0();
			object2 = (RigidBody) pm.getBody1();
			JBulletPhysicsObject obj1 = JBulletPhysicsObject.getJBulletPhysicsObject(object1);
			JBulletPhysicsObject obj2 = JBulletPhysicsObject.getJBulletPhysicsObject(object2);
			for (int j = 0; j < pm.getNumContacts(); j++) {
				contactPoint = pm.getContactPoint(j);
				if (contactPoint.getDistance() < 0.0f) {
					//                Collison logic
					//System.out.println("hit between: " + obj1 + " and " + obj2);
					break;
				}
			}
		}
	}

	private float[] toFloatArray(double[] arr) {
		if (arr == null)
			return null;
		int n = arr.length;
		float[] ret = new float[n];
		for (int i = 0; i < n; i++) {
			ret[i] = (float) arr[i];
		}
		return ret;
	}

	private double[] toDoubleArray(float[] arr) {
		if (arr == null)
			return null;
		int n = arr.length;
		double[] ret = new double[n];
		for (int i = 0; i < n; i++) {
			ret[i] = (double) arr[i];
		}
		return ret;
	}
	// END Custom Functions

	// -------------------------- GETTERS & SETTERS --------------------------

	public GameObject getAvatar() {
		return avatar;
	}

	public Engine getEngine() {
		return engine;
	}

	public static Camera getMainCamera() {
		return camMain;
	}

	public ObjShape getGhostShape() {
		return ghostS;
	}

	public TextureImage getGhostTexture() {
		return ghostT;
	}

	public GhostManager getGhostManager() {
		return gm;
	}

	public void togglePause() {
		paused = !paused;
	}

	public float getFrameDiff() {
		return (float) frameDiff;
	}

	public boolean getLazergunAim() {
		return lazergunAimed;
	}

	public void setLazergunAim(boolean newValue) {
		lazergunAimed = (newValue);
	}

	public float getMoveSpeed() {
		return moveSpeed;
	}

	public void setMoveSpeed(float moveSpeed) {
		this.moveSpeed = moveSpeed;
	}

	public void setMouseVisible(boolean newValue) {
		mouseVisible = newValue;
	}// END Getters & Setters

	// -------------------------- SCRIPTING SECTION --------------------------
	private void runScript(File scriptFile) {
		try {
			FileReader fileReader = new FileReader(scriptFile);
			jsEngine.eval(fileReader);
			fileReader.close();
		} catch (FileNotFoundException e1) {
			System.out.println(scriptFile + " not found " + e1);
		} catch (IOException e2) {
			System.out.println("IO problem with " + scriptFile + e2);
		} catch (ScriptException e3) {
			System.out.println("ScriptException in " + scriptFile + e3);
		} catch (NullPointerException e4) {
			System.out.println("Null ptr exception reading " + scriptFile + e4);
		}
	}
}