package net.imglib2.algorithm.metrics.segmentation;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.metrics.segmentation.assignment.MunkresKuhnAlgorithm;
import net.imglib2.type.numeric.IntegerType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

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
 * <p>
 * Pixels with value 0 are considered background and are ignored during the metrics calculation. If
 * both images are only background, then the metrics returns NaN and TP=FP=FN=0.
 * <p>
 * All metrics are computed simultaneously. The default metrics, which can be set at instantiation or using
 * the {@link #setMetrics(Metrics)} method, defines which metrics score is returned by {@link #computeMetrics(RandomAccessibleInterval, RandomAccessibleInterval) computeMetrics}.
 * After computation, all metrics score are available through {@link #getScore(Metrics)}. All metrics score can be
 * returned by a single call to {@link #computeAllMetrics(RandomAccessibleInterval, RandomAccessibleInterval) computeAllMetrics}.
 * <p>
 * Finally, the {@code threshold} can be set during instantiation, as well as in between calls to
 * {@link #computeMetrics(RandomAccessibleInterval, RandomAccessibleInterval) computMetrics} by using the {@link #setThreshold(double) setThreshold} method.
 *
 * @author Joran Deschamps
 * @see <a href="https://github.com/stardist/stardist">StarDist metrics</a>
 */
public class MultiMetrics extends Accuracy
{
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

	private Map< Metrics, Double > metricsResult;

	private Metrics defaultMetrics;

	/**
	 * Instantiate a MultiMetrics with accuracy as default metrics
	 * and a threshold of 0.5. The default metrics defines which score is returned
	 * by {@link #computeMetrics(RandomAccessibleInterval, RandomAccessibleInterval) computeMetrics},
	 * while the threshold is the minimum IoU at which ground-truth and prediction labels can be
	 * considered to be a potential match.
	 */
	public MultiMetrics()
	{
		this( Metrics.ACCURACY, 0.5 );
	}

	/**
	 * Instantiate a MultiMetrics with accuracy as default metrics
	 * and a threshold equal to {@code defaultThreshold}. The default metrics defines which score is returned
	 * by {@link #computeMetrics(RandomAccessibleInterval, RandomAccessibleInterval) computeMetrics},
	 * while the threshold is the minimum IoU at which ground-truth and prediction labels can be
	 * considered to be a potential match.
	 *
	 * @param defaultThreshold
	 * 		Threshold
	 */
	public MultiMetrics( double defaultThreshold )
	{
		this( Metrics.ACCURACY, defaultThreshold );
	}

	/**
	 * Instantiate a MultiMetrics with {@code defaultMetrics} as default metrics
	 * and a threshold equal to {@code defaultThreshold}. The default metrics defines which score is returned
	 * by {@link #computeMetrics(RandomAccessibleInterval, RandomAccessibleInterval) computeMetrics},
	 * while the threshold is the minimum IoU at which ground-truth and prediction labels can be
	 * considered to be a potential match.
	 *
	 * @param defaultMetrics
	 * @param defaultThreshold
	 */
	public MultiMetrics( Metrics defaultMetrics, double defaultThreshold )
	{
		super( defaultThreshold );
		this.defaultMetrics = defaultMetrics;
	}

	/**
	 * Return the current default metrics. The default metrics defines which score is returned
	 * by {@link #computeMetrics(RandomAccessibleInterval, RandomAccessibleInterval) computeMetrics}
	 *
	 * @return Current metrics
	 */
	public Metrics getCurrentMetrics()
	{
		return defaultMetrics;
	}

	/**
	 * Set a new default metrics. The default metrics defines which score is returned
	 * by {@link #computeMetrics(RandomAccessibleInterval, RandomAccessibleInterval) computeMetrics}
	 *
	 * @param metrics
	 *
	 * @return
	 */
	public MultiMetrics setMetrics( Metrics metrics )
	{
		this.defaultMetrics = metrics;
		return this;
	}

	/**
	 * {@inheritdoc}
	 */
	@Override
	public MultiMetrics setThreshold( double threshold )
	{
		return ( MultiMetrics ) super.setThreshold( threshold );
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
	 * @return Map of all metrics scores
	 */
	public < I extends IntegerType< I >, J extends IntegerType< J > > Map< Metrics, Double > computeAllMetrics(
			RandomAccessibleInterval< I > groundTruth,
			RandomAccessibleInterval< J > prediction )
	{

		this.computeMetrics( groundTruth, prediction );

		return metricsResult;
	}

	/**
	 * Returns the score of the specified metrics. The metrics must have been computed beforehand
	 * otherwise the method throws an exception.
	 *
	 * @param metrics
	 * 		Metrics score to be returned
	 *
	 * @return Metrics score
	 */
	public double getScore( Metrics metrics )
	{
		if ( metricsResult == null )
			throw new NullPointerException( "No metrics has been calculated yet." );

		return metricsResult.get( metrics );
	}

	/**
	 * Compute all metrics scores by matching ground-truth labels (row indices) with predicted labels
	 * (column indices). The assignment is performed by the Munkres-Kuhn algorithm using the {@code costMatrix}.
	 * This method returns the default metrics score. All metrics scores can be accessed by calling
	 * {@link #getScore(Metrics)} after this method.
	 *
	 * @param confusionMatrix
	 * 		Confusion matrix
	 * @param costMatrix
	 * 		Cost matrix
	 *
	 * @return Default metrics score
	 */
	@Override
	protected double computeMetrics( final ConfusionMatrix confusionMatrix, double[][] costMatrix )
	{
		metricsResult = new HashMap<>();

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
				if ( -costMatrix[ assignment[ i ][ 0 ] ][ assignment[ i ][ 1 ] ] >= getThreshold() )
				{
					tp++;
					sumIoU += -costMatrix[ assignment[ i ][ 0 ] ][ assignment[ i ][ 1 ] ];
				}
			}

			// compute all metrics
			double fn = confusionMatrix.getNumberGroundTruthLabels() - tp;
			double fp = confusionMatrix.getNumberPredictionLabels() - tp;
			double meanMatched = tp > 0 ? sumIoU / tp : 0;
			double meanTrue = sumIoU / confusionMatrix.getNumberGroundTruthLabels();
			double precision = ( tp + fp ) > 0 ? tp / ( tp + fp ) : 0;
			double recall = ( tp + fn ) > 0 ? tp / ( tp + fn ) : 0;
			double accuracy = ( tp + fn + fp ) > 0 ? tp / ( tp + fn + fp ) : 0;
			double f1 = ( precision + recall ) > 0 ? 2 * precision * recall / ( precision + recall ) : 0;

			// add to the map
			metricsResult.put( Metrics.TP, ( double ) tp );
			metricsResult.put( Metrics.FP, fp );
			metricsResult.put( Metrics.FN, fn );
			metricsResult.put( Metrics.MEAN_MATCHED_IOU, meanMatched );
			metricsResult.put( Metrics.MEAN_TRUE_IOU, meanTrue );
			metricsResult.put( Metrics.PRECISION, precision );
			metricsResult.put( Metrics.RECALL, recall );
			metricsResult.put( Metrics.ACCURACY, accuracy );
			metricsResult.put( Metrics.F1, f1 );
		}
		else if ( confusionMatrix.getNumberGroundTruthLabels() == 0 &&
				confusionMatrix.getNumberPredictionLabels() == 0 )
		{
			metricsResult.put( Metrics.TP, 0. );
			metricsResult.put( Metrics.FP, 0. );
			metricsResult.put( Metrics.FN, 0. );
			metricsResult.put( Metrics.MEAN_MATCHED_IOU, Double.NaN );
			metricsResult.put( Metrics.MEAN_TRUE_IOU, Double.NaN );
			metricsResult.put( Metrics.PRECISION, Double.NaN );
			metricsResult.put( Metrics.RECALL, Double.NaN );
			metricsResult.put( Metrics.ACCURACY, Double.NaN );
			metricsResult.put( Metrics.F1, Double.NaN );
		}
		else
		{
			double fn = confusionMatrix.getNumberGroundTruthLabels();
			double fp = confusionMatrix.getNumberPredictionLabels();

			metricsResult.put( Metrics.TP, 0. );
			metricsResult.put( Metrics.FP, fp );
			metricsResult.put( Metrics.FN, fn );
			metricsResult.put( Metrics.MEAN_MATCHED_IOU, 0. );
			metricsResult.put( Metrics.MEAN_TRUE_IOU, 0. );
			metricsResult.put( Metrics.PRECISION, 0. );
			metricsResult.put( Metrics.RECALL, 0. );
			metricsResult.put( Metrics.ACCURACY, 0. );
			metricsResult.put( Metrics.F1, 0. );
		}

		return metricsResult.get( defaultMetrics );
	}
}
