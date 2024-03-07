/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2023 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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
import net.imglib2.blocks.PrimitiveBlocks;

/**
 * A {@code BlockProcessor} computes values in a flattened primitive output
 * array from values in a flattened primitive input array.
 * <p>
 * Typically, {@code BlockProcessor} should not be used directly, but
 * wrapped in {@link UnaryBlockOperator} which has the ImgLib2 {@code Type}s
 * corresponding to {@code I}, {@code O}. This helps to avoid mistakes with
 * unchecked (primitive array) type casts.
 *
 * @param <I>
 * 		input primitive array type, e.g., float[]
 * @param <O>
 * 		output primitive array type, e.g., float[]
 */
public interface BlockProcessor< I, O >
{
	Supplier< ? extends BlockProcessor< I, O > > threadSafeSupplier();

	void setTargetInterval( Interval interval );

	long[] getSourcePos();

	int[] getSourceSize();

	// TODO: Its cumbersome to have both getSourcePos()/getSourceSize() *and* getSourceInterval()
	//       Only have getSourcePos()/getSourceSize() ?
	//       Have a modifiable SourceInterval class exposing getSourcePos()/getSourceSize() ?
	Interval getSourceInterval();

	/**
	 * Get a {@code src} array of sufficient size.
	 * <p>
	 * Consecutive calls may return the same array, but don't have to.
	 */
	I getSourceBuffer();

	/**
	 * Compute the {@code dest} array from the {@code src} array.
	 * <p>
	 * {@code src} and {@code dest} are expected to be flattened arrays of the
	 * dimensions specified in {@link #setTargetInterval} and computed in {@link
	 * #getSourceSize}, respectively. The typical sequence is:
	 * <ol>
	 * <li>A given target array {@code dest} with known flattened layout and min
	 * position should be computed.</li>
	 * <li>Call {@link #setTargetInterval}. This will compute the corresponding
	 * {@link #getSourceInterval source interval} (also available as {@link
	 * #getSourcePos}, {@link #getSourceSize}) including {@code
	 * BlockProcessor}-specific transformations, padding, etc.</li>
	 * <li>Fill a {@code src} array (either provided by {@link
	 * #getSourceBuffer}, or otherwise allocated) with the input data (see
	 * {@link PrimitiveBlocks#copy}).</li>
	 * <li>Call {@code compute(src, dest)} to compute the target array.</li>
	 * </ol>
	 * Note, that the {@code src} and {@code dest} arrays may be larger than
	 * implied by {@code setTargetInterval} and {@code getSourceSize}. In that
	 * case the trailing elements are ignored.
	 * <p>
	 * Typically, {@code BlockProcessor} should not be used directly, but
	 * wrapped in {@link UnaryBlockOperator} which has the ImgLib2 {@code Type}s
	 * corresponding to {@code I}, {@code O}.
	 *
	 * @param src
	 * 		flattened primitive array with input values
	 * @param dest
	 * 		flattened primitive array to fill with output values
	 */
	void compute( I src, O dest );

	/**
	 * Returns a {@code BlockProcessor concatenated} such that
	 * <pre>{@code
	 *   concatenated.compute(src, dest);
	 * }</pre>
	 * is equivalent to
	 * <pre>{@code
	 *   this.compute(src, tmp);
	 *   processor.compute(tmp, dest);
	 * }</pre>
	 */
	default < P > BlockProcessor< I, P > andThen( BlockProcessor< O, P > processor )
	{
		return new ConcatenatedBlockProcessor<>( this, processor );
	}
}
