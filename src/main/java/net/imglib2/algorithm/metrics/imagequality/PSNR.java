package net.imglib2.algorithm.metrics.imagequality;

import java.util.Arrays;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;

/**
 * Compute the peak signal-to-noise ratio (PSNR) between a reference and a processed image. The
 * metrics runs on the whole image, whether 2D or 3D. In order to get individual slice PSNR, run
 * the metrics on each slice independently.
 *
 * @author Joran Deschamps
 * @see <a href="https://en.wikipedia.org/wiki/Peak_signal-to-noise_ratio">PSNR on Wikipedia</a>
 */
public class PSNR
{
	/**
	 * Compute the peak signal-to-noise ratio (PSNR) score between reference and processed images. The metrics
	 * run on the whole image (regardless of the dimensions).
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
	public static < T extends RealType< T > > double computeMetrics( final RandomAccessibleInterval< T > reference, final RandomAccessibleInterval< T > processed )
	{
		if ( !Arrays.equals( reference.dimensionsAsLongArray(), processed.dimensionsAsLongArray() ) )
			throw new IllegalArgumentException( "Image dimensions must match." );

		// get image range
		// Note: scikit-image (python) expects float types to be between -1 and 1
		// here this will be dramatically different
		final double range = reference.randomAccess().get().getMaxValue()
				- reference.randomAccess().get().getMinValue();

		// compute mse
		double mse = MSE.computeMetrics( reference, processed );

		return mse > 0 ? 10 * Math.log10( range * range / mse ) : Double.NaN;
	}
}
