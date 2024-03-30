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
