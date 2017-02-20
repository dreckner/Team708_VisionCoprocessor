import org.opencv.core.Core;
import org.opencv.core.Mat;
//import org.opencv.core.Rect;
//import org.opencv.imgcodecs.Imgcodecs;
//import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import edu.wpi.first.wpilibj.networktables.NetworkTable;


public class VisionServer {
	
	static{
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	
	static GripPipelineBoiler boiler;
	static GripPipelineGear gear;
	static GripPipelineLift lift;
	
	public static VideoCapture camera0VideoCapture;
	public static VideoCapture camera1VideoCapture;
	
	static Mat gearMat;
	static Mat boilerMat;
	
	public static final double OFFSET_TO_FRONT = 0;
	public static final double CAMERA_WIDTH = 640;
	public static final double DISTANCE_CONSTANT= 5738;
	public static final double WIDTH_BETWEEN_TARGET = 8.5;
	public static boolean shouldRun = true;
	static NetworkTable table;
	
	static double lengthBetweenContours;
	
	static double distanceFromGear;
	static double distanceFromLift;
	static double distanceFromBoiler;
	
	static double lengthError;
	
	static double[] centerGear;
	static double[] centerLift;
	static double[] centerBoiler;
	
	public static void main(String[] args){
		NetworkTable.setClientMode();
		NetworkTable.setTeam(708);
		NetworkTable.setIPAddress("10.7.8.2");
		NetworkTable.initialize();
		
		Thread cam0Thread = new RunCamera0("Camera0Thread");
		cam0Thread.start();
		
		Thread cam1Thread = new RunCamera1("Camera1Thread");
		cam1Thread.start();
		
	}
	
	

}
