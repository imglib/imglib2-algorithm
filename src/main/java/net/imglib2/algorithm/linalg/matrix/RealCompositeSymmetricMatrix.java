package net.imglib2.algorithm.linalg.matrix;

import org.apache.commons.math3.linear.RealMatrix;

import net.imglib2.type.numeric.RealType;
import net.imglib2.view.composite.Composite;

/**
 *
 * @author Philipp Hanslovsky
 *
 *         Symmetric {@link RealMatrix} that reads data from {@link Composite}
 *         (non-copy).
 *
 * @param <T>
 */
public class RealCompositeSymmetricMatrix< T extends RealType< T > > extends RealCompositeSquareMatrix< T >
{

	public RealCompositeSymmetricMatrix( final Composite< T > data, final int nRowsOrCols )
	{
		this( data, nRowsOrCols, nRowsOrCols * ( nRowsOrCols + 1 ) / 2 );
	}

	public RealCompositeSymmetricMatrix( final Composite< T > data, final int nRowsOrCols, final int length )
	{
		super( data, nRowsOrCols, length );
	}

	@Override
	public int getRowDimension()
	{
		return this.nRows;
	}

	@Override
	public int expectedLength( final int nRows, final int nCols )
	{
		assert nRows == nCols;
		return nRows * ( nRows + 1 ) / 2;
	}

	@Override
	public < U extends RealType< U > > RealCompositeSymmetricMatrix< U > createMatrix( final Composite< U > data, final int nRows, final int nCols, final int length )
	{
		return new RealCompositeSymmetricMatrix<>( data, nRows );
	}

	@Override
	public int rowAndColumnToLinear( final int row, final int col )
	{

		// total number of elements: length = nRows * ( nRows + 1 ) / 2
		// row - 1 complete rows
		// number elements in non-complete rows: n = ( nRows - ( row - 1 ) ) * (
		// nRows -row ) / 2
		// number of elements total: length - n + ( col - row )

		if ( row < col )
		{
			final int rowDiff = nRows - row;
			final int n = rowDiff * ( rowDiff + 1 ) / 2;
			return length - n + col - row;
		}
		else
		{
			final int rowDiff = nRows - col;
			final int n = rowDiff * ( rowDiff + 1 ) / 2;
			return length - n + row - col;
		}
	}

}
