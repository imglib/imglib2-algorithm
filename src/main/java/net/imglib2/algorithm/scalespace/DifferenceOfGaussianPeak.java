package net.imglib2.algorithm.scalespace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.algorithm.scalespace.Blob.SpecialPoint;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

/**
 * Intermediate use class needed to compute and subpixel localize blobs found by
 * the scale-space algorithm. This class needs not to be used by end-user, which
 * will be return a collection of Blob.
 */
class DifferenceOfGaussianPeak extends Point
{

	private SpecialPoint peakType;

	private final double value;


	DifferenceOfGaussianPeak( final Localizable position, final double value, final SpecialPoint peakType )
	{
		super( position );
		this.value = value;
		this.peakType = peakType;
	}

	public double getValue()
	{
		return value;
	}

	public SpecialPoint getPeakType()
	{
		return peakType;
	}

	public void setPeakType( final SpecialPoint peakType )
	{
		this.peakType = peakType;
	}

	public boolean isMin()
	{
		return peakType == SpecialPoint.MIN;
	}

	public boolean isMax()
	{
		return peakType == SpecialPoint.MAX;
	}

	public boolean isValid()
	{
		return peakType != SpecialPoint.INVALID;
	}

	private static final SpecialPoint isSpecialPointFloat( final Neighborhood< FloatType > neighborhood, final float centerValue )
	{
		final Cursor< FloatType > c = neighborhood.cursor();
		while ( c.hasNext() )
		{
			final float v = c.next().get();
			if ( centerValue < v )
			{
				// it can only be a minimum
				while ( c.hasNext() )
					if ( centerValue > c.next().get() )
						return SpecialPoint.INVALID;
				// this mixup is intended, a minimum in the 2nd derivation is a
				// maxima in image space and vice versa
				return SpecialPoint.MAX;

			}
			else if ( centerValue > v )
			{

				// it can only be a maximum
				while ( c.hasNext() )
					if ( centerValue < c.next().get() )
						return SpecialPoint.INVALID;
				// this mixup is intended, a minimum in the 2nd derivation is a
				// maxima in image space and vice versa
				return SpecialPoint.MIN;
			}
		}
		return SpecialPoint.MIN; // all neighboring pixels have the same value.
									// count it as MIN.
	}

	public final static List< DifferenceOfGaussianPeak > findPeaks( final Img< FloatType > img, final double threshold, final int numThreads )
	{
		final List< DifferenceOfGaussianPeak > dogPeaks =
				Collections.synchronizedList( new ArrayList< DifferenceOfGaussianPeak >() );

		final Interval full = Intervals.expand( img, -1 );
		final int n = img.numDimensions();
		final int splitd = n - 1;
		final int numTasks = numThreads <= 1 ? 1 : ( int ) Math.min( full.dimension( splitd ), numThreads * 20 );
		final long dsize = full.dimension( splitd ) / numTasks;
		final long[] min = new long[ n ];
		final long[] max = new long[ n ];
		full.min( min );
		full.max( max );

		final RectangleShape shape = new RectangleShape( 1, true );

		final ExecutorService ex = Executors.newFixedThreadPool( numThreads );
		for ( int taskNum = 0; taskNum < numTasks; ++taskNum )
		{
			min[ splitd ] = full.min( splitd ) + taskNum * dsize;
			max[ splitd ] = ( taskNum == numTasks - 1 ) ? full.max( splitd ) : min[ splitd ] + dsize - 1;
			final RandomAccessibleInterval< FloatType > source = Views.interval( img, new FinalInterval( min, max ) );

			final Runnable r = new Runnable()
			{
				@Override
				public void run()
				{
					final Cursor< FloatType > center = Views.iterable( source ).cursor();
					for ( final Neighborhood< FloatType > neighborhood : shape.neighborhoods( source ) )
					{
						final float centerValue = center.next().get();
						if ( Math.abs( centerValue ) >= threshold )
						{
							final SpecialPoint specialPoint = isSpecialPointFloat( neighborhood, centerValue );
							if ( specialPoint != SpecialPoint.INVALID )
							{
								final DifferenceOfGaussianPeak peak = new DifferenceOfGaussianPeak( center, centerValue, specialPoint );
								dogPeaks.add( peak );
							}
						}
					}
				}
			};
			ex.execute( r );
		}
		ex.shutdown();
		try
		{
			ex.awaitTermination( 1000, TimeUnit.DAYS );
		}
		catch ( final InterruptedException e )
		{
			e.printStackTrace();
		}

		return dogPeaks;
	}


	@Override
	public String toString()
	{
		return "Peak @" + super.toString() + ", val = " + value + ", type = " + peakType;
	}

}
