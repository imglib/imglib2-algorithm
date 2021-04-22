package net.imglib2.algorithm.metrics.segmentation;

import java.util.concurrent.atomic.AtomicLong;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.metrics.segmentation.assignment.MunkresKuhnAlgorithm;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.IntegerType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Stream;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import static net.imglib2.algorithm.metrics.segmentation.SegmentationHelper.hasIntersectingLabels;

/**
 * Compute a set of metrics at a specific {@code threshold} between the labels of a predicted and of a
 * ground-truth image. In the context of instance segmentation, the labels are the pixel values. The
 * metrics score are obtained by matching ground-truth with prediction labels using the Munkres-Kuhn
 * algorithm. The assignment is based on a cost matrix, in which each element is the negation of the
 * IoU (a.k.a Jaccard index) between ground-truth and prediction labels if the IoU is greater than or
 * equal to the {@code threshold} and 0 otherwise. The metrics are the following:
 * <p>
 * TP = number of matched ground-truth labels
 * FP = number of unmatched predicted labels
 * FN = number of unmatched ground-truth labels
 * Precision = TP / (TP + FP)
 * Recall = TP / (TP + FN)
 * F1 = 2 * Precision * Recall / (Precision + Recall)
 * Accuracy = TP / ( TP + FP + FN)
 * Mean matched IoU = sum of the matched labels IoU / TP
 * Mean true IoU = sum of the matched labels IoU / number of ground-truth labels
 * <p>
 * The metrics are computed over all dimensions, meaning that the score over a 3D stack will be calculated
 * by considering that all pixels in the 3D stack with equal pixel value belong to the same label. For
 * slice-wise scoring, run the metrics on each slice individually.
 * <p> //TODO
 * Pixels with value 0 are considered background and are ignored during the metrics calculation. If
 * both images are only background, then the metrics returns NaN and TP=FP=FN=0.
 * <p>
 * All metrics are computed simultaneously. The default metrics, which can be set at instantiation or using
 * the {@link #(Metrics)} method, defines which metrics score is returned by {@link #(RandomAccessibleInterval, RandomAccessibleInterval) computeMetrics}.
 * After computation, all metrics score are available through {@link #(Metrics)}. All metrics score can be
 * returned by a single call to {@link #(RandomAccessibleInterval, RandomAccessibleInterval) computeAllMetrics}.
 * <p>
 * Finally, the {@code threshold} can be set during instantiation, as well as in between calls to
 * {@link #(RandomAccessibleInterval, RandomAccessibleInterval) computMetrics} by using the {@link #(double) setThreshold} method.
 * <p>
 * This class was inspired from the <a href="https://github.com/stardist/stardist">StarDist repository</a>.
 *
 * @author Joran Deschamps
 * @see <a href="https://github.com/stardist/stardist">StarDist metrics</a>
 */
public class MultiMetrics
{

	private static int T_AXIS = 3;

	/**
	 * Metrics computed by the {@link MultiMetrics}.
	 */
	public enum Metrics
	{
		ACCURACY( "Accuracy" ),
		MEAN_MATCHED_IOU( "Mean matched IoU" ),
		MEAN_TRUE_IOU( "Mean true IoU" ),
		TP( "True positives" ),
		FP( "False positives" ),
		FN( "False negatives" ),
		PRECISION( "Precision" ),
		RECALL( "Recall" ),
		F1( "F1" );

		final private String name;

		Metrics( String name )
		{
			this.name = name;
		}

		public String getName()
		{
			return name;
		}

		/**
		 * Return a stream of the available metrics.
		 *
		 * @return Stream of Metrics
		 */
		public static Stream< Metrics > stream()
		{
			Metrics[] all = { ACCURACY, MEAN_MATCHED_IOU, MEAN_TRUE_IOU, TP, FP, FN, PRECISION, RECALL, F1 };
			return Arrays.stream( all );
		}
	}

	public static class MetricsSummary
	{
		private AtomicLong aTP = new AtomicLong(0);

		private AtomicLong aFP = new AtomicLong(0);

		private AtomicLong aFN = new AtomicLong(0);

		private AtomicLong aSumIoU = new AtomicLong(0);

		public void addPoint( MetricsSummary metrics )
		{
			this.aTP.addAndGet( metrics.aTP.get() );
			this.aFP.addAndGet( metrics.aFP.get() );
			this.aFN.addAndGet( metrics.aFN.get() );

			addToAtomicLong(aSumIoU, atomicLongToDouble(metrics.aSumIoU));
		}

		public void addPoint( int tp, int fp, int fn, double sumIoU )
		{
			this.aTP.addAndGet( tp );
			this.aFP.addAndGet( fp );
			this.aFN.addAndGet( fn );

			addToAtomicLong(aSumIoU, sumIoU);
		}

		private void addToAtomicLong(AtomicLong a, double b){
			a.set( Double.doubleToRawLongBits( Double.longBitsToDouble( a.get()) + b ) );
		}

		private double atomicLongToDouble(AtomicLong a){
			return Double.longBitsToDouble(a.get());
		}

		private double meanMatchedIoU( double tp, double sumIoU )
		{
			return tp > 0 ? sumIoU / tp : Double.NaN;
		}

		private double meanTrueIoU( double tp, double fn, double sumIoU )
		{
			return ( tp + fn ) > 0 ? sumIoU / ( tp + fn ) : Double.NaN;
		}

		private double precision( double tp, double fp )
		{
			return ( tp + fp ) > 0 ? tp / ( tp + fp ) : Double.NaN;
		}

		private double recall( double tp, double fn )
		{
			return ( tp + fn ) > 0 ? tp / ( tp + fn ) : Double.NaN;
		}

		private double f1( double precision, double recall )
		{
			return ( precision + recall ) > 0 ? 2 * precision * recall / ( precision + recall ) : Double.NaN;
		}

		private double accuracy( double tp, double fp, double fn )
		{
			return ( tp + fn + fp ) > 0 ? tp / ( tp + fn + fp ) : Double.NaN;
		}

		public HashMap< Metrics, Double > getScores()
		{
			HashMap< Metrics, Double > metrics = new HashMap<>();

			// convert atomic elements
			double tp = aTP.get();
			double fp = aFP.get();
			double fn = aFN.get();
			double sumIoU = atomicLongToDouble(aSumIoU);

			// compute metrics given tp, fp, fn and sumIoU
			double meanMatched = meanMatchedIoU( tp, sumIoU );
			double meanTrue = meanTrueIoU( tp, fn, sumIoU );
			double precision = precision( tp, fp );
			double recall = recall( tp, fn );
			double f1 = f1( precision, recall );
			double accuracy = accuracy( tp, fp, fn );

			// add to the map
			metrics.put( Metrics.TP, tp );
			metrics.put( Metrics.FP, fp );
			metrics.put( Metrics.FN, fn );
			metrics.put( Metrics.MEAN_MATCHED_IOU, meanMatched );
			metrics.put( Metrics.MEAN_TRUE_IOU, meanTrue );
			metrics.put( Metrics.PRECISION, precision );
			metrics.put( Metrics.RECALL, recall );
			metrics.put( Metrics.F1, f1 );
			metrics.put( Metrics.ACCURACY, accuracy );

			return metrics;
		}
	}

	/**
	 * Compute a global metrics score between labels from a ground-truth and a predicted image. //TODO
	 * <p>
	 * The methods throws an {@link UnsupportedOperationException} if either of the images has intersecting labels.
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
	 *
	 * @return Metrics score
	 */
	public < T, I extends IntegerType< I >, U, J extends IntegerType< J > > HashMap< Metrics, Double > computeMetrics(
			final ImgLabeling< T, I > groundTruth,
			final ImgLabeling< U, J > prediction,
			double threshold
	)
	{
		if ( hasIntersectingLabels( groundTruth ) || hasIntersectingLabels( prediction ) )
			throw new UnsupportedOperationException( "ImgLabeling with intersecting labels are not supported." );

		return computeMetrics( groundTruth.getIndexImg(), prediction.getIndexImg(), threshold );
	}

	/**
	 * Compute the accuracy score between labels of a predicted and of a ground-truth image.
	 *
	 * @param groundTruth
	 * 		Ground-truth image
	 * @param prediction
	 * 		Predicted image
	 * @param <I>
	 * 		Ground-truth pixel type
	 * @param <J>
	 * 		Prediction pixel type
	 *
	 * @return Metrics score
	 */
	public < I extends IntegerType< I >, J extends IntegerType< J > > HashMap< Metrics, Double > computeMetrics(
			RandomAccessibleInterval< I > groundTruth,
			RandomAccessibleInterval< J > prediction,
			double threshold )
	{

		if ( !Arrays.equals( groundTruth.dimensionsAsLongArray(), prediction.dimensionsAsLongArray() ) )
			throw new IllegalArgumentException( "Image dimensions must match." );

		// check if it is a time-lapse
		boolean timeLapse = false;
		if(groundTruth.dimensionsAsLongArray().length >= 4){
			timeLapse = groundTruth.dimension( T_AXIS ) > 1;
		}

		if ( timeLapse )
		{
			return runAverageOverTime( groundTruth, prediction, threshold ).getScores();
		}
		else
		{
			return runSingle( groundTruth, prediction, threshold ).getScores();
		}
	}

	protected < I extends IntegerType< I >, J extends IntegerType< J > > MetricsSummary runAverageOverTime(
			RandomAccessibleInterval< I > groundTruth,
			RandomAccessibleInterval< J > prediction,
			double threshold )
	{
		int nFrames = Intervals.dimensionsAsIntArray( groundTruth )[ T_AXIS ];

		final MetricsSummary metrics = new MetricsSummary();

		// run over all time indices, and compute metrics on each XY or XYZ hyperslice
		for ( int i = 0; i < nFrames; i++ )
		{
			final RandomAccessibleInterval< I > gtFrame = Views.hyperSlice( groundTruth, T_AXIS, i );
			final RandomAccessibleInterval< J > predFrame = Views.hyperSlice( prediction, T_AXIS, i );

			final MetricsSummary result = runSingle( gtFrame, predFrame, threshold );

			metrics.addPoint( result );
		}

		return metrics;
	}

	protected < I extends IntegerType< I >, J extends IntegerType< J > > MetricsSummary runSingle(
			RandomAccessibleInterval< I > groundTruth,
			RandomAccessibleInterval< J > prediction,
			double threshold )
	{
		// compute confusion matrix
		final ConfusionMatrix confusionMatrix = new ConfusionMatrix( groundTruth, prediction );

		// compute cost matrix
		double[][] costMatrix = computeCostMatrix( confusionMatrix, threshold );

		return computeFinalScores( confusionMatrix, costMatrix, threshold );
	}

	/**
	 * Computes a cost matrix using the confusion matrix {@code cM}. Each element cij of the cost
	 * matrix is the negation of the IoU between ground-truth label i and prediction label j. If
	 * the IoU is smaller than the {@code threshold}, then cij is set to 0. The negation allows
	 * formulating the problem of matching labels as a minimum cost assignment.
	 *
	 * @param cM
	 * 		Confusion matrix
	 *
	 * @return Cost matrix
	 */
	protected double[][] computeCostMatrix( ConfusionMatrix cM, double threshold )
	{
		int M = cM.getNumberGroundTruthLabels();
		int N = cM.getNumberPredictionLabels();

		// empty cost matrix
		// make sure to obtain a rectangular matrix, with Npred > Ngt, in order
		// to avoid empty assignments if using Munkres-Kuhn
		double[][] costMatrix = new double[ M ][ Math.max( M + 1, N ) ];

		// fill in cost matrix
		for ( int i = 0; i < M; i++ )
		{
			for ( int j = 0; j < N; j++ )
			{
				// take the negation to obtain minimum cost assignment problem
				costMatrix[ i ][ j ] = -getLocalIoUScore( cM, i, j, threshold );
			}
		}

		return costMatrix;
	}

	/**
	 * Compute the IoU between the ground-truth label {@code iGT} and the prediction label
	 * {@code jPred}. If the IoU is smaller than the threshold, the method returns 0.
	 *
	 * @param cM
	 * 		Confusion matrix
	 * @param iGT
	 * 		Index of the ground-truth label
	 * @param jPred
	 * 		Index of the prediction label
	 * @param threshold
	 * 		Threshold
	 *
	 * @return IoU between label {@code iGT} and {@code jPred}, or 0 if the IoU
	 * is smaller than the {@code threshold}
	 */
	protected double getLocalIoUScore( ConfusionMatrix cM, int iGT, int jPred, double threshold )
	{
		// number of true positive pixels
		double tp = cM.getIntersection( iGT, jPred );

		// size of each label (number of pixels in each label)
		double sumI = cM.getGroundTruthLabelSize( iGT );
		double sumJ = cM.getPredictionLabelSize( jPred );

		// false positives and false negatives
		double fn = sumI - tp;
		double fp = sumJ - tp;

		double iou = ( tp + fp + fn ) > 0 ? tp / ( tp + fp + fn ) : 0;

		if ( iou < threshold )
		{
			iou = 0;
		}

		return iou;
	}

	/**
	 * Compute all metrics scores by matching ground-truth labels (row indices) with predicted labels
	 * (column indices). The assignment is performed by the Munkres-Kuhn algorithm using the {@code costMatrix}.
	 * This method returns the default metrics score. All metrics scores can be accessed by calling
	 * {@link } after this method.
	 *
	 * @param confusionMatrix
	 * 		Confusion matrix
	 * @param costMatrix
	 * 		Cost matrix
	 *
	 * @return Default metrics score
	 */
	protected MetricsSummary computeFinalScores( ConfusionMatrix confusionMatrix, double[][] costMatrix, double threshold )
	{
		MetricsSummary summary = new MetricsSummary();

		// Note: MunkresKuhnAlgorithm, as implemented, does not change the cost matrix
		int[][] assignment = new MunkresKuhnAlgorithm().computeAssignments( costMatrix );

		if ( assignment.length != 0 && assignment[ 0 ].length != 0 )
		{
			int tp = 0;
			double sumIoU = 0;

			for ( int i = 0; i < assignment.length; i++ )
			{
				// cost matrix values were negative to obtain a minimum assignment problem
				// we retain only "good" assignments, i.e. with -cost > threshold
				if ( -costMatrix[ assignment[ i ][ 0 ] ][ assignment[ i ][ 1 ] ] >= threshold )
				{
					tp++;
					sumIoU += -costMatrix[ assignment[ i ][ 0 ] ][ assignment[ i ][ 1 ] ];
				}
			}

			// compute all metrics
			int fn = confusionMatrix.getNumberGroundTruthLabels() - tp;
			int fp = confusionMatrix.getNumberPredictionLabels() - tp;

			summary.addPoint( tp, fp, fn, sumIoU );
		}
		else
		{
			int fn = confusionMatrix.getNumberGroundTruthLabels();
			int fp = confusionMatrix.getNumberPredictionLabels();

			summary.addPoint( 0, fp, fn, 0. );
		}

		return summary;
	}
}
