package net.imglib2.algorithm.metrics.segmentation;

import java.util.Arrays;
import java.util.HashMap;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.IntegerType;

import static net.imglib2.algorithm.metrics.segmentation.SegmentationHelper.hasIntersectingLabels;

/**
 * The LazyMultiMetrics computes a running {@link MultiMetrics} over all images added by calling
 * {@link #addTimePoint(ImgLabeling, ImgLabeling) addTimePoint}. The images are expected to be
 * of dimension XYZ, where Z can be of depth 1.
 * <p>
 * The scores are calculated by accumulating the number of TP, FP, FN and the sum of the IoU for each image.
 * Each contribution is computed by adding images using {@link #addTimePoint(ImgLabeling, ImgLabeling) addTimePoint}. The final score can be queried by
 * {@link #computeScore()}.
 * <p>
 * Each image's contributions are calculated independently. Therefore, the same LazyMultiMetrics object
 * can be called from multiple threads in order to speed up the computation. For instance, if the
 * total stack does not fit in memory, lazy loading and multithreading can be used to compute the
 * SEG score by splitting the XYZ images between threads and adding them one by one. The final score
 * can then be calculated once all threads have finished.
 * <p>
 * The {@link MultiMetrics} scores are calculated at a certain {@code threshold}. This threshold is
 * the minimum IoU between a ground-truth and a prediction label at which two labels are considered
 * a potential match. The {@code threshold} can only be set during instantiation.
 *
 * @author Joran Deschamps
 */
public class LazyMultiMetrics
{
	private final MultiMetrics.MetricsSummary summary;

	private final double threshold;

	/**
	 * Constructor with a default threshold of 0.5.
	 */
	public LazyMultiMetrics(){
		this.threshold = 0.5;
		summary = new MultiMetrics.MetricsSummary();
	};

	/**
	 * Constructor that sets the threshold value.
	 *
	 * @param threshold Threshold
	 */
	public LazyMultiMetrics( double threshold )
	{
		this.threshold = threshold;
		summary = new MultiMetrics.MetricsSummary();
	}

	/**
	 * Add a new images pair and compute its contribution to the metrics scores. The current
	 * scores can be computed by calling {@link #computeScore()}. This method is not compatible with
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
	 * Add a new images pair and compute its contribution to the metrics scores. The current
	 * scores can be computed by calling {@link #computeScore()}.
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
			RandomAccessibleInterval< J > prediction
	)
	{

		if ( !Arrays.equals( groundTruth.dimensionsAsLongArray(), prediction.dimensionsAsLongArray() ) )
			throw new IllegalArgumentException( "Image dimensions must match." );

		// compute multi metrics between the two images
		final MultiMetrics.MetricsSummary result = MultiMetrics.runSingle( groundTruth, prediction, threshold );

		// add results
		summary.addPoint( result );
	}

	/**
	 * Compute the total SEG score. If no image was added, or all images were empty, then the metrics
	 * scores are TP=FP=FN=0 and NaN for the others.
	 *
	 * @return Metrics scores
	 */
	public HashMap< MultiMetrics.Metrics, Double > computeScore()
	{
		return summary.getScores();
	}
}
