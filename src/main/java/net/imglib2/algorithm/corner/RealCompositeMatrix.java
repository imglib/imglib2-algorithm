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

/**
 *
 * @author Philipp Hanslovsky &lt;hanslovskyp@janelia.hhmi.org&gt;
 *
 *         {@link RealMatrix} that reads data from {@link RealComposite}
 *         (non-copy).
 *
 * @param <T>
 */
public class RealCompositeMatrix< T extends RealType< T > > extends AbstractRealMatrix
{

	protected final RealComposite< T > data;

	protected final int nRows;

	protected final int nCols;

	protected final int length;

	public RealCompositeMatrix( final RealComposite< T > data, final int nRows, final int nCols )
	{
		this( data, nRows, nCols, nRows * nCols );
	}

	public RealCompositeMatrix( final RealComposite< T > data, final int nRows, final int nCols, final int length )
	{
		super();

		assert length == expectedLength( nRows, nCols );

		this.data = data;
		this.nRows = nRows;
		this.nCols = nCols;
		this.length = length;
	}

	@Override
	public RealMatrix copy()
	{
		// Supposed to be a deep copy, cf apache docs:
		// http://commons.apache.org/proper/commons-math/apidocs/org/apache/commons/math3/linear/RealMatrix.html#copy()
		final RealCompositeMatrix< T > result = ( RealCompositeMatrix< T > ) createMatrix( nRows, nCols );
		for ( int i = 0; i < length; ++i )
		{
			result.data.get( i ).set( this.data.get( i ) );
		}
		return result;
	}

	@Override
	public RealMatrix createMatrix( final int nRows, final int nCols ) throws NotStrictlyPositiveException
	{
		final T t = this.data.get( 0 );
		final Img< T > img;
		final int length = expectedLength( nRows, nCols );
		if ( NativeType.class.isInstance( t ) )
		{
			img = ( ( NativeType ) t ).createSuitableNativeImg( new ArrayImgFactory<>(), new long[] { length } );
		}
		else
		{
			img = new ListImgFactory< T >().create( new long[] { length }, t );
		}
		final RealComposite< T > data = new RealComposite< T >( img.randomAccess(), length );

		return createMatrix( data, nRows, nCols, length );
	}

	public < U extends RealType< U > > RealCompositeMatrix< U > createMatrix( final RealComposite< U > data, final int nRows, final int nCols, final int length )
	{
		return new RealCompositeMatrix<>( data, nRows, nCols, length );
	}

	@Override
	public int getColumnDimension()
	{
		return this.nCols;
	}

	@Override
	public double getEntry( final int row, final int col ) throws OutOfRangeException
	{
		if ( row < 0 || row >= this.nRows )
		{
			throw new OutOfRangeException( row, 0, this.nRows );
		}
		else if ( col < 0 || col >= this.nCols ) {
			throw new OutOfRangeException( col, 0, this.nCols );
		}
		final double val = data.get( rowAndColumnToLinear( row, col ) ).getRealDouble();

		return val;
	}

	@Override
	public int getRowDimension()
	{
		return this.nRows;
	}

	@Override
	public void setEntry( final int row, final int col, final double val ) throws OutOfRangeException
	{
		if ( row < 0 || row >= this.nRows )
		{
			throw new OutOfRangeException( row, 0, this.nRows );
		}
		else if ( col < 0 || col >= this.nCols ) { throw new OutOfRangeException( col, 0, this.nCols ); }

		data.get( rowAndColumnToLinear( row, col ) ).setReal( val );

	}

	public int rowAndColumnToLinear( final int row, final int col )
	{
		return row * nCols + col;
	}

	public int expectedLength( final int nRows, final int nCols )
	{
		return nRows * nCols;
	}


}
