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
package net.imglib2.algorithm.blocks.transform;

import ij.IJ;
import ij.ImagePlus;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.blocks.transform.Transform.Interpolation;
import net.imglib2.blocks.PrimitiveBlocks;
import net.imglib2.converter.Converters;
import net.imglib2.converter.RealFloatConverter;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.PrimitiveType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
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
@Measurement( iterations = 20, time = 100, timeUnit = TimeUnit.MILLISECONDS )
@BenchmarkMode( Mode.AverageTime )
@OutputTimeUnit( TimeUnit.MILLISECONDS )
@Fork( 1 )
public class TransformBenchmark3DonlyCompute
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

	public TransformBenchmark3DonlyCompute()
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

		blocksnaiveSetup();
	}

	PrimitiveBlocks< FloatType > blocks;
	Affine3DProcessor< float[] > processor;
	float[] dest;

	public void blocksnaiveSetup()
	{
		blocks = PrimitiveBlocks.of(
				Converters.convert(
						Views.extendZero( img ),
						new RealFloatConverter<>(),
						new FloatType() ) );
		processor = new Affine3DProcessor<>( affine.inverse(), Interpolation.NLINEAR, PrimitiveType.FLOAT );
		long[] max = new long[ size.length ];
		Arrays.setAll( max, d -> min[ d ] + size[ d ] - 1 );
		processor.setTargetInterval( FinalInterval.wrap( min, max ) );
		blocks.copy( processor.getSourcePos(), processor.getSourceBuffer(), processor.getSourceSize() );
		dest = new float[ ( int ) Intervals.numElements( size ) ];
	}

	@Benchmark
	public void compute()
	{
		processor.compute( processor.getSourceBuffer(), dest );
	}

	public static void main( String[] args ) throws RunnerException
	{
		Options options = new OptionsBuilder().include( TransformBenchmark3DonlyCompute.class.getSimpleName() + "\\." ).build();
		new Runner( options ).run();
	}
}
