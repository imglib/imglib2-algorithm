/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2013 Stephan Preibisch, Tobias Pietzsch, Barry DeZonia,
 * Stephan Saalfeld, Albert Cardona, Curtis Rueden, Christian Dietz, Jean-Yves
 * Tinevez, Johannes Schindelin, Lee Kamentsky, Larry Lindsey, Grant Harris,
 * Mark Hiner, Aivar Grislis, Martin Horn, Nick Perry, Michael Zinsmaier,
 * Steffen Jaensch, Jan Funke, Mark Longair, and Dimiter Prodanov.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package net.imglib2.algorithm.morphology.neighborhoods;

import java.util.Iterator;

import net.imglib2.AbstractEuclideanSpace;
import net.imglib2.AbstractInterval;
import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.FlatIterationOrder;
import net.imglib2.Interval;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.region.localneighborhood.Neighborhood;
import net.imglib2.algorithm.region.localneighborhood.RectangleNeighborhood;
import net.imglib2.algorithm.region.localneighborhood.RectangleNeighborhoodCursor;
import net.imglib2.algorithm.region.localneighborhood.RectangleNeighborhoodFactory;
import net.imglib2.algorithm.region.localneighborhood.RectangleNeighborhoodRandomAccess;
import net.imglib2.algorithm.region.localneighborhood.RectangleNeighborhoodSkipCenter;
import net.imglib2.algorithm.region.localneighborhood.RectangleNeighborhoodSkipCenterUnsafe;
import net.imglib2.algorithm.region.localneighborhood.RectangleNeighborhoodUnsafe;
import net.imglib2.algorithm.region.localneighborhood.RectangleShape;
import net.imglib2.algorithm.region.localneighborhood.Shape;
import net.imglib2.util.Util;

/**
 * A factory for Accessibles on rectangular neighborhoods.
 * <p>
 * This specific factory differs to {@link RectangleShape} in that it allows
 * non-isotropic rectangular shapes. However, it constrains the the neighborhood
 * to be symmetric by its origin.
 * <p>
 * The size of the neighborhood is specified by an <code>int[]</code> span
 * array, so that in every dimension <code>d</code>, the extent of the
 * neighborhood is given by <code>2 × span[d] + 1</code>.
 * <p>
 * This factory exists because {@link RectangleShape} which is based on the same
 * components, only allows for square neighborhoods.
 * 
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 * @author Jean-Yves Tinevez <jeanyves.tinevez@gmail.com>
 */
public class CenteredRectangleShape implements Shape
{
	final int[] span;

	final boolean skipCenter;

	/**
	 * Constructs a factory for symmetric, non-isotropic rectangle
	 * neighborhoods.
	 * <p>
	 * The size of the neighborhood is specified by an <code>int[]</code> span
	 * array, so that in every dimension <code>d</code>, the extent of the
	 * neighborhood is given by <code>2 × span[d] + 1</code>.
	 * 
	 * @param span
	 *            the span of the neighborhood.
	 * @param skipCenter
	 *            whether we should skip the central pixel or not.
	 */
	public CenteredRectangleShape( final int[] span, final boolean skipCenter )
	{
		this.span = span;
		this.skipCenter = skipCenter;
	}

	@Override
	public < T > NeighborhoodsIterableInterval< T > neighborhoods( final RandomAccessibleInterval< T > source )
	{
		final RectangleNeighborhoodFactory< T > f = skipCenter ? RectangleNeighborhoodSkipCenterUnsafe.< T >factory() : RectangleNeighborhoodUnsafe.< T >factory();
		final Interval spanInterval = createSpan();
		return new NeighborhoodsIterableInterval< T >( source, spanInterval, f );
	}

	@Override
	public < T > NeighborhoodsAccessible< T > neighborhoodsRandomAccessible( final RandomAccessible< T > source )
	{
		final RectangleNeighborhoodFactory< T > f = skipCenter ? RectangleNeighborhoodSkipCenterUnsafe.< T >factory() : RectangleNeighborhoodUnsafe.< T >factory();
		final Interval spanInterval = createSpan();
		return new NeighborhoodsAccessible< T >( source, spanInterval, f );
	}

	@Override
	public < T > NeighborhoodsIterableInterval< T > neighborhoodsSafe( final RandomAccessibleInterval< T > source )
	{
		final RectangleNeighborhoodFactory< T > f = skipCenter ? RectangleNeighborhoodSkipCenter.< T >factory() : RectangleNeighborhood.< T >factory();
		final Interval spanInterval = createSpan();
		return new NeighborhoodsIterableInterval< T >( source, spanInterval, f );
	}

	@Override
	public < T > NeighborhoodsAccessible< T > neighborhoodsRandomAccessibleSafe( final RandomAccessible< T > source )
	{
		final RectangleNeighborhoodFactory< T > f = skipCenter ? RectangleNeighborhoodSkipCenter.< T >factory() : RectangleNeighborhood.< T >factory();
		final Interval spanInterval = createSpan();
		return new NeighborhoodsAccessible< T >( source, spanInterval, f );
	}

	@Override
	public String toString()
	{
		return "CenteredRectangleShape, span = " + Util.printCoordinates( span );
	}

	private Interval createSpan()
	{
		final long[] min = new long[ span.length ];
		final long[] max = new long[ span.length ];
		for ( int d = 0; d < span.length; ++d )
		{
			min[ d ] = -span[ d ];
			max[ d ] = span[ d ];
		}
		return new FinalInterval( min, max );
	}

	public static final class NeighborhoodsIterableInterval< T > extends AbstractInterval implements IterableInterval< Neighborhood< T > >
	{
		final RandomAccessibleInterval< T > source;

		final Interval spanInterval;

		final RectangleNeighborhoodFactory< T > factory;

		final long size;

		public NeighborhoodsIterableInterval( final RandomAccessibleInterval< T > source, final Interval span, final RectangleNeighborhoodFactory< T > factory )
		{
			super( source );
			this.source = source;
			this.spanInterval = span;
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
			return new RectangleNeighborhoodCursor< T >( source, spanInterval, factory );
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

		final RectangleNeighborhoodFactory< T > factory;

		final Interval spanInterval;

		public NeighborhoodsAccessible( final RandomAccessible< T > source, final Interval span, final RectangleNeighborhoodFactory< T > factory )
		{
			super( source.numDimensions() );
			this.source = source;
			this.spanInterval = span;
			this.factory = factory;
		}

		@Override
		public RandomAccess< Neighborhood< T >> randomAccess()
		{
			return new RectangleNeighborhoodRandomAccess< T >( source, spanInterval, factory );
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
