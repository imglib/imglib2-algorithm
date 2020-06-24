package net.imglib2.algorithm.bspline;

public abstract class AbstractBsplineKernel
{
	protected static final double ONESIXTH = 1.0 / 6.0;
	protected static final double TWOTHIRDS = 2.0 / 3.0;

	public abstract double c0();
	
	public abstract double evaluate( final double x );

	public abstract double evaluateNorm( final double x );
	
	protected static double powIntPositive( final double base, final int pow )
	{
		double result = 1;
		for ( int i = 0; i < pow; ++i )
		{
			result *= base;
		}
		return result;
	}

}
