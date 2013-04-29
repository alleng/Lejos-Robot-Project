
import lejos.geom.Point;
import lejos.nxt.Sound;
import lejos.robotics.navigation.Pose;



/**
 * The Locator uses its primary method, fixPosition() to determine the robot's location
 * The Locator uses the ultrasonic sensor to determine wall distance and the light sensor to determine relative bearing to the light beacons
 * The Locator calls on the lightScan and distanceAt method in Scanners
 * @author Allen Gurdus, Victor Sacristan
 *
 */
class Locator
{ 
    Scanner myScanner;
    double beaconDifference = 0;
    boolean problem = false; // goes true if we try to fix pose in between two obstacles. Robot always check this when fixPose
    
    /**
     * Constructor
     * @param theScanner
     */
    public Locator(Scanner theScanner)
    {
	myScanner = theScanner;
    }
        	    
	/**
	 * obtain the bearing
	 *calls on checkWall to identify startAngle, and direction to scan
	 *use pose to identify direction to scan
	 * @return
	 */
     float[] getBearings()
    {
    	int startAngle = checkWall();       
        int direction = 1; 
        int x = (int) _pose.getX();
        int y = (int) _pose.getY();
        if((x>0 && y<120) || (x<0 && y>120))
        {
             direction = -1;     
        }
        float[] bearings = myScanner.lightScan(startAngle,startAngle + 240*direction); // identify angles of bearings
        if(y>120)
        {
            float change = bearings[0]; // reverse bearings
            bearings[0]=bearings[1];
            bearings[1]=change;
        }
           
       return bearings;
    }

	/**
	 * calculates position from beacon bearings and wall distance
	 * Calls getBerings to get relative bearings to beacons
	 * At the end corrects the fact that the scanner is not on the center of robot
	 * @return
	 */
	public Pose fixPosition()
	{   	   
	    float[] bearings = getBearings();
	    bearings[0] = (float) bearings[0];
	    bearings[1] = (float) bearings[1];
	    
	    double c = bearings[0]-bearings[1];
	    c = normalize( (float) c );
	    beaconDifference = c; // used for data logger to collect info
	    
	    double tanC = Math.tan(c*Math.PI/180);
	    
	    float y = _pose.getY();    
	    float x = (float)  ( 0.5*( (hallWidth/tanC) + Math.sqrt( Math.pow(hallWidth/tanC, 2) + 4*y*(hallWidth-y) ) ) );
	    if (c < 0)
	    {
	       x = (float) ( 0.5*( (hallWidth/tanC) - Math.sqrt( Math.pow(hallWidth/tanC, 2) + 4*y*(hallWidth-y) ) ) );
	    }
	    
	    _pose.setLocation(x - (float) (5.4*Math.cos(_pose.getHeading()*Math.PI/180)), y - (float) (5.4*Math.sin(_pose.getHeading()*Math.PI/180)));
	    _pose.setHeading(normalize(_pose.angleTo(beacon[0]) - bearings[0]));
	    return _pose;            
	}
	
	/**
	 * Rotates scanner towards nearest wall to measure distance
	 * In case and obstacle is found, scan opposite wall and correct, angle and direction used in getBearings() 
	 * @return angle
	 */
	public int checkWall()
	{
		problem = false;
	    int angle; 
	    float newY = 0;
	       	    
	    if(_pose.getY() > 122)
	    {
	    	angle = 90 - (int) _pose.getHeading();
			if (angle < -180) angle += 360;
			newY = 240 - (float) myScanner.distanceAt(angle);					
	    }
	    else
	    {
	    	angle = 270 - (int) _pose.getHeading();
	    	if (angle >= 180) angle -= 360;
		    newY = (float) myScanner.distanceAt(angle);			
	    }
	    
	    if(checkIfObstacle(newY)) // if it is an obstacle scan opposite wall
	    {
	    	if(_pose.getY() > 122) newY = (float) myScanner.distanceAt(-angle);
			else newY = 240 -(float) myScanner.distanceAt(-angle);	
	    	if(checkIfObstacle(newY)) problem = true; // if it is also an obstacle in the opposite wall
	    }
	    
	    _pose.setLocation(_pose.getX(), newY);
	    Sound.playTone(1200, 50);
	    	   	    	    
	    return angle;
	}
	/**
	 * Check if there is an obstacle between the robot and the wall scanned, return true in that case
	 * 
	 */
	public boolean checkIfObstacle(float newY)
	{
	    if(Math.abs(newY -_pose.getY()) > 20) return true;  // if the value get is very different for our actual pose, return true(obstacle)
	    else return false;
	}
	/**
	 * get the distance of the nearest object in the angle direction
	 */	
	public float distanceAt(int angle)
	{
		return (float) myScanner.distanceAt(angle);
	}
	
	/**
	 * set new x, y and heading for pose
	 * @param pose
	 */
	public void updatePose(Pose pose)
	{
		_pose.setLocation(pose.getX(), pose.getY());
		_pose.setHeading(pose.getHeading());
	}
	/**
	 * returns beaconDifference
	 */
	public double getBeaconDifference()
	{
		return beaconDifference;
	}
	/**
	 * returns problem
	 */
	public boolean checkProblem()
	{
		return problem;
	}

	/**
	 * returns normalized angle between -180 and 180 degrees
	 * @param angle
	 * @return
	 */
	private float normalize(float angle){
		while(angle<-180)angle+=360;
		while(angle>180)angle-=360;	
		return angle;
	}


//----------Fields-------------------------------------- ------------------------
//	Scanner scanner;


    float hallWidth = 240f; // cm   - check with scanner.
    float  beaconY = 240f;// hallWidth -10;  // verify
    
    /**
     * beacon coordinates as Point objects;
     */
    Point[] beacon = {new Point(0,0), new Point(0,beaconY)};
    /**
     * current position robot; set by fixPosition()
     */
     public Pose _pose = new Pose();
    /**
     * set by scanForBeacons used by fixPosition()
     */
	public float echoDistance;
     public float[] _beaconBearing = new float[2];

	
}
