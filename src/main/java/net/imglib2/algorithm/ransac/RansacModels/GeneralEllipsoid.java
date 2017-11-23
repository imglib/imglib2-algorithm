package net.imglib2.algorithm.ransac.RansacModels;

import org.apache.commons.math3.geometry.Vector;
import org.apache.commons.math3.linear.RealVector;

public class GeneralEllipsoid {

	
	/** In order to obtain the points of intersection of ellipses we need the equation of an ellipse in a 
	 *  general form 
	 *  <i>ax</i><sup>2</sup> + <i>by</i><sup>2</sup> + <i>cz</i><sup>2</sup> +
	 * 2<i>dxy</i> + 2<i>exz</i> + 2<i>fyz</i> + 2<i>gx</i> + 2<i>hy</i> +
	 * 2<i>iz</i> = 1 <br />
	 * This general ellipsoid stores these coefficients as a vector V.
	 */

	double[] Coefficients;
	
	public GeneralEllipsoid(final RealVector V) {
		
	   Coefficients = V.toArray();
		
		
	}
	
	
	
	
	
}
