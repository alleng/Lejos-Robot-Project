import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTConnector;

/**
 * Communicator for Mission Control
 * @author Allen Gurdus, Victor Sacristan
 *
 */
public class MissionControlCommunicator {

	/**
	 * Constructor
	 * @param control
	 */
	public MissionControlCommunicator(MC control)
	{
		this.control = control;
		System.out.println("MCC built");
	}
				
	/**
	 * Establishes connection with robot
	 * @param name
	 */
	public void connect(String name)
	{
		try {connector.close();}
		catch (Exception e){System.out.println(e );}
		System.out.println("Connecting to " + name);
		if (connector.connectTo(name, "", NXTCommFactory.BLUETOOTH))
		{
			control.setMessage("Connected to " + name);
			System.out.println(" Connected! ");
			dataIn = new DataInputStream(connector.getInputStream());
			dataOut = new DataOutputStream(connector.getOutputStream());
			if (dataIn == null) System.out.println(" no data ");
			else if (!reader.isRunning) reader.start();
			else System.out.println(" no connection ");
		}
	}
	
	/**
	 * Send waypoint and heading
	 * @param x
	 * @param y
	 * @param heading
	 */
	public void send(int header, float[] data)
	{
		System.out.println("MS communicator");		
		try
		{
			dataOut.writeInt(header);
			for(int i = 0; i<data.length; i++)
			{
			    dataOut.writeFloat(data[i]);
			}
			dataOut.flush();
		} catch (IOException e)
		{
			System.out.println(e);
		}
	}
	
	/**
	 * Listens for new pose information
	 * Runs in own thread
	 *
	 */
	class Reader extends Thread
	{
		boolean isRunning = false;
		
		public void run()
		{
			System.out.println("reader started");
			isRunning = true;
			int header = 0;
			float[] data = new float[4];
			int count = 0;
			while (isRunning)
			{
				try
				{
					header = dataIn.readInt();
					for (int i = 0; i < data.length; i++)
					{
						data[i] = dataIn.readFloat();
					}
					System.out.println("message "+ header+ " "+ data[0]+ " "+ data[1]+ " "+ data[2]);
					control.execute(header, data);
				} catch (IOException e)
				{
					if(count == 0) System.out.println("Read Exception in Communicator");	
		               count++;
				}
				
			}
		}
	}
	
	   MC control;	
	   private NXTConnector connector = new NXTConnector();
	   /**
	    * used by reader
	    */
	   private DataInputStream dataIn;
	   /**
	    * used by send()
	    */
	   private DataOutputStream dataOut;
	   /**
	    * inner class extends Thread; listens  for incoming data from the NXT
	    */
	   private Reader reader = new Reader();	
}


