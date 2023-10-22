package objects.util.graphics;

import java.lang.Math;

import objects.util.graphics.geometry.*;

public class DirectShader extends Shader {
	private Light[] lights;
 	private double ambience;
	private Vector camLocation;
	private double[][][] screenRGB;
	public DirectShader(double lightAmbience) {
		ambience = lightAmbience;
	}
	
	public void reshape(double[][][] newScreenRGB) {
		screenRGB = newScreenRGB;
	}

	public void preload(Plane[] newPlanes, Light[] newLights, Vector newCamLocation) {
		lights = newLights;
		for (Light light: lights) {
			light.cast(newPlanes);
		}
		camLocation = newCamLocation;
	}
	
	public void draw(int x, int y, Vector point, Vector normal, double[] rgb, double gloss, int specularPower) {
		double[] pixelRGB = new double[] {ambience, ambience, ambience};
		for (Light light:lights) {
			double attenuation = light.cast(point);
			if (attenuation > 0) {
				//System.out.println("hello");
				Vector ray = light.displacement.subtract(point);
				ray.normalize();
				double diffuse = Math.abs(normal.dot(ray));
				Vector cam = camLocation.subtract(point);
				cam.normalize();
				Vector halfVector = cam.add(ray);
				halfVector.normalize();
				double specularDot = Math.abs(normal.dot(halfVector));
				double specular = 1;
				for (int i = 0; i < specularPower; i++) {
					specular *= specularDot;
				}
				double intensity = attenuation*(diffuse + gloss*specular);
				pixelRGB[0] += intensity*light.rgb[0];
				pixelRGB[1] += intensity*light.rgb[1];
				pixelRGB[2] += intensity*light.rgb[2];

			}
		}
		pixelRGB[0] *= rgb[0];
		pixelRGB[1] *= rgb[1];
		pixelRGB[2] *= rgb[2];
		screenRGB[x][y] = pixelRGB;
	}
}