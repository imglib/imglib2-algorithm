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
			target = erodeFull( target, strel, numThreads );
		}
		return MorphologyUtils.copyCropped( target, source, numThreads );
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
			target = erodeFull( target, strel, maxVal, numThreads );
		}
		return MorphologyUtils.copyCropped( target, source, numThreads );
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
		
		// Compute inflated temp size and offset.
		final long[] targetDims = new long[ target.numDimensions() ];
		final long[] translation = new long[ target.numDimensions() ];
		for ( int d = 0; d < targetDims.length; d++ )
		{
			targetDims[ d ] = target.dimension( d );
			translation[ d ] = target.min( d );
		}
		for ( final Shape strel : strels )
		{
			final Neighborhood< BitType > nh = MorphologyUtils.getNeighborhood( strel, target );
			for ( int d = 0; d < translation.length; d++ )
			{
				translation[ d ] -= nh.dimension( d ) / 2;
				targetDims[ d ] += nh.dimension( d ) - 1;
			}
		}

		// First shape -> write to enlarged temp.
		final ImgFactory< T > factory = MorphologyUtils.getSuitableFactory( targetDims, maxVal );
		Img< T > temp = factory.create( targetDims, maxVal );
		final IntervalView< T > translated = Views.translate( temp, translation );
		erode( source, translated, strels.get( 0 ), maxVal, numThreads );
		
		// Middle and last shapes -> do erosion.
		for ( int i = 1; i < strels.size(); i++ )
		{
			temp = erode( temp, strels.get( i ), maxVal, numThreads );
		}

		// Copy-crop back on target, focusing on the center part.
		final long[] offset = new long[ target.numDimensions() ];
		for ( int d = 0; d < offset.length; d++ )
		{
			offset[ d ] = target.min( d ) - ( ( temp.dimension( d ) - target.dimension( d ) ) / 2 );
		}
		MorphologyUtils.copy2( Views.translate( temp, offset ), target, numThreads );
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
						final Object tmp2 = target.cursor();
						@SuppressWarnings( "unchecked" )
						final Cursor< BitType > cursorTarget = ( Cursor< BitType > ) tmp2;
						cursorTarget.jumpFwd( chunk.getStartPosition() );

						for ( long steps = 0; steps < chunk.getLoopSize(); steps++ )
						{
							cursorTarget.fwd();
							randomAccess.setPosition( cursorTarget );
							final Object tmp3 = randomAccess.get();
							@SuppressWarnings( "unchecked" )
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
	 * result image has its dimension enlarged by the structuring element, with
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
	 * result image has its dimension enlarged by the structuring element, with
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
	 * result image has its dimension enlarged by the structuring element, with
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
		final long[][] dimensionsAndOffset = MorphologyUtils.computeTargetImageDimensionsAndOffset( source, strel );

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
	 * result image has its dimension enlarged by the structuring element, with
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

		final long[][] dimensionsAndOffset = MorphologyUtils.computeTargetImageDimensionsAndOffset( source, strel );
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
	public static < T extends RealType< T > > void erodeInPlace( final RandomAccessible< T > source, final Interval interval, final List< Shape > strels, final int numThreads )
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
	public static < T extends RealType< T > > void erodeInPlace( final RandomAccessible< T > source, final Interval interval, final Shape strel, final int numThreads )
	{
		final T maxVal = MorphologyUtils.createVariable( source, interval );
		final ImgFactory< T > factory = MorphologyUtils.getSuitableFactory( interval, maxVal );
		final Img< T > img = factory.create( interval, maxVal );
		final long[] min = new long[ interval.numDimensions() ];
		interval.min( min );
		final IntervalView< T > translated = Views.translate( img, min );

		erode( source, translated, strel, numThreads );
		MorphologyUtils.copy( translated, source, numThreads );
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
