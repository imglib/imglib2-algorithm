package net.imglib2.algorithm.ransac.RansacModels;

import net.imglib2.RealLocalizable;

public class Tangent2D {

	
	
	public static double[] GetTangent(final Ellipsoid ellipse, final double[] sourcepoint) {
		
		
		
		double[] center = ellipse.getCenter();
		double[] radius = ellipse.getRadii();
		
		double ratio = (sourcepoint[0] - center[0] ) / (sourcepoint[1] - center[1]);
		double preratio =  radius[1] * radius[1] / (radius[0] * radius[0]);
		double slope = - preratio * ratio;
		double intercept = sourcepoint[1] + preratio * ratio * sourcepoint[0];
		
		double[] tangentline = new double[] {slope, intercept};
		
		return tangentline;
	}
	
	
	
	
	public static double GetAngle(final double[] lineparamA, final double[] lineparamB) {
		
		
		double slopeA = lineparamA[0];
		
		double slopeB = lineparamB[0];
		
		double numerator = slopeA - slopeB;
		double denominator = 1 + slopeA * slopeB;
		
		double angle = Math.abs(numerator / denominator);
		
		
		return Math.toDegrees(angle);
		
	}
	
}
