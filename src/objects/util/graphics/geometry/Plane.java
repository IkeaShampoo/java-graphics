package objects.util.graphics.geometry;

import java.lang.Math;

/**
 * <h1>Triangular Planes for Rendering</h1>
 * This class exists to provide a tool that can calculate information about a plane in a 3D area from
 *     a 2D perspective.
 * 
 * Dependencies: Vector 1.3.3
 * 
 * <h2>Version Details</h2>
 * The last minor update corrected the depth finding of points, added plane clipping, and built safety mechanisms to help prevent NaN spreading.
 * 
 * @author         Deegan Osmundson
 * @version        1.5.0-alpha
 * @since         2021-02-19
 */
public class Plane {
	public Vector[] points;
	private double[] ab;
	private double[] ac;
	private double[] displacement;
	private byte weightMethod;
	private byte depthMethod;
	private double m;
	private double n;
	private double b;
	public Plane(double[][] newPoints) {
		// points[0-2] are a, b, c
		points = Vector.toVector(newPoints);
		build();
	}
	
	public Plane(Vector[] newPoints) {
		// Does a deep copy of newPoints
		points = new Vector[3];
		for (int i = 0; i < 3; i++) {
			points[i] = newPoints[i].clone();
		}
		build();
	}
	
	public void print() {
		System.out.println("{");
		for (Vector point:points){
			System.out.print("   ");
			point.print();
		}
		System.out.println("}");
	}
	
  public void print(int indentations) {
  	String indentation = new String();
  	for (int i = 0; i < indentations; i++) {
  		indentation += "   ";
  	}
		System.out.println(indentation + "{");
		for (Vector point:points){
			point.print(indentations + 1);
		}
		System.out.println(indentation + "}");
	}
	
	public Plane add(Vector vector) {
		Vector[] newPoints = new Vector[3];
		for (int i = 0; i < 3; i++) {
			newPoints[i] = points[i].add(vector);
		}
		return new Plane(newPoints);
	}
	
	public Plane subtract(Vector vector) {
		Vector[] newPoints = new Vector[3];
		for (int i = 0; i < 3; i++) {
			newPoints[i] = points[i].subtract(vector);
		}
		return new Plane(newPoints);
	}
	
	public Plane project(Rotation bases) {
		Vector[] newPoints = new Vector[3];
		for (int i = 0; i < 3; i++) {
				newPoints[i] = bases.dot(points[i]);
		}
		return new Plane(newPoints);
	}
	
	public Plane[] clip(double nearPlaneZ) {
		boolean[] inFrustrum = new boolean[3];
		int pointsInFrustrumCount = 0;
		for (int i = 0; i < 3; i++) {
			inFrustrum[i] = (points[i].get(2) - nearPlaneZ) > 0;
			if (inFrustrum[i]) {
				pointsInFrustrumCount++;
			}
		}
		int planesInFrustrumCount = pointsInFrustrumCount;
		if (pointsInFrustrumCount == 3) {planesInFrustrumCount = 1;}
		int[] pointsInFrustrum = new int[pointsInFrustrumCount];
		int[] pointsOutFrustrum = new int[3 - pointsInFrustrumCount];
		Plane[] clipPlanes = new Plane[planesInFrustrumCount];
		int j = 0;
		int k = 0;
		for (int i = 0; i < 3; i++) {
			if (inFrustrum[i]) {
				pointsInFrustrum[j] = i;
				j++;
			} else {
				pointsOutFrustrum[k] = i;
				k++;
			}
		}
		double pointInFrustrumZ;
		switch(pointsInFrustrumCount) {
			case 1:
				Vector[] newPoints = new Vector[3];
				newPoints[pointsInFrustrum[0]] = points[pointsInFrustrum[0]].clone();
				pointInFrustrumZ = points[pointsInFrustrum[0]].get(2);
				for (int pointIndex: pointsOutFrustrum) {
					Vector fromInFrustrum = points[pointIndex].subtract(points[pointsInFrustrum[0]]);
					newPoints[pointIndex] = (fromInFrustrum.multiply(Math.abs((pointInFrustrumZ - nearPlaneZ) / fromInFrustrum.get(2)))).add(points[pointsInFrustrum[0]]);
				}
				clipPlanes[0] = new Plane(newPoints);
				break;
			case 2: 
				Vector[] interpolatedPoints = new Vector[2];
				Vector pointOutFrustrum = points[pointsOutFrustrum[0]];
				for (int i = 0; i < 2; i++) {
					int pointIndex = pointsInFrustrum[i];
					pointInFrustrumZ = points[pointIndex].get(2);
					Vector fromInFrustrum = points[pointsOutFrustrum[0]].subtract(points[pointIndex]);
					interpolatedPoints[i] = (fromInFrustrum.multiply(Math.abs((pointInFrustrumZ - nearPlaneZ) / fromInFrustrum.get(2)))).add(points[pointIndex]);
				}
				clipPlanes[0] = new Plane(new Vector[] {interpolatedPoints[0], interpolatedPoints[1], points[pointsInFrustrum[0]]});
				clipPlanes[1] = new Plane(new Vector[] {points[pointsInFrustrum[0]], points[pointsInFrustrum[1]], interpolatedPoints[1]});
				break;
			case 3: 
				clipPlanes[0] = this;
				break;
		}
		return clipPlanes;
	}
	
	public Plane scale(double scalar) {
		Vector[] newPoints = new Vector[3];
		for (int i = 0; i < 3; i++) {
			double[] components = points[i].getComponents();
			newPoints[i] = new Vector(new double[] {
				components[0]*scalar, components[1]*-scalar, components[2]
			});
		}
		return new Plane(newPoints);
	}
	
	public Plane flatten() {
		Vector[] newPoints = new Vector[3];
		for (int i = 0; i < 3; i++) {
			double[] components = points[i].getComponents();
			double scalar = 1/(components[2]);
			newPoints[i] = new Vector(new double[] {
				components[0]*scalar, components[1]*scalar, 1
			});
		}
		return new Plane(newPoints);
	}
	
	public Plane[] cut() {
		if (ab[1] == ac[1]) {
			return new Plane[] {this};
		} else if (ab[1] == 0) {
			return new Plane[] {new Plane(new Vector[] {points[2], points[0], points[1]})};
		} else if (ac[1] == 0) {
			return new Plane[] {new Plane(new Vector[] {points[1], points[0], points[2]})};
		} else {
			double[] pA = points[0].getComponents();
			double[] pB = points[1].getComponents();
			double[] pC = points[2].getComponents();
			double[][] ordered = new double[][] {pA, pB, pC};
			if (ordered[0][1] > ordered[1][1]) {
				double[] b = ordered[1];
				ordered[1] = ordered[0];
				ordered[0] = b;
			}
			if (ordered[1][1] > ordered[2][1]) {
				double[] b = ordered[1];
				ordered[1] = ordered[2];
				ordered[2] = b;
			}
			if (ordered[0][1] > ordered[1][1]) {
				double[] b = ordered[1];
				ordered[1] = ordered[0];
				ordered[0] = b;
			}
			double midY = ordered[1][1];
			double slope = (ordered[2][0]-ordered[0][0])/(ordered[2][1]-ordered[0][1]);
			double bias = ordered[2][0]-(slope*ordered[2][1]);
			double[] intPt = new double[] {(midY*slope)+bias, midY, 1};
			double[][][] cutPlanes = new double[][][] {{ordered[2], ordered[1], intPt}, {ordered[0], ordered[1], intPt}};
			return new Plane[] {new Plane(cutPlanes[0]), new Plane(cutPlanes[1])};
		}
	}
	
	public int[] getExtrema(int index, int[] limits) {
		double min = limits[1];
		double max = limits[0];
		for (int i = 0; i < 3; i++) {
			double p = points[i].get(index);
			if (p < min) {
				min = p;
			} 
			if (p > max) {
				max = p;
			}
		}
		if (min < limits[0]) {min = limits[0];}
		if (max > limits[1]) {max = limits[1];}
		return new int[] {(int)min, (int)max};
	}
	
	public int[] getExtrema(int index) {
		double[] a = points[0].getComponents();
		double min = a[index];
		double max = a[index];
		for (int i = 1; i < 3; i++) {
			double p = points[i].get(index);
			if (p < min) {
				min = p;
			}
			if (p > max) {
				max = p;
			}
		}
		return new int[] {(int)min, (int)max};
	}
	
	public void build() {
		displacement = points[0].getComponents();
		ab = points[1].subtract(points[0]).getComponents();
		ac = points[2].subtract(points[0]).getComponents();
	}
	
	public Vector getPoint(double pAB, double pAC) {
		return new Vector(new double[] {
			pAB*ab[0] + pAC*ac[0] + displacement[0], 
			pAB*ab[1] + pAC*ac[1] + displacement[1], 
			pAB*ab[2] + pAC*ac[2] + displacement[2]
		});
	}
	
	public Vector getNormal() {
		Vector normal = (new Vector(ac)).cross(new Vector(ab));
		normal.normalize();
		return normal;
	}
	
	public double[] getPixelPosition(int x, int y) {
		double newX = (x*ab[0]) + (x*ac[0]) + displacement[0];
		double newY = (y*ab[1]) + (y*ac[1]) + displacement[1];
		return new double[] {newX, newY};
	}
	
	public double getDepth(double pAB, double pAC) {
		return (ab[2] * pAB) + (ac[2] * pAC) + displacement[2];
	}
	
	public boolean testForLocating() {
		boolean real = true;
		if ((ab[0] == 0 && ac[0] == 0) || (ab[1] == 0 && ac[1] == 0) || (ab[0] == 0 && ab[1] == 0) || 
			(ac[0] == 0 && ac[1] == 0) || (ab[1]/ab[0] == ac[1]/ac[0]) || (ab[0]/ab[1] == ac[0]/ac[1])) {
			real = false;
		}
		weightMethod = 0;
		if (ab[0] == ac[0]) {
			weightMethod = 1;
		}
		if (ab[1] == 0) {
			weightMethod = 2;
		}
		return real;
	}

	public boolean testForUnflattening() {
		boolean abX0 = ab[0] == 0;
		boolean abY0 = ab[1] == 0;
		boolean acX0 = ac[0] == 0;
		boolean acY0 = ac[1] == 0;
		boolean dX0 = displacement[0] == 0;
		boolean dY0 = displacement[1] == 0;
		if (abX0 && abY0 && acX0 && acY0) {
			return false;
		} else if (abX0 && acX0) {
			if (dX0) {
				return false;
			}
			depthMethod = 0;
			b = displacement[0];
		} else if (abY0 && acY0) {
			if (dY0) {
				return false;
			}
			depthMethod = 1;
			b = displacement[1];
		} else if (ab[1]/ab[0] == ac[1]/ac[0]) {
			if (dX0 && dY0) {
				return false;
			}
			depthMethod = 2;
			m = ab[1]/ab[0];
			b = displacement[1] - (m * displacement[0]);
		} else if (abX0 && acY0) {
			depthMethod = 3;
			m = ac[2]/ac[0];
			n = ab[2]/ab[1];
			b = displacement[2] - m*displacement[0] - n*displacement[1];
		} else if (abY0 && acX0) {
			depthMethod = 3;
			m = ab[2]/ab[0];
			n = ac[2]/ac[1];
			b = displacement[2] - m*displacement[0] - n*displacement[1];
		} else if (abX0 && abY0) {
			if (dX0 && dY0) {
				return false;
			}
			depthMethod = 2;
			m = ac[1]/ac[0];
			b = displacement[1] - (m * displacement[2]);
		} else if (acX0 && acY0) {
			if (dX0 && dY0) {
				return false;
			}
			depthMethod = 2;
			m = ab[1]/ab[0];
			b = displacement[1] - (m * displacement[2]);
		} else if (abX0) {
			depthMethod = 3;
			n = ab[2]/ab[1];
			m = (ac[2] - (n*ac[1]))/ac[0];
			b = displacement[2] - m*displacement[0] - n*displacement[1];
		} else if (abY0) {
			depthMethod = 3;
			m = ab[2]/ab[0];
			n = (ac[2] - (m*ac[0]))/ac[1];
			b = displacement[2] - m*displacement[0] - n*displacement[1];
		} else if (acX0) {
			depthMethod = 3;
			n = ac[2]/ac[1];
			m = (ab[2] - (n*ab[1]))/ab[0];
			b = displacement[2] - m*displacement[0] - n*displacement[1];
		} else if (acY0) {
			depthMethod = 3;
			m = ac[2]/ac[0];
			n = (ab[2] - (m*ab[0]))/ab[1];
			b = displacement[2] - m*displacement[0] - n*displacement[1];
		} else {
			depthMethod = 3;
			double[] xAlign = new double[] {(ab[0]/ab[1])-(ac[0]/ac[1]), 0, (ab[2]/ab[1])-(ac[2]/ac[1])};
			m = xAlign[2]/xAlign[0];
			n = (ab[2] - (m*ab[0]))/ab[1];
			b = displacement[2] - m*displacement[0] - n*displacement[1];
		}
		double abm = ab[1]/ab[0];
		double acm = ac[1]/ac[0];
		double dm = displacement[1]/displacement[0];
		if (abm == dm && acm == dm) {
			return false;
		}
		
		boolean finalTest;
		finalTest = (b == b) && ((weightMethod > 1 && m == m) || !(weightMethod > 1)) && ((weightMethod > 2 && n == n) || !(weightMethod > 2));
		
		//System.out.println(finalTest + ", " + depthMethod + ", " + m + ", " + n + ", " + b);
		
		return finalTest;
	}
	
	public double[] getLineData() {
		// for a cut plane
		double slopeAB = ab[0]/ab[1];
		double slopeAC = ac[0]/ac[1];
		double biasAB = displacement[0] - (slopeAB*displacement[1]);
		double biasAC = displacement[0] - (slopeAC*displacement[1]);
		return new double[] {slopeAB, biasAB, slopeAC, biasAC};
	}
	
	public double getDepthOfFlatPoint(double pxp, double pyp) {
		double pz = 1;
		switch (depthMethod) {
			case 0:
				pz = b/pxp;
				break;
			case 1:
				pz = b/pyp;
				break;
			case 2:
				double pm = pyp/pxp;
				double px = b/(pm-m);
				pz = px/pxp;
				break;
			default:
				pz = b/(1-(m*pxp)-(n*pyp));
				break;
		}
		return pz;
	} // TEST FOR NaN ON OTHER END

	public double[] getWeights(int x, int y) {
		double pX = (double)x - displacement[0];
		double pY = (double)y - displacement[1];
		double pAB;
		double pAC;
		switch (weightMethod) {
			case 1:
				pAB = -(1+((pX-(ac[0]*(pY/-ab[1]))) / (-ab[0]-ac[0])));
				pAC = (pY/ab[1]) - pAB;
				break;
			case 2:
				pAC = pY/ac[1];
				pAB = (pX-(pAC*ac[0]))/ab[0];
				break;
			default:
				pAB = (pX-(ac[0]*(pY/ab[1]))) / (ab[0]-ac[0]);
				pAC = (pY/ab[1]) - pAB;
				break;
		}
		return new double[] {pAB, pAC};
	}
}
