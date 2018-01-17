package net.imglib2.algorithm.ransac.RansacModels;

import java.util.ArrayList;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import ij.gui.EllipseRoi;
import ij.gui.Line;
import net.imglib2.RealLocalizable;
import net.imglib2.util.Pair;

public class Tangent2D {

	public static double[] GetTangent(final Ellipsoid ellipse, final double[] sourcepoint) {

		double[] coefficients = ellipse.getCoefficients();

		final double a = coefficients[0];
		final double b = coefficients[1];
		final double d = coefficients[2];
		final double g = coefficients[3];
		final double h = coefficients[4];

		final double x = sourcepoint[0];
		final double y = sourcepoint[1];

		double ratio = (a * x + d * y + g) / (b * y + d * x + h);
		double slope = -ratio;
		double intercept = y + x * ratio;

		double[] tangentline = new double[] { slope, intercept };

		return tangentline;
	}

	public static double GetAngle(final double[] lineparamA, final double[] lineparamB) {

		double slopeA = lineparamA[0];

		double slopeB = lineparamB[0];

		double numerator = slopeA - slopeB;
		double denominator = 1 + slopeA * slopeB;

		double angle = Math.atan((numerator / denominator));

		
		double degreeangle = Math.toDegrees(angle)%360;
		
		if (degreeangle >= 0)
			return degreeangle;
		else
			return 180 + degreeangle;
		

	}

	public static Angleobject GetTriAngle(final double[] lineparamA, final double[] lineparamB, final double[] sourcepoint,
			final Pair<Ellipsoid, Ellipsoid> ellipsepair) {

		Ellipsoid ellipseA = ellipsepair.getA();
		Ellipsoid ellipseB = ellipsepair.getB();

		double[] meanA = ellipseA.getCenter();
		double[][] covarianceA = ellipseA.getCovariance();

		EllipseRoi ellipseroiA = DisplayasROI.create2DEllipse(meanA,
				new double[] { covarianceA[0][0], covarianceA[0][1], covarianceA[1][1] });

		double[] meanB = ellipseB.getCenter();
		double[][] covarianceB = ellipseB.getCovariance();

		EllipseRoi ellipseroiB = DisplayasROI.create2DEllipse(meanB,
				new double[] { covarianceB[0][0], covarianceB[0][1], covarianceB[1][1] });
		
		double slopeA = lineparamA[0];
		
		double slopeB = lineparamB[0];

		double interceptA = lineparamA[1];

		double interceptB = lineparamB[1];

		final double midxA = sourcepoint[0];

		final double length = 20;
		double startxA = midxA - length / (Math.sqrt(1 + slopeA * slopeA));
		double endxA = midxA + length / (Math.sqrt(1 + slopeA * slopeA));

		double startyA = slopeA * startxA + interceptA;
		double endyA = slopeA * endxA + interceptA;
		
		
		double startxB = midxA - length / (Math.sqrt(1 + slopeB * slopeB));
		double endxB = midxA + length / (Math.sqrt(1 + slopeB * slopeB));

		double startyB = slopeB * startxB + interceptB;
		double endyB = slopeB * endxB + interceptB;
		
		final double[] candidatepointstartA = new double[] {startxA, startyA};
		final double[] candidatepointstartB = new double[] {startxB, startyB};
		
	
		
		final double[] candidatepointendA = new double[] {endxA, endyA};
		final double[] candidatepointendB = new double[] {endxB, endyB};
		
	
		
		
		final double[] realstartpoint = (ellipseroiA.contains((int)candidatepointstartA[0],(int) candidatepointstartA[1]) || ellipseroiB.contains((int)candidatepointstartA[0],(int) candidatepointstartA[1]))?
				candidatepointendA:candidatepointstartA; 
		final double[] realendpoint = (ellipseroiA.contains((int)candidatepointstartB[0],(int) candidatepointstartB[1]) || ellipseroiB.contains((int)candidatepointstartB[0],(int) candidatepointstartB[1]))?
				candidatepointendB:candidatepointstartB; 

		
		final double[] vA = new double[] { realstartpoint[0] - sourcepoint[0], realstartpoint[1] - sourcepoint[1]};
		final double[] vB = new double[] { realendpoint[0] - sourcepoint[0], realendpoint[1] - sourcepoint[1]};
		
		
		
		
		
		
		
		double argument = ( vA[0] * vB[0] +  vA[1] * vB[1] )  / Math.sqrt(( vA[0] * vA[0] +  vA[1] * vA[1])  * ( vB[0] * vB[0] +  vB[1] * vB[1] ) );
		
		double angle = Math.acos(argument);
		double angledeg = Math.toDegrees(angle)%360;
		System.out.println(angledeg);
		Line lineA = new Line(sourcepoint[0], sourcepoint[1], realendpoint[0], realendpoint[1]) ;
		Line lineB = new Line(sourcepoint[0], sourcepoint[1], realstartpoint[0], realstartpoint[1]) ;
		
		
	
		
		if (angledeg - 180 >=0 )
	       
	       angledeg =  360 - angledeg;
	   
		
		Angleobject angleandline = new Angleobject(lineA, lineB, angledeg);
		
		return angleandline;

	}
	
	public static double Distance(final double[] minCorner, final int[] maxCorner) {

		double distance = 0;

		for (int d = 0; d < minCorner.length; ++d) {

			distance += Math.pow((minCorner[d] - maxCorner[d]), 2);

		}
		return Math.sqrt(distance);
	}
	public static double Distance(final int[] minCorner, final int[] maxCorner) {

		double distance = 0;

		for (int d = 0; d < minCorner.length; ++d) {

			distance += Math.pow((minCorner[d] - maxCorner[d]), 2);

		}
		return Math.sqrt(distance);
	}
}
