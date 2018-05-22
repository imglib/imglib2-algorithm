/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2018 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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
package net.imglib2.algorithm.convolution.fast_gauss;

import ij.ImagePlus;
import ij.process.FloatProcessor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.DoubleArray;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

/**
 * Performance benchmark to demonstrate {@link LoopBuilder} performance.
 *
 * @author Matthias Arzt
 */
@State( Scope.Benchmark )
public class GaussPerformance
{

	private final double sigma = 2.0;

	Img< DoubleType > img = createImpulsImage();

	private static ArrayImg< DoubleType, DoubleArray > createImpulsImage()
	{
		long[] size = { 100, 100, 100 };
		long[] middle = { 50, 50, 50 };
		ArrayImg< DoubleType, DoubleArray > img = ArrayImgs.doubles(size);
		final RandomAccess< ? extends RealType<?> > ra = img.randomAccess();
		ra.setPosition( middle );
		ra.get().setReal( 10000 );
		return img;
	}

	@Benchmark
	public void gaussClever() {
		FastGauss.convolve( sigma, Views.extendBorder(img), img );
	}

	@Benchmark
	public void gaussOld() {
		Gauss3.gauss( sigma, Views.extendBorder(img), img );
	}

	RandomAccessibleInterval< FloatType > image = ArrayImgs.floats( 100, 100 );

	@Benchmark
	public void gauss2d() {
		Gauss3.gauss( sigma, Views.extendBorder(image), image );
	}

	ImagePlus imagePlus = new ImagePlus( "", new FloatProcessor( 100, 100 ) );

	@Benchmark
	public void gauss2dOld() {
		imagePlus.getProcessor().blurGaussian( sigma );
	}

	public static void main( final String... args ) throws RunnerException
	{
		final Options opt = new OptionsBuilder()
				.include( GaussPerformance.class.getSimpleName() )
				.forks( 1 )
				.warmupIterations( 8 )
				.measurementIterations( 8 )
				.warmupTime( TimeValue.milliseconds( 100 ) )
				.measurementTime( TimeValue.milliseconds( 100 ) )
				.build();
		new Runner( opt ).run();
	}
}
