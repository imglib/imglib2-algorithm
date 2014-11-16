package net.imglib2.algorithm.morphology.neighborhoods;

import net.imglib2.AbstractEuclideanSpace;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.Localizable;
import net.imglib2.RandomAccessible;
import net.imglib2.Sampler;
import net.imglib2.algorithm.region.localneighborhood.Neighborhood;

public abstract class DiamondNeighborhoodLocalizableSampler< T > extends AbstractEuclideanSpace implements Localizable, Sampler< Neighborhood< T >>
{
	protected final RandomAccessible< T > source;

	protected final Interval sourceInterval;

	protected final long radius;

	protected final DiamondNeighborhoodFactory< T > factory;

	protected final long[] currentPos;

	protected final Neighborhood< T > currentNeighborhood;

	public DiamondNeighborhoodLocalizableSampler( final RandomAccessible< T > source, final long radius, final DiamondNeighborhoodFactory< T > factory, final Interval accessInterval )
	{
		super( source.numDimensions() );
		this.source = source;
		this.radius = radius;
		this.factory = factory;
		this.currentPos = new long[ n ];

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
				accessMin[ d ] = currentPos[ d ] - radius;
				accessMax[ d ] = currentPos[ d ] + radius;
			}
			sourceInterval = new FinalInterval( accessMin, accessMax );
		}

		this.currentNeighborhood = factory.create( currentPos, radius, sourceInterval == null ? source.randomAccess() : source.randomAccess( sourceInterval ) );
	}

	protected DiamondNeighborhoodLocalizableSampler( final DiamondNeighborhoodLocalizableSampler< T > c )
	{
		super( c.n );
		this.source = c.source;
		this.sourceInterval = c.sourceInterval;
		this.radius = c.radius;
		this.factory = c.factory;
		this.currentPos = c.currentPos.clone();
		this.currentNeighborhood = factory.create( currentPos, radius, sourceInterval == null ? source.randomAccess() : source.randomAccess( sourceInterval ) );
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

}
