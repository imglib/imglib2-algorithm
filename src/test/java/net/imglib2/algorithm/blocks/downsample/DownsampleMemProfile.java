/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2025 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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
package net.imglib2.algorithm.blocks.downsample;

import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.blocks.BlockAlgoUtils;
import net.imglib2.algorithm.blocks.BlockSupplier;
import net.imglib2.algorithm.blocks.ComputationType;
import net.imglib2.algorithm.blocks.downsample.Downsample.Offset;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.cell.AbstractCellImg;
import net.imglib2.parallel.Parallelization;
import net.imglib2.parallel.TaskExecutor;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.view.Views;

public class DownsampleMemProfile
{
	final RandomAccessibleInterval< UnsignedByteType > raw;
	final boolean[] downsampleInDim = { true, true, true };
	final int[] cellDimensions = { 64, 64, 64 };
//	final int[] cellDimensions = { 32, 32, 32 };
	final long[] downsampledDimensions;

	public DownsampleMemProfile()
	{
//		final String fn = "/Users/pietzsch/workspace/data/e002_stack_fused-8bit.tif";
//		raw = IOFunctions.openAs32BitArrayImg( new File( fn ) );

		raw = ArrayImgs.unsignedBytes( 256, 256, 128 );
		final Cursor< UnsignedByteType > c = Views.iterable( raw ).localizingCursor();
		while ( c.hasNext() )
		{
			c.next().set( c.getIntPosition( 0 ) % 10 + c.getIntPosition( 1 ) % 13 + c.getIntPosition( 2 ) % 3 );
		}

		downsampledDimensions = Downsample.getDownsampledDimensions( raw.dimensionsAsLongArray(), downsampleInDim );
	}

	public void benchmarkDownsampleFloat()
	{
		final BlockSupplier< UnsignedByteType > blocksFloat = BlockSupplier
				.of( Views.extendBorder( raw ) )
				.andThen( Downsample.downsample(ComputationType.FLOAT, Offset.CENTERED, downsampleInDim ) );

		final CachedCellImg< UnsignedByteType, ? > downsampleFloat = BlockAlgoUtils.cellImg(
				blocksFloat, downsampledDimensions, cellDimensions );

//		touchAllCellsSingleThreaded( downsampleFloat );
		touchAllCells( downsampleFloat );
		downsampleFloat.getCache().invalidateAll();
	}

	private static void touchAllCells( final AbstractCellImg< ?, ?, ?, ? > img )
	{
		final IterableInterval< ? > cells = img.getCells();

		final TaskExecutor te = Parallelization.getTaskExecutor();
		final int numThreads = te.getParallelism();
		final long size = cells.size();
		final AtomicLong nextIndex = new AtomicLong();
		te.forEach( IntStream.range( 0, numThreads ).boxed().collect( Collectors.toList() ), workerIndex -> {
			final Cursor< ? > cursor = cells.cursor();
			long iCursor = -1;
			for ( long i = nextIndex.getAndIncrement(); i < size; i = nextIndex.getAndIncrement() )
			{
				cursor.jumpFwd( i - iCursor );
				cursor.get();
				iCursor = i;
			}
		} );
	}

	private static void touchAllCellsSingleThreaded( final AbstractCellImg< ?, ?, ?, ? > img )
	{
		final Cursor< ? > cursor = img.getCells().cursor();
		while ( cursor.hasNext() )
			cursor.next();
	}

	public static void main( String... args ) throws InterruptedException
	{
		final DownsampleMemProfile benchmark = new DownsampleMemProfile();
		while ( true )
		{
			benchmark.benchmarkDownsampleFloat();
		}
	}
}
