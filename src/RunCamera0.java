import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import edu.wpi.first.wpilibj.networktables.NetworkTable;

public class RunCamera0 extends Thread{
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	
	public static final double OFFSET_TO_FRONT = 0;
	public static final double CAMERA_WIDTH = 640;
	public static final double DISTANCE_CONSTANT= 5738;
	public static final double WIDTH_BETWEEN_TARGET = 8.5;
	
	static GripPipelineGear gear;
	static GripPipelineLift lift;

	public static VideoCapture cap;

	static Mat mat;

	static NetworkTable table;
	
	static double gearLengthBetweenContours;
	static double distanceFromGear;
	
	static double liftLengthBetweenContours;
	static double distanceFromLift;
	
	static double lengthError;
	static double[] gearCenter;
	static double[] liftCenter;
	
	public RunCamera0(String threadName){
		super(threadName);
	}

	public void run() {

		table = NetworkTable.getTable("camera0");

		while (true) {
			try {

				cap = new VideoCapture();

				gear = new GripPipelineGear();
				lift = new GripPipelineLift();

				cap.open(0);

				while (!cap.isOpened()) {
					System.out.println("Didn't open Camera 0, restart jar");
				}

				while (cap.isOpened()) {
					processCamera0();

				}
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}

		}

	}

	public static void processCamera0() {
		System.out.println("Processing Gear starting");
		System.out.flush();
		mat = new Mat();

		while (true) {
			cap.read(mat);
			gear.process(mat);
			System.out.println("Writing Gear Values");
			System.out.flush();
			table.putNumber("centerOfGear", centerOfGear());
			System.out.println("center of gear:" + centerOfGear());
			table.putNumber("distanceFromGear", distanceFromGear());
			System.out.println("distance from gear:" + distanceFromGear());
			table.putNumber("angelFromGear", angleFromGear());
			System.out.println("angle from gear:" + angleFromGear());
			

			lift.process(mat);
			System.out.println("Writing Lift Values");
			System.out.flush();
			table.putNumber("centerOfLift", centerOfLift());
			table.putNumber("distanceFromLift", distanceFromLift());
			table.putNumber("angleFromLift", angleFromLift());
			
		}
	}

	public static double distanceFromGear() {
		distanceFromGear = DISTANCE_CONSTANT / gearLengthBetweenContours;
		return distanceFromGear - OFFSET_TO_FRONT; 
	}

	public static double angleFromGear() {
		// 8.5in is for the distance from center to center from goal, then divide by lengthBetweenCenters in pixels to get proportion
		double constant = WIDTH_BETWEEN_TARGET / gearLengthBetweenContours;
		double angleToGoal = 0;
			//Looking for the 2 blocks to actually start trig
		if(!gear.filterContoursOutput().isEmpty() && gear.filterContoursOutput().size() >= 2){

			if(gearCenter.length == 2){
				// this calculates the distance from the center of goal to center of webcam 
				double distanceFromCenterPixels= ((gearCenter[0] + gearCenter[1]) / 2) - (CAMERA_WIDTH / 2);
				// Converts pixels to inches using the constant from above.
				double distanceFromCenterInch = distanceFromCenterPixels * constant;
				// math brought to you buy Chris and Jones
				angleToGoal = Math.atan(distanceFromCenterInch / distanceFromGear());
				angleToGoal = Math.toDegrees(angleToGoal);
				// prints angle
				//System.out.println("Angle: " + angleToGoal);
				}
			}
			return angleToGoal;
	}

	public static double centerOfGear() {
		
		// This is the center value returned by GRIP thank WPI
		if(!gear.filterContoursOutput().isEmpty() && gear.filterContoursOutput().size() >= 2){
			Rect r = Imgproc.boundingRect(gear.filterContoursOutput().get(1));
			Rect r1 = Imgproc.boundingRect(gear.filterContoursOutput().get(0)); 
			gearCenter = new double[]{r1.x + (r1.width / 2), r.x + (r.width / 2)};
			Imgcodecs.imwrite("outputng", mat);
			//System.out.println(centerX.length); //testing
			// this again checks for the 2 shapes on the target
			if(gearCenter.length == 2){
				// subtracts one another to get length in pixels
				gearLengthBetweenContours = Math.abs(gearCenter[0] - gearCenter[1]);
			}
		}
	return gearLengthBetweenContours;
	}

	public static double distanceFromLift() {
		return -1;
	}

	public static double angleFromLift() {
		return -1;
	}

	public static double centerOfLift() {

		// This is the center value returned by GRIP thank WPI
		if(!lift.filterContoursOutput().isEmpty() && lift.filterContoursOutput().size() >= 2){
			Rect r = Imgproc.boundingRect(lift.filterContoursOutput().get(1));
			Rect r1 = Imgproc.boundingRect(lift.filterContoursOutput().get(0)); 
			liftCenter = new double[]{r1.x + (r1.width / 2), r.x + (r.width / 2)};
			Imgcodecs.imwrite("output.png", mat);
			//System.out.println(centerX.length); //testing
			// this again checks for the 2 shapes on the target
			if(liftCenter.length == 2){
				// subtracts one another to get length in pixels
				liftLengthBetweenContours = Math.abs(liftCenter[0] - liftCenter[1]);
			}
		}
	return liftLengthBetweenContours;
	}
}