/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2016 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
 * John Bogovic, Albert Cardona, Barry DeZonia, Christian Dietz, Jan Funke,
 * Aivar Grislis, Jonathan Hale, Grant Harris, Stefan Helfrich, Mark Hiner,
 * Martin Horn, Steffen Jaensch, Lee Kamentsky, Larry Lindsey, Melissa Linkert,
 * Mark Longair, Brian Northan, Nick Perry, Curtis Rueden, Johannes Schindelin,
 * Jean-Yves Tinevez and Michael Zinsmaier.
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
 * #L%
 */

package net.imglib2.algorithm.labeling;

import java.util.function.LongFunction;
import java.util.function.LongUnaryOperator;
import java.util.function.ToLongBiFunction;

import gnu.trove.map.hash.TLongLongHashMap;
import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.Localizable;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.DiamondShape;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleNeighborhood;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.algorithm.util.unionfind.IntArrayRankedUnionFind;
import net.imglib2.algorithm.util.unionfind.UnionFind;
import net.imglib2.type.BooleanType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.util.IntervalIndexer;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

/**
 *
 * @author Philipp Hanslovsky
 *
 */
public class ConnectedComponentAnalysis
{

	private static class StartAtOneIdForNextSet implements LongUnaryOperator
	{

		private final TLongLongHashMap setMappings = new TLongLongHashMap();

		@Override
		public long applyAsLong( final long root )
		{

			if ( !setMappings.containsKey( root ) )
			{
				setMappings.put( root, setMappings.size() + 1 );
			}
			return setMappings.get( root );
		}

	}

	private static final class IdFromIntervalIndexerWithInterval< T > implements ToLongBiFunction< Localizable, T >
	{

		private final Interval interval;

		private IdFromIntervalIndexerWithInterval( final Interval interval )
		{
			this.interval = interval;
		}

		@Override
		public long applyAsLong( final Localizable l, final T t )
		{
			return IntervalIndexer.positionToIndexForInterval( l, interval );
		}
	}

	public static < T > ToLongBiFunction< Localizable, T > idFromIntervalIndexer( final Interval interval )
	{
		return new IdFromIntervalIndexerWithInterval<>( interval );
	}

	/**
	 *
	 * Implementation of connected component analysis that uses
	 * {@link IntArrayRankedUnionFind} to find sets of pixels that are connected
	 * with respect to a 4-neighborhood ({@link DiamondShape}) or the
	 * generalization for higher dimenions over a binary mask. {@code mask} and
	 * {@code labeling} are expected to have equal min and max.
	 *
	 * @param mask
	 *            Boolean mask to distinguish foreground ({@code true}) from
	 *            background ({@code false}).
	 * @param labeling
	 *            Output parameter to store labeling: background pixels are
	 *            labeled zero, foreground pixels are greater than zero: 1, 2,
	 *            ..., N. Note that initially all pixels are expected to be zero
	 *            as background values will not be written.
	 */
	public static < B extends BooleanType< B >, L extends IntegerType< L > > void connectedComponents(
			final RandomAccessibleInterval< B > mask,
			final RandomAccessibleInterval< L > labeling )
	{
		connectedComponents( mask, labeling, new DiamondShape( 1 ) );
	}

	/**
	 *
	 * Implementation of connected component analysis that uses
	 * {@link IntArrayRankedUnionFind} to find sets of pixels that are connected
	 * with respect to a neighborhood ({@code shape}) over a binary mask. {@code mask}
	 * and {@code labeling} are expected to have equal min and max.
	 *
	 * @param mask
	 *            Boolean mask to distinguish foreground ({@code true}) from
	 *            background ({@code false}).
	 * @param labeling
	 *            Output parameter to store labeling: background pixels are
	 *            labeled zero, foreground pixels are greater than zero: 1, 2,
	 *            ..., N. Note that initially all pixels are expected to be zero
	 *            as background values will not be written.
	 * @param shape
	 *            Connectivity of connected components, e.g. 4-neighborhood
	 *            ({@link DiamondShape}), 8-neighborhood
	 *            ({@link RectangleNeighborhood}) and their generalisations for
	 *            higher dimensions.
	 */
	public static < B extends BooleanType< B >, L extends IntegerType< L > > void connectedComponents(
			final RandomAccessibleInterval< B > mask,
			final RandomAccessibleInterval< L > labeling,
			final Shape shape )
	{
		assert Intervals.numElements( labeling ) < Integer.MAX_VALUE: "Cannot Image Using array union find.";
		connectedComponents(
				mask,
				labeling,
				shape,
				n -> new IntArrayRankedUnionFind( ( int ) n ),
				idFromIntervalIndexer( labeling ),
				new StartAtOneIdForNextSet() );
	}

	/**
	 *
	 * Implementation of connected component analysis that uses
	 * {@link UnionFind} to find sets of pixels that are connected with respect
	 * to a neighborhood ({@code shape}) over a binary mask. {@code mask} and
	 * {@code labeling} are expected to have equal min and max.
	 *
	 * @param mask
	 *            Boolean mask to distinguish foreground ({@code true}) from
	 *            background ({@code false}).
	 * @param labeling
	 *            Output parameter to store labeling: background pixels are
	 *            labeled zero, foreground pixels are greater than zero: 1, 2,
	 *            ..., N. Note that this is expected to be zero as background
	 *            values will not be written.
	 * @param shape
	 *            Connectivity of connected components, e.g. 4-neighborhood
	 *            ({@link DiamondShape}), 8-neighborhood
	 *            ({@link RectangleNeighborhood}) and their generalisations for
	 *            higher dimensions.
	 * @param unionFindFactory
	 *            Creates appropriate {@link UnionFind} data structure for size
	 *            of {@code labeling}, e.g. {@link IntArrayRankedUnionFind} of
	 *            appropriate size.
	 * @param idForPixel
	 *            Create id from pixel location and value. Multiple calls with
	 *            the same argument should always return the same result.
	 * @param idForSet
	 *            Create id for a set from the root id of a set. Multiple calls
	 *            with the same argument should always return the same result.
	 */
	public static < B extends BooleanType< B >, L extends IntegerType< L > > void connectedComponents(
			final RandomAccessibleInterval< B > mask,
			final RandomAccessibleInterval< L > labeling,
			final Shape shape,
			final LongFunction< UnionFind > unionFindFactory,
			final ToLongBiFunction< Localizable, L > idForPixel,
			final LongUnaryOperator idForSet )
	{
		assert Intervals.contains( mask, labeling ) && Intervals.contains( labeling, mask ): "Mask and labeling are not the same size.";
		assert Intervals.numElements( labeling ) <= Integer.MAX_VALUE: "Too many pixels for integer based union find.";

		final UnionFind uf = makeUnion( mask, labeling, shape, unionFindFactory, idForPixel );
		UnionFind.relabel( mask, labeling, uf, idForPixel, idForSet );
	}

	private static < B extends BooleanType< B >, L extends IntegerType< L > > UnionFind makeUnion(
			final RandomAccessibleInterval< B > mask,
			final RandomAccessibleInterval< L > labeling,
			final Shape shape,
			final LongFunction< UnionFind > unionFindFactory,
			final ToLongBiFunction< Localizable, L > idForPixel )
	{
		final UnionFind uf = unionFindFactory.apply( Intervals.numElements( labeling ) );
		if ( shape instanceof DiamondShape && ( ( DiamondShape ) shape ).getRadius() == 1 )
		{
			connectedComponentsDiamondShape( mask, labeling, uf, idForPixel );
		}
		else
		{
			connectedComponentsGeneralShape( mask, labeling, shape, uf, idForPixel );
		}
		return uf;
	}

	private static < B extends BooleanType< B >, L extends IntegerType< L > > void connectedComponentsDiamondShape(
			final RandomAccessible< B > mask,
			final RandomAccessibleInterval< L > labeling,
			final UnionFind uf,
			final ToLongBiFunction< Localizable, L > id )
	{
		final int nDim = labeling.numDimensions();
		final long[] min = Intervals.minAsLongArray( labeling );
		final long[] max = Intervals.maxAsLongArray( labeling );

		for ( int d = 0; d < nDim; ++d )
		{
			final long[] minPlusOne = min.clone();
			final long[] maxMinusOne = max.clone();
			minPlusOne[ d ] += 1;
			maxMinusOne[ d ] -= 1;
			final Cursor< B > lower = Views.interval( mask, new FinalInterval( min, maxMinusOne ) ).cursor();
			final Cursor< B > upper = Views.interval( mask, new FinalInterval( minPlusOne, max ) ).cursor();
			final Cursor< L > lowerL = Views.interval( labeling, new FinalInterval( min, maxMinusOne ) ).localizingCursor();
			final Cursor< L > upperL = Views.interval( labeling, new FinalInterval( minPlusOne, max ) ).localizingCursor();

			while ( lower.hasNext() )
			{
				final boolean l = lower.next().get();
				final boolean u = upper.next().get();
				lowerL.fwd();
				upperL.fwd();
				if ( l && u )
				{
					final long r1 = uf.findRoot( id.applyAsLong( lowerL, lowerL.get() ) );
					final long r2 = uf.findRoot( id.applyAsLong( upperL, upperL.get() ) );
					if ( r1 != r2 )
					{
						uf.join( r1, r2 );
					}
				}
			}
		}
	}

	private static < B extends BooleanType< B >, L extends IntegerType< L > > void connectedComponentsGeneralShape(
			final RandomAccessibleInterval< B > mask,
			final RandomAccessibleInterval< L > labeling,
			final Shape shape,
			final UnionFind uf,
			final ToLongBiFunction< Localizable, L > id )
	{

		final RandomAccessible< Neighborhood< B > > maskNeighborhood = shape.neighborhoodsRandomAccessible( Views.extendZero( mask ) );
		final RandomAccessible< Neighborhood< L > > labelingNeighborhood = shape.neighborhoodsRandomAccessible( Views.extendZero( labeling ) );

		final Cursor< Neighborhood< B > > mnhCursor = Views.flatIterable( Views.interval( maskNeighborhood, labeling ) ).cursor();
		final Cursor< Neighborhood< L > > lnhCursor = Views.flatIterable( Views.interval( labelingNeighborhood, labeling ) ).cursor();
		final Cursor< B > maskCursor = Views.flatIterable( mask ).cursor();
		final Cursor< L > labelCursor = Views.flatIterable( labeling ).localizingCursor();

		while ( maskCursor.hasNext() )
		{
			final B center = maskCursor.next();
			final L label = labelCursor.next();
			final Neighborhood< B > mnh = mnhCursor.next();
			final Neighborhood< L > lnh = lnhCursor.next();
			if ( center.get() )
			{
				final long index = id.applyAsLong( labelCursor, label );
				final Cursor< L > lnhc = lnh.localizingCursor();
				final Cursor< B > mnhc = mnh.cursor();
				while ( mnhc.hasNext() )
				{
					final L l = lnhc.next();
					final B m = mnhc.next();
					if ( m.get() )
					{
						final long r1 = uf.findRoot( index );
						final long r2 = uf.findRoot( id.applyAsLong( lnhc, l ) );
						if ( r1 != r2 )
						{
							uf.join( r1, r2 );
						}
					}
				}
			}
		}

	}

}
