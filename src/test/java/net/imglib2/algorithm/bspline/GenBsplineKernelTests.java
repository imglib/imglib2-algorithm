package net.imglib2.algorithm.bspline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests surrounding polynomial code used to pre-compute the coefficients of the
 * polynomials comprising the b-spline kernel functions.
 *
 */
public class GenBsplineKernelTests
{
	@Test
	public void testCombinatorics()
	{
		long fact5 = GenBsplineKernels.fact( 5 );
		Assert.assertEquals( "5 factorial ", 120, fact5 );

		long fact6 = GenBsplineKernels.fact( 6 );
		Assert.assertEquals( "6 factorial ", 720, fact6 );

		long tenChooseThree = GenBsplineKernels.nCk( 10, 3 );
		Assert.assertEquals( "10 choose 3", 120, tenChooseThree );

		long twelveChooseFour = GenBsplineKernels.nCk( 12, 4 );
		Assert.assertEquals( "12 choose 4", 495, twelveChooseFour );
	}

	@Test
	public void testKnotValueSums()
	{
		// sum of kernel at the knots should equal zero
		for( int order = 0; order <= 5; order++ )
		{
			ArrayList<double[]> polys = GenBsplineKernels.centeredSplinePieces( 3 );
			double[] knotValues = GenBsplineKernels.valuesAtKnots( polys );
			double sum = Arrays.stream( knotValues ).sum();

			Assert.assertEquals( String.format( "knot sum order: %d", order ), 1.0, sum, 1e-6 );
		}
	}

	@Test
	public void testPolyShift()
	{
		double[] p = new double[]{ 0, 0, 1 };
		double[] q = GenBsplineKernels.shift( p , -2 );
		for( double x = -2; x <= 2; x += 0.5 )
		{
			double y = GenBsplineKernels.apply( p, x );
			double ys = GenBsplineKernels.apply( q, x + 2 );
			Assert.assertEquals( String.format( "shift test at %f", x ), y, ys, 1e-6 );
		}
	}

	@Test
	public void testPolyMult()
	{
		// (-1,1) * (1,0,1)
		// ->
		// ( -1 + x ) * (1 + x^2  )
		// = 
		// (x^3 + x - x^2 - 1)
		// = 
		// ( -1 + x - x^2 + x^3 )
		// ->
		// (1, -1, 1, -1)
		double[] a = new double[]{ -1, 1 };
		double[] b = new double[]{ 1, 0, 1 };
		double[] c = GenBsplineKernels.polyMult( a, b );
		Assert.assertArrayEquals( "polynomial multiply", new double[] {-1,1,-1,1}, c, 1e-6 );
	}
	
	@Test
	public void testKernelPolynomialPieces()
	{
		double[] p0_j0 = GenBsplineKernels.bsplinePolyPiece( 0, 0 );
		Assert.assertArrayEquals( "p0", new double[]{ 0, 1 }, p0_j0, 1e-6 );
		
		ArrayList< double[] > p0c = GenBsplineKernels.centeredSplinePieces( 0 );
		Assert.assertEquals( "number of polys order 0", 1, p0c.size() );
		Assert.assertArrayEquals( "p0 centered", new double[]{ -0.5, 1 }, p0c.get( 0 ), 1e-6 );

		ArrayList< double[] > p3c = GenBsplineKernels.centeredSplinePieces( 3 );
		Assert.assertEquals( "number of polys order 3", 4, p3c.size() );
		
		double[] p3_r0to1 = new double[]{ 2.0/3, 0, -1, 0.5 };
		double[] tmp = new double[]{ 2.0/6, -1/6 };
		double[] p3_r1to2 = 
				GenBsplineKernels.polyMult(
					tmp,
					GenBsplineKernels.polyMult( tmp, tmp ));

		// confirm that kernel computed from spline pieces is consistent with closed form 
		// equations from Unser et al.
		Function<Double,Double> b3_0to1 = x -> { return ( 2.0 / 3) - (x * x ) + (x * x * x) / 2; };
		Function<Double,Double> b3_1to2 = x -> { return ( 2 - x ) * ( 2 - x ) * (2 - x) / 6; };
		
		for( double x = 0; x <= 1; x += 0.25 )
		{
			double ytrue = b3_0to1.apply( x );
			double y = GenBsplineKernels.bsplineKernel( x, 3 );
			Assert.assertEquals( String.format( "b3 at %f", x ), 
					ytrue, y, 1e-6 );
		}
		for( double x = 1; x <= 2; x += 0.25 )
		{
			double ytrue = b3_1to2.apply( x );
			double y = GenBsplineKernels.bsplineKernel( x, 3 );
			Assert.assertEquals( String.format( "b3 at %f", x ), 
					ytrue, y, 1e-6 );
		}
	}

}
