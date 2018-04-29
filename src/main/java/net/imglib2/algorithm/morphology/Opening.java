/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2016 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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
package net.imglib2.algorithm.morphology;

import java.util.List;

import net.imglib2.Interval;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;
import net.imglib2.view.ExtendedRandomAccessibleInterval;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

/**
 * Morphological opening for ImgLib2. See <a
 * href="http://en.wikipedia.org/wiki/Opening_(morphology)">Opening_(
 * morphology)</a>.
 * 
 * @author Jean-Yves Tinevez - 2014
 */
public class Opening
{
	/**
	 * Performs the morphological opening operation on a {@link RealType}
	 * {@link Img}, using a list of {@link Shape}s as a structuring element. See
	 * <a href="http://en.wikipedia.org/wiki/Opening_(morphology)">Opening_(
	 * morphology)</a>.
	 * <p>
	 * The opening operation is simply an erosion followed by a dilation.
	 * <p>
	 * The structuring element is specified through a list of {@link Shape}s, to
	 * allow for performance optimization through structuring element
	 * decomposition. Each shape is processed in order as given in the list. If
	 * the list is empty, the source image is returned.
	 * 
	 * @param source
	 *            the {@link Img} to operate on.
	 * @param strels
	 *            the list of {@link Shape}s that serves as a structuring
	 *            element.
	 * @param numThreads
	 *            the number of threads to use for calculation.
	 * @param <T>
	 *            the type of the source image and the result image. Must
	 *            extends {@link RealType}.
	 * @return an {@link Img} of the same type and same dimensions that of the
	 *         source.
	 */
	public static final < T extends RealType< T >> Img< T > open( final Img< T > source, final List< Shape > strels, final int numThreads )
	{
		final Img< T > eroded = Erosion.erode( source, strels, numThreads );
		final Img< T > dilated = Dilation.dilate( eroded, strels, numThreads );
		return dilated;
	}

	/**
	 * Performs the morphological opening operation on an {@link Img} of
	 * {@link Comparable} , using a list of {@link Shape}s as a structuring
	 * element. See <a href="http://en.wikipedia.org/wiki/Opening_(morphology)"
	 * >Opening_(morphology)</a>.
	 * <p>
	 * The opening operation is simply an erosion followed by a dilation.
	 * <p>
	 * The structuring element is specified through a list of {@link Shape}s, to
	 * allow for performance optimization through structuring element
	 * decomposition. Each shape is processed in order as given in the list. If
	 * the list is empty, the source image is returned.
	 * <p>
	 * This method relies on a specified minimal and maximal value to start
	 * comparing to other pixels in the neighborhood. For this code to perform
	 * properly, it is sufficient that the specified min value is smaller
	 * (against {@link Comparable}) than any of the value found in the source
	 * image, and the converse for the max value. These normally unseen
	 * parameters are required to operate on
	 * {@code T extends Comparable & Type}.
	 * 
	 * @param source
	 *            the {@link Img} to operate on.
	 * @param strels
	 *            the list of {@link Shape}s that serves as a structuring
	 *            element.
	 * @param minVal
	 *            a T containing set to a value smaller than any of the values
	 *            in the source {@link Img} (against {@link Comparable}).
	 * @param maxVal
	 *            a T containing set to a value larger than any of the values in
	 *            the source {@link Img} (against {@link Comparable}).
	 * @param numThreads
	 *            the number of threads to use for calculation.
	 * @param <T>
	 *            the type of the source image and the result. Must be a
	 *            sub-type of {@code T extends Comparable & Type}.
	 * @return an {@link Img} of the same type and same dimensions that of the
	 *         source.
	 */
	public static final < T extends Type< T > & Comparable< T > > Img< T > open( final Img< T > source, final List< Shape > strels, final T minVal, final T maxVal, final int numThreads )
	{
		final Img< T > eroded = Erosion.erode( source, strels, maxVal, numThreads );
		final Img< T > dilated = Dilation.dilate( eroded, strels, minVal, numThreads );
		return dilated;
	}

	/**
	 * Performs the morphological opening operation on a {@link RealType}
	 * {@link Img}, using a {@link Shape} as a structuring element. See <a
	 * href="http://en.wikipedia.org/wiki/Opening_(morphology)"
	 * >Opening_(morphology)</a>.
	 * <p>
	 * The opening operation is simply an erosion followed by a dilation.
	 * 
	 * @param source
	 *            the {@link Img} to operate on.
	 * @param strel
	 *            the {@link Shape} that serves as a structuring element.
	 * @param numThreads
	 *            the number of threads to use for calculation.
	 * @param <T>
	 *            the type of the source image and the result image. Must
	 *            extends {@link RealType}.
	 * @return an {@link Img} of the same type and same dimensions that of the
	 *         source.
	 */
	public static final < T extends RealType< T >> Img< T > open( final Img< T > source, final Shape strel, final int numThreads )
	{
		final Img< T > eroded = Erosion.erode( source, strel, numThreads );
		final Img< T > dilated = Dilation.dilate( eroded, strel, numThreads );
		return dilated;
	}


	/**
	 * Performs the morphological opening operation on an {@link Img} of
	 * {@link Comparable} , using a {@link Shape} as a structuring element. See
	 * <a href="http://en.wikipedia.org/wiki/Opening_(morphology)"
	 * >Opening_(morphology)</a>.
	 * <p>
	 * The opening operation is simply an erosion followed by a dilation.
	 * <p>
	 * This method relies on a specified minimal and maximal value to start
	 * comparing to other pixels in the neighborhood. For this code to perform
	 * properly, it is sufficient that the specified min value is smaller
	 * (against {@link Comparable}) than any of the value found in the source
	 * image, and the converse for the max value. These normally unseen
	 * parameters are required to operate on
	 * {@code T extends Comparable & Type}.
	 * 
	 * @param source
	 *            the {@link Img} to operate on.
	 * @param strel
	 *            the {@link Shape} that serves as a structuring element.
	 * @param minVal
	 *            a T containing set to a value smaller than any of the values
	 *            in the source {@link Img} (against {@link Comparable}).
	 * @param maxVal
	 *            a T containing set to a value larger than any of the values in
	 *            the source {@link Img} (against {@link Comparable}).
	 * @param numThreads
	 *            the number of threads to use for calculation.
	 * @param <T>
	 *            the type of the source image and the result. Must be a
	 *            sub-type of {@code T extends Comparable & Type}.
	 * @return an {@link Img} of the same type and same dimensions that of the
	 *         source.
	 */
	public static final < T extends Type< T > & Comparable< T > > Img< T > open( final Img< T > source, final Shape strel, final T minVal, final T maxVal, final int numThreads )
	{
		final Img< T > eroded = Erosion.erode( source, strel, maxVal, numThreads );
		final Img< T > dilated = Dilation.dilate( eroded, strel, minVal, numThreads );
		return dilated;
	}

	/**
	 * Performs the morphological opening operation on a {@link RealType} source
	 * {@link RandomAccessible}, using a list of {@link Shape}s as a structuring
	 * element, and writes the result on a specified target which must be an
	 * {@link IterableInterval}. See <a
	 * href="http://en.wikipedia.org/wiki/Opening_(morphology)"
	 * >Opening_(morphology)</a>.
	 * <p>
	 * The opening operation is simply an erosion followed by a dilation.
	 * <p>
	 * <b>Careful: Target must point to a different structure than source.</b>
	 * In place operation will not work but will not generate an error.
	 * <p>
	 * It is the caller responsibility to ensure that the source is sufficiently
	 * padded to properly cover the target range plus the shape size. See
	 * <i>e.g.</i> {@link Views#extendValue(RandomAccessibleInterval, Type)}
	 * <p>
	 * It is limited to flat structuring elements, only having
	 * {@code on/off} pixels, contrary to grayscale structuring elements.
	 * This allows to simply use a {@link Shape} as a type for these structuring
	 * elements.
	 * <p>
	 * The structuring element is specified through a list of {@link Shape}s, to
	 * allow for performance optimization through structuring element
	 * decomposition. Each shape is processed in order as given in the list. If
	 * the list is empty, the target is left untouched.
	 * 
	 * @param source
	 *            the {@link RandomAccessible} to operate on.
	 * @param target
	 *            the {@link IterableInterval} to write the results on.
	 * @param strels
	 *            the structuring element, as a list of {@link Shape}s.
	 * @param numThreads
	 *            the number of threads to use for calculation.
	 * @param <T>
	 *            the type of the source and the result. Must extends
	 *            {@link RealType}.
	 */
	public static < T extends RealType< T > > void open( final RandomAccessible< T > source, final IterableInterval< T > target, final List< Shape > strels, final int numThreads )
	{
		final T maxVal = MorphologyUtils.createVariable( source, target );
		maxVal.setReal( maxVal.getMaxValue() );
		final T minVal = MorphologyUtils.createVariable( source, target );
		minVal.setReal( minVal.getMinValue() );
		open( source, target, strels, minVal, maxVal, numThreads );
	}

	/**
	 * Performs the morphological opening operation on a source
	 * {@link RandomAccessible}, using a list of {@link Shape}s as a structuring
	 * element, and writes the result on a specified target which must be an
	 * {@link IterableInterval}. See <a
	 * href="http://en.wikipedia.org/wiki/Opening_(morphology)"
	 * >Opening_(morphology)</a>.
	 * <p>
	 * The opening operation is simply an erosion followed by a dilation.
	 * <p>
	 * <b>Careful: Target must point to a different structure than source.</b>
	 * In place operation will not work but will not generate an error.
	 * <p>
	 * It is the caller responsibility to ensure that the source is sufficiently
	 * padded to properly cover the target range plus the shape size. See
	 * <i>e.g.</i> {@link Views#extendValue(RandomAccessibleInterval, Type)}
	 * <p>
	 * It is limited to flat structuring elements, only having
	 * {@code on/off} pixels, contrary to grayscale structuring elements.
	 * This allows to simply use a {@link Shape} as a type for these structuring
	 * elements.
	 * <p>
	 * The structuring element is specified through a list of {@link Shape}s, to
	 * allow for performance optimization through structuring element
	 * decomposition. Each shape is processed in order as given in the list. If
	 * the list is empty, the target is left untouched.
	 * <p>
	 * This method relies on specified minimal and maximal values to start
	 * comparing to other pixels in the neighborhood. For this code to properly
	 * perform opening, it is sufficient that the specified max value is larger
	 * (against {@link Comparable}) than any of the value found in the source
	 * image, and conversely for the min value. These normally unseen parameters
	 * are required to operate on
	 * {@code T extends Comparable & Type}.
	 * 
	 * @param source
	 *            the {@link RandomAccessible} to operate on.
	 * @param target
	 *            the {@link IterableInterval} to write the results on.
	 * @param strels
	 *            the structuring element, as a list of {@link Shape}s.
	 * @param minVal
	 *            a T containing set to a value smaller than any of the values
	 *            in the source (against {@link Comparable}).
	 * @param maxVal
	 *            a T containing set to a value larger than any of the values in
	 *            the source (against {@link Comparable}).
	 * @param numThreads
	 *            the number of threads to use for calculation.
	 * @param <T>
	 *            the type of the source and the result. Must extends
	 *            {@code Compparable}.
	 */
	public static < T extends Type< T > & Comparable< T > > void open( final RandomAccessible< T > source, final IterableInterval< T > target, final List< Shape > strels, final T minVal, final T maxVal, final int numThreads )
	{
		// Create temp image
		final ImgFactory< T > factory = Util.getSuitableImgFactory( target, maxVal );
		final Img< T > img = factory.create( target );
		final long[] min = new long[ target.numDimensions() ];
		target.min( min );

		final IntervalView< T > translated = Views.translate( img, min );
		Erosion.erode( source, translated, strels, maxVal, numThreads );

		final ExtendedRandomAccessibleInterval< T, IntervalView< T >> extended = Views.extendValue( translated, minVal );
		Dilation.dilate( extended, target, strels, minVal, numThreads );
	}

	/**
	 * Performs the morphological opening operation on a {@link RealType} source
	 * {@link RandomAccessible}, using a {@link Shape} as a structuring element,
	 * and writes the result on a specified target which must be an
	 * {@link IterableInterval}. See <a
	 * href="http://en.wikipedia.org/wiki/Opening_(morphology)"
	 * >Opening_(morphology)</a>.
	 * <p>
	 * The opening operation is simply an erosion followed by a dilation.
	 * <p>
	 * <b>Careful: Target must point to a different structure than source.</b>
	 * In place operation will not work but will not generate an error.
	 * <p>
	 * It is the caller responsibility to ensure that the source is sufficiently
	 * padded to properly cover the target range plus the shape size. See
	 * <i>e.g.</i> {@link Views#extendValue(RandomAccessibleInterval, Type)}
	 * <p>
	 * It is limited to flat structuring elements, only having
	 * {@code on/off} pixels, contrary to grayscale structuring elements.
	 * This allows to simply use a {@link Shape} as a type for these structuring
	 * elements.
	 * 
	 * @param source
	 *            the {@link RandomAccessible} to operate on.
	 * @param target
	 *            the {@link IterableInterval} to write the results on.
	 * @param strel
	 *            the {@link Shape} that serves as a structuring element.
	 * @param numThreads
	 *            the number of threads to use for calculation.
	 * @param <T>
	 *            the type of the source and the result. Must extends
	 *            {@link RealType}.
	 */
	public static < T extends RealType< T > > void open( final RandomAccessible< T > source, final IterableInterval< T > target, final Shape strel, final int numThreads )
	{
		final T maxVal = MorphologyUtils.createVariable( source, target );
		maxVal.setReal( maxVal.getMaxValue() );
		final T minVal = MorphologyUtils.createVariable( source, target );
		minVal.setReal( minVal.getMinValue() );
		open( source, target, strel, minVal, maxVal, numThreads );
	}

	/**
	 * Performs the morphological opening operation on a source
	 * {@link RandomAccessible}, using a {@link Shape} as a structuring element,
	 * and writes the result on a specified target which must be an
	 * {@link IterableInterval}. See <a
	 * href="http://en.wikipedia.org/wiki/Opening_(morphology)"
	 * >Opening_(morphology)</a>.
	 * <p>
	 * The opening operation is simply an erosion followed by a dilation.
	 * <p>
	 * <b>Careful: Target must point to a different structure than source.</b>
	 * In place operation will not work but will not generate an error.
	 * <p>
	 * It is the caller responsibility to ensure that the source is sufficiently
	 * padded to properly cover the target range plus the shape size. See
	 * <i>e.g.</i> {@link Views#extendValue(RandomAccessibleInterval, Type)}
	 * <p>
	 * It is limited to flat structuring elements, only having
	 * {@code on/off} pixels, contrary to grayscale structuring elements.
	 * This allows to simply use a {@link Shape} as a type for these structuring
	 * elements.
	 * <p>
	 * This method relies on specified minimal and maximal values to start
	 * comparing to other pixels in the neighborhood. For this code to properly
	 * perform opening, it is sufficient that the specified max value is larger
	 * (against {@link Comparable}) than any of the value found in the source
	 * image, and conversely for the min value. These normally unseen parameters
	 * are required to operate on
	 * {@code T extends Comparable & Type}.
	 * 
	 * @param source
	 *            the {@link RandomAccessible} to operate on.
	 * @param target
	 *            the {@link IterableInterval} to write the results on.
	 * @param strel
	 *            the {@link Shape} that serves as a structuring element.
	 * @param minVal
	 *            a T containing set to a value smaller than any of the values
	 *            in the source (against {@link Comparable}).
	 * @param maxVal
	 *            a T containing set to a value larger than any of the values in
	 *            the source (against {@link Comparable}).
	 * @param numThreads
	 *            the number of threads to use for calculation.
	 * @param <T>
	 *            the type of the source and the result. Must extends
	 *            {@code Comparable}.
	 */
	public static < T extends Type< T > & Comparable< T > > void open( final RandomAccessible< T > source, final IterableInterval< T > target, final Shape strel, final T minVal, final T maxVal, final int numThreads )
	{
		// Create temp image
		final ImgFactory< T > factory = Util.getSuitableImgFactory( target, maxVal );
		final Img< T > img = factory.create( target );
		final long[] min = new long[ target.numDimensions() ];
		target.min( min );

		final IntervalView< T > translated = Views.translate( img, min );
		Erosion.erode( source, translated, strel, maxVal, numThreads );

		final ExtendedRandomAccessibleInterval< T, IntervalView< T >> extended = Views.extendValue( translated, minVal );
		Dilation.dilate( extended, target, strel, minVal, numThreads );
	}

	/**
	 * Performs the opening morphological operation, on a source
	 * {@link RandomAccessibleInterval} using a list of {@link Shape}s as a flat
	 * structuring element.
	 * 
	 * See <a href="http://en.wikipedia.org/wiki/Opening_(morphology)">
	 * Opening_(morphology)</a>.
	 * <p>
	 * The result is written in the source image. This method is limited to flat
	 * structuring elements, only having {@code on/off} pixels, contrary to
	 * grayscale structuring elements. This allows to simply use a {@link Shape}
	 * as a type for these structuring elements.
	 * <p>
	 * It is the caller responsibility to ensure that the source is sufficiently
	 * padded to properly cover the target range plus the shape size. See
	 * <i>e.g.</i> {@link Views#extendValue(RandomAccessibleInterval, Type)} *
	 * <p>
	 * The structuring element is specified through a list of {@link Shape}s, to
	 * allow for performance optimization through structuring element
	 * decomposition. Each shape is processed in order as given in the list. If
	 * the list is empty, the source image is left untouched.
	 * 
	 * @param source
	 *            the source image.
	 * @param interval
	 *            an interval in the source image to process.
	 * @param strels
	 *            the structuring element as a list of {@link Shape}s.
	 * @param numThreads
	 *            the number of threads to use for the calculation.
	 * @param <T>
	 *            the type of the source image. Must be a sub-type of
	 *            {@code T extends RealType}.
	 */
	public static < T extends RealType< T > > void openInPlace( final RandomAccessibleInterval< T > source, final Interval interval, final List< Shape > strels, final int numThreads )
	{
		final T maxVal = MorphologyUtils.createVariable( source, interval );
		maxVal.setReal( maxVal.getMaxValue() );
		final T minVal = MorphologyUtils.createVariable( source, interval );
		minVal.setReal( minVal.getMinValue() );

		openInPlace( source, interval, strels, minVal, maxVal, numThreads );
	}

	/**
	 * Performs the opening morphological operation, on a
	 * {@link RandomAccessibleInterval} using a list of {@link Shape}s as a flat
	 * structuring element.
	 * 
	 * See <a href="http://en.wikipedia.org/wiki/Opening_(morphology)">
	 * Opening_(morphology)</a>.
	 * <p>
	 * The result is written in the source image. This method is limited to flat
	 * structuring elements, only having {@code on/off} pixels, contrary to
	 * grayscale structuring elements. This allows to simply use a {@link Shape}
	 * as a type for these structuring elements.
	 * <p>
	 * It is the caller responsibility to ensure that the source is sufficiently
	 * padded to properly cover the target range plus the shape size. See
	 * <i>e.g.</i> {@link Views#extendValue(RandomAccessibleInterval, Type)}
	 * <p>
	 * The structuring element is specified through a list of {@link Shape}s, to
	 * allow for performance optimization through structuring element
	 * decomposition. Each shape is processed in order as given in the list. If
	 * the list is empty, the source image is left untouched.
	 * <p>
	 * This method relies on specified minimal and maximal values to start
	 * comparing to other pixels in the neighborhood. For this code to properly
	 * perform opening, it is sufficient that the specified max value is larger
	 * (against {@link Comparable}) than any of the value found in the source
	 * image, and conversely for the min value. These normally unseen parameters
	 * are required to operate on
	 * {@code T extends Comparable & Type}.
	 * 
	 * @param source
	 *            the source image.
	 * @param interval
	 *            an interval in the source image to process.
	 * @param strels
	 *            the structuring element as a list of {@link Shape}s.
	 * @param minVal
	 *            a T containing set to a value smaller than any of the values
	 *            in the source (against {@link Comparable}).
	 * @param maxVal
	 *            a T containing set to a value larger than any of the values in
	 *            the source (against {@link Comparable}).
	 * @param numThreads
	 *            the number of threads to use for the calculation.
	 * @param <T>
	 *            the type of the source image. Must be a sub-type of
	 *            {@code T extends Comparable}.
	 */
	public static < T extends Type< T > & Comparable< T >> void openInPlace( final RandomAccessibleInterval< T > source, final Interval interval, final List< Shape > strels, final T minVal, final T maxVal, final int numThreads )
	{
		for ( final Shape strel : strels )
		{
			openInPlace( source, interval, strel, minVal, maxVal, numThreads );
		}
	}

	/**
	 * Performs the opening morphological operation, on a {@link RealType}
	 * {@link RandomAccessibleInterval} using a {@link Shape} as a flat
	 * structuring element.
	 * 
	 * See <a href="http://en.wikipedia.org/wiki/Opening_(morphology)">
	 * Opening_(morphology)</a>.
	 * <p>
	 * The result is written in the source image. This method is limited to flat
	 * structuring elements, only having {@code on/off} pixels, contrary to
	 * grayscale structuring elements. This allows to simply use a {@link Shape}
	 * as a type for these structuring elements.
	 * <p>
	 * It is the caller responsibility to ensure that the source is sufficiently
	 * padded to properly cover the target range plus the shape size. See
	 * <i>e.g.</i> {@link Views#extendValue(RandomAccessibleInterval, Type)}.
	 * 
	 * @param source
	 *            the source image.
	 * @param interval
	 *            an interval in the source image to process.
	 * @param strel
	 *            the structuring element as a {@link Shape}.
	 * @param numThreads
	 *            the number of threads to use for the calculation.
	 * @param <T>
	 *            the type of the source image. Must be a sub-type of
	 *            {@code T extends RealType}.
	 */
	public static < T extends RealType< T > > void openInPlace( final RandomAccessibleInterval< T > source, final Interval interval, final Shape strel, final int numThreads )
	{
		final T maxVal = MorphologyUtils.createVariable( source, interval );
		maxVal.setReal( maxVal.getMaxValue() );
		final T minVal = MorphologyUtils.createVariable( source, interval );
		minVal.setReal( minVal.getMinValue() );

		openInPlace( source, interval, strel, minVal, maxVal, numThreads );
	}

	/**
	 * Performs the opening morphological operation, on a
	 * {@link RandomAccessibleInterval} using a {@link Shape} as a flat
	 * structuring element.
	 * 
	 * See <a href="http://en.wikipedia.org/wiki/Opening_(morphology)">
	 * Opening_(morphology)</a>.
	 * <p>
	 * The result is written in the source image. This method is limited to flat
	 * structuring elements, only having {@code on/off} pixels, contrary to
	 * grayscale structuring elements. This allows to simply use a {@link Shape}
	 * as a type for these structuring elements.
	 * <p>
	 * It is the caller responsibility to ensure that the source is sufficiently
	 * padded to properly cover the target range plus the shape size. See
	 * <i>e.g.</i> {@link Views#extendValue(RandomAccessibleInterval, Type)}
	 * <p>
	 * This method relies on specified minimal and maximal values to start
	 * comparing to other pixels in the neighborhood. For this code to properly
	 * perform opening, it is sufficient that the specified max value is larger
	 * (against {@link Comparable}) than any of the value found in the source
	 * image, and conversely for the min value. These normally unseen parameters
	 * are required to operate on
	 * {@code T extends Comparable & Type}.
	 * 
	 * @param source
	 *            the source image.
	 * @param interval
	 *            an interval in the source image to process.
	 * @param strel
	 *            the structuring element as a {@link Shape}.
	 * @param minVal
	 *            a T containing set to a value smaller than any of the values
	 *            in the source (against {@link Comparable}).
	 * @param maxVal
	 *            a T containing set to a value larger than any of the values in
	 *            the source (against {@link Comparable}).
	 * @param numThreads
	 *            the number of threads to use for the calculation.
	 * @param <T>
	 *            the type of the source image. Must be a sub-type of
	 *            {@code T extends Comparable}.
	 */
	public static < T extends Type< T > & Comparable< T >> void openInPlace( final RandomAccessibleInterval< T > source, final Interval interval, final Shape strel, final T minVal, final T maxVal, final int numThreads )
	{
		final ExtendedRandomAccessibleInterval< T, RandomAccessibleInterval< T >> extended = Views.extendValue( source, maxVal );

		final ImgFactory< T > factory = Util.getSuitableImgFactory( interval, maxVal );
		final Img< T > img = factory.create( interval );
		final long[] min = new long[ interval.numDimensions() ];
		interval.min( min );
		final IntervalView< T > translated = Views.translate( img, min );

		open( extended, translated, strel, minVal, maxVal, numThreads );
		MorphologyUtils.copy( translated, extended, numThreads );
	}

	/**
	 * Private constructor. Unused.
	 */
	private Opening()
	{}
}
