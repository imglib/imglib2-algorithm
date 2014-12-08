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

public class Erosion
{
	/**
	 * Static util to compute the final image dimensions and required offset
	 * when performing a full erosion with the specified strel.
	 * 
	 * @param source
	 *            the source image.
	 * @param strel
	 *            the strel to use for erosion.
	 * @return a 2-elements <code>long[][]</code>:
	 *         <ol start="0">
	 *         <li>a <code>long[]</code> array with the final image target
	 *         dimensions.
	 *         <li>a <code>long[]</code> array with the offset to apply to the
	 *         source image.
	 *         </ol>
	 */
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
		 * of that, this is the part that causes the full erosion not to be a
		 * real full erosion: if the structuring element has more dimensions
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

			targetDims[ d ] = Math.max( 1, d1 - ( d2 - 1 ) );
			// At least of size 1 in all dimensions. We do not prune dimensions.
		}

		// Offset coordinates so that they match the source coordinates, which
		// will not be extended.
		final long[] offset = new long[ source.numDimensions() ];
		for ( int d = 0; d < offset.length; d++ )
		{
			if ( d < sampleNeighborhood.numDimensions() )
			{
				offset[ d ] = Math.min( sampleNeighborhood.min( d ), targetDims[ d ] - 1 );
			}
			else
			{
				offset[ d ] = 0;
			}
		}

		return new long[][] { targetDims, offset };
	}

	/**
	 * Performs the erosion morphological operation, on a {@link RealType}
	 * {@link Img} using a list of {@link Shape}s as a flat structuring element.
	 * 
	 * See <a href="http://en.wikipedia.org/wiki/Erosion_(morphology)">
	 * Erosion_(morphology)</a>.
	 * <p>
	 * The structuring element is specified through a list of {@link Shape}s, to
	 * allow for performance optimization through structuring element
	 * decomposition. Each shape is processed in order as given in the list. If
	 * the list is empty, the source image is returned.
	 * <p>
	 * The result image has the same dimensions that of the source image. It is
	 * limited to flat structuring elements, only having <code>on/off</code>
	 * pixels, contrary to grayscale structuring elements. This allows to simply
	 * use a {@link Shape} as a type for these structuring elements.
	 * 
	 * @param source
	 *            the source image.
	 * @param strels
	 *            the structuring element as a list of {@link Shape}s.
	 * @param numThreads
	 *            the number of threads to use for the calculation.
	 * @param <T>
	 *            the type of the source image and the erosion result. Must be a
	 *            sub-type of <code>T extends {@link RealType}</code>.
	 * @return a new {@link Img}, of same dimensions than the source.
	 */
	public static < T extends RealType< T > > Img< T > erode( final Img< T > source, final List< Shape > strels, final int numThreads )
	{
		Img< T > target = source;
		for ( final Shape strel : strels )
		{
			target = erode( target, strel, numThreads );
		}
		return target;
	}

	/**
	 * Performs the erosion morphological operation, on an {@link Img} using a
	 * list of {@link Shape}s as a flat structuring element.
	 * 
	 * See <a href="http://en.wikipedia.org/wiki/Erosion_(morphology)">
	 * Erosion_(morphology)</a>.
	 * <p>
	 * The result image has the same dimensions that of the source image. It is
	 * limited to flat structuring elements, only having <code>on/off</code>
	 * pixels, contrary to grayscale structuring elements. This allows to simply
	 * use a {@link Shape} as a type for these structuring elements.
	 * <p>
	 * The structuring element is specified through a list of {@link Shape}s, to
	 * allow for performance optimization through structuring element
	 * decomposition. Each shape is processed in order as given in the list. If
	 * the list is empty, the source image is returned.
	 * <p>
	 * This method relies on a specified maximal value to start comparing to
	 * other pixels in the neighborhood. For this code to properly perform
	 * erosion, it is sufficient that the specified max value is larger (against
	 * {@link Comparable}) than any of the value found in the source image. This
	 * normally unseen parameter is required to operate on
	 * <code>T extends {@link Comparable} & {@link Type}</code>.
	 * 
	 * @param source
	 *            the source image.
	 * @param strels
	 *            the structuring element, as a list of {@link Shape}s.
	 * @param maxVal
	 *            a T containing set to a value larger than any of the values in
	 *            the source {@link Img} (against {@link Comparable}. This is
	 *            required to perform a proper mathematical erosion. Because we
	 *            operate on a generic {@link Type}, it has to be provided
	 *            manually.
	 * @param numThreads
	 *            the number of threads to use for the calculation.
	 * @param <T>
	 *            the type of the source image and the erosion result. Must be a
	 *            sub-type of <code>T extends {@link Comparable} & {@link Type}
	 *            </code>.
	 * @return a new {@link Img}, of same dimensions than the source.
	 */
	public static < T extends Type< T > & Comparable< T > > Img< T > erode( final Img< T > source, final List< Shape > strels, final T maxVal, final int numThreads )
	{
		Img< T > target = source;
		for ( final Shape strel : strels )
		{
			target = erode( target, strel, maxVal, numThreads );
		}
		return target;
	}

	/**
	 * Performs the erosion morphological operation, on a {@link RealType}
	 * {@link Img} using a {@link Shape} as a flat structuring element.
	 * 
	 * See <a href="http://en.wikipedia.org/wiki/Erosion_(morphology)">
	 * Erosion_(morphology)</a>.
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
	 *            the type of the source image and the erosion result. Must be a
	 *            sub-type of <code>T extends {@link RealType}</code>.
	 * @return a new {@link Img}, of same dimensions than the source.
	 */
	public static < T extends RealType< T >> Img< T > erode( final Img< T > source, final Shape strel, final int numThreads )
	{
		final Img< T > target = source.factory().create( source, source.firstElement().copy() );
		final T maxVal = source.firstElement().createVariable();
		maxVal.setReal( maxVal.getMaxValue() );
		final ExtendedRandomAccessibleInterval< T, Img< T >> extended = Views.extendValue( source, maxVal );
		erode( extended, target, strel, numThreads );
		return target;
	}

	/**
	 * Performs the erosion morphological operation, on an {@link Img} using a
	 * {@link Shape} as a flat structuring element.
	 * 
	 * See <a href="http://en.wikipedia.org/wiki/Erosion_(morphology)">
	 * Erosion_(morphology)</a>.
	 * <p>
	 * The result image has the same dimensions that of the source image. It is
	 * limited to flat structuring elements, only having <code>on/off</code>
	 * pixels, contrary to grayscale structuring elements. This allows to simply
	 * use a {@link Shape} as a type for these structuring elements.
	 * <p>
	 * This method relies on a specified maximal value to start comparing to
	 * other pixels in the neighborhood. For this code to properly perform
	 * erosion, it is sufficient that the specified max value is larger (against
	 * {@link Comparable}) than any of the value found in the source image. This
	 * normally unseen parameter is required to operate on
	 * <code>T extends {@link Comparable} & {@link Type}</code>.
	 * 
	 * @param source
	 *            the source image.
	 * @param strel
	 *            the structuring element as a {@link Shape}.
	 * @param maxVal
	 *            a T containing set to a value larger than any of the values in
	 *            the source {@link Img} (against {@link Comparable}. This is
	 *            required to perform a proper mathematical erosion. Because we
	 *            operate on a generic {@link Type}, it has to be provided
	 *            manually.
	 * @param numThreads
	 *            the number of threads to use for the calculation.
	 * @param <T>
	 *            the type of the source image and the erosion result. Must be a
	 *            sub-type of <code>T extends {@link Comparable} & {@link Type}
	 *            </code>.
	 * @return a new {@link Img}, of same dimensions than the source.
	 */
	public static < T extends Type< T > & Comparable< T > > Img< T > erode( final Img< T > source, final Shape strel, final T maxVal, final int numThreads )
	{
		final Img< T > target = source.factory().create( source, source.firstElement().copy() );
		final ExtendedRandomAccessibleInterval< T, Img< T >> extended = Views.extendValue( source, maxVal );
		erode( extended, target, strel, maxVal, numThreads );
		return target;
	}

	/**
	 * Performs the erosion morphological operation, on a {@link RealType}
	 * {@link RandomAccessible} as a source and writing results in an
	 * {@link IterableInterval}.
	 * 
	 * See <a href="http://en.wikipedia.org/wiki/Erosion_(morphology)">
	 * Erosion_(morphology)</a>.
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
	 *            the source {@link RandomAccessible}, must be sufficiently
	 *            padded.
	 * @param target
	 *            the target image.
	 * @param strels
	 *            the structuring element, as a list of {@link Shape}s.
	 * @param numThreads
	 *            the number of threads to use for the calculation.
	 */
	public static < T extends RealType< T >> void erode( final RandomAccessible< T > source, final IterableInterval< T > target, final List< Shape > strels, final int numThreads )
	{
		final T maxVal = MorphologyUtils.createVariable( source, target );
		maxVal.setReal( maxVal.getMaxValue() );
		erode( source, target, strels, maxVal, numThreads );
	}

	/**
	 * Performs the erosion morphological operation, using a
	 * {@link RandomAccessible} as a source and writing results in an
	 * {@link IterableInterval}.
	 * 
	 * See <a href="http://en.wikipedia.org/wiki/Erosion_(morphology)">
	 * Erosion_(morphology)</a>.
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
	 * This method relies on a specified maximal value to start comparing to
	 * other pixels in the neighborhood. For this code to properly perform
	 * erosion, it is sufficient that the specified max value is larger (against
	 * {@link Comparable}) than any of the value found in the source image. This
	 * normally unseen parameter is required to operate on
	 * <code>T extends {@link Comparable} & {@link Type}</code>.
	 * 
	 * @param source
	 *            the source {@link RandomAccessible}, must be sufficiently
	 *            padded.
	 * @param target
	 *            the target image.
	 * @param strels
	 *            the structuring element, as a list of {@link Shape}s.
	 * @param maxVal
	 *            a T containing set to a value larger than any of the values in
	 *            the source (against {@link Comparable}. This is required to
	 *            perform a proper mathematical erosion. Because we operate on a
	 *            generic {@link Type}, it has to be provided manually.
	 * @param numThreads
	 *            the number of threads to use for the calculation.
	 * @param <T>
	 *            the type of the source image and the erosion result. Must be a
	 *            sub-type of <code>T extends {@link Comparable} & {@link Type}
	 *            </code>.
	 */
	public static < T extends Type< T > & Comparable< T > > void erode( final RandomAccessible< T > source, final IterableInterval< T > target, final List< Shape > strels, final T maxVal, final int numThreads )
	{
		if ( strels.isEmpty() ) { return; }
		if ( strels.size() == 1 )
		{
			erode( source, target, strels.get( 0 ), maxVal, numThreads );
			return;
		}

		final ImgFactory< T > factory = MorphologyUtils.getSuitableFactory( target, maxVal );
		Img< T > temp = factory.create( target, maxVal );
		final long[] translation = new long[ target.numDimensions() ];
		target.min( translation );
		IntervalView< T > translated = Views.translate( temp, translation );

		// First shape.
		erode( source, translated, strels.get( 0 ), maxVal, numThreads );

		// Middle shapes.
		for ( int i = 1; i < strels.size() - 1; i++ )
		{
			temp = erode( temp, strels.get( i ), maxVal, numThreads );
		}

		// Last shape
		translated = Views.translate( temp, translation );
		final ExtendedRandomAccessibleInterval< T, IntervalView< T >> extended = Views.extendValue( translated, maxVal );
		erode( extended, target, strels.get( strels.size() - 1 ), maxVal, numThreads );
	}

	/**
	 * Performs the erosion morphological operation, on a {@link RealType}
	 * {@link RandomAccessible} as a source and writing results in an
	 * {@link IterableInterval}.
	 * 
	 * See <a href="http://en.wikipedia.org/wiki/Erosion_(morphology)">
	 * Erosion_(morphology)</a>.
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
	 *            the source {@link RandomAccessible}, must be sufficiently
	 *            padded.
	 * @param target
	 *            the target image.
	 * @param strel
	 *            the structuring element, as a {@link Shape}.
	 * @param numThreads
	 *            the number of threads to use for the calculation.
	 */
	public static < T extends RealType< T >> void erode( final RandomAccessible< T > source, final IterableInterval< T > target, final Shape strel, final int numThreads )
	{
		final T maxVal = MorphologyUtils.createVariable( source, target );
		maxVal.setReal( maxVal.getMaxValue() );
		erode( source, target, strel, maxVal, numThreads );
	}

	/**
	 * Performs the erosion morphological operation, using a
	 * {@link RandomAccessible} as a source and writing results in an
	 * {@link IterableInterval}.
	 * 
	 * See <a href="http://en.wikipedia.org/wiki/Erosion_(morphology)">
	 * Erosion_(morphology)</a>.
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
	 * This method relies on a specified maximal value to start comparing to
	 * other pixels in the neighborhood. For this code to properly perform
	 * erosion, it is sufficient that the specified max value is larger (against
	 * {@link Comparable}) than any of the value found in the source image. This
	 * normally unseen parameter is required to operate on
	 * <code>T extends {@link Comparable} & {@link Type}</code>.
	 * 
	 * @param source
	 *            the source {@link RandomAccessible}, must be sufficiently
	 *            padded.
	 * @param target
	 *            the target image.
	 * @param strel
	 *            the structuring element, as a {@link Shape}.
	 * @param maxVal
	 *            a T containing set to a value larger than any of the values in
	 *            the source (against {@link Comparable}. This is required to
	 *            perform a proper mathematical erosion. Because we operate on a
	 *            generic {@link Type}, it has to be provided manually.
	 * @param numThreads
	 *            the number of threads to use for the calculation.
	 * @param <T>
	 *            the type of the source image and the erosion result. Must be a
	 *            sub-type of <code>T extends {@link Comparable} & {@link Type}
	 *            </code>.
	 */
	public static < T extends Type< T > & Comparable< T > > void erode( final RandomAccessible< T > source, final IterableInterval< T > target, final Shape strel, final T maxVal, int numThreads )
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

		final Object tmp = maxVal;
		if ( tmp instanceof BitType )
		{
			/*
			 * Optimization for BitType
			 */

			for ( int i = 0; i < threads.length; i++ )
			{
				final Chunk chunk = chunks.get( i );
				threads[ i ] = new Thread( "Morphology erode thread " + i )
				{
					@Override
					public void run()
					{
						final RandomAccess< Neighborhood< T >> randomAccess = accessible.randomAccess( target );
						@SuppressWarnings( "unchecked" )
						final Object tmp2 = target.cursor();
						final Cursor< BitType > cursorTarget = ( Cursor< BitType > ) tmp2;
						cursorTarget.jumpFwd( chunk.getStartPosition() );

						for ( long steps = 0; steps < chunk.getLoopSize(); steps++ )
						{
							cursorTarget.fwd();
							randomAccess.setPosition( cursorTarget );
							@SuppressWarnings( "unchecked" )
							final Object tmp3 = randomAccess.get();
							final Neighborhood< BitType > neighborhood = ( Neighborhood< BitType > ) tmp3;
							final Cursor< BitType > nc = neighborhood.cursor();

							cursorTarget.get().set( true );
							while ( nc.hasNext() )
							{
								nc.fwd();
								final BitType val = nc.get();
								if ( !val.get() )
								{
									cursorTarget.get().set( false );
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
				threads[ i ] = new Thread( "Morphology erode thread " + i )
				{
					@Override
					public void run()
					{
						final RandomAccess< Neighborhood< T >> randomAccess = accessible.randomAccess( target );
						final Cursor< T > cursorTarget = target.cursor();
						cursorTarget.jumpFwd( chunk.getStartPosition() );

						final T max = MorphologyUtils.createVariable( source, target );
						for ( long steps = 0; steps < chunk.getLoopSize(); steps++ )
						{
							cursorTarget.fwd();
							randomAccess.setPosition( cursorTarget );
							final Neighborhood< T > neighborhood = randomAccess.get();
							final Cursor< T > nc = neighborhood.cursor();

							/*
							 * Look for max in the neighborhood.
							 */

							max.set( maxVal );
							while ( nc.hasNext() )
							{
								nc.fwd();
								final T val = nc.get();
								// We need only Comparable to do this:
								if ( val.compareTo( max ) < 0 )
								{
									max.set( val );
								}
							}
							cursorTarget.get().set( max );
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
	 * Performs the erosion morphological operation, on a {@link RealType}
	 * {@link Img} using a list of {@link Shape}s as a flat structuring element.
	 * 
	 * See <a href="http://en.wikipedia.org/wiki/Erosion_(morphology)">
	 * Erosion_(morphology)</a>.
	 * <p>
	 * This method performs what is called the 'full' erosion. That is: the
	 * result image has its dimension eroded by the structuring element, with
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
	 * the full erosion. Indeed, if the structuring element has more dimensions
	 * than the source {@link Img}, they are ignored, and the returned
	 * {@link Img} has the same number of dimensions that of the source (but
	 * eroded). This is due to the fact that we use a {@link Shape} for
	 * structuring elements, and that it does not return a number of dimensions.
	 * The neighborhood created have therefore at most as many dimensions as the
	 * source image. The real, full erosion results should have a number of
	 * dimensions equals to the maximum of the number of dimension of both
	 * source and structuring element.
	 * 
	 * @param source
	 *            the source image.
	 * @param strels
	 *            the structuring element as a list of {@link Shape}s.
	 * @param numThreads
	 *            the number of threads to use for the calculation.
	 * @param <T>
	 *            the type of the source image and the erosion result. Must be a
	 *            sub-type of <code>T extends {@link RealType}</code>.
	 * @return a new {@link Img}, possibly of larger dimensions than the source.
	 */
	public static < T extends RealType< T > > Img< T > erodeFull( final Img< T > source, final List< Shape > strels, final int numThreads )
	{
		Img< T > target = source;
		for ( final Shape strel : strels )
		{
			target = erodeFull( target, strel, numThreads );
		}
		return target;
	}

	/**
	 * Performs the erosion morphological operation, on an {@link Img} using a
	 * list of {@link Shape}s as a flat structuring element.
	 * 
	 * See <a href="http://en.wikipedia.org/wiki/Erosion_(morphology)">
	 * Erosion_(morphology)</a>.
	 * <p>
	 * This method performs what is called the 'full' erosion. That is: the
	 * result image has its dimension eroded by the structuring element, with
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
	 * This method relies on a specified maximal value to start comparing to
	 * other pixels in the neighborhood. For this code to properly perform
	 * erosion, it is sufficient that the specified max value is larger (against
	 * {@link Comparable}) than any of the value found in the source image. This
	 * normally unseen parameter is required to operate on
	 * <code>T extends {@link Comparable} & {@link Type}</code>.
	 * <p>
	 * <b>Warning:</b> Current implementation does not do <i>stricto sensu</i>
	 * the full erosion. Indeed, if the structuring element has more dimensions
	 * than the source {@link Img}, they are ignored, and the returned
	 * {@link Img} has the same number of dimensions that of the source (but
	 * eroded). This is due to the fact that we use a {@link Shape} for
	 * structuring elements, and that it does not return a number of dimensions.
	 * The neighborhood created have therefore at most as many dimensions as the
	 * source image. The real, full erosion results should have a number of
	 * dimensions equals to the maximum of the number of dimension of both
	 * source and structuring element.
	 * 
	 * @param source
	 *            the source image.
	 * @param strels
	 *            the structuring element as a list of {@link Shape}s.
	 * @param maxVal
	 *            a T containing set to a value larger than any of the values in
	 *            the source {@link Img} (against {@link Comparable}. This is
	 *            required to perform a proper mathematical erosion. Because we
	 *            operate on a generic {@link Type}, it has to be provided
	 *            manually.
	 * @param numThreads
	 *            the number of threads to use for the calculation.
	 * @param <T>
	 *            the type of the source image and the erosion result. Must be a
	 *            sub-type of <code>T extends {@link Comparable} & {@link Type}
	 *            </code>.
	 * @return a new {@link Img}, possibly of larger dimensions than the source.
	 */
	public static < T extends Type< T > & Comparable< T > > Img< T > erodeFull( final Img< T > source, final List< Shape > strels, final T maxVal, final int numThreads )
	{
		Img< T > target = source;
		for ( final Shape strel : strels )
		{
			target = erodeFull( target, strel, maxVal, numThreads );
		}
		return target;
	}

	/**
	 * Performs the erosion morphological operation, on a {@link RealType}
	 * {@link Img} using a list of {@link Shape}s as a flat structuring element.
	 * 
	 * See <a href="http://en.wikipedia.org/wiki/Erosion_(morphology)">
	 * Erosion_(morphology)</a>.
	 * <p>
	 * This method performs what is called the 'full' erosion. That is: the
	 * result image has its dimension eroded by the structuring element, with
	 * respect to the source image. It is limited to flat structuring elements,
	 * only having <code>on/off</code> pixels, contrary to grayscale structuring
	 * elements. This allows to simply use a {@link Shape} as a type for these
	 * structuring elements.
	 * <p>
	 * <b>Warning:</b> Current implementation does not do <i>stricto sensu</i>
	 * the full erosion. Indeed, if the structuring element has more dimensions
	 * than the source {@link Img}, they are ignored, and the returned
	 * {@link Img} has the same number of dimensions that of the source (but
	 * eroded). This is due to the fact that we use a {@link Shape} for
	 * structuring elements, and that it does not return a number of dimensions.
	 * The neighborhood created have therefore at most as many dimensions as the
	 * source image. The real, full erosion results should have a number of
	 * dimensions equals to the maximum of the number of dimension of both
	 * source and structuring element.
	 * 
	 * @param source
	 *            the source image.
	 * @param strel
	 *            the structuring element as {@link Shape}.
	 * @param numThreads
	 *            the number of threads to use for the calculation.
	 * @param <T>
	 *            the type of the source image and the erosion result. Must be a
	 *            sub-type of <code>T extends {@link RealType}</code>.
	 * @return a new {@link Img}, possibly of larger dimensions than the source.
	 */
	public static < T extends RealType< T >> Img< T > erodeFull( final Img< T > source, final Shape strel, final int numThreads )
	{
		final long[][] dimensionsAndOffset = computeTargetImageDimensionsAndOffset( source, strel );

		final long[] targetDims = dimensionsAndOffset[ 0 ];
		final long[] offset = dimensionsAndOffset[ 1 ];

		final Img< T > target = source.factory().create( targetDims, source.firstElement().copy() );
		final IntervalView< T > offsetTarget = Views.offset( target, offset );
		final T maxVal = MorphologyUtils.createVariable( source, source );
		maxVal.setReal( maxVal.getMaxValue() );
		final ExtendedRandomAccessibleInterval< T, Img< T >> extended = Views.extendValue( source, maxVal );

		erode( extended, offsetTarget, strel, numThreads );
		return target;
	}

	/**
	 * Performs the erosion morphological operation, on an {@link Img} using a
	 * list of {@link Shape}s as a flat structuring element.
	 * 
	 * See <a href="http://en.wikipedia.org/wiki/Erosion_(morphology)">
	 * Erosion_(morphology)</a>.
	 * <p>
	 * This method performs what is called the 'full' erosion. That is: the
	 * result image has its dimension eroded by the structuring element, with
	 * respect to the source image. It is limited to flat structuring elements,
	 * only having <code>on/off</code> pixels, contrary to grayscale structuring
	 * elements. This allows to simply use a {@link Shape} as a type for these
	 * structuring elements.
	 * <p>
	 * This method relies on a specified maximal value to start comparing to
	 * other pixels in the neighborhood. For this code to properly perform
	 * erosion, it is sufficient that the specified max value is larger (against
	 * {@link Comparable}) than any of the value found in the source image. This
	 * normally unseen parameter is required to operate on
	 * <code>T extends {@link Comparable} & {@link Type}</code>.
	 * <p>
	 * <b>Warning:</b> Current implementation does not do <i>stricto sensu</i>
	 * the full erosion. Indeed, if the structuring element has more dimensions
	 * than the source {@link Img}, they are ignored, and the returned
	 * {@link Img} has the same number of dimensions that of the source (but
	 * eroded). This is due to the fact that we use a {@link Shape} for
	 * structuring elements, and that it does not return a number of dimensions.
	 * The neighborhood created have therefore at most as many dimensions as the
	 * source image. The real, full erosion results should have a number of
	 * dimensions equals to the maximum of the number of dimension of both
	 * source and structuring element.
	 * 
	 * @param source
	 *            the source image.
	 * @param strel
	 *            the structuring element as a {@link Shape}.
	 * @param maxVal
	 *            a T containing set to a value larger than any of the values in
	 *            the source {@link Img} (against {@link Comparable}. This is
	 *            required to perform a proper mathematical erosion. Because we
	 *            operate on a generic {@link Type}, it has to be provided
	 *            manually.
	 * @param numThreads
	 *            the number of threads to use for the calculation.
	 * @param <T>
	 *            the type of the source image and the erosion result. Must be a
	 *            sub-type of <code>T extends {@link Comparable} & {@link Type}
	 *            </code>.
	 * @return a new {@link Img}, possibly of larger dimensions than the source.
	 */
	public static < T extends Type< T > & Comparable< T > > Img< T > erodeFull( final Img< T > source, final Shape strel, final T maxVal, final int numThreads )
	{

		final long[][] dimensionsAndOffset = computeTargetImageDimensionsAndOffset( source, strel );
		final long[] targetDims = dimensionsAndOffset[ 0 ];
		final long[] offset = dimensionsAndOffset[ 1 ];

		final Img< T > target = source.factory().create( targetDims, source.firstElement().copy() );
		final IntervalView< T > offsetTarget = Views.offset( target, offset );
		final ExtendedRandomAccessibleInterval< T, Img< T >> extended = Views.extendValue( source, maxVal );

		erode( extended, offsetTarget, strel, maxVal, numThreads );
		return target;
	}

	/**
	 * Performs the erosion morphological operation, on a {@link RealType}
	 * {@link RandomAccessibleInterval} using a {@link Shape} as a flat
	 * structuring element.
	 * 
	 * See <a href="http://en.wikipedia.org/wiki/Erosion_(morphology)">
	 * Erosion_(morphology)</a>.
	 * <p>
	 * The result is written in the source image. This method is limited to flat
	 * structuring elements, only having <code>on/off</code> pixels, contrary to
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
	public static < T extends RealType< T > > void erodeInPlace( final RandomAccessibleInterval< T > source, final Interval interval, final List< Shape > strels, final int numThreads )
	{
		for ( final Shape strel : strels )
		{
			erodeInPlace( source, interval, strel, numThreads );
		}
	}

	/**
	 * Performs the erosion morphological operation, on a {@link RealType}
	 * {@link RandomAccessibleInterval} using a {@link Shape} as a flat
	 * structuring element.
	 * 
	 * See <a href="http://en.wikipedia.org/wiki/Erosion_(morphology)">
	 * Erosion_(morphology)</a>.
	 * <p>
	 * The result is written in the source image. This method is limited to flat
	 * structuring elements, only having <code>on/off</code> pixels, contrary to
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
	 * the list is empty, the source image is returned. *
	 * <p>
	 * This method relies on a specified maximal value to start comparing to
	 * other pixels in the neighborhood. For this code to properly perform
	 * erosion, it is sufficient that the specified max value is larger (against
	 * {@link Comparable}) than any of the value found in the source image. This
	 * normally unseen parameter is required to operate on
	 * <code>T extends {@link Comparable} & {@link Type}</code>.
	 * 
	 * @param source
	 *            the source image.
	 * @param interval
	 *            an interval in the source image to process.
	 * @param strels
	 *            the structuring element as a list of {@link Shape}s.
	 * @param maxVal
	 *            a T containing set to a value larger than any of the values in
	 *            the source (against {@link Comparable}. This is required to
	 *            perform a proper mathematical erosion. Because we operate on a
	 *            generic {@link Type}, it has to be provided manually.
	 * @param numThreads
	 *            the number of threads to use for the calculation.
	 * @param <T>
	 *            the type of the source image. Must be a sub-type of
	 *            <code>T extends {@link Comparable} & {@link Type}</code>.
	 */
	public static < T extends Type< T > & Comparable< T > > void erodeInPlace( final RandomAccessibleInterval< T > source, final Interval interval, final List< Shape > strels, final T maxVal, final int numThreads )
	{
		for ( final Shape strel : strels )
		{
			erodeInPlace( source, interval, strel, maxVal, numThreads );
		}
	}

	/**
	 * Performs the erosion morphological operation, on a {@link RealType}
	 * {@link RandomAccessibleInterval} using a {@link Shape} as a flat
	 * structuring element.
	 * 
	 * See <a href="http://en.wikipedia.org/wiki/Erosion_(morphology)">
	 * Erosion_(morphology)</a>.
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
	public static < T extends RealType< T > > void erodeInPlace( final RandomAccessibleInterval< T > source, final Interval interval, final Shape strel, final int numThreads )
	{
		final T maxVal = MorphologyUtils.createVariable( source, interval );
		maxVal.setReal( maxVal.getMaxValue() );
		final ExtendedRandomAccessibleInterval< T, RandomAccessibleInterval< T >> extended = Views.extendValue( source, maxVal );

		final ImgFactory< T > factory = MorphologyUtils.getSuitableFactory( interval, maxVal );
		final Img< T > img = factory.create( interval, maxVal );
		final long[] min = new long[ interval.numDimensions() ];
		interval.min( min );
		final IntervalView< T > translated = Views.translate( img, min );

		erode( extended, translated, strel, numThreads );
		MorphologyUtils.copy( translated, extended, numThreads );
	}

	/**
	 * Performs the erosion morphological operation, on a {@link RealType}
	 * {@link RandomAccessibleInterval} using a {@link Shape} as a flat
	 * structuring element.
	 * 
	 * See <a href="http://en.wikipedia.org/wiki/Erosion_(morphology)">
	 * Erosion_(morphology)</a>.
	 * <p>
	 * The result is written in the source image. This method is limited to flat
	 * structuring elements, only having <code>on/off</code> pixels, contrary to
	 * grayscale structuring elements. This allows to simply use a {@link Shape}
	 * as a type for these structuring elements.
	 * <p>
	 * It is the caller responsibility to ensure that the source is sufficiently
	 * padded to properly cover the target range plus the shape size. See
	 * <i>e.g.</i> {@link Views#extendValue(RandomAccessibleInterval, Type)} *
	 * <p>
	 * This method relies on a specified maximal value to start comparing to
	 * other pixels in the neighborhood. For this code to properly perform
	 * erosion, it is sufficient that the specified max value is larger (against
	 * {@link Comparable}) than any of the value found in the source image. This
	 * normally unseen parameter is required to operate on
	 * <code>T extends {@link Comparable} & {@link Type}</code>.
	 * 
	 * @param source
	 *            the source image.
	 * @param interval
	 *            an interval in the source image to process.
	 * @param strel
	 *            the structuring element as a {@link Shape}.
	 * @param maxVal
	 *            a T containing set to a value larger than any of the values in
	 *            the source (against {@link Comparable}. This is required to
	 *            perform a proper mathematical erosion. Because we operate on a
	 *            generic {@link Type}, it has to be provided manually.
	 * @param numThreads
	 *            the number of threads to use for the calculation.
	 * @param <T>
	 *            the type of the source image. Must be a sub-type of
	 *            <code>T extends {@link Comparable} & {@link Type}</code>.
	 */
	public static < T extends Type< T > & Comparable< T > > void erodeInPlace( final RandomAccessibleInterval< T > source, final Interval interval, final Shape strel, final T maxVal, final int numThreads )
	{

		final ExtendedRandomAccessibleInterval< T, RandomAccessibleInterval< T >> extended = Views.extendValue( source, maxVal );

		final ImgFactory< T > factory = MorphologyUtils.getSuitableFactory( interval, maxVal );
		final Img< T > img = factory.create( interval, maxVal );
		final long[] min = new long[ interval.numDimensions() ];
		interval.min( min );
		final IntervalView< T > translated = Views.translate( img, min );

		erode( extended, translated, strel, maxVal, numThreads );
		MorphologyUtils.copy( translated, extended, numThreads );
	}

	/**
	 * Private constructor. Unused.
	 */
	private Erosion()
	{}
}
