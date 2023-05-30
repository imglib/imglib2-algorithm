package net.imglib2.algorithm.blocks.downsample;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import net.imglib2.FinalInterval;
import net.imglib2.algorithm.blocks.downsample.DownsampleBlockProcessors.CenterDouble;
import net.imglib2.algorithm.blocks.downsample.DownsampleBlockProcessors.CenterFloat;
import net.imglib2.algorithm.blocks.downsample.DownsampleBlockProcessors.HalfPixelDouble;
import net.imglib2.algorithm.blocks.downsample.DownsampleBlockProcessors.HalfPixelFloat;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
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
public class DownsampleBenchmarkFull
{
	int[] imgSize;

	int[] outputSize;
	int[] inputSize;
	float[] inputF;
	float[] outputF;
	double[] inputD;
	double[] outputD;
	CenterFloat centerFloat;
	CenterDouble centerDouble;
	HalfPixelFloat halfPixelFloat;
	HalfPixelDouble halfPixelDouble;

//	@Param( { "X", "Y", "Z", "XYZ" } )
	@Param( { "XYZ" } )
	public String scenario;

	//	@Param( { "64", "128", "256" } )
	@Param( { "128" } )
	public int size;


	@Setup(Level.Trial)
	public void setUp() {
		final Random random = new Random( 1 );

		final int n = 3;
		inputSize = new int[ n ];
		outputSize = new int[ n ];

		boolean[] downsampleInDim = null;
		switch ( scenario )
		{
		case "X":
			downsampleInDim = new boolean[] { true, false, false };
			break;
		case "Y":
			downsampleInDim = new boolean[] { false, true, false };
			break;
		case "Z":
			downsampleInDim = new boolean[] { false, false, true };
			break;
		case "XYZ":
			downsampleInDim = new boolean[] { true, true, true };
			break;
		}

		imgSize = new int[] { size, size, size };

		final long[] destSize = Downsample.getDownsampledDimensions( Util.int2long( imgSize ), downsampleInDim );
		System.out.println( "destSize = " + Arrays.toString( destSize ) );
		Arrays.setAll( outputSize, d -> ( int ) destSize[ d ] );

		centerFloat = new CenterFloat( downsampleInDim );
		centerFloat.setTargetInterval( new FinalInterval( Util.int2long( outputSize ) ) );
		System.arraycopy( centerFloat.getSourceSize(), 0, inputSize, 0, inputSize.length );
		inputF = new float[ ( int ) Intervals.numElements( inputSize ) ];
		for ( int i = 0; i < inputF.length; i++ )
			inputF[ i ] = random.nextFloat();
		outputF = new float[ ( int ) Intervals.numElements( outputSize ) ];

		centerDouble = new CenterDouble( downsampleInDim );
		centerDouble.setTargetInterval( new FinalInterval( Util.int2long( outputSize ) ) );
		inputD = new double[ ( int ) Intervals.numElements( inputSize ) ];
		for ( int i = 0; i < inputD.length; i++ )
			inputD[ i ] = random.nextDouble();
		outputD = new double[ ( int ) Intervals.numElements( outputSize ) ];

		halfPixelFloat = new HalfPixelFloat( downsampleInDim );
		halfPixelFloat.setTargetInterval( new FinalInterval( Util.int2long( outputSize ) ) );

		halfPixelDouble = new HalfPixelDouble( downsampleInDim );
		halfPixelDouble.setTargetInterval( new FinalInterval( Util.int2long( outputSize ) ) );
	}

	@Benchmark
	public void benchmarkCenterFloat()
	{
		centerFloat.compute( inputF, outputF );
	}

	@Benchmark
	public void benchmarkCenterDouble()
	{
		centerDouble.compute( inputD, outputD );
	}

	@Benchmark
	public void benchmarkHalfPixelFloat()
	{
		halfPixelFloat.compute( inputF, outputF );
	}

	@Benchmark
	public void benchmarkHalfPixelDouble()
	{
		halfPixelDouble.compute( inputD, outputD );
	}

	public static void main( String... args ) throws RunnerException
	{
		Options options = new OptionsBuilder().include( DownsampleBenchmarkFull.class.getSimpleName() + "\\." ).build();
		new Runner( options ).run();
	}
}
