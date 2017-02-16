package net.imglib2.algorithm.linalg.eigen;

import net.imglib2.type.numeric.RealType;
import net.imglib2.view.composite.RealComposite;

public class EigenValues2DSquare< T extends RealType< T >, U extends RealType< U > > implements EigenValues< T, U >
{
	@Override
	public void compute( final RealComposite< T > tensor, final RealComposite< U > evs )
	{
		final double x11 = tensor.get( 0 ).getRealDouble();
		final double x12 = tensor.get( 1 ).getRealDouble();
		final double x21 = tensor.get( 2 ).getRealDouble();
		final double x22 = tensor.get( 3 ).getRealDouble();
		final double sum = x11 + x22;
		final double diff = x11 - x22;
		final double sqrt = Math.sqrt( 4 * x12 * x21 + diff * diff );
		evs.get( 0 ).setReal( 0.5 * ( sum + sqrt ) );
		evs.get( 1 ).setReal( 0.5 * ( sum - sqrt ) );
	}
}