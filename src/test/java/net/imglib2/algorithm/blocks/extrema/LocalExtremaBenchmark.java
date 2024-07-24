package net.imglib2.algorithm.blocks.extrema;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

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

import net.imglib2.Point;
import net.imglib2.algorithm.localextrema.LocalExtrema;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;

@State( Scope.Benchmark )
@Warmup( iterations = 10, time = 100, timeUnit = TimeUnit.MILLISECONDS )
@Measurement( iterations = 30, time = 100, timeUnit = TimeUnit.MILLISECONDS )
@BenchmarkMode( Mode.AverageTime )
@OutputTimeUnit( TimeUnit.MILLISECONDS )
@Fork( 1 )
public class LocalExtremaBenchmark
{
//	final int[] size = { 128, 128, 128 };
	final int[] size = { 64, 64, 64 };
//	final int[] size = { 32, 32, 32 };
//	final int[] size = { 16, 16, 16 };
//	final int[] size = { 8, 8, 8 };

	final LocalMaximaProcessor proc;
	final float[] srcBuf;

	public LocalExtremaBenchmark()
	{
		final int n = size.length;
		proc = new LocalMaximaProcessor( n );
		proc.setTargetInterval( new long[ n ], size );

		final int[] sourceSize = proc.getSourceSize();
		final int sourceLen = Util.safeInt( Intervals.numElements( sourceSize ) );
		srcBuf = new float[ sourceLen ];
		final Random random = new Random();
		for ( int i = 0; i < sourceLen; ++i )
			srcBuf[ i ] = random.nextInt( 100 );
	}

	@Benchmark
	public Object b()
	{
		final byte[] dest = new byte[ ( int ) Intervals.numElements( size ) ];
		proc.compute( srcBuf, dest );
		return dest;
	}

	@Benchmark
	public Object reference()
	{
		final Img< FloatType > srcImg = ArrayImgs.floats( srcBuf, Util.int2long( proc.getSourceSize() ) );
		final List< Point > localExtrema = LocalExtrema.findLocalExtrema( srcImg, new LocalExtrema.MaximumCheck<>( new FloatType( 0f ) ) );
		return localExtrema;
	}

	public static void main2( String[] args )
	{
		new LocalExtremaBenchmark().b();
	}

	public static void main( String[] args ) throws RunnerException
	{
		Options options = new OptionsBuilder().include( LocalExtremaBenchmark.class.getSimpleName() + "\\." ).build();
		new Runner( options ).run();
	}
}
