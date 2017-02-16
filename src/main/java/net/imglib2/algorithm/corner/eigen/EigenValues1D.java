package net.imglib2.algorithm.corner.eigen;

import net.imglib2.type.numeric.RealType;
import net.imglib2.view.composite.RealComposite;

public class EigenValues1D< T extends RealType< T >, U extends RealType< U > > implements EigenValues< T, U >
{
	@Override
	public void compute( final RealComposite< T > tensor, final RealComposite< U > evs )
	{
		evs.get( 0 ).setReal( tensor.get( 0 ).getRealDouble() );
	}
}