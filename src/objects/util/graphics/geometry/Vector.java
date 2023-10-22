package objects.util.graphics.geometry;

import java.lang.Math;

/**
 * <h1>Vector Mathematics</h1>
 * This class offers an an easy vector-oriented approach to math involving geometric algebra in three dimensions.
 * <ul>
 * 	<li>Vector-vector operations include addition, subtraction, multiplication, cross production, and dot production</li>
 * 	<li>Vector-scalar operations include multiplication and division.</li>
 * 	<li>Scalar component-scalar operations include multiplication.</li>
 * 	<li>Other operations include normalization, magnitude calculation, printing, cloning, and accessors.</li>
 * </ul>
 * 
 * This class has no dependencies.
 * 
 * <h2>Version Details</h2>
 * In the last minor update, unnecessary processes and loops were removed.
 * 
 * @author 		Deegan Osmundson
 * @version		1.5.0
 * @since 		2021-1-15
 */
public class Vector {
  private double[] components;
  public final static int[][] planeIndices = new int[][] {{1, 2}, {0, 2}, {0, 1}};
  public Vector() {
    components = new double[3];
    zero();
  }
  
  public Vector(double[] newComponents) {
    components = new double[3];
    set(newComponents);
  }
  
  public Vector(int directionIndex) {
    components = new double[3];
    zero();
    components[directionIndex] = 1;
  }
  
  public static Vector[] toVector(double[][] vectorComps) {
    Vector[] vectors = new Vector[vectorComps.length];
    for (int i = 0; i < vectorComps.length; i++) {
      vectors[i] = new Vector(vectorComps[i].clone());
    }
    return vectors;
  }
	
	//SET METHODS
  public void zero() {
    components[0] = 0;
    components[1] = 0;
    components[2] = 0;
  }

  public void set(double[] newComponents) {
    components = newComponents;
  }
  
  public void set(double newComponent, int index) {
    components[index] = newComponent;
  }
  
  public void setDirection(int directionIndex) {
    zero();
    components[directionIndex] = 1;
  }
	
	//GET METHODS
  public double get(int index) {
    return components[index];
  }
  
  public double[] getComponents() {
    return components;
  }
  
  public double getMagnitude() {
    return Math.sqrt(
      components[0]*components[0] + 
      components[1]*components[1] + 
      components[2]*components[2]
    );
  }
  
  //CLONE METHODS
  public Vector clone() {
    return new Vector(components.clone());
  }
  
  public double[] cloneComponents() {
    return components.clone();
  }
	
	//PRINT METHOD
  public void print(int indentations) {
  	String indentation = new String();
  	for (int i = 0; i < indentations; i++) {
  		indentation += "   ";
  	}
    String printed = indentation + "{";
    for (int i = 0; i < 2; i++) {
      printed += components[i] + ", ";
    }
    printed += components[2] + "} " + getMagnitude();
    System.out.println(printed);
  }
  public void print() {
    String printed = "{";
    for (int i = 0; i < 2; i++) {
      printed += components[i] + ", ";
    }
    printed += components[2] + "} " + getMagnitude();
    System.out.println(printed);
  }
  
  public void printRadians(int axisIndex) {
  	double[] radians = getAxesRadians(axisIndex);
  	System.out.println("(" + radians[0] + ", " + radians[1] + ", " + radians[2] + ")");
  }
  
	//MAINTENANCE METHODS
  public void normalize() {
    double magnitude = getMagnitude();
    components[0] /= magnitude;
    components[1] /= magnitude;
    components[2] /= magnitude;
  }
  
  //COMPONENT ARRAY OPERATIONS
  public double[] addComps(Vector vector) {
    return new double[] {
      components[0] + vector.components[0],
      components[1] + vector.components[1],
      components[2] + vector.components[2]
    };
  }
  
  public double[] subtractComps(Vector vector) {
    return new double[] {
      components[0] - vector.components[0],
      components[1] - vector.components[1],
      components[2] - vector.components[2]
    };
  }
  
  public double[] multiplyComps(Vector vector) {
    return new double[] {
      components[0] * vector.components[0], 
      components[1] * vector.components[1], 
      components[2] * vector.components[2]
    };
  }
  
  public double[] multiplyComps(double multiplier) {
    return new double[] {
      components[0] * multiplier, 
      components[1] * multiplier, 
      components[2] * multiplier
    };
  }
  
  public double[] multiplyComps(int index, double multiplier) {
    double[] newComponents = components.clone();
    newComponents[index] *= multiplier;
    return newComponents;
  }
  
  public double[] divideComps(double divider) {
    return new double[] {
      components[0] / divider,
      components[1] / divider,
      components[2] / divider
    };
  }
  
  public double[] crossComps(Vector vector) {
    double[] A = components;
    double[] B = vector.components;
    double[] C = new double[3];
    
    C[0] = (A[1] * B[2]) - (A[2] * B[1]);
    C[1] = (A[2] * B[0]) - (A[0] * B[2]);
    C[2] = (A[0] * B[1]) - (A[1] * B[0]);
    
    return C;
  }
  
  //DIRECT MODIFIER OPERATIONS
  public void selfAdd(Vector vector) {
    set(addComps(vector));
  }
  
  public void selfSubtract(Vector vector) {
  	set(subtractComps(vector));
  }
  
  public void selfMultiply(Vector vector) {
    set(multiplyComps(vector));
  }
  
  public void selfMultiply(double multiplier) {
    set(multiplyComps(multiplier));
  }
  
  public void selfMultiply(int index, double multiplier) {
    set((components[index] * multiplier), index);
  }
  
  public void selfDivide(double divider) {
    set(divideComps(divider));
  }

  //STANDARD RETURN OPERATIONS
  public Vector add(Vector vector) {
    return new Vector(addComps(vector));
  }
  
  public Vector subtract(Vector vector) {
  	return new Vector(subtractComps(vector));
  }
  
  public Vector multiply(Vector vector) {
    return new Vector(multiplyComps(vector));
  }
  
  public Vector multiply(double multiplier) {
    return new Vector(multiplyComps(multiplier));
  }

  public Vector multiply(int index, double multiplier) {
    return new Vector(multiplyComps(index, multiplier));
  }
  
  public Vector divide(double divider) {
    return new Vector(divideComps(divider));
  }
  
  public double dot(Vector vector) {
    return 
      components[0] * vector.components[0] +
      components[1] * vector.components[1] +
      components[2] * vector.components[2];
  }
  
  public Vector cross(Vector vector) {
    return new Vector(crossComps(vector));
  }

  //RADIAN ANGLE OPERATIONS
  public static double getRadian(double adj, double opp) {
    double radian =  Math.atan(opp/adj);
    if (adj < 0) {
      if (opp > 0) {
        radian += Math.PI;
      } else {
        radian -= Math.PI;
      }
    }
    return radian;
  }
  
  public double[] getAxesRadians(int axisIndex) {
  	double[] radians = new double[3];
  	int[] indices = planeIndices[axisIndex];
  	radians[indices[0]] = -getRadian(components[axisIndex], components[indices[1]]);
  	radians[indices[1]] = -getRadian(components[axisIndex], components[indices[0]]);
  	radians[axisIndex] = 0;
  	return radians;
  }
  
  //STATIC OPERATIONS
	public static Vector[] getPrismCorners(Vector[] directions) {
		Vector[] corners = new Vector[8];
		corners[0] = directions[0].add(directions[1]).add(directions[2]);
		corners[1] = directions[0].add(directions[1]).subtract(directions[2]);
		corners[2] = directions[0].subtract(directions[1]).add(directions[2]);
		corners[3] = directions[0].subtract(directions[1]).subtract(directions[2]);
		for (int i = 4; i < 8; i++) {
			corners[i] = corners[i-4].multiply(-1);
		}
		return corners;
	}
  
  public static double[] getExtrema(int index, Vector[] points) {
  	double[] a = points[0].components;
  	double min = a[index];
  	double max = a[index];
  	for (int i = 1; i < points.length; i++) {
  		double p = points[i].components[i];
  		if (p < min) {
  			min = p;
  		} else if (p > max) {
  			max = p;
  		}
  	}
  	return new double[] {min, max};
  }
	
	/* OBSOLETE OPERATIONS
  public static double[] getAxesRadians(double[] xyz, int axisIndex) { //OBSOLETE
    double[] xyzRadians = new double[3];
    int[] indices = getIndices(axisIndex);
    xyzRadians[indices[0]] = getRadian(xyz[axisIndex], xyz[indices[1]]);
    xyzRadians[indices[1]] = getRadian(xyz[axisIndex], xyz[indices[0]]);
    xyzRadians[axisIndex] = 0;
    
    //ALTERNATIVE TO SWITCH:
    //int indice = 1;
    //for (int i = axisIndex; i > 0; i--) {
    //  xyzRadians[indices[indice]] -= Math.PI/2;
    //  indice--;
    //}
    
    switch (axisIndex) {
      case 0:
        break;
      case 1:
        xyzRadians[indices[1]] -= Math.PI/2;
        break;
      default:
        xyzRadians[indices[0]] -= Math.PI/2;
        xyzRadians[indices[1]] -= Math.PI/2;
        break;
    }
    
    return xyzRadians;
  }
	
  public static double[] rotate(double adj, double opp, double radian) { //OBSOLETE
    double[] adjVector = {Math.cos(radian), Math.sin(radian)};
    double[] oppVector = {-Math.sin(radian), Math.cos(radian)};
    return NumJ.add(NumJ.multiply(adjVector, adj), NumJ.multiply(oppVector, opp));
  }

  public double getAxisRadian(int axisIndex) { //UNNECESSARY
  	int[] indices = getIndices(axisIndex);
    return getRadian(components[indices[0]], components[indices[1]]);
  }
  */
}