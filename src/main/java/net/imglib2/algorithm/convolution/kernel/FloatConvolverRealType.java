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
import net.imglib2.type.numeric.RealType;

/**
 * A 1-dimensional line convolver that operates on all {@link RealType}. It
 * implemented using a shifting window buffer that is stored in a small double[]
 * array.
 *
 * @author Tobias Pietzsch
 * @see ConvolverFactory
 * 
 */
public final class FloatConvolverRealType implements Runnable
{

	final private float[] kernel;

	final private RandomAccess< ? extends RealType<?> > in;

	final private RandomAccess< ? extends RealType<?> > out;

	final private int d;

	final private int k1k1;

	final private int k1k;

	final private long fill2;

	final private float[] buffer;

	public FloatConvolverRealType(final Kernel1D kernel, final RandomAccess< ? extends RealType<?> > in, final RandomAccess< ? extends RealType<?> > out, final int d, final long lineLength, Object dummy )
	{
		// NB: This constructor is used in ConvolverFactories. It needs to be public and have this exact signature.
		this.in = in;
		this.out = out;
		this.d = d;
		this.kernel = doubleToFloat(kernel.fullKernel());

		k1k = this.kernel.length;
		k1k1 = k1k - 1;
		fill2 = lineLength;
		buffer = new float[ k1k + 1 ];
	}

	private float[] doubleToFloat(double[] in) {
		float[] out = new float[in.length];
		for (int i = 0; i < in.length; i++) out[i] = (float) in[i];
		return out;
	}

	private void prefill()
	{
		final float w = in.get().getRealFloat();
		process(w);
		in.fwd( d );
	}

	private void next()
	{
		final float w = in.get().getRealFloat();
		out.get().setReal( w * kernel[ 0 ] + buffer[ 1 ] );
		process(w);
		in.fwd( d );
		out.fwd( d );
	}

	private void process(float w) {
		for ( int i = 1; i < k1k; ++i )
			buffer[ i ] = w * kernel[ i ] + buffer[ i + 1 ];
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
