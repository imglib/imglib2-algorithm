package net.imglib2.algorithm.corner.eigen;

import org.apache.commons.math3.linear.EigenDecomposition;

import net.imglib2.algorithm.corner.matrix.RealCompositeSymmetricMatrix;
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
		final int nImageDim = nDim - 1;
		final RealCompositeSymmetricMatrix< T > m = new RealCompositeSymmetricMatrix<>( tensor, nImageDim );
		final EigenDecomposition ed = new EigenDecomposition( m );
		final double[] evArray = ed.getRealEigenvalues();
		for ( int z = 0; z < evArray.length; ++z )
		{
			evs.get( z ).setReal( evArray[ z ] );
		}
	}
}