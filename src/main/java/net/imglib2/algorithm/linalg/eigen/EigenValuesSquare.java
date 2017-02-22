package net.imglib2.algorithm.linalg.eigen;

import org.apache.commons.math3.linear.EigenDecomposition;

import net.imglib2.algorithm.linalg.matrix.RealCompositeSquareMatrix;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.composite.Composite;
import net.imglib2.view.composite.RealComposite;

public class EigenValuesSquare< T extends RealType< T >, U extends ComplexType< U > > implements EigenValues< T, U >
{
	private final int nDim;

	public EigenValuesSquare( final int nDim )
	{
		super();
		this.nDim = nDim;
	}

	@Override
	public void compute( final RealComposite< T > tensor, final Composite< U > evs )
	{
		final RealCompositeSquareMatrix< T > m = new RealCompositeSquareMatrix<>( tensor, nDim );
		final EigenDecomposition ed = new EigenDecomposition( m );
		for ( int z = 0; z < nDim; ++z )
			evs.get( z ).setComplexNumber( ed.getRealEigenvalue( z ), ed.getImagEigenvalue( z ) );
	}
}