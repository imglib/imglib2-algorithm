/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2016 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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

package net.imglib2.algorithm.gauss3;

import net.imglib2.RandomAccess;
import net.imglib2.type.numeric.RealType;

/**
 * A 1-dimensional line convolver that operates on all {@link RealType}. It
 * implemented using a shifting window buffer that is stored in a small float[]
 * array. This is intented for very large images, where a single line has more
 * than {@link Integer#MAX_VALUE} elements. For smaller images, the faster
 * {@link DoubleConvolverRealTypeBuffered} should be used.
 * 
 * @author Tobias Pietzsch
 * @see ConvolverFactory
 * 
 * @param <S>
 *            input type
 * @param <T>
 *            output type
 */
public final class FloatConvolverRealType< S extends RealType< S >, T extends RealType< T > > implements Runnable
{
	/**
	 * @return a {@link ConvolverFactory} producing
	 *         {@link FloatConvolverRealType}.
	 */
	public static < S extends RealType< S >, T extends RealType< T > > ConvolverFactory< S, T > factory()
	{
		return new ConvolverFactory< S, T >()
		{
			@Override
			public Runnable create( final double[] halfkernel, final RandomAccess< S > in, final RandomAccess< T > out, final int d, final long lineLength )
			{
				return new FloatConvolverRealType< S, T >( halfkernel, in, out, d, lineLength );
			}
		};
	}

	final private float[] kernel;

	final private RandomAccess< S > in;

	final private RandomAccess< T > out;

	final private int d;

	final private int k;

	final private int k1;

	final private int k1k1;

	final private int k1k;

	final private long fill2;

	final private boolean fillAdditional;

	final private float[] buf1;

	final private float[] buf2;

	private FloatConvolverRealType( final double[] kernel, final RandomAccess< S > in, final RandomAccess< T > out, final int d, final long lineLength )
	{
		this.kernel = new float[ kernel.length ];
		for ( int i = 0; i < kernel.length; ++i )
			this.kernel[ i ] = ( float ) kernel[ i ];
		this.in = in;
		this.out = out;
		this.d = d;

		k = this.kernel.length;
		k1 = k - 1;
		k1k1 = k1 + k1;
		k1k = k1 + k;
		fill2 = lineLength / 2;
		fillAdditional = ( lineLength % 2 == 1 );

		final int l = 2 * k;
		buf1 = new float[ l ];
		buf2 = new float[ l ];
	}

	private void prefill1()
	{
		final float w = in.get().getRealFloat();
		buf1[ k1 ] = w * kernel[ 0 ] + buf2[ k ];
		for ( int i = 1; i < k1; ++i )
		{
			final float wk = w * kernel[ i ];
			buf1[ k1 + i ] = wk + buf2[ k + i ];
			buf1[ k1 - i ] = wk + buf2[ k - i ];
		}
		buf1[ k1k1 ] = w * kernel[ k1 ] + buf2[ k1k ];
		in.fwd( d );
	}

	private void prefill2()
	{
		final float w = in.get().getRealFloat();
		buf2[ k1 ] = w * kernel[ 0 ] + buf1[ k ];
		for ( int i = 1; i < k1; ++i )
		{
			final float wk = w * kernel[ i ];
			buf2[ k1 + i ] = wk + buf1[ k + i ];
			buf2[ k1 - i ] = wk + buf1[ k - i ];
		}
		buf2[ k1k1 ] = w * kernel[ k1 ] + buf1[ k1k ];
		in.fwd( d );
	}

	private void next2()
	{
		final float w = in.get().getRealFloat();
		buf2[ k1 ] = w * kernel[ 0 ] + buf1[ k ];
		for ( int i = 1; i < k1; ++i )
		{
			final float wk = w * kernel[ i ];
			buf2[ k1 + i ] = wk + buf1[ k + i ];
			buf2[ k1 - i ] = wk + buf1[ k - i ];
		}
		final float wk = w * kernel[ k1 ];
		buf2[ k1k1 ] = wk + buf1[ k1k ];
		out.get().setReal( wk + buf1[ 1 ] );
		in.fwd( d );
		out.fwd( d );
	}

	private void next1()
	{
		final float w = in.get().getRealFloat();
		buf1[ k1 ] = w * kernel[ 0 ] + buf2[ k ];
		for ( int i = 1; i < k1; ++i )
		{
			final float wk = w * kernel[ i ];
			buf1[ k1 + i ] = wk + buf2[ k + i ];
			buf1[ k1 - i ] = wk + buf2[ k - i ];
		}
		final float wk = w * kernel[ k1 ];
		buf1[ k1k1 ] = wk + buf2[ k1k ];
		out.get().setReal( wk + buf2[ 1 ] );
		in.fwd( d );
		out.fwd( d );
	}

	@Override
	public void run()
	{
		for ( int i = 0; i < k1; ++i )
		{
			prefill1();
			prefill2();
		}
		for ( long i = 0; i < fill2; ++i )
		{
			next1();
			next2();
		}
		if ( fillAdditional )
			next1();
	}
}
