/*
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

import java.util.Arrays;

import net.imglib2.RandomAccess;
import net.imglib2.algorithm.convolution.LineConvolverFactory;
import net.imglib2.type.numeric.RealType;

/**
 * A 1-dimensional line convolver that operates on all {@link RealType}. It
 * copies the input line into a float[] array and loops over the kernel in the
 * outer loop, so that the JIT can vectorize the inner loop over the line.
 *
 * @author Tobias Pietzsch
 * @author Matthias Arzt
 *
 * @see LineConvolverFactory
 */
public final class FloatConvolverRealType implements Runnable
{

	private final float[] kernel;

	private final RandomAccess< ? extends RealType< ? > > in;

	private final RandomAccess< ? extends RealType< ? > > out;

	private final int d;

	private final int k1k1;

	private final float[] src;

	private final float[] dst;

	public FloatConvolverRealType( final Kernel1D kernel, final RandomAccess< ? extends RealType< ? > > in, final RandomAccess< ? extends RealType< ? > > out, final int d, final long lineLength )
	{
		// NB: This constructor is used in ConvolverFactories. It needs to be public and have this exact signature.
		this.in = in;
		this.out = out;
		this.d = d;
		this.kernel = doubleToFloat( kernel.fullKernel() );

		k1k1 = this.kernel.length - 1;
		dst = new float[ Math.toIntExact( lineLength ) ];
		src = new float[ Math.toIntExact( lineLength + k1k1 ) ];
	}

	private float[] doubleToFloat( final double[] in )
	{
		final float[] out = new float[ in.length ];
		for ( int i = 0; i < in.length; i++ )
			out[ i ] = ( float ) in[ i ];
		return out;
	}

	@Override
	public void run()
	{
		final int n = dst.length;
		for ( int i = 0; i < src.length; ++i )
		{
			src[ i ] = in.get().getRealFloat();
			in.fwd( d );
		}
		Arrays.fill( dst, 0 );
		for ( int j = k1k1; j >= 0; --j )
		{
			final float w = kernel[ j ];
			final int o = k1k1 - j;
			for ( int i = 0; i < n; ++i )
				dst[ i ] += w * src[ i + o ];
		}
		for ( int i = 0; i < n; ++i )
		{
			out.get().setReal( dst[ i ] );
			out.fwd( d );
		}
	}
}
