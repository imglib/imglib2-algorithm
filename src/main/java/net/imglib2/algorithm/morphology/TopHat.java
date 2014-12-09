package net.imglib2.algorithm.morphology;

import java.util.List;

import net.imglib2.Interval;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.algorithm.region.localneighborhood.Shape;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.operators.Sub;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

public class TopHat
{

	/*
	 * To new Img
	 */

	public static < T extends RealType< T >> Img< T > topHat( final Img< T > source, final Shape strel, final int numThreads )
	{
		final Img< T > opened = Opening.open( source, strel, numThreads );
		MorphologyUtils.subABA( opened, source, numThreads );
		return opened;
	}

	public static < T extends RealType< T >> Img< T > topHat( final Img< T > source, final List< Shape > strels, final int numThreads )
	{
		final Img< T > opened = Opening.open( source, strels, numThreads );
		MorphologyUtils.subABA( opened, source, numThreads );
		return opened;
	}

	public static < T extends Type< T > & Comparable< T > & Sub< T > > Img< T > topHat( final Img< T > source, final Shape strel, final T minVal, final T maxVal, final int numThreads )
	{
		final Img< T > opened = Opening.open( source, strel, minVal, maxVal, numThreads );
		MorphologyUtils.subABA( opened, source, numThreads );
		return opened;
	}

	public static < T extends Type< T > & Comparable< T > & Sub< T > > Img< T > topHat( final Img< T > source, final List< Shape > strels, final T minVal, final T maxVal, final int numThreads )
	{
		final Img< T > opened = Opening.open( source, strels, minVal, maxVal, numThreads );
		MorphologyUtils.subABA( opened, source, numThreads );
		return opened;
	}

	/*
	 * In place
	 */

	public static < T extends RealType< T >> void topHatInPlace( final RandomAccessible< T > source, final Interval interval, final Shape strel, final int numThreads )
	{
		// Prepare tmp holder
		final T minVal = MorphologyUtils.createVariable( source, interval );
		final ImgFactory< T > factory = MorphologyUtils.getSuitableFactory( interval, minVal );
		final Img< T > img = factory.create( interval, minVal );
		final long[] min = new long[ interval.numDimensions() ];
		interval.min( min );
		final IntervalView< T > translated = Views.translate( img, min );

		Opening.open( source, translated, strel, numThreads );
		MorphologyUtils.subABA2( translated, source, numThreads );
	}

	public static < T extends RealType< T >> void topHatInPlace( final RandomAccessible< T > source, final Interval interval, final List< Shape > strels, final int numThreads )
	{
		// Prepare tmp holder
		final T minVal = MorphologyUtils.createVariable( source, interval );
		final ImgFactory< T > factory = MorphologyUtils.getSuitableFactory( interval, minVal );
		final Img< T > img = factory.create( interval, minVal );
		final long[] min = new long[ interval.numDimensions() ];
		interval.min( min );
		final IntervalView< T > translated = Views.translate( img, min );

		Opening.open( source, translated, strels, numThreads );
		MorphologyUtils.subABA2( translated, source, numThreads );
	}

	public static < T extends Type< T > & Comparable< T > & Sub< T >> void topHatInPlace( final RandomAccessible< T > source, final Interval interval, final Shape strel, final T minVal, final T maxVal, final int numThreads )
	{
		// Prepare tmp holder
		final ImgFactory< T > factory = MorphologyUtils.getSuitableFactory( interval, minVal );
		final Img< T > img = factory.create( interval, minVal );
		final long[] min = new long[ interval.numDimensions() ];
		interval.min( min );
		final IntervalView< T > translated = Views.translate( img, min );

		Opening.open( source, translated, strel, minVal, maxVal, numThreads );
		MorphologyUtils.subABA2( translated, source, numThreads );
	}

	public static < T extends Type< T > & Comparable< T > & Sub< T >> void topHatInPlace( final RandomAccessible< T > source, final Interval interval, final List< Shape > strels, final T minVal, final T maxVal, final int numThreads )
	{
		// Prepare tmp holder
		final ImgFactory< T > factory = MorphologyUtils.getSuitableFactory( interval, minVal );
		final Img< T > img = factory.create( interval, minVal );
		final long[] min = new long[ interval.numDimensions() ];
		interval.min( min );
		final IntervalView< T > translated = Views.translate( img, min );

		Opening.open( source, translated, strels, minVal, maxVal, numThreads );
		MorphologyUtils.subABA2( translated, source, numThreads );
	}

	/*
	 * To provided target
	 */

	public static < T extends RealType< T >> void topHat( final RandomAccessible< T > source, final IterableInterval< T > target, final Shape strel, final int numThreads )
	{
		Opening.open( source, target, strel, numThreads );
		MorphologyUtils.subBAB( source, target, numThreads );
	}

	public static < T extends RealType< T >> void topHat( final RandomAccessible< T > source, final IterableInterval< T > target, final List< Shape > strels, final int numThreads )
	{
		Opening.open( source, target, strels, numThreads );
		MorphologyUtils.subBAB( source, target, numThreads );
	}

	public static < T extends Type< T > & Comparable< T > & Sub< T >> void topHat( final RandomAccessible< T > source, final IterableInterval< T > target, final Shape strel, final T minVal, final T maxVal, final int numThreads )
	{
		Opening.open( source, target, strel, minVal, maxVal, numThreads );
		MorphologyUtils.subBAB( source, target, numThreads );
	}

	public static < T extends Type< T > & Comparable< T > & Sub< T >> void topHat( final RandomAccessible< T > source, final IterableInterval< T > target, final List< Shape > strels, final T minVal, final T maxVal, final int numThreads )
	{
		Opening.open( source, target, strels, minVal, maxVal, numThreads );
		MorphologyUtils.subBAB( source, target, numThreads );
	}


	/**
	 * Private default constructor.
	 */
	private TopHat()
	{}

}
