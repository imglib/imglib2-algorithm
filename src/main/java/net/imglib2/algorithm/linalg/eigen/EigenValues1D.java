package net.imglib2.algorithm.linalg.eigen;

import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.composite.Composite;

public class EigenValues1D< T extends RealType< T >, U extends ComplexType< U > > implements EigenValues< T, U >
{
	@Override
	public void compute( final Composite< T > tensor, final Composite< U > evs )
	{
		evs.get( 0 ).setReal( tensor.get( 0 ).getRealDouble() );
	}
}