package net.imglib2.algorithm.linalg.eigen;

import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.composite.Composite;

public class EigenValues2DSquare< T extends RealType< T >, U extends ComplexType< U > > implements EigenValues< T, U >
{
	@Override
	public void compute( final Composite< T > tensor, final Composite< U > evs )
	{
		final double x11 = tensor.get( 0 ).getRealDouble();
		final double x12 = tensor.get( 1 ).getRealDouble();
		final double x21 = tensor.get( 2 ).getRealDouble();
		final double x22 = tensor.get( 3 ).getRealDouble();
		final double sum = x11 + x22;
		final double diff = x11 - x22;
		final double radicand = 4 * x12 * x21 + diff * diff;
		if ( radicand < 0.0d )
		{
			final double halfSqrt = 0.5 * Math.sqrt( -radicand );
			final double halfSum = 0.5 * sum;
			evs.get( 0 ).setComplexNumber( halfSum, halfSqrt );
			evs.get( 1 ).setComplexNumber( halfSum, -halfSqrt );
		}
		else
		{
			final double sqrt = Math.sqrt( radicand );
			evs.get( 0 ).setComplexNumber( 0.5 * ( sum + sqrt ), 0.0d );
			evs.get( 1 ).setComplexNumber( 0.5 * ( sum - sqrt ), 0.0d );
		}
	}
}