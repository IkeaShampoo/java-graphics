package objects.util.graphics;

import java.lang.Math;

import objects.util.graphics.geometry.*;

public abstract class Light {
	public Rotation orientation;
	public Vector displacement;
	public double[] rgb;
	
	public abstract void reset();
	
	public abstract Light clone();
	
	public abstract void cast(Plane[] planes);
	
	public abstract double cast(Vector point);
}