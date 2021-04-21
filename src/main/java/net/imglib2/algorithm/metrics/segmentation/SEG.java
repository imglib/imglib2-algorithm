package net.imglib2.algorithm.metrics.segmentation;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.IntegerType;

import java.util.Arrays;
import net.imglib2.util.Intervals;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import net.imglib2.view.Views;

import static net.imglib2.algorithm.metrics.segmentation.SegmentationHelper.hasIntersectingLabels;

/**
 * The SEG metrics computes the IoU (a.k.a. Jaccard index) metrics between ground-truth labels
 * and the prediction labels that have an overlap percentage greater than 0.5. The final score
 * is averaged over all ground-truth labels. In the context of instance segmentation, the labels
 * are the pixel values.
 * <p>
 * // TODO write about time dimension (if needed use Views to change axis number)
 * // TODO expects XYZT, if T and Z then it runs on XYZ, if T runs on XY
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
public class SEG
{
	// TODO Tests for T and 3D, and NaN
	// TODO finish java doc
	// TODO how to make it practical for many files for users that want weighted average?

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
	public < T, I extends IntegerType< I >, U, J extends IntegerType< J > > double computeMetrics(
			final ImgLabeling< T, I > groundTruth,
			final ImgLabeling< U, J > prediction
	)
	{
		if ( hasIntersectingLabels( groundTruth ) || hasIntersectingLabels( prediction ) )
			throw new UnsupportedOperationException( "ImgLabeling with intersecting labels are not supported." );

		return computeMetrics( groundTruth.getIndexImg(), prediction.getIndexImg() );
	}


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

		// check if it is a time-lapse
		boolean timeLapse = groundTruth.dimension( 3 ) > 1;

		if ( timeLapse )
		{
			return runAverageOverTime( groundTruth, prediction );
		}
		else
		{
			final Pair< Integer, Double > result = runSingle( groundTruth, prediction );
			return result.getA() > 0 ? result.getB() / ( double ) result.getA() : Double.NaN;
		}
	}

	private < I extends IntegerType< I >, J extends IntegerType< J > > double runAverageOverTime(
			RandomAccessibleInterval< I > groundTruth,
			RandomAccessibleInterval< J > prediction )
	{
		int nFrames = Intervals.dimensionsAsIntArray( groundTruth )[ 3 ];

		double sumScores = 0.;
		double nGT = 0.;

		// run over all time indices, and compute metrics on each XY or XYZ hyperslice
		for ( int i = 0; i < nFrames; i++ )
		{
			final RandomAccessibleInterval< I > gtFrame = Views.hyperSlice( groundTruth, 3, i );
			final RandomAccessibleInterval< J > predFrame = Views.hyperSlice( prediction, 3, i );

			final Pair< Integer, Double > result = runSingle( gtFrame, predFrame );

			// ignore NaNs
			if ( Double.compare( result.getB(), Double.NaN ) != 0 )
			{
				nGT += result.getA();
				sumScores += result.getB();
			}
		}

		return nGT > 0 ? sumScores / nGT : Double.NaN;
	}

	protected < I extends IntegerType< I >, J extends IntegerType< J > > Pair< Integer, Double > runSingle(
			RandomAccessibleInterval< I > groundTruth,
			RandomAccessibleInterval< J > prediction )
	{
		// compute confusion matrix
		final ConfusionMatrix confusionMatrix = new ConfusionMatrix( groundTruth, prediction );
		int n = confusionMatrix.getNumberGroundTruthLabels();

		// compute cost matrix
		final double[][] costMatrix = computeCostMatrix( confusionMatrix );

		return new ValuePair<>( n, computeMetrics( confusionMatrix, costMatrix ) );
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
			return precision;
		}

		return 0.;
	}
}
