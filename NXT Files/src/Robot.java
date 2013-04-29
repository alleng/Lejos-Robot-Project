import java.util.ArrayList;



import lejos.robotics.navigation.Navigator;
import lejos.robotics.navigation.Pose;
import lejos.util.Delay;

/**
 * Headers created for each command to code messages sent over communicators and to the robot
 */
enum Header{GO,STOP,FIX,ROTATE,TRAVEL,POSE,SET_POSE,MAP,PING,OBSTACLE,BOMB,CHECK_BOMB,PEAKS_FAILED,WALL_FAILED}

/**
 * Object that carries out primary tasks for robot including navigation
 * Calls on Locator, Communicator and Navigator to complete tasks
 * @author Allen Gurdus, Victor Sacristan
 *
 */
public class Robot
{	
	Navigator myNavigator;
	Locator myLocator;
	Communicator myCommunicator;
	Detector myDetector;
	Pose myPose = new Pose();
	ArrayList<Message> inBox = new ArrayList<Message>();
	float[] sendData = new float[4];
	int bombCount = 0; // times we try to pick the bomb
	 			
	/**
	 * Constructor
	 * @param thePilot
	 * @param theCommunicator
	 * @param theLocator
	 */
	public Robot(Navigator theNavigator, Communicator theCommunicator, Locator theLocator, Detector theDetector)
	{		
		myNavigator = theNavigator;
		myCommunicator = theCommunicator;
		myLocator = theLocator;
		myDetector = theDetector;
		myCommunicator.constructRobot(this);
		myDetector.constructRobot(this);
				
		myCommunicator.connect();
		myDetector.start();
	}

	
	/**
	 * Carries out commands in inbox
	 * checks constantly if the inbox is empty, if not executes commands
	 */
	public void go()
	{
		while(true)
		{
			if(!inBox.isEmpty()) // check if there is any commands in the list
			{
				execute(inBox.get(0)); // execute the command in first place
				if(!inBox.isEmpty()) // check again in case STOP command had cleared the list, in the middle of an execute()
				{
					inBox.remove(0); // delete the command from the list after is done
				}
				 
			}
		}
	}
	
	/**
	 * Method that convert data from the communicator into a message that can be read by the robot
	 * After that is added into the inBox
	 */	
	public  void setMessage(int header, float[] data)
	{		
		Message message = new Message();
		message.header = Header.values()[header];
		for(int i=0; i<data.length; i++)
		{
			message.data[i] = data[i];
		}
		addToInBox(message);
	}
	
	/**
	 * Adds message containing command that was sent by communicator
	 * @param message
	 */
	public void addToInBox(Message message)
	{
		if(message.header == Header.STOP) // if we need to stop, stop right away
		{
			inBox.clear();	// clear the list
			stop();			
			reportPose();
		}
		else //if is not stop command
		{
			inBox.add(message);  // add command to the list
		}
		
	}
	
	/**
	 * Executes commands held in messages by using enum to deCode header
	 * @param message
	 */
	public void execute(Message message)
	{
		
		switch(message.header)
		{
		case GO: float destination[] = {message.data[0], message.data[1]};				    
	             navigate(destination, false, 0);
	             reportPose();
	             break;
		case FIX: fixPosition();
		          reportPose();
		          break;
		case ROTATE: int angle = (int) message.data[0];
	                 rotateTo(angle);
	                 reportPose();
	                 break;
		case TRAVEL: int distance = (int)message.data[0];
	                 travel(distance);
	                 reportPose();
	                 break;
		case SET_POSE: setPose(message.data);
		               break;
		case MAP:  float _destination[] = {message.data[0], message.data[1]};	
		           map(message.data[2]); // move the scanner before star travelling, map the first point
                   navigate(_destination, true, message.data[2]);
                   reportPose();
                   break; 
		case PING: ping((int) message.data[0]);
		           break;
		case BOMB: bombCount = 1; // first try to pickBomb
		           pickBomb();
		           reportPose();
		           break;
		case CHECK_BOMB: checkBomb();
                         break; 		
		}
	}
	
	/**
	 * Carries out goTo command based on destination coordinates
	 * Use a boolean, true in case map is needed. 
	 * For mapping left mapDirection = 1, -1 for right
	 * @param destination, map, mapDirection
	 */
	public void navigate(float[] destination, boolean map, float mapDirection)
	{
		myNavigator.goTo(destination[0], destination[1]);
	    while (myNavigator.isMoving())
	    {    	
	    	Delay.msDelay(100);
	    	reportPose(); 
	    	if(map) map(mapDirection);
	    }						    					    		
	}
	/**
	 * MAP
	 * if obstacle is found calls reportObstacleDistance() to report to PC side
	 * calls on myLocator to get distance to obstacles
	 * @param maping direction
	 */
	public void map(float mapDirection)
	{
		float angle = 90*mapDirection;
		float distance = myLocator.distanceAt((int)angle);	
		if(distance > 220); // if the distance is to high don't map, we want reliability
		else reportObstacleDistance(distance, angle);
	}
	
	/**
	 * Sends current pose to communicator so it can be sent to Mission Control
	 */
	public void reportPose()
	{
	    myPose = myNavigator.getPoseProvider().getPose();
	    sendData[0] = myPose.getX();
	    sendData[1] = myPose.getY();
	    sendData[2] = myPose.getHeading();
	    myCommunicator.send(Header.POSE.ordinal(), sendData);		
	}	
	/**
	 * Reports distance of objects at certain angle to Mission Control to build map
	 * @param distance
	 * @param angle
	 */
	public void reportObstacleDistance(float distance, float angle)
	{
		sendData[0] = distance;
		sendData[1] = angle;
		myCommunicator.send(Header.MAP.ordinal(), sendData);
	}	
	/**	 
	 * Sets pose for robot
	 * @param pose
	 */
	public void setPose(float[] pose)
	{		
		 myPose.setLocation(pose[0], pose[1]);
         myPose.setHeading(pose[2]);	
         myNavigator.getPoseProvider().setPose(myPose);
	}	
	/**
	 * Utilizes Locator to fix robot position to proper pose
	 */
	public void fixPosition()
	{
		myLocator.updatePose(myNavigator.getPoseProvider().getPose()); // updated the pose in Locator with the real pose of Robot
		myPose = myLocator.fixPosition(); 
		if (myLocator.checkProblem()) myCommunicator.send(Header.WALL_FAILED.ordinal(), sendData);
		else if (myLocator.myScanner.checkProblem()) myCommunicator.send(Header.PEAKS_FAILED.ordinal(), sendData);
		else myNavigator.getPoseProvider().setPose(myPose); //updated the pose in robot with the one of fixPosition
	}	
	/**
	 * Stops robot ASAP
	 * Don't need pilot.stop cause pilot is always access from myNavigator.moveController and myNavigator.stop() stops his move controller
	 */
	public void stop()
	{
	    myNavigator.stop();
	    myNavigator.clearPath();
	}	
	/**
	 * Robot travels a given distance in direction of current heading
	 * @param distance
	 */
	public void travel(int distance)
	{
	    myNavigator.getMoveController().travel(distance);
	}	
	/**
	 * Robot rotates to a given heading
	 * @param angle
	 */
	public void rotateTo(int angle)
	{
	    myNavigator.rotateTo(angle);
	}
	/**
	 * get the distance of obstacle in the angle direction 
	 * if obstacle is found set data[0]=1, if not data[0]=0
	 * report this information to PC side 
	 * @param angle
	 */
	public void ping(int angle)
	{
		int trueAngle = (int) (angle - myNavigator.getPoseProvider().getPose().getHeading());
		float distance = myLocator.distanceAt(trueAngle);
		if(distance > 220) sendData[0] = 0; // no obstacle
		else 
		{
			sendData[0] = 1; // obstacle
			sendData[1] = distance;
			sendData[2] = trueAngle;
		}
		myCommunicator.send(Header.OBSTACLE.ordinal(), sendData);	
	}
	/**
	 * pick the bomb
	 * calls on myScanner.bomb() to find the bomb location
	 * calls on checkBomb(), to see if we have the bomb, if not restart everything
	 * Scans always forward to avoid the possibility of picking our own robot by scanning backwards 
	 */
	public void pickBomb()
	{
		int[] data = new int[2];
		int distance = 0; // distance to the bomb
		int angle =0; // angle to the bomb
		int gain = -7;
		
		for(int i=0; i<2; i++) // scan twice
		{
			data = myLocator.myScanner.bomb(-30,30);
			angle = (int) (myNavigator.getPoseProvider().getPose().getHeading() + data[1]); // true angle
			distance = data[0];
			if(i==0) // if first scanner, approach half the distance
			{
				rotateTo(angle);
			    travel(distance/2);
			}
		}
		rotateTo(180 + angle); // face the bomb backwards
		travel(-(distance+gain)); // adjust distance of scanner to real distances
		reportPose(); // report the pose to mission control

		if(!checkBomb() && bombCount == 0) // check if the bomb is there, if it is not there try only twice
		{
			myDetector.setDetection(false); // we are going to check if the following travel and rotation hits the bumpers to stop the pickBomb method, because they are potentially risky
			travel(distance); //get away of the bomb
			rotateTo((int) (myNavigator.getPoseProvider().getPose().getHeading()-180)); // face the bomb
			reportPose();
			bombCount++; // second try to pickBomb
			if(!myDetector.getDetection()) pickBomb(); // if myDetector don't detects we try to pick the bomb again. Do only two tries
		}			
	}
	/**
	 * check if we still have the Bomb
	 * if yes set data[0]=1, if no data[0]=0
	 * report this information to PC side 
	 * it compares two getDistances instead of comparing a getDistance with the distance from the scanner to the back of the robot just in case the bomb is there but is not stock in our robot
	 */
	public boolean checkBomb()
	{
		float firstDistance = myLocator.distanceAt(180); // first ping
		travel(10); //move
		float secondDistance = myLocator.distanceAt(180); // second ping
		if(Math.abs(firstDistance - secondDistance) < 5 && firstDistance < 30) // compare pings
		{
			sendData[0] = 1; // we have the bomb
			myCommunicator.send(Header.CHECK_BOMB.ordinal(), sendData);
			return true;
		}
		else
		{
			sendData[0] = 0; // we don't have the bomb
			myCommunicator.send(Header.CHECK_BOMB.ordinal(), sendData);
			return false;
		}
	}
	
	/**
	 * Detector calls on it when an obstacle is detected
	 * stop the robot asap and backup
	 * report to PC side, so obstacle can be drawn and position can be updated
	 */
	public void avoider()
	{
		inBox.clear();	// clear the list of commands
		stop();
		
		sendData[0] = 1; //obstacle
		sendData[1] = 5.4f; // distance from scanner to touchSensors
		sendData[2] = 0; // assume that the angle of obstacle is 0 (in front of us)
		myCommunicator.send(Header.OBSTACLE.ordinal(), sendData);

		travel(-15);
		reportPose();
	}
	
}
