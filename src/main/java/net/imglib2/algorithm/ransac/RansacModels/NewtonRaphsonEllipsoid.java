package net.imglib2.algorithm.ransac.RansacModels;

import java.util.Random;

/**
 * Newton Raphson routine to get the shortest distance of a point from an
 * ellipsoid.
 * 
 * @author Varun Kapoor
 *
 */

public class NewtonRaphsonEllipsoid {

	public static int MAX_ITER = 1000000;
	public static double MIN_CHANGE = 1.0E-3;
	public double xc, xcNew, func = 0, funcdiff = 0, newratio, oldratio;
	double damp = 0.001;
	final Random rndx;

	public NewtonRaphsonEllipsoid(final Random rndx) {

		this.rndx = rndx;
		this.xc = rndx.nextFloat();
		this.xcNew = rndx.nextFloat() * rndx.nextFloat();
	}

	public double run(final int numComponents, final double[] ellipseCoeff, final double[] sourcePoint,
			final double[] targetPoint) {

		final int n = ellipseCoeff.length;
		final double[] z = new double[ n ];
		final double emin = ellipseCoeff[ numComponents - 1 ];
		final double[] pSqr = new double[ n ];
		final double[] numerator = new double[ n ];
		for ( int i = 0; i < numComponents; ++i )
		{
			z[ i ] = sourcePoint[ i ] / ellipseCoeff[ i ];

		
			final double p = ellipseCoeff[ i ] / emin;
			pSqr[ i ] = p * p;
			numerator[ i ] = pSqr[ i ] * z[ i ];
		}

		

		updateFunctions(xc, sourcePoint, ellipseCoeff, numComponents);

		int iteration = 0;

		do {

			

			xc = xcNew;
			updateFunctions(xc, sourcePoint, ellipseCoeff, numComponents);


			++iteration;

			iterate();

			// Compute the functions and the required derivates at the new point
			if (Double.isNaN(xcNew))
				xcNew = xc;


		
			if (iteration >= MAX_ITER)
				break;

		} while (Math.abs((xcNew - xc)) > MIN_CHANGE);

		for (int i = 0; i < numComponents; ++i) {
			final double p = ellipseCoeff[i] / emin;
			pSqr[i] = p * p;
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

		this.xcNew = iterate(xc, func, funcdiff);

	}

	public double iterate(final double oldpoint, final double function, final double functionderiv) {

		return oldpoint - (function / functionderiv);

	}

	protected void updateFunctions(final double xc, final double[] sourcePoint, final double[] ellipseCoeff,
			final int numComponents) {

		func = -1;
		funcdiff= 0;
		final int n = ellipseCoeff.length;
		final double[] z = new double[ n ];

		final double emin = ellipseCoeff[ numComponents - 1 ];
		final double[] pSqr = new double[ n ];
		final double[] numerator = new double[ n ];
		final double[] denominator = new double[ n ];
		for ( int i = 0; i < numComponents; ++i )
		{
			z[ i ] = sourcePoint[ i ] / ellipseCoeff[ i ];

		
			final double p = ellipseCoeff[ i ] / emin;
			pSqr[ i ] = p * p;
			numerator[ i ] = pSqr[ i ] * z[ i ];
		

			denominator[i] = xc + pSqr[i];

			func += (numerator[i] / denominator[i]) * (numerator[i] / denominator[i]);
			funcdiff += -2 * numerator[i] * numerator[i] / (denominator[i] * denominator[i] * denominator[i]   );

		}

	}

}
