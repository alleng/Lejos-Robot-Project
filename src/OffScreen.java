
import java.awt.*;
import javax.swing.JTextField;
import java.awt.event.*;

/**
 * OffScreen.java ; manages drawing of grid and robot path on an Image 
 * which is displayed when  repaint() is called. 
 * Mouse listener used    
 * updated 10/13   2011
 * @author  Roger Glassey, Allen Gurdus, Victor Sacristan
 */
public class OffScreen extends javax.swing.JPanel
{

	/** Creates new form OffScreen */
	public OffScreen()
	{
		initComponents();
		setBackground(Color.white);
		System.out.println(" OffScreen Drawing constructor ");
		addMouseListener(new java.awt.event.MouseAdapter() 
		{
	            public void mouseClicked(java.awt.event.MouseEvent evt) 
	            {
	                mouseClick(evt.getPoint());
	            }			
	        });
	}
	
	/**
	 * records location of mouse click
	 * @param p
	 */
	public void mouseClick(Point p)
	{
	    int x =(int) p.getX();
	    int y = (int) p.getY();	    
	    textX.setText("" + gridX(x));
	    textY.setText("" + gridY(y));
	}

	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		if (offScreenImage == null)
		{
			makeImage();
		}
		g.drawImage(offScreenImage, 0, 0, this);  //Writes the Image to the screen
	}

	/**
	 * Create the offScreenImage, 
	 */
	public void makeImage()
	{
		System.out.println("OffScreenGrid  makeImage() called");
		imageWidth = getSize().width;// size from the panel
		imageHeight = getSize().height;
		yOrigin = imageHeight - 15;
		offScreenImage = createImage(imageWidth, imageHeight);// the container can make an image
		try {Thread.sleep(500);}
		catch(Exception e){};
		System.out.print("Off Screen Grid  create image ----- " );
		System.out.println( offScreenImage == null);
		if(offScreenImage == null)
		{
			System.out.println("Null image" );
			offScreenImage =  createImage(imageWidth, imageHeight);
		}
		osGraphics = (Graphics2D) offScreenImage.getGraphics();
		osGraphics.setColor(getBackground());
		osGraphics.fillRect(0, 0, imageWidth, imageHeight);// erase everything
		drawGrid();
	}

	/**
	 *draws the grid with labels
	 */
	public void drawGrid()
	{
		if(offScreenImage == null)makeImage();
		int xmax = 240;
		int ymax = 240;
		osGraphics.setColor(Color.BLACK); // Set the line color
		osGraphics.drawLine(xpixel(-240), ypixel(0), xpixel(xmax), ypixel(0));//horizontal lines
		osGraphics.drawLine(xpixel(-240), ypixel(ymax), xpixel(xmax), ypixel(ymax));//horizontal lines
		osGraphics.drawLine(xpixel(-240), ypixel(0), xpixel(-240), ypixel(ymax)); // vertical lines
		osGraphics.drawLine(xpixel(xmax), ypixel(0), xpixel(xmax), ypixel(ymax));
		
		osGraphics.setColor(Color.GREEN); // Set the line color
		for (int x = -240; x <= xmax; x+= 60)
		{
			osGraphics.drawLine(xpixel(x), ypixel(0), xpixel(x), ypixel(ymax));// vertical lines
		}
		for (int y = 0; y <= ymax; y+= 60)
		{
			osGraphics.drawLine(xpixel(-240), ypixel(y), xpixel(xmax), ypixel(y));// horizontal lines
		}
		
		osGraphics.setColor(Color.black); //set number color 	
		for (int y = 0; y <= ymax; y+=60) // number the  y axis
		{
			osGraphics.drawString(y + "", xpixel(-251f), ypixel(y-1f));
		}
		for (int x = -240; x <= xmax; x+=60) // number the x axis
		{
			osGraphics.drawString(x + "", xpixel(x-5f), ypixel(-6f));
		}
	}

	/**
	 *clear the screen and draw a new grid
	 */
	public void clear()
	{
		System.out.println(" clear called ");
		osGraphics.setColor(getBackground());
		osGraphics.fillRect(0, 0, imageWidth, imageHeight);// clear the image
		drawGrid();
		repaint();
	}
	
	Polygon myTriangle;
	
	/**
	 * Represents robot on grid as a triangle pointing in direction of heading
	 * @param _x
	 * @param _y
	 * @param heading
	 */
	public void drawRobot(float x, float y, float heading)
	{
		if (myTriangle != null)
		{
			osGraphics.setColor(Color.WHITE);
			osGraphics.drawPolygon(myTriangle);
		}
		myTriangle = Triangle.triangle(xpixel(x), ypixel(y), heading);
		osGraphics.setColor(Color.RED);
		osGraphics.drawPolygon(myTriangle);
		repaint();
	}
	
	int oldWallX = 2000;
	int oldWallY = 2000;
	
	public void drawWall(float _x, float _y)
	{
		int x = xpixel(_x);
		int y = ypixel(_y);
		osGraphics.setColor(Color.MAGENTA);
		if (Math.abs(x - oldWallX) < 30 && Math.abs(y - oldWallY) < 30 )
		{
			osGraphics.drawLine(oldWallX, oldWallY, x, y);
		}
		repaint();
		oldWallX = x;
		oldWallY = y;
	}

	/**
	 *Obstacles shown as black dot
	 */
	public void drawObstacle(int x, int y)
	{
		x = xpixel(x); // coordinates of intersection
		y = ypixel(y);
		osGraphics.setColor(Color.BLACK);
		osGraphics.fillOval(x - 5, y - 5, 10, 10);
		repaint();
	}
	
	/**
	 *blue line connects current robot position to last position if adjacent to current position
	 */
	public void drawRobotPath(int xx, int yy)
	{
		int x = xpixel(xx); // coordinates of intersection
		int y = ypixel(yy);
		osGraphics.setColor(Color.BLUE);		
		osGraphics.drawLine(robotPrevX, robotPrevY, x, y);
		robotPrevX = x;
		robotPrevY = y;
		repaint();
	}
	/**
	 * set the prev coordinates of the robot
	 * call by setPose in mission control
	 */
	public void setPrevCoordinates(int x, int y)
	{
		robotPrevX = xpixel(x);
		robotPrevY = ypixel(y);
	}

	/**
	 *convert grid coordinates to pixels
	 */
	private int xpixel(float x)
	{
		return xOrigin + (int) (x * gridSpacing);
	}

	private int gridX(int xpix)
	{
		float x = (xpix - xOrigin)/(1.0f*gridSpacing);
		return Math.round(x);
	}
	private int ypixel(float y)
	{
		return yOrigin - (int) (y * gridSpacing);
	}
	private int gridY(int ypix)
	{
		float y = (yOrigin - ypix)/(1.0f*gridSpacing);
		return Math.round(y);
	}
	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {

		clearB = new javax.swing.JButton();

		addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				formMouseClicked(evt);
			}
		});

		clearB.setText("Clear Map");
		clearB.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				clearBActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addGap(10, 10, 10)
						.addComponent(clearB)
						.addContainerGap(169, Short.MAX_VALUE))
				);
		layout.setVerticalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addGap(10, 10, 10)
						.addComponent(clearB)
						.addContainerGap(412, Short.MAX_VALUE))
				);
		
	}// </editor-fold>//GEN-END:initComponents

	private void clearBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearBActionPerformed

		clear();
	}//GEN-LAST:event_clearBActionPerformed

	private void formMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_formMouseClicked
	{
		
	}//GEN-LAST:event_formMouseClicked

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton clearB;
	// End of variables declaration//GEN-END:variables
	/**
	 *The robot path is drawn and updated on this object. <br>
	 *created by makeImage which is called by paint(); guarantees image always exists before used; 
	 */
	Image offScreenImage;
	/**
	 *width of the drawing area;set by makeImage,used by clearImage
	 */
	int imageWidth;
	/**
	 *height of the drawing are; set by  makeImage,used by clearImage
	 */
	int imageHeight;
	/** 
	 *the graphics context of the image; set by makeImage, used by all methods that draw on the image
	 */
	private Graphics2D osGraphics;
	/**
	 * y origin in pixels
	 */
	public int yOrigin;
	/**
	 * line spacing in  pixels
	 */
	public final int gridSpacing = 2;
	/**
	 * origin in pixels from corner of drawing area
	 */
	public final int xOrigin = 505;	
	/**
	 * robot position; used by drawRobotPath
	 */
	private int robotPrevX = xpixel(0);
	private int robotPrevY = ypixel(0);

	public JTextField textX;
	public JTextField textY;
}
