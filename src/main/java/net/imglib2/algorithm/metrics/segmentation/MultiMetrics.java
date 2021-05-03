package net.imglib2.algorithm.metrics.segmentation;

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
 * Pixels with value 0 are considered background and are ignored during the metrics calculation. If the
 * ground-truth image is only background (no labels), then the metrics returns TP=FP=FN=0 and NaN for
 * the others.
 * <p>
 * The metrics expect images of dimensions XYZT, where Z and T can be of depth 1. The metrics scores
 * are calculated for each ground-truth label in each XYZ volume (XY if dimension Z is of depth 1),
 * and averaged over the total number of ground-truth labels in the XYZT volume. If the Z dimension
 * has depth greater than 1, then the labels are considered 3D and pixels of equal values at different
 * depths are considered to be part of the same labeling.
 * <p>
 * Finally, if the image stack does not fit in memory, you can use {@link LazyMultiMetrics} to compute
 * running metrics scores for which images can be added one at time.
 * <p>
 * Note: the accuracy is in some cases also called the average precision (see DSB challenge).
 * <p>
 * This class was inspired from the <a href="https://github.com/stardist/stardist">StarDist repository</a>.
 *
 * @author Joran Deschamps
 * @see <a href="https://github.com/stardist/stardist">StarDist metrics</a>
 */
public class MultiMetrics
{

	private final static int T_AXIS = 3;

	/**
	 * Metrics computed by {@link MultiMetrics} objects.
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

	// TODO does not make sense to have the atomic values here since they are only really useful for the LazyMultiMetrics

	/**
	 * An object holding the sum of TP, FP, FN and IoU values, accumulated over the T dimension. These
	 * quantities are then used to compute all other metrics by calling {@link #getScores()}. Since the
	 * aggregates are held by atomic objects, this class is compatible with multithreaded calls.
	 */
	protected static class MetricsSummary
	{
		private int tp = 0;

		private int fp = 0;

		private int fn = 0;

		private double sumIoU = 0.;

		/**
		 * Add the {@code otherMetrics} values to the aggregate of the current object.
		 *
		 * @param otherMetrics
		 * 		MetricsSummary containing values to add to the aggregates
		 */
		public void addPoint( MetricsSummary otherMetrics )
		{
			this.tp += otherMetrics.tp;
			this.fp += otherMetrics.fp;
			this.fn += otherMetrics.fn;
			this.sumIoU += otherMetrics.sumIoU;
		}

		/**
		 * Add the values to the aggregates.
		 *
		 * @param tp
		 * 		Number of TP to add to the TP aggregate
		 * @param fp
		 * 		Number of FP to add to the FP aggregate
		 * @param fn
		 * 		Number of FN to add to the FN aggregate
		 * @param sumIoU
		 * 		IoU sum to add to the IoU aggregate
		 */
		public void addPoint( int tp, int fp, int fn, double sumIoU )
		{
			this.tp += tp;
			this.fp += fp;
			this.fn += fn;
			this.sumIoU += sumIoU;
		}

		public int getTP()
		{
			return tp;
		}

		public int getFN()
		{
			return fn;
		}

		public int getFP()
		{
			return fp;
		}

		public double getIoU()
		{
			return sumIoU;
		}

		protected double meanMatchedIoU( double tp, double sumIoU )
		{
			return tp > 0 ? sumIoU / tp : Double.NaN;
		}

		protected double meanTrueIoU( double tp, double fn, double sumIoU )
		{
			return ( tp + fn ) > 0 ? sumIoU / ( tp + fn ) : Double.NaN;
		}

		protected double precision( double tp, double fp )
		{
			return ( tp + fp ) > 0 ? tp / ( tp + fp ) : Double.NaN;
		}

		protected double recall( double tp, double fn )
		{
			return ( tp + fn ) > 0 ? tp / ( tp + fn ) : Double.NaN;
		}

		protected double f1( double precision, double recall )
		{
			return ( precision + recall ) > 0 ? 2 * precision * recall / ( precision + recall ) : Double.NaN;
		}

		protected double accuracy( double tp, double fp, double fn )
		{
			return ( tp + fn + fp ) > 0 ? tp / ( tp + fn + fp ) : Double.NaN;
		}

		/**
		 * Compute all metrics scores from the aggregates.
		 *
		 * @return Map of the metrics scores
		 */
		public HashMap< Metrics, Double > getScores()
		{
			HashMap< Metrics, Double > metrics = new HashMap<>();

			// compute metrics given tp, fp, fn and sumIoU
			double meanMatched = meanMatchedIoU( tp, sumIoU );
			double meanTrue = meanTrueIoU( tp, fn, sumIoU );
			double precision = precision( tp, fp );
			double recall = recall( tp, fn );
			double f1 = f1( precision, recall );
			double accuracy = accuracy( tp, fp, fn );

			// add to the map
			metrics.put( Metrics.TP, ( double ) tp );
			metrics.put( Metrics.FP, ( double ) fp );
			metrics.put( Metrics.FN, ( double ) fn );
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
	 * Compute the global metrics scores between labels from a ground-truth and a predicted image. The
	 * method expects images of dimension XYZT. The scores are computed over each XYZ volume (or XY if
	 * Z is of depth 1) and accumulated. The mean matched IoU and mean true IoU scores are then averaged
	 * over all true positives or all ground-truth labels, respectively. If both images are empty (only
	 * pixels with value 0), then the metrics score is NaN.
	 * <p>
	 * The {@code threshold} is the minimum IoU between a ground-truth and a prediction label at which
	 * two labels are considered a potential match.
	 * <p>
	 * This method is not compatible with {@link ImgLabeling} with intersecting labels.
	 *
	 * @param groundTruth
	 * 		Ground-truth image
	 * @param prediction
	 * 		Predicted image
	 * @param threshold
	 * 		Threshold
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
	public static < T, I extends IntegerType< I >, U, J extends IntegerType< J > > HashMap< Metrics, Double > computeMetrics(
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
	 * Compute the global metrics scores between labels from a ground-truth and a predicted image. The
	 * method expects images of dimension XYZT. The scores are computed over each XYZ volume (or XY if
	 * Z is of depth 1) and accumulated. The mean matched IoU and mean true IoU scores are then averaged
	 * over all true positives or all ground-truth labels, respectively. If both images are empty (only
	 * pixels with value 0), then the metrics score is NaN.
	 * <p>
	 * The {@code threshold} is the minimum IoU between a ground-truth and a prediction label at which
	 * two labels are considered a potential match.
	 *
	 * @param groundTruth
	 * 		Ground-truth image
	 * @param prediction
	 * 		Predicted image
	 * @param threshold
	 * 		Threshold
	 * @param <I>
	 * 		Ground-truth pixel type
	 * @param <J>
	 * 		Prediction pixel type
	 *
	 * @return Metrics scores
	 */
	public static < I extends IntegerType< I >, J extends IntegerType< J > > HashMap< Metrics, Double > computeMetrics(
			RandomAccessibleInterval< I > groundTruth,
			RandomAccessibleInterval< J > prediction,
			double threshold )
	{

		if ( !Arrays.equals( groundTruth.dimensionsAsLongArray(), prediction.dimensionsAsLongArray() ) )
			throw new IllegalArgumentException( "Image dimensions must match." );

		// check if it is a time-lapse
		boolean timeLapse = false;
		if ( groundTruth.dimensionsAsLongArray().length > T_AXIS )
		{
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

	protected static < I extends IntegerType< I >, J extends IntegerType< J > > MetricsSummary runAverageOverTime(
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

	protected static < I extends IntegerType< I >, J extends IntegerType< J > > MetricsSummary runSingle(
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
	protected static double[][] computeCostMatrix( ConfusionMatrix cM, double threshold )
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
	protected static double getLocalIoUScore( ConfusionMatrix cM, int iGT, int jPred, double threshold )
	{
		// number of true positive pixels
		double tp = cM.getIntersection( iGT, jPred );

		// size of each label (number of pixels in each label)
		int sumI = cM.getGroundTruthLabelSize( iGT );
		int sumJ = cM.getPredictionLabelSize( jPred );

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
	protected static MetricsSummary computeFinalScores( ConfusionMatrix confusionMatrix, double[][] costMatrix, double threshold )
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
