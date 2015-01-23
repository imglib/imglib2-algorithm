package net.imglib2.algorithm.morphology.neighborhoods;

import net.imglib2.AbstractEuclideanSpace;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.Localizable;
import net.imglib2.RandomAccessible;
import net.imglib2.Sampler;
import net.imglib2.algorithm.region.localneighborhood.Neighborhood;

public abstract class PeriodicLineNeighborhoodLocalizableSampler< T > extends AbstractEuclideanSpace implements Localizable, Sampler< Neighborhood< T > >
{
	protected final RandomAccessible< T > source;

	protected final Interval sourceInterval;

	protected final long span;

	protected final long[] currentPos;

	protected final PeriodicLineNeighborhoodFactory< T > neighborhoodFactory;

	protected final Neighborhood< T > currentNeighborhood;

	private final int[] increments;

	public PeriodicLineNeighborhoodLocalizableSampler( final RandomAccessible< T > source, final long span, final int[] increments, final PeriodicLineNeighborhoodFactory< T > factory, final Interval accessInterval )
	{
		super( source.numDimensions() );
		this.source = source;
		this.span = span;
		this.increments = increments;
		this.currentPos = new long[ n ];
		neighborhoodFactory = factory;

		if ( accessInterval == null )
		{
			sourceInterval = null;
		}
		else
		{
			final long[] accessMin = new long[ n ];
			final long[] accessMax = new long[ n ];
			accessInterval.min( accessMin );
			accessInterval.max( accessMax );
			for ( int d = 0; d < n; ++d )
			{
				accessMin[ d ] = currentPos[ d ] - increments[ d ] * span;
				accessMax[ d ] = currentPos[ d ] + increments[ d ] * span;
			}
			sourceInterval = new FinalInterval( accessMin, accessMax );
		}
		currentNeighborhood = neighborhoodFactory.create( currentPos, span, increments, sourceInterval == null ? source.randomAccess() : source.randomAccess( sourceInterval ) );
	}

	protected PeriodicLineNeighborhoodLocalizableSampler( final PeriodicLineNeighborhoodLocalizableSampler< T > c )
	{
		super( c.n );
		source = c.source;
		sourceInterval = c.sourceInterval;
		span = c.span;
		increments = c.increments.clone();
		neighborhoodFactory = c.neighborhoodFactory;
		currentPos = c.currentPos.clone();
		currentNeighborhood = neighborhoodFactory.create( currentPos, span, increments, source.randomAccess() );
	}

	@Override
	public Neighborhood< T > get()
	{
		return currentNeighborhood;
	}

	@Override
	public void localize( final int[] position )
	{
		currentNeighborhood.localize( position );
	}

	@Override
	public void localize( final long[] position )
	{
		currentNeighborhood.localize( position );
	}

	@Override
	public int getIntPosition( final int d )
	{
		return currentNeighborhood.getIntPosition( d );
	}

	@Override
	public long getLongPosition( final int d )
	{
		return currentNeighborhood.getLongPosition( d );
	}

	@Override
	public void localize( final float[] position )
	{
		currentNeighborhood.localize( position );
	}

	@Override
	public void localize( final double[] position )
	{
		currentNeighborhood.localize( position );
	}

	@Override
	public float getFloatPosition( final int d )
	{
		return currentNeighborhood.getFloatPosition( d );
	}

	@Override
	public double getDoublePosition( final int d )
	{
		return currentNeighborhood.getDoublePosition( d );
	}

}
