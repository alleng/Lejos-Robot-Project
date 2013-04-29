
import lejos.nxt.TouchSensor;


/**
 * The Detector is responsible for identifying obstacles using the touch sensors
 * Detector runs in its own thread so it is constantly able to identify objects and notify Robot
 * @author Allen Gurdus, Victor Sacristan
 *
 */
public class Detector extends Thread{
	
	TouchSensor rightTouchSensor;
	TouchSensor leftTouchSensor;
	Robot myRobot;
	
	boolean detection = false;
	
	/**
	 * Constructor for detector
	 * @param _rightTouchSensor
	 * @param _leftTouchSensor
	 * @param _myRacer
	 */
	public Detector(TouchSensor _rightTouchSensor, TouchSensor _leftTouchSensor)
    {
		rightTouchSensor = _rightTouchSensor;
		leftTouchSensor = _leftTouchSensor;
    }
	public void constructRobot(Robot theRobot)
	{
		myRobot = theRobot;
	}
	
	/**
     * Runs the  thread
     * If an object is detected calls on avoider() in Robot to stop
     */
    public void run() 
    {		    
	    while(true)
	    {		       						
				if(rightTouchSensor.isPressed()|| leftTouchSensor.isPressed()) // if an object is detected
				{
					detection = true; // only for pickBomb purposes
				    myRobot.avoider();	// call racer that will call avoider			        
				}
	    }
    }
    /**
     * detection boolean getter.
     */
    public boolean getDetection()
    {
		return detection;
    }
    /**
     * detection boolean setter.
     */
    public void setDetection(boolean _detection)
    {
    	detection = _detection;
    }
    
 

}
