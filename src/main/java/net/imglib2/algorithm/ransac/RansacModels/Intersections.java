package net.imglib2.algorithm.ransac.RansacModels;

import java.util.Vector;

import net.imglib2.RealLocalizable;

public class Intersections {

	/**
	 * Takes in the GeneralEllipsoid of the form
	 * a x^2 + b y^2 + c z^2 + 2 d xy  + 2 e xz + 2 f yz + 2 gx + 2 hy + 2 iz = 1 
	 * 
	 * In 2D
	 * a x^2 + b y^2  + 2 d xy  + 2 gx + 2 hy = 1 
	 * 
	 * For a chosen Z plane, determine the intersection to the ellipse to another ellipse
	 * by
	 * Dividing the form above with the coefficient of y^2 (b) to reduce the ellipse to the form
	 * 
	 *  A(x,y) = a0 + a1 x + a2 y + a3 x^2 + a4 xy + y^2
	 *  
	 *  
	 *  a0 = -1/b, a1 = 2 g / b,a2 = 2 h / b, a3 = a/b, a4 = 2 d / b 
	 *  
	 */
	
	
	public Vector<double[]> PointsofIntersection(GeneralEllipsoid EllipseA, GeneralEllipsoid EllipseB) {
		
	
		final double[] coefficients = EllipseA.Coefficients;
		
		final double a = coefficients[0];
		final double b = coefficients[1];
		final double d = coefficients[3];
		final double g = coefficients[6];
		final double h = coefficients[7];
		
		final double a0 = -1.0 / b;
		final double a1 = 2.0 * g / b;
		final double a2 = 2.0 * h / b ;
		final double a3 = a / b;
		final double a4 = 2 * d / b;
		
      final double[] coefficientsSec = EllipseB.Coefficients;
		
		final double aSec = coefficientsSec[0];
		final double bSec = coefficientsSec[1];
		final double dSec = coefficientsSec[3];
		final double gSec = coefficientsSec[6];
		final double hSec = coefficientsSec[7];
		
		final double a0Sec = -1.0 / bSec;
		final double a1Sec = 2.0 * gSec / bSec;
		final double a2Sec = 2.0 * hSec / bSec ;
		final double a3Sec = aSec / bSec;
		final double a4Sec = 2 * dSec / bSec;
		
		
		final double d0 = a0 - a0Sec;
		final double d1 = a1 - a1Sec;
		final double d2 = a2 - a2Sec;
		final double d3 = a3 - a3Sec;
		final double d4 = a4 - a4Sec;
		final double[] dVector = {d0, d1, d2, d3, d4};
		
		
		final double e0 = d0 - a2 * d2 / 2;
		final double e1 = d1  - (a2 * d4 + a4 * d2)/2;
		final double e2 = d3 - a4 * d4 / 2;
		
		
		final double c0 = a0 - a2 * a2 / 4;
		final double c1 = a1 - a2 * a4 / 2;
		final double c2 = a3 - a4 * a4 / 2;
		
		for (int i = 0; i < dVector.length; ++i) {
			
			if (dVector[i] == 0) {
				
				System.out.println("Ellipses are identical");
//f				
				return null;
				
			}
			
		}
		
		// Finding points of intersection in different cases
		
		if (d4 == 0 && d2 ==0 && e2 == 0) {
			
           // Listing 5 David Eberly, intersection of ellipses text		
			Vector<double[]> intersection = new Vector<>();
			double w, y;
			
			double xhat = -e0 / e1;
			double nchat = - (c0 + xhat * (c1 + xhat * c2));
			
			if (nchat > 0) {
				
				
				double translate = (a2 + xhat * a4) / 2;
				
				w = Math.sqrt(nchat);
				y = w - translate;
				intersection.add(new double[] {xhat, y});

				w = -w;
				y = w - translate;
				intersection.add(new double[] {xhat, y});

				
				
			}
			
			
			else if (nchat == 0) {
				
				y = - (a2 + xhat * a4) / 2;
				intersection.add(new double[] {xhat, y});

				
				
			}
			
			return intersection;
			
		}
		
		
		else
			
			return null;
		
		
		
	}
	
	
}
