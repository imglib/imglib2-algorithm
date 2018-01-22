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

import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TLongLongHashMap;
import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.DiamondShape;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.algorithm.util.UnionFind;
import net.imglib2.type.BooleanType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.util.IntervalIndexer;
import net.imglib2.util.Intervals;
import net.imglib2.util.Pair;
import net.imglib2.view.Views;

/**
 *
 * @author Philipp Hanslovsky
 *
 */
public class ConnectedComponentAnalysis
{

	private static final long NO_ENTRY_KEY = -1;

	private static final long NO_ENTRY_VALUE = -1;

	/**
	 *
	 * Implementation of connected component analysis that uses
	 * {@link UnionFind} to find sets of pixels that are connected with respect
	 * to a {@link DiamondShape} of unit radius over a binary mask.
	 *
	 *
	 * @param mask
	 *            Boolean mask to distinguish foreground ({@code true}) from
	 *            background ({@code false}).
	 * @param labeling
	 *            Output parameter to store labeling: background pixels are
	 *            labeled zero, foreground pixels are greater than zero: 1, 2,
	 *            ..., N. Note that this is expected to be zero as background
	 *            values will not be written.
	 * @return Number of non-background labels: N for non-background connected
	 *         components 1, 2, ..., N.
	 */
	public static < B extends BooleanType< B >, L extends IntegerType< L > > int connectedComponents(
			final RandomAccessibleInterval< B > mask,
			final RandomAccessibleInterval< L > labeling )
	{
		return connectedComponents( mask, labeling, new DiamondShape( 1 ) );
	}

	/**
	 *
	 * Implementation of connected component analysis that uses
	 * {@link UnionFind} to find sets of pixels that are connected with respect
	 * to a neighborhood over a binary mask.
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
	 *            Neighborhood
	 * @return Number of non-background labels: N for non-background connected
	 *         components 1, 2, ..., N.
	 */
	public static < B extends BooleanType< B >, L extends IntegerType< L > > int connectedComponents(
			final RandomAccessibleInterval< B > mask,
			final RandomAccessibleInterval< L > labeling,
			final Shape shape )
	{

		assert Intervals.contains( mask, labeling ) && Intervals.contains( labeling, mask ): "Mask and labeling are not the same size.";
		assert Intervals.numElements( labeling ) <= Integer.MAX_VALUE: "Too many pixels for integer based union find.";

		final UnionFind uf = new UnionFind( ( int ) Intervals.numElements( labeling ) );
		if ( shape instanceof DiamondShape && ( ( DiamondShape ) shape ).getRadius() == 1 )
			connectedComponentsDiamondShape( mask, labeling, uf );
		else
			connectedComponentsGeneralShape( mask, labeling, shape, uf );

		final Cursor< L > label = Views.flatIterable( labeling ).cursor();
		final Cursor< B > maskCursor = Views.flatIterable( Views.interval( mask, labeling ) ).cursor();
		final TLongLongHashMap setMappings = new TLongLongHashMap( Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, NO_ENTRY_KEY, NO_ENTRY_VALUE );
		while ( label.hasNext() )
		{
			final B m = maskCursor.next();
			label.fwd();
			if ( m.get() )
			{
				final long root = uf.findRoot( ( int ) IntervalIndexer.positionToIndexForInterval( label, labeling ) );
				long mapping = setMappings.get( root );
				if ( mapping == NO_ENTRY_VALUE )
				{
					mapping = setMappings.size() + 1;
					setMappings.put( root, mapping );
				}
				label.get().setInteger( mapping );
			}
		}

		return setMappings.size();
	}

	private static < B extends BooleanType< B >, L extends IntegerType< L > > void connectedComponentsDiamondShape(
			final RandomAccessible< B > mask,
			final RandomAccessibleInterval< L > labeling,
			final UnionFind uf )
	{

		assert Intervals.numElements( labeling ) <= Integer.MAX_VALUE: "Too many pixels for integer based union find.";

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

			while ( lower.hasNext() )
			{
				final boolean l = lower.next().get();
				final boolean u = upper.next().get();
				if ( l && u )
				{
					final int r1 = uf.findRoot( ( int ) IntervalIndexer.positionToIndexForInterval( lower, labeling ) );
					final int r2 = uf.findRoot( ( int ) IntervalIndexer.positionToIndexForInterval( upper, labeling ) );
					if ( r1 != r2 )
						uf.join( r1, r2 );
				}
			}
		}
	}

	private static < B extends BooleanType< B >, L extends IntegerType< L > > void connectedComponentsGeneralShape(
			final RandomAccessibleInterval< B > mask,
			final RandomAccessibleInterval< L > labeling,
			final Shape shape,
			final UnionFind uf )
	{

		assert Intervals.numElements( labeling ) <= Integer.MAX_VALUE: "Too many pixels for integer based union find.";

		final RandomAccessible< Neighborhood< B > > maskNeighborhood = shape.neighborhoodsRandomAccessible( Views.extendZero( mask ) );
		for ( final Cursor< Pair< B, Neighborhood< B > > > maskCursor = Views.interval( Views.pair( mask, maskNeighborhood ), labeling ).cursor(); maskCursor.hasNext(); )
		{
			final Pair< B, Neighborhood< B > > p = maskCursor.next();
			final boolean center = p.getA().get();
			if ( center )
			{
				final int index = ( int ) IntervalIndexer.positionToIndexForInterval( maskCursor, labeling );
				final Neighborhood< B > neighborhood = p.getB();
				for ( final Cursor< B > nc = neighborhood.cursor(); nc.hasNext(); )
					if ( nc.next().get() )
					{
						final int r1 = uf.findRoot( index );
						final int r2 = uf.findRoot( ( int ) IntervalIndexer.positionToIndexForInterval( nc, labeling ) );
						if ( r1 != r2 )
							uf.join( r1, r2 );
					}
			}
		}

	}

}
