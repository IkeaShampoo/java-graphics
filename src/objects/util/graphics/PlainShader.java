package objects.util.graphics;

import java.lang.Math;

import objects.util.graphics.geometry.*;

public class PlainShader extends Shader {
	private double[][][] screenRGB;
	public PlainShader() {
	}
	
	public void reshape(double[][][] newScreenRGB) {
		screenRGB = newScreenRGB;
	}

	public void preload(Plane[] newPlanes, Light[] newLights, Vector newCamLocation) {return;}
	
	public void draw(int x, int y, Vector point, Vector normal, double[] rgb, double gloss, int specularPower) {
		screenRGB[x][y] = rgb;
	}
}