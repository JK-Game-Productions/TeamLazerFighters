package client;

import org.joml.*;
import org.joml.Math;

import com.bulletphysics.collision.broadphase.Dispatcher;
import com.bulletphysics.collision.narrowphase.ManifoldPoint;
import com.bulletphysics.collision.narrowphase.PersistentManifold;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;

import tage.*;
import tage.input.*;
import tage.shapes.*;
import tage.audio.Sound;
import tage.audio.SoundType;
import tage.input.action.*;
import tage.audio.AudioManagerFactory;
import tage.audio.AudioResource;
import tage.audio.AudioResourceType;
import tage.audio.IAudioManager;
import tage.audio.joal.JOALAudioManager;
import tage.audio.joal.*;
import tage.audio.*;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.Robot;
import java.awt.event.KeyEvent;
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
	private Image crosshair;
	private InputManager im;
	private GhostManager gm;
	private Robot robot;
	private File scriptFile1;
	private String serverAddress;
	private CameraOrbit3D orbitCam;
	private Vector3f lastCamLocation;
	private ProtocolClient protClient;
	private Vector<GameObject> objects;
	private Sound laserSound, walkingSound, runningSound, voiceline1;

	// network & script variables
	private PhysicsEngine ps;
	private ScriptEngine jsEngine;
	private IAudioManager audioMgr;
	private ProtocolType serverProtocol;

	// Basic Variables
	private int width;
	private int score = 0;
	private int serverPort;
	private int lakeIslands;
	private boolean isRecentering;
	private float moveSpeed = 3.0f;
	private boolean paused = false;
	private boolean endGame = false;
	private boolean isRunning = false;
	private boolean isWalking = false;
	private boolean cameraSetUp = false;
	private boolean mouseVisible = true;
	private float vals[] = new float[16];
	private boolean lazergunAimed = false;
	private boolean isClientConnected = false;
	private double lastFrameTime, currFrameTime, elapsTime, frameDiff;
	private float curMouseX, curMouseY, prevMouseX, prevMouseY, centerX, centerY;
	// private int fluffyClouds;

	// object variables
	private GameObject avatar, NPC, lazergun, lazergun1, prize1, prize2, prize3, prize4, ground, x, y, z, npc, lazer1;
	private ObjShape ghostS, avatarS, NPCs, lazergunS, prize1S, prize2S, prize3S, prize4S, linxS, linyS, linzS, terrS,
			lazerS;
	private TextureImage ghostT, avatartx, NPCtx, lazerguntx, johntx, p1tx, p2tx, p4tx, groundtx, river, lazerT;
	private PhysicsObject groundP, prize1P, avatarP, prize2P, prize1GroundP, avatarGroundP, prize2GroundP, npcP,
			npcGroundP, lazerP, lazerGroundP;
	private boolean placedOnMap = false;

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
		NPCs = avatarS;

		lazergunS = new ImportedModel("lazergun.obj");
		lazerS = new Sphere();
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
		lazerT = new TextureImage("lazerbeam.png");
		NPCtx = avatartx;

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

		npc = new GameObject(GameObject.root(), avatarS, avatartx);
		npc.setLocalTranslation(new Matrix4f().translation(80.0f, 0.0f, 20.0f));
		npc.setLocalRotation(new Matrix4f().rotateY((float) Math.PI));
		npc.setLocalScale(new Matrix4f().scale(.50f));

		lazergun1 = new GameObject(GameObject.root(), lazergunS, lazerguntx);
		lazergun1.setLocalTranslation((new Matrix4f()).translation(80.00f, 41.00f, 15.00f));
		lazergun1.setLocalScale((new Matrix4f()).scaling(0.20f));

		// build avatar in the center of the window
		avatar = new GameObject(GameObject.root(), avatarS, avatartx);
		avatar.setLocalTranslation((new Matrix4f()).translation(80f, 0f, 12.0f));
		avatar.setLocalScale((new Matrix4f()).scaling(.43f));

		// NPC
		NPC = new GameObject(GameObject.root(), NPCs, NPCtx);
		NPC.setLocalTranslation((new Matrix4f()).translation(30f, 5f, 24f));
		NPC.setLocalScale((new Matrix4f()).scaling(0.43f));

		// NPC
		NPC = new GameObject(GameObject.root(), NPCs, NPCtx);
		NPC.setLocalTranslation((new Matrix4f()).translation(30f, 5f, 24f));
		NPC.setLocalScale((new Matrix4f()).scaling(0.43f));

		// build lazergun object
		lazergun = new GameObject(GameObject.root(), lazergunS, lazerguntx);
		lazergun.setLocalTranslation((new Matrix4f()).translation(4f, 4f, 4f));
		lazergun.setLocalScale((new Matrix4f()).scaling(0.15f));
		lazergun.setParent(avatar);
		lazergun.propagateRotation(true);
		lazergun.propagateTranslation(true);

		lazer1 = new GameObject(GameObject.root(), lazerS, lazerT);
		lazer1.setLocalTranslation(new Matrix4f().translation(80.67f, 40.955f, 15.18f));
		lazer1.setLocalScale(new Matrix4f().scale(0.02f, 0.02f, 0.30f));
		lazer1.setParent(lazergun);
		lazer1.propagateRotation(true);
		lazer1.propagateScale(false);
		// lazer1.propagateTranslation(true);
		lazer1.setLocalTranslation(new Matrix4f().translation(.67f, -.045f, .18f));

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

		// --------------- initialize custom functions ---------------- //
		setupNetworking();
		initMouseMode();
		initAudio();

		// -------------------- game logic ------------------- //

		// ------------------- Input Setup ------------------- //

		AimAction aimAction = new AimAction(this);
		MoveAction moveAction = new MoveAction(this);
		PauseAction pauseAction = new PauseAction(this);
		ZoomCameraAction zoomAction = new ZoomCameraAction(this);
		ToggleMouseAction mouseAction = new ToggleMouseAction(this);

		// Keyboard Actions ---------------------------------------------------
		im.associateActionWithAllKeyboards(Component.Identifier.Key.R, aimAction,
				InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
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
		im.associateActionWithAllKeyboards(Component.Identifier.Key.TAB, pauseAction,
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

		// --------------------- Physics Engine --------------------- //

		// init physics engine
		String engine = "tage.physics.JBullet.JBulletPhysicsEngine";
		float[] gravity = { 0f, -10f, 0f };
		ps = PhysicsEngineFactory.createPhysicsEngine(engine);
		ps.initSystem();
		ps.setGravity(gravity);
		// creating physics world
		float mass = 1.0f;
		float massless = 0.0f;
		float up[] = { 0, 1, 0 };
		float gsize[] = { 1f, 0.1f, 1f };
		float asize[] = { .33f, .33f, .33f };
		float psize[] = { 4.0f, 4.0f, 4.0f };
		float nsize[] = { 1.0f, 1.0f, 1.0f };
		double[] tempTransform;

		// --------------------- Physics Objects --------------------- //
		Matrix4f translation = new Matrix4f(prize1.getLocalTranslation());
		tempTransform = toDoubleArray(translation.get(vals));
		prize1P = ps.addSphereObject(ps.nextUID(), massless, tempTransform, 3.0f);
		prize1P.setBounciness(0.5f);
		prize1.setPhysicsObject(prize1P);

		translation = new Matrix4f().translation(0, 0, 0);
		tempTransform = toDoubleArray(translation.get(vals));
		prize1GroundP = ps.addBoxObject(ps.nextUID(), massless, tempTransform, gsize);
		prize1GroundP.setFriction(0.5f);
		prize1GroundP.setBounciness(0.1f);

		/*
		 * translation = new Matrix4f(avatar.getLocalTranslation());
		 * tempTransform = toDoubleArray(translation.get(vals));
		 * avatarP = ps.addBoxObject(ps.nextUID(), mass, tempTransform, asize);
		 * //avatarP.setFriction(0f);
		 * avatar.setPhysicsObject(avatarP);
		 * 
		 */
		translation = new Matrix4f().translation(0, 0, 0);
		tempTransform = toDoubleArray(translation.get(vals));
		avatarGroundP = ps.addBoxObject(ps.nextUID(), massless, tempTransform, gsize);

		translation = new Matrix4f(prize2.getLocalTranslation());
		tempTransform = toDoubleArray(translation.get(vals));
		prize2P = ps.addBoxObject(ps.nextUID(), massless, tempTransform, psize);
		prize2P.setFriction(0.5f);
		prize2P.setBounciness(1.0f);
		prize2.setPhysicsObject(prize2P);

		translation = new Matrix4f().translation(0, 0, 0);
		tempTransform = toDoubleArray(translation.get(vals));
		prize2GroundP = ps.addBoxObject(ps.nextUID(), massless, tempTransform, gsize);

		translation = new Matrix4f(npc.getLocalTranslation());
		tempTransform = toDoubleArray(translation.get(vals));
		npcP = ps.addBoxObject(ps.nextUID(), mass, tempTransform, nsize);
		npcP.setDamping(0f, 1f);
		npc.setPhysicsObject(npcP);

		translation = new Matrix4f().translation(0, 0, 0);
		tempTransform = toDoubleArray(translation.get(vals));
		npcGroundP = ps.addBoxObject(ps.nextUID(), massless, tempTransform, gsize);

		translation = new Matrix4f(lazer1.getLocalTranslation());
		tempTransform = toDoubleArray(translation.get(vals));
		lazerP = ps.addCapsuleObject(ps.nextUID(), mass - .99f, tempTransform, 0.30f, 0.02f);

		translation = new Matrix4f(ground.getLocalTranslation());
		tempTransform = toDoubleArray(translation.get(vals));
		groundP = ps.addStaticPlaneObject(ps.nextUID(), tempTransform, up, 0.0f);
		groundP.setBounciness(0.1f);
		groundP.setFriction(1.0f);
		ground.setPhysicsObject(groundP);
	}

	// -------------------------- UPDATE -------------------------- //

	@Override
	public void update() {
		lastFrameTime = currFrameTime;
		currFrameTime = System.currentTimeMillis();
		frameDiff = (currFrameTime - lastFrameTime) / 1000.0;
		width = (engine.getRenderSystem()).getWidth();

		if (paused) {
			im.update((float) elapsTime);
		}
		if (!paused && !endGame) {

			elapsTime += frameDiff;
			im.update((float) elapsTime);

			// orbitCam.updateCameraPosition();
			positionCameraBehindAvatar();

			// update lazergun position and aim
			lazergun.applyParentRotationToPosition(true);
			// lazer1.applyParentRotationToPosition(true);
			if (lazergunAimed) {
				lazergun.setLocalTranslation(new Matrix4f().translation(-0.217f, 0.8f, 0.9f));
			} else {
				lazergun.setLocalTranslation(new Matrix4f().translation(-0.4f, 0.8f, 0.9f));
			}

			// update walking sound
			if (isWalking && (walkingSound.getIsPlaying() == false)) {
				walkingSound.play();
				walkingSound.resume();
			}
			if (!isWalking && (walkingSound.getIsPlaying() == true)) {
				walkingSound.pause();
			}
			setAvatarWalking(false);

			// update running sound
			if (isRunning && (runningSound.getIsPlaying() == false)) {
				runningSound.play();
				runningSound.resume();
			}
			if (!isRunning && (runningSound.getIsPlaying() == true)) {
				runningSound.pause();
			}
			setAvatarWalking(false);
			setAvatarRunning(false);

			// update all sounds
			laserSound.setLocation(lazergun.getWorldLocation());
			walkingSound.setLocation(avatar.getWorldLocation());
			voiceline1.setLocation(NPC.getWorldLocation());
			setEarParameters();

			// process the networking functions
			processNetworking((float) elapsTime);

			// show/hide mouse logic
			checkMouse();
			setMouseVisible(false);

			// Player logic
			/*
			 * if (distToP1 <= prize1.getScale() && !prize1.isCollected()) {
			 * score++;
			 * prize1.collect();
			 * rc1.toggle();
			 * prize1.setParent(avatar);
			 * prize1.propagateRotation(false);
			 * prize1.setLocalScale(new Matrix4f().scaling(0.25f));
			 * prize1.applyParentRotationToPosition(true);
			 * prize1.setLocalTranslation(new Matrix4f().translation(0f, 0f, trailLength));
			 * trailLength += -1.5f;
			 * }
			 * 
			 * if (distToP2 <= prize2.getScale() && !prize2.isCollected()) {
			 * score++;
			 * prize2.collect();
			 * rc2.toggle();
			 * prize2.setParent(avatar);
			 * prize2.propagateRotation(false);
			 * prize2.setLocalScale(new Matrix4f().scaling(0.25f));
			 * prize2.applyParentRotationToPosition(true);
			 * prize2.setLocalTranslation(new Matrix4f().translation(0f, 0f, trailLength));
			 * trailLength += -1.5f;
			 * }
			 * if (distToP3 <= prize3.getScale() && !prize3.isCollected()) {
			 * score++;
			 * prize3.collect();
			 * rc3.toggle();
			 * prize3.setParent(avatar);
			 * prize3.propagateRotation(false);
			 * prize3.setLocalScale(new Matrix4f().scaling(0.25f));
			 * prize3.applyParentRotationToPosition(true);
			 * prize3.setLocalTranslation(new Matrix4f().translation(0f, 0f, trailLength));
			 * trailLength += -1.5f;
			 * }
			 * if (distToP4 <= prize4.getScale() && !prize4.isCollected()) {
			 * score++;
			 * prize4.collect();
			 * rc4.toggle();
			 * prize4.setParent(avatar);
			 * prize4.propagateRotation(false);
			 * prize4.setLocalScale(new Matrix4f().scaling(0.25f));
			 * prize4.applyParentRotationToPosition(true);
			 * prize4.setLocalTranslation(new Matrix4f().translation(0f, 0f, trailLength));
			 * trailLength += -1.5f;
			 * }
			 */
			// pause scope and end game cutoff

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
					matrix.set(toFloatArray(go.getPhysicsObject().getTransform()));
					matrix.getRotation(aAngle);
					rotMatrix.rotation(aAngle);
					go.setLocalRotation(rotMatrix);
					identityMatrix.set(3, 0, matrix.m30());
					identityMatrix.set(3, 1, matrix.m31());
					identityMatrix.set(3, 2, matrix.m32());
					go.setLocalTranslation(identityMatrix);
				}
			}

			mapHeight(avatar, avatarGroundP);
			mapHeight(prize2, prize2GroundP);
			mapHeight(prize1, prize1GroundP);
			mapHeight(npc, npcGroundP);
			// } If condition for running physics with scripts
		}
		// END if statement for game not paused

		// update HUD variables
		String scoreStr = "Score: " + Integer.toString(score);
		String dolLocStr = "X: " + avatar.getWorldLocation().x() + " Y: " + avatar.getWorldLocation().y() + "  Z: "
				+ avatar.getWorldLocation().z();
		Vector3f dolLocColor = new Vector3f(1, 1, 1);
		Vector3f scoreColor = new Vector3f(0, 1, 0);
		String winStr = "You Have Beaten The Blob";
		Vector3f winColor = new Vector3f(0, 1, 0);
		if (prize1.isCollected() && prize2.isCollected() && prize3.isCollected() && prize4.isCollected()) {
			(engine.getHUDmanager()).setHUD1(winStr, winColor, (int) (width * 0.75f), 15);
			endGame = true;
		} else {
			(engine.getHUDmanager()).setHUD1(dolLocStr, dolLocColor, (int) (width * 0.75f), 15);
			(engine.getHUDmanager()).setHUD2(scoreStr, scoreColor, 15, 15);
		} // END Update
	}// END VariableFrameRate Game Overrides

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
					// handle collison?
					if (npcP.getUID() == obj1.getUID()
							|| npcP.getUID() == obj2.getUID() && lazerP.getUID() == obj1.getUID()
							|| lazerP.getUID() == obj2.getUID()) {
						System.out.println("");
						System.out.println("hit between: " + obj1 + " and " + obj2);
						break;
					} else {
						System.out.println("No Collision");
					}
				}
			}
		}
	}
	// ------------------------- KEY PRESSED ------------------------ //

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
			case KeyEvent.VK_F: {
				voiceline1.play();
				break;
			}
		}

		super.keyPressed(e);
	}

	// ------------------------- MOUSE MANAGEMENT ------------------------ //

	private void initMouseMode() {
		if (paused) {
			return;
		} else {
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
		}

		// rs.imageUpdate(ch, 1, (int)centerX, (int)centerY, ch.getWidth(null),
		// ch.getHeight(null));
	}

	private void recenterMouse() {
		if (paused) {
			return;
		} else {
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
	}

	@Override
	public void mouseMoved(java.awt.event.MouseEvent e) {
		if (paused) {
			return;
		} else {
			if (isRecentering && centerX == e.getXOnScreen() && centerY == e.getYOnScreen()) {
				isRecentering = false;
			} else {

				curMouseX = e.getXOnScreen();
				curMouseY = e.getYOnScreen();
				float mouseDeltaX = prevMouseX - curMouseX;
				float mouseDeltaY = prevMouseY - curMouseY;

				avatar.gyaw(getFrameDiff(), mouseDeltaX);
				// camMain.yaw(mouseDeltaX);
				avatar.pitch(getFrameDiff() / 2, mouseDeltaY);
				prevMouseX = curMouseX;
				prevMouseY = curMouseY;

				recenterMouse();
				prevMouseX = centerX;
				prevMouseY = centerY;
			}
		}
	}

	@Override
	// triggers on release of mouse click
	public void mouseClicked(MouseEvent e) {
		// System.out.println(e.getButton());
		setLazergunAim(false);
	}

	@Override
	// triggers on first press of mouse
	public void mousePressed(java.awt.event.MouseEvent e) {
		// System.out.println(e.getButton());

		if (paused) {
			return;
		} else {
			laserSound.play();
			setLazergunAim(true);
		}
		if (e.getButton() == 1) {
			System.out.println("fire");
			fireLazer();
		}
	}
	// END Mouse Management

	// ------------------------- AUDIO SECTION ------------------------ //

	private void fireLazer() {
	}

	public void initAudio() {
		AudioResource resource1, resource2, resource3, resource4;
		audioMgr = AudioManagerFactory.createAudioManager(
				"tage.audio.joal.JOALAudioManager");
		if (!audioMgr.initialize()) {
			System.out.println("Audio Manager failed to initialize!");
			return;
		}
		resource1 = audioMgr.createAudioResource("assets/sounds/grassWalking.wav",
				AudioResourceType.AUDIO_SAMPLE);
		walkingSound = new Sound(resource1, SoundType.SOUND_EFFECT, 40, true);
		walkingSound.initialize(audioMgr);
		walkingSound.setMaxDistance(10.0f);
		walkingSound.setMinDistance(0.5f);
		walkingSound.setRollOff(5.0f);
		walkingSound.setLocation(avatar.getWorldLocation());

		resource2 = audioMgr.createAudioResource("assets/sounds/grassRunning.wav",
				AudioResourceType.AUDIO_SAMPLE);
		runningSound = new Sound(resource2, SoundType.SOUND_EFFECT, 40, true);
		runningSound.initialize(audioMgr);
		runningSound.setMaxDistance(10.0f);
		runningSound.setMinDistance(0.5f);
		runningSound.setRollOff(5.0f);
		runningSound.setLocation(avatar.getWorldLocation());

		resource3 = audioMgr.createAudioResource("assets/sounds/laser.wav",
				AudioResourceType.AUDIO_SAMPLE);
		laserSound = new Sound(resource3, SoundType.SOUND_EFFECT, 100, false);
		laserSound.initialize(audioMgr);
		laserSound.setMaxDistance(10.0f);
		laserSound.setMinDistance(0.5f);
		laserSound.setRollOff(5.0f);
		laserSound.setLocation(lazergun.getWorldLocation());

		resource4 = audioMgr.createAudioResource("assets/sounds/teatimeVoiceline.wav",
				AudioResourceType.AUDIO_SAMPLE);
		voiceline1 = new Sound(resource4, SoundType.SOUND_EFFECT, 100, false);
		voiceline1.initialize(audioMgr);
		voiceline1.setMaxDistance(5.0f);
		voiceline1.setMinDistance(0.5f);
		voiceline1.setRollOff(3.0f);
		voiceline1.setLocation(NPC.getWorldLocation());

		setEarParameters();
	}

	public void setEarParameters() {
		Camera camera = getMainCamera();
		audioMgr.getEar().setLocation(avatar.getWorldLocation());
		audioMgr.getEar().setOrientation(camera.getN(),
				new Vector3f(0.0f, 1.0f, 0.0f));
	}// END Audio Section

	// -------------------------- NETWORKING SECTION -------------------------- //

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
		} else {
			// Send the initial join message with a unique identifier for this client
			System.out.println("sending join message to protocol host");
			protClient.sendJoinMessage();
		}
	}

	// network custom functions ------------
	protected void processNetworking(float elapsTime) {
		// Process packets received by the client from the server
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

	// --------------------------- CUSTOM FUNCTIONS --------------------------- //

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

	private void mapHeight(GameObject object, PhysicsObject objGround) {
		Vector3f loc = object.getWorldLocation();
		float height = ground.getHeight(loc.x(), loc.z());
		object.setLocalLocation(new Vector3f(loc.x(), (height + 1.0f), loc.z()));
		Matrix4f translation = new Matrix4f().translation(loc.x(), height - .05f, loc.z());
		double[] tempTransform = toDoubleArray(translation.get(vals));
		objGround.setTransform(tempTransform);
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
		Cursor crosshair = tk.createCustomCursor(tk.getImage("./assets/textures/normalCrosshair.png"), new Point(),
				"Crosshair");

		if (!mouseVisible) {
			canvas.setCursor(crosshair);
		} else {
			// set mouse back to default
			canvas.setCursor(null);
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

	protected double[] toDoubleArray(float[] arr) {
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

	// -------------------------- GETTERS & SETTERS -------------------------- //

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

	public float getFrameDiff() {
		return (float) frameDiff;
	}

	public boolean getLazergunAim() {
		return lazergunAimed;
	}

	public float getMoveSpeed() {
		return moveSpeed;
	}

	// -----------------------------------------

	public void togglePause() {
		paused = !paused;
	}

	public void toggleMouse() {
		mouseVisible = !mouseVisible;
	}

	public void toggleAim() {
		lazergunAimed = !lazergunAimed;
	}

	public void setLazergunAim(boolean newValue) {
		lazergunAimed = (newValue);
	}

	public void setAvatarWalking(boolean newValue) {
		isWalking = newValue;
	}

	public void setAvatarRunning(boolean newValue) {
		isRunning = newValue;
	}

	public void setMoveSpeed(float moveSpeed) {
		this.moveSpeed = moveSpeed;
	}

	public void setMouseVisible(boolean newValue) {
		mouseVisible = newValue;
	}// END Getters & Setters

	// -------------------------- SCRIPTING SECTION -------------------------- //

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
}// END