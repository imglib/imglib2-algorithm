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

package net.imglib2.algorithm.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.util.Intervals;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;

/**
 *
 * @author Philipp Hanslovsky
 *
 */
public class Grids
{
	/**
	 *
	 * Get all blocks of size {@code blockSize} contained within an interval
	 * specified by {@code dimensions} and their positions within a cell grid.
	 *
	 * @param dimensions
	 * @param blockSize
	 * @return list of blocks as specified by {@link Interval} and the position
	 *         within a cell grid.
	 */
	public static List< Pair< Interval, long[] > > collectAllContainedIntervalsWithGridPositions( final long[] dimensions, final int[] blockSize )
	{
		return collectAllOffsets( dimensions, blockSize, croppedIntervalAndGridPosition( dimensions, blockSize ) );
	}

	/**
	 *
	 * Get all blocks of size {@code blockSize} contained within an interval
	 * specified by {@code min}, {@code max} and their positions within a cell
	 * grid.
	 *
	 * @param min
	 * @param max
	 * @param blockSize
	 * @return list of blocks as specified by {@link Interval} and the position
	 *         within a cell grid.
	 */
	public static List< Pair< Interval, long[] > > collectAllContainedIntervalsWithGridPositions( final long[] min, final long[] max, final int[] blockSize )
	{
		return collectAllOffsets( min, max, blockSize, croppedIntervalAndGridPosition( min, max, blockSize ) );
	}

	/**
	 *
	 * Get all blocks of size {@code blockSize} contained within an interval
	 * specified by {@code dimensions} and their positions within a cell grid.
	 *
	 * @param dimensions
	 * @param blockSize
	 * @return list of blocks as specified by {@link Interval}
	 */
	public static List< Interval > collectAllContainedIntervals( final long[] dimensions, final int[] blockSize )
	{
		return collectAllOffsets( dimensions, blockSize, new CreateAndCropBlockToFitInterval( blockSize, new FinalInterval( dimensions ) ) );
	}

	/**
	 *
	 * Get all blocks of size {@code blockSize} contained within an interval
	 * specified by {@code min}, {@code max} and their positions within a cell
	 * grid.
	 *
	 * @param min
	 * @param max
	 * @param blockSize
	 * @return list of blocks as specified by {@link Interval}
	 */
	public static List< Interval > collectAllContainedIntervals( final long[] min, final long[] max, final int[] blockSize )
	{
		return collectAllOffsets( min, max, blockSize, new CreateAndCropBlockToFitInterval( blockSize, max ) );
	}

	/**
	 *
	 * Get all blocks of size {@code blockSize} contained within an interval
	 * specified by {@code dimensions}.
	 *
	 * @param dimensions
	 * @param blockSize
	 * @return list of blocks defined by minimum
	 */
	public static List< long[] > collectAllOffsets( final long[] dimensions, final int[] blockSize )
	{
		return collectAllOffsets( dimensions, blockSize, block -> block );
	}

	/**
	 *
	 * Get all blocks of size {@code blockSize} contained within an interval
	 * specified by {@code dimensions}.
	 *
	 * @param dimensions
	 * @param blockSize
	 * @param func
	 *            Apply this function to each block, e.g. create a
	 *            {@link Interval} for each block.
	 * @return list of blocks mapped by {@code funk}
	 */
	public static < T > List< T > collectAllOffsets( final long[] dimensions, final int[] blockSize, final Function< long[], T > func )
	{
		return collectAllOffsets( new long[ dimensions.length ], Arrays.stream( dimensions ).map( d -> d - 1 ).toArray(), blockSize, func );
	}

	/**
	 *
	 * Get all blocks of size {@code blockSize} contained within an interval
	 * specified by {@code min} and {@code max}.
	 *
	 * @param min
	 * @param max
	 * @param blockSize
	 * @return list of blocks defined by minimum
	 */
	public static List< long[] > collectAllOffsets( final long[] min, final long[] max, final int[] blockSize )
	{
		return collectAllOffsets( min, max, blockSize, block -> block );
	}

	/**
	 *
	 * Get all blocks of size {@code blockSize} contained within an interval
	 * specified by {@code min} and {@code max}.
	 *
	 * @param min
	 * @param max
	 * @param blockSize
	 * @param func
	 *            Apply this function to each block, e.g. create a
	 *            {@link Interval} for each block.
	 * @return list of blocks mapped by {@code funk}
	 */
	public static < T > List< T > collectAllOffsets( final long[] min, final long[] max, final int[] blockSize, final Function< long[], T > func )
	{
		final List< T > blocks = new ArrayList<>();
		final int nDim = min.length;
		final long[] offset = min.clone();
		for ( int d = 0; d < nDim; )
		{
			final long[] target = offset.clone();
			blocks.add( func.apply( target ) );
			for ( d = 0; d < nDim; ++d )
			{
				offset[ d ] += blockSize[ d ];
				if ( offset[ d ] <= max[ d ] )
					break;
				else
					offset[ d ] = min[ d ];
			}
		}
		return blocks;
	}

	/**
	 *
	 * @author Philipp Hanslovsky
	 *
	 *         Map the minimum of block to an interval specified by said minimum
	 *         and a block size such that the maximum of the interval is no
	 *         larger than {@code max} for any dimensions.
	 *
	 */
	public static class CreateAndCropBlockToFitInterval implements Function< long[], Interval >
	{

		private final int[] blockSize;

		private final long[] max;

		/**
		 *
		 * @param blockSize
		 *            Regular size for intervals.
		 * @param max
		 *            Upper bound for intervals.
		 */
		public CreateAndCropBlockToFitInterval( final int[] blockSize, final long[] max )
		{
			super();
			this.blockSize = blockSize;
			this.max = max;
		}

		/**
		 * Convenience constructor for {@link Interval}. Delegates to
		 * {@link CreateAndCropBlockToFitInterval#CreateAndCropBlockToFitInterval(int[], long[])}
		 * using {@code max = Intervals.maxAsLongArray( interval )}.
		 *
		 * @param blockSize
		 * @param interval
		 */
		public CreateAndCropBlockToFitInterval( final int[] blockSize, final Interval interval )
		{
			this( blockSize, Intervals.maxAsLongArray( interval ) );
		}

		@Override
		public Interval apply( final long[] min )
		{
			final long[] max = new long[ min.length ];
			Arrays.setAll( max, d -> Math.min( min[ d ] + this.blockSize[ d ] - 1, this.max[ d ] ) );
			return new FinalInterval( min, max );
		}

	}

	/**
	 *
	 * @author Philipp Hanslovsky
	 *
	 *         Map the minimum of block to its position within a grid, specified
	 *         by the minimum of the grid, and the block size
	 *
	 */
	public static class GetGridCoordinates implements Function< long[], long[] >
	{

		private final int[] blockSize;

		private final long[] gridMin;

		/**
		 *
		 * @param blockSize
		 *            Regular size for intervals.
		 * @param min
		 *            minimum of the grid
		 */
		public GetGridCoordinates( final int[] blockSize, final long[] min )
		{
			super();
			this.blockSize = blockSize;
			this.gridMin = min;
		}

		/**
		 * Convenience constructor for {@link Interval}. Delegates to
		 * {@link CreateAndCropBlockToFitInterval#CreateAndCropBlockToFitInterval(int[], long[])}
		 * using {@code min = Intervals.minAsLongArray( interval )}.
		 *
		 * @param blockSize
		 * @param interval
		 */
		public GetGridCoordinates( final int[] blockSize, final Interval interval )
		{
			this( blockSize, Intervals.minAsLongArray( interval ) );
		}

		/**
		 * Convenience constructor for zero min grid. Delegates to
		 * {@link CreateAndCropBlockToFitInterval#CreateAndCropBlockToFitInterval(int[], long[])}
		 * using {@code min = 0}.
		 *
		 * @param blockSize
		 * @param interval
		 */
		public GetGridCoordinates( final int[] blockSize )
		{
			this( blockSize, new long[ blockSize.length ] );
		}

		@Override
		public long[] apply( final long[] min )
		{
			final long[] gridPosition = new long[ min.length ];
			Arrays.setAll( gridPosition, d -> ( min[ d ] - this.gridMin[ d ] ) / this.blockSize[ d ] );
			return gridPosition;
		}
	}

	public static Function< long[], Pair< Interval, long[] > > croppedIntervalAndGridPosition(
			final long[] dimensions,
			final int[] blockSize )
	{
		final CreateAndCropBlockToFitInterval makeInterval = new CreateAndCropBlockToFitInterval( blockSize, new FinalInterval( dimensions ) );
		final GetGridCoordinates getGridCoordinates = new GetGridCoordinates( blockSize );
		return blockMinimum -> new ValuePair<>( makeInterval.apply( blockMinimum ), getGridCoordinates.apply( blockMinimum ) );
	}

	/**
	 * Convenience method to create {@link Function} that maps a block minimum
	 * into a {@link Pair} of {@link Interval} and {@link long[]} that specify
	 * the block and its position in grid coordinates.
	 *
	 * @param min
	 *            minimum of the grid
	 * @param max
	 *            maximum of the grid
	 * @param blockSize
	 *            regular size of blocks in the grid
	 * @return {@link Pair} of {@link Interval} and {@link long[]} that specify
	 *         the block and its position in grid coordinates. Blocks are
	 *         cropped
	 */
	public static Function< long[], Pair< Interval, long[] > > croppedIntervalAndGridPosition(
			final long[] min,
			final long[] max,
			final int[] blockSize )
	{
		final CreateAndCropBlockToFitInterval makeInterval = new CreateAndCropBlockToFitInterval( blockSize, max );
		final GetGridCoordinates getGridCoordinates = new GetGridCoordinates( blockSize, min );
		return blockMinimum -> new ValuePair<>( makeInterval.apply( blockMinimum ), getGridCoordinates.apply( blockMinimum ) );
	}

}
