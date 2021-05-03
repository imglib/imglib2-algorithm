/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2021 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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

import static org.junit.Assert.assertArrayEquals;

import net.imglib2.RandomAccess;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.DoubleType;

import org.junit.Test;

/**
 * Tests {@link ConvolverNativeType}, {@link ConvolverNumericType},
 * {@link DoubleConvolverRealType} and {@link FloatConvolverRealType}.
 *
 * @author Tobias Pietzsch
 */
public class ConvolverTest
{
	@Test
	public void testConvolverNativeType()
	{
		testConvolver( ConvolverNativeType::new );
	}

	@Test
	public void testConvolverNumericType()
	{
		testConvolver( ConvolverNumericType::new );
	}

	@Test
	public void testDoubleConvolverRealType()
	{
		testConvolver( DoubleConvolverRealType::new );
	}

	@Test
	public void testFloatConvolverRealType()
	{
		testConvolver( FloatConvolverRealType::new );
	}

	private void testConvolver( ConvolverConstructor< DoubleType > constructor )
	{
		final double[] kernel = { 1.0, 2.0, 3.0, 4.0 };
		final int center = 2;
		final double[] in = { 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0 };
		final double[] out = new double[ kernel.length ];
		final Runnable convolver = constructor.create(
				Kernel1D.asymmetric( kernel, center ),
				ArrayImgs.doubles( in, in.length ).randomAccess(),
				ArrayImgs.doubles( out, out.length ).randomAccess(),
				0,
				out.length );
		convolver.run();
		assertArrayEquals( kernel, out, 0 );
	}

	private interface ConvolverConstructor< T >
	{
		Runnable create( final Kernel1D kernel, final RandomAccess< ? extends T > in, final RandomAccess< ? extends T > out, final int d, final long lineLength );
	}
}
