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

package net.imglib2.algorithm.hough;

import java.util.List;
import java.util.function.Predicate;

import net.imglib2.Cursor;
import net.imglib2.Dimensions;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.localextrema.LocalExtrema;
import net.imglib2.algorithm.localextrema.LocalExtrema.MaximumCheck;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

/**
 * This abstract class provides some basic functionality for use with arbitrary
 * Hough-like transforms.
 *
 * @param <T>
 *            the data type of the input image.
 * @author Larry Lindsey
 * @author Yili Zhao
 * @author Gabe Selzer
 */
public class HoughTransforms< T extends RealType< T > & Comparable< T > >
{

	public static final int DEFAULT_THETA = 180;

	/**
	 * Calculates the geometric distance between [0, ... , 0] and position.
	 *
	 * @param position
	 *            - the position of the second point
	 * @return {@code double} - the distance between the two points.
	 */
	final private static double computeLength( final long[] position )
	{
		double dist = 0;

		for ( int d = 0; d < position.length; ++d )
		{
			final long pos = position[ d ];

			dist += pos * pos;
		}

		return Math.sqrt( dist );
	}

	/**
	 * Calculates a default number of rho bins, which corresponds to a
	 * resolution of one pixel.
	 *
	 * @param dimensions
	 *            the {@link Dimensions} of the input image.
	 * @return default number of rho bins.
	 */
	private static int defaultRho( final Dimensions dimensions )
	{
		return ( int ) ( 2 * computeLength( Intervals.dimensionsAsLongArray( dimensions ) ) );
	}

	/**
	 * Returns the size of the vote space output image given an input
	 * {@link RandomAccessibleInterval}.
	 *
	 * @param dimensions
	 *            - the {@link Dimensions} over which the Hough Line Transform
	 *            will be run
	 * @return {@code long[]} - the dimensions of the vote space image
	 */
	public static long[] getVotespaceSize( final Dimensions dimensions )
	{
		return new long[] { defaultRho( dimensions ), DEFAULT_THETA };
	}

	/**
	 * Returns the size of the vote space output image given an input
	 * {@link RandomAccessibleInterval}.
	 *
	 * @param dimensions
	 *            - the {@link Dimensions} over which the Hough Line Transform
	 *            will be run
	 * @param nTheta
	 *            - the number of theta bins.
	 * @return {@code long[]} - the dimensions of the vote space image
	 */
	public static long[] getVotespaceSize( final Dimensions dimensions, final int nTheta )
	{
		return new long[] { defaultRho( dimensions ), nTheta };
	}

	/**
	 * Returns the size of the voteSpace output image given desired {@code nRho}
	 * and {@code nTheta} values.
	 *
	 * @param nRho
	 *            - the number of bins for rho resolution
	 * @param nTheta
	 *            - the number of bins for theta resolution
	 * @return {@code long[]} - the dimensions of the vote space image.
	 */
	public static long[] getVotespaceSize( final int nRho, final int nTheta )
	{
		return new long[] { nRho, nTheta };
	}

	/**
	 * Pick vote space peaks with a {@link LocalExtrema}.
	 *
	 * @param voteSpace
	 *            - the {@link RandomAccessibleInterval} containing the output
	 *            of a Hough Transform vote
	 * @param threshold
	 *            - the {@link IntegerType} at and below which maxima will be
	 *            ignored
	 * @return {@code List<Point>} - a list of all of the local maxima of the
	 *         {@code voteSpace}
	 */
	public static < T extends IntegerType< T > > List< Point > pickLinePeaks(
			final RandomAccessibleInterval< T > voteSpace,
			final long threshold )
	{
		final T minPeak = Util.getTypeFromInterval( voteSpace ).createVariable();
		minPeak.setInteger( threshold );
		return pickLinePeaks( voteSpace, minPeak );
	}

	/**
	 * Pick vote space peaks with a {@link LocalExtrema}.
	 *
	 * @param voteSpace
	 *            - the {@link RandomAccessibleInterval} containing the output
	 *            of a Hough Transform vote
	 * @param minPeak
	 *            - the {@link Comparable} at and below which maxima will be
	 *            ignored
	 * @return {@code List<Point>} - a list of all of the local maxima of the
	 *         {@code voteSpace}.
	 */
	public static < T extends Comparable< T > > List< Point > pickLinePeaks(
			final RandomAccessibleInterval< T > voteSpace,
			final T minPeak )
	{
		final MaximumCheck< T > maxCheck = new MaximumCheck<>( minPeak );

		// since the vote space runs [0, maxRho * 2) x [0, pi) to allow
		// negative rho/pi values without having the user to reinterval their
		// vote space, we need to translate the vote space by {@code -maxRho} in
		// the first dimension and by {@code -pi / 2} in the second dimension so
		// that we return accurate coordinates from the
		// vote space.
		final long[] translation = { -( voteSpace.dimension( 0 ) / 2 ), -( voteSpace.dimension( 1 ) / 2 ) };
		final IntervalView< T > translatedVotes = Views.translate( voteSpace, translation );

		return LocalExtrema.findLocalExtrema( translatedVotes, maxCheck );
	}

	/**
	 * Runs a Hough Line Tranform on an image and populates the vote space
	 * parameter with the results.
	 *
	 * @param input
	 *            - the {@link RandomAccessibleInterval} to run the Hough Line
	 *            Transform over
	 * @param votespace
	 *            - the {@link RandomAccessibleInterval} in which the results
	 *            are stored
	 */
	public static < T extends Comparable< T >, U extends IntegerType< U > > void voteLines(
			final RandomAccessibleInterval< T > input,
			final RandomAccessibleInterval< U > votespace )
	{
		voteLines( input, votespace, DEFAULT_THETA, defaultRho( input ) );
	}

	/**
	 * Runs a Hough Line Tranform on an image and populates the vote space
	 * parameter with the results.
	 *
	 * @param input
	 *            - the {@link RandomAccessibleInterval} to run the Hough Line
	 *            Transform over
	 * @param votespace
	 *            - the {@link RandomAccessibleInterval} in which the results
	 *            are stored
	 * @param nTheta
	 *            - the number of bins for theta resolution
	 */
	public static < T extends Comparable< T >, U extends IntegerType< U > > void voteLines(
			final RandomAccessibleInterval< T > input,
			final RandomAccessibleInterval< U > votespace,
			final int nTheta )
	{
		voteLines( input, votespace, nTheta, defaultRho( input ), Util.getTypeFromInterval( input ) );
	}

	/**
	 * Runs a Hough Line Tranform on an image and populates the vote space
	 * parameter with the results.
	 *
	 * @param input
	 *            - the {@link RandomAccessibleInterval} to run the Hough Line
	 *            Transform over
	 * @param votespace
	 *            - the {@link RandomAccessibleInterval} in which the results
	 *            are stored
	 * @param nTheta
	 *            - the number of bins for theta resolution
	 * @param nRho
	 *            - the number of bins for rho resolution
	 */
	public static < T extends Comparable< T >, U extends IntegerType< U > > void voteLines(
			final RandomAccessibleInterval< T > input,
			final RandomAccessibleInterval< U > votespace,
			final int nTheta,
			final int nRho )
	{
		voteLines( input, votespace, nTheta, nRho, Util.getTypeFromInterval( input ) );
	}

	/**
	 * Runs a Hough Line Tranform on an image and populates the vote space
	 * parameter with the results.
	 *
	 * @param input
	 *            - the {@link RandomAccessibleInterval} to run the Hough Line
	 *            Transform over
	 * @param votespace
	 *            - the {@link RandomAccessibleInterval} in which the results
	 *            are stored
	 * @param nTheta
	 *            - the number of bins for theta resolution
	 * @param nRho
	 *            - the number of bins for rho resolution
	 * @param threshold
	 *            - the minimum value allowed by the populator. Any input less
	 *            than this value will be disregarded by the populator.
	 */
	public static < T extends Comparable< T >, U extends IntegerType< U > > void voteLines(
			final RandomAccessibleInterval< T > input,
			final RandomAccessibleInterval< U > votespace,
			final int nTheta,
			final int nRho,
			final T threshold )

	{
		final Predicate< T > p = o -> threshold.compareTo( o ) < 0;
		voteLines( input, votespace, nTheta, nRho, p );
	}

	/**
	 *
	 * Runs a Hough Line Tranform on an image and populates the vote space
	 * parameter with the results.
	 * <p>
	 * Vote space here has two dimensions: {@code rho} and {@code theta}.
	 * {@code theta} is measured in radians {@code [-pi/2 pi/2)}, {@code rho} is
	 * measured in {@code [-rhoMax, rhoMax)}.
	 * </p>
	 * <p>
	 * Lines are modeled as
	 * </p>
	 *
	 * <pre>
	 * l(t) = | x | = rho * |  cos(theta) | + t * | sin(theta) |
	 *        | y |         | -sin(theta) |       | cos(theta) |
	 * </pre>
	 * <p>
	 * In other words, {@code rho} represents the signed minimum distance from
	 * the image origin to the line, and {@code theta} indicates the angle
	 * between the row-axis and the minimum offset vector.
	 * </p>
	 * <p>
	 * For a given point, then, votes are placed along the curve
	 * </p>
	 *
	 * <pre>
	 * rho = y * sin( theta ) + x * cos( theta )
	 * </pre>
	 * <p>
	 * It is important to note that the interval of the first dimension of the
	 * vote space image is NOT {@code [-maxRho, maxRho)} but instead
	 * {@code [0, maxRho * 2)}; the same applies to the second dimension of the
	 * vote space as well. Thus if {@link HoughTransforms#pickPeaks} is not used
	 * to retrieve the maxima from the vote space, the vote space will have to
	 * be translated by {@code -maxRho} in dimension 0 to get the correct
	 * {@code rho} and {@code theta} values from the vote space.
	 * </p>
	 *
	 * @param input
	 *            - the {@link RandomAccessibleInterval} to run the Hough Line
	 *            Transform over
	 * @param votespace
	 *            - the {@link RandomAccessibleInterval} in which the results
	 *            are stored
	 * @param nTheta
	 *            - the number of bins for theta resolution
	 * @param nRho
	 *            - the number of bins for rho resolution
	 * @param filter
	 *            - a {@link Predicate} judging whether or not the a value is
	 *            above the minimum value allowed by the populator. Any input
	 *            less than or equal to this value will be disregarded by the
	 *            populator.
	 */
	public static < T, U extends IntegerType< U > > void voteLines(
			final RandomAccessibleInterval< T > input,
			final RandomAccessibleInterval< U > votespace,
			final int nTheta,
			final int nRho,
			final Predicate< T > filter )
	{

		final long[] dims = new long[ input.numDimensions() ];
		input.dimensions( dims );

		final double minRho = -computeLength( dims );
		final double dRho = 2 * computeLength( dims ) / nRho;

		final double minTheta = -Math.PI / 2;
		final double dTheta = Math.PI / nTheta;

		final double[] cTheta = new double[ nTheta ];
		final double[] sTheta = new double[ nTheta ];

		// create cos(theta) LUT
		for ( int t = 0; t < nTheta; ++t )
		{
			cTheta[ t ] = Math.cos( dTheta * t + minTheta );
		}

		// create sin`(theta) LUT
		for ( int t = 0; t < nTheta; ++t )
		{
			sTheta[ t ] = Math.sin( dTheta * t + minTheta );
		}

		final Cursor< T > imageCursor = Views.iterable( Views.zeroMin( input ) ).localizingCursor();
		final RandomAccess< U > outputRA = votespace.randomAccess();

		while ( imageCursor.hasNext() )
		{
			double fRho;
			int r;

			imageCursor.fwd();

			if ( filter.test( imageCursor.get() ) )
			{
				for ( int t = 0; t < nTheta; ++t )
				{
					fRho = cTheta[ t ] * imageCursor.getDoublePosition( 0 ) + sTheta[ t ] * imageCursor.getDoublePosition( 1 );
					r = Math.round( ( float ) ( ( fRho - minRho ) / dRho ) );

					// place vote
					outputRA.setPosition( r, 0 );
					outputRA.setPosition( t, 1 );
					outputRA.get().inc();
				}
			}
		}
	}

	/**
	 * Method used to convert the {rho, theta} output of the
	 * {@link HoughTransforms#voteLines} algorithm into a more useful
	 * y-intercept value. Used with {@link HoughTransforms#getSlope} to create
	 * line equations.
	 *
	 * @param rho
	 *            - the {@code rho} of the line
	 * @param theta
	 *            - the {@code theta} of the line
	 * @return {@code double} - the y-intercept of the line
	 */
	public static double getIntercept( final long rho, final long theta )
	{
		final double radians = Math.PI * theta / 180;
		return rho / Math.sin( radians );
	}

	/**
	 * Method used to convert the {rho, theta} output of the
	 * {@link HoughTransforms#voteLines} algorithm into a more useful slope
	 * value. Used with {@link HoughTransforms#getIntercept} to create line
	 * equations.
	 *
	 * @param theta
	 *            - the {@code theta} of the line
	 * @return {@code double} - the y-intercept of the line
	 */
	public static double getSlope( final long theta )
	{
		final double radians = Math.PI * theta / 180;

		final double n = -Math.cos( radians );
		final double d = Math.sin( radians );

		// to avoid a divide by zero error return an infinite slope if the
		// denominator is zero.
		if ( Math.abs( n ) == 1 )
			return n > 0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
		return n / d;
	}
}
