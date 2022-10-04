/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2022 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
 * John Bogovic, Albert Cardona, Barry DeZonia, Christian Dietz, Jan Funke,
 * Aivar Grislis, Jonathan Hale, Grant Harris, Stefan Helfrich, Mark Hiner,
 * Martin Horn, Steffen Jaensch, Lee Kamentsky, Larry Lindsey, Melissa Linkert,
 * Mark Longair, Brian Northan, Nick Perry, Curtis Rueden, Johannes Schindelin,
 * Jean-Yves Tinevez and Michael Zinsmaier.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package net.imglib2.algorithm.bspline;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;

import net.imglib2.algorithm.interpolation.randomaccess.BSplineInterpolator;

/**
 * Contains methods that pre-compute the coefficients of
 * the polynomials comprising the b-spline kernel functions.
 * Specifically {@link BsplineKernel4} and {@link BsplineKernel5}.
 * 
 * See:
 * Unser, Aldroubi, Eden  "Fast B-Spline Transform for Continuous Image Representation
 * and Interpolation" PAMI 1991
 *
 */
public class GenBsplineKernels
{
	public static void main(String[] args) throws IOException
	{
		System.out.println( "Uncentered:");
		writeUnCenteredSplinePieces();

		System.out.println( "\n\n");
		System.out.println( "Centered:");
		writeCenteredSplinePieces();
	}

	public static double[] valuesAtKnots( final ArrayList<double[]> polys )
	{
		int N = polys.size() + 1;
		double[] knotValues = new double[ N + 1 ];
		for( int i = 0; i <= N; i++ )
		{
			knotValues[ i ] = apply( polys, (double)i );
		}
		return knotValues;
	}
	
	public static double bsplineKernel( double x, int order )
	{
		return apply(
				splinePieces( order ),
				x + kernelCenter( order ));
	}

	public static void validateForCubicKernel()
	{
		double start = -3;
		double end = 3.0001;
		double step = 0.1;

		ArrayList<double[]> cubK = centeredSplinePieces( 3 );
		double y = BSplineInterpolator.evaluate3Normalized( 0 );

//		double z = apply( cubK, 2 );
		double z = bsplineKernel( 0, 3 );

		for( double x = start; x <= end; x += step)
		{
			y = BSplineInterpolator.evaluate3Normalized( x );
			z = bsplineKernel( x, 3 );
			System.out.println(  String.format("%f  :  %f  %f  #  %f ", x, y, z, (Math.abs(y-z)) ));
			System.out.println( "abs diff: " + (Math.abs(y-z)));
		}
	}

	public static ArrayList<double[]> centeredSplinePieces( int order )
	{
		double c = kernelCenter( order );

		ArrayList<double[]> ppolysUncentered = splinePieces( order );
		ArrayList<double[]> ppolys = new ArrayList<>();

		for( double[] p : ppolysUncentered )
			ppolys.add( shift( p, -c ));

		return ppolys;
	}

	/**
	 * Prints the polynomials comprising un-centered bspline kernels 
	 * for splines of order 0 through 5.
	 * 
	 * Un-centered means that the support of the kernel is strictly positive.
	 */
	public static void writeUnCenteredSplinePieces()
	{
		for( int i = 0; i <= 5; i++ )
		{
			ArrayList<double[]> ppolysUncentered = splinePieces( i );

			for( int j = 0; j < ppolysUncentered.size(); j++ )
				System.out.println( "p " + Arrays.toString( ppolysUncentered.get( j )) );

			System.out.println( " " );
		}
	}
	
	/**
	 * Prints the polynomials comprising centered bspline kernels 
	 * for splines of order 0 through 5.
	 * 
	 * centered means that the kernel is symmetric around zero.
	 */
	public static void writeCenteredSplinePieces()
	{
		for( int i = 0; i <= 5; i++ )
		{
			ArrayList<double[]> ppolys = centeredSplinePieces( i );

			for( int j = 0; j < ppolys.size(); j++ )
				System.out.println( "p " + Arrays.toString( ppolys.get( j )) );

			System.out.println( " " );
		}
	}

	public static void testShift()
	{
		double[] p = new double[]{ 0, 0, 1 };
		double[] q = shift( p , -2 );
		System.out.println( "q : " + Arrays.toString( q ));
	}
	
	/**
	 * spline pieces directly from the formula below, resulting in
	 * kernels not centered at the origin.
	 */
	public static void shiftedSplinePieces()
	{
		for( int i = 0; i <= 5; i++ )
		{
			ArrayList<double[]> ppolys = splinePieces( i );
			for( int j = 0; j < ppolys.size(); j++ )
				System.out.println( "p " + Arrays.toString( ppolys.get( j )) );

			System.out.println( " " );
		}
	}
	
	public static double kernelCenter( final int order )
	{
		return (double)( order + 1 ) / 2.0;
	}
	
	public static void centeredPolysToCsv() throws IOException
	{
		double start = -7.0;
		double end = 7.00001;

		toCsv( 	"src/test/resources/cker0.csv", 
				centeredSplinePieces( 0 ),
				start, end, 0.1, 0.5 );

		toCsv( 	"src/test/resources/cker1.csv", 
				centeredSplinePieces( 1 ),
				start, end, 0.1, 1 );

		toCsv( 	"src/test/resources/cker2.csv", 
				centeredSplinePieces( 2 ),
				start, end, 0.1, 1.5 );

		toCsv( 	"src/test/resources/cker3.csv", 
				centeredSplinePieces( 3 ),
				start, end, 0.1, 2.0 );

		toCsv( 	"src/test/resources/cker4.csv", 
				centeredSplinePieces( 4 ),
				start, end, 0.1, 2.5 );

		toCsv( 	"src/test/resources/cker5.csv", 
				centeredSplinePieces( 5 ),
				start, end, 0.1, 3.0 );
	}
	
	/**
	 * Return a new polynomial that is shifted in x by s.  Specifically if p is
	 *   p_0 + p_1 * x + ... + p_n * x^n
	 * 
	 * this method returns
	 * 
	 *   p_0 + p_1 * (x+s)  + ... + p_n * (x+s)^n
	 *   
	 * but in canonical form: 
	 *   = q_0 + q_1 * x + ... + q_n * x^n
	 * 
	 * @param p the polynomial coeficients
	 * @param s the shift
	 * @return the shifted polynomial
	 */
	public static double[] shift( final double[] p, final double s )
	{
		int order = p.length;
		double[] q = new double[ order ];
		q[ 0 ] = p[ 0 ];

		for( int i = 1; i < order; i++ )
		{
			double[] tmp = shiftedPoly( i, s );

			for( int j = 0; j < tmp.length; j++ )
				tmp[ j ] *= p[ i ];

			q = add( q, tmp );
		}

		return q;
	}

	public static void toCsv( String fpath, ArrayList<double[]> piecewisePolys, double start, double end, double step, double offset ) throws IOException
	{
		StringBuffer s = new StringBuffer();

		for( double xx = start; xx <= end; xx += step)
		{
			double x = xx + offset;
			int i = (int)Math.floor( x );
			double y = 0.0;
			if( i >= 0 &&  i < piecewisePolys.size() )
			{
				y = apply( piecewisePolys.get( i ), x );
			}
			s.append( String.format("%f,%f\n", xx, y ));
		}
		Files.write( Paths.get( fpath ), s.toString().getBytes(), StandardOpenOption.CREATE);
	}
	
	public static void toCsv( String fpath, ArrayList<double[]> piecewisePolys, double start, double end, double step ) throws IOException
	{
		toCsv( fpath, piecewisePolys, start, end, step, 0.0 );
	}

	/**
	 * Uncentered polys only
	 * 
	 */
	public static double apply( final ArrayList<double[]> polys, double x )
	{
		int i = (int)Math.floor( x );
		if( i < 0 )
			return 0.0;
		else if( i >= polys.size())
			return 0.0;
		else
			return apply( polys.get( i ), x );
	}
	
	public static double apply( final double[] poly, double x )
	{
		double y = poly[ 0 ];
		for( int i = 1; i < poly.length; i++ )
		{
			y += ( poly[ i ] * pow( x, i ));
		}
		return y;
	}
	
	public static double pow( final double x, final int n )
	{
		assert n >= 0;

		double y = 1;
		for( int i = 0; i < n; i++ )
		{
			y *= x;
		}
		return y;	
	}

	/**
	 * Returns a list representing the bspline kernel of order n.
	 * 
	 * A polynomial is represented as a double array (p), where p[i] is the
	 * coefficient of the ith term of the polynomial ( e.g. [2,1,0,4]
	 * corresponds to 2 + x + 4(x^3))
	 *
	 * The ith array of represents the ith polynomial of the sum in Eq. 2.2 in
	 * Unser et al.
	 * 
	 * @param n
	 *            the spline order
	 * @return a representation of the kernel
	 */
	public static ArrayList< double[] > splinePieces( long n )
	{
		ArrayList< double[] > piecewisePolys = new ArrayList<>();

		double[] p;
		double[] last = null;
		for ( long j = 0; j < n + 1; j++ )
		{
			p = bsplinePolyPiece( n, j );

			double[] q;
			if ( last != null )
			{
				q = add( last, p );
			}
			else
				q = p;

			piecewisePolys.add( q );

			last = q;
		}

		return piecewisePolys;
	}

	/**
	 * Generate part of the polynomial that is present at x >= j
	 * 
	 * @param n
	 *            spline order
	 * @param j
	 *            offset
	 * @return the polynomial
	 * 
	 */
	public static double[] bsplinePolyPiece( long n, long j )
	{
		long a = nCk( n + 1, j );
		long sng = ( j % 2 == 0 ) ? 1 : -1;
		long b = fact( n );
		
		double coef = ((double) a / b ) * sng; 
		double[] p = shiftedPoly( n, (double)-j );

		for( int i = 0; i < p.length; i++ )
			p[ i ] *= coef;

		return p;
	}
	
	/**
	 * returns the polynomial coefficients for (x + j)^n
	 * 
	 * @param n
	 *            spline order
	 * @param j
	 *            offset
	 * @return the shifted polynomial
	 */
	public static double[] shiftedPoly( final long n, final double j )
	{
		double[] start = new double[]{ j, 1 };
		double[] out = start;
		for( int i = 0; i < n - 1; i++ )
			out = polyMult( out, start );

		return out;
	}
	
	/**
	 * Multiply polynomials defined by their coefficients.
	 * 
	 * @param a
	 *            first coefs
	 * @param b
	 *            second coefs
	 * @return coefficients of the new polynomial
	 */
	public static double[] polyMult( final double[] a, final double[] b )
	{
		int M = a.length + b.length - 1;

		double[] out = new double[ M ];
		for ( int i = 0; i < a.length; i++ )
		{
			for ( int j = 0; j < b.length; j++ )
			{
				out[ i + j ] += a[ i ] * b[ j ];
			}
		}

		return out;
	}

	/**
	 * Add two polynomials of potentially different order.
	 * 
	 * @param a
	 *            first polynomial
	 * @param b
	 *            second polynomial
	 * @return resulting polynomial
	 */
	public static double[] add( final double[] a, final double[] b )
	{
		int Na = a.length;
		int Nb = b.length;
		int N = Na > Nb ? Na : Nb;

		double[] c = new double[ N ];
		for ( int i = 0; i < N; i++ )
		{
			if ( i < Na && i < Nb )
				c[ i ] = a[ i ] + b[ i ];
			else if ( i < Na )
				c[ i ] = a[ i ];
			else if ( i < Nb )
				c[ i ] = b[ i ];
		}

		return c;
	}

	/**
	 * Factorial
	 * 
	 * @param i
	 * @return
	 */
	public static long fact( long i )
	{
		long out = 1;
		while ( i > 1 )
		{
			out *= i;
			i--;
		}
		return out;
	}

	/**
	 * Binomial coefficient: n choose k
	 */
	public static long nCk( long n, long k )
	{
		long num = fact( n );
		long den = fact( k ) * fact( n - k );
		return num / den;
	}

}
