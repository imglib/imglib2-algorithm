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

import net.imglib2.Interval;
import net.imglib2.type.NativeType;
import net.imglib2.util.Cast;

/**
 * Default implementation of {@link UnaryBlockOperator}.
 * <p>
 * If you implement a {@code BlockProcessor<I,O>}, you typically wrap it with a
 * {@code DefaultUnaryBlockOperator}, specifying the source {@code S} and target
 * {@code T} ImgLib2 {@code NativeType}s corresponding to primitive array types
 * {@code I} and {@code O}, and the number of source and target dimension.
 * <p>
 * The {@link UnaryBlockOperator} can then be used in {@link
 * BlockSupplier#andThen(UnaryBlockOperator)} chains in a type- and
 * dimensionality-safe manner.
 *
 * @param <S>
 * 		source type
 * @param <T>
 * 		target type
 */
public class DefaultUnaryBlockOperator< S extends NativeType< S >, T extends NativeType< T > > extends AbstractUnaryBlockOperator< S, T >
{
	@SuppressWarnings( "rawtypes" )
	private final BlockProcessor blockProcessor;

	public DefaultUnaryBlockOperator( S sourceType, T targetType, int numSourceDimensions, int numTargetDimensions, BlockProcessor< ?, ? > blockProcessor )
	{
		super( sourceType, targetType, numSourceDimensions, numTargetDimensions );
		this.blockProcessor = blockProcessor;
	}

	private DefaultUnaryBlockOperator( DefaultUnaryBlockOperator< S, T > op )
	{
		super( op );
		this.blockProcessor = op.blockProcessor.independentCopy();
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public void compute( final BlockSupplier< S > src, final Interval interval, final Object dest )
	{
		blockProcessor.setTargetInterval( interval );
		final Object buf = blockProcessor.getSourceBuffer();
		src.copy( blockProcessor.getSourceInterval(), buf );
		blockProcessor.compute( buf, dest );
	}

	@Override
	public UnaryBlockOperator< S, T > independentCopy()
	{
		return new DefaultUnaryBlockOperator<>( this );
	}

	/**
	 * Get (an instance of) the wrapped {@code BlockProcessor}.
	 * <p>
	 * Note that this involves an unchecked cast, that is, the returned {@code
	 * BlockProcessor<I,O>} will be cast to the {@code I, O} types expected by
	 * the caller.
	 * <p>
	 * This is mostly intended for debugging.
	 *
	 * @param <I>
	 * 		input primitive array type, e.g., float[]. Must correspond to S.
	 * @param <O>
	 * 		output primitive array type, e.g., float[]. Must correspond to T.
	 *
	 * @return an instance of the wrapped {@code BlockProcessor}
	 */
	public < I, O > BlockProcessor< I, O > blockProcessor()
	{
		return Cast.unchecked( blockProcessor );
	}
}
