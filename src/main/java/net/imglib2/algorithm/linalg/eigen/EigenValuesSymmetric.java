package net.imglib2.algorithm.linalg.eigen;

import org.apache.commons.math3.linear.EigenDecomposition;

import net.imglib2.algorithm.linalg.matrix.RealCompositeSymmetricMatrix;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.composite.RealComposite;

public class EigenValuesSymmetric< T extends RealType< T >, U extends RealType< U > > implements EigenValues< T, U >
{
	private final int nDim;

	public EigenValuesSymmetric( final int nDim )
	{
		super();
		this.nDim = nDim;
	}

	@Override
	public void compute( final RealComposite< T > tensor, final RealComposite< U > evs )
	{
		final RealCompositeSymmetricMatrix< T > m = new RealCompositeSymmetricMatrix<>( tensor, nDim );
		final EigenDecomposition ed = new EigenDecomposition( m );
		for ( int z = 0; z < nDim; ++z )
			evs.get( z ).setReal( ed.getRealEigenvalue( z ) );
	}
}