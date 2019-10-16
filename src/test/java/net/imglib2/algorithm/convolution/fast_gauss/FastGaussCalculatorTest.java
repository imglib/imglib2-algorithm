package net.imglib2.algorithm.convolution.fast_gauss;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests {@link FastGaussCalculator}.
 */
public class FastGaussCalculatorTest
{
	private final double sigma = 10.0;

	private final FastGaussCalculator.Parameters parameters = FastGaussCalculator.Parameters.exact( sigma );

	private final FastGaussCalculator calculator = new FastGaussCalculator( parameters );

	@Test
	public void testInitialize() {
		double boundaryValue = 1.0;
		calculator.initialize( boundaryValue );
		assertEquals( boundaryValue, calculator.getValue(), 1e-10 );
		for ( int i = 0; i < parameters.N; i++ )
		{
			calculator.update( boundaryValue + boundaryValue );
			assertEquals( boundaryValue, calculator.getValue(), 1e-10 );
		}
	}

	@Test
	public void testOverall() {
		calculator.initialize( 0.0 );
		for ( int x = - 2 * parameters.N; x < 2 * parameters.N; x++ )
		{
			double expected = gauss( sigma, x );
			double actual = calculator.getValue();
			assertEquals( expected, actual, 1e-4 );
			calculator.update( input( x - parameters.N ) + input( x + parameters.N) );
		}
	}

	private double input( long x )
	{
		return ( x == 0 ) ? 1.0 : 0.0;
	}

	private double gauss( double sigma, long x )
	{
		double a = 1. / Math.sqrt( 2 * Math.PI * Math.pow( sigma, 2 ) );
		double b = -0.5 / Math.pow( sigma, 2 );
		return a * Math.exp( b * Math.pow( x, 2 ) );
	}
}
