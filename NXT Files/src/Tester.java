import lejos.nxt.LightSensor;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.TouchSensor;
import lejos.nxt.UltrasonicSensor;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.navigation.Navigator;

/**
 * Test file for Bomb Locator
 * @author Allen Gurdus, Victor Sacristan
 *
 */
public class Tester {
	public static void main(String[] args)
	{		
		DifferentialPilot myPilot = new DifferentialPilot(5.459, 5.461, 13.51, Motor.A, Motor.C, false);
		myPilot.setTravelSpeed(10);
		myPilot.setAcceleration(50);
		
		LightSensor myLS = new LightSensor(SensorPort.S2);
		UltrasonicSensor myUS = new UltrasonicSensor(SensorPort.S1);
		TouchSensor myRTS = new TouchSensor(SensorPort.S3);
		TouchSensor myLTS = new TouchSensor(SensorPort.S4);
		Scanner myScanner = new Scanner(Motor.B, myLS, myUS);		
		Locator myLocator = new Locator(myScanner);	
		Navigator myNavigator = new Navigator(myPilot);
		Communicator myCommunicator = new Communicator();
		Detector myDetector = new Detector(myRTS, myLTS);
		Robot myRobot = new Robot(myNavigator, myCommunicator, myLocator, myDetector);
		
		
		myRobot.go();	
	}
}


