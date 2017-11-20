package net.imglib2.algorithm.ransac.RansacModels;

import java.util.Random;

import net.imglib2.util.LinAlgHelpers;

/**
 * Newton Raphson routine to get the shortest distance of a point from an
 * ellipsoid.
 * 
 * @author Varun Kapoor
 *
 */

public class NewtonRaphsonEllipsoid implements NumericalSolvers {

	public static int MAX_ITER = 10000;
	public static double MIN_CHANGE = 1.0E-2;
	public double xc = 0, xcNew = 0, func = 0, funcdiff = 0, funcsecdiff = 0, newratio = 0, newsecratio = 0;
	double damp = 0.01;

	public NewtonRaphsonEllipsoid() {

		
	}

	public double run(final int numComponents, final double[] ellipseCoeff, final double[] sourcePoint,
			final double[] targetPoint) {

		final int n = ellipseCoeff.length;
		final double[] z = new double[n];
		final double emin = ellipseCoeff[numComponents - 1];
		final double[] pSqr = new double[n];
		final double[] numerator = new double[n];
		for (int i = 0; i < numComponents; ++i) {
			z[i] = sourcePoint[i] / ellipseCoeff[i];

			final double p = ellipseCoeff[i] / emin;
			pSqr[i] = p * p;
			numerator[i] = pSqr[i] * z[i];
		}
		
		int iteration = 0;
		int count = 0;
		double smin = z[ numComponents - 1 ] - 1;
		double smax = LengthRobust( numerator ) - 1;
		xcNew = 0.5 * (smin + smax);
		
		xc = xcNew;
		updateFunctions(xc, sourcePoint, ellipseCoeff, numComponents);
		iterate();
	//Iterate

		for (int iter = 0; iter< MAX_ITER; ++iter) {
			
			xc = xcNew;
			
			
			updateFunctions(xc, sourcePoint, ellipseCoeff, numComponents);
		
			// Compute the first iteration of the new point

			++iteration;

			if ( iteration%1000 == 0)
			{
			
				damp = new Random((int)Math.round(smax)).nextDouble();
				iterate();
				damp = 0.01;
			}
			else
			{
				iterate();
			}

			// Compute the functions and the required derivates at the new point
			if (Double.isNaN(xcNew) ) {
				xcNew = xc;
			    count++;	
			}
			else count = 0;
			
			
			


			
			
			if (Math.abs(xc -xcNew) > MIN_CHANGE && iteration > 10 || count > 10)
				break;
			
		} 
		
		
	
		double sqrdist = 0;
		for (int i = 0; i < numComponents; ++i) {

			targetPoint[i] = pSqr[i] * sourcePoint[i] / (xcNew + pSqr[i]);

			final double diff = targetPoint[i] - sourcePoint[i];

			sqrdist += diff * diff;

		}

		return sqrdist;
		
	}

	protected void iterate() {

		this.xcNew = iterate(xc, func, funcdiff, funcsecdiff);
	

	}

	public double iterate(final double oldpoint, final double function, final double functionderiv,
			final double functionsecderiv) {

		return oldpoint -  (function / functionderiv)* (1 + damp * 0.5 * function * functionsecderiv / (functionderiv * functionderiv) );

	}

	protected void updateFunctions(final double xc, final double[] sourcePoint, final double[] ellipseCoeff,
			final int numComponents) {

		func = -1;
		funcdiff = 0;
		funcsecdiff = 0;
		
		final int n = ellipseCoeff.length;
		final double[] z = new double[n];

		final double emin = ellipseCoeff[n - 1];
		final double[] pSqr = new double[n];
		final double[] numerator = new double[n];
		final double[] denominator = new double[n];

		for (int i = 0; i < numComponents; ++i) {
			z[i] = sourcePoint[i] / ellipseCoeff[i];
		
			final double p = ellipseCoeff[i] / emin;
			pSqr[i] = p * p;
			numerator[i] = pSqr[i] * z[i];

			denominator[i] = xc + pSqr[i];
			final double ratio = numerator[ i ] / denominator[i];
			final double ratiosq = - 2* ( numerator[ i ] *  numerator[ i ]  )/ (denominator[i] * denominator[i] * denominator[i]  );
			final double ratiocube = 6* ( numerator[ i ] *  numerator[ i ]  )/ (denominator[i] * denominator[i] * denominator[i] * denominator[i]  );
			func += ratio * ratio;
			funcdiff += ratiosq;
			funcsecdiff += ratiocube;
			
		}

		

	}
	private static double LengthRobust( final double[] v )
	{
		return LinAlgHelpers.length( v );
	}
	

}
