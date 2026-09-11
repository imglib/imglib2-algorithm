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
package net.imglib2.algorithm.blocks;

import java.util.function.Supplier;

import net.imglib2.Interval;
import net.imglib2.Volatile;
import net.imglib2.blocks.VolatileArray;
import net.imglib2.blocks.VolatilePrimitiveBlocks.AllValid;
import net.imglib2.type.NativeType;
import net.imglib2.util.CloseableThreadLocal;

/**
 * Boilerplate for {@link VolatileBlockSupplier} to simplify implementations.
 * <p>
 * Implements {@link VolatileBlockSupplier#threadSafe()} as a wrapper that makes
 * {@link ThreadLocal} {@link VolatileBlockSupplier#independentCopy()} copies.
 */
public abstract class AbstractVolatileBlockSupplier< T extends Volatile< ? > & NativeType< T > > implements VolatileBlockSupplier< T >
{
	private Supplier< VolatileBlockSupplier< T > > threadSafeSupplier;

	@Override
	public VolatileBlockSupplier< T > threadSafe()
	{
		if ( threadSafeSupplier == null )
			threadSafeSupplier = CloseableThreadLocal.withInitial( this::independentCopy )::get;
		return new VolatileBlockSupplier< T >()
		{
			@Override
			public T getType()
			{
				return AbstractVolatileBlockSupplier.this.getType();
			}

			@Override
			public int numDimensions()
			{
				return AbstractVolatileBlockSupplier.this.numDimensions();
			}

			@Override
			public void copy( final Interval interval, final VolatileArray< ? > dest )
			{
				threadSafeSupplier.get().copy( interval, dest );
			}

			@Override
			public AllValid allValid( final Interval interval, final boolean failFast )
			{
				return threadSafeSupplier.get().allValid( interval, failFast );
			}

			@Override
			public VolatileBlockSupplier< T > independentCopy()
			{
				return AbstractVolatileBlockSupplier.this.independentCopy().threadSafe();
			}

			@Override
			public VolatileBlockSupplier< T > threadSafe()
			{
				return this;
			}
		};
	}
}
