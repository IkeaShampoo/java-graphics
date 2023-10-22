package objects.util.graphics;

import objects.util.graphics.geometry.*;

public abstract class Shader {
	private double[][][] screenRGB;
	public void reshape(double[][][] newScreenRGB) {
		screenRGB = newScreenRGB;
	}

	public abstract void preload(Plane[] newPlanes, Light[] newLights, Vector newCamLocation);
	
	public abstract void draw(int x, int y, Vector point, Vector normal, double[] rgb, double gloss, int specularPower);
}