package net.imglib2.algorithm.convolution;

import net.imglib2.RandomAccessible;
import net.imglib2.algorithm.convolution.fast_gauss.FastGauss;
import net.imglib2.algorithm.convolution.kernel.Kernel1D;
import net.imglib2.algorithm.convolution.kernel.SeparableKernelConvolution;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.algorithm.gauss3.SeparableSymmetricConvolution;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@State( Scope.Benchmark )
public class GaussBenchmark
{

	private double sigma = 2.0;

	private long[] dims = { 200, 200, 200 };

	private RandomAccessible< DoubleType > inImage = Views.extendBorder( ArrayImgs.doubles( dims ) );

	private Img< DoubleType > outImage = ArrayImgs.doubles( dims );

	@Benchmark
	public void benchmarkSeparableKernelConvolution()
	{
		double[][] halfKernels = Gauss3.halfkernels( new double[] { sigma, sigma, sigma } );
		final int numthreads = Runtime.getRuntime().availableProcessors();
		final ExecutorService service = Executors.newFixedThreadPool( numthreads );
		final Convolution< NumericType< ? > > convolution = SeparableKernelConvolution.convolution( Kernel1D.symmetric( halfKernels ) );
		convolution.setExecutor( service );
		convolution.process( inImage, outImage );
		service.shutdown();
	}

	@Benchmark
	public void benchmarkSeparableSymmetricConvolution()
	{
		double[][] halfKernels = Gauss3.halfkernels( new double[] { sigma, sigma, sigma } );
		final int numthreads = Runtime.getRuntime().availableProcessors();
		final ExecutorService service = Executors.newFixedThreadPool( numthreads );
		SeparableSymmetricConvolution.convolve( halfKernels, inImage, outImage, service );
		service.shutdown();
	}

	@Benchmark
	public void benchmarkFastGauss()
	{
		FastGauss.convolution( sigma ).process( inImage, outImage );
	}

	public static void main( String[] args ) throws RunnerException
	{
		Options opt = new OptionsBuilder()
				.include( GaussBenchmark.class.getSimpleName() )
				.forks( 1 )
				.warmupIterations( 8 )
				.measurementIterations( 8 )
				.warmupTime( TimeValue.milliseconds( 100 ) )
				.measurementTime( TimeValue.milliseconds( 100 ) )
				.build();
		new Runner( opt ).run();
	}
}
