package client;

import tage.Utils;
import tage.shapes.ManualObject;

public class HexBlock extends ManualObject {
	// an effort in futility is only so to the unambitious
	private float[] vertices = new float[] {
			// upper hexagon
			-1.0f, 1.0f, 0.0f, -0.5f, 1.0f, 0.87f, 0.0f, 1.0f, 0.0f,
			-0.5f, 1.0f, 0.87f, 0.5f, 1.0f, 0.87f, 0.0f, 1.0f, 0.0f,
			0.5f, 1.0f, 0.87f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
			1.0f, 1.0f, 0.0f, 0.5f, 1.0f, -0.87f, 0.0f, 1.0f, 0.0f,
			0.5f, 1.0f, -0.87f, -0.5f, 1.0f, -0.87f, 0.0f, 1.0f, 0.0f,
			-0.5f, 1.0f, -0.87f, -1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
			// top hexagon sides
			-1.0f, 1.0f, 0.0f, -0.5f, 1.0f, 0.87f, -0.5f, -1.0f, 0.87f, //
			-0.5f, 1.0f, 0.87f, 0.5f, 1.0f, 0.87f, 0.5f, -1.0f, 0.87f,
			0.5f, 1.0f, 0.87f, 1.0f, 1.0f, 0.0f, 1.0f, -1.0f, 0.0f,
			1.0f, 1.0f, 0.0f, 0.5f, 1.0f, -0.87f, 0.5f, -1.0f, -0.87f,
			0.5f, 1.0f, -0.87f, -0.5f, 1.0f, -0.87f, -0.5f, -1.0f, -0.87f,
			-0.5f, 1.0f, -0.87f, -1.0f, 1.0f, 0.0f, -1.0f, -1.0f, 0.0f,
			// lower hexagon side
			-1.0f, -1.0f, 0.0f, -0.5f, -1.0f, 0.87f, -1.0f, 1.0f, 0.0f, //
			-0.5f, -1.0f, 0.87f, 0.5f, -1.0f, 0.87f, -0.5f, 1.0f, 0.87f,
			0.5f, -1.0f, 0.87f, 1.0f, -1.0f, 0.0f, 0.5f, 1.0f, 0.87f,
			1.0f, -1.0f, 0.0f, 0.5f, -1.0f, -0.87f, 1.0f, 1.0f, 0.0f,
			0.5f, -1.0f, -0.87f, -0.5f, -1.0f, -0.87f, 0.5f, 1.0f, -0.87f,
			-0.5f, -1.0f, -0.87f, -1.0f, -1.0f, 0.0f, -0.5f, 1.0f, -0.87f,
			// lower hexagon
			-1.0f, -1.0f, 0.0f, -0.5f, -1.0f, 0.87f, 0.0f, -1.0f, 0.0f,
			-0.5f, -1.0f, 0.87f, 0.5f, -1.0f, 0.87f, 0.0f, -1.0f, 0.0f,
			0.5f, -1.0f, 0.87f, 1.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f,
			1.0f, -1.0f, 0.0f, 0.5f, -1.0f, -0.87f, 0.0f, -1.0f, 0.0f,
			0.5f, -1.0f, -0.87f, -0.5f, -1.0f, -0.87f, 0.0f, -1.0f, 0.0f,
			-0.5f, -1.0f, -0.87f, -1.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f
	};
	private float[] texcords = new float[] {
			// upper hexagon
			0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
			0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
			0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
			0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
			0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
			0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
			// top hexagon sides
			0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,
			0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,
			0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,
			0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,
			0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,
			0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,
			// lower hexagon sides
			0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,
			0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,
			0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,
			0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,
			0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,
			0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,
			/*
			 * 1.0f,1.0f, 0.0f,0.0f, 1.0f,0.0f,
			 * 1.0f,1.0f, 0.0f,0.0f, 1.0f,0.0f,
			 * 1.0f,1.0f, 0.0f,0.0f, 1.0f,0.0f,
			 * 1.0f,1.0f, 0.0f,0.0f, 1.0f,0.0f,
			 * 1.0f,1.0f, 0.0f,0.0f, 1.0f,0.0f,
			 * 1.0f,1.0f, 0.0f,0.0f, 1.0f,0.0f,
			 * 
			 */
			// lower hexagon
			0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
			0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
			0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
			0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
			0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
			0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f
	};
	private float[] normals = new float[] {
			// upper hexagon
			0.0f, 0.87f, 0.0f, 0.0f, 0.87f, 0.0f, 0.0f, 0.87f, 0.0f,
			0.0f, 0.87f, 0.0f, 0.0f, 0.87f, 0.0f, 0.0f, 0.87f, 0.0f,
			0.0f, 0.87f, 0.0f, 0.0f, 0.87f, 0.0f, 0.0f, 0.87f, 0.0f,
			0.0f, 0.87f, 0.0f, 0.0f, 0.87f, 0.0f, 0.0f, 0.87f, 0.0f,
			0.0f, 0.87f, 0.0f, 0.0f, 0.87f, 0.0f, 0.0f, 0.87f, 0.0f,
			0.0f, 0.87f, 0.0f, 0.0f, 0.87f, 0.0f, 0.0f, 0.87f, 0.0f,
			// top hexagon sides
			-1.74f, 0.0f, 1.0f, -1.74f, 0.0f, 1.0f, -1.74f, 0.0f, 1.0f,
			0.0f, 0.0f, 2.0f, 0.0f, 0.0f, 2.0f, 0.0f, 0.0f, 2.0f,
			1.74f, 0.0f, 1.0f, 1.74f, 0.0f, 1.0f, 1.74f, 0.0f, 1.0f,
			1.74f, 0.0f, -1.0f, 1.74f, 0.0f, -1.0f, 1.74f, 0.0f, -1.0f,
			0.0f, 0.0f, 2.0f, 0.0f, 0.0f, 2.0f, 0.0f, 0.0f, 2.0f,
			-1.74f, 0.0f, -1.0f, -1.74f, 0.0f, -1.0f, -1.74f, 0.0f, -1.0f,
			// lower hexagon sides
			-1.74f, 0.0f, 1.0f, -1.74f, 0.0f, 1.0f, -1.74f, 0.0f, 1.0f,
			0.0f, 0.0f, 2.0f, 0.0f, 0.0f, 2.0f, 0.0f, 0.0f, 2.0f,
			1.74f, 0.0f, 1.0f, 1.74f, 0.0f, 1.0f, 1.74f, 0.0f, 1.0f,
			1.74f, 0.0f, -1.0f, 1.74f, 0.0f, -1.0f, 1.74f, 0.0f, -1.0f,
			0.0f, 0.0f, 2.0f, 0.0f, 0.0f, 2.0f, 0.0f, 0.0f, 2.0f,
			-1.74f, 0.0f, -1.0f, -1.74f, 0.0f, -1.0f, -1.74f, 0.0f, -1.0f,
			// lower hexagon
			0.0f, -0.87f, 0.0f, 0.0f, -0.87f, 0.0f, 0.0f, -0.87f, 0.0f,
			0.0f, -0.87f, 0.0f, 0.0f, -0.87f, 0.0f, 0.0f, -0.87f, 0.0f,
			0.0f, -0.87f, 0.0f, 0.0f, -0.87f, 0.0f, 0.0f, -0.87f, 0.0f,
			0.0f, -0.87f, 0.0f, 0.0f, -0.87f, 0.0f, 0.0f, -0.87f, 0.0f,
			0.0f, -0.87f, 0.0f, 0.0f, -0.87f, 0.0f, 0.0f, -0.87f, 0.0f,
			0.0f, -0.87f, 0.0f, 0.0f, -0.87f, 0.0f, 0.0f, -0.87f, 0.0f
	};

	public HexBlock() {
		super();

		setNumVertices(72);
		setVertices(vertices);
		setTexCoords(texcords);
		setNormals(normals);

		setMatAmb(Utils.silverAmbient());
		setMatDif(Utils.silverDiffuse());
		setMatSpe(Utils.silverSpecular());
		setMatShi(Utils.silverShininess());
	}

}
