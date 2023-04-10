package tage;
import org.joml.*;

/**
* A Camera specifies the rendering viewpoint for a Viewport.
* A Camera instance includes fields for location, and U, V, N vectors.
* It includes look-at() methods for pointing the camera at a given location or object.
* It also includes a method for generating a VIEW matrix.
* <p>
* U, V, and N must form an orthogonal left-handed system of axes indicating the camera orientation.
* Note that this is NOT a camera controller - however, a controller could be written
* for a Camera instance by modifying location, U, V, and N based on user input.
* The default camera position is at (0,0,1) looking down the -Z axis towards the origin.
* @author Scott Gordon
*/

public class Camera
{
	private Vector3f u, v, n;
	private Vector3f defaultU, defaultV, defaultN;
	private Vector3f location, defaultLocation;
	private Matrix4f view, viewR, viewT;
	private float cameraDist = 0.0f;

	/** instantiates a Camera object at location (0,0,1) and facing down the -Z axis towards the origin */
	public Camera()
	{	defaultLocation = new Vector3f(0.0f, 0.0f, 1.0f);
		defaultU = new Vector3f(1.0f, 0.0f, 0.0f);
		defaultV = new Vector3f(0.0f, 1.0f, 0.0f);
		defaultN = new Vector3f(0.0f, 0.0f, -1.0f);
		location = new Vector3f(defaultLocation);
		u = new Vector3f(defaultU);
		v = new Vector3f(defaultV);
		n = new Vector3f(defaultN);
		view = new Matrix4f();
		viewR = new Matrix4f();
		viewT = new Matrix4f();
	}

	/** sets the world location of this Camera */
	public void setLocation(Vector3f l) { location.set(l); }

	/** sets the U (right-facing) vector for this Camera */
	public void setU(Vector3f newU) { u.set(newU); }

	/** sets the V (upward-facing) vector for this Camera */
	public void setV(Vector3f newV) { v.set(newV); }

	/** sets the N (forward-facing) vector for this Camera */
	public void setN(Vector3f newN) { n.set(newN); }

	/** returns the world location of this Camera */
	public Vector3f getLocation() { return new Vector3f(location); }

	/** gets the U (right-facing) vector for this Camera */
	public Vector3f getU() { return new Vector3f(u); }

	/** gets the V (upward-facing) vector for this Camera */
	public Vector3f getV() { return new Vector3f(v); }

	/** gets the N (forward-facing) vector for this Camera */
	public Vector3f getN() { return new Vector3f(n); }


	/** orients this Camera so that it faces a specified Vector3f world location */
	public void lookAt(Vector3f target) { lookAt(target.x(), target.y(), target.z()); }

	/** orients this Camera so that it faces a specified GameObject */
	public void lookAt(GameObject go) { lookAt(go.getWorldLocation()); }

	/** orients this Camera so that it faces a specified (x,y,z) world location */
	public void lookAt(float x, float y, float z)
	{	setN((new Vector3f(x-location.x(), y-location.y(), z-location.z())).normalize());
		Vector3f copyN = new Vector3f(n);
		if (n.equals(0,1,0))
			u = new Vector3f(1f,0f,0f);
		else
			u = (new Vector3f(copyN.cross(0f,1f,0f))).normalize();
		Vector3f copyU = new Vector3f(u);
		v = (new Vector3f(copyU.cross(n))).normalize();
	}

	protected Matrix4f getViewMatrix()
	{	viewT.set(1.0f, 0.0f, 0.0f, 0.0f,
		0.0f, 1.0f, 0.0f, 0.0f,
		0.0f, 0.0f, 1.0f, 0.0f,
		-location.x(), -location.y(), -location.z(), 1.0f);

		viewR.set(u.x(), v.x(), -n.x(), 0.0f,
		u.y(), v.y(), -n.y(), 0.0f,
		u.z(), v.z(), -n.z(), 0.0f,
		0.0f, 0.0f, 0.0f, 1.0f);

		view.identity();
		view.mul(viewR);
		view.mul(viewT);

		return(view);
	}

	/** This function takes a camera position and rotates around V axis based on the difference in time and direction*/
	public void yaw(float diff, boolean left){
		if(left) {
			setU(u.rotateAxis(1.0f * diff, v.x(), v.y(), v.z()));
			setN(n.rotateAxis(1.0f * diff, v.x(), v.y(), v.z()));
		}
		else {
			setU(u.rotateAxis(-1.0f * diff, v.x(), v.y(), v.z()));
			setN(n.rotateAxis(-1.0f * diff, v.x(), v.y(), v.z()));
		}
	}

	/** This function take a camera position and rotates around the U axis based on the difference in time and direction */
	public void pitch(float diff, boolean up) {
		if(up){
			setV(v.rotateAxis(1.0f * diff, u.x(), u.y(), u.z()));
			setN(n.rotateAxis(1.0f * diff, u.x(), u.y(), u.z()));
		} else {
			setV(v.rotateAxis(-1.0f * diff, u.x(), u.y(), u.z()));
			setN(n.rotateAxis(-1.0f * diff, u.x(), u.y(), u.z()));
		}
	}

	/** This function moves the camera down the positive V axis*/
	public void panUp(float diff){
		Vector3f up = getV().mul(1.0f * diff);
		location.add(up.x(), up.y(), up.z());
	}

	/** This function moves the camera down the negative V axis*/
	public void panDown(float diff) {
		Vector3f down = getV().mul(1.0f * diff);
		location.sub(down.x(), down.y(), down.z());
	}

	/** This function moves the camera down the negative U axis*/
	public void panLeft(float diff) {
		Vector3f left = getU().mul(1.0f * diff);
		location.sub(left.x(), left.y(), left.z());
	}

	/** This function moves the camera down the positive U axis*/
	public void panRight(float diff) {
		Vector3f right = getU().mul(1.0f * diff);
		location.add(right.x(), right.y(), right.z());
	}

	/** This function set the class variable camera distance*/
	public void setCameraDist(float cameraDist) {
		this.cameraDist = cameraDist;
	}

	/** This function moves the cameras location on the Y axis by changing the class variable and applying that to
	 * the current XZ location */
	public void zoom(float diff, boolean zout) {
		if (zout)
			cameraDist += (3.0f * diff);
		else
			cameraDist -= (3.0f * diff);
		if (cameraDist <= 0.0f) { cameraDist = 0.0f; }
		setLocation(new Vector3f(location.x(), cameraDist, location.z()));
		//System.out.println("outer condition y: "+location.y());
	}

	/** This function sets up the viewport with V along the X-axis and U along the Z axis*/
	public void setUpViewport(){
		setV(new Vector3f(1f,0f,0f));
		setN(new Vector3f(0f,-1f,0f));
		setU(new Vector3f(0f,0f,1f));
		setLocation(new Vector3f(0.0f, cameraDist, 0.0f));
	}
	/*
	public void moveWithDolphin(GameObject pl) {
		Vector3f newPos = pl.getWorldLocation().mul(1f, 0f, 1f);
		setLocation(new Vector3f(newPos.x(), cameraDist, newPos.z()));
	}
	public void lookDownAtDolphin(GameObject pl) {
		setN(pl.getWorldUpVector().mul(-1.0f));
		setV(pl.getWorldForwardVector());
		setU(pl.getWorldRightVector().mul(-1.0f));

	}
*/

}
