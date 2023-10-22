package objects.util.graphics;

import objects.util.graphics.geometry.*;

public abstract class Texture {
	public boolean ISLIGHTSOURCE;
	public abstract void write(Plane screenPlane, Plane projectedPlane, Plane worldPlane, Rotation camOrientation, Vector camDisplacement, int width, int height, double resolutionScalar, double[][] zBuffer, Shader shader);
}