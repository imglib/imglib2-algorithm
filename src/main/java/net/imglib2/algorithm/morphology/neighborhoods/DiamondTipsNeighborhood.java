package net.imglib2.algorithm.morphology.neighborhoods;

import java.util.Iterator;

import net.imglib2.AbstractEuclideanSpace;
import net.imglib2.AbstractLocalizable;
import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.Positionable;
import net.imglib2.RandomAccess;
import net.imglib2.RealPositionable;
import net.imglib2.algorithm.region.localneighborhood.Neighborhood;
import net.imglib2.util.Intervals;

/**
 * A {@link Neighborhood} that iterates through the <b>tips</b> of a
 * multi-dimensional diamond.
 * <p>
 * Though it has very few direct applications, it is used in structuring element
 * decomposition for mathematical morphology.
 * 
 * @author Jean-Yves Tinevez <jeanyves.tinevez@gmail.com> Nov 6, 2013
 * @param <T>
 */
public class DiamondTipsNeighborhood< T > extends AbstractLocalizable implements Neighborhood< T >
{

	public static < T > DiamondTipsNeighborhoodFactory< T > factory()
	{
		return new DiamondTipsNeighborhoodFactory< T >()
				{
			@Override
			public DiamondTipsNeighborhood< T > create( final long[] position, final long radius, final RandomAccess< T > sourceRandomAccess )
			{
				return new DiamondTipsNeighborhood< T >( position, radius, sourceRandomAccess );
			}
				};
	}

	private final long radius;

	private final RandomAccess< T > sourceRandomAccess;

	private final Interval structuringElementBoundingBox;

	/**
	 * Creates a new diamond tip neighborhood.
	 * 
	 * @param position
	 *            the central position of the diamond.
	 * @param radius
	 *            the diamond radius in all dimensions.
	 * @param sourceRandomAccess
	 *            a {@link RandomAccess} on the target source.
	 */
	public DiamondTipsNeighborhood( final long[] position, final long radius, final RandomAccess< T > sourceRandomAccess )
	{
		super( position );
		this.radius = radius;
		this.sourceRandomAccess = sourceRandomAccess;
		this.structuringElementBoundingBox = createInterval();
	}

	@Override
	public LocalCursor cursor()
	{
		return new LocalCursor( sourceRandomAccess.copyRandomAccess() );
	}

	@Override
	public LocalCursor localizingCursor()
	{
		return cursor();
	}

	@Override
	public long size()
	{
		return sourceRandomAccess.numDimensions() * 2;
	}

	@Override
	public T firstElement()
	{
		return cursor().next();
	}

	@Override
	public Object iterationOrder()
	{
		return this;
	}

	@Override
	public double realMin( final int d )
	{
		return position[ d ] - radius;
	}

	@Override
	public void realMin( final double[] min )
	{
		for ( int d = 0; d < min.length; d++ )
		{
			min[ d ] = position[ d ] - radius;
		}
	}

	@Override
	public void realMin( final RealPositionable min )
	{
		for ( int d = 0; d < position.length; d++ )
		{
			min.setPosition( position[ d ] - radius, d );
		}
	}

	@Override
	public double realMax( final int d )
	{
		return position[ d ] + radius;
	}

	@Override
	public void realMax( final double[] max )
	{
		for ( int d = 0; d < max.length; d++ )
		{
			max[ d ] = position[ d ] + radius;
		}
	}

	@Override
	public void realMax( final RealPositionable max )
	{
		for ( int d = 0; d < position.length; d++ )
		{
			max.setPosition( position[ d ] + radius, d );
		}
	}

	@Override
	public Iterator< T > iterator()
	{
		return cursor();
	}

	@Override
	public long min( final int d )
	{
		return position[ d ] - radius;
	}

	@Override
	public void min( final long[] min )
	{
		for ( int d = 0; d < min.length; d++ )
		{
			min[ d ] = position[ d ] - radius;
		}
	}

	@Override
	public void min( final Positionable min )
	{
		for ( int d = 0; d < position.length; d++ )
		{
			min.setPosition( position[ d ] - radius, d );
		}
	}

	@Override
	public long max( final int d )
	{
		return position[ d ] + radius;
	}

	@Override
	public void max( final long[] max )
	{
		for ( int d = 0; d < max.length; d++ )
		{
			max[ d ] = position[ d ] + radius;
		}
	}

	@Override
	public void max( final Positionable max )
	{
		for ( int d = 0; d < position.length; d++ )
		{
			max.setPosition( position[ d ] + radius, d );
		}
	}

	@Override
	public void dimensions( final long[] dimensions )
	{
		for ( int d = 0; d < dimensions.length; d++ )
		{
			dimensions[ d ] = 2 * radius + 1;
		}
	}

	@Override
	public long dimension( final int d )
	{
		return 2 * radius + 1;
	}

	@Override
	public Interval getStructuringElementBoundingBox()
	{
		return structuringElementBoundingBox;
	}

	public final class LocalCursor extends AbstractEuclideanSpace implements Cursor< T >
	{
		private final RandomAccess< T > source;

		private int currentDim;

		private boolean parity;

		public LocalCursor( final RandomAccess< T > source )
		{
			super( source.numDimensions() );
			this.source = source;
			reset();
		}

		protected LocalCursor( final LocalCursor c )
		{
			super( c.numDimensions() );
			this.source = c.source.copyRandomAccess();
			this.currentDim = c.currentDim;
			this.parity = c.parity;
		}

		@Override
		public T get()
		{
			return source.get();
		}

		@Override
		public void fwd()
		{
			if ( !parity )
			{
				if ( currentDim >= 0 )
				{
					source.setPosition( position[ currentDim ], currentDim );
				}
				currentDim++;
				source.move( -radius, currentDim );
				parity = true;
			}
			else
			{
				source.move( 2 * radius, currentDim );
				parity = false;

			}
		}

		@Override
		public void jumpFwd( final long steps )
		{
			for ( int i = 0; i < steps; i++ )
			{
				fwd();
			}
		}

		@Override
		public T next()
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
		public void reset()
		{
			source.setPosition( position );
			currentDim = -1;
			parity = false;
		}

		@Override
		public boolean hasNext()
		{
			return currentDim < source.numDimensions() - 1 || parity;
		}

		@Override
		public float getFloatPosition( final int d )
		{
			return source.getFloatPosition( d );
		}

		@Override
		public double getDoublePosition( final int d )
		{
			return source.getDoublePosition( d );
		}

		@Override
		public int getIntPosition( final int d )
		{
			return source.getIntPosition( d );
		}

		@Override
		public long getLongPosition( final int d )
		{
			return source.getLongPosition( d );
		}

		@Override
		public void localize( final long[] position )
		{
			source.localize( position );
		}

		@Override
		public void localize( final float[] position )
		{
			source.localize( position );
		}

		@Override
		public void localize( final double[] position )
		{
			source.localize( position );
		}

		@Override
		public void localize( final int[] position )
		{
			source.localize( position );
		}

		@Override
		public LocalCursor copy()
		{
			return new LocalCursor( this );
		}

		@Override
		public LocalCursor copyCursor()
		{
			return copy();
		}
	}

	private Interval createInterval()
	{
		final long[] minmax = new long[ 2 * position.length ];
		for ( int i = 0; i < position.length; i++ )
		{
			minmax[ i ] = position[ i ] - radius;
		}
		for ( int i = position.length; i < minmax.length; i++ )
		{
			minmax[ i ] = position[ i - position.length ] + radius;
		}
		return Intervals.createMinMax( minmax );
	}
}
