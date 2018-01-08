package net.imglib2.algorithm.ransac.RansacModels;

import net.imglib2.RealLocalizable;

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
		
		double[] tangentline = new double[] {slope, intercept};
		
		return tangentline;
	}
	
	
	
	
	public static double GetAngle(final double[] lineparamA, final double[] lineparamB) {
		
		
		double slopeA = lineparamA[0];
		
		double slopeB = lineparamB[0];
		
		double numerator = slopeA - slopeB;
		double denominator = 1 + slopeA * slopeB;
		
		double angle = Math.atan(Math.abs(numerator/ denominator));
		
		
		return Math.toDegrees(angle);
		
	}
	
}
