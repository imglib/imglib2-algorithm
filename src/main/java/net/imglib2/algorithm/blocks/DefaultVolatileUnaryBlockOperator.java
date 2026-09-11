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

import net.imglib2.Interval;
import net.imglib2.Volatile;
import net.imglib2.blocks.VolatileArray;
import net.imglib2.blocks.VolatilePrimitiveBlocks.AllValid;
import net.imglib2.type.NativeType;

/**
 * Default implementation of {@link VolatileUnaryBlockOperator}, wrapping a
 * {@code BlockProcessor<VolatileArray<I>, VolatileArray<O>>}. The {@code
 * Volatile} counterpart of {@link DefaultUnaryBlockOperator}.
 *
 * @param <S>
 * 		source type
 * @param <T>
 * 		target type
 */
public class DefaultVolatileUnaryBlockOperator< S extends Volatile< ? > & NativeType< S >, T extends Volatile< ? > & NativeType< T > >
		extends AbstractUnaryBlockOperator< S, T >
		implements VolatileUnaryBlockOperator< S, T >
{
	@SuppressWarnings( "rawtypes" )
	private final BlockProcessor blockProcessor;

	public DefaultVolatileUnaryBlockOperator( S sourceType, T targetType, int numSourceDimensions, int numTargetDimensions, BlockProcessor< ? extends VolatileArray< ? >, ? extends VolatileArray< ? > > blockProcessor )
	{
		super( sourceType, targetType, numSourceDimensions, numTargetDimensions );
		this.blockProcessor = blockProcessor;
	}

	private DefaultVolatileUnaryBlockOperator( DefaultVolatileUnaryBlockOperator< S, T > op )
	{
		super( op );
		this.blockProcessor = op.blockProcessor.independentCopy();
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public void compute( final VolatileBlockSupplier< S > src, final Interval interval, final VolatileArray< ? > dest )
	{
		blockProcessor.setTargetInterval( interval );
		final VolatileArray< ? > buf = ( VolatileArray< ? > ) blockProcessor.getSourceBuffer();
		src.copy( blockProcessor.getSourceInterval(), buf );
		blockProcessor.compute( buf, dest );
	}

	@Override
	public AllValid allValid( final VolatileBlockSupplier< S > src, final Interval interval, final boolean failFast )
	{
		// The BlockProcessor maps the target interval to the source interval it
		// is computed from, which is exactly what has to be valid. This is the
		// same mapping compute() uses, just without the copying.
		blockProcessor.setTargetInterval( interval );
		return src.allValid( blockProcessor.getSourceInterval(), failFast );
	}

	@Override
	public VolatileUnaryBlockOperator< S, T > independentCopy()
	{
		return new DefaultVolatileUnaryBlockOperator<>( this );
	}
}
