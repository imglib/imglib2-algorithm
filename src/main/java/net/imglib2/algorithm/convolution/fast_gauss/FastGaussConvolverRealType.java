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
package net.imglib2.algorithm.convolution.fast_gauss;

import java.util.Arrays;

import net.imglib2.RandomAccess;
import net.imglib2.algorithm.convolution.LineConvolverFactory;
import net.imglib2.loops.ClassCopyProvider;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;

/**
 * Implementation of {@link LineConvolverFactory} that uses
 * {@link FastGaussCalculator} to calculate a fast Gauss transform.
 *
 * @author Vladimir Ulman
 * @author Matthias Arzt
 */
public class FastGaussConvolverRealType implements LineConvolverFactory< RealType< ? > >
{
	private final FastGaussCalculator.Parameters fc;

	private static final ClassCopyProvider< Runnable > provider = new ClassCopyProvider<>( MyConvolver.class, Runnable.class );

	public FastGaussConvolverRealType( final double sigma )
	{
		this.fc = FastGaussCalculator.Parameters.exact( sigma );
	}

	@Override
	public long getBorderBefore()
	{
		return fc.N + 1;
	}

	@Override
	public long getBorderAfter()
	{
		return fc.N - 1;
	}

	@Override
	public Runnable getConvolver( final RandomAccess< ? extends RealType< ? > > in, final RandomAccess< ? extends RealType< ? > > out, final int d, final long lineLength )
	{
		final Object key = Arrays.asList( in.getClass(), out.getClass(), in.get().getClass(), out.get().getClass() );
		return provider.newInstanceForKey( key, d, fc, in, out, lineLength );
	}

	@Override
	public RealType< ? > preferredSourceType( RealType< ? > targetType )
	{
		return (targetType instanceof DoubleType) ? targetType : new FloatType();
	}

	public static class MyConvolver implements Runnable
	{
		private final int d;

		private final RandomAccess< ? extends RealType< ? > > in;

		private final RandomAccess< ? extends RealType< ? > > out;

		private final long lineLength;

		private final FastGaussCalculator fg;

		private final int offset;

		private final double[] tmpE;

		public MyConvolver( final int d, final FastGaussCalculator.Parameters fc, final RandomAccess< ? extends RealType< ? > > in, final RandomAccess< ? extends RealType< ? > > out, final long lineLength )
		{
			if ( lineLength > Integer.MAX_VALUE )
				throw new UnsupportedOperationException();
			this.d = d;
			this.in = in;
			this.out = out;
			this.lineLength = lineLength;
			this.offset = 2 * fc.N;
			this.tmpE = new double[ ( int ) lineLength + offset ];
			this.fg = new FastGaussCalculator( fc );
		}

		@Override
		public void run()
		{
			// left boundary: x=-Nm1, such that tmp = I(0) + I(0),
			// but we say x=-N to simplify the following code block a bit
			for ( int i = 0; i < lineLength + offset; ++i )
			{
				tmpE[ i ] = in.get().getRealDouble(); // backward edge + forward edge
				in.fwd( d );
			}

			// cache...
			final double boundaryValue = tmpE[ 0 ];

			fg.initialize( boundaryValue );
			for ( int i = -offset; i < 0; ++i )
			{
				fg.update( boundaryValue + tmpE[ i + offset ] );
			}

			for ( int i = 0; i < lineLength; ++i )
			{
				fg.update( tmpE[ i ] + tmpE[ i + offset ] );
				out.get().setReal( fg.getValue() );
				out.fwd( d );
			}
		}
	}
}
