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

import java.lang.reflect.Array;

import net.imglib2.RandomAccess;
import net.imglib2.algorithm.convolution.ConvolverFactory;
import net.imglib2.type.numeric.NumericType;

/**
 * A 1-dimensional line convolver that operates on all {@link NumericType}. It
 * implemented using a shifting window buffer that is stored in a T[] array.
 * 
 * @author Tobias Pietzsch
 * @see ConvolverFactory
 * 
 * @param <T>
 *            input and output type
 */
public class ConvolverNumericType< T extends NumericType< T > > implements Runnable
{

	final private double[] kernel;

	final private RandomAccess< ? extends T > in;

	final private RandomAccess< ? extends T > out;

	final private int d;

	final private int k1k1;

	final private int k1k;

	final private long fill2;

	final private T[] buffer;

	final private T tmp;

	@SuppressWarnings( "unchecked" )
	public ConvolverNumericType(final Kernel1D kernel, final RandomAccess< ? extends T > in, final RandomAccess< ? extends T > out, final int d, final long lineLength, final T type )
	{
		// NB: This constructor is used in ConvolverFactories. It needs to be public and have this exact signature.
		this.in = in;
		this.out = out;
		this.d = d;
		this.kernel = kernel.fullKernel().clone();

		k1k = this.kernel.length;
		k1k1 = k1k - 1;
		fill2 = lineLength;

		buffer = ( T[] ) Array.newInstance( type.getClass(), k1k + 1);
		for ( int i = 0; i < k1k + 1; ++i )
			buffer[ i ] = type.createVariable();

		tmp = type.createVariable();
	}

	private void prefill()
	{
		tmp.set(in.get());
		process(tmp);
		in.fwd( d );
	}

	private void next()
	{
		tmp.set(in.get());
		final T t = buffer[0];
		t.set(tmp);
		t.mul(kernel[0]);
		t.add(buffer[1]);
		out.get().set(t);
		process(tmp);
		in.fwd( d );
		out.fwd( d );
	}

	private void process(T tmp) {
		for ( int i = 1; i < k1k; ++i ) {
			final T t = buffer[i];
			t.set(tmp);
			t.mul(kernel[i]);
			t.add(buffer[i+1]);
		}
	}

	@Override
	public void run()
	{
		for ( int i = 0; i < k1k1; ++i )
			prefill();
		for ( long i = 0; i < fill2; ++i )
			next();
	}
}
