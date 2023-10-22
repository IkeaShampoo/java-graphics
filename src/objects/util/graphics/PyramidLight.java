package objects.util.graphics;

import java.lang.Math;

import objects.util.graphics.geometry.*;

public class PyramidLight extends Light {
	private double halfAngle;
	private int resolutionWidth;
	private int resolutionHeight;
  private double nearPlane;
  private double farPlane;
	private double resolutionScalar;
	private Vector screenCenter;
  private double brightness;
	private double[][] rayDepths;
	private final double tolerance = 0.001;
	public PyramidLight(int width, int height, double angle, double minDistance, double maxDistance, double[] lightRGB, double intensity, Rotation originalOrientation, Vector originalDisplacement) {
    orientation = originalOrientation.clone();
    displacement = originalDisplacement.clone();
		resolutionWidth = width;
    resolutionHeight = height;
    halfAngle = angle/2;
    rgb = lightRGB.clone();
		rayDepths = new double[resolutionWidth][resolutionHeight];
		nearPlane = minDistance;
    farPlane = maxDistance;
    reset();
		screenCenter = new Vector(new double[] {resolutionWidth/2, resolutionHeight/2, 0});
		resolutionScalar = Math.max(resolutionWidth,resolutionHeight)/(2*Math.tan(halfAngle));
    brightness = intensity;
	}
	
	public void reset() {
		for (int i = 0; i < rayDepths.length; i++) {
			for (int j = 0; j < rayDepths[0].length; j++) {
				rayDepths[i][j] = farPlane;
			}
		}
	}
	
	public PyramidLight clone() {
		return new PyramidLight(resolutionWidth, resolutionHeight, halfAngle*2, nearPlane, farPlane, rgb.clone(), brightness, orientation.clone(), displacement.clone());
	}
	
	public void cast(Plane[] planes) {
		reset();
		Rotation momentOrientation = orientation.clone();
		Vector momentDisplacement = displacement.clone();
		
		for (int i = 0; i < planes.length; i++) {
			Plane projectedPlane = planes[i].subtract(momentDisplacement).project(momentOrientation).scale(resolutionScalar);
			if (projectedPlane.testForUnflattening()) {
				Plane[] clipPlanes = projectedPlane.clip(nearPlane);
				for (int j = 0; j < clipPlanes.length; j++) {
					Plane[] cutPlanes = clipPlanes[j].flatten().add(screenCenter).cut();
					for (int k = 0; k < cutPlanes.length; k++) {
						Plane screenPlane = cutPlanes[k];
						int[] yExtrema = screenPlane.getExtrema(1, new int[] {0, resolutionHeight});
						int yMin = yExtrema[0];
						int yMax = yExtrema[1];
						
						int halfWidth = resolutionWidth/2;
						int halfHeight = resolutionHeight/2;
						double[] lineData = screenPlane.getLineData();
						for (int y = yMin; y < yMax; y++) {
							int[] xIntercepts = new int[] {(int)((lineData[0]*y) + lineData[1]), (int)((lineData[2]*y) + lineData[3])};
							int xMin = Math.min(Math.max(Math.min(xIntercepts[0], xIntercepts[1]), 0), resolutionWidth);
							int xMax = Math.max(Math.min(Math.max(xIntercepts[0], xIntercepts[1])+1, resolutionWidth), 0);
							for (int x = xMin; x < xMax; x++) {
								double z = projectedPlane.getDepthOfFlatPoint((double)(x - halfWidth), (double)(y - halfHeight)) + tolerance;
								if (z < rayDepths[x][y] && z > 0 && z == z) {
									rayDepths[x][y] = z;
								}
							}
						}
					}
				}
			}
		}
	}
	
	public double cast(Vector point) {
		Vector orientedPoint = orientation.dot(point.subtract(displacement));
		if (orientedPoint.get(2) > nearPlane) {
			double radialDistance = orientedPoint.getMagnitude();
			double[] orientedComps = orientedPoint.getComponents();
			double pointScalar = resolutionScalar/orientedComps[2];
			double[] perspectiveComps = new double[] {
				orientedComps[0]*pointScalar, 
		    orientedComps[1]*-pointScalar, 
		    orientedComps[2]
			};
		  Vector perspectivePoint = new Vector(perspectiveComps);
		  //double radialDistance = (Math.sqrt((perspectiveComps[0]*perspectiveComps[0])+
		  //	(perspectiveComps[1]*perspectiveComps[1])))/resolutionScalar; RESERVED FOR SPOTLIGHT CLASS
			
			//perspectivePoint.print();
			
			perspectivePoint.selfAdd(screenCenter);
			int x = (int) perspectivePoint.get(0);
			int y = (int) perspectivePoint.get(1);
			double z = perspectivePoint.get(2);
			
			//perspectivePoint.print();
			
			if (x >= 0 && x <= resolutionWidth-1 && y >= 0 && y <= resolutionHeight-1) {
				if (rayDepths[x][y] >= z || 
						Math.max(
							Math.max(rayDepths[Math.max(x-1,0)][y],
								rayDepths[Math.min(x+1,resolutionWidth-1)][y]),
							Math.max(rayDepths[x][Math.max(y-1,0)],
								rayDepths[x][Math.min(y+1,resolutionHeight-1)])) >= z){
		      return brightness/(radialDistance*radialDistance);
				}
			}
		}
    return 0;
	}
}