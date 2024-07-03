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
package net.imglib2.algorithm.blocks;

import java.util.function.Supplier;

import net.imglib2.type.NativeType;
import net.imglib2.util.Cast;
import net.imglib2.util.CloseableThreadLocal;

class ConcatenatedBlockSupplier< T extends NativeType< T > > implements BlockSupplier< T >
{
	private final BlockSupplier< ? > p0;

	private final BlockProcessor< ?, ? > p1;

	private final T type;

	private final int numDimensions;

	private Supplier< BlockSupplier< T > > threadSafeSupplier;

	public < S extends NativeType< S > > ConcatenatedBlockSupplier(
			final BlockSupplier< S > srcSupplier,
			final UnaryBlockOperator< S, T > operator )
	{
		this.p0 = srcSupplier;
		this.p1 = operator.blockProcessor();
		this.type = operator.getTargetType();
		if ( operator.numSourceDimensions() > 0 )
		{
			if ( srcSupplier.numDimensions() != operator.numSourceDimensions() )
				throw new IllegalArgumentException( "UnaryBlockOperator cannot be concatenated: number of dimensions mismatch." );
			this.numDimensions = operator.numTargetDimensions();
		}
		else
		{
			this.numDimensions = srcSupplier.numDimensions();
		}
	}

	private ConcatenatedBlockSupplier( final ConcatenatedBlockSupplier< T > s )
	{
		p0 = s.p0.independentCopy();
		p1 = s.p1.independentCopy();
		type = s.type;
		numDimensions = s.numDimensions;
	}

	@Override
	public T getType()
	{
		return type;
	}

	@Override
	public int numDimensions()
	{
		return numDimensions;
	}

	@Override
	public void copy( final long[] srcPos, final Object dest, final int[] size )
	{
		p1.setTargetInterval( srcPos, size );
		final Object src = p1.getSourceBuffer();
		p0.copy( p1.getSourcePos(), src, p1.getSourceSize() );
		p1.compute( Cast.unchecked( src ), Cast.unchecked( dest ) );
	}

	@Override
	public BlockSupplier< T > independentCopy()
	{
		return new ConcatenatedBlockSupplier<>( this );
	}

	@Override
	public BlockSupplier< T > threadSafe()
	{
		if ( threadSafeSupplier == null )
			threadSafeSupplier = CloseableThreadLocal.withInitial( this::independentCopy )::get;
		return new BlockSupplier< T >()
		{
			@Override
			public T getType()
			{
				return type;
			}

			@Override
			public int numDimensions()
			{
				return numDimensions;
			}

			@Override
			public void copy( final long[] srcPos, final Object dest, final int[] size )
			{
				threadSafeSupplier.get().copy( srcPos, dest, size );
			}

			@Override
			public BlockSupplier< T > independentCopy()
			{
				return ConcatenatedBlockSupplier.this.independentCopy().threadSafe();
			}

			@Override
			public BlockSupplier< T > threadSafe()
			{
				return this;
			}
		};
	}
}
