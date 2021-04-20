package net.imglib2.algorithm.metrics.segmentation;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.IntegerType;

import java.util.Arrays;

/**
 * The SEG metrics computes the IoU (a.k.a. Jaccard index) metrics between ground-truth labels
 * and the prediction labels that have an overlap percentage greater than 0.5. The final score
 * is averaged over all ground-truth labels. In the context of instance segmentation, the labels
 * are the pixel values.
 * <p>
 * The metrics is computed over all dimensions, meaning that the score over a 3D stack will be
 * calculated by considering that all pixels in the 3D stack with equal pixel value belong to the
 * same label. For slice-wise scoring, run the metrics on each slice individually.
 * <p>
 * Finally, pixels with value 0 are considered background and are ignored during the metrics
 * calculation. If both images are only background, then the metrics returns NaN.
 * <p>
 * Reference: Ulman, V., Maška, M., Magnusson, K. et al. An objective comparison of cell-tracking
 * algorithms. Nat Methods 14, 1141–1152 (2017).
 *
 * @author Joran Deschamps
 * @see <a href="https://github.com/CellTrackingChallenge/CTC-FijiPlugins">Original implementation by Martin Maška and Vladimír Ulman</a>
 */
public class SEG implements SegmentationMetrics
{
	/**
	 * Compute the accuracy score between labels of a predicted and of a ground-truth image. If none
	 * of the images have labels, then the metrics returns NaN.
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

		return run( groundTruth, prediction );
	}

	private < I extends IntegerType< I >, J extends IntegerType< J > > double run(
			RandomAccessibleInterval< I > groundTruth,
			RandomAccessibleInterval< J > prediction )
	{
		// compute confusion matrix
		final ConfusionMatrix confusionMatrix = new ConfusionMatrix( groundTruth, prediction );

		// compute cost matrix
		double[][] costMatrix = computeCostMatrix( confusionMatrix );

		return computeMetrics( confusionMatrix, costMatrix );
	}

	private double[][] computeCostMatrix( ConfusionMatrix cM )
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
				costMatrix[ i ][ j ] = getLocalIoUScore( cM, i, j );
			}
		}

		return costMatrix;
	}

	private double getLocalIoUScore( ConfusionMatrix cM, int i, int j )
	{
		double intersection = cM.getIntersection( i, j );
		double gtSize = cM.getGroundTruthLabelSize( i );

		double overlap = intersection / gtSize;
		if ( overlap > 0.5 )
		{
			double predSize = cM.getPredictionLabelSize( j );

			return intersection / ( gtSize + predSize - intersection );
		}
		else
		{
			return 0.;
		}
	}

	private double computeMetrics( ConfusionMatrix confusionMatrix, double[][] costMatrix )
	{
		if ( costMatrix.length != 0 && costMatrix[ 0 ].length != 0 )
		{
			final int M = costMatrix.length;
			final int N = costMatrix[ 0 ].length;

			double precision = 0.;
			for ( int i = 0; i < M; i++ )
			{
				for ( int j = 0; j < N; j++ )
				{
					precision += costMatrix[ i ][ j ];
				}
			}

			return precision / ( double ) M;
		}
		else if ( confusionMatrix.getNumberGroundTruthLabels() == 0 &&
				confusionMatrix.getNumberPredictionLabels() == 0 )
		{
			return Double.NaN;
		}
		return 0.;
	}
}
