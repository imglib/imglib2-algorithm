package net.imglib2.algorithm.linalg.matrix;

import org.apache.commons.math3.linear.RealMatrix;

import net.imglib2.type.numeric.RealType;
import net.imglib2.view.composite.Composite;

/**
 *
 * @author Philipp Hanslovsky
 *
 *         Square {@link RealMatrix} that reads data from {@link Composite}
 *         (non-copy).
 *
 * @param <T>
 */
public class RealCompositeSquareMatrix< T extends RealType< T > > extends RealCompositeMatrix< T >
{

	public RealCompositeSquareMatrix( final Composite< T > data, final int nRowsOrCols )
	{
		this( data, nRowsOrCols, nRowsOrCols * nRowsOrCols );
	}

	public RealCompositeSquareMatrix( final Composite< T > data, final int nRowsOrCols, final int length )
	{
		super( data, nRowsOrCols, nRowsOrCols, length );
	}

	@Override
	public < U extends RealType< U > > RealCompositeSquareMatrix< U > createMatrix( final Composite< U > data, final int nRows, final int nCols, final int length )
	{
		return new RealCompositeSquareMatrix<>( data, nRows, length );
	}

}
