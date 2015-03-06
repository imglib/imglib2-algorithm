package net.imglib2.algorithm.neighborhood;

import net.imglib2.Interval;
import net.imglib2.Localizable;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;

public class PairOfPointsNeighborhoodRandomAccess< T > extends PairOfPointsNeighborhoodLocalizableSampler< T > implements RandomAccess< Neighborhood< T > >
{
	public PairOfPointsNeighborhoodRandomAccess( final RandomAccessible< T > source, final long[] offset, final PairOfPointsNeighborhoodFactory< T > factory, final Interval interval )
	{
		super( source, offset, factory, interval );
	}

	public PairOfPointsNeighborhoodRandomAccess( final RandomAccessible< T > source, final long[] offset, final PairOfPointsNeighborhoodFactory< T > factory )
	{
		super( source, offset, factory, null );
	}

	private PairOfPointsNeighborhoodRandomAccess( final PairOfPointsNeighborhoodRandomAccess< T > c )
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
	public PairOfPointsNeighborhoodRandomAccess< T > copy()
	{
		return new PairOfPointsNeighborhoodRandomAccess< T >( this );
	}

	@Override
	public PairOfPointsNeighborhoodRandomAccess< T > copyRandomAccess()
	{
		return copy();
	}
}
