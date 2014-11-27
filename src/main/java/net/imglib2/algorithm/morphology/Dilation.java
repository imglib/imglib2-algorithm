package net.imglib2.algorithm.morphology;

import java.util.List;
import java.util.Vector;

import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.region.localneighborhood.Neighborhood;
import net.imglib2.algorithm.region.localneighborhood.Shape;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.multithreading.Chunk;
import net.imglib2.multithreading.SimpleMultiThreading;
import net.imglib2.type.Type;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.ExtendedRandomAccessibleInterval;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

public class Dilation
{
	private static final < T > long[][] computeTargetImageDimensionsAndOffset( final RandomAccessibleInterval< T > source, final Shape strel )
	{
		/*
		 * Compute target image size
		 */

		final long[] targetDims;

		/*
		 * Get a neighborhood to play with. Note: if we would have a dedicated
		 * interface for structuring elements, that would extend Shape and
		 * Dimensions, we would need to do what we are going to do now. On top
		 * of that, this is the part that causes the full dilation not to be a
		 * real full dilation: if the structuring element has more dimensions
		 * than the source, they are ignored. This is because we use the source
		 * as the Dimension to create the sample neighborhood we play with.
		 */
		final Neighborhood< BitType > sampleNeighborhood = MorphologyUtils.getNeighborhood( strel, source );
		int ndims = sampleNeighborhood.numDimensions();
		ndims = Math.max( ndims, source.numDimensions() );
		targetDims = new long[ ndims ];
		for ( int d = 0; d < ndims; d++ )
		{
			long d1;
			if ( d < source.numDimensions() )
			{
				d1 = source.dimension( d );
			}
			else
			{
				d1 = 1;
			}

			long d2;
			if ( d < sampleNeighborhood.numDimensions() )
			{
				d2 = sampleNeighborhood.dimension( d );
			}
			else
			{
				d2 = 1;
			}

			targetDims[ d ] = d1 + d2 - 1;
		}

		// Offset coordinates so that they match the source coordinates, which
		// will not be extended.
		final long[] offset = new long[ source.numDimensions() ];
		for ( int d = 0; d < offset.length; d++ )
		{
			if ( d < sampleNeighborhood.numDimensions() )
			{
				offset[ d ] = -sampleNeighborhood.min( d );
			}
			else
			{
				offset[ d ] = 0;
			}
		}

		return new long[][] { targetDims, offset };
	}

	public static < T extends RealType< T > > Img< T > dilate( final Img< T > source, final List< Shape > strels, final int numThreads )
	{
		Img< T > target = source;
		for ( final Shape strel : strels )
		{
			target = dilate( target, strel, numThreads );
		}
		return target;
	}

	public static < T extends Type< T > & Comparable< T > > Img< T > dilate( final Img< T > source, final List< Shape > strels, final T minVal, final int numThreads )
	{
		Img< T > target = source;
		for ( final Shape strel : strels )
		{
			target = dilate( target, strel, minVal, numThreads );
		}
		return target;
	}

	/**
	 * Performs the dilation morphological operation, on a {@link RealType}
	 * {@link Img} using a {@link Shape} as a flat structuring element.
	 * 
	 * See <a href="http://en.wikipedia.org/wiki/Dilation_(morphology)">
	 * Dilation_(morphology)</a>.
	 * <p>
	 * The result image has the same dimensions that of the source image. It is
	 * limited to flat structuring elements, only having <code>on/off</code>
	 * pixels, contrary to grayscale structuring elements. This allows to simply
	 * use a {@link Shape} as a type for these structuring elements.
	 * 
	 * @param source
	 *            the source image.
	 * @param strel
	 *            the structuring element as a {@link Shape}.
	 * @param numThreads
	 *            the number of threads to use for the calculation.
	 * @param <T>
	 *            the type of the source image and the dilation result. Must be
	 *            a sub-type of <code>T extends {@link RealType}</code>.
	 * @return a new {@link Img}, of same dimensions than the source.
	 */
	public static < T extends RealType< T >> Img< T > dilate( final Img< T > source, final Shape strel, final int numThreads )
	{
		final Img< T > target = source.factory().create( source, source.firstElement().copy() );
		final T minVal = source.firstElement().createVariable();
		minVal.setReal( minVal.getMinValue() );
		final ExtendedRandomAccessibleInterval< T, Img< T >> extended = Views.extendValue( source, minVal );
		dilate( extended, target, strel, numThreads );
		return target;
	}

	public static < T extends Type< T > & Comparable< T > > Img< T > dilate( final Img< T > source, final Shape strel, final T minVal, final int numThreads )
	{
		final Img< T > target = source.factory().create( source, source.firstElement().copy() );
		final ExtendedRandomAccessibleInterval< T, Img< T >> extended = Views.extendValue( source, minVal );
		dilate( extended, target, strel, minVal, numThreads );
		return target;
	}

	public static < T extends RealType< T >> void dilate( final RandomAccessible< T > source, final IterableInterval< T > target, final List< Shape > strels, final int numThreads )
	{
		final T minVal = MorphologyUtils.createVariable( source, target );
		minVal.setReal( minVal.getMinValue() );
		dilate( source, target, strels, minVal, numThreads );
	}

	public static < T extends Type< T > & Comparable< T > > void dilate( final RandomAccessible< T > source, final IterableInterval< T > target, final List< Shape > strels, final T minVal, final int numThreads )
	{
		if ( strels.isEmpty() ) { return; }
		if ( strels.size() == 1 )
		{
			dilate( source, target, strels.get( 0 ), minVal, numThreads );
			return;
		}

		final ImgFactory< T > factory = MorphologyUtils.getSuitableFactory( target, minVal );
		Img< T > temp = factory.create( target, minVal );
		final long[] translation = new long[ target.numDimensions() ];
		target.min( translation );
		IntervalView< T > translated = Views.translate( temp, translation );

		// First shape.
		dilate( source, translated, strels.get( 0 ), minVal, numThreads );

		// Middle shapes.
		for ( int i = 1; i < strels.size() - 1; i++ )
		{
			temp = dilate( temp, strels.get( i ), minVal, numThreads );
		}

		// Last shape
		translated = Views.translate( temp, translation );
		final ExtendedRandomAccessibleInterval< T, IntervalView< T >> extended = Views.extendValue( translated, minVal );
		dilate( extended, target, strels.get( strels.size() - 1 ), minVal, numThreads );
	}

	public static < T extends RealType< T >> void dilate( final RandomAccessible< T > source, final IterableInterval< T > target, final Shape strel, final int numThreads )
	{
		final T minVal = MorphologyUtils.createVariable( source, target );
		minVal.setReal( minVal.getMinValue() );
		dilate( source, target, strel, minVal, numThreads );
	}

	/**
	 * Performs the dilation morphological operation, on an {@link Img} using a
	 * {@link Shape} as a flat structuring element.
	 * See <a href="http://en.wikipedia.org/wiki/Dilation_(morphology)">
	 * Dilation_(morphology)</a>.
	 * <p>
	 * It is limited to flat structuring elements, only having
	 * <code>on/off</code> pixels, contrary to grayscale structuring elements.
	 * This allows to simply use a {@link Shape} as a type for these structuring
	 * elements.
	 * <p>
	 * This method relies on a specified minimal value to start comparing to
	 * other pixels in the neighborhood. For this code to properly perform
	 * dilation, it is sufficient that the specified min value is smaller
	 * (against {@link Comparable}) than any of the value found in the source
	 * image. This normally unseen parameter is required to operate on
	 * <code>T extends {@link Comparable} & {@link Type}</code>.
	 * 
	 * @param source
	 *            the source {@link RandomAccessible}, must be sufficiently
	 *            padded (<it>e.g.</it>
	 *            {@link Views#extendValue(net.imglib2.RandomAccessibleInterval, Type)}
	 *            using the minVal parameter).
	 * @param target
	 *            an {@link IterableInterval} into the target. <b>Careful:
	 *            Dilation does not work for target pointing to the source
	 *            data.</b>
	 * @param strel
	 *            the structuring element as a {@link Shape}.
	 * @param numThreads
	 *            the number of threads to use for the calculation.
	 * @param minVal
	 *            a T containing set to a value smaller than any of the values
	 *            in the source {@link Img} (against {@link Comparable}. This is
	 *            required to perform a proper mathematical dilation. Because we
	 *            operate on a generic {@link Type}, it has to be provided
	 *            manually.
	 * @param <T>
	 *            the type of the source image and the dilation result. Must be
	 *            a sub-type of <code>T extends {@link Comparable} &
	 *            {@link Type}</code>.
	 */
	public static < T extends Type< T > & Comparable< T > > void dilate( final RandomAccessible< T > source, final IterableInterval< T > target, final Shape strel, final T minVal, int numThreads )
	{
		numThreads = Math.max( 1, numThreads );

		/*
		 * Prepare iteration.
		 */

		final RandomAccessible< Neighborhood< T >> accessible;
		if ( numThreads > 1 )
		{
			accessible = strel.neighborhoodsRandomAccessibleSafe( source );
		}
		else
		{
			accessible = strel.neighborhoodsRandomAccessible( source );
		}

		/*
		 * Multithread
		 */

		final Vector< Chunk > chunks = SimpleMultiThreading.divideIntoChunks( target.size(), numThreads );
		final Thread[] threads = SimpleMultiThreading.newThreads( numThreads );


		if ( minVal instanceof BitType )
		{
			/*
			 * Optimization for BitType
			 */

			for ( int i = 0; i < threads.length; i++ )
			{
				final Chunk chunk = chunks.get( i );
				threads[ i ] = new Thread( "Morphology dilate thread " + i )
				{
					@Override
					public void run()
					{
						final RandomAccess< Neighborhood< T >> randomAccess = accessible.randomAccess( target );
						@SuppressWarnings( "unchecked" )
						final Cursor< BitType > cursorDilated = ( Cursor< BitType > ) target.cursor();
						cursorDilated.jumpFwd( chunk.getStartPosition() );

						for ( long steps = 0; steps < chunk.getLoopSize(); steps++ )
						{
							cursorDilated.fwd();
							randomAccess.setPosition( cursorDilated );
							final Neighborhood< T > neighborhood = randomAccess.get();
							@SuppressWarnings( "unchecked" )
							final Cursor< BitType > nc = ( Cursor< BitType > ) neighborhood.cursor();

							while ( nc.hasNext() )
							{
								nc.fwd();
								final BitType val = nc.get();
								if ( val.get() )
								{
									cursorDilated.get().set( true );
									break;
								}
							}
						}

					}
				};
			}
		}
		else
		{
			/*
			 * All other comparable type.
			 */

			for ( int i = 0; i < threads.length; i++ )
			{
				final Chunk chunk = chunks.get( i );
				threads[ i ] = new Thread( "Morphology dilate thread " + i )
				{
					@Override
					public void run()
					{
						final RandomAccess< Neighborhood< T >> randomAccess = accessible.randomAccess( target );
						final Cursor< T > cursorDilated = target.cursor();
						cursorDilated.jumpFwd( chunk.getStartPosition() );

						final T max = MorphologyUtils.createVariable( source, target );
						for ( long steps = 0; steps < chunk.getLoopSize(); steps++ )
						{
							cursorDilated.fwd();
							randomAccess.setPosition( cursorDilated );
							final Neighborhood< T > neighborhood = randomAccess.get();
							final Cursor< T > nc = neighborhood.cursor();

							/*
							 * Look for max in the neighborhood.
							 */

							max.set( minVal );
							while ( nc.hasNext() )
							{
								nc.fwd();
								final T val = nc.get();
								// We need only Comparable to do this:
								if ( val.compareTo( max ) > 0 )
								{
									max.set( val );
								}
							}
							cursorDilated.get().set( max );
						}

					}
				};
			}
		}

		/*
		 * Launch calculation
		 */

		SimpleMultiThreading.startAndJoin( threads );
	}

	/**
	 * Performs the dilation morphological operation, on a {@link RealType}
	 * {@link Img} using a list of {@link Shape}s as a flat structuring element.
	 * 
	 * See <a href="http://en.wikipedia.org/wiki/Dilation_(morphology)">
	 * Dilation_(morphology)</a>.
	 * <p>
	 * This method performs what is called the 'full' dilation. That is: the
	 * result image has its dimension dilated by the structuring element, with
	 * respect to the source image. It is limited to flat structuring elements,
	 * only having <code>on/off</code> pixels, contrary to grayscale structuring
	 * elements. This allows to simply use a {@link Shape} as a type for these
	 * structuring elements.
	 * <p>
	 * The structuring element is specified through a list of {@link Shape}s, to
	 * allow for performance optimization through structuring element
	 * decomposition. Each shape is processed in order as given in the list. If
	 * the list is empty, the source image is returned.
	 * <p>
	 * <b>Warning:</b> Current implementation does not do <i>stricto sensu</i>
	 * the full dilation. Indeed, if the structuring element has more dimensions
	 * than the source {@link Img}, they are ignored, and the returned
	 * {@link Img} has the same number of dimensions that of the source (but
	 * dilated). This is due to the fact that we use a {@link Shape} for
	 * structuring elements, and that it does not return a number of dimensions.
	 * The neighborhood created have therefore at most as many dimensions as the
	 * source image. The real, full dilation results should have a number of
	 * dimensions equals to the maximum of the number of dimension of both
	 * source and structuring element.
	 * 
	 * @param source
	 *            the source image.
	 * @param strel
	 *            the structuring element as a list of {@link Shape}s.
	 * @param numThreads
	 *            the number of threads to use for the calculation.
	 * @param <T>
	 *            the type of the source image and the dilation result. Must be
	 *            a sub-type of <code>T extends {@link RealType}</code>.
	 * @return a new {@link Img}, possibly of larger dimensions than the source.
	 */
	public static < T extends RealType< T > > Img< T > dilateFull( final Img< T > source, final List< Shape > strels, final int numThreads )
	{
		Img< T > target = source;
		for ( final Shape strel : strels )
		{
			target = dilateFull( target, strel, numThreads );
		}
		return target;
	}

	/**
	 * Performs the dilation morphological operation, on an {@link Img} using a
	 * list of {@link Shape}s as a flat structuring element.
	 * 
	 * See <a href="http://en.wikipedia.org/wiki/Dilation_(morphology)">
	 * Dilation_(morphology)</a>.
	 * <p>
	 * This method performs what is called the 'full' dilation. That is: the
	 * result image has its dimension dilated by the structuring element, with
	 * respect to the source image. It is limited to flat structuring elements,
	 * only having <code>on/off</code> pixels, contrary to grayscale structuring
	 * elements. This allows to simply use a {@link Shape} as a type for these
	 * structuring elements.
	 * <p>
	 * The structuring element is specified through a list of {@link Shape}s, to
	 * allow for performance optimization through structuring element
	 * decomposition. Each shape is processed in order as given in the list. If
	 * the list is empty, the source image is returned.
	 * <p>
	 * This method relies on a specified minimal value to start comparing to
	 * other pixels in the neighborhood. For this code to properly perform
	 * dilation, it is sufficient that the specified min value is smaller
	 * (against {@link Comparable}) than any of the value found in the source
	 * image. This normally unseen parameter is required to operate on
	 * <code>T extends {@link Comparable} & {@link Type}</code>.
	 * <p>
	 * <b>Warning:</b> Current implementation does not do <i>stricto sensu</i>
	 * the full dilation. Indeed, if the structuring element has more dimensions
	 * than the source {@link Img}, they are ignored, and the returned
	 * {@link Img} has the same number of dimensions that of the source (but
	 * dilated). This is due to the fact that we use a {@link Shape} for
	 * structuring elements, and that it does not return a number of dimensions.
	 * The neighborhood created have therefore at most as many dimensions as the
	 * source image. The real, full dilation results should have a number of
	 * dimensions equals to the maximum of the number of dimension of both
	 * source and structuring element.
	 * 
	 * @param source
	 *            the source image.
	 * @param strel
	 *            the structuring element as a list of {@link Shape}s.
	 * @param numThreads
	 *            the number of threads to use for the calculation.
	 * @param minVal
	 *            a T containing set to a value smaller than any of the values
	 *            in the source {@link Img} (against {@link Comparable}. This is
	 *            required to perform a proper mathematical dilation. Because we
	 *            operate on a generic {@link Type}, it has to be provided
	 *            manually.
	 * @param <T>
	 *            the type of the source image and the dilation result. Must be
	 *            a sub-type of <code>T extends {@link Comparable} &
	 *            {@link Type}</code>.
	 * @return a new {@link Img}, possibly of larger dimensions than the source.
	 */
	public static < T extends Type< T > & Comparable< T > > Img< T > dilateFull( final Img< T > source, final List< Shape > strels, final T minVal, final int numThreads )
	{
		Img< T > target = source;
		for ( final Shape strel : strels )
		{
			target = dilateFull( target, strel, minVal, numThreads );
		}
		return target;
	}

	public static < T extends RealType< T >> Img< T > dilateFull( final Img< T > source, final Shape strel, final int numThreads )
	{
		final long[][] dimensionsAndOffset = computeTargetImageDimensionsAndOffset( source, strel );

		final long[] targetDims = dimensionsAndOffset[ 0 ];
		final long[] offset = dimensionsAndOffset[ 1 ];

		final Img< T > target = source.factory().create( targetDims, source.firstElement().copy() );
		final IntervalView< T > offsetTarget = Views.offset( target, offset );
		final T minVal = MorphologyUtils.createVariable( source, source );
		minVal.setReal( minVal.getMinValue() );
		final ExtendedRandomAccessibleInterval< T, Img< T >> extended = Views.extendValue( source, minVal );

		dilate( extended, offsetTarget, strel, numThreads );
		return target;
	}

	public static < T extends Type< T > & Comparable< T > > Img< T > dilateFull( final Img< T > source, final Shape strel, final T minVal, final int numThreads )
	{

		final long[][] dimensionsAndOffset = computeTargetImageDimensionsAndOffset( source, strel );
		final long[] targetDims = dimensionsAndOffset[ 0 ];
		final long[] offset = dimensionsAndOffset[ 1 ];

		final Img< T > target = source.factory().create( targetDims, source.firstElement().copy() );
		final IntervalView< T > offsetTarget = Views.offset( target, offset );
		final ExtendedRandomAccessibleInterval< T, Img< T >> extended = Views.extendValue( source, minVal );

		dilate( extended, offsetTarget, strel, minVal, numThreads );
		return target;
	}

	
	public static < T extends RealType< T > > void dilateInPlace( final RandomAccessibleInterval< T > source, final Interval interval, final List< Shape > strels, final int numThreads )
	{
		for ( final Shape strel : strels )
		{
			dilateInPlace( source, interval, strel, numThreads );
		}
	}

	public static < T extends Type< T > & Comparable< T > > void dilateInPlace( final RandomAccessibleInterval< T > source, final Interval interval, final List< Shape > strels, final T minVal, final int numThreads )
	{
		for ( final Shape strel : strels )
		{
			dilateInPlace( source, interval, strel, minVal, numThreads );
		}
	}

	public static < T extends RealType< T > > void dilateInPlace( final RandomAccessibleInterval< T > source, final Interval interval, final Shape strel, final int numThreads )
	{
		final T minVal = MorphologyUtils.createVariable( source, interval );
		minVal.setReal( minVal.getMinValue() );
		final ExtendedRandomAccessibleInterval< T, RandomAccessibleInterval< T >> extended = Views.extendValue( source, minVal );

		final ImgFactory< T > factory = MorphologyUtils.getSuitableFactory( interval, minVal );
		final Img< T > img = factory.create( interval, minVal );
		final long[] min = new long[ interval.numDimensions() ];
		interval.min( min );
		final IntervalView< T > translated = Views.translate( img, min );

		dilate( extended, translated, strel, numThreads );
		MorphologyUtils.copy( translated, extended );
	}

	public static < T extends Type< T > & Comparable< T > > void dilateInPlace( final RandomAccessibleInterval< T > source, final Interval interval, final Shape strel, final T minVal, final int numThreads )
	{
		// Any chance we could do something similar with a RandomAccessible?
		// Doing the following with a RandomAccessible as source generated an
		// java.lang.ArrayIndexOutOfBoundsException when the cursor neighborhood
		// meets the central point of the neighborhood, if this point is out of
		// bounds.
		// final ExtendedRandomAccessibleInterval< T, IntervalView< T >>
		// extended = Views.extendValue(
		// Views.interval( source, interval ),
		// minVal );

		final ExtendedRandomAccessibleInterval< T, RandomAccessibleInterval< T >> extended = Views.extendValue( source, minVal );

		final ImgFactory< T > factory = MorphologyUtils.getSuitableFactory( interval, minVal );
		final Img< T > img = factory.create( interval, minVal );
		final long[] min = new long[ interval.numDimensions() ];
		interval.min( min );
		final IntervalView< T > translated = Views.translate( img, min );

		dilate( extended, translated, strel, minVal, numThreads );
		MorphologyUtils.copy( translated, extended );
	}

	private Dilation()
	{}
}
