package net.imglib2.algorithm.blocks.transform;

import ij.IJ;
import ij.ImagePlus;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealRandomAccessible;
import net.imglib2.algorithm.blocks.BlockProcessor;
import net.imglib2.algorithm.blocks.transform.Transform.Interpolation;
import net.imglib2.blocks.PrimitiveBlocks;
import net.imglib2.converter.Converters;
import net.imglib2.converter.RealDoubleConverter;
import net.imglib2.converter.RealFloatConverter;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.interpolation.randomaccess.ClampingNLinearInterpolatorFactory;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.realtransform.RealViews;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
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
@Warmup( iterations = 10, time = 100, timeUnit = TimeUnit.MILLISECONDS )
@Measurement( iterations = 30, time = 100, timeUnit = TimeUnit.MILLISECONDS )
@BenchmarkMode( Mode.AverageTime )
@OutputTimeUnit( TimeUnit.MILLISECONDS )
@Fork( 1 )
public class TransformBenchmark3D
{

//	TransformBenchmark3D.blocksnaive  avgt   30   17,605 ± 0,100  ms/op
//  TransformBenchmark3D.realviews    avgt   30  316,352 ± 1,976  ms/op

//	final long[] min = { 693, 343, 208 };
//	final int[] size = { 128, 128, 128 };
//	final int[] size = { 64, 64, 64 }; // 19
//	final int[] size = { 32, 32, 32 }; // 15
//	final int[] size = { 16, 16, 16 }; // 13
//	final int[] size = { 8, 8, 8 }; // 12

	final long[] min = { 200, -330, 120 };
//	final int[] size = { 256, 256, 256 };
//	final int[] size = { 128, 128, 128 };
	final int[] size = { 64, 64, 64 }; // 16
//	final int[] size = { 32, 32, 32 }; // 15
//	final int[] size = { 16, 16, 16 }; // 15
//	final int[] size = { 8, 8, 8 }; // 14
	final AffineTransform3D affine = new AffineTransform3D();
	final RandomAccessibleInterval< UnsignedByteType > img;

	public TransformBenchmark3D()
	{
		final String fn = "/Users/pietzsch/workspace/data/e002_stack_fused-8bit.tif";
		final ImagePlus imp = IJ.openImage( fn );
		img = ImageJFunctions.wrapByte( imp );
//		affine.rotate( 2,0.3 );
//		affine.rotate( 1,0.1 );
//		affine.rotate( 0,-0.2 );
//		affine.scale( 1.4 );
		affine.rotate( 2,0.3 );
		affine.rotate( 1,0.1 );
		affine.rotate( 0,1.5 );
		affine.scale( 1.4 );

		realviewsSetup();
		blocksnaiveSetup();
	}

	RandomAccessible< UnsignedByteType > transformed;

	public void realviewsSetup()
	{
		RealRandomAccessible< UnsignedByteType > interpolated = Views.interpolate( Views.extendZero( img ), new ClampingNLinearInterpolatorFactory<>() );
		transformed = RealViews.affine( interpolated, affine );
	}

	@Benchmark
	public Object realviews()
	{
		final RandomAccessibleInterval< UnsignedByteType > copy = copy( transformed, new UnsignedByteType(), min, size );
		return copy;
	}

	PrimitiveBlocks< FloatType > blocks;
	BlockProcessor< float[], float[] > processor;
	PrimitiveBlocks< DoubleType > blocksDouble;
	BlockProcessor< double[], double[] > processorDouble;
	PrimitiveBlocks< UnsignedByteType > blocksUnsignedByte;
	BlockProcessor< byte[], byte[] > processorUnsignedByte;

	public void blocksnaiveSetup()
	{
		blocks = PrimitiveBlocks.of(
				Converters.convert(
						Views.extendZero( img ),
						new RealFloatConverter<>(),
						new FloatType() ) );
		processor = Transform.affine( new FloatType(), affine, Interpolation.NLINEAR ).blockProcessor();
		blocksDouble = PrimitiveBlocks.of(
				Converters.convert(
						Views.extendZero( img ),
						new RealDoubleConverter<>(),
						new DoubleType() ) );
		processorDouble = Transform.affine( new DoubleType(), affine, Interpolation.NLINEAR ).blockProcessor();
		blocksUnsignedByte = PrimitiveBlocks.of( Views.extendZero( img ) );
		processorUnsignedByte = Transform.affine( new UnsignedByteType(), affine, Interpolation.NLINEAR ).blockProcessor();
		blocksFloat();
		blocksDouble();
		blocksUnsignedByte();
		blocksFloat();
		blocksDouble();
		blocksUnsignedByte();
	}

	@Benchmark
	public Object blocksFloat()
	{
		long[] max = new long[ size.length ];
		Arrays.setAll( max, d -> min[ d ] + size[ d ] - 1 );
		processor.setTargetInterval( FinalInterval.wrap( min, max ) );
		blocks.copy( processor.getSourcePos(), processor.getSourceBuffer(), processor.getSourceSize() );
		final float[] dest = new float[ ( int ) Intervals.numElements( size ) ];
		processor.compute( processor.getSourceBuffer(), dest );
		final RandomAccessibleInterval< FloatType > destImg = ArrayImgs.floats( dest, size[ 0 ], size[ 1 ], size[ 2 ] );
		return destImg;
	}

	@Benchmark
	public Object blocksDouble()
	{
		long[] max = new long[ size.length ];
		Arrays.setAll( max, d -> min[ d ] + size[ d ] - 1 );
		processorDouble.setTargetInterval( FinalInterval.wrap( min, max ) );
		blocksDouble.copy( processorDouble.getSourcePos(), processorDouble.getSourceBuffer(), processorDouble.getSourceSize() );
		final double[] dest = new double[ ( int ) Intervals.numElements( size ) ];
		processorDouble.compute( processorDouble.getSourceBuffer(), dest );
		final RandomAccessibleInterval< DoubleType > destImg = ArrayImgs.doubles( dest, size[ 0 ], size[ 1 ], size[ 2 ] );
		return destImg;
	}

	@Benchmark
	public Object blocksUnsignedByte()
	{
		long[] max = new long[ size.length ];
		Arrays.setAll( max, d -> min[ d ] + size[ d ] - 1 );
		processorUnsignedByte.setTargetInterval( FinalInterval.wrap( min, max ) );
		blocksUnsignedByte.copy( processorUnsignedByte.getSourcePos(), processorUnsignedByte.getSourceBuffer(), processorUnsignedByte.getSourceSize() );
		final byte[] dest = new byte[ ( int ) Intervals.numElements( size ) ];
		processorUnsignedByte.compute( processorUnsignedByte.getSourceBuffer(), dest );
		final RandomAccessibleInterval< UnsignedByteType > destImg = ArrayImgs.unsignedBytes( dest, size[ 0 ], size[ 1 ], size[ 2 ] );
		return destImg;
	}


	public static void main( String[] args ) throws RunnerException
	{
		Options options = new OptionsBuilder().include( TransformBenchmark3D.class.getSimpleName() + "\\." ).build();
		new Runner( options ).run();
	}


	// ------------------------------------------------------------------------


	private static < T extends NativeType< T > > RandomAccessibleInterval< T > copy(
			final RandomAccessible< T > ra,
			final T type,
			final long[] min,
			final int[] size )
	{
		final ArrayImg< T, ? > img = new ArrayImgFactory<>( type ).create( size );
		long[] max = new long[ size.length ];
		Arrays.setAll( max, d -> min[ d ] + size[ d ] - 1 );
		final Cursor< T > cin = Views.flatIterable( Views.interval( ra, min, max ) ).cursor();
		final Cursor< T > cout = img.cursor();
		while ( cout.hasNext() )
			cout.next().set( cin.next() );
		return img;
	}
}
