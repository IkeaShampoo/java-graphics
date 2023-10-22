package objects.util.graphics;

import java.lang.Math;
import java.util.ArrayList;

import objects.util.graphics.geometry.*;

public class Camera extends ImageSource {
	private final Shader primaryShader;
	private final Shader plainShader;
	private double[][] zBuffer;
	private double[][][] screenRGB;
	private double[] defaultRGB;
	private int WIDTH;
	private int HEIGHT;
	private double halfFOV;
	private Vector screenCenter;
	private double resolutionScalar;
	private double nearPlane;
	private double farPlane;
	private Rotation orientation;
	private Vector displacement;
	private Texture[] textures = new Texture[] {};
	private Plane[] planes = new Plane[] {};
	private Light[] lights = new Light[] {};
	public Camera(int width, int height, double fov, double minDistance, double maxDistance, Shader graphicsShader) {
		halfFOV = fov/2;
		nearPlane = minDistance;
		farPlane = maxDistance;
		primaryShader = graphicsShader;
		plainShader = new PlainShader();
		reshape(width, height);
		defaultRGB = new double[] {0, 0, 0};
		reset();
		orientation = new Rotation();
		displacement = new Vector();
	}
	
	public Camera(int width, int height, double fov, double minDistance, double maxDistance, Shader graphicsShader, Rotation originalOrientation, Vector originalDisplacement) {
		halfFOV = fov/2;
		nearPlane = minDistance;
		farPlane = maxDistance;
		primaryShader = graphicsShader;
		plainShader = new PlainShader();
		reshape(width, height);
		defaultRGB = new double[] {0, 0, 0};
		reset();
		orientation = originalOrientation;
		displacement = originalDisplacement;
	}
	
	private void reset() {
		for (int x = 0; x < WIDTH; x++) {
			for (int y = 0; y < HEIGHT; y++) {
				zBuffer[x][y] = farPlane;
				screenRGB[x][y][0] = defaultRGB[0];
				screenRGB[x][y][1] = defaultRGB[1];
				screenRGB[x][y][2] = defaultRGB[2];
			}
		}
	}
	
	public void reshape(int newWidth, int newHeight) {
		WIDTH = newWidth;
		HEIGHT = newHeight;
		zBuffer = new double[WIDTH][HEIGHT];
		screenRGB = new double[newWidth][newHeight][3];
		primaryShader.reshape(screenRGB);
		plainShader.reshape(screenRGB);
		screenCenter = new Vector(new double[] {WIDTH/2, HEIGHT/2, 0});
		resolutionScalar = Math.max(WIDTH, HEIGHT)/(2*Math.tan(halfFOV)); // Math.tan(halfFOV) sine or tangent? Tangent, right?
	}
	
	public void setDefaultColor(double[] newDefaultRGB) {
		defaultRGB = newDefaultRGB;
	}
	
	public void setPosition(Vector newPosition) {
		displacement = newPosition;
	}
	
	public void setOrientation(Rotation newOrientation) {
		orientation = newOrientation;
	}
	
	public Vector getPosition() {
		return displacement;
	}
	
	public Rotation getOrientation() {
		return orientation;
	}
	
	public void feed(Plane[] newPlanes, Texture[] newTextures, Light[] newLights) {
		planes = newPlanes;
		textures = newTextures;
		lights = newLights;
	}
	
	public void render() {
		reset();
		Rotation momentOrientation = orientation.clone();
		Vector momentDisplacement = displacement.clone();
		primaryShader.preload(planes, lights, momentDisplacement);
		for (int i = 0; i < planes.length; i++) {
			Shader planeShader;
			Texture planeTexture = textures[i];
			if (planeTexture.ISLIGHTSOURCE) {
				planeShader = plainShader;
			} else {
				planeShader = primaryShader;
			}
			Plane projectedPlane = planes[i].subtract(momentDisplacement).project(momentOrientation).scale(resolutionScalar);
			if (projectedPlane.testForUnflattening()) {
				Plane[] clipPlanes = projectedPlane.clip(nearPlane);
				
				//System.out.println("PLANE " + i);
				//planes[i].print();
				
				for (int j = 0; j < clipPlanes.length; j++) {
				
					//clipPlanes[j].print(1);
					
					Plane screenPlane = clipPlanes[j].flatten().add(screenCenter);
					Plane[] cutPlanes = screenPlane.cut();
					
					//System.out.println("    Clip " + j);
					//System.out.println("    Cut planes: " + cutPlanes.length);
					
					for (int k = 0; k < cutPlanes.length; k++) {
						
						//System.out.println("    Cut " + j + ", " + k);
						//cutPlanes[k].print(2);
						
						planeTexture.write(cutPlanes[k], projectedPlane, planes[i], momentOrientation, momentDisplacement, WIDTH, HEIGHT, resolutionScalar, zBuffer, planeShader);
					}
				}
			}
		}
	}
	
	public void load(int[] pixels) {
		int width = screenRGB.length;
		int height = screenRGB[0].length;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int red = 65536*((int)(255*Math.min(screenRGB[x][y][0], 1)));
				int blue = 256*((int)(255*Math.min(screenRGB[x][y][1], 1)));
				int green = (int)(255*Math.min(screenRGB[x][y][2], 1));
				pixels[x + (y*width)] = red + blue + green;
			}
		}
	}
}
