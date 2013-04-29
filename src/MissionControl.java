import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import lejos.robotics.navigation.Pose;




/**
 * Creates GUI for user input and information
 * Calls on OffScreen to build grid
 * Calls on communicator to receive and send info
 * @author Allen Gurdus, Victor Sacristan
 *
 */
public class MissionControl extends JFrame implements MC
{
	static MissionControl frame;
	private JPanel contentPane;
	private JTextField nameField;
	private JTextField xField;
	private JTextField yField;
	private JTextField commandField;
	private JTextField headingField;
	private JLabel PoseX;
	private JLabel PoseY;
	private JLabel Heading;
	private JTextField statusField;
	/**
	 * provides communications services: sends and recieves NXT data
	 */
	private MissionControlCommunicator communicator = new MissionControlCommunicator(this);
	private OffScreen oSGrid = new OffScreen();
	
	float[] data = new float[4]; 

	

	/**
	 * Launch the application.
	 */
	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
				    frame = new MissionControl();
					frame.setVisible(true);
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MissionControl()
	{
		setTitle("Bomb Locator");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1050, 780);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JPanel topPanel = new JPanel();
		topPanel.setBounds(new Rectangle(0, 0, 200, 50));
		contentPane.add(topPanel, BorderLayout.NORTH);
		topPanel.setLayout(new GridLayout(5, 1, 0, 0));

		JPanel connectPanel = new JPanel();
		topPanel.add(connectPanel);

		JLabel lblName = new JLabel("Name");
		connectPanel.add(lblName);

		nameField = new JTextField();
		connectPanel.add(nameField);
		nameField.setColumns(10);
		nameField.setText("NXT3");

		JButton btnConnect = new JButton("Connect");
		btnConnect.addActionListener(new BtnConnectActionListener());
		connectPanel.add(btnConnect);
		
		JPanel posePanel = new JPanel();
		topPanel.add(posePanel);
		
		JLabel lblNewLabel_1 = new JLabel("Pose X:       \r\n");
		posePanel.add(lblNewLabel_1);
		
		PoseX = new JLabel("0\r\n");
		posePanel.add(PoseX);
		
		JLabel lblNewLabel_3 = new JLabel("     Pose Y:      \r\n");
		posePanel.add(lblNewLabel_3);
		
		PoseY = new JLabel("0\r\n");
		posePanel.add(PoseY);
		
		JLabel lblNewLabel_4 = new JLabel("     Heading:      \r\n");
		posePanel.add(lblNewLabel_4);
		
		Heading = new JLabel("0\r\n");
		posePanel.add(Heading);		

		JPanel goToPanel = new JPanel();
		topPanel.add(goToPanel);
		
		JButton btnMapL = new JButton("MapL");
		goToPanel.add(btnMapL);
		btnMapL.addActionListener(new btnMapLActionListener());
		
		JButton btnMapR = new JButton("MapR");
		goToPanel.add(btnMapR);
		btnMapR.addActionListener(new btnMapRActionListener());
		
		JButton btnGoTo = new JButton("goTo");
		btnGoTo.setForeground(Color.BLUE);
		goToPanel.add(btnGoTo);
		btnGoTo.addActionListener(new btnGoToActionListener());
		
		JLabel lblX = new JLabel(" X:");
		goToPanel.add(lblX);

		xField = new JTextField();
		goToPanel.add(xField);
		xField.setColumns(5);

		JLabel lblNewLabel = new JLabel("   Y:");
		goToPanel.add(lblNewLabel);

		yField = new JTextField();
		goToPanel.add(yField);
		yField.setColumns(5);
		
		JPanel rot_travPanel = new JPanel();
		topPanel.add(rot_travPanel);
		
		JLabel lblAngledistance = new JLabel("Angle / Distance");
		rot_travPanel.add(lblAngledistance);
		
		commandField = new JTextField();
		rot_travPanel.add(commandField);
		commandField.setColumns(5);
		
		JButton btnRotate = new JButton("RotateTo");
		rot_travPanel.add(btnRotate);
		btnRotate.addActionListener(new btnRotateActionListener());
		
		JButton btnTravel = new JButton("Travel");
		rot_travPanel.add(btnTravel);
		btnTravel.addActionListener(new btnTravelActionListener());
		
		JButton btnPing = new JButton("Ping");
		rot_travPanel.add(btnPing);
		btnPing.addActionListener(new btnPingActionListener());
				
		JPanel commandPanel = new JPanel();
		topPanel.add(commandPanel);
		
		JButton btnStop = new JButton("Stop");
		btnStop.setForeground(Color.RED);
		btnStop.addActionListener(new btnStopActionListener());
		commandPanel.add(btnStop);
		
		JButton btnFixPose = new JButton("FixPose");
		btnFixPose.addActionListener(new btnFixPoseActionListener());
		commandPanel.add(btnFixPose);
		
		JButton btnBomb = new JButton("BOMB");
		btnBomb.addActionListener(new btnBombActionListener());
		commandPanel.add(btnBomb);
		
		JButton btnCheckBomb = new JButton("checkBomb");
		btnCheckBomb.addActionListener(new btnCheckBombActionListener());
		commandPanel.add(btnCheckBomb);
		
		JLabel lblStatus = new JLabel("   Status:");
		commandPanel.add(lblStatus);
		
		statusField = new JTextField();
		commandPanel.add(statusField);
		statusField.setColumns(28);


		contentPane.add(oSGrid, BorderLayout.CENTER);

		oSGrid.textX = this.xField;
		oSGrid.textY = this.yField;
		
		JLabel lblHeading = new JLabel("  Heading:  ");
		goToPanel.add(lblHeading);
		
		headingField = new JTextField();
		goToPanel.add(headingField);
		headingField.setColumns(5);
		headingField.setText("0");
		
		JButton btnSetPose = new JButton("SetPose");
		goToPanel.add(btnSetPose);
		btnSetPose.addActionListener(new btnSetPoseActionListener());
	
	}
	private class BtnConnectActionListener implements ActionListener
	{
		public void actionPerformed(ActionEvent arg0)
		{
			String name = nameField.getText();
			communicator.connect(name);
			System.out.println("Connect to "+name);
		}
	}
	
	private class btnGoToActionListener implements ActionListener
	{
		public void actionPerformed(ActionEvent event) 
		{		
			try
			{
				data[0] = Integer.parseInt(xField.getText());
				data[1] = Integer.parseInt(yField.getText());
			} catch (Exception e)
			{
			    setMessage(e.toString());
			    return;
			}			
			communicator.send(Header.GO.ordinal(), data);
			setMessage("Going to: " + (int)data[0] + " " + (int)data[1]);
			repaint();
		}
	}		
	
	private class btnStopActionListener implements ActionListener
	{
		public void actionPerformed(ActionEvent arg0)
		{
			communicator.send(Header.STOP.ordinal(), data);
			setMessage("Robot STOP");
		}
	}
	private class btnSetPoseActionListener implements ActionListener
	{
		public void actionPerformed(ActionEvent arg0)
		{
			try
			{
				data[0] = Integer.parseInt(xField.getText());
				data[1] = Integer.parseInt(yField.getText());
				data[2] = Integer.parseInt(headingField.getText());
				
				setPose(data[0],data[1],data[2]);	// set Pose graphically	
				communicator.send(Header.SET_POSE.ordinal(), data); // send to Robot so robot can setPose	
				setMessage("Pose set");
			} catch (Exception e)
			{
				setMessage(e.toString());
			}			
			
		}
	}
	private class btnFixPoseActionListener implements ActionListener
	{
		public void actionPerformed(ActionEvent arg0)
		{
		    communicator.send(Header.FIX.ordinal(), data);
		    setMessage("Fixing Pose");
		}
	}
	private class btnRotateActionListener implements ActionListener
	{
		public void actionPerformed(ActionEvent arg0)
		{
		     try
		     {
		    	 data[0] = Integer.parseInt(commandField.getText());		    
				 communicator.send(Header.ROTATE.ordinal(), data);
				 setMessage("Rotating to: " + (int)data[0]);
		     }catch(Exception e){System.out.println(e);}
		     
		}
	}
	private class btnTravelActionListener implements ActionListener
	{
		public void actionPerformed(ActionEvent arg0)
		{
			 try
		     {
		    	 data[0] = Integer.parseInt(commandField.getText());		    
				 communicator.send(Header.TRAVEL.ordinal(), data);
				 setMessage("Travelling: " + (int)data[0]);
		     }catch(Exception e){setMessage(e.toString());}	
		}
	}
	private class btnMapLActionListener implements ActionListener
	{
		public void actionPerformed(ActionEvent arg0)
		{
			try
			{
				data[0] = Integer.parseInt(xField.getText());
				data[1] = Integer.parseInt(yField.getText());
				data[2] = 1; //direction to map
				communicator.send(Header.MAP.ordinal(), data);
				setMessage("Mapping Left to: " + (int)data[0] + " " + (int)data[1]);
			}catch(Exception e){setMessage(e.toString());}
		    
		    	
		}
	}
	private class btnMapRActionListener implements ActionListener
	{
		public void actionPerformed(ActionEvent arg0)
		{
			try
			{
				data[0] = Integer.parseInt(xField.getText());
				data[1] = Integer.parseInt(yField.getText());
				data[2] = -1; //direction to map
				communicator.send(Header.MAP.ordinal(), data);
				setMessage("Mapping Right to: " + (int)data[0] + " " + (int)data[1]);
			}catch(Exception e){setMessage(e.toString());}	
		}
	}
	private class btnPingActionListener implements ActionListener
	{
		public void actionPerformed(ActionEvent arg0)
		{
			try
			{
				data[0] = Integer.parseInt(commandField.getText());				
				communicator.send(Header.PING.ordinal(), data);
				setMessage("PING");
			}catch(Exception e){setMessage(e.toString());}	
		}
	}
	private class btnBombActionListener implements ActionListener
	{
		public void actionPerformed(ActionEvent arg0)
		{
			communicator.send(Header.BOMB.ordinal(), data);
			setMessage("Catching BOMB");
		}
	}
	private class btnCheckBombActionListener implements ActionListener
	{
		public void actionPerformed(ActionEvent arg0)
		{
			communicator.send(Header.CHECK_BOMB.ordinal(), data);			
		}
	}
	
	/**
	 * Carries out command using data sent from robot
	 * called by reader() in MissionControlCommunicator class
	 */
	public void execute(int _header, float[]data)
	{
		Header header = Header.values()[_header];
		switch(header)
		{
		case POSE: updatePose(data[0], data[1], data[2]);	               
	                   break;
		case MAP: displayWall(data[0], data[1]);		          
					   break;
		case OBSTACLE: if(data[0] == 0) statusField.setText("No obstacle found");
		               else
		               {		           	   
		            	   displayObstacle(data[1],data[2]);
		            	   float distance = data[1];
		            	   if(distance == 5.4f)
		            	   {
		            		   statusField.setText("You hit an obstacle stupid. BE CAREFUL!!!!");
		            		   JOptionPane.showMessageDialog(frame," You hit an obstacle stupid. BE CAREFUL!!!!");
		            	   }
		            	   else statusField.setText("Obstacle found at distance: " + distance);		            	   
		               } 
                       break;		
		case CHECK_BOMB: if(data[0] == 1) statusField.setText("We have the BOMB ;) ");
		                 else statusField.setText("We don't have the BOMB!!!! ");
		                 break;
		case PEAKS_FAILED:  statusField.setText("FixPose failed, PEAKS");
		                  JOptionPane.showMessageDialog(frame," FixPose failed! PEAKS"); 
		                  break;
		case WALL_FAILED:  statusField.setText("FixPose failed, WALL");
                           JOptionPane.showMessageDialog(frame," FixPose failed! WALL"); 
                           break;                  
		}
	}
	 /** Methods to update the GUI
	  * change labels and textFields
	  * draw robot, path and obstacles 
	  */		
	   public void setMessage(String s) 
	   {
		  statusField.setText(s);
	   }
	   /** 
	    * Set Pose labels
	    * Draw robot and robot path
	    */		
	   public void updatePose(float x, float y, float heading)
	   {		   	      		   
		   PoseX.setText(Integer.toString((int)x)); //set Pose labels
	       PoseY.setText(Integer.toString((int)y));
	       Heading.setText(Integer.toString((int)heading));
	       	     
	       oSGrid.drawRobot(x, y, heading);
	       oSGrid.drawRobotPath((int) x, (int) y); 
	       
	       pose.setLocation(x, y);  // used just for mapping
	       pose.setHeading(heading);
	   }
	   
	  Pose pose = new Pose();  // used for mapping
	  
	  /** 
	    *Draw walls and obstacles in maping
	    */
	   public void displayWall(float distance, float angle)
	   {
		   float x = pose.getX() + (float) (distance*Math.cos((pose.getHeading()+angle)*Math.PI/180));
		   float y = pose.getY() + (float) (distance*Math.sin((pose.getHeading()+angle)*Math.PI/180));
		   
		   oSGrid.drawWall(x, y);
	   }
	   /** 
	    * Draw single obstacles when ping or TouchSensor is activated
	    */
	   public void displayObstacle(float distance, float angle)
	   {
		   int x = (int) (pose.getX() +  (distance*Math.cos((pose.getHeading()+angle)*Math.PI/180)));
		   int y = (int) (pose.getY() +  (distance*Math.sin((pose.getHeading()+angle)*Math.PI/180)));
		   
		   oSGrid.drawObstacle(x, y);
	   }
	   /** 
	    * Set Pose 
	    * Draw robot 
	    */
	   public void setPose(float x, float y, float heading)
	   {
		   PoseX.setText(Integer.toString((int)x)); //set Pose labels
	       PoseY.setText(Integer.toString((int)y));
	       Heading.setText(Integer.toString((int)heading));
	       
	       oSGrid.setPrevCoordinates((int)x, (int)y); // set the prevCoordinates to the pose we are setting, because we are not changing our path
		   oSGrid.drawRobot(x, y, heading);	   
	   }	  	
}

enum Header{GO,STOP,FIX,ROTATE,TRAVEL,POSE,SET_POSE,MAP,PING,OBSTACLE,BOMB,CHECK_BOMB,PEAKS_FAILED,WALL_FAILED}




