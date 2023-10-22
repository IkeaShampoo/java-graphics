package objects.util.graphics;

import java.lang.Math;

import objects.util.graphics.geometry.*;

public class ColorTexture extends Texture {
	private final double[] COLOR;
	private final double GLOSS;
	private final int SPECULARITY;
	public ColorTexture(double[] rgb, double gloss, int specularity) {
		COLOR = rgb;
		SPECULARITY = specularity;
		GLOSS = gloss;
		ISLIGHTSOURCE = false;
	}
	
	
	public ColorTexture(double[] rgb, double gloss, int specularity, boolean makeLightSource) {
		COLOR = rgb;
		SPECULARITY = specularity;
		GLOSS = gloss;
		ISLIGHTSOURCE = makeLightSource;
	}
	
	public void write(Plane screenPlane, Plane projectedPlane, Plane worldPlane, Rotation camOrientation, Vector camDisplacement, int width, int height, double resolutionScalar, double[][] zBuffer, Shader shader) {
		//Extrema [min, max]
		int[] yExtrema = screenPlane.getExtrema(1, new int[] {0, height});
		int yMin = yExtrema[0];
		int yMax = yExtrema[1];
		
		
		//System.out.println(COLOR[0] + ", " + COLOR[1] + ", " + COLOR[2]);
		//System.out.println("    Y min, max: " + yMin + ", " + yMax);
		
		double reverseScalar = 1/resolutionScalar;
		int halfWidth = width/2;
		int halfHeight = height/2;
		Vector normal = worldPlane.getNormal();
		double[] lineData = screenPlane.getLineData();
		
		//System.out.println(lineData[0] + ", " + lineData[1] + ", " + lineData[2] + ", " + lineData[3]);
		
		for (int y = yMin; y < yMax; y++) {
			int[] xIntercepts = new int[] {(int)((lineData[0]*y) + lineData[1]), (int)((lineData[2]*y) + lineData[3])};
			int xMin = Math.min(Math.max(Math.min(xIntercepts[0], xIntercepts[1]), 0), width);
			int xMax = Math.max(Math.min(Math.max(xIntercepts[0], xIntercepts[1])+1, width), 0);
			
			//System.out.println("    Y = " + y + ": " + xMin + ", " + xMax);
			
			for (int x = xMin; x < xMax; x++) {
				double perspectiveX = (double)(x - halfWidth);
				double perspectiveY = (double)(y - halfHeight);
				double z = projectedPlane.getDepthOfFlatPoint(perspectiveX, perspectiveY);
				
				/*
				if (COLOR[2] == 1 && z < 0) {
					System.out.println(x + ", " + y + ", " + z);
				}*/
				
				if (z < zBuffer[x][y] && z > 0 && z == z) {
					zBuffer[x][y] = z;
					double pointScalar = z*reverseScalar;
					Vector projectedPoint = new Vector(new double[] {
						perspectiveX*pointScalar, 
						perspectiveY*-pointScalar, 
						z
					});
					Vector worldPoint = camOrientation.rotated(projectedPoint).add(camDisplacement);
					shader.draw(x, y, worldPoint, normal, COLOR.clone(), GLOSS, SPECULARITY);
				}
			}
		}
	}
}
