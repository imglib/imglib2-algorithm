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
package net.imglib2.algorithm.convolution;

import net.imglib2.RandomAccess;
import net.imglib2.type.Type;

/**
 * The interesting part of a separable convolution is how one line of the image
 * is convolved. An implementation of {@link LineConvolverFactory} needs to do
 * exactly this: Convolve one line of the image.
 * <p>
 * All the "boring" stuff is done by {@link LineConvolution}, which takes care
 * of applying {@link LineConvolverFactory} in a multi threaded fashion on an
 * complete image.
 */
public interface LineConvolverFactory< T >
{
	/**
	 * When doing a convolution, a neighborhood of a pixel needs to be known.
	 * For line convolution, neighborhood means pixels before and after.
	 *
	 * @return How many pixels are needed before.
	 */
	long getBorderBefore();

	/**
	 * When doing a convolution, a neighborhood of a pixel needs to be known.
	 * For line convolution, neighborhood means pixels before and after.
	 *
	 * @return How many pixels are needed after.
	 */
	long getBorderAfter();

	/**
	 * The {@link Runnable} returned by this method is responsible for
	 * convolving on line of the image. But it will be reused. After the first
	 * line is convolved. The {@link RandomAccess}es are moved to the start of
	 * the next line. And the Runnable is executed again.
	 * {@link LineConvolution} works with multiple threads.
	 * {@link #getConvolver} is called once per thread, and the returned
	 * {@link Runnable} is exclusively used in one thread.
	 *
	 * @param in
	 *            {@link RandomAccess}, that is positioned on the start of the
	 *            line of the input image.
	 * @param out
	 *            {@link RandomAccess}, that is positioned on the start of the
	 *            line of the output image.
	 * @param d
	 *            Dimension in which the line should go.
	 * @param lineLength
	 *            Length of the output line in pixels.
	 * @return A {@link Runnable} that will read a line of the input image,
	 *         convolve it, and write the result to the output image. (Input
	 *         line length should be getBorderBefore() + lineLength +
	 *         getBorderAfter())
	 */
	Runnable getConvolver( RandomAccess< ? extends T > in, RandomAccess< ? extends T > out, int d, long lineLength );

	T preferredSourceType( T targetType );
}
