import java.awt.Polygon;

/**
 * Triangle class for representation position and heading of robot graphically
 * @author allen gurdus, victor sacristan
 *
 */
public class Triangle {
	/**
	 * Creates triangle using coordinates and heading
	 * @param x
	 * @param y
	 * @param heading
	 * @return
	 */
	public static Polygon triangle(float x, float y, float heading)
	{
		int n_points = 3;
		int[] x_points = new int[3];
		int[] y_points = new int[3];
		
		x_points[0] = (int) (x + 20*Math.cos((double) Math.PI*-heading/180));
		x_points[1] = (int) (x + 10*Math.cos((double) Math.PI*(-heading - 120)/180));
		x_points[2] = (int) (x + 10*Math.cos((double) Math.PI*(-heading - 240)/180));
		
		y_points[0] = (int) (y + 20*Math.sin((double) Math.PI*-heading/180));
		y_points[1] = (int) (y + 10*Math.sin((double) Math.PI*(-heading - 120)/180));
		y_points[2] = (int) (y + 10*Math.sin((double) Math.PI*(-heading - 240)/180));
		
		Polygon triangle = new Polygon(x_points, y_points, n_points);
		return triangle;
	}
}