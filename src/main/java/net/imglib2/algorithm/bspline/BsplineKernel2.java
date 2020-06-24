package net.imglib2.algorithm.bspline;

public class BsplineKernel2 extends AbstractBsplineKernel
{
	public double c0()
	{
		return 8;
	}

	public double evaluate( final double x )
	{
		final double xabs = Math.abs( x ) + 1.5;
		if( xabs < 2 )
		{
			return ( 3 - xabs ) * xabs - 1.5;
		}
		else if( xabs < 3 )
		{
			double xr = xabs - 3;
			return 0.5 * xr * xr;
		}
		else
			return 0;
	}

	public double evaluateNorm( final double x )
	{
		return c0() * evaluate( x ); // TODO check and make efficient?
	}

}
