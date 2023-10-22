import objects.util.graphics.*;
import objects.util.graphics.geometry.*;

import java.util.ArrayList;
import java.util.Scanner;
import java.lang.Math;

public class Tests {
	public static void testConeLight() {
		DirectShader shader = new DirectShader(0.01);
		
		double distance = 10;
		Plane floor0 = new Plane(new double[][] {{-10, -2, -10}, {-10, -2, 10}, {10, -2, -10}});
		Plane floor1 = new Plane(new double[][] {{10, -2, 10}, {-10, -2, 10}, {10, -2, -10}});
		Plane triangle = new Plane(new double[][] {{-2, -1, 10}, {2, -1, 10}, {0, 1, 10}});
		Plane[] planes = new Plane[] {floor0, floor1, triangle};
		
		ColorTexture blue = new ColorTexture(new double[] {0, 0, 1}, 1, 1);
		Rotation lightOrientation = new Rotation(Math.toRadians(90), 0);
		Vector lightDisplacement = new Vector(new double[] {0, 10, 0});
		PyramidLight mainLight = new PyramidLight(200, 200, Math.toRadians(100), 0.1, 1000, new double[] {1, 1, 1}, 2500, lightOrientation, lightDisplacement);
		
		mainLight.cast(planes);
		Vector point = triangle.getPoint(0.5, 0.5);
		point.print();
		double attenuation = mainLight.cast(point);
		System.out.println(attenuation);
	}
	
	public static void testDisplay() {
		Camera cam = new Camera(800, 600, 1.57, 0.1, 1000, new DirectShader(0.15), new Rotation(Math.toRadians(15), 0), new Vector());
		cam.setDefaultColor(new double[] {0, 0, 0});
		UserInterface display = new UserInterface(800, 600);
		display.addImageSource("camera", cam);
		display.setFPS(4);
		display.start();
		
		double distance = 10;
		Plane floor0 = new Plane(new double[][] {{-10, -2, -10}, {-10, -2, 10}, {10, -2, -10}});
		Plane floor1 = new Plane(new double[][] {{10, -2, 10}, {-10, -2, 10}, {10, -2, -10}});
		Plane triangle = new Plane(new double[][] {{-2, -1, 10}, {2, -1, 10}, {0, 1, 10}});
		Plane wall = new Plane(new double[][] {{10, -2, 10}, {10, 20, 10}, {10, -2, -10}});
		Plane obstruction = new Plane(new double[][] {{0, 4, 2}, {-2, 5, -2}, {2, 3, -2}});
		
		Vector point0 = new Vector(new double[] {0, 9.6, 0});
		Vector point1 = new Vector(new double[] {-0.5, 10.3, 0.5});
		Vector point2 = new Vector(new double[] {0.5, 10.3, 0.5});
		Vector point3 = new Vector(new double[] {0.5, 10.3, -0.5});
		Vector point4 = new Vector(new double[] {-0.5, 10.3, -0.5});
		Plane side01 = new Plane(new Vector[] {point1, point2, point3});
		Plane side02 = new Plane(new Vector[] {point4, point2, point3});
		Plane side1 = new Plane(new Vector[] {point0, point1, point2});
		Plane side2 = new Plane(new Vector[] {point0, point2, point3});
		Plane side3 = new Plane(new Vector[] {point0, point3, point4});
		Plane side4 = new Plane(new Vector[] {point0, point4, point1});
		
		Plane[] planes = new Plane[] {floor0, floor1, triangle, wall, obstruction, side01, side02, side1, side2, side3, side4};
		ColorTexture black = new ColorTexture(new double[] {0, 0, 0}, 2, 3);
		ColorTexture red = new ColorTexture(new double[] {1, 0, 0}, 10, 50);
		ColorTexture orange = new ColorTexture(new double[] {1, 0.65, 0}, 2, 3);
		ColorTexture green = new ColorTexture(new double[] {0, 1, 0}, 2, 3);
		ColorTexture blue = new ColorTexture(new double[] {0, 0, 1}, 2, 3);
		ColorTexture white = new ColorTexture(new double[] {1, 1, 1}, 1, 1, true);
		Texture[] textures = new Texture[] {red, blue, green, orange, green, white, white, white, white, white, white};
		
		Vector lightDisplacement = new Vector(new double[] {0, 10, 0});
		/*
		PyramidLight frontLight = new PyramidLight(400, 400, Math.toRadians(90), 0.2, 1000, new double[] {1, 1, 1}, 15, new Rotation(), lightDisplacement);
		PyramidLight rightLight = frontLight.clone();
		rightLight.orientation = new Rotation(-Math.toRadians(90), 1);
		PyramidLight downLight = frontLight.clone();
		downLight.orientation = new Rotation(Math.toRadians(90), 0);*/
		PointLight centerLight = new PointLight(500, 0.75, 1000, new double[] {1, 1, 1}, 15, new Rotation(), lightDisplacement);
		Light[] lights = new Light[] {centerLight};
		
		cam.feed(planes, textures, lights);
		
		double fteenDeg = -Math.toRadians(15);
		Rotation rTurn = new Rotation(fteenDeg, 1);
		Rotation lTurn = new Rotation(-fteenDeg, 1);
		Rotation uTurn = new Rotation(fteenDeg, 0);
		Rotation dTurn = new Rotation(-fteenDeg, 0);
		Vector forth = new Vector(new double[] {0, 0, 1});
		Vector right = new Vector(new double[] {1, 0, 0});
		Vector up = new Vector(new double[] {0, 1, 0});
		
		Scanner keys = new Scanner(System.in);
		boolean running = true;
		while (running) {
			switch (keys.next().charAt(0)) {
				case 'w' -> cam.setPosition(cam.getPosition().add(cam.getOrientation().rotatedClone(forth)));
				case 's' -> cam.setPosition(cam.getPosition().subtract(cam.getOrientation().rotatedClone(forth)));
				case 'a' -> cam.setPosition(cam.getPosition().subtract(cam.getOrientation().rotatedClone(right)));
				case 'd' -> cam.setPosition(cam.getPosition().add(cam.getOrientation().rotatedClone(right)));
				case 'x' -> cam.setPosition(cam.getPosition().add(up));
				case 'c' -> cam.setPosition(cam.getPosition().subtract(up));
				case 'q' -> cam.setOrientation(lTurn.rotatedClone(cam.getOrientation()));
				case 'e' -> cam.setOrientation(rTurn.rotatedClone(cam.getOrientation()));
				case 'r' -> cam.setOrientation(cam.getOrientation().rotatedClone(uTurn));
				case 'f' -> cam.setOrientation(cam.getOrientation().rotatedClone(dTurn));
				case 'b' -> running = false;
			}
		}
		keys.close();
	}
	
	public static void testUnflattening() {
		Vector screenCenter = new Vector(new double[] {400, 300, 0});
		//Shader shader = new PlainShader();
		ColorTexture blue = new ColorTexture(new double[] {0, 0, 1}, 1, 1);
		Rotation orientation = new Rotation(Math.toRadians(15), 0);
		Vector displacement = new Vector();
		//double[][] zBuffer = new double[800][600];
		
		double[][] points = new double[][] {{10, -2, 10}, {-10, -2, 10}, {10, -2, -10}};
		Plane original = new Plane(points);
    Plane projected = original.subtract(displacement).project(orientation).scale(800);
    projected.print();
    System.out.println(projected.testForUnflattening());
    Plane[] clips = projected.clip(0.1);
    Plane issue = clips[1].flatten().add(screenCenter).cut()[0];
    issue.print();
    //blue.write(issue, projected, original, orientation, displacement, 800, 600, zBuffer, shader);
    /*
    for (int i = 0; i < clips.length; i++) {
    	System.out.println("Clip" + i);
			clips[i].print();
    	Plane screen = clips[i].flatten().add(screenCenter);
    	System.out.println("Screen");
    	screen.print();
			Plane[] cuts = screen.cut();
    	for (int j = 0; j < cuts.length; j++) {
    		System.out.println("    Cut" + j);
    		cuts[j].print(2);
    		blue.write(cuts[j], projected, original, orientation, displacement, 800, 600, zBuffer, shader);
    	}
    }*/
    /*
    Vector worldPointOG = original.getPoint(0.75, 0.75);
    worldPointOG.print();
    Vector perspectivePoint = worldPointOG.divide(worldPointOG.get(2));
    double x = perspectivePoint.get(0);
    double y = perspectivePoint.get(1);
    */
    
    double reverseScalar = 1/(double)800;
    double x = 400;
    double y = 500;
		double perspectiveX = (double)(x - 400);
		double perspectiveY = (double)(y - 300);
    double z = projected.getDepthOfFlatPoint(perspectiveX, perspectiveY);
		double pointScalar = z*reverseScalar;
		Vector projectedPoint = new Vector(new double[] {
			perspectiveX*pointScalar, 
			perspectiveY*-pointScalar, 
			z
		});
		Vector worldPoint = orientation.rotated(projectedPoint).add(displacement);
    worldPoint.print();
	}
	
	public static void testClip() {
		double[][] points = {{0, 3, -2}, {-2, 3, 2}, {2, 3, 2}};
    Plane original = new Plane(points);
    Plane[] clips = original.clip(1);
    for (Plane clip: clips) {
    	clip.print();
    }
	}
	
	public static void testCut() {
		Vector p1 = new Vector(new double[] {2, 2, 1});
		Vector p2 = new Vector(new double[] {1, 1, 1});
		Vector p3 = new Vector(new double[] {4, 0, 1});
		Plane plane1 = new Plane(new Vector[] {p1, p2, p3});
		Plane[] cuts1 = plane1.cut();
		for (Plane cut: cuts1) {
			cut.print();
		}
	}
	
	public static void testOnionHashMap() {
		/**HashMap<String, Object> data = new HashMap();
		data.put("integers", new int[] {1, 2, 3});
		data.put("doubles", new double[] {1.5, 2.5, 3.5});
		try {
			Onion file = new Onion("/home/pi/Documents/Java/data");
			file.create();
			file.save(data);
			HashMap<String, Object> recovered = (HashMap<String, Object>)(file.load()[0]);
  		System.out.println(((int[]) recovered.get("integers"))[2]);
  	} catch (IOException e) {e.printStackTrace();}**/
	}
	
	public static void testPlaneLineData() {
		double[][] points = {{0, 0, 0}, {0, 1, 0}, {1, 1, 0}};
		double[] lineData = (new Plane(points)).getLineData();
		System.out.println(lineData[0] + ", " + lineData[1] + ", " + lineData[2] + ", " + lineData[3]);
	}
}