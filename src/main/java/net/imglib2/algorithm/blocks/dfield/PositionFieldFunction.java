/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2025 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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
package net.imglib2.algorithm.blocks.dfield;

import net.imglib2.algorithm.blocks.BlockSupplier;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

/**
 * A function that maps position vectors to {@code T}.
 *
 * @param <D>
 *     position component type. should be {@code DoubleType} or {@code FloatType}
 * @param <T>
 * 		target pixel type
 * @param <F>
 * 		corresponding position field array type (must be float[] or double[])
 * @param <P>
 * 		corresponding target primitive array type (i.e., float[] or double[])
 */
public interface PositionFieldFunction< D extends NativeType< D > & RealType< D >, T extends NativeType< T >, F, P >
{
	/**
	 * Compute a block of target values from a block of position vectors.
	 * (The components of the position vectors are flattened in dimension 0.)
	 *
	 * @param dest
	 * 		block of output values to fill
	 * @param length
	 * 		number of output values (and number of positionField vectors)
	 * @param positionField
	 * 		position field block
	 * @param positionOffset
	 * 		offset to add to {@code positionField} vectors
	 */
	void compute( P dest, int length, F positionField, double[] positionOffset );

	/**
	 * Returns an instance of this {@link PositionFieldFunction} that can be
	 * used independently, e.g., in another thread.
	 */
	PositionFieldFunction< D, T, F, P > independentCopy();

	/**
	 * Returns an instance of the target pixel type.
	 *
	 * @return target pixel type
	 */
	T getType();
}
