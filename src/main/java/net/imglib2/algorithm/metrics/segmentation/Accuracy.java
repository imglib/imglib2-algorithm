package net.imglib2.algorithm.metrics.segmentation;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.metrics.segmentation.assignment.MunkresKuhnAlgorithm;
import net.imglib2.type.numeric.IntegerType;

import java.util.Arrays;

/**
 * Compute the accuracy at a specific {@code threshold} between the labels of a predicted and of a
 * ground-truth image. In the context of instance segmentation, the labels are the pixel values. The
 * accuracy score is obtained by matching ground-truth with prediction labels using the Munkres-Kuhn
 * algorithm. The assignment is based on a cost matrix, in which each element is the negation of the
 * IoU (a.k.a Jaccard index) between ground-truth and prediction labels if the IoU is greater than or
 * equal to the {@code threshold} and 0 otherwise. The final score is equal to:
 * <p>
 * Acc = TP / ( TP + FP + FN)
 * <p>
 * where TP is the number of matched ground-truth labels, FP the number of unmatched prediction labels
 * and FN the number of unmatched ground-truth labels.
 * <p>
 * The metrics is computed over all dimensions, meaning that the score over a 3D stack will be calculated
 * by considering that all pixels in the 3D stack with equal pixel value belong to the same label. For
 * slice-wise scoring, run the metrics on each slice individually.
 * <p>
 * Pixels with value 0 are considered background and are ignored during the metrics calculation. If
 * both images are only background, then the metrics score is 1.
 * <p>
 * Finally, the {@code threshold} can be set during instantiation, as well as in between calls to
 * {@link #computeMetrics(RandomAccessibleInterval, RandomAccessibleInterval) computMetrics} by using the {@link #setThreshold(double) setThreshold} method.
 *
 * @author Joran Deschamps
 * @see <a href="https://github.com/stardist/stardist">StarDist metrics</a>
 */
public class Accuracy implements SegmentationMetrics
{

	private double threshold;

	/**
	 * Constructor with a default {@code threshold} value of 0.5. The threshold is the minimum
	 * IoU at which ground-truth and prediction labels can be considered to be a potential match.
	 */
	public Accuracy()
	{
		this( 0.5 );
	}

	/**
	 * Constructor setting the {@code threshold} value. The threshold is the minimum IoU at which
	 * ground-truth and prediction labels can be considered to be a potential match.
	 *
	 * @param threshold
	 */
	public Accuracy( double threshold )
	{
		this.threshold = threshold;
	}

	/**
	 * Return the current {@code threshold} value. The threshold is the minimum IoU at which
	 * ground-truth and prediction labels can be considered to be a potential match.
	 *
	 * @return Threshold value
	 */
	public double getThreshold()
	{
		return threshold;
	}

	/**
	 * Set a new {@code threshold}. The threshold is the minimum IoU at which ground-truth
	 * and prediction labels can be considered to be a potential match.This method allows
	 * method chaining by returning the Accuracy object.
	 *
	 * @param threshold
	 * 		New {@code threshold}
	 *
	 * @return Accuracy object
	 */
	public Accuracy setThreshold( double threshold )
	{
		this.threshold = threshold;
		return this;
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
	public < I extends IntegerType< I >, J extends IntegerType< J > > double computeMetrics(
			RandomAccessibleInterval< I > groundTruth,
			RandomAccessibleInterval< J > prediction )
	{

		if ( !Arrays.equals( groundTruth.dimensionsAsLongArray(), prediction.dimensionsAsLongArray() ) )
			throw new IllegalArgumentException( "Image dimensions must match." );

		// compute confusion matrix
		final ConfusionMatrix confusionMatrix = new ConfusionMatrix( groundTruth, prediction );

		// compute cost matrix
		double[][] costMatrix = computeCostMatrix( confusionMatrix );

		return computeMetrics( confusionMatrix, costMatrix );
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
	protected double[][] computeCostMatrix( ConfusionMatrix cM )
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
	 * Compute the metrics score by matching ground-truth labels (row indices) with predicted labels
	 * (column indices). The assignment is performed by the Munkres-Kuhn algorithm using the {@code costMatrix}.
	 * The score is then calculated as:
	 * <p>
	 * Acc = TP / ( TP + FP + FN)
	 * <p>
	 * where TP is the number of matched ground-truth labels, FP the number of unmatched prediction labels
	 * and FN the number of unmatched ground-truth labels.
	 * <p>
	 * If the confusion matrix has neither ground-truth labels nor prediction labels, then the metrics score
	 * is 1.
	 *
	 * @param confusionMatrix
	 * 		Confusion matrix
	 * @param costMatrix
	 * 		Cost matrix
	 *
	 * @return Metrics score
	 */
	protected double computeMetrics( ConfusionMatrix confusionMatrix, double[][] costMatrix )
	{
		// Note: MunkresKuhnAlgorithm, as implemented, does not change the cost matrix
		int[][] assignment = new MunkresKuhnAlgorithm().computeAssignments( costMatrix );

		if ( assignment.length != 0 && assignment[ 0 ].length != 0 )
		{
			int tp = 0;

			for ( int i = 0; i < assignment.length; i++ )
			{
				// cost matrix values were negative to obtain a minimum assignment problem
				// we retain only "good" assignments
				if ( -costMatrix[ assignment[ i ][ 0 ] ][ assignment[ i ][ 1 ] ] >= threshold )
				{
					tp++;
				}
			}

			double fn = confusionMatrix.getNumberGroundTruthLabels() - tp;
			double fp = confusionMatrix.getNumberPredictionLabels() - tp;

			return ( tp + fn + fp ) > 0 ? tp / ( tp + fn + fp ) : 0;
		}
		else if ( confusionMatrix.getNumberGroundTruthLabels() == 0 &&
				confusionMatrix.getNumberPredictionLabels() == 0 )
		{
			return 1.;
		}
		return 0.;
	}
}
