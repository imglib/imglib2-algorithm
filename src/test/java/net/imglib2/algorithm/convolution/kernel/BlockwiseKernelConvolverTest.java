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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.convolution.LineConvolution;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.list.ListImgFactory;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.junit.Test;

public class BlockwiseKernelConvolverTest
{
	// NB: more pixels than BlockwiseKernelConvolver.MAX_BLOCK_SIZE, so blocks are split
	private static final long[] DIMS = { 300, 250 };

	private static final Kernel1D[] KERNELS = {
			Kernel1D.symmetric( new double[] { 2, 1, 0.5 } ),
			Kernel1D.asymmetric( new double[] { 1, 2, 3, 0, 0 }, 1 ),
			Kernel1D.asymmetric( new double[] { 4, 5, 6 }, 2 ) };

	@Test
	public void testBlockwiseIsUsedForArrayImg()
	{
		final Img< DoubleType > source = createSource();
		assertNotNull( BlockwiseKernelConvolver.create( KERNELS[ 0 ], Views.extendZero( source ), ArrayImgs.floats( DIMS ), 0 ) );
		assertNull( BlockwiseKernelConvolver.create( KERNELS[ 0 ], Views.extendZero( toListImg( source ) ), ArrayImgs.floats( DIMS ), 0 ) );
	}

	@Test
	public void testBlockwiseMatchesDirectConvolution()
	{
		testMatchesDirectConvolution( createSource() );
	}

	@Test
	public void testFallbackMatchesDirectConvolution()
	{
		testMatchesDirectConvolution( toListImg( createSource() ) );
	}

	@Test
	public void testIntTargetIsExact()
	{
		// NB: not representable as float, so this needs double precision
		final int value = 100_000_001;
		final Img< IntType > source = ArrayImgs.ints( DIMS );
		source.forEach( t -> t.set( value ) );
		final Img< IntType > target = ArrayImgs.ints( DIMS );
		final Kernel1D identity = Kernel1D.symmetric( new double[] { 1 } );
		assertNotNull( BlockwiseKernelConvolver.create( identity, Views.extendZero( source ), target, 0 ) );
		new LineConvolution<>( new KernelConvolverFactory( identity ), 0 ).process( Views.extendBorder( source ), target );
		target.forEach( t -> assertEquals( value, t.get() ) );
	}

	private static void testMatchesDirectConvolution( final Img< DoubleType > source )
	{
		final RandomAccessible< DoubleType > extended = Views.extendZero( source );
		for ( final Kernel1D kernel : KERNELS )
		{
			for ( int d = 0; d < DIMS.length; ++d )
			{
				final RandomAccessibleInterval< DoubleType > doubleTarget = Views.translate( ArrayImgs.doubles( DIMS ), 5, -3 );
				new LineConvolution<>( new KernelConvolverFactory( kernel ), d ).process( extended, doubleTarget );
				assertMatchesDirectConvolution( extended, kernel, d, doubleTarget, 1e-10 );

				final RandomAccessibleInterval< FloatType > floatTarget = Views.translate( ArrayImgs.floats( DIMS ), 5, -3 );
				new LineConvolution<>( new KernelConvolverFactory( kernel ), d ).process( extended, floatTarget );
				assertMatchesDirectConvolution( extended, kernel, d, floatTarget, 1e-3 );
			}
		}
	}

	private static void assertMatchesDirectConvolution( final RandomAccessible< DoubleType > source, final Kernel1D kernel, final int d, final RandomAccessibleInterval< ? extends RealType< ? > > target, final double tolerance )
	{
		final double[] k = kernel.fullKernel();
		final RandomAccess< DoubleType > in = source.randomAccess();
		final Cursor< ? extends RealType< ? > > out = Views.flatIterable( target ).localizingCursor();
		while ( out.hasNext() )
		{
			out.fwd();
			double expected = 0;
			for ( long m = kernel.min(); m <= kernel.max(); ++m )
			{
				in.setPosition( out );
				in.move( -m, d );
				expected += k[ ( int ) ( m - kernel.min() ) ] * in.get().get();
			}
			assertEquals( expected, out.get().getRealDouble(), tolerance );
		}
	}

	private static Img< DoubleType > createSource()
	{
		final Img< DoubleType > source = ArrayImgs.doubles( DIMS );
		long i = 0;
		for ( final DoubleType t : source )
		{
			t.set( i * i * i % 17 );
			++i;
		}
		return source;
	}

	private static Img< DoubleType > toListImg( final Img< DoubleType > img )
	{
		final Img< DoubleType > list = new ListImgFactory<>( new DoubleType() ).create( DIMS );
		LoopBuilder.setImages( img, list ).forEachPixel( ( a, b ) -> b.set( a ) );
		return list;
	}
}
