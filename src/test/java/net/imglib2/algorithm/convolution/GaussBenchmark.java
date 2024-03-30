/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2024 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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
