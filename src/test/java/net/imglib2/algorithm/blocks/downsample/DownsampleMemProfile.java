package net.imglib2.algorithm.blocks.downsample;

import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.blocks.downsample.Downsample.ComputationType;
import net.imglib2.algorithm.blocks.downsample.Downsample.Offset;
import net.imglib2.algorithm.blocks.BlockAlgoUtils;
import net.imglib2.blocks.PrimitiveBlocks;
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
		final PrimitiveBlocks< UnsignedByteType > blocksFloat = PrimitiveBlocks.of( Views.extendBorder( raw ) );

//		final CellLoader< FloatType> loader = cellLoader( blocksFloat, net.imglib2.algorithm.blocks.downsample.Downsample.downsample( new FloatType(), ComputationType.AUTO, Offset.CENTERED, downsampleInDim ) );
//		final CachedCellImg< FloatType, ? > downsampleFloat =  new ReadOnlyCachedCellImgFactory().create(
//				downsampledDimensions,
//				new FloatType(),
//				loader,
//				ReadOnlyCachedCellImgOptions.options().cellDimensions( cellDimensions).cacheType( BOUNDED ).maxCacheSize( 1 ) );
		final CachedCellImg< UnsignedByteType, ? > downsampleFloat = BlockAlgoUtils.cellImg(
				blocksFloat, Downsample.downsample( new UnsignedByteType(), ComputationType.FLOAT, Offset.CENTERED, downsampleInDim ), new UnsignedByteType(), downsampledDimensions, cellDimensions );

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
