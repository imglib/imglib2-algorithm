/*
 * #%L
 * ImgLib: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2015 Stephan Preibisch, Tobias Pietzsch, Barry DeZonia,
 * Stephan Saalfeld, Albert Cardona, Curtis Rueden, Christian Dietz, Jean-Yves
 * Tinevez, Johannes Schindelin, Lee Kamentsky, Larry Lindsey, Grant Harris,
 * Mark Hiner, Aivar Grislis, Martin Horn, Nick Perry, Michael Zinsmaier,
 * Steffen Jaensch, Jan Funke, Mark Longair, and Dimiter Prodanov.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

package net.imglib2.algorithm.scalespace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.imglib2.Cursor;
import net.imglib2.Localizable;
import net.imglib2.RandomAccess;
import net.imglib2.Sampler;
import net.imglib2.algorithm.Benchmark;
import net.imglib2.algorithm.MultiThreaded;
import net.imglib2.algorithm.OutputAlgorithm;
import net.imglib2.algorithm.function.Function;
import net.imglib2.algorithm.function.SubtractNormReal;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.algorithm.localextrema.LocalExtrema;
import net.imglib2.algorithm.localextrema.LocalExtrema.LocalNeighborhoodCheck;
import net.imglib2.algorithm.localextrema.RefinedPeak;
import net.imglib2.algorithm.localextrema.SubpixelLocalization;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.scalespace.Blob.SpecialPoint;
import net.imglib2.converter.Converter;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;
import Jama.EigenvalueDecomposition;
import Jama.Matrix;

/**
 * Scale-space detection algorithm, based on difference-of-gaussians.
 * <p>
 * This algorithm first computes the scale-space of the provided source image,
 * using difference-of-gaussians. Blobs corresponding to scales are then
 * detected and returned. The detection processes as follow:
 * 
 * <ol start="1">
 * <li>Computation of the scale-space on floats. Can be retrieved with the
 * {@link #getResult()} method.
 * <li>Detection of extrema (maxima and minima) in the scale-space.
 * <li>Non-maximal suppression. This permits the suppression of sprurious
 * detections at small scale when a better detection at a larger scale exists.
 * <li>Elimination of edges responses. Edges give extrema in the scale-space
 * image as well. This step discards them based on the expected difference in
 * the principal curvature at the blob detection (large difference for edges).
 * <li>Sub-pixel localization, using a quadratic interpolation.
 * </ol>
 * 
 * 
 * @author Stephan Preibisch
 * @author Stephan Saalfeld
 * @author Jean-Yves Tinevez
 */
public class DogScaleSpace< A extends Type< A >> implements OutputAlgorithm< Img< FloatType >>, MultiThreaded, Benchmark
{

	private final Img< A > image;

	private final Converter< A, FloatType > converter;

	private Collection< Blob > darkBlobs;

	private Collection< Blob > brightBlobs;

	private Img< FloatType > scaleSpace;

	private double initialSigma;

	private double scale;

	private double imageSigma;

	private final double threshold;

	private int minImageSize;

	private final int stepsPerOctave;

	private long processingTime;

	private int numThreads;

	private String errorMessage = "";

	private final double suppressingRadiusFactor;

	/**
	 * The threshold used to discard edge responses in blob detection. Adapted
	 * from the SIFT framework.
	 * <p>
	 * This value is the maximal ratio between the largest and smallest
	 * curvature at the blob location. If the two curvatures are too different,
	 * and this ratio is <b>big</b>, then it is likely that we detected an edge.
	 * <p>
	 * In the SIFT paper, the value 10. is used.
	 * 
	 * @see <a
	 *      href=https://en.wikipedia.org/wiki/Scale-invariant_feature_transform
	 *      #Eliminating_edge_responses>Eliminating edge responses</a>
	 * 
	 */
	private final double edgeResponseThreshold;

	/**
	 * Creates a new scale-space algorithm.
	 * <p>
	 * if the specified <code>initialSigma</code> is smaller than one, the image
	 * is upsampled prior to scale-space calculation.
	 * <p>
	 * The <code>threshold</code> on intensity is taken on the normalized
	 * scale-space image, which contain absolute value ranging from 0 to 1.
	 * Typical values are around 0.03.
	 * <p>
	 * The <code>suppressingRadiusFactor</code> determines a search radius for
	 * non-maximal suppression, in units of the larger detection. A value of 2
	 * is a good starting point.
	 * <p>
	 * The <code>edgeResponseThreshold</code> is the maximal ratio between the
	 * largest and smallest curvature at the blob location tolerated. If the two
	 * curvatures are too different, and this ratio is <b>big</b>, then it is
	 * likely that we detected an edge. In the SIFT paper, the value 10 is used.
	 * 
	 * @see <a
	 *      href=https://en.wikipedia.org/wiki/Scale-invariant_feature_transform
	 *      #Eliminating_edge_responses>Eliminating edge responses</a>
	 * 
	 * @param image
	 *            the source image to operate on.
	 * @param converter
	 *            a converter than can convert the type of the source image to
	 *            floats.
	 * @param initialSigma
	 *            the smallest gaussian sigma to build the scale space from.
	 * @param threshold
	 *            the intensity threshold on blobs.
	 * @param suppressingRadiusFactor
	 *            the radius factor when searching non-maximal blobs to
	 *            suppress. If negative, non-maximal blobs will not be
	 *            suppressed.
	 * @param edgeResponseThreshold
	 *            the edge response threshold. If negative, edge responses will
	 *            not be eliminated.
	 */
	public DogScaleSpace( final Img< A > image, final Converter< A, FloatType > converter, final double initialSigma, final double threshold, final double suppressingRadiusFactor, final double edgeResponseThreshold )
	{
		setNumThreads();
		this.image = image;
		this.converter = converter;
		this.initialSigma = initialSigma;
		this.scale = 1.0;
		this.imageSigma = 0.5;
		this.minImageSize = 16;
		this.stepsPerOctave = 7;
		this.threshold = threshold;
		this.suppressingRadiusFactor = suppressingRadiusFactor;
		this.edgeResponseThreshold = edgeResponseThreshold;
	}

	@Override
	public Img< FloatType > getResult()
	{
		return scaleSpace;
	}

	public Collection< Blob > getBrightBlobs()
	{
		return brightBlobs;
	}

	public Collection< Blob > getDarkBlobs()
	{
		return darkBlobs;
	}

	public void setMinImageSize( final int minImageSize )
	{
		this.minImageSize = minImageSize;
	}

	public int getMinImageSize()
	{
		return minImageSize;
	}

	@Override
	public boolean process()
	{
		final long startTime = System.currentTimeMillis();

		/*
		 * Compute the input image by upsampling or converting.
		 */
		final Img< FloatType > input;
		ImgFactory< FloatType > floatFactory = null;
		try
		{
			floatFactory = image.factory().imgFactory( new FloatType() );
		}
		catch ( final IncompatibleTypeException e )
		{
			e.printStackTrace();
		}

		if ( initialSigma < 1.0 )
		{
			input = upSample( image, converter );

			imageSigma *= 2.0;
			initialSigma *= 2.0;
			scale = 2.0;
		}
		else
		{
			input = convert( image, floatFactory, converter );
		}

		if ( input == null )
		{
			errorMessage = "Error creating input image: " + errorMessage;
			return false;
		}

		/*
		 * Normalize the image to [0 ... 1].
		 */
		if ( !normImageMinMax( input ) ) { return false; }

		/*
		 * Compute the necessary sigmas and normalization.
		 */
		final double[] sigma = getSigmas( input, initialSigma, minImageSize, stepsPerOctave );
		final double[] sigmaInc = getIncrementalSigmas( sigma, imageSigma );
		final double norm = getNormalizationFactor( stepsPerOctave );

		/*
		 * Build scale space.
		 */
		try
		{
			scaleSpace = computeScaleSpace( input, sigmaInc, norm );
		}
		catch ( final IncompatibleTypeException e )
		{
			errorMessage = "Cannot compute scale space: " + e.getMessage();
			e.printStackTrace();
		}
		if ( scaleSpace == null )
		{
			errorMessage = "Cannot compute scale space: " + errorMessage;
			return false;
		}

		/*
		 * Find extrema.
		 */

		final ExecutorService service = Executors.newFixedThreadPool( numThreads );
		final ArrayList< DifferenceOfGaussianPeak > integerPeaks = LocalExtrema.findLocalExtrema( scaleSpace, new ScaleSpaceExtremaCheck( threshold ), service );

		/*
		 * Suppress non-maximal blobs.
		 */

		final AdaptiveNonMaximalSuppression nonMaximaSuppressor = new AdaptiveNonMaximalSuppression( integerPeaks, suppressingRadiusFactor );
		// Only execute if suppressingRadiusFactor > 0
		if ( suppressingRadiusFactor > 0 )
		{
			if ( !nonMaximaSuppressor.checkInput() || !nonMaximaSuppressor.process() )
			{
				errorMessage = "Cannot suppress peaks: " + nonMaximaSuppressor.getErrorMessage();
				return false;
			}
		}
		final List< DifferenceOfGaussianPeak > validPeaks = nonMaximaSuppressor.getClearedList();

		/*
		 * Eliminate edge responses.
		 */

		if ( edgeResponseThreshold > 0 )
		{
			eliminateEdgeResponses( validPeaks, scaleSpace, edgeResponseThreshold );
		}

		/*
		 * Subpixel localize them.
		 */

		final SubpixelLocalization< DifferenceOfGaussianPeak, FloatType > spl = new SubpixelLocalization< DifferenceOfGaussianPeak, FloatType >( scaleSpace.numDimensions() );
		spl.setNumThreads( numThreads );
		final ArrayList< RefinedPeak< DifferenceOfGaussianPeak >> refinedPeaks = spl.process( validPeaks, scaleSpace, scaleSpace );

		/*
		 * Adjust the correct sigma and correct the locations if the image was
		 * originally upscaled. Also create the collection of blobs.
		 */

		brightBlobs = new ArrayList< Blob >( refinedPeaks.size() );
		darkBlobs = new ArrayList< Blob >( refinedPeaks.size() );
		for ( final RefinedPeak< DifferenceOfGaussianPeak > peak : refinedPeaks )
		{
			if ( !peak.isValid() )
			{
				continue;
			}
			/*
			 * +0.5 to get it relative to the sigmas and not the difference of
			 * the sigmas e.g. dog 1 corresponds to between sigmas 1 and 2
			 */
			double optimalSigma = peak.getDoublePosition( scaleSpace.numDimensions() - 1 ) + 0.5d;
			optimalSigma = initialSigma * Math.pow( 2.0d, optimalSigma / stepsPerOctave );
			peak.setPosition( optimalSigma, scaleSpace.numDimensions() - 1 );

			if ( scale != 1.0 )
			{
				for ( int d = 0; d < scaleSpace.numDimensions(); ++d )
				{
					final double sizeHalf = peak.getDoublePosition( d ) / 2.0;
					peak.setPosition( sizeHalf, d );
				}
			}

			final double radius = peak.getDoublePosition( image.numDimensions() ) * Math.sqrt( image.numDimensions() );
			final Blob blob = new Blob( peak, radius );
			if ( blob.isMax() )
			{
				brightBlobs.add( blob );
			}
			else
			{
				darkBlobs.add( blob );
			}
		}
		processingTime = System.currentTimeMillis() - startTime;
		return true;
	}

	protected void eliminateEdgeResponses( final List< DifferenceOfGaussianPeak > detections, final Img< FloatType > scaleSpace, final double rth )
	{
		// Store detections to remove.
		final Collection< DifferenceOfGaussianPeak > toRemove = new ArrayList< DifferenceOfGaussianPeak >();

		// Store the Hessian.
		final int n = scaleSpace.numDimensions();
		final Matrix H = new Matrix( n - 1, n - 1 );

		// Access scale space value.
		final RandomAccess< FloatType > access = scaleSpace.randomAccess( scaleSpace );

		for ( final DifferenceOfGaussianPeak detection : detections )
		{
			access.setPosition( detection );

			// We compute the Hessian at a fixed scale.
			final double a1 = access.get().getRealDouble();
			for ( int d = 0; d < n - 1; ++d )
			{
				access.bck( d );
				final double a0 = access.get().getRealDouble();
				access.move( 2, d );
				final double a2 = access.get().getRealDouble();
				H.set( d, d, a2 - 2 * a1 + a0 );

				// Move back to center point
				access.bck( d );

				for ( int e = d + 1; e < n - 1; ++e )
				{
					// We start from center point.
					access.fwd( d );
					access.fwd( e );
					final double a2b2 = access.get().getRealDouble();
					access.move( -2, d );
					final double a0b2 = access.get().getRealDouble();
					access.move( -2, e );
					final double a0b0 = access.get().getRealDouble();
					access.move( 2, d );
					final double a2b0 = access.get().getRealDouble();
					// back to the original position
					access.bck( d );
					access.fwd( e );
					final double v = ( a2b2 - a0b2 - a2b0 + a0b0 ) * 0.25;
					H.set( d, e, v );
					H.set( e, d, v );
				}
			}

			final double r;
			if ( n == 3 )
			{
				// 2D case, shortcut for the computation of eigenvalues.
				final double detHessian = H.det();
				final double traceHessian = H.trace();
				r = Math.abs( traceHessian * traceHessian / detHessian );

				/*
				 * See
				 * https://en.wikipedia.org/wiki/Scale-invariant_feature_transform
				 * #Eliminating_edge_responses.
				 */
			}
			else
			{
				final EigenvalueDecomposition decomposition = H.eig();
				final double[] eig = decomposition.getRealEigenvalues();
				r = Math.abs( Util.max( eig ) / Util.min( eig ) );
			}

			if ( r > ( ( rth + 1 ) * ( rth + 1 ) / rth ) )
			{
				toRemove.add( detection );
			}
		}

		detections.removeAll( toRemove );
	}

	protected Img< FloatType > computeScaleSpace( final Img< FloatType > image, final double[] sigma, final double norm ) throws IncompatibleTypeException
	{
		// Compute the dimensions for the scale space.
		final long[] dimensions = new long[ image.numDimensions() + 1 ];
		image.dimensions( dimensions );
		dimensions[ image.numDimensions() ] = sigma.length - 1;

		// create scale space
		ImgFactory< FloatType > floatImgFactory;
		try
		{
			floatImgFactory = image.factory().imgFactory( new FloatType() );
		}
		catch ( final IncompatibleTypeException e )
		{
			errorMessage = "Cannot create float Img factory: " + e.getMessage();
			e.printStackTrace();
			return null;
		}
		final Img< FloatType > scaleSpace = floatImgFactory.create( dimensions, new FloatType() );
		Img< FloatType > gauss1 = floatImgFactory.create( image, new FloatType() );

		/*
		 * Compute initial gaussian convolution.
		 */
		final int n = gauss1.numDimensions();
		final double[] sigmaArray = new double[ n ];
		Arrays.fill( sigmaArray, sigma[ 0 ] );
		Gauss3.gauss( sigmaArray, Views.extendMirrorSingle( image ), gauss1, numThreads );

		/*
		 * Compute all scales.
		 */

		for ( int s = 1; s < sigma.length; ++s )
		{
			// Compute gaussian convolution.
			final Img< FloatType > gauss2 = floatImgFactory.create( image, new FloatType() );
			Arrays.fill( sigmaArray, sigma[ s ] );
			Gauss3.gauss( sigmaArray, Views.extendMirrorSingle( gauss1 ), gauss2, numThreads );

			// Compute difference of gaussian, overwrite gauss1.
			final Function< FloatType, FloatType, FloatType > function = new SubtractNormReal< FloatType, FloatType, FloatType >( norm );
			final ImageCalculator< FloatType, FloatType, FloatType > imageCalc = new ImageCalculator< FloatType, FloatType, FloatType >( gauss2, gauss1, gauss1, function );
			imageCalc.setNumThreads( getNumThreads() );

			if ( !imageCalc.checkInput() || !imageCalc.process() )
			{
				errorMessage = "Cannot subtract images: " + imageCalc.getErrorMessage();
				return null;
			}

			// copy DoG image into the scalespace
			final Cursor< FloatType > cursorIn = gauss1.localizingCursor();
			final RandomAccess< FloatType > cursorOut = scaleSpace.randomAccess();

			final long[] position = new long[ cursorOut.numDimensions() ];
			cursorOut.localize( position );
			position[ scaleSpace.numDimensions() - 1 ] = s - 1;

			while ( cursorIn.hasNext() )
			{
				cursorIn.fwd();

				// This will only overwrite the lower dimensions.
				cursorIn.localize( position );
				cursorOut.setPosition( position );
				cursorOut.get().set( cursorIn.get() );
			}

			// Update the lower sigma image.
			gauss1 = gauss2;
		}
		return scaleSpace;
	}

	protected double getNormalizationFactor( final int stepsPerOctave )
	{
		final double K = Math.pow( 2.0, 1.0 / stepsPerOctave );
		final double K_MIN1_INV = 1.0f / ( K - 1.0f );

		return K_MIN1_INV;
	}

	protected double[] getIncrementalSigmas( final double[] sigma, final double imageSigma )
	{
		final double[] sigmaInc = new double[ sigma.length ];

		// first convolution is to the inital sigma
		sigmaInc[ 0 ] = Math.sqrt( sigma[ 0 ] * sigma[ 0 ] - imageSigma * imageSigma );

		// the others are always to the previous convolution
		for ( int i = 1; i < sigma.length; ++i )
		{
			sigmaInc[ i ] = Math.sqrt( sigma[ i ] * sigma[ i ] - sigma[ i - 1 ] * sigma[ i - 1 ] );
		}
		return sigmaInc;
	}

	protected double[] getSigmas( final Img< ? > img, final double initialSigma, final int minImageSize, final int stepsPerOctave )
	{
		long minDim = img.dimension( 0 );

		for ( int d = 1; d < img.numDimensions(); ++d )
		{
			minDim = Math.min( minDim, img.dimension( d ) );
		}

		final int numOctaves = ( int ) Math.round( Util.log2( minDim ) - Util.log2( minImageSize ) + 0.25 );

		final double[] sigma = new double[ numOctaves * stepsPerOctave + 3 ];

		for ( int i = 0; i < sigma.length; ++i )
		{
			sigma[ i ] = initialSigma * Math.pow( 2.0f, ( double ) i / ( double ) stepsPerOctave );
		}

		return sigma;
	}

	protected boolean normImageMinMax( final Img< FloatType > image )
	{

		final NormalizeImageMinMax< FloatType > norm = new NormalizeImageMinMax< FloatType >( image );
		norm.setNumThreads( getNumThreads() );

		if ( !norm.checkInput() || !norm.process() )
		{
			errorMessage = "Cannot normalize image: " + norm.getErrorMessage();
			return false;
		}

		return true;
	}

	protected Img< FloatType > convert( final Img< A > input, final ImgFactory< FloatType > processFactory, final Converter< A, FloatType > converter )
	{
		final Img< FloatType > output = processFactory.create( input, new FloatType() );
		final ImageConverter< A, FloatType > imgConv = new ImageConverter< A, FloatType >( image, output, converter );
		imgConv.setNumThreads( getNumThreads() );

		if ( !imgConv.checkInput() || !imgConv.process() )
		{
			errorMessage = "Cannot convert image: " + imgConv.getErrorMessage();
			return null;
		}
		else
		{
			return output;
		}
	}

	/**
	 * Up-samples the image by a factor of 2.
	 *
	 * @param input
	 *            The input image
	 * @param converter
	 *            - How to convert between A and B
	 *
	 * @return the up-sampled image, linearly interpolated
	 */
	protected Img< FloatType > upSample( final Img< A > input, final Converter< A, FloatType > converter )
	{
		final int numDimensions = input.numDimensions();
		final long dim[] = new long[ numDimensions ];
		input.dimensions( dim );

		// we do a centered upsampling
		for ( int d = 0; d < numDimensions; ++d )
		{
			dim[ d ] = dim[ d ] * 2 - 1;
		}

		// create output image
		ImgFactory< FloatType > processFactory = null;
		try
		{
			processFactory = input.factory().imgFactory( new FloatType() );
		}
		catch ( final IncompatibleTypeException e )
		{
			e.printStackTrace();
		}
		final Img< FloatType > upSampled = processFactory.create( dim, new FloatType() );

		// create cursors and temp arrays
		final Cursor< A > inCursor = input.localizingCursor();
		final RandomAccess< FloatType > outCursor = upSampled.randomAccess();
		final long[] tmp = new long[ numDimensions ];

		while ( inCursor.hasNext() )
		{
			inCursor.fwd();
			inCursor.localize( tmp );

			for ( int d = 0; d < numDimensions; ++d )
				tmp[ d ] *= 2;

			outCursor.setPosition( tmp );
			converter.convert( inCursor.get(), outCursor.get() );
		}

		// now interpolate the missing pixels, dimension by dimension
		final Cursor< FloatType > outCursor2 = upSampled.localizingCursor();

		for ( int d = 0; d < numDimensions; ++d )
		{
			outCursor2.reset();

			while ( outCursor2.hasNext() )
			{
				outCursor2.fwd();

				final long pos = outCursor2.getLongPosition( d );

				// is it an empty spot?
				if ( pos % 2 == 1 )
				{
					outCursor.setPosition( outCursor2 );
					outCursor.bck( d );

					final double left = outCursor.get().getRealDouble();

					outCursor.fwd( d );
					outCursor.fwd( d );

					final double right = outCursor.get().getRealDouble();

					outCursor.bck( d );
					outCursor.get().setReal( ( right + left ) / 2.0 );
				}
			}
		}

		return upSampled;
	}

	@Override
	public boolean checkInput()
	{
		if ( errorMessage.length() > 0 )
		{
			return false;
		}
		else if ( image == null )
		{
			errorMessage = "ScaleSpace: [Img<A> img] is null.";
			return false;
		}
		return true;
	}

	@Override
	public String getErrorMessage()
	{
		return errorMessage;
	}

	@Override
	public long getProcessingTime()
	{
		return processingTime;
	}

	@Override
	public void setNumThreads()
	{
		this.numThreads = Runtime.getRuntime().availableProcessors();
	}

	@Override
	public void setNumThreads( final int numThreads )
	{
		this.numThreads = numThreads;
	}

	@Override
	public int getNumThreads()
	{
		return numThreads;
	}

	/*
	 * INNER CLASSES
	 */

	private static class ScaleSpaceExtremaCheck implements LocalNeighborhoodCheck< DifferenceOfGaussianPeak, FloatType >
	{
		private final double threshold;

		public ScaleSpaceExtremaCheck( final double threshold )
		{
			this.threshold = threshold;
		}

		@Override
		public < C extends Localizable & Sampler< FloatType >> DifferenceOfGaussianPeak check( final C center, final Neighborhood< FloatType > neighborhood )
		{
			final float centerValue = center.get().get();
			if ( Math.abs( centerValue ) < threshold ) { return null; }

			final Cursor< FloatType > c = neighborhood.cursor();
			while ( c.hasNext() )
			{
				final float v = c.next().get();
				if ( centerValue < v )
				{
					// it can only be a minimum
					while ( c.hasNext() )
						if ( centerValue > c.next().get() )
							return null;
					// this mixup is intended, a minimum in the 2nd derivation
					// is a maxima in image space and vice versa
					return new DifferenceOfGaussianPeak( center, centerValue, SpecialPoint.MAX );

				}
				else if ( centerValue > v )
				{
					// it can only be a maximum
					while ( c.hasNext() )
						if ( centerValue < c.next().get() )
							return null;
					// this mixup is intended, a minimum in the 2nd derivation
					// is a maxima in image space and vice versa
					return new DifferenceOfGaussianPeak( center, centerValue, SpecialPoint.MIN );
				}
			}
			return new DifferenceOfGaussianPeak( center, centerValue, SpecialPoint.MIN );
			// all neighboring pixels have the same value. count it as MIN.
		}

	}

}
