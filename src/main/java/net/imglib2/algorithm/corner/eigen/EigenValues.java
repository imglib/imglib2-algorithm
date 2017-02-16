package net.imglib2.algorithm.corner.eigen;

import net.imglib2.type.numeric.RealType;
import net.imglib2.view.composite.RealComposite;

/**
 *
 * Interface for handling different cases, e.g. square, symmetric, or 2
 * dimensional tensors.
 *
 */
public interface EigenValues< T extends RealType< T >, U extends RealType< U > >
{
	public void compute( final RealComposite< T > matrix, final RealComposite< U > evs );

	public static < T extends RealType< T >, U extends RealType< U > > EigenValues1D< T, U > oneDimensional()
	{
		return new EigenValues1D<>();
	}

	public static < T extends RealType< T >, U extends RealType< U > > EigenValues2DSquare< T, U > square2D()
	{
		return new EigenValues2DSquare<>();
	}

	public static < T extends RealType< T >, U extends RealType< U > > EigenValues2DSymmetric< T, U > symmetric2D()
	{
		return new EigenValues2DSymmetric<>();
	}

	public static < T extends RealType< T >, U extends RealType< U > > EigenValuesSquare< T, U > square( final int nDim )
	{
		return new EigenValuesSquare<>( nDim );
	}

	public static < T extends RealType< T >, U extends RealType< U > > EigenValuesSymmetric< T, U > symmetric( final int nDim )
	{
		return new EigenValuesSymmetric<>( nDim );
	}

	public static < T extends RealType< T >, U extends RealType< U > > EigenValues< T, U > invalid()
	{
		return ( m, evs ) -> {
			throw new UnsupportedOperationException( "EigenValues not implemented yet!" );
		};
	}

}