package net.imglib2.algorithm.convolution;

import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.algorithm.convolution.fast_gauss.FastGaussConvolverRealType;
import net.imglib2.algorithm.convolution.kernel.ConvolverNativeType;
import net.imglib2.algorithm.convolution.kernel.ConvolverNumericType;
import net.imglib2.algorithm.convolution.kernel.FloatConvolverRealType;
import net.imglib2.algorithm.convolution.kernel.Kernel1D;
import net.imglib2.algorithm.convolution.kernel.KernelConvolverFactory;
import net.imglib2.algorithm.convolution.kernel.DoubleConvolverRealType;
import net.imglib2.algorithm.gauss3.ConvolverFactory;
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

	private final int d = 2;

	private final long lineLength = 1000;

	private final double sigma = 2.0;

	private final long[] dims = { 10, 10, lineLength };

	private final RandomAccessible< DoubleType > inImage = Views.extendBorder( ArrayImgs.doubles( dims ) );

	private final Img< DoubleType > outImage = ArrayImgs.doubles( dims );

	private final double[] halfKernel = Gauss3.halfkernels( new double[] { sigma } )[ 0 ];

	private final Kernel1D kernel = Kernel1D.symmetric( halfKernel );

	@Benchmark
	public void asymmetricKernelConvolver()
	{
		LineConvolverFactory< NumericType< ? > > assymetricConvolver = new KernelConvolverFactory( kernel );
		final Runnable runnable = assymetricConvolver.getConvolver( in(), out(), d, lineLength );
		runnable.run();
	}

	@Benchmark
	public void symmetricKernelConvolver()
	{
		final ConvolverFactory< DoubleType, DoubleType > factory = net.imglib2.algorithm.gauss3.DoubleConvolverRealType.< DoubleType, DoubleType >factory();
		final Runnable runnable = factory.create( halfKernel, in(), out(), d, lineLength );
		runnable.run();
	}

	@Benchmark
	public void asymmetricDoubleConvolver()
	{
		final Runnable runnable = new DoubleConvolverRealType( kernel, in(), out(), d, lineLength );
		runnable.run();
	}

	@Benchmark
	public void asymmetricFloatConvolver()
	{
		final Runnable runnable = new FloatConvolverRealType( kernel, in(), out(), d, lineLength );
		runnable.run();
	}

	@Benchmark
	public void asymmetricNativeConvolver()
	{
		final Runnable runnable = new ConvolverNativeType<>( kernel, in(), out(), d, lineLength );
		runnable.run();
	}

	@Benchmark
	public void asymmetricNumericConvolver()
	{
		final Runnable runnable = new ConvolverNumericType<>( kernel, in(), out(), d, lineLength );
		runnable.run();
	}

	@Benchmark
	public void fastGaussConvolver()
	{
		Runnable runnable = new FastGaussConvolverRealType( sigma )
				.getConvolver( in(), out(), d, lineLength );
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
				.forks( 1 )
				.warmupIterations( 8 )
				.measurementIterations( 8 )
				.warmupTime( TimeValue.milliseconds( 100 ) )
				.measurementTime( TimeValue.milliseconds( 100 ) )
				.build();
		new Runner( opt ).run();
	}
}
