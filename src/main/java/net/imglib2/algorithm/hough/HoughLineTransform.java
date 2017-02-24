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

import java.util.ArrayList;

import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.type.numeric.integer.ShortType;

/**
 * A class that extends {@link HoughTransform} to handle Hough Line voting over
 * an edge map. This implementation uses a threshold to determine whether a
 * pixel at a certain point is an edge or not. Comparison is
 * strictly-greater-than. This implementation is fairly dumb in that it does not
 * take gradients into account. The threshold used is the default value returned
 * by calling the constructor for the {@link Type} of the input {@link Img}.
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
 * In other words, {@code rho} represents the signed minimum distance from the
 * image origin to the line, and {@code theta} indicates the angle between the
 * row-axis and the minimum offset vector.
 * </p>
 * <p>
 * For a given point, then, votes are placed along the curve
 * </p>
 * 
 * <pre>
 * rho = y * sin( theta ) + x * cos( theta )
 * </pre>
 */
public class HoughLineTransform< S extends RealType< S > & NativeType< S >, T extends Type< T > & Comparable< T > > extends HoughTransform< S, T >
{

	public static final int DEFAULT_THETA = 180;

	public final double dTheta;

	public final double dRho;

	private final T threshold;

	private final int nRho;

	private final int nTheta;

	private final double[] rho;

	private final double[] theta;

	private ArrayList< double[] > rtPeaks;

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
	 *            the {@link Img} in question.
	 * @return default number of rho bins.
	 */
	public static int defaultRho( final Img< ? > inputImage )
	{
		final long[] dims = new long[ inputImage.numDimensions() ];
		inputImage.dimensions( dims );
		return ( int ) ( 2 * computeLength( dims ) );
	}

	/**
	 * Creates a default {@link HoughLineTransform} with {@ShortType} vote
	 * space.
	 *
	 * @param <T>
	 *            the {@link Type} of the {@link Img} in question.
	 * @param inputImage
	 *            the {@link Img} to perform the Hough Line Transform against.
	 * @return a default {@link HoughLineTransform} with {@link IntType} vote
	 *         space.
	 */
	public static < T extends Type< T > & Comparable< T > > HoughLineTransform< ShortType, T > shortHoughLine( final Img< T > inputImage)
	{
		return new HoughLineTransform<>( inputImage, new ShortType());
	}

	/**
	 * Creates a default {@link HoughLineTransform} with {@IntType} vote space.
	 *
	 * @param <T>
	 *            the {@link Type} of the {@link Img} in question.
	 * @param inputImage
	 *            the {@link Img} to perform the Hough Line Transform against.
	 * @return a default {@link HoughLineTransform} with {@link IntType} vote
	 *         space.
	 */
	public static < T extends Type< T > & Comparable< T > > HoughLineTransform< IntType, T > integerHoughLine( final Img< T > inputImage)
	{
		return new HoughLineTransform<>( inputImage, new IntType());
	}

	/**
	 * Creates a default {@link HoughLineTransform} with {@link LongType} vote
	 * space.
	 *
	 * @param <T>
	 *            the {@link Type} of the {@link Img} in question.
	 * @param inputImage
	 *            the {@link Img} to perform the Hough Line Transform against.
	 * @return a default {@link HoughLineTransform} with {@link LongType} vote
	 *         space.
	 *         <p>
	 *         Use this for voting against large images, but reasonably small
	 *         vote space. If you need a big voting space, it would be better to
	 *         create a {@link HoughLineTransform} instantiated with an
	 *         {@link ImgFactory} capable of handling it.
	 */
	public static < T extends Type< T > & Comparable< T > > HoughLineTransform< LongType, T > longHoughLine( final Img< T > inputImage)
	{
		return new HoughLineTransform<>( inputImage, new LongType());
	}

	/**
	 * Create a {@link HoughLineTransform} to operate against a given
	 * {@link Img}, with a specific {@link Type} of vote space. Defaults are
	 * used for rho- and theta-resolution.
	 *
	 * @param inputImage
	 *            the {@link Img} to operate against.
	 * @param type
	 *            the {@link Type} for the vote space.
	 */
	public HoughLineTransform( final Img< T > inputImage, final S type)
	{
		this( inputImage, DEFAULT_THETA, type);
	}

	/**
	 * Create a {@link HoughLineTransform} to operate against a given
	 * {@link Img}, with a specific {@link Type} of vote space and
	 * theta-resolution. Rho-resolution is set to the default.
	 *
	 * @param inputImage
	 *            the {@link Img} to operate against.
	 * @param theta
	 *            the number of bins for theta-resolution.
	 * @param type
	 *            the {@link Type} for the vote space.
	 */
	public HoughLineTransform( final Img< T > inputImage, final int theta, final S type)
	{
		this( inputImage, defaultRho( inputImage ), theta, type);
	}

	/**
	 * Create a {@link HoughLineTransform} to operate against a given
	 * {@link Img}, with a specific {@link Type} of vote space and rho- and
	 * theta-resolution.
	 *
	 * @param inputImage
	 *            the {@link Img} to operate against.
	 * @param inNRho
	 *            the number of bins for rho resolution.
	 * @param inNTheta
	 *            the number of bins for theta resolution.
	 * @param type
	 *            the {@link Type} for the vote space.
	 */
	public HoughLineTransform( final Img< T > inputImage, final int inNRho, final int inNTheta, final S type)
	{
		// Call the base constructor
		super( inputImage, new long[] { inNRho, inNTheta }, type);

		// Theta by definition is in [0..pi].
		dTheta = Math.PI / inNTheta;

		// The furthest a point can be from the origin is the length calculated
		// from the dimensions of the Image.
		final long[] dims = new long[ inputImage.numDimensions() ];
		inputImage.dimensions( dims );
		dRho = 2 * computeLength( dims ) / ( double ) inNRho;
		threshold = inputImage.firstElement().createVariable();
		nRho = inNRho;
		nTheta = inNTheta;
		theta = new double[ inNTheta ];
		rho = new double[ inNRho ];
		rtPeaks = null;
	}

	/**
	 * Create a {@link HoughLineTransform} to operate against a given
	 * {@link Img}, with a specific {@link ImgFactory} for the vote space, and
	 * specific rho- and theta-resolution.
	 *
	 * @param inputImage
	 *            the {@link Img} to operate against.
	 * @param factory
	 *            the {@link ImgFactory} object.
	 * @param inNRho
	 *            the number of bisn for rho resolution.
	 * @param inNTheta
	 *            the number of bins for theta resolution.
	 * @param type
	 *            the {@link Type} for the vote space.
	 */
	public HoughLineTransform( final Img< T > inputImage, final ImgFactory< S > factory, final S type, final int inNRho, final int inNTheta)
	{
		// Call the base constructor
		super( inputImage, new long[] { inNRho, inNTheta }, factory, type);

		dTheta = Math.PI / inNTheta;

		final long[] dims = new long[ inputImage.numDimensions() ];
		inputImage.dimensions( dims );
		dRho = 2 * computeLength( dims ) / ( double ) inNRho;
		threshold = inputImage.firstElement().createVariable();
		nRho = inNRho;
		nTheta = inNTheta;
		theta = new double[ inNTheta ];
		rho = new double[ inNRho ];
		rtPeaks = null;
	}

	public void setThreshold( final T inThreshold )
	{
		threshold.set( inThreshold );
	}

	@Override
	public boolean process()
	{
		final Cursor< T > imageCursor = getImage().localizingCursor();
		final long[] position = new long[ getImage().numDimensions() ];
		final double minTheta = -Math.PI / 2;

		final long[] dims = new long[ getImage().numDimensions() ];
		getImage().dimensions( dims );
		final double minRho = -computeLength( dims );

		final long sTime = System.currentTimeMillis();
		boolean success;

		for ( int t = 0; t < nTheta; ++t )
		{
			theta[ t ] = dTheta * t + minTheta;
		}

		for ( int r = 0; r < nRho; ++r )
		{
			rho[ r ] = dRho * r + minRho;
		}

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
					super.placeVote( voteLoc );
				}
			}
		}
		// pick peaks
		success = super.pickPeaks();
		super.pTime = System.currentTimeMillis() - sTime;
		return success;
	}

	public ArrayList< double[] > getTranslatedPeakList()
	{
		if ( rtPeaks == null )
		{
			final ArrayList< long[] > peaks = getPeakList();
			rtPeaks = new ArrayList<>( peaks.size() );
			for ( final long[] irt : peaks )
			{
				final double[] rt = new double[ 2 ];
				rt[ 0 ] = rho[ ( int ) irt[ 0 ] ];
				rt[ 1 ] = theta[ ( int ) irt[ 1 ] ];
				rtPeaks.add( rt );
			}
		}

		return rtPeaks;
	}

}
