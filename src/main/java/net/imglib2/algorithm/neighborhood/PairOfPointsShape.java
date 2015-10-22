package net.imglib2.algorithm.neighborhood;

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
import net.imglib2.util.Util;

/**
 * A {@link Shape} representing a pair of points.
 * <p>
 * The Shape as its origin at the first point, and the second one is simply
 * found by adding the value of the <code>offset</code> array to its position.
 *
 * @author Jean-Yves Tinevez, 2013
 */
public class PairOfPointsShape implements Shape
{
	private final long[] offset;

	/**
	 * Create a new pair of points shape.
	 * <p>
	 *
	 * @param offset
	 *            the offset of the second point with respect to the origin, as
	 *            a <code>long[]</code> array.
	 */
	public PairOfPointsShape( final long[] offset )
	{
		this.offset = offset;
	}

	@Override
	public < T > NeighborhoodsIterableInterval< T > neighborhoods( final RandomAccessibleInterval< T > source )
	{
		final PairOfPointsNeighborhoodFactory< T > f = PairOfPointsNeighborhoodUnsafe.< T >factory();
		return new NeighborhoodsIterableInterval< T >( source, offset, f );
	}

	@Override
	public < T > NeighborhoodsAccessible< T > neighborhoodsRandomAccessible( final RandomAccessible< T > source )
	{
		final PairOfPointsNeighborhoodFactory< T > f = PairOfPointsNeighborhoodUnsafe.< T >factory();
		return new NeighborhoodsAccessible< T >( source, offset, f );
	}

	@Override
	public < T > IterableInterval< Neighborhood< T >> neighborhoodsSafe( final RandomAccessibleInterval< T > source )
	{
		final PairOfPointsNeighborhoodFactory< T > f = PairOfPointsNeighborhood.< T >factory();
		return new NeighborhoodsIterableInterval< T >( source, offset, f );
	}

	@Override
	public < T > NeighborhoodsAccessible< T > neighborhoodsRandomAccessibleSafe( final RandomAccessible< T > source )
	{
		final PairOfPointsNeighborhoodFactory< T > f = PairOfPointsNeighborhood.< T >factory();
		return new NeighborhoodsAccessible< T >( source, offset, f );
	}

	/**
	 * @return Copy of the offset of this shape.
	 */
	public long[] getOffset()
	{
		return offset.clone();
	}

	@Override
	public String toString()
	{
		return "PairOfPointsShape, offset = " + Util.printCoordinates( offset );
	}

	public static final class NeighborhoodsIterableInterval< T > extends AbstractInterval implements IterableInterval< Neighborhood< T > >
	{
		final RandomAccessibleInterval< T > source;

		final PairOfPointsNeighborhoodFactory< T > factory;

		final long[] offset;

		public NeighborhoodsIterableInterval( final RandomAccessibleInterval< T > source, final long[] offset, final PairOfPointsNeighborhoodFactory< T > factory )
		{
			super( source );
			this.source = source;
			this.offset = offset;
			this.factory = factory;
		}

		@Override
		public Cursor< Neighborhood< T >> cursor()
		{
			return new PairOfPointsNeighborhoodCursor< T >( source, offset, factory );
		}

		@Override
		public long size()
		{
			return 2;
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
		public Iterator< Neighborhood< T >> iterator()
		{
			return cursor();
		}

		@Override
		public Cursor< Neighborhood< T >> localizingCursor()
		{
			return cursor();
		}
	}

	public static final class NeighborhoodsAccessible< T > extends AbstractEuclideanSpace implements RandomAccessible< Neighborhood< T > >
	{
		final RandomAccessible< T > source;

		final PairOfPointsNeighborhoodFactory< T > factory;

		private final long[] offset;

		public NeighborhoodsAccessible( final RandomAccessible< T > source, final long[] offset, final PairOfPointsNeighborhoodFactory< T > factory )
		{
			super( source.numDimensions() );
			this.source = source;
			this.offset = offset;
			this.factory = factory;
		}

		@Override
		public RandomAccess< Neighborhood< T >> randomAccess()
		{
			return new PairOfPointsNeighborhoodRandomAccess< T >( source, offset, factory );
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
