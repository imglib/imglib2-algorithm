
package net.imglib2.algorithm.morphology.neighborhoods;

import java.util.Iterator;

import net.imglib2.AbstractEuclideanSpace;
import net.imglib2.AbstractInterval;
import net.imglib2.Cursor;
import net.imglib2.FlatIterationOrder;
import net.imglib2.Interval;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.region.localneighborhood.Neighborhood;
import net.imglib2.algorithm.region.localneighborhood.Shape;

public class DiamondShape implements Shape
{

	private final long radius;

	public DiamondShape( final long radius )
	{
		this.radius = radius;
	}

	@Override
	public < T > IterableInterval< Neighborhood< T >> neighborhoods( final RandomAccessibleInterval< T > source )
	{
		return new NeighborhoodsIterableInterval< T >( source, radius, DiamondNeighborhoodUnsafe.< T >factory() );
	}

	@Override
	public < T > NeighborhoodsAccessible< T > neighborhoodsRandomAccessible( final RandomAccessible< T > source )
	{
		final DiamondNeighborhoodFactory< T > f = DiamondNeighborhoodUnsafe.< T >factory();
		return new NeighborhoodsAccessible< T >( source, radius, f );
	}

	@Override
	public < T > NeighborhoodsIterableInterval< T > neighborhoodsSafe( final RandomAccessibleInterval< T > source )
	{
		return new NeighborhoodsIterableInterval< T >( source, radius, DiamondNeighborhood.< T >factory() );
	}

	@Override
	public < T > NeighborhoodsAccessible< T > neighborhoodsRandomAccessibleSafe( final RandomAccessible< T > source )
	{
		return new NeighborhoodsAccessible< T >( source, radius, DiamondNeighborhood.< T >factory() );
	}

	public static final class NeighborhoodsIterableInterval< T > extends AbstractInterval implements IterableInterval< Neighborhood< T > >
	{
		final RandomAccessibleInterval< T > source;

		final DiamondNeighborhoodFactory< T > factory;

		final long size;

		private final long radius;

		public NeighborhoodsIterableInterval( final RandomAccessibleInterval< T > source, final long radius, final DiamondNeighborhoodFactory< T > factory )
		{
			super( source );
			this.source = source;
			this.radius = radius;
			this.factory = factory;
			long s = source.dimension( 0 );
			for ( int d = 1; d < n; ++d )
			{
				s *= source.dimension( d );
			}
			size = s;
		}

		@Override
		public Cursor< Neighborhood< T >> cursor()
		{
			return new DiamondNeighborhoodCursor< T >( source, radius, factory );
		}

		@Override
		public long size()
		{
			return size;
		}

		@Override
		public Neighborhood< T > firstElement()
		{
			return cursor().next();
		}

		@Override
		public Object iterationOrder()
		{
			return new FlatIterationOrder( this );
		}

		@Override
		public Cursor< Neighborhood< T >> localizingCursor()
		{
			return cursor();
		}

		@Override
		public Iterator< Neighborhood< T >> iterator()
		{
			return cursor();
		}
	}

	public static final class NeighborhoodsAccessible< T > extends AbstractEuclideanSpace implements RandomAccessible< Neighborhood< T > >
	{
		final RandomAccessible< T > source;

		final DiamondNeighborhoodFactory< T > factory;

		private final long radius;

		public NeighborhoodsAccessible( final RandomAccessible< T > source, final long radius, final DiamondNeighborhoodFactory< T > factory )
		{
			super( source.numDimensions() );
			this.source = source;
			this.radius = radius;
			this.factory = factory;
		}

		@Override
		public RandomAccess< Neighborhood< T >> randomAccess()
		{
			return new DiamondNeighborhoodRandomAccess< T >( source, radius, factory );
		}

		@Override
		public RandomAccess< Neighborhood< T >> randomAccess( final Interval interval )
		{
			return randomAccess();
		}

		@Override
		public int numDimensions()
		{
			return source.numDimensions();
		}

	}
}
