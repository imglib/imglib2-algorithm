package net.imglib2.algorithm.corner;

import org.apache.commons.math3.linear.RealMatrix;

import net.imglib2.type.numeric.RealType;
import net.imglib2.view.composite.RealComposite;

public class RealCompositeSymmetricMatrix< T extends RealType< T > > extends RealCompositeSquareMatrix< T >
{

	public RealCompositeSymmetricMatrix( final RealComposite< T > data, final int nRowsOrCols )
	{
		this( data, nRowsOrCols, nRowsOrCols * ( nRowsOrCols + 1 ) / 2 );
	}

	public RealCompositeSymmetricMatrix( final RealComposite< T > data, final int nRowsOrCols, final int length )
	{
		super( data, nRowsOrCols, length );
	}

	@Override
	public RealMatrix copy()
	{
		// Is this supposed to be deep copy?
		return new RealCompositeSymmetricMatrix<>( this.data, this.nRows );
	}

//	@Override
//	public RealMatrix createMatrix( final int nRows, final int nColums ) throws NotStrictlyPositiveException
//	{
//		if ( nRows != nColums ) {
//			throw new UnsupportedOperationException("Number of colums and number of rows must be the same!");
//		}
//
//		final T t = this.data.get( 0 );
//		final Img< T > img;
//		final int length = expectedLength( nRows, nCols );
//		if ( NativeType.class.isInstance( t ) )
//		{
//			img = ( ( NativeType ) t ).createSuitableNativeImg( new ArrayImgFactory<>(), new long[] { length } );
//		}
//		else
//		{
//			img = new ListImgFactory< T >().create( new long[] { length }, t );
//		}
//		final RealComposite< T > data = new RealComposite< T >( img.randomAccess(), length );
//
//		return new RealCompositeSymmetricMatrix<>( data, nRows, length );
//	}

	@Override
	public int getRowDimension()
	{
		return this.nRows;
	}

	@Override
	public int expectedLength( final int nRows, final int nCols )
	{
		return nRows * ( nRows + 1 ) / 2;
	}

	@Override
	public < U extends RealType< U > > RealCompositeMatrix< U > createMatrix( final RealComposite< U > data, final int nRows, final int nCols, final int length )
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
