package net.imglib2.algorithm.morphology.neighborhoods;

import net.imglib2.Interval;
import net.imglib2.Localizable;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.algorithm.region.localneighborhood.Neighborhood;

public class PeriodicLineNeighborhoodRandomAccess< T > extends PeriodicLineNeighborhoodLocalizableSampler< T > implements RandomAccess< Neighborhood< T > >
{
	public PeriodicLineNeighborhoodRandomAccess( final RandomAccessible< T > source, final long span, final int[] increments, final PeriodicLineNeighborhoodFactory< T > factory, final Interval interval )
	{
		super( source, span, increments, factory, interval );
	}

	public PeriodicLineNeighborhoodRandomAccess( final RandomAccessible< T > source, final long span, final int[] increments, final PeriodicLineNeighborhoodFactory< T > factory )
	{
		super( source, span, increments, factory, null );
	}

	private PeriodicLineNeighborhoodRandomAccess( final PeriodicLineNeighborhoodRandomAccess< T > c )
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
	public PeriodicLineNeighborhoodRandomAccess< T > copy()
	{
		return new PeriodicLineNeighborhoodRandomAccess< T >( this );
	}

	@Override
	public PeriodicLineNeighborhoodRandomAccess< T > copyRandomAccess()
	{
		return copy();
	}
}
