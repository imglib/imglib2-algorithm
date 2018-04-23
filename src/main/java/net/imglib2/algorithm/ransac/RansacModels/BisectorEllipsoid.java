package net.imglib2.algorithm.ransac.RansacModels;

import net.imglib2.util.LinAlgHelpers;

public class BisectorEllipsoid implements NumericalSolvers {
	
	
	
	public double run(
			final int numComponents,
			final double[] e,
			final double[] y,
			final double[] x )
	{
		final int n = e.length;
		final double[] z = new double[ n ];
		double sumZSqr = 0;
		for ( int i = 0; i < numComponents; ++i )
		{
			z[ i ] = y[ i ] / e[ i ];
			sumZSqr += z[ i ] * z[ i ];
		}

		if ( sumZSqr == 1 )
		{
			// The point is on the hyperellipsoid.
			for ( int i = 0; i < numComponents; ++i )
			{
				x[ i ] = y[ i ];
			}
			return 0;
		}

		final double emin = e[ numComponents - 1 ];
		final double[] pSqr = new double[ n ];
		final double[] numerator = new double[ n ];
		for ( int i = 0; i < numComponents; ++i )
		{
			final double p = e[ i ] / emin;
			pSqr[ i ] = p * p;
			numerator[ i ] = pSqr[ i ] * z[ i ];
		}

		double s = 0, smin = z[ numComponents - 1 ] - 1, smax;
		if ( sumZSqr < 1 )
		{
			// The point is strictly inside the hyperellipsoid.
			smax = 0;
		}
		else
		{
			// The point is strictly outside the hyperellipsoid.
			smax = LengthRobust( numerator ) - 1;
		}

		final double jmax = GetMaxBisections();
		for ( int j = 0; j < jmax; ++j )
		{
			s = ( smin + smax ) * 0.5;
			if ( s == smin || s == smax )
			{
				break;
			}

			double g = -1;
			for ( int i = 0; i < numComponents; ++i )
			{
				final double ratio = numerator[ i ] / ( s + pSqr[ i ] );
				g += ratio * ratio;
			}

			if ( g > 0 )
			{
				smin = s;
			}
			else if ( g < 0 )
			{
				smax = s;
			}
			else
			{
				break;
			}
		}
//		if(s<Long.MAX_VALUE)
	//	System.out.println(s);
		double sqrDistance = 0;
		for ( int i = 0; i < numComponents; ++i )
		{
			x[ i ] = pSqr[ i ] * y[ i ] / ( s + pSqr[ i ] );
			final double diff = x[ i ] - y[ i ];
			sqrDistance += diff * diff;
		}
		return sqrDistance;
	}

	private static int GetMaxBisections()
	{
		final int doubleDigits = 52;
		final int doubleMinExponent = -1021;
		return 3 + doubleDigits - doubleMinExponent;
	}

	private static double LengthRobust( final double[] v )
	{
		return LinAlgHelpers.length( v );
	}
	

}
