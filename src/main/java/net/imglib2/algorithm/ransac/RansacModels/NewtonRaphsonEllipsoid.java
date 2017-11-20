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
	public static double MIN_CHANGE = 1.0E-1;
	public double xc = 0, xcNew = 0, func = 0, funcdiff = 0, funcsecdiff = 0, newratio = 0, newsecratio = 0;
	double damp = 0.01;

	public NewtonRaphsonEllipsoid() {

		
	}

	public double run(final int numComponents, final double[] ellipseCoeff, final double[] sourcePoint,
			final double[] targetPoint) {

		final int n = ellipseCoeff.length;
		final double[] z = new double[ n ];
		double sumZSqr = 0;
		for ( int i = 0; i < numComponents; ++i )
		{
			z[ i ] = sourcePoint[ i ] / ellipseCoeff[ i ];
			sumZSqr += z[ i ] * z[ i ];
		}

		if ( sumZSqr == 1 )
		{
			// The point is on the hyperellipsoid.
			for ( int i = 0; i < numComponents; ++i )
			{
				targetPoint[ i ] = sourcePoint[ i ];
			}
			return 0;
		}

		final double emin = ellipseCoeff[ numComponents - 1 ];
		final double[] pSqr = new double[ n ];
		final double[] numerator = new double[ n ];
		for ( int i = 0; i < numComponents; ++i )
		{
			final double p = ellipseCoeff[ i ] / emin;
			pSqr[ i ] = p * p;
			numerator[ i ] = pSqr[ i ] * z[ i ];
		}

		
		
		xc = -ellipseCoeff[1] * ellipseCoeff[1] + ellipseCoeff[1] * sourcePoint[1];
		xcNew = xc;
 // Main NR method
		updateFunctions(xc, sourcePoint, ellipseCoeff, numComponents);
		int count = 0;
		int iteration = 0;
		for (int iter = 0; iter < MAX_ITER; ++iter) {
			
			
			xc = xcNew;
			++iteration;
			iterate();
			
			if(Double.isNaN(xcNew) || xcNew > 1.0E10) {
				xcNew = xc;
			    count++;	
				
			}
			
			else count = 0;
			
			if (count > 1)
				break;
			
			if (iteration > MAX_ITER)
				break;
			
			updateFunctions(xcNew, sourcePoint, ellipseCoeff, numComponents);
			
			
		//	System.out.println((xc - xcNew) + " " + iteration);
		
			
			if (Math.abs(xc - xcNew) < MIN_CHANGE)
				break;
			
		}
		
		double sqrDistance = 0;
		for ( int i = 0; i < numComponents; ++i )
		{
			targetPoint[ i ] = pSqr[ i ] * sourcePoint[ i ] / ( xc + pSqr[ i ] );
			final double diff = targetPoint[ i ] - sourcePoint[ i ];
			sqrDistance += diff * diff;
		}
		return sqrDistance;
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
