package net.imglib2.algorithm.blocks.downsample;

import bdv.export.DownsampleBlock;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.blocks.downsample.Downsample.ComputationType;
import net.imglib2.algorithm.blocks.downsample.Downsample.Offset;
import net.imglib2.algorithm.blocks.BlockAlgoUtils;
import net.imglib2.blocks.PrimitiveBlocks;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.ReadOnlyCachedCellImgFactory;
import net.imglib2.cache.img.ReadOnlyCachedCellImgOptions;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.cell.AbstractCellImg;
import net.imglib2.parallel.Parallelization;
import net.imglib2.parallel.TaskExecutor;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.view.Views;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State( Scope.Benchmark )
@Warmup( iterations = 3, time = 100, timeUnit = TimeUnit.MILLISECONDS )
@Measurement( iterations = 10, time = 100, timeUnit = TimeUnit.MILLISECONDS )
@BenchmarkMode( Mode.AverageTime )
@OutputTimeUnit( TimeUnit.MILLISECONDS )
@Fork( 1 )
public class DownsampleBdvBenchmark
{
	final RandomAccessibleInterval< UnsignedByteType > raw;
	final boolean[] downsampleInDim = { true, true, true };
	final int[] cellDimensions = { 64, 64, 64 };
//	final int[] cellDimensions = { 32, 32, 32 };
	final long[] downsampledDimensions;

	public DownsampleBdvBenchmark()
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

	@Benchmark
	public void benchmarkBdv()
	{
		RandomAccessible< UnsignedByteType > extended = Views.extendBorder( raw );
		int[] downsamplingFactors = new int[ 3 ];
		Arrays.setAll(downsamplingFactors, d -> downsampleInDim[ d ] ? 2 : 1 );
		final DownsampleBlock< UnsignedByteType > downsampleBlock = DownsampleBlock.create( cellDimensions, downsamplingFactors, UnsignedByteType.class, RandomAccess.class );
		final RandomAccess< UnsignedByteType > in = extended.randomAccess();
		final long[] currentCellMin = new long[ 3 ];
		final int[] currentCellDim = new int[ 3 ];
		CellLoader< UnsignedByteType > downsampleBlockLoader = cell -> {
			Arrays.setAll( currentCellMin, d -> cell.min( d ) * downsamplingFactors[ d ] );
			Arrays.setAll( currentCellDim, d -> ( int ) cell.dimension( d ) );
			in.setPosition( currentCellMin );
			downsampleBlock.downsampleBlock( in, cell.cursor(), currentCellDim );
		};
		final CachedCellImg< UnsignedByteType, ? > downsampled = new ReadOnlyCachedCellImgFactory().create(
				downsampledDimensions,
				new UnsignedByteType(),
				downsampleBlockLoader,
				ReadOnlyCachedCellImgOptions.options().cellDimensions( cellDimensions) );

		touchAllCellsSingleThreaded( downsampled );
		downsampled.getCache().invalidateAll();
	}

	@Benchmark
	public void benchmarkDownsampleDouble()
	{
		final PrimitiveBlocks< UnsignedByteType > blocks = PrimitiveBlocks.of( Views.extendBorder( raw ) );

//		final CellLoader< DoubleType> loader = cellLoader( blocks, net.imglib2.algorithm.blocks.downsample.Downsample.downsample( new DoubleType(), ComputationType.AUTO, Offset.CENTERED, downsampleInDim ) );
//		final CachedCellImg< DoubleType, ? > downsampleDouble =  new ReadOnlyCachedCellImgFactory().create(
//				downsampledDimensions,
//				new DoubleType(),
//				loader,
//				ReadOnlyCachedCellImgOptions.options().cellDimensions( cellDimensions).cacheType( BOUNDED ).maxCacheSize( 1 ) );
		final CachedCellImg< UnsignedByteType, ? > downsampleDouble = BlockAlgoUtils.cellImg(
				blocks, Downsample.downsample( new UnsignedByteType(), ComputationType.DOUBLE, Offset.HALF_PIXEL, downsampleInDim ), new UnsignedByteType(), downsampledDimensions, cellDimensions );

		touchAllCellsSingleThreaded( downsampleDouble );
		downsampleDouble.getCache().invalidateAll();
	}

	@Benchmark
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
				blocksFloat, Downsample.downsample( new UnsignedByteType(), ComputationType.FLOAT, Offset.HALF_PIXEL, downsampleInDim ), new UnsignedByteType(), downsampledDimensions, cellDimensions );

		touchAllCellsSingleThreaded( downsampleFloat );
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

	public static void main( String... args ) throws RunnerException
	{
		Options options = new OptionsBuilder()
				.include( DownsampleBdvBenchmark.class.getSimpleName() + "\\." )
				.shouldDoGC( true )
				.build();
		new Runner( options ).run();
	}
}
