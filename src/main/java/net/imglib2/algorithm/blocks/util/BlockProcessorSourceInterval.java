package net.imglib2.algorithm.blocks.util;

import net.imglib2.Interval;
import net.imglib2.algorithm.blocks.BlockProcessor;

/**
 * Helper class that wraps {@link BlockProcessor#getSourcePos()} and {@link
 * BlockProcessor#getSourceSize()} as an {@code Interval}.
 */
public class BlockProcessorSourceInterval implements Interval
{
	private BlockProcessor< ?, ? > p;

	public BlockProcessorSourceInterval( BlockProcessor< ?, ? > blockProcessor )
	{
		this.p = blockProcessor;
	}

	@Override
	public int numDimensions()
	{
		return p.getSourcePos().length;
	}

	@Override
	public long min( final int d )
	{
		return p.getSourcePos()[ d ];
	}

	@Override
	public long max( final int d )
	{
		return min( d ) + dimension( d ) - 1;
	}

	@Override
	public long dimension( final int d )
	{
		return p.getSourceSize()[ d ];
	}
}
