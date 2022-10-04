/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2022 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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

import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.parallel.Parallelization;
import net.imglib2.parallel.TaskExecutors;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Intervals;
import org.junit.Test;

import java.util.concurrent.Executors;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class LineConvolutionTest
{

	@Test
	public void testConvolveOneLine()
	{
		byte[] result = new byte[ 3 ];
		Img< UnsignedByteType > out = ArrayImgs.unsignedBytes( result, result.length );
		Img< UnsignedByteType > in = ArrayImgs.unsignedBytes( new byte[] { 1, 2, 0, 3 }, 4 );
		new LineConvolution<>( new ForwardDifferenceConvolverFactory(), 0 ).process( in, out );
		assertArrayEquals( new byte[] { 1, -2, 3 }, result );
	}

	@Test
	public void testConvolve()
	{
		Img< UnsignedByteType > source = ArrayImgs.unsignedBytes( new byte[] {
				0, 0, 0, 1,
				3, 4, 5, 0,
				0, 0, 0, 3
		}, 2, 2, 3 );
		byte[] result = new byte[ 8 ];
		byte[] expected = new byte[] {
				3, 4, 5, -1,
				-3, -4, -5, 3
		};
		Img< UnsignedByteType > target = ArrayImgs.unsignedBytes( result, 2, 2, 2 );
		Convolution< UnsignedByteType > convolver = new LineConvolution<>( new ForwardDifferenceConvolverFactory(), 2 );
		Interval requiredSource = convolver.requiredSourceInterval( target );
		convolver.process( source, target );
		assertTrue( Intervals.equals( source, requiredSource ) );
		assertArrayEquals( expected, result );
	}

	@Test
	public void testNumTasksEqualsIntegerMaxValue() {
		byte[] result = new byte[ 1 ];
		Img< UnsignedByteType > out = ArrayImgs.unsignedBytes( result, result.length );
		Img< UnsignedByteType > in = ArrayImgs.unsignedBytes( new byte[] { 1, 2 }, 2 );
		Runnable runnable = () -> {
			final LineConvolution< UnsignedByteType > convolution = new LineConvolution<>( new ForwardDifferenceConvolverFactory(), 0 );
			convolution.process( in, out );
		};
		Parallelization.runWithExecutor( TaskExecutors.forExecutorServiceAndNumTasks( Executors.newSingleThreadExecutor(), Integer.MAX_VALUE) , runnable );
		assertArrayEquals( new byte[] { 1 }, result );
	}

	static class ForwardDifferenceConvolverFactory implements LineConvolverFactory< UnsignedByteType >
	{

		@Override public long getBorderBefore()
		{
			return 0;
		}

		@Override public long getBorderAfter()
		{
			return 1;
		}

		@Override public Runnable getConvolver( RandomAccess< ? extends UnsignedByteType > in, RandomAccess< ? extends UnsignedByteType > out, int d, long lineLength )
		{
			return () -> {
				for ( int i = 0; i < lineLength; i++ )
				{
					int center = in.get().get();
					in.fwd( d );
					int front = in.get().get();
					out.get().set( front - center );
					out.fwd( d );
				}
			};
		}

		@Override
		public UnsignedByteType preferredSourceType( UnsignedByteType targetType )
		{
			return new UnsignedByteType();
		}
	}
}
