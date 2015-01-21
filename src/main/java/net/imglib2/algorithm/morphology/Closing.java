package net.imglib2.algorithm.morphology;

import java.util.List;

import net.imglib2.Interval;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.region.localneighborhood.Shape;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.ExtendedRandomAccessibleInterval;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

public class Closing
{
	/**
	 * Performs the morphological closing operation on a {@link RealType}
	 * {@link Img}, using a list of {@link Shape}s as a structuring element. See
	 * <a href="http://en.wikipedia.org/wiki/Closing_(morphology)">Closing_(
	 * morphology)</a>.
	 * <p>
	 * The closing operation is simply a dilation followed by an erosion.
	 * <p>
	 * The structuring element is specified through a list of {@link Shape}s, to
	 * allow for performance optimization through structuring element
	 * decomposition. Each shape is processed in order as given in the list. If
	 * the list is empty, the source image is returned.
	 * 
	 * @param source
	 *            the {@link Img} to operate on.
	 * @param strel
	 *            the list of {@link Shape}s that serves as a structuring
	 *            element.
	 * @param numThreads
	 *            the number of threads to use for calculation.
	 * @param <T>
	 *            the type of the source image and the result image. Must
	 *            extends <code>RealType</code>.
	 * @return an {@link Img} of the same type and same dimensions that of the
	 *         source.
	 */
	public static final < T extends RealType< T >> Img< T > close( final Img< T > source, final List< Shape > strels, final int numThreads )
	{
		final Img< T > dilated = Dilation.dilate( source, strels, numThreads );
		final Img< T > eroded = Erosion.erode( dilated, strels, numThreads );
		return eroded;
	}

	/**
	 * Performs the morphological closing operation on an {@link Img} of
	 * {@link Comparable} , using a list of {@link Shape}s as a structuring
	 * element. See <a href="http://en.wikipedia.org/wiki/Closing_(morphology)"
	 * >Closing_(morphology)</a>.
	 * <p>
	 * The closing operation is simply a dilation followed by an erosion.
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
	 * <code>T extends {@link Comparable} & {@link Type}</code>.
	 * 
	 * @param source
	 *            the {@link Img} to operate on.
	 * @param strel
	 *            the list of {@link Shape}s that serves as a structuring
	 *            element.
	 * @param minVal
	 *            a T containing set to a value smaller than any of the values
	 *            in the source {@link Img} (against {@link Comparable}.
	 * @param maxVal
	 *            a T containing set to a value larger than any of the values in
	 *            the source {@link Img} (against {@link Comparable}.
	 * @param numThreads
	 *            the number of threads to use for calculation.
	 * @param <T>
	 *            the type of the source image and the result. Must be a
	 *            sub-type of <code>T extends {@link Comparable} & {@link Type}
	 *            </code>.
	 * @return an {@link Img} of the same type and same dimensions that of the
	 *         source.
	 */
	public static final < T extends Type< T > & Comparable< T > > Img< T > close( final Img< T > source, final List< Shape > strels, final T minVal, final T maxVal, final int numThreads )
	{
		final Img< T > dilated = Dilation.dilate( source, strels, minVal, numThreads );
		final Img< T > eroded = Erosion.erode( dilated, strels, maxVal, numThreads );
		return eroded;
	}

	/**
	 * Performs the morphological closing operation on a {@link RealType}
	 * {@link Img}, using a {@link Shape} as a structuring element. See <a
	 * href="http://en.wikipedia.org/wiki/Closing_(morphology)"
	 * >Closing_(morphology)</a>.
	 * <p>
	 * The closing operation is simply a dilation followed by an erosion.
	 * 
	 * @param source
	 *            the {@link Img} to operate on.
	 * @param strel
	 *            the {@link Shape} that serves as a structuring element.
	 * @param numThreads
	 *            the number of threads to use for calculation.
	 * @param <T>
	 *            the type of the source image and the result image. Must
	 *            extends <code>RealType</code>.
	 * @return an {@link Img} of the same type and same dimensions that of the
	 *         source.
	 */
	public static final < T extends RealType< T >> Img< T > close( final Img< T > source, final Shape strel, final int numThreads )
	{
		final Img< T > dilated = Dilation.dilate( source, strel, numThreads );
		final Img< T > eroded = Erosion.erode( dilated, strel, numThreads );
		return eroded;
	}


	/**
	 * Performs the morphological closing operation on an {@link Img} of
	 * {@link Comparable} , using a {@link Shape} as a structuring element. See
	 * <a href="http://en.wikipedia.org/wiki/Closing_(morphology)"
	 * >Closing_(morphology)</a>.
	 * <p>
	 * The closing operation is simply a dilation followed by an erosion.
	 * <p>
	 * This method relies on a specified minimal and maximal value to start
	 * comparing to other pixels in the neighborhood. For this code to perform
	 * properly, it is sufficient that the specified min value is smaller
	 * (against {@link Comparable}) than any of the value found in the source
	 * image, and the converse for the max value. These normally unseen
	 * parameters are required to operate on
	 * <code>T extends {@link Comparable} & {@link Type}</code>.
	 * 
	 * @param source
	 *            the {@link Img} to operate on.
	 * @param strel
	 *            the {@link Shape} that serves as a structuring element.
	 * @param minVal
	 *            a T containing set to a value smaller than any of the values
	 *            in the source {@link Img} (against {@link Comparable}.
	 * @param maxVal
	 *            a T containing set to a value larger than any of the values in
	 *            the source {@link Img} (against {@link Comparable}.
	 * @param numThreads
	 *            the number of threads to use for calculation.
	 * @param <T>
	 *            the type of the source image and the result. Must be a
	 *            sub-type of <code>T extends {@link Comparable} & {@link Type}
	 *            </code>.
	 * @return an {@link Img} of the same type and same dimensions that of the
	 *         source.
	 */
	public static final < T extends Type< T > & Comparable< T > > Img< T > close( final Img< T > source, final Shape strel, final T minVal, final T maxVal, final int numThreads )
	{
		final Img< T > dilated = Dilation.dilate( source, strel, minVal, numThreads );
		final Img< T > eroded = Erosion.erode( dilated, strel, maxVal, numThreads );
		return eroded;
	}

	/**
	 * Performs the morphological closing operation on a {@link RealType} source
	 * {@link RandomAccessible}, using a list of {@link Shape}s as a structuring
	 * element, and writes the result on a specified target which must be an
	 * {@link IterableInterval}. See <a
	 * href="http://en.wikipedia.org/wiki/Closing_(morphology)"
	 * >Closing_(morphology)</a>.
	 * <p>
	 * The closing operation is simply a dilation followed by an erosion.
	 * <p>
	 * <b>Careful: Target must point to a different structure than source.</b>
	 * In place operation will not work but will not generate an error.
	 * <p>
	 * It is the caller responsibility to ensure that the source is sufficiently
	 * padded to properly cover the target range plus the shape size. See
	 * <i>e.g.</i> {@link Views#extendValue(RandomAccessibleInterval, Type)}
	 * <p>
	 * It is limited to flat structuring elements, only having
	 * <code>on/off</code> pixels, contrary to grayscale structuring elements.
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
	 *            <code>RealType</code>.
	 */
	public static < T extends RealType< T > > void close( final RandomAccessible< T > source, final IterableInterval< T > target, final List< Shape > strels, final int numThreads )
	{
		final T maxVal = MorphologyUtils.createVariable( source, target );
		maxVal.setReal( maxVal.getMaxValue() );
		final T minVal = MorphologyUtils.createVariable( source, target );
		minVal.setReal( minVal.getMinValue() );
		close( source, target, strels, minVal, maxVal, numThreads );
	}

	/**
	 * Performs the morphological closing operation on a source
	 * {@link RandomAccessible}, using a list of {@link Shape}s as a structuring
	 * element, and writes the result on a specified target which must be an
	 * {@link IterableInterval}. See <a
	 * href="http://en.wikipedia.org/wiki/Closing_(morphology)"
	 * >Closing_(morphology)</a>.
	 * <p>
	 * The closing operation is simply a dilation followed by an erosion.
	 * <p>
	 * <b>Careful: Target must point to a different structure than source.</b>
	 * In place operation will not work but will not generate an error.
	 * <p>
	 * It is the caller responsibility to ensure that the source is sufficiently
	 * padded to properly cover the target range plus the shape size. See
	 * <i>e.g.</i> {@link Views#extendValue(RandomAccessibleInterval, Type)}
	 * <p>
	 * It is limited to flat structuring elements, only having
	 * <code>on/off</code> pixels, contrary to grayscale structuring elements.
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
	 * perform closing, it is sufficient that the specified max value is larger
	 * (against {@link Comparable}) than any of the value found in the source
	 * image, and conversely for the min value. These normally unseen parameters
	 * are required to operate on
	 * <code>T extends {@link Comparable} & {@link Type}</code>.
	 * 
	 * @param source
	 *            the {@link RandomAccessible} to operate on.
	 * @param target
	 *            the {@link IterableInterval} to write the results on.
	 * @param strels
	 *            the structuring element, as a list of {@link Shape}s.
	 * @param minVal
	 *            a T containing set to a value smaller than any of the values
	 *            in the source (against {@link Comparable}.
	 * @param maxVal
	 *            a T containing set to a value larger than any of the values in
	 *            the source (against {@link Comparable}.
	 * @param numThreads
	 *            the number of threads to use for calculation.
	 * @param <T>
	 *            the type of the source and the result. Must extends
	 *            <code>Compparable</code>.
	 */
	public static < T extends Type< T > & Comparable< T > > void close( final RandomAccessible< T > source, final IterableInterval< T > target, final List< Shape > strels, final T minVal, final T maxVal, final int numThreads )
	{
		// Create temp image
		final ImgFactory< T > factory = MorphologyUtils.getSuitableFactory( target, maxVal );
		final Img< T > img = factory.create( target, maxVal );
		final long[] min = new long[ target.numDimensions() ];
		target.min( min );

		final IntervalView< T > translated = Views.translate( img, min );
		Dilation.dilate( source, translated, strels, minVal, numThreads );

		final ExtendedRandomAccessibleInterval< T, IntervalView< T >> extended = Views.extendValue( translated, maxVal );
		Erosion.erode( extended, target, strels, maxVal, numThreads );
	}

	/**
	 * Performs the morphological closing operation on a {@link RealType} source
	 * {@link RandomAccessible}, using a {@link Shape} as a structuring element,
	 * and writes the result on a specified target which must be an
	 * {@link IterableInterval}. See <a
	 * href="http://en.wikipedia.org/wiki/Closing_(morphology)"
	 * >Closing_(morphology)</a>.
	 * <p>
	 * The closing operation is simply a dilation followed by an erosion.
	 * <p>
	 * <b>Careful: Target must point to a different structure than source.</b>
	 * In place operation will not work but will not generate an error.
	 * <p>
	 * It is the caller responsibility to ensure that the source is sufficiently
	 * padded to properly cover the target range plus the shape size. See
	 * <i>e.g.</i> {@link Views#extendValue(RandomAccessibleInterval, Type)}
	 * <p>
	 * It is limited to flat structuring elements, only having
	 * <code>on/off</code> pixels, contrary to grayscale structuring elements.
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
	 *            <code>RealType</code>.
	 */
	public static < T extends RealType< T > > void close( final RandomAccessible< T > source, final IterableInterval< T > target, final Shape strel, final int numThreads )
	{
		final T maxVal = MorphologyUtils.createVariable( source, target );
		maxVal.setReal( maxVal.getMaxValue() );
		final T minVal = MorphologyUtils.createVariable( source, target );
		minVal.setReal( minVal.getMinValue() );
		close( source, target, strel, minVal, maxVal, numThreads );
	}

	/**
	 * Performs the morphological closing operation on a source
	 * {@link RandomAccessible}, using a {@link Shape} as a structuring element,
	 * and writes the result on a specified target which must be an
	 * {@link IterableInterval}. See <a
	 * href="http://en.wikipedia.org/wiki/Closing_(morphology)"
	 * >Closing_(morphology)</a>.
	 * <p>
	 * The closing operation is simply a dilation followed by an erosion.
	 * <p>
	 * <b>Careful: Target must point to a different structure than source.</b>
	 * In place operation will not work but will not generate an error.
	 * <p>
	 * It is the caller responsibility to ensure that the source is sufficiently
	 * padded to properly cover the target range plus the shape size. See
	 * <i>e.g.</i> {@link Views#extendValue(RandomAccessibleInterval, Type)}
	 * <p>
	 * It is limited to flat structuring elements, only having
	 * <code>on/off</code> pixels, contrary to grayscale structuring elements.
	 * This allows to simply use a {@link Shape} as a type for these structuring
	 * elements.
	 * <p>
	 * This method relies on specified minimal and maximal values to start
	 * comparing to other pixels in the neighborhood. For this code to properly
	 * perform closing, it is sufficient that the specified max value is larger
	 * (against {@link Comparable}) than any of the value found in the source
	 * image, and conversely for the min value. These normally unseen parameters
	 * are required to operate on
	 * <code>T extends {@link Comparable} & {@link Type}</code>.
	 * 
	 * @param source
	 *            the {@link RandomAccessible} to operate on.
	 * @param target
	 *            the {@link IterableInterval} to write the results on.
	 * @param strel
	 *            the {@link Shape} that serves as a structuring element.
	 * @param minVal
	 *            a T containing set to a value smaller than any of the values
	 *            in the source (against {@link Comparable}.
	 * @param maxVal
	 *            a T containing set to a value larger than any of the values in
	 *            the source (against {@link Comparable}.
	 * @param numThreads
	 *            the number of threads to use for calculation.
	 * @param <T>
	 *            the type of the source and the result. Must extends
	 *            <code>Comparable</code>.
	 */
	public static < T extends Type< T > & Comparable< T > > void close( final RandomAccessible< T > source, final IterableInterval< T > target, final Shape strel, final T minVal, final T maxVal, final int numThreads )
	{
		// Create temp image
		final ImgFactory< T > factory = MorphologyUtils.getSuitableFactory( target, minVal );
		final Img< T > img = factory.create( target, minVal );
		final long[] min = new long[ target.numDimensions() ];
		target.min( min );

		final IntervalView< T > translated = Views.translate( img, min );
		Dilation.dilate( source, translated, strel, minVal, numThreads );

		final ExtendedRandomAccessibleInterval< T, IntervalView< T >> extended = Views.extendValue( translated, maxVal );
		Erosion.erode( extended, target, strel, maxVal, numThreads );
	}

	/**
	 * Performs the closing morphological operation, on a source
	 * {@link RandomAccessibleInterval} using a list of {@link Shape}s as a flat
	 * structuring element.
	 * 
	 * See <a href="http://en.wikipedia.org/wiki/Closing_(morphology)">
	 * Closing_(morphology)</a>.
	 * <p>
	 * The result is written in the source image. This method is limited to flat
	 * structuring elements, only having <code>on/off</code> pixels, contrary to
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
	 * the list is empty, the source image is returned.
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
	 *            <code>T extends {@link RealType}</code>.
	 */
	public static < T extends RealType< T > > void closeInPlace( final RandomAccessibleInterval< T > source, final Interval interval, final List< Shape > strels, final int numThreads )
	{
		final T maxVal = MorphologyUtils.createVariable( source, interval );
		maxVal.setReal( maxVal.getMaxValue() );
		final T minVal = MorphologyUtils.createVariable( source, interval );
		minVal.setReal( minVal.getMinValue() );

		closeInPlace( source, interval, strels, minVal, maxVal, numThreads );
	}

	/**
	 * Performs the closing morphological operation, on a
	 * {@link RandomAccessibleInterval} using a list of {@link Shape}s as a flat
	 * structuring element.
	 * 
	 * See <a href="http://en.wikipedia.org/wiki/Closing_(morphology)">
	 * Closing_(morphology)</a>.
	 * <p>
	 * The result is written in the source image. This method is limited to flat
	 * structuring elements, only having <code>on/off</code> pixels, contrary to
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
	 * the list is empty, the source image is returned.
	 * <p>
	 * This method relies on specified minimal and maximal values to start
	 * comparing to other pixels in the neighborhood. For this code to properly
	 * perform closing, it is sufficient that the specified max value is larger
	 * (against {@link Comparable}) than any of the value found in the source
	 * image, and conversely for the min value. These normally unseen parameters
	 * are required to operate on
	 * <code>T extends {@link Comparable} & {@link Type}</code>.
	 * 
	 * @param source
	 *            the source image.
	 * @param interval
	 *            an interval in the source image to process.
	 * @param strels
	 *            the structuring element as a list of {@link Shape}s.
	 * @param minVal
	 *            a T containing set to a value smaller than any of the values
	 *            in the source (against {@link Comparable}.
	 * @param maxVal
	 *            a T containing set to a value larger than any of the values in
	 *            the source (against {@link Comparable}.
	 * @param numThreads
	 *            the number of threads to use for the calculation.
	 * @param <T>
	 *            the type of the source image. Must be a sub-type of
	 *            <code>T extends {@link Comparable}</code>.
	 */
	public static < T extends Type< T > & Comparable< T >> void closeInPlace( final RandomAccessibleInterval< T > source, final Interval interval, final List< Shape > strels, final T minVal, final T maxVal, final int numThreads )
	{
		for ( final Shape strel : strels )
		{
			closeInPlace( source, interval, strel, minVal, maxVal, numThreads );
		}
	}

	/**
	 * Performs the closing morphological operation, on a {@link RealType}
	 * {@link RandomAccessibleInterval} using a {@link Shape} as a flat
	 * structuring element.
	 * 
	 * See <a href="http://en.wikipedia.org/wiki/Closing_(morphology)">
	 * Closing_(morphology)</a>.
	 * <p>
	 * The result is written in the source image. This method is limited to flat
	 * structuring elements, only having <code>on/off</code> pixels, contrary to
	 * grayscale structuring elements. This allows to simply use a {@link Shape}
	 * as a type for these structuring elements.
	 * <p>
	 * It is the caller responsibility to ensure that the source is sufficiently
	 * padded to properly cover the target range plus the shape size. See
	 * <i>e.g.</i> {@link Views#extendValue(RandomAccessibleInterval, Type)}.
	 * <p>
	 * This method relies on specified minimal and maximal values to start
	 * comparing to other pixels in the neighborhood. For this code to properly
	 * perform closing, it is sufficient that the specified max value is larger
	 * (against {@link Comparable}) than any of the value found in the source
	 * image, and conversely for the min value. These normally unseen parameters
	 * are required to operate on
	 * <code>T extends {@link Comparable} & {@link Type}</code>.
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
	 *            <code>T extends {@link RealType}</code>.
	 */
	public static < T extends RealType< T > > void closeInPlace( final RandomAccessibleInterval< T > source, final Interval interval, final Shape strel, final int numThreads )
	{
		final T maxVal = MorphologyUtils.createVariable( source, interval );
		maxVal.setReal( maxVal.getMaxValue() );
		final T minVal = MorphologyUtils.createVariable( source, interval );
		minVal.setReal( minVal.getMinValue() );

		closeInPlace( source, interval, strel, minVal, maxVal, numThreads );
	}

	/**
	 * Performs the closing morphological operation, on a
	 * {@link RandomAccessibleInterval} using a {@link Shape} as a flat
	 * structuring element.
	 * 
	 * See <a href="http://en.wikipedia.org/wiki/Closing_(morphology)">
	 * Closing_(morphology)</a>.
	 * <p>
	 * The result is written in the source image. This method is limited to flat
	 * structuring elements, only having <code>on/off</code> pixels, contrary to
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
	 * the list is empty, the source image is returned.
	 * <p>
	 * This method relies on specified minimal and maximal values to start
	 * comparing to other pixels in the neighborhood. For this code to properly
	 * perform closing, it is sufficient that the specified max value is larger
	 * (against {@link Comparable}) than any of the value found in the source
	 * image, and conversely for the min value. These normally unseen parameters
	 * are required to operate on
	 * <code>T extends {@link Comparable} & {@link Type}</code>.
	 * 
	 * @param source
	 *            the source image.
	 * @param interval
	 *            an interval in the source image to process.
	 * @param strel
	 *            the structuring element as a {@link Shape}.
	 * @param minVal
	 *            a T containing set to a value smaller than any of the values
	 *            in the source (against {@link Comparable}.
	 * @param maxVal
	 *            a T containing set to a value larger than any of the values in
	 *            the source (against {@link Comparable}.
	 * @param numThreads
	 *            the number of threads to use for the calculation.
	 * @param <T>
	 *            the type of the source image. Must be a sub-type of
	 *            <code>T extends {@link Comparable}</code>.
	 */
	public static < T extends Type< T > & Comparable< T >> void closeInPlace( final RandomAccessibleInterval< T > source, final Interval interval, final Shape strel, final T minVal, final T maxVal, final int numThreads )
	{
		final ExtendedRandomAccessibleInterval< T, RandomAccessibleInterval< T >> extended = Views.extendValue( source, maxVal );

		final ImgFactory< T > factory = MorphologyUtils.getSuitableFactory( interval, maxVal );
		final Img< T > img = factory.create( interval, maxVal );
		final long[] min = new long[ interval.numDimensions() ];
		interval.min( min );
		final IntervalView< T > translated = Views.translate( img, min );

		close( extended, translated, strel, minVal, maxVal, numThreads );
		MorphologyUtils.copy( translated, extended, numThreads );
	}

	/**
	 * Private constructor. Unused.
	 */
	private Closing()
	{}
}
