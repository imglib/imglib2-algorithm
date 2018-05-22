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

package net.imglib2.algorithm.convolution.kernel;

import net.imglib2.RandomAccess;
import net.imglib2.algorithm.convolution.ConvolverFactory;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;

/**
 * A 1-dimensional line convolver that operates on all {@link NumericType} with
 * {@link NativeType} representation. It implemented using a shifting window
 * buffer that is stored in a small {@link NativeType} image.
 *
 * @author Tobias Pietzsch
 * @see ConvolverFactory
 * 
 * @param <T>
 *            input and output type
 */
public final class ConvolverNativeType< T extends NumericType< T > & NativeType< T > > implements Runnable
{

	final private double[] kernel;

	final private RandomAccess< ? extends T > in;

	final private RandomAccess< ? extends T > out;

	final private int d;

	private final int k1k;

	final private int k1k1;

	final private long linelen;

	final T b1;

	final T b2;

	final T tmp;

	public ConvolverNativeType(final Kernel1D kernel, final RandomAccess< ? extends T > in, final RandomAccess< ? extends T > out, final int d, final long lineLength, final T type )
	{
		// NB: This constructor is used in ConvolverFactories. It needs to be public and have this exact signature.
		this.in = in;
		this.out = out;
		this.d = d;
		this.kernel = kernel.fullKernel().clone();

		k1k = this.kernel.length;
		k1k1 = k1k - 1;
		linelen = lineLength;

		final ArrayImg< T, ? > buf = new ArrayImgFactory< T >().create( new long[] {k1k}, type );
		b1 = buf.randomAccess().get();
		b2 = buf.randomAccess().get();

		tmp = type.createVariable();
	}

	private void prefill()
	{
		// add new values
		final T w = in.get();
		process(w);
		in.fwd( d );
	}

	private void next()
	{
		// add new values
		final T w = in.get();
		process(w);
		in.fwd( d );
		b1.updateIndex( 0 );
		out.get().set( b1 );
		out.fwd( d );
	}

	private void process(T w)
	{
		// move buf contents down
		for ( int i = 0; i < k1k1; ++i )
		{
			b2.updateIndex( i + 1 );
			b1.updateIndex( i );
			b1.set( b2 );
		}

		b1.updateIndex(k1k1);
		b1.setZero();

		// loop
		for (int j = 0; j < k1k; ++j )
		{
			tmp.set( w );
			tmp.mul( kernel[ j ] );
			b1.updateIndex( j );
			b1.add( tmp );
		}
	}

	@Override
	public void run()
	{
		for ( int i = 0; i < k1k1; ++i )
			prefill();
		for ( long i = 0; i < linelen; ++i )
			next();
	}
}
