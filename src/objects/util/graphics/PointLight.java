package objects.util.graphics;

import java.lang.Math;

import objects.util.graphics.geometry.*;

public class PointLight extends Light {
	private PyramidLight[] sides;
	private double halfAngle;
	private int resolution;
  private double nearPlane;
  private double farPlane;
	private Vector screenCenter;
  private double brightness;
	private final double tolerance = 0.001;
	public PointLight(int resolution, double minDistance, double maxDistance, double[] lightRGB, double intensity, Rotation originalOrientation, Vector originalDisplacement) {
    orientation = originalOrientation.clone();
    displacement = originalDisplacement.clone();
		this.resolution = resolution;
    halfAngle = Math.toRadians(45);
    rgb = lightRGB.clone();
		nearPlane = minDistance;
    farPlane = maxDistance;
		screenCenter = new Vector(new double[] {resolution/2, resolution/2, 0});
		double resolutionScalar = resolution/(Math.tan(halfAngle));
    brightness = intensity;
    
    sides = new PyramidLight[6];
    sides[0] = new PyramidLight(resolution, resolution, Math.toRadians(90), nearPlane, farPlane, rgb.clone(), brightness, orientation.clone(), displacement.clone());
    sides[1] = sides[0].clone();
    sides[1].orientation = sides[1].orientation.rotated(new Rotation(Math.toRadians(180), 1));
    sides[2] = sides[0].clone();
    sides[2].orientation = sides[2].orientation.rotated(new Rotation(Math.toRadians(-90), 1));
    sides[3] = sides[0].clone();
    sides[3].orientation = sides[3].orientation.rotated(new Rotation(Math.toRadians(90), 1));
    sides[4] = sides[0].clone();
    sides[4].orientation = sides[4].orientation.rotated(new Rotation(Math.toRadians(-90), 0));
    sides[5] = sides[0].clone();
    sides[5].orientation = sides[5].orientation.rotated(new Rotation(Math.toRadians(90), 0));
    //reset();
	}
	
	public void reset() {
		for (PyramidLight side:sides) {
			side.reset();
		}
	}
	
	public PointLight clone() {
		return new PointLight(resolution, nearPlane, farPlane, rgb.clone(), brightness, orientation.clone(), displacement.clone());
	}
	
	public void cast(Plane[] planes) {
		for (PyramidLight side:sides) {
			side.cast(planes);
		}
	}
	
	public double cast(Vector point) {
		double[] casts = new double[6];
		for (int i = 0; i < 6; i++) {
			casts[i] = sides[i].cast(point);
		}
		return Math.max(Math.max(casts[0], casts[1]), Math.max(Math.max(casts[2], casts[3]), Math.max(casts[4], casts[5])));
	}
}