package net.imglib2.algorithm.morphology.neighborhoods;

import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.region.localneighborhood.Neighborhood;
import net.imglib2.util.IntervalIndexer;

public class DiamondTipsNeighborhoodCursor< T > extends DiamondTipsNeighborhoodLocalizableSampler< T > implements Cursor< Neighborhood< T > >
{

	private final long[] dimensions;

	private final long maxIndex;

	private long maxIndexOnLine;

	private long index;

	private long[] min;

	private long[] max;

	public DiamondTipsNeighborhoodCursor( final RandomAccessibleInterval< T > source, final long radius, final DiamondTipsNeighborhoodFactory< T > factory )
	{
		super( source, radius, factory, source );
		dimensions = new long[ n ];

		min = new long[ n ];
		max = new long[ n ];
		source.dimensions( dimensions );
		source.min( min );
		source.max( max );

		long size = dimensions[ 0 ];
		for ( int d = 1; d < n; ++d )
		{
			size *= dimensions[ d ];
		}
		maxIndex = size;
		reset();
	}


	public DiamondTipsNeighborhoodCursor( final DiamondTipsNeighborhoodCursor< T > c )
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
		{
			nextLine();
		}
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
			{
				break;
			}
		}
	}

	@Override
	public void reset()
	{
		index = 0;
		maxIndexOnLine = dimensions[ 0 ];
		for ( int d = 0; d < n; ++d )
		{
			currentPos[ d ] = ( d == 0 ) ? min[ d ] - 1 : min[ d ];
		}
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
		maxIndexOnLine = ( index < 0 ) ? dimensions[ 0 ] : ( 1 + index / dimensions[ 0 ] ) * dimensions[ 0 ];
		IntervalIndexer.indexToPositionWithOffset( index + 1, dimensions, min, currentPos );
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
	public DiamondTipsNeighborhoodCursor< T > copy()
	{
		return new DiamondTipsNeighborhoodCursor< T >( this );
	}

	@Override
	public DiamondTipsNeighborhoodCursor< T > copyCursor()
	{
		return copy();
	}
}
