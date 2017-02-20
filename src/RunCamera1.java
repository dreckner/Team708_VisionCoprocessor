import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import edu.wpi.first.wpilibj.networktables.NetworkTable;

public class RunCamera1 extends Thread {
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	public static final double OFFSET_TO_FRONT = 0;
	public static final double CAMERA_WIDTH = 640;
	public static final double DISTANCE_CONSTANT= 5738;
	public static final double WIDTH_BETWEEN_TARGET = 8.5;

	static GripPipelineBoiler boiler;
		
	public static VideoCapture cap;

	static Mat mat;

	static NetworkTable table;
	
	static double boilerLengthBetweenContours;
	static double distanceFromBoiler;
	static double angleToBoiler;
	
	static double lengthError;
	static double[] boilerCenter;
	
	public RunCamera1(String threadName){
		super(threadName);
	}
	
	public void run(){
		table = NetworkTable.getTable("camera1");

		while (true) {
			try {

				cap = new VideoCapture();

				boiler = new GripPipelineBoiler();
				

				cap.open(1);

				while (!cap.isOpened()) {
					System.out.println("Didn't open Camera 1, restart jar");
				}

				while (cap.isOpened()) {
					processCamera1();

				}
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}

		}
	}
	public static void processCamera1() {
		System.out.println("Processing Boiler starting");
		System.out.flush();
		mat = new Mat();

		while (true) {
			cap.read(mat);
			boiler.process(mat);
			System.out.println("Writing Boiler Values");
			System.out.flush();
			table.putNumber("centerOfBoiler", centerOfBoiler());
			table.putNumber("distanceFromBoiler", distanceFromBoiler());
			table.putNumber("angelFromBoiler", angleFromBoiler());
			

			
		}
	}

	public static double distanceFromBoiler() {
		distanceFromBoiler = DISTANCE_CONSTANT / boilerLengthBetweenContours;
		return distanceFromBoiler - OFFSET_TO_FRONT; 
	}

	public static double angleFromBoiler() {
		// 8.5in is for the distance from center to center from goal, then divide by lengthBetweenCenters in pixels to get proportion
				double constant = WIDTH_BETWEEN_TARGET / boilerLengthBetweenContours;
				double angleToGoal = 0;
					//Looking for the 2 blocks to actually start trig
				if(!boiler.filterContoursOutput().isEmpty() && boiler.filterContoursOutput().size() >= 2){

					if(boilerCenter.length == 2){
						// this calculates the distance from the center of goal to center of webcam 
						double distanceFromCenterPixels= ((boilerCenter[0] + boilerCenter[1]) / 2) - (CAMERA_WIDTH / 2);
						// Converts pixels to inches using the constant from above.
						double distanceFromCenterInch = distanceFromCenterPixels * constant;
						// math brought to you buy Chris and Jones
						angleToGoal = Math.atan(distanceFromCenterInch / distanceFromBoiler());
						angleToGoal = Math.toDegrees(angleToGoal);
						// prints angle
						//System.out.println("Angle: " + angleToGoal);
						}
					}
					return angleToGoal;
		
	}

	public static double centerOfBoiler() {
		// This is the center value returned by GRIP thank WPI
		if(!boiler.filterContoursOutput().isEmpty() && boiler.filterContoursOutput().size() >= 2){
			Rect r = Imgproc.boundingRect(boiler.filterContoursOutput().get(1));
			Rect r1 = Imgproc.boundingRect(boiler.filterContoursOutput().get(0)); 
			boilerCenter = new double[]{r1.x + (r1.width / 2), r.x + (r.width / 2)};
			Imgcodecs.imwrite("output.png", mat);
			//System.out.println(centerX.length); //testing
			// this again checks for the 2 shapes on the target
			if(boilerCenter.length == 2){
				// subtracts one another to get length in pixels
				boilerLengthBetweenContours = Math.abs(boilerCenter[0] - boilerCenter[1]);
			}
		}
	return boilerLengthBetweenContours;
	}
}
