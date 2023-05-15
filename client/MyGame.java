package client;

import org.joml.*;
import org.joml.Math;

import com.bulletphysics.collision.broadphase.Dispatcher;
import com.bulletphysics.collision.narrowphase.ManifoldPoint;
import com.bulletphysics.collision.narrowphase.PersistentManifold;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;

import tage.*;
import tage.Light.LightType;
import tage.input.*;
import tage.shapes.*;
import tage.audio.Sound;
import tage.audio.SoundType;
import tage.input.action.*;
import tage.audio.AudioManagerFactory;
import tage.audio.AudioResource;
import tage.audio.AudioResourceType;
import tage.audio.IAudioManager;

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
	private Robot robot;
	private InputManager im;
	private GhostManager gm;
	private String startupStr;
	private String serverAddress;
	private Vector3f lastCamLocation;
	private ProtocolClient protClient;
	private Light blueSpawn, redSpawn;
	private File scriptFile1, scriptFile2, scriptFile3;
	private Sound laserSound, walkingSound, runningSound, riverSound, birdSounds, song;

	// network & script variables
	private int serverPort;
	private PhysicsEngine ps;
	private ScriptEngine jsEngine;
	private IAudioManager audioMgr;
	private ProtocolType serverProtocol;

	// Basic Variables
	private String team;
	private boolean paused; // pause game on startup
	private boolean endGame;
	private float moveSpeed;
	private boolean viewAxis;
	private boolean isRunning;
	private boolean isWalking;
	private boolean mouseVisible;
	private boolean isRecentering;
	private boolean lazergunAimed;
	private int width, height, forest;
	private boolean isClientConnected;
	private float vals[] = new float[16];
	private boolean NPCisWalking = false;
	private int score, blueScore, redScore;
	private double lastFrameTime, currFrameTime, elapsTime, frameDiff;
	private float curMouseX, curMouseY, prevMouseX, prevMouseY, centerX, centerY;

	// object variables
	private Vector<GhostNPC> npcs;
	private AnimatedShape avatarS, npcS;
	private Vector<GameObject> lazers;
	private Vector<GhostAvatar> ghosts;
	private PhysicsObject npcP, avatarP;// lazerGroundP;
	private GameObject lazergun, avatar, water, ground, x, y, z, npc, riverWater;
	private ObjShape lazergunS, riverS, linxS, linyS, linzS, terrS, lazerS, waterS;
	private TextureImage avatartx, avatartxBlue, avatartxRed, lazerguntx, riverT, groundtx, river, lazerT, waterT;

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
		avatarS = new AnimatedShape("man5.rkm", "man5.rks");
		avatarS.loadAnimation("WALK", "man5.rka");
		npcS = new AnimatedShape("man5.rkm", "man5.rks");
		npcS.loadAnimation("WALKnpc", "man5.rka");
		lazergunS = new ImportedModel("lazergun.obj");
		lazerS = new Sphere();
		riverS = new Cube();
		linxS = new Line(new Vector3f(0f, 40f, 0f), new Vector3f(500f, 40f, 0f));
		linyS = new Line(new Vector3f(0f, 0f, 0f), new Vector3f(0f, 80f, 0f));
		linzS = new Line(new Vector3f(0f, 40f, 0f), new Vector3f(0f, 40f, 500f));
		terrS = new TerrainPlane(1000);
		waterS = new TerrainPlane(1000);
	}

	@Override
	public void loadTextures() {
		// default texture is blue
		avatartx = new TextureImage("man5.png");
		avatartxBlue = new TextureImage("man5.png");
		avatartxRed = new TextureImage("man6.png");
		lazerT = new TextureImage("lazerbeam.png");
		lazerguntx = new TextureImage("lazergun.png");
		riverT = new TextureImage("brushwalker437.png");
		groundtx = new TextureImage("brown_mud_leaves_01_diff_2k.jpg");
		river = new TextureImage("river.jpg");
		waterT = new TextureImage("brushwalker437.png");
	}

	@Override
	public void buildObjects() {
		jsEngine.put("rand", rand);
		scriptFile2 = new File("assets/scripts/BlueSpawn.js");

		// build the ground
		ground = new GameObject(GameObject.root(), terrS, groundtx);
		ground.setLocalTranslation(new Matrix4f().translation(0f, 0, 0f));
		ground.setLocalScale((new Matrix4f()).scaling(500.0f, 40.0f, 500.0f));
		ground.getRenderStates().setTiling(1);
		ground.setHeightMap(river);

		// build water
		riverWater = new GameObject(GameObject.root(), waterS, waterT);
		riverWater.setLocalTranslation(new Matrix4f().translation(0f, 2.0f, 0f));
		riverWater.setLocalScale(new Matrix4f().scaling(500.0f, 0.0f, 500.0f));

		// NPC setup
		npc = new GameObject(GameObject.root(), avatarS, avatartx);
		npc.setLocalTranslation(new Matrix4f().translation(80.0f, 0.0f, 20.0f));
		npc.getRenderStates().setModelOrientationCorrection(
				(new Matrix4f()).rotationY((float) java.lang.Math.toRadians(270.0f)));
		npc.setLocalScale(new Matrix4f().scale(.45f));

		// build avatar in the center of the window
		avatar = new GameObject(GameObject.root(), avatarS, avatartx);
		avatar.setLocalTranslation((new Matrix4f()).translation(500f, 500f, 500f));
		avatar.setLocalScale((new Matrix4f()).scaling(.43f));
		// avatar.setLocalRotation(new Matrix4f().rotateY((float)
		// Math.toRadians(270f))); // red 90f
		avatar.getRenderStates().setModelOrientationCorrection(
				(new Matrix4f()).rotationY((float) java.lang.Math.toRadians(270.0f)));

		// build lazergun object
		lazergun = new GameObject(GameObject.root(), lazergunS, lazerguntx);
		lazergun.setLocalTranslation((new Matrix4f()).translation(0f, 0f, 0f));
		lazergun.setLocalScale((new Matrix4f()).scaling(0.15f));
		lazergun.setParent(avatar);
		lazergun.propagateRotation(true);
		lazergun.propagateTranslation(true);
		lazergun.applyParentRotationToPosition(true);

		// build prize 1
		water = new GameObject(GameObject.root(), riverS, riverT);
		// jsEngine.put("object", prize1);
		// this.runScript(scriptFile1);
		water.setLocalTranslation(new Matrix4f().translation(-60f, 0.0f, 0.0f));
		water.setLocalScale((new Matrix4f()).scaling(.5f));

		// build world axes
		x = new GameObject(GameObject.root(), linxS);
		y = new GameObject(GameObject.root(), linyS);
		z = new GameObject(GameObject.root(), linzS);
		(x.getRenderStates()).setColor(new Vector3f(1f, 0f, 0f));
		(y.getRenderStates()).setColor(new Vector3f(0f, 1f, 0f));
		(z.getRenderStates()).setColor(new Vector3f(0f, 0f, 1f));
		(x.getRenderStates()).disableRendering();
		(y.getRenderStates()).disableRendering();
		(z.getRenderStates()).disableRendering();

		// add objects to vector
		lazers = new Vector<GameObject>();
		npcs = new Vector<GhostNPC>();
		ghosts = new Vector<GhostAvatar>();
	}

	@Override
	public void initializeLights() {
		Light.setGlobalAmbient(0.5f, 0.5f, 0.5f);
		blueSpawn = new Light();
		redSpawn = new Light();
		blueSpawn.setLocation(new Vector3f(250.0f, 200.0f, 0.0f));
		blueSpawn.setType(LightType.SPOTLIGHT);
		blueSpawn.setDirection(new Vector3f(0.0f, -1.0f, 0.0f));
		blueSpawn.setDiffuse(0.0f, 0.0f, 1.0f);
		blueSpawn.setAmbient(0.0f, 0.0f, 0.5f);
		(engine.getSceneGraph()).addLight(blueSpawn);
		redSpawn.setLocation(new Vector3f(-250.0f, 200.0f, 0.0f));
		redSpawn.setType(LightType.SPOTLIGHT);
		redSpawn.setDirection(new Vector3f(0.0f, -1.0f, 0.0f));
		redSpawn.setDiffuse(0.75f, 0.0f, 0.0f);
		// redSpawn.setAmbient(0.25f, 0.0f, 0.0f);
		(engine.getSceneGraph()).addLight(redSpawn);
	}

	@Override
	public void loadSkyBoxes() {
		forest = (engine.getSceneGraph()).loadCubeMap("forest");
		(engine.getSceneGraph()).setActiveSkyBoxTexture(forest);
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
		// ----------------- JS initialization ----------------- //
		scriptFile1 = new File("assets/scripts/GameParams.js");
		this.runScript(scriptFile1);
		moveSpeed = Float.parseFloat(jsEngine.get("moveSpeed").toString());
		score = ((int) jsEngine.get("score"));
		blueScore = ((int) jsEngine.get("blueScore"));
		redScore = ((int) jsEngine.get("redScore"));
		elapsTime = ((double) jsEngine.get("elapsTime"));
		paused = ((boolean) jsEngine.get("paused"));
		endGame = ((boolean) jsEngine.get("endGame"));
		viewAxis = ((boolean) jsEngine.get("viewAxis"));
		isRunning = ((boolean) jsEngine.get("isRunning"));
		isWalking = ((boolean) jsEngine.get("isWalking"));
		mouseVisible = ((boolean) jsEngine.get("mouseVisible"));
		lazergunAimed = ((boolean) jsEngine.get("lazergunAimed"));
		isClientConnected = ((boolean) jsEngine.get("isClientConnected"));

		// ----------------- set window size ----------------- //
		(engine.getRenderSystem()).setWindowDimensions(1920, 1080);

		// -------------------- variables -------------------- //
		im = engine.getInputManager();
		lastFrameTime = System.currentTimeMillis();
		currFrameTime = System.currentTimeMillis();

		// add a toggle function that can toggle between using
		// a regular camera and an orbit camera

		// ---------------- initialize camera ---------------- //
		camMain = (engine.getRenderSystem().getViewport("LEFT").getCamera());
		// positionCameraBehindAvatar();

		// ------------------- orbit camera ------------------ //
		// String gpName = im.getFirstGamepadName();
		// orbitCam = new CameraOrbit3D(camMain, avatar, gpName, engine);

		// --------------- initialize custom functions ---------------- //
		setupNetworking();
		initMouseMode();
		initAudio();

		// ------------------- Input Setup ------------------- //

		// AimAction aimAction = new AimAction(this);
		MoveAction moveAction = new MoveAction(this);
		PauseAction pauseAction = new PauseAction(this);
		ToggleMouseAction mouseAction = new ToggleMouseAction(this);
		SendCloseConnectionPacketAction closeAction = new SendCloseConnectionPacketAction();

		// Keyboard Actions ---------------------------------------------------
		// im.associateActionWithAllKeyboards(Component.Identifier.Key.R, aimAction,
		// InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		im.associateActionWithAllKeyboards(Component.Identifier.Key.W, moveAction,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(Component.Identifier.Key.S, moveAction,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(Component.Identifier.Key.A, moveAction,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(Component.Identifier.Key.D, moveAction,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(Component.Identifier.Key.LALT, mouseAction,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(Component.Identifier.Key.TAB, pauseAction,
				InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		im.associateActionWithAllKeyboards(Component.Identifier.Key.LSHIFT, moveAction,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(Component.Identifier.Key.ESCAPE, closeAction,
				InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);

		// Gamepad Actions ----------------------------------------------------
		im.associateActionWithAllGamepads(Component.Identifier.Axis.X, moveAction,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllGamepads(Component.Identifier.Axis.Y, moveAction,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllGamepads(Component.Identifier.Button._6, mouseAction,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllGamepads(Component.Identifier.Button._7, pauseAction,
				InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);

		// --------------------- Physics Engine --------------------- //

		// init physics engine
		String eng = "tage.physics.JBullet.JBulletPhysicsEngine";
		float[] gravity = { 0f, -10f, 0f };
		ps = PhysicsEngineFactory.createPhysicsEngine(eng);
		ps.initSystem();
		ps.setGravity(gravity);

		// creating physics world
		float mass = 1.0f;
		float psize[] = { 4.0f, 4.0f, 4.0f };
		double[] tempTransform;

		// --------------------- Physics Objects --------------------- //

		buildNpc();
		// buildAvatar();
	}

	// -------------------------- UPDATE -------------------------- //

	@Override
	public void update() {
		lastFrameTime = currFrameTime;
		currFrameTime = System.currentTimeMillis();
		frameDiff = (currFrameTime - lastFrameTime) / 1000.0;
		width = (engine.getRenderSystem()).getWidth();
		height = (engine.getRenderSystem()).getHeight();

		ghosts = gm.getGhostAvatars();
		if (paused) {
			im.update((float) elapsTime);
			mapHeight(avatar);
			// buildAvatar();
			positionCameraAboveMap();
		}
		if (!paused && !endGame) {

			// update time
			elapsTime += frameDiff;
			im.update((float) elapsTime);

			// update player location
			// ps.removeObject(avatarP.getUID());
			mapHeight(avatar);
			// buildAvatar();
			positionCameraBehindAvatar();
			// update all sounds
			laserSound.setLocation(lazergun.getWorldLocation());
			walkingSound.setLocation(avatar.getWorldLocation());
			runningSound.setLocation(avatar.getWorldLocation());
			riverSound.setLocation(water.getWorldLocation());
			setEarParameters();

			// update lazergun position and aim
			lazergun.applyParentRotationToPosition(true);
			if (lazergunAimed) { // -0.217f, 0.8f, 0.9f
				lazergun.setLocalTranslation(new Matrix4f().translation(-0.23f, 1.06f, 0.85f));
			} else {
				lazergun.setLocalTranslation(new Matrix4f().translation(-0.4f, 1.06f, 0.9f));
			}

			// update walking sound and animation
			if (isWalking && (walkingSound.getIsPlaying() == false)) {
				walkingSound.play();
				walkingSound.resume();
				avatarS.playAnimation("WALK", 0.5f, AnimatedShape.EndType.LOOP, 0);
			}
			if (!isWalking && (walkingSound.getIsPlaying() == true)) {
				walkingSound.pause();
				avatarS.stopAnimation();
			}

			// update running sound and animation
			if (isRunning && (runningSound.getIsPlaying() == false)) {
				runningSound.play();
				runningSound.resume();
				avatarS.playAnimation("WALK", 0.5f, AnimatedShape.EndType.LOOP, 0);
			}
			if (!isRunning && (runningSound.getIsPlaying() == true)) {
				runningSound.pause();
				avatarS.stopAnimation();
			}
			setAvatarWalking(false);
			setAvatarRunning(false);

			// update all sounds
			laserSound.setLocation(lazergun.getWorldLocation());
			walkingSound.setLocation(avatar.getWorldLocation());
			setEarParameters();

			// show/hide mouse logic
			checkMouse();
			// setMouseVisible(false);

			// update animation
			avatarS.updateAnimation();

			// --------------------- PHYSICS LOGIC --------------------------//

			// update npc physics objects
			ps.removeObject(npcP.getUID());

			// npc movement and animation
			npc.move((float) elapsTime * 2);
			// npcS.updateAnimation();
			getProtocolClient().sendMoveNPCMessage(npc.getLocalLocation());

			// set npc to map height and rebuild physics object
			mapHeight(npc);
			buildNpc();

			Matrix4f matrix = new Matrix4f();
			Matrix4f rotMatrix = new Matrix4f();
			AxisAngle4f aAngle = new AxisAngle4f();
			Matrix4f identityMatrix = new Matrix4f().identity();
			checkCollisions();
			updateGhostPhysics(ghosts);
			// checkBulletDistances();
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
			// update static objects
			mapHeight(water);

			// npc look at avatar
			npc.lookAt(avatar.getLocalLocation());

			// process the networking functions
			processNetworking((float) elapsTime);
		}
		// END if statement for game not paused

		// ----------------------------- HUD ----------------------------- //

		if (paused) {
			startupStr = "CHOOSE YOUR TEAM: Blue (1) OR Red (2)";
			Vector3f startupColor = new Vector3f(1, 0, 1);
			(engine.getHUDmanager()).setHUD1(startupStr, startupColor, (int) (width * 0.375f), (int) (height * 0.5f));
		} else {
			startupStr = "Blue Team: " + blueScore + " || " + "Red Team: " + redScore;
			Vector3f startupColor = new Vector3f(1, .25f, 1);
			(engine.getHUDmanager()).setHUD1(startupStr, startupColor, (int) (width * 0.45f), (int) (height * 0.9f)); // 1080
		}

		String scoreStr = "Score: " + Integer.toString(score);
		Vector3f scoreColor = new Vector3f(0, 1, 0);
		(engine.getHUDmanager()).setHUD2(scoreStr, scoreColor, 15, 15);

		// END Update
	}// END VariableFrameRate Game Overrides

	private void updateGhostPhysics(Vector<GhostAvatar> ga) {
		GhostAvatar ghostAvatar;
		Iterator<GhostAvatar> it = ga.iterator();
		while (it.hasNext()) {
			ghostAvatar = it.next();
			PhysicsObject ghostP = ghostAvatar.getPhysicsObject();
			if (ghostP != null) {
				ps.removeObject(ghostP.getUID());
			}
			float[] psize = { 1.5f, 3f, 1.5f };
			Matrix4f translation = new Matrix4f(ghostAvatar.getLocalTranslation());
			double[] tempTransform = toDoubleArray(translation.get(vals));
			ghostP = ps.addBoxObject(ps.nextUID(), 0.0f, tempTransform, psize);
			ghostP.setDamping(0f, 1f);
			ghostAvatar.setPhysicsObject(ghostP);
		}
	}

	// --------------- COLLISION DETECTION AND HANDLING --------------- //
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
					// handle collison
					GameObject laz;
					Iterator<GameObject> it1 = lazers.iterator();
					while (it1.hasNext()) {
						laz = it1.next();
						PhysicsObject lazP = laz.getPhysicsObject();
						if (lazP.getUID() == obj1.getUID() || lazP.getUID() == obj2.getUID()) {
							GhostAvatar ga;
							Iterator<GhostAvatar> it2 = ghosts.iterator();
							while (it2.hasNext()) {
								ga = it2.next();
								PhysicsObject gaP = ga.getPhysicsObject();
								if (gaP.getUID() == obj1.getUID() || gaP.getUID() == obj2.getUID()) {
									ps.removeObject(lazP.getUID());
									engine.getSceneGraph().removeGameObject(laz);
									it1.remove();
									if (team.equals("BLUE")) {
										blueScore++;
										updateBlueScore();
									}
									if (team.equals("RED")) {
										redScore++;
										updateRedScore();
									}
									score++;
								}
							}
						}
					}
					System.out.println("Physics Hit");
					break;
				}
			}
		}
	}

	// ------------------------- KEY PRESSED ------------------------- //

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
			case KeyEvent.VK_0: {
				viewAxis = !viewAxis;
				toggleAxies();
				break;
			}
			case KeyEvent.VK_1: {
				System.out.println("Blue team chosen");
				avatartx = avatartxBlue;
				team = "BLUE";
				paused = !paused;
				joinTeam();
				break;
			}
			case KeyEvent.VK_2: {
				System.out.println("Red team chosen");
				avatartx = avatartxRed;
				team = "RED";
				paused = !paused;
				joinTeam();
				break;
			}
		}

		super.keyPressed(e);
	}

	// ------------------------- MOUSE MANAGEMENT ------------------------ //

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
				// ps.removeObject(avatarP.getUID());
				avatar.gyaw(getFrameDiff(), mouseDeltaX);
				// camMain.yaw(mouseDeltaX);
				avatar.pitch(getFrameDiff() / 2, mouseDeltaY);
				// buildAvatar();
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
	public void mouseReleased(MouseEvent e) {
		if (paused) {
			return;
		} else if (e.getButton() == 3) {
			setLazergunAim(false);
		} // else System.out.println(e.getButton());
	}

	@Override
	// triggers on first press of mouse
	public void mousePressed(java.awt.event.MouseEvent e) {
		if (paused) {
			return;
		} else if (e.getButton() == 1) {
			laserSound.play();
			fireLazer();
		} else if (e.getButton() == 3) {
			setLazergunAim(true);
		} else
			System.out.println(e.getButton());
	}

	@Override
	public void mouseDragged(java.awt.event.MouseEvent e) {
		if (paused) {
			return;
		} else if (e.getButton() == 0) {
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
		// System.out.println(e.getButton());
	}

	// checks if mouse is hidden or shown and sets the cursor icon
	private void checkMouse() {
		RenderSystem rs = engine.getRenderSystem();
		Toolkit tk = Toolkit.getDefaultToolkit();
		Canvas canvas = rs.getGLCanvas();

		// Cursor clear = tk.createCustomCursor(tk.getImage(""), new Point(),
		// "ClearCursor");
		Cursor crosshair = tk.createCustomCursor(tk.getImage("./assets/textures/Blue-Crosshair-1.png"), new Point(),
				"Crosshair");

		if (!mouseVisible) {
			canvas.setCursor(crosshair);
		} else {
			// set mouse back to default
			canvas.setCursor(null);
		}
	}
	// END Mouse Management

	// ------------------------- AUDIO SECTION ------------------------ //

	public void initAudio() {
		AudioResource resource1, resource2, resource3, resource4, resource5, resource6, resource7;
		audioMgr = AudioManagerFactory.createAudioManager(
				"tage.audio.joal.JOALAudioManager");
		if (!audioMgr.initialize()) {
			System.out.println("Audio Manager failed to initialize!");
			return;
		}

		resource1 = audioMgr.createAudioResource("assets/sounds/grassWalking.wav",
				AudioResourceType.AUDIO_SAMPLE);
		walkingSound = new Sound(resource1, SoundType.SOUND_EFFECT, 17, true);
		walkingSound.initialize(audioMgr);
		walkingSound.setMaxDistance(10.0f);
		walkingSound.setMinDistance(0.5f);
		walkingSound.setRollOff(2.0f);
		walkingSound.setLocation(avatar.getWorldLocation());

		resource2 = audioMgr.createAudioResource("assets/sounds/grassRunning.wav",
				AudioResourceType.AUDIO_SAMPLE);
		runningSound = new Sound(resource2, SoundType.SOUND_EFFECT, 10, true);
		runningSound.initialize(audioMgr);
		runningSound.setMaxDistance(10.0f);
		runningSound.setMinDistance(0.5f);
		runningSound.setRollOff(2.0f);
		runningSound.setLocation(avatar.getWorldLocation());

		resource3 = audioMgr.createAudioResource("assets/sounds/laser.wav",
				AudioResourceType.AUDIO_SAMPLE);
		laserSound = new Sound(resource3, SoundType.SOUND_EFFECT, 100, false);
		laserSound.initialize(audioMgr);
		laserSound.setMaxDistance(10.0f);
		laserSound.setMinDistance(0.5f);
		laserSound.setRollOff(2.0f);
		laserSound.setLocation(lazergun.getWorldLocation());

		resource4 = audioMgr.createAudioResource("assets/sounds/birdsInForest.wav",
				AudioResourceType.AUDIO_SAMPLE);
		birdSounds = new Sound(resource4, SoundType.SOUND_EFFECT, 100, true);
		birdSounds.initialize(audioMgr);
		birdSounds.setMaxDistance(10.0f);
		birdSounds.setMinDistance(0.5f);
		birdSounds.setRollOff(1.0f);
		birdSounds.setLocation(avatar.getWorldLocation());

		resource5 = audioMgr.createAudioResource(
				"assets/sounds/river.wav", AudioResourceType.AUDIO_SAMPLE);
		riverSound = new Sound(resource5,
				SoundType.SOUND_EFFECT, 80, true);
		riverSound.initialize(audioMgr);
		riverSound.setMaxDistance(10.0f);
		riverSound.setMinDistance(0.5f);
		riverSound.setRollOff(1.5f);
		riverSound.setLocation(water.getWorldLocation());

		// ---- music

		resource7 = audioMgr.createAudioResource(
				"assets/sounds/80's synth 2.wav", AudioResourceType.AUDIO_SAMPLE);
		song = new Sound(resource7,
				SoundType.SOUND_EFFECT, 10, true);
		song.initialize(audioMgr);
		song.setMaxDistance(10.0f);
		song.setMinDistance(0.5f);
		song.setRollOff(2.0f);
		song.setLocation(avatar.getWorldLocation());

		setEarParameters();
		riverSound.play();
		birdSounds.play();
		song.play();
	}

	public void setEarParameters() {
		Camera camera = (engine.getRenderSystem()).getViewport("LEFT").getCamera();
		audioMgr.getEar().setLocation(avatar.getWorldLocation());
		audioMgr.getEar().setOrientation(camera.getN(),
				new Vector3f(0.0f, 1.0f, 0.0f));
	}// END Audio Section

	// -------------------------- NETWORKING SECTION -------------------------- //

	private void setupNetworking() {
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

	private void updateBlueScore() {
		protClient.sendBlueScore(blueScore);
	}

	private void updateRedScore() {
		protClient.sendRedScore(redScore);
	}

	public class SendCloseConnectionPacketAction extends AbstractInputAction {

		@Override
		public void performAction(float time, net.java.games.input.Event evt) {
			if (protClient != null && isClientConnected == true) {
				protClient.sendByeMessage();
			}
		}
	}// END Networking Section

	// --------------------------- CUSTOM FUNCTIONS --------------------------- //

	public void checkNPCNear(GhostNPC gNPC) {
		float distance;

		if (gNPC != null) {
			distance = distanceTo(gNPC.getLocalLocation(), avatar);

			if (distance < 4) {
				System.out.println("NPC near");
				getProtocolClient().sendNPCNearMessage(gNPC.getNPCid());

			}
		}
	}

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

	protected void mapHeight(GameObject object) {
		Vector3f loc = object.getWorldLocation();
		float height = ground.getHeight(loc.x(), loc.z());
		float[] gsize = { 2.0f, 1.0f, 2.0f };
		object.setLocalLocation(new Vector3f(loc.x(), (height + 1.5f), loc.z()));
	}

	private void fireLazer() {
		GameObject newLazer = new GameObject(GameObject.root(), lazerS, lazerT);
		Vector3f loc = getPlayerPosition();
		Vector3f fwd = avatar.getLocalForwardVector();
		newLazer.setLocalTranslation(new Matrix4f().translation(loc.x() - .1f, loc.y() + 1f, loc.z()));
		newLazer.setLocalScale(new Matrix4f().scaling(0.02f, 0.02f, 0.02f));

		Matrix4f translation = new Matrix4f(newLazer.getLocalTranslation());
		double[] tempTransform = toDoubleArray(translation.get(vals));
		PhysicsObject newLazerP = ps.addSphereObject(ps.nextUID(), 1f, tempTransform, .02f);
		newLazerP.setBounciness(.01f);
		newLazerP.setLinearVelocity(new float[] { fwd.x() * 100, fwd.y() * 100, fwd.z() * 100 });
		newLazer.setPhysicsObject(newLazerP);
		lazers.add(newLazer);
	}

	public void buildNpc() {
		float[] psize = { 1f, 2f, 1f };
		Matrix4f translation = new Matrix4f(npc.getLocalTranslation());
		double[] tempTransform = toDoubleArray(translation.get(vals));
		npcP = ps.addBoxObject(ps.nextUID(), 1.0f, tempTransform, psize);
		npcP.setDamping(0f, 1f);
		npcP.setBounciness(1.0f);
		npc.setPhysicsObject(npcP);
	}

	protected void buildAvatar() {
		float[] psize = { 1f, 2f, 1f };
		Matrix4f translation = new Matrix4f(avatar.getLocalTranslation());
		double[] tempTransform = toDoubleArray(translation.get(vals));
		avatarP = ps.addBoxObject(ps.nextUID(), 1.0f, tempTransform, psize);
		avatarP.setDamping(0f, 1f);
		avatarP.setBounciness(1.0f);
		avatar.setPhysicsObject(avatarP);
	}

	private void joinTeam() {
		if (team.equals("BLUE")) {
			scriptFile2 = new File("assets/scripts/BlueSpawn.js");
			jsEngine.put("rand", rand);
			avatar.setTextureImage(avatartxBlue);
			jsEngine.put("object", avatar);
			this.runScript(scriptFile2);
			avatar.setLocalRotation(new Matrix4f().rotateY((float) Math.toRadians(270f)));
		} else {
			scriptFile3 = new File("assets/scripts/RedSpawn.js");
			jsEngine.put("rand", rand);
			avatar.setTextureImage(avatartxRed);
			jsEngine.put("object", avatar);
			this.runScript(scriptFile3);
			avatar.setLocalRotation(new Matrix4f().rotateY((float) Math.toRadians(90f)));
		}
	}

	private void positionCameraBehindAvatar() {
		Vector3f location = avatar.getWorldLocation();
		camMain.setU(avatar.getWorldRightVector());
		camMain.setV(avatar.getWorldUpVector());
		camMain.setN(avatar.getWorldForwardVector());
		location.add(camMain.getV().mul(1.2f));
		location.add(camMain.getN().mul(.3f));
		camMain.setLocation(location);
	}

	private void positionCameraAboveMap() {
		camMain.setU(new Vector3f(-1f, 0f, 0f));
		camMain.setV(new Vector3f(0f, 0f, 1f));
		camMain.setN(new Vector3f(0f, -1f, 0f));
		camMain.setLocation(new Vector3f(0.0f, 400.0f, 0.0f));
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

	public boolean paused() {
		return paused;
	}

	public GameObject getAvatar() {
		return avatar;
	}

	public Engine getEngine() {
		return engine;
	}

	public PhysicsEngine getPhysicsEngine() {
		return ps;
	}

	public static Camera getMainCamera() {
		return camMain;
	}

	public GameObject getNPC() {
		return npc;
	}

	public ObjShape getNPCshape() {
		return npcS;
	}

	public TextureImage getNPCtexture() {
		return npc.getTextureImage();
	}

	public ObjShape getGhostShape() {
		return avatar.getShape();
	}

	public TextureImage getGhostTexture() {
		return avatar.getTextureImage();
	}

	public Matrix4f getGhostRotation() {
		return avatar.getLocalRotation();
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

	public void toggleAxies() {
		if (viewAxis) {
			(x.getRenderStates()).enableRendering();
			(y.getRenderStates()).enableRendering();
			(z.getRenderStates()).enableRendering();
		} else {
			(x.getRenderStates()).disableRendering();
			(y.getRenderStates()).disableRendering();
			(z.getRenderStates()).disableRendering();
		}
	}

	public void setLazergunAim(boolean newValue) {
		lazergunAimed = newValue;
	}

	public void setNPCWalking(boolean newValue) {
		NPCisWalking = newValue;
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
	}

	public void setBlueScore(String bs) {
		blueScore = Integer.parseInt(bs);
	}

	public void setRedScore(String rs) {
		redScore = Integer.parseInt(rs);
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

	// -------------------------- SHUTDOWN PROTOCOL -------------------------- //

	@Override
	public void shutdown() {
		super.shutdown();
		setIsConnected(false);
		protClient.sendByeMessage();
	}
}// END