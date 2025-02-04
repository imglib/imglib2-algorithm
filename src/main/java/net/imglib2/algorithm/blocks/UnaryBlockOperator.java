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
import net.imglib2.algorithm.blocks.convert.Convert;
import net.imglib2.type.NativeType;

/**
 * A {@code UnaryBlockOperator} computes blocks of {@code NativeType<T>} data
 * from blocks of {@code NativeType<S>} data.
 * <p>
 * Use the {@link UnaryBlockOperator#compute(BlockSupplier, Interval, Object)}
 * method to compute a block of data and store it into a flat primitive array
 * (of the appropriate primitive type corresponding to {@code T}). The input
 * block (appropriately sized and positioned to produce the desired output
 * {@code Interval}) is read from a {@code BlockSupplier<S>}.
 * <p>
 * Typically, {@code UnaryBlockOperator} are chained to a particular {@code
 * BlockSupplier<S>} using {@link BlockSupplier#andThen} (or {@link #applyTo},
 * instead of calling {@code compute()} explicitly.
 * <p>
 * Implementations are not thread-safe in general. Use {@link
 * #independentCopy()} to obtain independent instances.
 *
 * @param <S>
 * 		source pixel type
 * @param <T>
 * 		target pixel type
 */
public interface UnaryBlockOperator< S extends NativeType< S >, T extends NativeType< T > >
{
	/**
	 * Compute a block (using {@code src} to provide input data) and store into the given primitive array (of the appropriate type).
	 *
	 * @param src
	 * 		the {@code BlockSupplier} that provides input data
	 * @param interval
	 * 		the output interval to compute
	 * @param dest
	 * 		primitive array to store computation result. Must correspond to {@code T}, for
	 * 		example, if {@code T} is {@code UnsignedByteType} then {@code dest} must
	 * 		be {@code byte[]}.
	 */
	void compute( BlockSupplier< S > src, Interval interval, Object dest );

	S getSourceType();

	T getTargetType();

	/**
	 * Number of source dimensions.
	 * <p>
	 * Some operators can be applied to arbitrary dimensions, e.g., converters.
	 * In this case, they should return {@code numSourceDimensions() <= 0}. It
	 * is expected that these operators do not modify the number of dimensions,
	 * that is, when such an operator is applied to a 3D block, the result is
	 * also a 3D block.
	 *
	 * @return the number of source dimensions
	 */
	int numSourceDimensions();

	/**
	 * Number of target dimensions.
	 * <p>
	 * Some operators can be applied to arbitrary dimensions, e.g., converters.
	 * In this case, they should return {@code numTargetDimensions() <= 0}. It
	 * is expected that these operators do not modify the number of dimensions,
	 * that is, when such an operator is applied to a 3D block, the result is
	 * also a 3D block.
	 *
	 * @return the number of target dimensions
	 */
	int numTargetDimensions();

	/**
	 * Returns an instance of this {@link UnaryBlockOperator} that can be used
	 * independently, e.g., in another thread or in another place in the same
	 * pipeline.
	 */
	UnaryBlockOperator< S, T > independentCopy();

	/**
	 * Returns a {@code BlockSupplier<T>} that provide blocks by {@link #compute
	 * computing} on input blocks from the given {@code BlockSupplier<S>}.
	 */
	default BlockSupplier< T > applyTo( BlockSupplier< S > blocks )
	{
		return new ConcatenatedBlockSupplier<>( blocks, independentCopy() );
	}

	/**
	 * Returns a {@code UnaryBlockOperator} that is equivalent to applying
	 * {@code this}, and then applying {@code op} to the result.
	 */
	default < U extends NativeType< U > > UnaryBlockOperator< S, U > andThen( UnaryBlockOperator< T, U > op )
	{
		return new ConcatenatedUnaryBlockOperator<>( this, op.independentCopy() );
	}

	/**
	 * Returns a {@code UnaryBlockOperator} that is equivalent to converting the
	 * input values to type {@code U} (possibly {@code clamp}ing to the range of
	 * {@code U}). and then applying {@code this} to the result.
	 */
	default < U extends NativeType< U > > UnaryBlockOperator< U, T > adaptSourceType( U newSourceType, ClampType clamp )
	{
		return Convert.createOperator( newSourceType, getSourceType(), clamp ).andThen( this );
	}

	/**
	 * Returns a {@code UnaryBlockOperator} that is equivalent to applying
	 * {@code this}, an then converting the output values to type {@code U}
	 * (possibly {@code clamp}ing to the range of {@code U}).
	 */
	default  < U extends NativeType< U > > UnaryBlockOperator< S, U > adaptTargetType( U newTargetType, ClampType clamp )
	{
		return this.andThen( Convert.createOperator( getTargetType(), newTargetType, clamp ) );
	}
}
