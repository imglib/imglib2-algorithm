package net.imglib2.algorithm.corner;

import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.linear.AbstractRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.list.ListImgFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.composite.RealComposite;

public class RealCompositeSymmetricMatrix< T extends RealType< T > > extends AbstractRealMatrix
{

	private final RealComposite< T > data;

	private final int nRowsOrCols;

	private final int length;

	public RealCompositeSymmetricMatrix( final RealComposite< T > data, final int nRowsOrCols )
	{
		this( data, nRowsOrCols, nRowsOrCols * ( nRowsOrCols + 1 ) / 2 );
	}

	public RealCompositeSymmetricMatrix( final RealComposite< T > data, final int nRowsOrCols, final int length )
	{
		super();

		assert length == nRowsOrCols * ( nRowsOrCols + 1 ) / 2;

		this.data = data;
		this.nRowsOrCols = nRowsOrCols;
		this.length = length;
	}

	@Override
	public RealMatrix copy()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RealMatrix createMatrix( final int nRows, final int nColums ) throws NotStrictlyPositiveException
	{
		if ( nRows != nColums ) {
			throw new UnsupportedOperationException("Number of colums and number of rows must be the same!");
		}

		final T t = this.data.get( 0 );
		final Img< T > img;
		final int length = nRows * ( nRows + 1 ) / 2;
		if ( NativeType.class.isInstance( t ) )
		{
			img = ( ( NativeType ) t ).createSuitableNativeImg( new ArrayImgFactory<>(), new long[] { length } );
		}
		else
		{
			img = new ListImgFactory< T >().create( new long[] { length }, t );
		}
		final RealComposite< T > data = new RealComposite< T >( img.randomAccess(), length );

		return new RealCompositeSymmetricMatrix<>( data, nRowsOrCols, length );
	}

	@Override
	public int getColumnDimension()
	{
		return this.nRowsOrCols;
	}

	@Override
	public double getEntry( final int row, final int col ) throws OutOfRangeException
	{
		if ( row < 0 || row >= nRowsOrCols )
		{
			throw new OutOfRangeException( row, 0, nRowsOrCols );
		}
		else if ( col < 0 || col >= nRowsOrCols ) {
			throw new OutOfRangeException( col, 0, nRowsOrCols );
		}

		final double val;
		if ( row < col )
		{
			val = data.get( rowAndColumnToLinear( nRowsOrCols, length, row, col ) ).getRealDouble();
		}
		else
		{
			val = data.get( rowAndColumnToLinear( nRowsOrCols, length, col, row ) ).getRealDouble();
		}
		return val;
	}

	@Override
	public int getRowDimension()
	{
		return this.nRowsOrCols;
	}

	@Override
	public void setEntry( final int row, final int col, final double val ) throws OutOfRangeException
	{
		if ( row < 0 || row >= nRowsOrCols )
		{
			throw new OutOfRangeException( row, 0, nRowsOrCols );
		}
		else if ( col < 0 || col >= nRowsOrCols ) { throw new OutOfRangeException( col, 0, nRowsOrCols ); }

		if ( row < col )
		{
			data.get( rowAndColumnToLinear( nRowsOrCols, length, row, col ) ).setReal( val );
		}
		else
		{
			data.get( rowAndColumnToLinear( nRowsOrCols, length, col, row ) ).setReal( val );
		}

	}

	public static int rowAndColumnToLinear( final int nRowsOrCols, final int length, final int row, final int col )
	{

		// total number of elements: length = nRows * ( nRows + 1 ) / 2
		// row - 1 complete rows
		// number elements in non-complete rows: n = ( nRows - ( row - 1 ) ) * (
		// nRows -row ) / 2
		// number of elements total: length - n + ( col - row )

		final int rowDiff = nRowsOrCols - row;
		final int n = rowDiff * ( rowDiff + 1 ) / 2;
		return length - n + col - row;
	}

}
