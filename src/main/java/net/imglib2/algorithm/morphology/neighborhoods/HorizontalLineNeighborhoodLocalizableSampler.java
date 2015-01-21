package net.imglib2.algorithm.morphology.neighborhoods;

import net.imglib2.AbstractEuclideanSpace;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.Localizable;
import net.imglib2.RandomAccessible;
import net.imglib2.Sampler;
import net.imglib2.algorithm.region.localneighborhood.Neighborhood;

public abstract class HorizontalLineNeighborhoodLocalizableSampler< T > extends AbstractEuclideanSpace implements Localizable, Sampler< Neighborhood< T > >
{
	protected final RandomAccessible< T > source;

	protected final Interval sourceInterval;

	protected final long span;

	protected final int dim;

	protected final long[] currentPos;

	protected final HorizontalLineNeighborhoodFactory< T > neighborhoodFactory;

	protected final Neighborhood< T > currentNeighborhood;

	private final boolean skipCenter;

	public HorizontalLineNeighborhoodLocalizableSampler( final RandomAccessible< T > source, final long span, final int dim, final boolean skipCenter, final HorizontalLineNeighborhoodFactory< T > factory, final Interval accessInterval )
	{
		super( source.numDimensions() );
		this.source = source;
		this.span = span;
		this.dim = dim;
		this.skipCenter = skipCenter;
		this.currentPos = new long[ n ];
		this.neighborhoodFactory = factory;
		
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
				accessMin[ d ] = currentPos[ d ] - span;
				accessMax[ d ] = currentPos[ d ] + span;
			}
			sourceInterval = new FinalInterval( accessMin, accessMax );
		}

		currentNeighborhood = neighborhoodFactory.create( currentPos, span, dim, false, sourceInterval == null ? source.randomAccess() : source.randomAccess( sourceInterval ) );
	}

	protected HorizontalLineNeighborhoodLocalizableSampler( final HorizontalLineNeighborhoodLocalizableSampler< T > c )
	{
		super( c.n );
		source = c.source;
		sourceInterval = c.sourceInterval;
		span = c.span;
		skipCenter = c.skipCenter;
		dim = c.dim;
		neighborhoodFactory = c.neighborhoodFactory;
		currentPos = c.currentPos.clone();
		currentNeighborhood = neighborhoodFactory.create( currentPos, span, dim, skipCenter, source.randomAccess() );
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
