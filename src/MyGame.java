package src;

import net.java.games.input.Component;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import tage.*;
import tage.input.InputManager;
import tage.nodeControllers.FlattenController;
import tage.nodeControllers.RotationController;
import tage.shapes.*;

import java.util.Random;
import java.util.Vector;

public class MyGame extends VariableFrameRateGame {
	// Static Variables
	private static Engine engine;
	private static Camera camMain, camSma;
	private static Random rand = new Random();

	// Basic Variables
	private boolean paused = false;
	private boolean newTarget = true;
	private boolean endGame = false;
	private double lastFrameTime, currFrameTime, elapsTime, frameDiff;
	private float distToP1, distToP2, distToP3, distToP4;
	private float trailLength = -2.0f;
	private int score = 0;
	private int width;
	// private int fluffyClouds;
	private int lakeIslands;

	// Tage Class Variables
	private CameraOrbit3D orbitCam;
	private InputManager im;
	private GameObject dol, blob, prize1, prize2, prize3, prize4, ground, x, y, z, lastTarget, nextTarget,
			lastestTarget;
	private Vector<GameObject> objects;
	private ObjShape dolS, blobS, prize1S, prize2S, prize3S, prize4S, groundS, linxS, linyS, linzS, terrS;
	private TextureImage doltx, blobtx, johntx, p1tx, p2tx, p4tx, groundtx, river;
	private Light light1;
	private Vector3f lastCamLocation;
	private NodeController rc1, rc2, rc3, rc4, fc;


	public MyGame() {
		super();
		objects = new Vector<>();
	}

	public static void main(String[] args) {
		MyGame game = new MyGame();
		engine = new Engine(game);
		game.initializeSystem();
		game.game_loop();
	}

	// VariableFrameRate Game Overrides
	@Override
	public void loadShapes() {
		dolS = new ImportedModel("dolphinHighPoly.obj");
		blobS = new Cube();
		prize1S = new Torus();
		prize2S = new Cube();
		prize3S = new Sphere();
		prize4S = new HexBlock();
		groundS = new Plane();
		linxS = new Line(new Vector3f(0f, 0f, 0f), new Vector3f(50f, 0f, 0f));
		linyS = new Line(new Vector3f(0f, 0f, 0f), new Vector3f(0f, 50f, 0f));
		linzS = new Line(new Vector3f(0f, 0f, 0f), new Vector3f(0f, 0f, 50f));
		terrS = new TerrainPlane(1000);
	}

	@Override
	public void loadTextures() {
		doltx = new TextureImage("Dolphin_HighPolyUV.png");
		blobtx = new TextureImage("pattern___glowing_coal_by_n4pgamer-d4jqm5c_edit.jpg");
		p2tx = new TextureImage("gold_energy.jpg");
		p1tx = new TextureImage("tex_Water.jpg");
		p4tx = new TextureImage("rooffrance.jpg");
		johntx = new TextureImage("galt_cow.jpg");
		groundtx = new TextureImage("grass2.png");
		river = new TextureImage("river.jpg");
	}

	@Override
	public void buildObjects() {
		Matrix4f initialTranslation, initialScale, initialRotation;

		// build dolphin in the center of the window
		dol = new GameObject(GameObject.root(), dolS, doltx);
		initialTranslation = (new Matrix4f()).translation(0f, 2f, 0f);
		initialScale = (new Matrix4f()).scaling(4.0f);
		dol.setLocalTranslation(initialTranslation);
		initialRotation = (new Matrix4f()).rotationY((float) Math.toRadians(135.0f));
		dol.setLocalRotation(initialRotation);
		dol.setLocalScale(initialScale);
		// build prize 1
		prize1 = new GameObject(GameObject.root(), prize1S, p1tx);
		initialTranslation = (new Matrix4f()).translation((rand.nextInt(100) + (-50)), 2f, (rand.nextInt(100) + (-50)));
		initialScale = (new Matrix4f()).scaling(3.0f);
		prize1.setLocalTranslation(initialTranslation);
		prize1.setLocalScale(initialScale);
		prize1.getRenderStates().setTiling(1);
		// build prize 2
		prize2 = new GameObject(GameObject.root(), prize2S, p2tx);
		initialTranslation = (new Matrix4f()).translation((rand.nextInt(100) + (-50)), 2f, (rand.nextInt(100) + (-50)));
		initialScale = (new Matrix4f()).scaling(2.0f);
		prize2.setLocalTranslation(initialTranslation);
		prize2.setLocalScale(initialScale);
		// build prize 3
		prize3 = new GameObject(GameObject.root(), prize3S, johntx);
		initialTranslation = (new Matrix4f()).translation((rand.nextInt(100) + (-50)), 2f, (rand.nextInt(100) + (-50)));
		initialScale = (new Matrix4f()).scaling(3.5f, 2.0f, 3.5f);
		prize3.setLocalTranslation(initialTranslation);
		prize3.setLocalScale(initialScale);
		// build prize 4
		prize4 = new GameObject(GameObject.root(), prize4S, p4tx);
		initialTranslation = (new Matrix4f()).translation((rand.nextInt(100) + (-50)), 2f, (rand.nextInt(100) + (-50)));
		initialScale = (new Matrix4f()).scaling(4.0f, 2.0f, 4.0f);
		prize4.setLocalTranslation(initialTranslation);
		prize4.setLocalScale(initialScale);
		// build world axes
		x = new GameObject(GameObject.root(), linxS);
		y = new GameObject(GameObject.root(), linyS);
		z = new GameObject(GameObject.root(), linzS);
		(x.getRenderStates()).setColor(new Vector3f(1f, 0f, 0f));
		(y.getRenderStates()).setColor(new Vector3f(0f, 1f, 0f));
		(z.getRenderStates()).setColor(new Vector3f(0f, 0f, 1f));

		// build the blob
		blob = new GameObject(GameObject.root(), blobS, blobtx);
		blob.setMoveFact(1.75f);
		initialTranslation = (new Matrix4f()).translation((rand.nextInt(100) + (-50)), 2f, (rand.nextInt(100) + (-50)));
		initialScale = (new Matrix4f()).scaling((blob.getMoveFact()));
		blob.setLocalTranslation(initialTranslation);
		blob.setLocalScale(initialScale);

		// build the ground
		ground = new GameObject(GameObject.root(), terrS, groundtx);
		initialTranslation = (new Matrix4f().translation(0f, 0, 0f));
		initialScale = (new Matrix4f()).scaling(50.0f,2.0f,50.0f);
		ground.setLocalTranslation(initialTranslation);
		ground.setLocalScale(initialScale);
		ground.setHeightMap(river);

		// add objects to vector
		objects.add(dol);
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
		(engine.getRenderSystem()).addViewport("RIGHT", .75f, 0, .25f, .25f);

		Viewport leftVp = (engine.getRenderSystem()).getViewport("LEFT");
		Viewport rightVp = (engine.getRenderSystem()).getViewport("RIGHT");
		Camera leftCamera = leftVp.getCamera();
		Camera rightCamera = rightVp.getCamera();

		rightVp.setHasBorder(true);
		rightVp.setBorderWidth(4);
		rightVp.setBorderColor(0.0f, 1.0f, 0.0f);

		leftCamera.setLocation(new Vector3f(-2, 0, 2));
		leftCamera.setU(new Vector3f(1, 0, 0));
		leftCamera.setV(new Vector3f(0, 1, 0));
		leftCamera.setN(new Vector3f(0, 0, -1));

		rightCamera.setLocation(new Vector3f(0, 3, 0));
		rightCamera.setU(new Vector3f(1, 0, 0));
		rightCamera.setV(new Vector3f(0, 0, -1));
		rightCamera.setN(new Vector3f(0, -1, 0));
	}

	@Override
	public void initializeGame() {
		lastFrameTime = System.currentTimeMillis();
		currFrameTime = System.currentTimeMillis();
		elapsTime = 0.0;
		(engine.getRenderSystem()).setWindowDimensions(3800, 2000);

		// ------------- camera init -------------
		im = engine.getInputManager();
		camMain = (engine.getRenderSystem().getViewport("LEFT").getCamera());
		camSma = (engine.getRenderSystem().getViewport("RIGHT").getCamera());
		camSma.setCameraDist(5.0f);
		camSma.setUpViewport();
		orbitCam = new CameraOrbit3D(camMain, dol, im, engine);

		// ---------------- game logic --------------------//
		score = 0;
		nextTarget = null;
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
		fc = new FlattenController(dol.getLocalScale().m11());
		fc.addTarget(dol);
		(engine.getSceneGraph()).addNodeController(fc);

		// --------------Input Zone-----------------//

		TurnAction turnAction = new TurnAction(this);
		MoveAction moveAction = new MoveAction(this);
		PauseAction pauseAction = new PauseAction(this);
		// StrafeAction strafeAction = new StrafeAction(this);
		PanCameraAction panAction = new PanCameraAction(this);
		ZoomCameraAction zoomAction = new ZoomCameraAction(this);
		ToggleTransparentAction transAction = new ToggleTransparentAction(this, x, y, z);

		// Keyboard Actions
		im.associateActionWithAllKeyboards(Component.Identifier.Key.A, turnAction,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(Component.Identifier.Key.D, turnAction,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(Component.Identifier.Key.W, moveAction,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(Component.Identifier.Key.S, moveAction,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(Component.Identifier.Key.J, panAction,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(Component.Identifier.Key.L, panAction,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(Component.Identifier.Key.I, panAction,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(Component.Identifier.Key.K, panAction,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(Component.Identifier.Key.COMMA, zoomAction,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(Component.Identifier.Key.PERIOD, zoomAction,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(Component.Identifier.Key.T, transAction,
				InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);

		// Extra keyboard actions
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.P, pauseAction,
				InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		// add strafe

		// Gamepad Actions
		im.associateActionWithAllGamepads(Component.Identifier.Axis.X, turnAction,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllGamepads(Component.Identifier.Axis.Y, moveAction,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllGamepads(Component.Identifier.Button._0, panAction,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllGamepads(Component.Identifier.Button._1, panAction,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllGamepads(Component.Identifier.Button._2, panAction,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllGamepads(Component.Identifier.Button._3, panAction,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllGamepads(Component.Identifier.Axis.Z, zoomAction,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllGamepads(Component.Identifier.Button._6, transAction,
				InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		// Extra Gamepad Actions
		im.associateActionWithAllGamepads(Component.Identifier.Button._7, pauseAction,
				InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		// add strafe
	}

	@Override
	public void update() { 
		lastFrameTime = currFrameTime;
		currFrameTime = System.currentTimeMillis();
		frameDiff = (currFrameTime - lastFrameTime) / 1000.0;
		width = (engine.getRenderSystem()).getWidth();

		if (paused)
			im.update((float) elapsTime);
		if (!paused && !endGame) {
			elapsTime += frameDiff;
			im.update((float) elapsTime);
			orbitCam.updateCameraPosition();
			distToP1 = distanceToDolphin(prize1);
			distToP2 = distanceToDolphin(prize2);
			distToP3 = distanceToDolphin(prize3);
			distToP4 = distanceToDolphin(prize4);

			//player elevation
			Vector3f loc = dol.getWorldLocation();
			float height = ground.getHeight(loc.x(), loc.z());
			dol.setLocalLocation(new Vector3f(loc.x(), height, loc.z()));

			// enemy blob logic
			if (blobMovement())
			{
				if (newTarget) {
					newTarget();
					newTarget = false;
				
				}
				blob.lookAt(nextTarget);
				blob.move((float) frameDiff);
				for (int i = 0; i < objects.size(); i++) {
					if (blobContact(objects.elementAt(i)) && objects.elementAt(i) == nextTarget) {
						if (objects.elementAt(i) == dol) {
							blob.setMoveFact(blob.getMoveFact() + 0.25f);
							blob.changeScale(blob.getMoveFact());
							fc.enable();
						} else {
							blob.setMoveFact(blob.getMoveFact() + 0.50f);
							blob.changeScale(blob.getMoveFact());
						}
						score--;
						newTarget = true;
						lastestTarget = lastTarget;
						lastTarget = nextTarget;
					}
				}

			}

			//Player logic
			if (distToP1 <= prize1.getScale() && !prize1.isCollected()) {
				score++;
				prize1.collect();
				rc1.toggle();
				prize1.setParent(dol);
				prize1.propagateRotation(false);
				prize1.setLocalScale(new Matrix4f().scaling(0.25f));
				prize1.applyParentRotationToPosition(true);
				prize1.setLocalTranslation(new Matrix4f().translation(0f, 0f, trailLength));
				trailLength += -1.5f;
				blob.setMoveFact(blob.getMoveFact() - 0.50f);
				blob.changeScale(blob.getMoveFact());
			}
			if (distToP2 <= prize2.getScale() && !prize2.isCollected()) {
				score++;
				prize2.collect();
				rc2.toggle();
				prize2.setParent(dol);
				prize2.propagateRotation(false);
				prize2.setLocalScale(new Matrix4f().scaling(0.25f));
				prize2.applyParentRotationToPosition(true);
				prize2.setLocalTranslation(new Matrix4f().translation(0f, 0f, trailLength));
				trailLength += -1.5f;
				blob.setMoveFact(blob.getMoveFact() - 0.50f);
				blob.changeScale(blob.getMoveFact());
			}
			if (distToP3 <= prize3.getScale() && !prize3.isCollected()) {
				score++;
				prize3.collect();
				rc3.toggle();
				prize3.setParent(dol);
				prize3.propagateRotation(false);
				prize3.setLocalScale(new Matrix4f().scaling(0.25f));
				prize3.applyParentRotationToPosition(true);
				prize3.setLocalTranslation(new Matrix4f().translation(0f, 0f, trailLength));
				trailLength += -1.5f;
				blob.setMoveFact(blob.getMoveFact() - 0.50f);
				blob.changeScale(blob.getMoveFact());
			}
			if (distToP4 <= prize4.getScale() && !prize4.isCollected()) {
				score++;
				prize4.collect();
				rc4.toggle();
				prize4.setParent(dol);
				prize4.propagateRotation(false);
				prize4.setLocalScale(new Matrix4f().scaling(0.25f));
				prize4.applyParentRotationToPosition(true);
				prize4.setLocalTranslation(new Matrix4f().translation(0f, 0f, trailLength));
				trailLength += -1.5f;
				blob.setMoveFact(blob.getMoveFact() - 0.50f);
				blob.changeScale(blob.getMoveFact());
			}
			
		} //pause scope and end game cutoff
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

		String scoreStr = "Score: " + Integer.toString(score);
		String dolLocStr = "X: " + dol.getWorldLocation().x() + "  Z: " + dol.getWorldLocation().z();
		Vector3f dolLocColor = new Vector3f(1, 1, 1);
		Vector3f scoreColor = new Vector3f(0, 1, 0);
		String winStr = "You Have Beaten The Blob";
		Vector3f winColor = new Vector3f(0, 1, 0);
		String loseStr = "The Blob Has Beaten You";
		Vector3f loseColor = new Vector3f(1, 0, 0);
		if (prize1.isCollected() && prize2.isCollected() && prize3.isCollected() && prize4.isCollected()
				|| blob.getScale() < 0.25f) {
			(engine.getHUDmanager()).setHUD1(winStr, winColor, (int) (width * 0.75f), 15);
			endGame = true;
		} else if (blob.getScale() > 20.0f) {
			(engine.getHUDmanager()).setHUD1(loseStr, loseColor, (int) (width * 0.75f), 15);
			endGame = true;
		} else
			(engine.getHUDmanager()).setHUD1(dolLocStr, dolLocColor, (int) (width * 0.75f), 15);
		(engine.getHUDmanager()).setHUD2(scoreStr, scoreColor, 15, 15);
	}

	// Custom Functions
	public GameObject getDolphin() {
		return dol;
	}

	public static Engine getEngine() {
		return engine;
	}

	public static Camera getMainCamera() {
		return camMain;
	}

	public static Camera getSmallCamera() {
		return camSma;
	}

	public void togglePause() {
		paused = !paused;
	}

	public float getFrameDiff() {
		return (float) frameDiff;
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

	/** Returns distance of a GameObject to the dolphin player */
	public float distanceToDolphin(GameObject obj) {
		return dol.getWorldLocation().distance(obj.getWorldLocation());
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

	public GameObject newTarget() {
		if (nextTarget == null)
			nextTarget = objects.elementAt(0);
		else {
			if (lastestTarget != dol && nextTarget != dol)
				nextTarget = objects.elementAt(0);
			else if (lastestTarget != prize1 && nextTarget != prize1)
				nextTarget = objects.elementAt(1);
			else if (lastestTarget != prize2 && nextTarget != prize2)
				nextTarget = objects.elementAt(2);
			else if (lastestTarget != prize3 && nextTarget != prize3)
				nextTarget = objects.elementAt(3);
			else if (lastestTarget != prize4 && nextTarget != prize4)
				nextTarget = objects.elementAt(4);
		}
		for (int i = 0; i < objects.size(); i++) {
			if (objects.elementAt(i) != lastTarget && objects.elementAt(i) != lastestTarget) {
				if (distanceTo(nextTarget, blob) >= distanceTo(objects.elementAt(i), blob)) {
					nextTarget = objects.elementAt(i);
				}
			}
		}
		return nextTarget;
	}

	public boolean blobContact(GameObject contact) {
		if (distanceTo(blob, contact) <= contact.getScale())
			return true;
		return false;
	}

	private boolean blobMovement() {
		int moveBlob = (int) elapsTime % 2;
		if (moveBlob == 0)
			return true;
		return false;
	}
}