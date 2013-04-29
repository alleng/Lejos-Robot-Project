

import lejos.nxt.LightSensor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.Sound;
import lejos.nxt.UltrasonicSensor;

/**
 * Uses the light sensor to identify the angle of both light sources by identifying the peaks of light intensity when rotating in both directions
 * Uses the ultrasonic sensor to determine distance to nearest object at specific angle
 * Called on by Locator for fixing position
 * @author Allen Gurdus, Victor Sacristan
 *
 */
public class Scanner
{
	NXTRegulatedMotor myMotor;
	LightSensor myLS;
	UltrasonicSensor myUS;
	
	int highSpeed = 200;  // speed to set the scanner position
	int lowSpeed = 80;   // speed for scan
	int peakCount = 0;  // count to be sure that 4 beacons has been scanned
	boolean problem = false;  // indicate if a problem happened when recording peaks. Robot will check this value every time it fix pose
	
	/**
	 * Constructor for Scanner
	 * @param theMotor
	 * @param theEye
	 */
	public Scanner(NXTRegulatedMotor theMotor, LightSensor theLS, UltrasonicSensor theUS)
	{
		myUS = theUS;
		myLS = theLS;
		myMotor = theMotor;
		myMotor.setSpeed(150); 
		myLS.setFloodlight(false);
	}
	
	/**
	 * Identifies four angles of peak light intensity (two in each direction of scan)
	 * Angles are measured precisely by scanning in both direction and averaging the two results
	 * Determines peak by using threshold value to identify when one peak has passed and it can start scanning for another
	 * @param startAngle
	 * @param finishAngle
	 * @return angles
	 */
	public float[] lightScan(int startAngle, int finishAngle)
	{
		problem = false; // set problem boolean to false
	    myMotor.setSpeed(highSpeed);  // set high speed to get scanner in position
	    myMotor.rotateTo(startAngle);  // get scanner in position
	    myMotor.setSpeed(lowSpeed);  // set low speed to scan						
		int[] angles = new int[4];
		
		myMotor.rotateTo(finishAngle, true); // rotate to scan
		for (int i = 0; i < 4; i++) // for all 4 identified peaks
		{
			if (i == 2) // if ready to identify third peak
			{
				myMotor.stop();
				myMotor.rotateTo(startAngle, true); // reverse scan direction
			}			
			else if (i == 1 || i == 3) // if between two different peaks
			{
				int motorAngle = myMotor.getTachoCount();
				while ( Math.abs(myMotor.getTachoCount() - motorAngle) < 20 ) // wait for 20 degrees of rotation to avoid double counting peaks
				{
					if(!myMotor.isMoving()) // in case it get stock in here, break when motor stop adn set problem to true
					{
						problem = true;
						break;
					}
				}
			}
			
			angles[i] = identifyPeaks();
		}		
		myMotor.stop();
		
		float[] calculatedAngles = new float[2];
		calculatedAngles[0] = (angles[0] + angles[3])/2;
		calculatedAngles[1] = (angles[1] + angles[2])/2;		
		
		return calculatedAngles;
	}
	
	/**
	 * Identifies pea light intensity
	 * Uses threshold light intensity to know when to stop scanning
	 * Called on by lightScan method
	 * @return
	 */
	public int identifyPeaks()
	{
		int maxLight = 0; // highest value in current peak measurement
		int lightVal = 0;
		int lightAngle = 0;
		int maxLightAngle = 0; // angle of motor at highest light value in current peak measurement
		int threshold = 34;
		
		while (myLS.getLightValue() <= threshold) // no recording until over threshold value
		{
			if(!myMotor.isMoving()) // in case it get stock in here, break when motor stop adn set problem to true
			{
				problem = true;
				break;
			}
		}
		
		lightVal = myLS.getLightValue();
		lightAngle = myMotor.getTachoCount();
		while (lightVal >= threshold-3 && myMotor.isMoving()) // if motor is still moving and light values are still over threshold
		{
			if (lightVal > maxLight)
			{
				maxLight = lightVal;
				maxLightAngle = lightAngle;
			}
			
			lightVal = myLS.getLightValue();
			lightAngle = myMotor.getTachoCount();
		}		
		Sound.playTone(150, 100);
		return maxLightAngle;
	}
	/**
	 * obtain the distance and angle of the bomb for only one scan
	 */
	public int[] scanBomb(int startAngle, int finishAngle)
	{
		int minDistance = 99; // initialize minDistance to a high number
		int angle = 0;
		myMotor.setSpeed(highSpeed);  // set high speed to get scanner in position
		myMotor.rotateTo(startAngle);
		myMotor.setSpeed(lowSpeed);  // set low speed to scan	
		myMotor.rotateTo(finishAngle, true);
		while(myMotor.isMoving())
		{
			 int _angle = myMotor.getTachoCount();
			 myUS.ping();
			 int distance = myUS.getDistance();
			 if(distance < minDistance)
			 {
				 minDistance = distance;
				 angle = _angle;
			 }
		}
		System.out.println("individual  " + minDistance + " "+ angle);
		int[] data = new int[2];
		data[0] = minDistance;
		data[1] = angle;
		
		return data;				
	}
	/**
	 * obtain the distance and angle of the bomb
	 * calls on scanBomb for each scan
	 */
	public int[] bomb(int startAngle, int finishAngle )
	{
		int distance = 0;
		int angle = 0;
		for(int i=0; i<4; i++) //scan 4 times
		{
			int[] data = new int[2];
			data = scanBomb(startAngle, finishAngle);
			distance += data[0]; // add all distances of each scan to later obtain average
			angle += data[1]; // add all angles of each scan to later obtain average
			
			int _angle = startAngle; // change scan direction after each scan
			startAngle = finishAngle;
			finishAngle = _angle; 
		}
		int[] values = new int[2];
		values[0] = distance/4; // average
		values[1] = angle/4;
		
		return values;	
	}

	/**
	 * Measures distance to nearest object with ultrasonic sensor
	 * Called on by checkWall() method in Locator to check distance to nearest wall
	 */
	public int distanceAt(int angle)
	{
		myMotor.rotateTo(angle);
		myUS.ping();
		int distance = myUS.getDistance();
		return distance;
	}
	/**
	 * return the boolean problem. True if problem, false if everything is ok
	 */
	public boolean checkProblem()
	{
		return problem;
	}
	
}
