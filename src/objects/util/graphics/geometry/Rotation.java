package objects.util.graphics.geometry;

/**
 * <h1>Rotation Matrices</h1>
 * This class allows you to make simple, single-axis rotations, as well as complex rotations in three dimensions.
 * <ul>
 * 	<li>Constructors build with blank, vector array, single-axis data, complex axis data, and round-vector rotation data.</li>
 * 	<li>Standard operations work on vectors, planes, and other rotations.</li>
 * 	<li>Other operations include a blank reset, radian calculation, standard correction, oriented correction,
 *	normalization, inversion, rotation around a vector, cloning, and printing.</li>
 * </ul>
 * 
 * Dependencies: Vector 1.3.2
 * 
 * <h2>Version Details</h2>
 * The last minor update integrated rotation of a Plane.
 * The last patch removed Vector dimension specification.
 * 
 * @author 		Deegan Osmundson
 * @version		1.4.0
 * @since 		2020-12-22
 */
public class Rotation {
  private Vector[] bases;
  /**
   * This method constructs a Rotation that is identical to the default orientation, with ijk basis 
   * 	vectors being identical to the xyz basis vectors.
   */
  public Rotation() {
    reset();
  }
	
	/**
	 * This method constructs a new Rotation using pre-generated basis vectors.
	 * @param newBases This is the array that contains the new basis vectors for the Rotation.
	 */
  public Rotation(Vector[] newBases) {
    bases = newBases;
  }
  
  /**
 	 * This method creates a Rotation whose k basis is pointed in the direction of the Vector parameter.
 	 * The Rotation produced is far less of a rotation, and more of an orientation or direction.
 	 * By default, i is orthogonal to y and k, and j is orthogonal to k and i.
 	 * If k is identical to y, j is orthogonal to k and x, and i is orthogonal to j and k.
 	 * This constructor is not intended for typical use. Instead, it is meant to form a Rotation onto
 	 * 	which a Vector may project itself, so that after this Rotation has been rotated, the projection 
 	 * 	may be rotated by the new Rotation to produce a new Vector who has been rotated around the 
 	 * 	direction vector by the radians of whatever Rotation rotated this. In this way, all that 
 	 * 	matters about this specific Rotation is the direction (k), and that the i, j, and k bases are 
 	 * 	orthogonal to eachother (with relationships identical to that of x, y, and z).
 	 * 
 	 * @param direction This parameter defines the k basis and the axis of the Rotation.
   */
  public Rotation(Vector direction) {
  	bases = new Vector[3];
		bases[2] = direction;
		if (direction.get(1) != 1) {
			Vector axis = new Vector(1);
			
			bases[0] = axis.cross(direction);
			
			bases[1] = direction.cross(bases[0]);
		} else {
			Vector axis = new Vector(0);
			
			bases[1] = direction.cross(axis);
			
			bases[0] = bases[1].cross(direction);
		}
		normalize();
  }
	
	/**
	 * This method constructs a Rotation around an axis (or along the plane orthogonal to the axis).
	 * @param radian This is the radian measurement of the rotation to be generated.
	 * @param axisIndex This is the index of xyz for the axis on which to make the rotation.
	 */
  public Rotation(double radian, int axisIndex) {
    bases = new Vector[3];
    int[] indices = Vector.planeIndices[axisIndex];
    double cos = Math.cos(radian);
    double sin = Math.sin(radian);

    double[] rotationVector = new double[3];
    rotationVector[axisIndex] = 1;
    rotationVector[indices[0]] = 0;
    rotationVector[indices[1]] = 0;

    double[] adjacentVector = new double[3];
    adjacentVector[axisIndex] = 0;
    adjacentVector[indices[0]] = cos;
    adjacentVector[indices[1]] = sin;

    double[] oppositeVector = new double[3];
    oppositeVector[axisIndex] = 0;
    oppositeVector[indices[0]] = -sin;
    oppositeVector[indices[1]] = cos;

    bases[axisIndex] = new Vector(rotationVector);
    bases[indices[0]] = new Vector(adjacentVector);
    bases[indices[1]] = new Vector(oppositeVector);
  }
	
	/**
	 * This method constructs a Rotation along a direction vector pointed by two 2D rotations along two
	 * 	different planes. The general orientation of this Rotation is defined as follows:
	 * 	The plane formed by the direction (axisIndex basis of ijk) and constantAxisIndex basis of ijk 
	 * 	is orthogonal to the plane that is formed by the two xyz bases orthogonal to the 
	 * 	constantAxisIndex basis of xyz.
	 * 
	 * The direction is calculated by generating two rotations, using the two direction radians.
	 * The basis vectors of the direction's index are extracted from each rotation.
	 * Each one only has two non-zero components, and for both of them, one of those components is the
	 * 	component of the direction's index (axisIndex).
	 * What's important to keep the angle for each vector the same is the proportion between those
	 * 	non-zero components.
	 * To keep that proportion the same, one vector is scaled to the other to make the components of
	 * 	axisIndex equal. Now that component and the other non-zero component (different indices) of
	 * 	each vector are used to form the direction basis vector of ijk axisIndex.
	 *  
	 * @param radians This radian array has the main rotation radian and the direction radians. The 
	 * 	main rotation radian is the radian of index axisIndex.
	 * @param axisIndex This is the index of the radians array for the main rotation, and is 
	 * 	also the index of the ijk bases for the direction vector.
	 * @param constantAxisIndex This is the index of the xyz bases for the vector that is crossed with 
	 * 	the direction vector to generate one of the ijk basis vectors. The other ijk basis vector 
	 * 	forms a plane with the direction vector (or its identical ijk basis vector), on which the 
	 * 	constantAxisIndex basis of xyz lies.
	 */
  public Rotation(double[] radians, int axisIndex, int constantAxisIndex) {
    Vector[] directionBases = new Vector[3];
    int[] indices = Vector.planeIndices[axisIndex];
    
    Vector adjVector = (new Rotation(radians[indices[0]], indices[0])).get(axisIndex);
    /* adjVect is the vector of axisIndex index of the rotation along the adjacent 
    axis of the plane perpendicular to the axisIndex axis*/
    Vector oppVector = (new Rotation(radians[indices[1]], indices[1])).get(axisIndex);
    /* oppVect is the vector of axisIndex index of the rotation along the opposite 
    axis of the plane perpendicular to the axisIndex axis*/
    
    double scaler = adjVector.get(axisIndex) / oppVector.get(axisIndex);
    oppVector.selfMultiply(scaler);
    
		Vector direction = adjVector.clone();
		direction.set(oppVector.get(indices[0]), indices[0]);
		direction.normalize();
		directionBases[axisIndex] = direction;
		
		Vector axis = new Vector(constantAxisIndex);
		Vector orthogonalDirection = axis.cross(direction);
		orthogonalDirection.normalize();
		
		Vector constantDirection = direction.cross(orthogonalDirection);
		constantDirection.normalize();
		
		for (int i:indices) {
			if (i == constantAxisIndex) {
				directionBases[i] = constantDirection;
			} else {
				directionBases[i] = orthogonalDirection;
			}
		}
		
		Rotation directionRotation = new Rotation(directionBases);
		Rotation axisRotation = new Rotation(radians[axisIndex], axisIndex);
		bases = (directionRotation.rotated(axisRotation)).bases;
  }
	
	//MAINTENANCE METHODS
	
	/**
	 * This method sets the ijk basis vectors equal to the xyz basis vectors.
	 * Rotating something by these vectors will not make any changes.
	 */
  public void reset() {
    bases = new Vector[3];
    bases[0] = new Vector(new double[] {1, 0, 0});
    bases[1] = new Vector(new double[] {0, 1, 0});
    bases[2] = new Vector(new double[] {0, 0, 1});
  }
  
  /**
   * This method normalizes every basis vector of the Rotation [matrix].
   */
  public void normalize() {
  	for (Vector basis:bases) {
  		basis.normalize();
  	}
  }
	
	/**
	 * This method ensures that all ijk basis vectors are orthogonal to each other.
	 * The i basis is made orthogonal to j and k, and j is made orthogonal to k and i.
	 */
  public void correct() {
		bases[0] = bases[1].cross(bases[2]);
		bases[1] = bases[2].cross(bases[0]);
		normalize();
  }

	/**
	 * This method ensures that all ijk basis vectors are orthogonal to each other, and that
	 * 	the plane formed by the axisIndex and constantAxisIndex bases of ijk is orthogonal to
	 * 	the xyz plane perpendicular to the constantAxisIndex basis of xyz.
	 * 
	 * @param axisIndex This represents the index of the ijk basis vectors for the Rotation's
	 * 	primary axis and direction.
	 * @param constantAxisIndex This represents the index of an xyz basis vector and an ijk
	 * 	basis vector, both of which lie on the same plane that they form with the direction, 
	 * 	primary basis/axis, or axisIndex vector (those three mean the same thing).
	 */
  public void correct(int axisIndex, int constantAxisIndex) {
  	int[] indices = Vector.planeIndices[axisIndex];
  	
		Vector axis = new Vector(constantAxisIndex);
		Vector orthogonalDirection = axis.cross(bases[axisIndex]);
		
		Vector constantDirection = bases[axisIndex].cross(orthogonalDirection);
		
		for (int i:indices) {
			if (i == constantAxisIndex) {
				bases[i] = constantDirection;
			} else {
				bases[i] = orthogonalDirection;
			}
		}
		
		normalize();
  }
	
	//GET METHODS
  public Vector get(int index) {
    return bases[index];
  }
	
  public double[] getRadians(int axisIndex, int constantAxisIndex) {
    int[] indices = Vector.planeIndices[axisIndex];
    double[] radians = bases[axisIndex].getAxesRadians(axisIndex);
    Rotation corrected = clone();
    corrected.correct(axisIndex, constantAxisIndex);
   	
    double adjDot = bases[indices[0]].dot(corrected.bases[indices[0]]);
    double oppDot = bases[indices[0]].dot(corrected.bases[indices[1]]);
    radians[axisIndex] = Vector.getRadian(adjDot, oppDot);
    
    return radians;
  }

	//CLONE METHODS
  public Rotation clone() {
    return new Rotation(cloneBases());
  }

  public Vector[] cloneBases() {
    Vector[] newBases = new Vector[3];
    for (int i = 0; i < 3; i++) {
      newBases[i] = bases[i].clone();
    }
    return newBases;
  }
  
  //PRINT METHODS
  public void print(int indentations) {
  	String indentation = new String();
  	for (int i = 0; i < indentations; i++) {
  		indentation += "   ";
  	}
    System.out.println(indentation + "{");
    for (Vector basis:bases){
      basis.print(indentations + 1);
    }
    System.out.println(indentation + "}");
  }
  
  public void print() {
    System.out.println("{");
    for (Vector basis:bases){
      System.out.print("   ");
      basis.print();
    }
    System.out.println("}");
  }
  
  //DIRECT MODIFIER OPERATIONS
  private void scale(double multiplier) {
    for (Vector basis:bases) {
      basis.selfMultiply(multiplier);
    }
  }
  
  public void invert() {
  	for (int i = 0; i < 3; i++) {
  		int[] indices = Vector.planeIndices[i];
      for (int j:indices) {
        bases[i].selfMultiply(j, -1);
      }
  	}
  }
  
  public Rotation inverted() {
  	invert();
    return this;
  }
  
  public Rotation invertedClone() {
  	return (clone()).inverted();
  }

  public void rotate(Vector axis, double radian) {
    Rotation first = new Rotation(axis);
    Rotation second = first.rotated(new Rotation(radian, 2));

    for (int i = 0; i < 3; i++) {
      bases[i] = first.dot(bases[i]);
    }
    second.rotate(this);
  }

  public Rotation rotated(Vector axis, double radian) {
    rotate(axis, radian);
    return this;
  }

  public Rotation rotatedClone(Vector axis, double radian) {
    Rotation first = new Rotation(axis);
    Rotation second = first.rotated(new Rotation(radian, 2));
		
		Vector[] newBases = new Vector[3];
    for (int i = 0; i < 3; i++) {
      newBases[i] = first.dot(bases[i]);
    }
    return second.rotated(new Rotation(newBases));
  }

  //STANDARD VECTOR OPERATIONS
  public void rotate(Vector vector) {
    double[] components = vector.cloneComponents();
    vector.zero();
    for (int i = 0; i < 3; i++) {
      vector.selfAdd(bases[i].multiply(components[i]));
    }
  }

  public Vector rotated(Vector vector) {
    rotate(vector);
    return vector;
  }
  
  public Vector rotatedClone(Vector vector) {
    return rotated(vector.clone());
  }

  public Vector dot(Vector vector) {
    double[] dotProducts = new double[3];
    for (int i = 0; i < 3; i++) {
      dotProducts[i] = vector.dot(bases[i]);
    }
    return new Vector(dotProducts);
  }
  
  //STANDARD PLANE OPERATION
  public Plane rotate(Plane plane) {
  	Vector[] newPoints = new Vector[3];
  	for (int i = 0; i < 3; i++) {
  		newPoints[i] = rotatedClone(plane.points[i]);
  	}
  	return new Plane(newPoints);
  }
	
	//STANDARD ROTATION OPERATIONS
  public void rotate(Rotation rotation) {
    for (Vector basis:rotation.bases) {
      rotate(basis);
      basis.normalize();
    }
  }

  public Rotation rotated(Rotation rotation) {
    rotate(rotation);
    return rotation;
  }
  
  public Rotation rotatedClone(Rotation rotation) {
    return rotated(rotation.clone());
  }
}