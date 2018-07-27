package net.imglib2.algorithm.convolution;

import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.algorithm.convolution.kernel.Kernel1D;
import net.imglib2.algorithm.convolution.kernel.KernelConvolverFactory;
import net.imglib2.algorithm.gauss3.DoubleConvolverRealType;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

@State( Scope.Benchmark )
public class ConvolverBenchmark
{

	private int d = 2;

	private long lineLength = 1000;

	private double sigma = 2.0;

	private long[] dims = { 10, 10, lineLength };

	RandomAccessible< DoubleType > inImage = Views.extendBorder( ArrayImgs.doubles( dims ) );

	Img< DoubleType > outImage = ArrayImgs.doubles( dims );

	double[] halfKernel = Gauss3.halfkernels( new double[] { sigma } )[ 0 ];

	@Benchmark
	public void asymmetricKernelConvolver()
	{
		LineConvolverFactory< NumericType< ? > > assymetricConvolver = new KernelConvolverFactory( Kernel1D.symmetric( halfKernel ) );
		Runnable runnable = assymetricConvolver.getConvolver( in(), out(), d, lineLength );
		runnable.run();
	}

	@Benchmark
	public void symmetricKernelConvolver()
	{
		Runnable runnable = DoubleConvolverRealType.< DoubleType, DoubleType >factory().create( halfKernel, in(), out(), d, lineLength );
		runnable.run();
	}

	private RandomAccess< DoubleType > in()
	{
		return inImage.randomAccess();
	}

	private RandomAccess< DoubleType > out()
	{
		return outImage.randomAccess();
	}

	public static void main( String[] args ) throws RunnerException
	{
		Options opt = new OptionsBuilder()
				.include( ConvolverBenchmark.class.getSimpleName() )
				.forks( 0 )
				.warmupIterations( 4 )
				.measurementIterations( 8 )
				.warmupTime( TimeValue.milliseconds( 100 ) )
				.measurementTime( TimeValue.milliseconds( 100 ) )
				.build();
		new Runner( opt ).run();
	}
}
