import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lejos.nxt.LCD;
import lejos.nxt.Sound;
import lejos.nxt.comm.BTConnection;
import lejos.nxt.comm.Bluetooth;

/**
 * Robot's communicator for sending and receiving information with PC
 * Called on by the Robot
 * @author Allen Gurdus, Victor Sacristan
 *
 */
public class Communicator {
   	
	Robot myRobot;
	
	DataInputStream dataIn;
	DataOutputStream dataOut;
	
	
        private Reader reader = new Reader();
	
	float[] data = new float[4];	
	/**
	 * Construct robot 
	 */	
	public void constructRobot(Robot theRobot)
	{
	    myRobot = theRobot;
	}
		
	/**
	 * Establishes connection with PC
	 */
	public void connect()
	{
		LCD.drawString("connect waiting",0,0);
		BTConnection btc = Bluetooth.waitForConnection(); 
		LCD.clear();
		LCD.drawString("connected",0,0);
		try 
		{
			dataIn = btc.openDataInputStream();
			dataOut = btc.openDataOutputStream();
		} catch(Exception e) {};
		Sound.beepSequence();
		reader.start();
	}
	
	/**
	 * Sends information to the PC side. Header and data
	 */
	public void send(int header, float[] info)
	{
		try
		{
			dataOut.writeInt(header);
			for (int i = 0; i < info.length; i++)
			{
				dataOut.writeFloat(info[i]);
			}
			dataOut.flush();
		} catch (IOException e) {System.out.println(e);};
	}
		
	/**
	 * inner class extends Thread; listens  for incoming data from the NXT
	 */
	class Reader extends Thread
	{
		boolean isRunning = false;
		int count=0;
		
		public void run()
		{
			System.out.println("reader started");
			isRunning = true;
			for (int i = 0; i < data.length; i++)
			{
				data[i] = 0; 
			}
			while (isRunning)
			{
				try
				{
					int header = dataIn.readInt();
					for (int i = 0; i < data.length; i++)
					{
						data[i] = dataIn.readFloat();
					}
					myRobot.setMessage(header, data); // convert the data into a message that can be understand in the robot	
					count = 0;
				} catch (IOException e)
				{
					if(count == 0) System.out.println("Read Exception in Communicator");	
					               count++;
				}
				
			}
		}
	}
}


