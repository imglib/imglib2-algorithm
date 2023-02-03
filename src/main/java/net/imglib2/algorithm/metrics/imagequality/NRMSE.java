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
