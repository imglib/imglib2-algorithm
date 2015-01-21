package net.imglib2.algorithm.morphology.neighborhoods;

import net.imglib2.Interval;
import net.imglib2.Localizable;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.algorithm.region.localneighborhood.Neighborhood;

public class PairNeighborhoodRandomAccess< T > extends PairNeighborhoodLocalizableSampler< T > implements RandomAccess< Neighborhood< T > >
{
	public PairNeighborhoodRandomAccess( final RandomAccessible< T > source, final long[] offset, final PairNeighborhoodFactory< T > factory, final Interval interval )
	{
		super( source, offset, factory, interval );
	}

	public PairNeighborhoodRandomAccess( final RandomAccessible< T > source, final long[] offset, final PairNeighborhoodFactory< T > factory )
	{
		super( source, offset, factory, null );
	}

	private PairNeighborhoodRandomAccess( final PairNeighborhoodRandomAccess< T > c )
	{
		super( c );
	}

	@Override
	public void fwd( final int d )
	{
		++currentPos[ d ];
	}

	@Override
	public void bck( final int d )
	{
		--currentPos[ d ];
	}

	@Override
	public void move( final int distance, final int d )
	{
		currentPos[ d ] += distance;
	}

	@Override
	public void move( final long distance, final int d )
	{
		currentPos[ d ] += distance;
	}

	@Override
	public void move( final Localizable localizable )
	{
		for ( int d = 0; d < n; ++d )
		{
			final long distance = localizable.getLongPosition( d );
			currentPos[ d ] += distance;
		}
	}

	@Override
	public void move( final int[] distance )
	{
		for ( int d = 0; d < n; ++d )
		{
			currentPos[ d ] += distance[ d ];
		}
	}

	@Override
	public void move( final long[] distance )
	{
		for ( int d = 0; d < n; ++d )
		{
			currentPos[ d ] += distance[ d ];
		}
	}

	@Override
	public void setPosition( final Localizable localizable )
	{
		for ( int d = 0; d < n; ++d )
		{
			final long position = localizable.getLongPosition( d );
			currentPos[ d ] = position;
		}
	}

	@Override
	public void setPosition( final int[] position )
	{
		for ( int d = 0; d < n; ++d )
		{
			currentPos[ d ] = position[ d ];
		}
	}

	@Override
	public void setPosition( final long[] position )
	{
		for ( int d = 0; d < n; ++d )
		{
			currentPos[ d ] = position[ d ];
		}
	}

	@Override
	public void setPosition( final int position, final int d )
	{
		currentPos[ d ] = position;
	}

	@Override
	public void setPosition( final long position, final int d )
	{
		currentPos[ d ] = position;
	}

	@Override
	public PairNeighborhoodRandomAccess< T > copy()
	{
		return new PairNeighborhoodRandomAccess< T >( this );
	}

	@Override
	public PairNeighborhoodRandomAccess< T > copyRandomAccess()
	{
		return copy();
	}
}
