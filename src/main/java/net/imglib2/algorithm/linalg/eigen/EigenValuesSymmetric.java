package net.imglib2.algorithm.linalg.eigen;

import org.apache.commons.math3.linear.EigenDecomposition;

import net.imglib2.algorithm.linalg.matrix.RealCompositeSymmetricMatrix;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.composite.Composite;
import net.imglib2.view.composite.RealComposite;

public class EigenValuesSymmetric< T extends RealType< T >, U extends ComplexType< U > > implements EigenValues< T, U >
{
	private final int nDim;

	private final RealCompositeSymmetricMatrix< T > m;

	public EigenValuesSymmetric( final int nDim )
	{
		super();
		this.nDim = nDim;
		this.m = new RealCompositeSymmetricMatrix<>( null, nDim );
	}

	@Override
	public void compute( final RealComposite< T > tensor, final Composite< U > evs )
	{
		m.setData( tensor );
		final EigenDecomposition ed = new EigenDecomposition( m );
		for ( int z = 0; z < nDim; ++z )
			evs.get( z ).setReal( ed.getRealEigenvalue( z ) );
	}

	@Override
	public EigenValuesSymmetric< T, U > copy()
	{
		return new EigenValuesSymmetric<>( nDim );
	}
}