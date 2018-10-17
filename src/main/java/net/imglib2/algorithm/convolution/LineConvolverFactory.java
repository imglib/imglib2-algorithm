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
