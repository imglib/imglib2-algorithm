/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2026 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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
package net.imglib2.algorithm.convolution.kernel;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.img.list.ListImgFactory;
import net.imglib2.parallel.Parallelization;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
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

/**
 * Measures {@link Gauss3#gauss} on a 200³ image for several pixel types, image
 * types, and thread counts. {@code array} and {@code cell} images take the
 * {@link BlockwiseKernelConvolver} path, {@code list} images the per-line path.
 */
@Fork( 1 )
@State( Scope.Benchmark )
@Warmup( iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS )
@Measurement( iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS )
@BenchmarkMode( Mode.AverageTime )
@OutputTimeUnit( TimeUnit.MILLISECONDS )
public class BlockwiseKernelConvolverBenchmark
{
	@Param( { "float", "double", "u8", "u16" } )
	public String type;

	@Param( { "array", "cell", "list" } )
	public String img;

	@Param( { "1", "all" } )
	public String threads;

	private static final double SIGMA = 2.0;

	private static final long[] SIZE = { 200, 200, 200 };

	private Img< ? extends RealType< ? > > source;

	private Img< ? extends RealType< ? > > target;

	@Setup
	public void setup()
	{
		source = create();
		target = create();
		final Random random = new Random( 42 );
		source.forEach( t -> t.setReal( random.nextInt( 256 ) ) );
	}

	private RealType< ? > pixelType()
	{
		switch ( type )
		{
		case "float": return new FloatType();
		case "double": return new DoubleType();
		case "u8": return new UnsignedByteType();
		case "u16": return new UnsignedShortType();
		default: throw new IllegalArgumentException( type );
		}
	}

	@SuppressWarnings( { "unchecked", "rawtypes" } )
	private Img< ? extends RealType< ? > > create()
	{
		final NativeType t = ( NativeType ) pixelType();
		final ImgFactory factory;
		switch ( img )
		{
		case "array": factory = new ArrayImgFactory( t ); break;
		case "cell": factory = new CellImgFactory( t, 64 ); break;
		case "list": factory = new ListImgFactory( t ); break;
		default: throw new IllegalArgumentException( img );
		}
		return factory.create( SIZE );
	}

	@Benchmark
	public void gauss3()
	{
		if ( threads.equals( "1" ) )
			Parallelization.runSingleThreaded( this::run );
		else
			run();
	}

	@SuppressWarnings( { "unchecked", "rawtypes" } )
	private void run()
	{
		Gauss3.gauss( SIGMA, Views.extendBorder( ( Img ) source ), ( Img ) target );
	}

	public static void main( final String[] args ) throws RunnerException
	{
		final Options options = new OptionsBuilder()
				.include( BlockwiseKernelConvolverBenchmark.class.getSimpleName() )
				.build();
		new Runner( options ).run();
	}
}
