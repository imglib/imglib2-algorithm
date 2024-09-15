package net.imglib2.algorithm.blocks;

import net.imglib2.Interval;

/**
 * Wraps {@code long[] pos} and {@code int[] size} as a {@code Interval}, for
 * the default implementation of {@link BlockProcessor#setTargetInterval(long[],
 * int[])}.
 */
class BlockProcessorTargetInterval implements Interval
{
	private final long[] min;

	private final int[] size;

	BlockProcessorTargetInterval( long[] min, int[] size )
	{
		this.min = min;
		this.size = size;
	}

	@Override
	public long min( final int i )
	{
		return min[ i ];
	}

	@Override
	public long max( final int i )
	{
		return min[ i ] + size[ i ] - 1;
	}

	@Override
	public long dimension( final int d )
	{
		return size[ d ];
	}

	@Override
	public int numDimensions()
	{
		return min.length;
	}
}
