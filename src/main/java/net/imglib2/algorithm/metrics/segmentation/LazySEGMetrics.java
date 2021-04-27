package net.imglib2.algorithm.metrics.segmentation;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.util.Pair;

import static net.imglib2.algorithm.metrics.segmentation.SegmentationHelper.hasIntersectingLabels;

/**
 * The LazySEGMetrics computes a running {@link SEGMetrics} over all images added by calling
 * {@link #addTimePoint(ImgLabeling, ImgLabeling) addTimePoint}. The images are expected to be
 * of dimension XYZ, where Z can be of depth 1.
 * <p>
 * The score is computed by accumulating the number of ground-truth labels and their corresponding
 * IoU with respect to matching prediction labels. Each contribution is computed by adding images
 * using {@link #addTimePoint(ImgLabeling, ImgLabeling) addTimePoint}. The final score can be queried by {@link #computeScore()}.
 * <p>
 * Each image's contribution is calculated independently. Therefore, the same LazYSEGMetrics object
 * can be called from multiple threads in order to speed up the computation. For instance, if the
 * total stack does not fit in memory, lazy loading and multithreading can be used to compute the
 * SEG score by splitting the XYZ images between threads and adding them one by one. The final score
 * can then be calculated once all threads have finished.
 * <p>
 * Reference: Ulman, V., Maška, M., Magnusson, K. et al. An objective comparison of cell-tracking
 * algorithms. Nat Methods 14, 1141–1152 (2017).
 *
 * @author Joran Deschamps
 * @see <a href="https://github.com/CellTrackingChallenge/CTC-FijiPlugins">Original implementation by Martin Maška and Vladimír Ulman</a>
 */
public class LazySEGMetrics
{

	private AtomicInteger nGT = new AtomicInteger( 0 );

	private AtomicLong sumScores = new AtomicLong( 0 );

	/**
	 * Add a new images pair and compute its contribution to the SEG metrics. The current SEG score
	 * can be computed by calling {@link #computeScore()}. This method is not compatible with
	 * {@link ImgLabeling} with intersecting labels.
	 *
	 * @param groundTruth
	 * 		Ground-truth image
	 * @param prediction
	 * 		Predicted image
	 * @param <T>
	 * 		Label type associated to the ground-truth
	 * @param <I>
	 * 		Ground-truth pixel type
	 * @param <U>
	 * 		Label type associated to the prediction
	 * @param <J>
	 * 		Prediction pixel type
	 */
	public < T, I extends IntegerType< I >, U, J extends IntegerType< J > > void addTimePoint(
			final ImgLabeling< T, I > groundTruth,
			final ImgLabeling< U, J > prediction
	)
	{
		if ( hasIntersectingLabels( groundTruth ) || hasIntersectingLabels( prediction ) )
			throw new UnsupportedOperationException( "ImgLabeling with intersecting labels are not supported." );

		addTimePoint( groundTruth.getIndexImg(), prediction.getIndexImg() );
	}

	/**
	 * Add a new images pair and compute its contribution to the SEG metrics. The current SEG score
	 * can be computed by calling {@link #computeScore()}.
	 *
	 * @param groundTruth
	 * 		Ground-truth image
	 * @param prediction
	 * 		Predicted image
	 * @param <I>
	 * 		Ground-truth pixel type
	 * @param <J>
	 * 		Prediction pixel type
	 */
	public < I extends IntegerType< I >, J extends IntegerType< J > > void addTimePoint(
			RandomAccessibleInterval< I > groundTruth,
			RandomAccessibleInterval< J > prediction )
	{

		if ( !Arrays.equals( groundTruth.dimensionsAsLongArray(), prediction.dimensionsAsLongArray() ) )
			throw new IllegalArgumentException( "Image dimensions must match." );

		// compute SEG between the two images
		final Pair< Integer, Double > result = SEGMetrics.runSingle( groundTruth, prediction );

		// ignore NaNs
		if ( Double.compare( result.getB(), Double.NaN ) != 0 )
		{
			nGT.addAndGet( result.getA() );
			addToAtomicLong( sumScores, result.getB() );
		}
	}

	/**
	 * Compute the total SEG score. If no image was added, or all images were empty, then the SEG score
	 * is NaN.
	 *
	 * @return SEG score
	 */
	public double computeScore()
	{
		return nGT.get() > 0 ? atomicLongToDouble( sumScores ) / ( double ) nGT.get() : Double.NaN;
	}

	private void addToAtomicLong( AtomicLong a, double b )
	{
		a.set( Double.doubleToRawLongBits( Double.longBitsToDouble( a.get() ) + b ) );
	}

	private double atomicLongToDouble( AtomicLong a )
	{
		return Double.longBitsToDouble( a.get() );
	}
}