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
	private Light light1;
	private InputManager im;
	private GhostManager gm;
	private File scriptFile1;
	private String startupStr;
	private String serverAddress;
	private Vector3f lastCamLocation;
	private ProtocolClient protClient;
	private Vector<GameObject> objects;
	private Sound laserSound, walkingSound, runningSound, voiceline1, riverSound;

	// network & script variables
	private PhysicsEngine ps;
	private ScriptEngine jsEngine;
	private IAudioManager audioMgr;
	private ProtocolType serverProtocol;

	// Basic Variables
	private int width;
	private int score = 0;
	private int serverPort;
	private int forest;
	private boolean isRecentering;
	private float moveSpeed = 3.0f;
	private boolean paused = true; // pause game on startup
	private boolean endGame = false;
	private boolean isRunning = false;
	private boolean isWalking = false;
	// private boolean placedOnMap = false;
	private boolean cameraSetUp = false;
	private boolean mouseVisible = true;
	private float vals[] = new float[16];
	private boolean NPCisWalking = false;
	private boolean lazergunAimed = false;
	private boolean isClientConnected = false;
	private double lastFrameTime, currFrameTime, elapsTime, frameDiff;
	private float curMouseX, curMouseY, prevMouseX, prevMouseY, centerX, centerY;

	// object variables
	private AnimatedShape avatarS, npcS;
	private GameObject lazergun, avatar, prize1, ground, x, y, z, npc, riverWater;
	private ObjShape lazergunS, prize1S, linxS, linyS, linzS, terrS, lazerS, waterS;
	private TextureImage avatartx, avatartxBlue, avatartxRed, lazerguntx, p1tx, groundtx, river, lazerT, waterT;
	private PhysicsObject prize1P, npcP;// lazerGroundP, avatarP;
	private ArrayList<GameObject> lazers;
	private ArrayList<GameObject> ghosts;
	private ArrayList<GameObject> npcs;

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
		npcS.loadAnimation("WALK", "man5.rka");
		lazergunS = new ImportedModel("lazergun.obj");
		lazerS = new Sphere();
		prize1S = new Torus();
		linxS = new Line(new Vector3f(0f, 0f, 0f), new Vector3f(50f, 0f, 0f));
		linyS = new Line(new Vector3f(0f, 0f, 0f), new Vector3f(0f, 50f, 0f));
		linzS = new Line(new Vector3f(0f, 0f, 0f), new Vector3f(0f, 0f, 50f));
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
		p1tx = new TextureImage("tex_Water.jpg");
		groundtx = new TextureImage("brown_mud_leaves_01_diff_2k.jpg");
		river = new TextureImage("river.jpg");
		waterT = new TextureImage("brushwalker437.png");
	}

	@Override
	public void buildObjects() {
		jsEngine.put("rand", rand);
		scriptFile1 = new File("assets/scripts/RandomTranslation.js");

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
		npc.setLocalRotation(new Matrix4f().rotateY((float) Math.PI));
		npc.setLocalScale(new Matrix4f().scale(.45f));

		// build avatar in the center of the window
		avatar = new GameObject(GameObject.root(), avatarS, avatartx);
		avatar.setLocalTranslation((new Matrix4f()).translation(80f, 0f, 12.0f));
		avatar.setLocalScale((new Matrix4f()).scaling(.43f));
		avatar.getRenderStates().setModelOrientationCorrection(
				(new Matrix4f()).rotationY((float) java.lang.Math.toRadians(90.0f)));

		// build lazergun object
		lazergun = new GameObject(GameObject.root(), lazergunS, lazerguntx);
		lazergun.setLocalTranslation((new Matrix4f()).translation(4f, 4f, 4f));
		lazergun.setLocalScale((new Matrix4f()).scaling(0.15f));
		lazergun.setParent(avatar);
		lazergun.propagateRotation(true);
		lazergun.propagateTranslation(true);
		lazergun.applyParentRotationToPosition(true);

		// build prize 1
		prize1 = new GameObject(GameObject.root(), prize1S, p1tx);
		jsEngine.put("object", prize1);
		this.runScript(scriptFile1);
		prize1.setLocalScale((new Matrix4f()).scaling(3.0f));
		prize1.getRenderStates().setTiling(1);

		// build world axes
		x = new GameObject(GameObject.root(), linxS);
		y = new GameObject(GameObject.root(), linyS);
		z = new GameObject(GameObject.root(), linzS);
		(x.getRenderStates()).setColor(new Vector3f(1f, 0f, 0f));
		(y.getRenderStates()).setColor(new Vector3f(0f, 1f, 0f));
		(z.getRenderStates()).setColor(new Vector3f(0f, 0f, 1f));

		// add objects to vector
		lazers = new ArrayList<>();
		npcs = new ArrayList<>();
		ghosts = new ArrayList<>();

	}

	@Override
	public void initializeLights() {
		Light.setGlobalAmbient(0.5f, 0.5f, 0.5f);
		light1 = new Light();
		light1.setLocation(new Vector3f(0.0f, 100.0f, 0.0f));
		(engine.getSceneGraph()).addLight(light1);
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

		// AimAction aimAction = new AimAction(this);
		MoveAction moveAction = new MoveAction(this);
		PauseAction pauseAction = new PauseAction(this);
		ToggleMouseAction mouseAction = new ToggleMouseAction(this);

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
		float[] gravity = { 0f, -5f, 0f };
		ps = PhysicsEngineFactory.createPhysicsEngine(eng);
		ps.initSystem();
		ps.setGravity(gravity);

		// creating physics world
		float mass = 1.0f;
		float psize[] = { 4.0f, 4.0f, 4.0f };
		double[] tempTransform;

		// --------------------- Physics Objects --------------------- //
		Matrix4f translation = new Matrix4f(prize1.getLocalTranslation());
		tempTransform = toDoubleArray(translation.get(vals));
		prize1P = ps.addSphereObject(ps.nextUID(), mass, tempTransform, 3.0f);
		prize1P.setBounciness(0.5f);
		prize1.setPhysicsObject(prize1P);

		buildNpc();

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

			// update time
			elapsTime += frameDiff;
			im.update((float) elapsTime);

			// update player location
			mapHeight(avatar);
			positionCameraBehindAvatar();

			// update all sounds
			laserSound.setLocation(lazergun.getWorldLocation());
			walkingSound.setLocation(avatar.getWorldLocation());
			runningSound.setLocation(avatar.getWorldLocation());
			voiceline1.setLocation(avatar.getWorldLocation());
			riverSound.setLocation(prize1.getWorldLocation());
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
			voiceline1.setLocation(npc.getWorldLocation());
			setEarParameters();

			// process the networking functions
			processNetworking((float) elapsTime);

			// show/hide mouse logic
			checkMouse();
			setMouseVisible(false);

			// update animation
			avatarS.updateAnimation();

			// --------------------- PHYSICS LOGIC --------------------------//

			// update npc physics objects
			ps.removeObject(npcP.getUID());
			// move graphic objects
			mapHeight(npc);
			buildNpc();

			// if(running){
			Matrix4f matrix = new Matrix4f();
			Matrix4f rotMatrix = new Matrix4f();
			AxisAngle4f aAngle = new AxisAngle4f();
			Matrix4f identityMatrix = new Matrix4f().identity();
			checkCollisions();
			checkBulletDistances();
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
			mapHeight(prize1);

			// } If condition for running physics with scripts
		}
		// END if statement for game not paused

		// ----------------------------- HUD ----------------------------- //

		if (paused) {
			startupStr = "CHOOSE YOUR TEAM: Blue (1) OR Red (2)";
			Vector3f startupColor = new Vector3f(1, 0, 1);
			(engine.getHUDmanager()).setHUD1(startupStr, startupColor, 660, 540);// half of 1920, 1080
		} else {
			startupStr = "";
			Vector3f startupColor = new Vector3f(1, 0, 1);
			(engine.getHUDmanager()).setHUD1(startupStr, startupColor, 660, 540);// half of 1920, 1080
		}

		String scoreStr = "Score: " + Integer.toString(score);
		Vector3f scoreColor = new Vector3f(0, 1, 0);
		(engine.getHUDmanager()).setHUD2(scoreStr, scoreColor, 15, 15);

		// END Update
	}// END VariableFrameRate Game Overrides

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
					// handle collison it litterally detects nothing to handle
					System.out.println("Hit");
					break;
				}
			}
		}
	}

	private void checkBulletDistances() {
		for (int i = 0; i < lazers.size(); i++) {
			GameObject laz = lazers.get(i);
			if (distanceTo(laz, npc) <= 1.0f) {
				ps.removeObject(laz.getPhysicsObject().getUID());
				engine.getSceneGraph().removeGameObject(laz);
				lazers.remove(i);
				score++;
				System.out.println("Hit");
			}
		}
	}

	// ------------------------- KEY PRESSED ------------------------- //

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
			case KeyEvent.VK_F: {
				voiceline1.setLocation(avatar.getWorldLocation());
				setEarParameters();
				voiceline1.play();
				break;
			}
			case KeyEvent.VK_R: {
				System.out.println("reload");
				break;
			}
			case KeyEvent.VK_1: {
				System.out.println("Blue team chosen");
				avatartx = avatartxBlue;
				paused = !paused;
				break;
			}
			case KeyEvent.VK_2: {
				System.out.println("Red team chosen");
				avatartx = avatartxRed;
				buildObjects();// rebuild objects for red team
				paused = !paused;
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
		AudioResource resource1, resource2, resource3, resource4, resource5;
		audioMgr = AudioManagerFactory.createAudioManager(
				"tage.audio.joal.JOALAudioManager");
		if (!audioMgr.initialize()) {
			System.out.println("Audio Manager failed to initialize!");
			return;
		}

		resource1 = audioMgr.createAudioResource("assets/sounds/grassWalking.wav",
				AudioResourceType.AUDIO_SAMPLE);
		walkingSound = new Sound(resource1, SoundType.SOUND_EFFECT, 10, true);
		walkingSound.initialize(audioMgr);
		walkingSound.setMaxDistance(10.0f);
		walkingSound.setMinDistance(0.5f);
		walkingSound.setRollOff(2.0f);
		walkingSound.setLocation(avatar.getWorldLocation());

		resource2 = audioMgr.createAudioResource("assets/sounds/grassRunning.wav",
				AudioResourceType.AUDIO_SAMPLE);
		runningSound = new Sound(resource2, SoundType.SOUND_EFFECT, 5, true);
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

		resource4 = audioMgr.createAudioResource("assets/sounds/teatimeVoiceline.wav",
				AudioResourceType.AUDIO_SAMPLE);
		voiceline1 = new Sound(resource4, SoundType.SOUND_EFFECT, 70, false);
		voiceline1.initialize(audioMgr);
		voiceline1.setMaxDistance(10.0f);
		voiceline1.setMinDistance(0.5f);
		voiceline1.setRollOff(5.0f);
		voiceline1.setLocation(avatar.getWorldLocation());

		resource5 = audioMgr.createAudioResource(
				"assets/sounds/river.wav", AudioResourceType.AUDIO_SAMPLE);
		riverSound = new Sound(resource5,
				SoundType.SOUND_EFFECT, 90, true);
		riverSound.initialize(audioMgr);
		riverSound.setMaxDistance(10.0f);
		riverSound.setMinDistance(0.5f);
		riverSound.setRollOff(0.5f);
		riverSound.setLocation(prize1.getWorldLocation());

		setEarParameters();
		riverSound.play();
	}

	public void setEarParameters() {
		Camera camera = (engine.getRenderSystem()).getViewport("LEFT").getCamera();
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

	public class SendCloseConnectionPacketAction extends AbstractInputAction {

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

	private void mapHeight(GameObject object) {
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
		// System.out.println("fwdvec: "+fwd.x()+","+fwd.y()+","+fwd.z());
		newLazerP.setBounciness(.01f);
		newLazerP.setLinearVelocity(new float[] { fwd.x() * 100, fwd.y() * 100, fwd.z() * 100 });
		newLazer.setPhysicsObject(newLazerP);
		// newLazerP.applyForce(1000.0f, 0f, 0, 0, 0, 0);
		lazers.add(newLazer);
	}

	private void buildNpc() {
		float[] psize = { 1f, 2f, 1f };
		Matrix4f translation = new Matrix4f(npc.getLocalTranslation());
		double[] tempTransform = toDoubleArray(translation.get(vals));
		npcP = ps.addBoxObject(ps.nextUID(), 1.0f, tempTransform, psize);
		npcP.setDamping(.5f, .8f);
		npcP.setBounciness(1.0f);
		npc.setPhysicsObject(npcP);
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
		return p1tx;
	}

	public ObjShape getGhostShape() {
		return avatarS;
	}

	public TextureImage getGhostTexture() {
		return avatartx;
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