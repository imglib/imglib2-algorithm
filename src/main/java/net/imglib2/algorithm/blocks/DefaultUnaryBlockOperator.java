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

public class DefaultUnaryBlockOperator< S extends NativeType< S >, T extends NativeType< T > > implements UnaryBlockOperator< S, T >
{
	private final S sourceType;
	private final T targetType;
	private final BlockProcessor< ?, ? > blockProcessor;

	public DefaultUnaryBlockOperator( S sourceType, T targetType, BlockProcessor< ?, ? > blockProcessor )
	{
		this.sourceType = sourceType;
		this.targetType = targetType;
		this.blockProcessor = blockProcessor;
	}

	@Override
	public < I, O > BlockProcessor< I, O > blockProcessor()
	{
		return Cast.unchecked( blockProcessor );
	}

	@Override
	public S getSourceType()
	{
		return sourceType;
	}

	@Override
	public T getTargetType()
	{
		return targetType;
	}

	@Override
	public UnaryBlockOperator< S, T > threadSafe()
	{
		final Supplier< ? extends BlockProcessor< ?, ? > > processorSupplier = blockProcessor.threadSafeSupplier();
		return new UnaryBlockOperator< S, T >()
		{
			@Override
			public < I, O > BlockProcessor< I, O > blockProcessor()
			{
				return Cast.unchecked( processorSupplier.get() );
			}

			@Override
			public S getSourceType()
			{
				return sourceType;
			}

			@Override
			public T getTargetType()
			{
				return targetType;
			}

			@Override
			public UnaryBlockOperator< S, T > threadSafe()
			{
				return this;
			}
		};
	}

}
