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

import net.imglib2.Cursor;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.localextrema.LocalExtrema;
import net.imglib2.algorithm.localextrema.LocalExtrema.MaximumCheck;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;
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
	 * @return {@code float} - the distance between the two points.
	 */
	final private static float computeLength( final long[] position )
	{
		float dist = 0;

		for ( int d = 0; d < position.length; ++d )
		{
			final long pos = position[ d ];

			dist += pos * pos;
		}

		return ( float ) Math.sqrt( dist );
	}

	/**
	 * Calculates a default number of rho bins, which corresponds to a
	 * resolution of one pixel.
	 *
	 * @param inputImage
	 *            the {@link RandomAccessibleInterval} in question.
	 * @return default number of rho bins.
	 */
	private static int defaultRho( final RandomAccessibleInterval< ? > inputImage )
	{
		final long[] dims = new long[ inputImage.numDimensions() ];
		inputImage.dimensions( dims );
		return ( int ) ( 2 * computeLength( dims ) );
	}

	/**
	 * Returns the size of the vote space output image given an input
	 * {@link RandomAccessibleInterval}.
	 * 
	 * @param input
	 *            - the {@link RandomAccessibleInterval} over which the Hough
	 *            Line Transform will be run
	 * @return {@code long[]} - the dimensions of the vote space image
	 */
	public static < T extends RealType< T > > long[] getVotespaceSize( RandomAccessibleInterval< T > input )
	{
		return new long[] { defaultRho( input ), DEFAULT_THETA };
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
	public static long[] getVotespaceSize( int nRho, int nTheta )
	{
		return new long[] { nRho, nTheta };
	}

	/**
	 * Pick vote space peaks with a {@link LocalExtrema}.
	 *
	 * @return {@code List<Point>} - a list of all of the local maxima of the
	 *         {@code voteSpace}.
	 */
	public static < T extends RealType< T > & NativeType< T > > List< Point > pickPeaks( RandomAccessibleInterval< T > voteSpace, T minPeak )
	{
		final MaximumCheck< T > maxCheck = new MaximumCheck<>( minPeak );

		return LocalExtrema.findLocalExtrema( voteSpace, maxCheck );
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
	public static < T extends RealType< T > > void voteLines( final RandomAccessibleInterval< T > input, final RandomAccessibleInterval< T > votespace )
	{
		voteLines( input, votespace, defaultRho( input ), DEFAULT_THETA );
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
	 * @param nRho
	 *            - the number of bins for rho resolution
	 * @param nTheta
	 *            - the number of bins for theta resolution
	 */
	public static < T extends RealType< T > > void voteLines( final RandomAccessibleInterval< T > input, final RandomAccessibleInterval< T > votespace, final int nRho, final int nTheta )
	{
		voteLines( input, votespace, nRho, nTheta, Util.getTypeFromInterval( input ).createVariable() );
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
	 * @param nRho
	 *            - the number of bins for rho resolution
	 * @param nTheta
	 *            - the number of bins for theta resolution
	 * @param threshold
	 *            - the minimum value allowed by the populator. Any input less
	 *            than or equal to this value will be disregarded by the
	 *            populator.
	 */
	public static < T extends RealType< T > > void voteLines( final RandomAccessibleInterval< T > input, final RandomAccessibleInterval< T > votespace, final int nRho, final int nTheta, final T threshold )
	{

		final long[] dims = new long[ input.numDimensions() ];
		input.dimensions( dims );

		final Cursor< T > imageCursor = Views.iterable( input ).localizingCursor();
		final RandomAccess< T > outputRA = votespace.randomAccess();

		final double minRho = -computeLength( dims );
		final double dRho = 2 * computeLength( dims ) / ( double ) nRho;

		final double minTheta = -Math.PI / 2;
		final double dTheta = Math.PI / nTheta;

		final double[] rho = new double[ nRho ];
		final double[] theta = new double[ nTheta ];

		for ( int t = 0; t < nTheta; ++t )
		{
			theta[ t ] = dTheta * t + minTheta;
		}

		for ( int r = 0; r < nRho; ++r )
		{
			rho[ r ] = dRho * r + minRho;
		}

		final long[] position = new long[ input.numDimensions() ];

		final T one = Util.getTypeFromInterval( input ).createVariable();
		one.setOne();

		while ( imageCursor.hasNext() )
		{
			double fRho;
			int r;
			final int[] voteLoc = new int[ 2 ];

			imageCursor.fwd();
			imageCursor.localize( position );

			for ( int t = 0; t < nTheta; ++t )
			{
				if ( imageCursor.get().compareTo( threshold ) > 0 )
				{
					fRho = Math.cos( theta[ t ] ) * position[ 0 ] + Math.sin( theta[ t ] ) * position[ 1 ];
					r = Math.round( ( float ) ( ( fRho - minRho ) / dRho ) );
					voteLoc[ 0 ] = r;
					voteLoc[ 1 ] = t;
					// place vote
					outputRA.setPosition( voteLoc );
					outputRA.get().add( one );
				}
			}
		}
	}
}
