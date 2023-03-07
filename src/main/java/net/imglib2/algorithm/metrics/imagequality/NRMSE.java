/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2023 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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
package net.imglib2.algorithm.metrics.imagequality;

import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.stats.ComputeMinMax;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import static net.imglib2.algorithm.metrics.imagequality.NRMSE.Normalization.EUCLIDEAN;
import static net.imglib2.algorithm.metrics.imagequality.NRMSE.Normalization.MINMAX;

/**
 * Compute the normalised root mean squared error (NRMSE) between a reference and a processed image.
 * The metrics runs on the whole image, whether 2D or 3D. In order to get individual slice NRMSE, run
 * the metrics on each slice independently.
 * <p>
 * Three normalization methods are available:
 * <ul>
 * <li>euclidean: NRMSE = sqrt(MSE(reference, processed)) * sqrt(N) / || reference || </li>
 * <li>min-max: NRMSE = sqrt(MSE(reference, processed)) / |max(reference)-min(reference)| </li>
 * <li>mean: NRMSE = sqrt(MSE(reference, processed)) / mean(reference) </li>
 * </ul>
 *
 * @author Joran Deschamps
 */
public class NRMSE
{
	/**
	 * Normalization methods.
	 */
	public enum Normalization
	{
		EUCLIDEAN,
		MINMAX,
		MEAN
	}

	/**
	 * Compute the normalised root mean squared error (NRMSE) score between reference and processed images. The metrics
	 * run on the whole image (regardless of the dimensions).
	 * <p>
	 * Three normalization methods are available:
	 * <ul>
	 * <li>euclidean: NRMSE = sqrt(MSE(reference, processed)) * sqrt(N) / || reference || </li>
	 * <li>min-max: NRMSE = sqrt(MSE(reference, processed)) / |max(reference)-min(reference)| </li>
	 * <li>mean: NRMSE = sqrt(MSE(reference, processed)) / mean(reference) </li>
	 * </ul>
	 *
	 * @param reference
	 * 		Reference image
	 * @param processed
	 * 		Processed image
	 * @param <T>
	 * 		Type of the image pixels
	 *
	 * @return Metrics score
	 */
	public static < T extends RealType< T > > double computeMetrics(
			final RandomAccessibleInterval< T > reference,
			final RandomAccessibleInterval< T > processed,
			final Normalization norm )
	{
		if ( !Intervals.equalDimensions( reference, processed ) )
			throw new IllegalArgumentException( "Image dimensions must match." );

		// get mse
		double mse = MSE.computeMetrics( reference, processed );

		double nFactor;
		if ( norm == EUCLIDEAN )
		{
			nFactor = getEuclideanNorm( reference );
		}
		else if ( norm == MINMAX )
		{
			nFactor = getMinMaxNorm( reference );
		}
		else // if ( norm == MEAN )
		{
			nFactor = getMean( reference );
		}

		return nFactor > 0 ? Math.sqrt( mse ) / nFactor : Double.NaN;
	}

	protected static < T extends RealType< T > > double getEuclideanNorm(
			final RandomAccessibleInterval< T > reference )
	{
		// get image size
		final long nPixels = Intervals.numElements( reference );

		if ( nPixels > 0 )
		{
			double ms = 0.;
			final Cursor< T > cu = Views.iterable( reference ).cursor();
			while ( cu.hasNext() )
			{
				double dRef = cu.next().getRealDouble();
				ms += dRef * dRef / nPixels; // division here to avoid precision loss due to overflow
			}

			return Math.sqrt( ms );
		}
		return Double.NaN;
	}

	protected static < T extends RealType< T > > double getMinMaxNorm(
			final RandomAccessibleInterval< T > reference )
	{
		T min = reference.randomAccess().get().copy();
		T max = min.copy();

		ComputeMinMax.computeMinMax( reference, min, max );

		return max.getRealDouble() - min.getRealDouble();
	}

	protected static < T extends RealType< T > > double getMean(
			final RandomAccessibleInterval< T > reference )
	{
		// get image size
		final long nPixels = Intervals.numElements( reference );

		if ( nPixels > 0 )
		{
			double mean = 0.;
			final Cursor< T > cu = Views.iterable( reference ).cursor();
			while ( cu.hasNext() )
			{
				double dRef = cu.next().getRealDouble();
				mean += dRef / nPixels; // division here to avoid precision loss due to overflow
			}
			return mean;
		}

		return Double.NaN;
	}
}
