package net.imglib2.algorithm.neighborhood;

import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.util.IntervalIndexer;

public class HorizontalLineNeighborhoodCursor< T > extends HorizontalLineNeighborhoodLocalizableSampler< T > implements Cursor< Neighborhood< T > >
{
	private final long[] dimensions;

	private long index;

	private final long maxIndex;

	private long maxIndexOnLine;

	private long[] min;

	private long[] max;

	public HorizontalLineNeighborhoodCursor( final RandomAccessibleInterval< T > source, final long span, final int dim, final boolean skipCenter, final HorizontalLineNeighborhoodFactory< T > factory )
	{
		super( source, span, dim, skipCenter, factory, source );

		dimensions = new long[ n ];

		min = new long[ n ];
		max = new long[ n ];
		source.dimensions( dimensions );
		source.min( min );
		source.max( max );

		long size = dimensions[ 0 ];
		for ( int d = 1; d < n; ++d )
			size *= dimensions[ d ];
		maxIndex = size - 1;
		reset();
	}

	private HorizontalLineNeighborhoodCursor( final HorizontalLineNeighborhoodCursor< T > c )
	{
		super( c );
		dimensions = c.dimensions.clone();
		maxIndex = c.maxIndex;
		index = c.index;
		maxIndexOnLine = c.maxIndexOnLine;
	}

	@Override
	public void fwd()
	{
		++currentPos[ 0 ];
		if ( ++index > maxIndexOnLine )
			nextLine();
	}

	private void nextLine()
	{
		currentPos[ 0 ] = min[ 0 ];
		maxIndexOnLine += dimensions[ 0 ];
		for ( int d = 1; d < n; ++d )
		{
			++currentPos[ d ];
			if ( currentPos[ d ] > max[ d ] )
			{
				currentPos[ d ] = min[ d ];
			}
			else
				break;
		}
	}

	@Override
	public void reset()
	{
		index = -1;
		maxIndexOnLine = -1;
		System.arraycopy( max, 0, currentPos, 0, n );
	}

	@Override
	public boolean hasNext()
	{
		return index < maxIndex;
	}

	@Override
	public void jumpFwd( final long steps )
	{
		index += steps;
		if ( index < 0 )
		{
			maxIndexOnLine = ( ( 1 + index ) / dimensions[ 0 ] ) * dimensions[ 0 ] - 1;
			final long size = maxIndex + 1;
			IntervalIndexer.indexToPositionWithOffset( size - ( -index % size ), dimensions, min, currentPos );
		}
		else
		{
			maxIndexOnLine = ( 1 + index / dimensions[ 0 ] ) * dimensions[ 0 ] - 1;
			IntervalIndexer.indexToPositionWithOffset( index, dimensions, min, currentPos );
		}
	}

	@Override
	public Neighborhood< T > next()
	{
		fwd();
		return get();
	}

	@Override
	public void remove()
	{
		// NB: no action.
	}

	@Override
	public HorizontalLineNeighborhoodCursor< T > copy()
	{
		return new HorizontalLineNeighborhoodCursor< T >( this );
	}

	@Override
	public HorizontalLineNeighborhoodCursor< T > copyCursor()
	{
		return copy();
	}

}
