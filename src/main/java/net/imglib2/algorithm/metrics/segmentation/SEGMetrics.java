/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2023 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
 * John Bogovic, Albert Cardona, Barry DeZonia, Christian Dietz, Jan Funke,
 * Aivar Grislis, Jonathan Hale, Grant Harris, Stefan Helfrich, Mark Hiner,
 * Martin Horn, Steffen Jaensch, Lee Kamentsky, Larry Lindsey, Melissa Linkert,
 * Mark Longair, Brian Northan, Nick Perry, Curtis Rueden, Johannes Schindelin,
 * Jean-Yves Tinevez and Michael Zinsmaier.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package net.imglib2.algorithm.metrics.segmentation;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.Labelings;
import net.imglib2.type.numeric.IntegerType;

import net.imglib2.util.Intervals;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import net.imglib2.view.Views;

/**
 * The SEG metrics computes the IoU metrics between ground-truth labels
 * and the prediction labels that overlap with a percentage greater than 0.5. In the context of
 * instance segmentation, the labels are the integer pixel values. Pixels with value 0 are considered
 * background and are ignored during the metrics calculation. If the ground-truth image is only
 * background (no labels), then the metrics returns NaN.
 * <p>
 * The metrics expect images of dimensions XYZT, where Z and T can be of depth 1. The metrics score
 * is calculated for each ground-truth label in each XYZ volume (XY if dimension Z is of depth 1),
 * and averaged over the total number of ground-truth labels in the XYZT volume. If the Z dimension
 * has depth greater than 1, then the labels are considered 3D and pixels of equal values at different
 * depths are considered to be part of the same labeling.
 * <p>
 * Finally, if the image stack does not fit in memory, you can use {@link LazySEGMetrics} to compute
 * a running SEG score for which images can be added one at time.
 * <p>
 * Reference: Ulman, V., Maška, M., Magnusson, K. et al. An objective comparison of cell-tracking
 * algorithms. Nat Methods 14, 1141–1152 (2017).
 *
 * @author Joran Deschamps
 * @see <a href="https://github.com/CellTrackingChallenge/CTC-FijiPlugins">Original implementation by Martin Maška and Vladimír Ulman</a>
 */
public class SEGMetrics
{
	private final static int T_AXIS = 3;

	/**
	 * Compute a global SEG score between labels from a ground-truth and a predicted image. The
	 * method expects images of dimension XYZT. The score is computed for each ground-truth label over
	 * each XYZ volume (or XY if Z is of depth 1) and averaged over all ground-truth labels in XYZT. If
	 * both images are empty (only pixels with value 0), then the metrics score is NaN.
	 * <p>
	 * This method is not compatible with {@link ImgLabeling} with intersecting labels.
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
	public static < T, I extends IntegerType< I >, U, J extends IntegerType< J > > double computeMetrics(
			final ImgLabeling< T, I > groundTruth,
			final ImgLabeling< U, J > prediction
	)
	{
		if ( Labelings.hasIntersectingLabels( groundTruth ) || Labelings.hasIntersectingLabels( prediction ) )
			throw new UnsupportedOperationException( "ImgLabeling with intersecting labels are not supported." );

		return computeMetrics( groundTruth.getIndexImg(), prediction.getIndexImg() );
	}

	/**
	 * Compute a global SEG score between labels from a ground-truth and a predicted image. The
	 * method expects images of dimension XYZT. The score is computed over the XYZ volume (or XY if
	 * Z is of depth 1) and averaged over all ground-truth labels. If both images are empty (only
	 * pixels with value 0), then the metrics score is NaN.
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
	public static < I extends IntegerType< I >, J extends IntegerType< J > > double computeMetrics(
			RandomAccessibleInterval< I > groundTruth,
			RandomAccessibleInterval< J > prediction )
	{
		if ( !Intervals.equalDimensions( groundTruth, prediction ) )
			throw new IllegalArgumentException( "Image dimensions must match." );

		// check if it is a time-lapse
		boolean timeLapse = false;
		if ( groundTruth.numDimensions() > T_AXIS )
		{
			timeLapse = groundTruth.dimension( T_AXIS ) > 1;
		}

		if ( timeLapse )
		{
			return runAverageOverTime( groundTruth, prediction );
		}
		else
		{
			final Pair< Integer, Double > result = runSingle( groundTruth, prediction );
			return result.getA() > 0 ? result.getB() / result.getA() : Double.NaN;
		}
	}

	private static < I extends IntegerType< I >, J extends IntegerType< J > > double runAverageOverTime(
			RandomAccessibleInterval< I > groundTruth,
			RandomAccessibleInterval< J > prediction )
	{
		int nFrames = ( int ) groundTruth.dimension( T_AXIS );

		double sumScores = 0.;
		double nGT = 0.;

		// run over all time indices, and compute metrics on each XY or XYZ hyperslice
		for ( int i = 0; i < nFrames; i++ )
		{
			final RandomAccessibleInterval< I > gtFrame = Views.hyperSlice( groundTruth, T_AXIS, i );
			final RandomAccessibleInterval< J > predFrame = Views.hyperSlice( prediction, T_AXIS, i );

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

	protected static < I extends IntegerType< I >, J extends IntegerType< J > > Pair< Integer, Double > runSingle(
			RandomAccessibleInterval< I > groundTruth,
			RandomAccessibleInterval< J > prediction )
	{
		// compute confusion matrix
		final ConfusionMatrix<I, J> confusionMatrix = new ConfusionMatrix( groundTruth, prediction );
		int n = confusionMatrix.getNumberGroundTruthLabels();

		// compute cost matrix
		final double[][] costMatrix = computeCostMatrix( confusionMatrix );

		return new ValuePair<>( n, computeFinalScore( costMatrix ) );
	}

	protected static double[][] computeCostMatrix( ConfusionMatrix cM )
	{
		int M = cM.getNumberGroundTruthLabels();
		int N = cM.getNumberPredictionLabels();

		// empty cost matrix
		double[][] costMatrix = new double[ M ][ N ];

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

	protected static double getLocalIoUScore( ConfusionMatrix cM, int i, int j )
	{
		double intersection = cM.getIntersection( i, j );
		double gtSize = cM.getGroundTruthLabelSize( i );

		double overlap = intersection / gtSize;
		if ( overlap > 0.5 )
		{
			double predSize = cM.getPredictionLabelSize( j );

			return intersection / ( gtSize + predSize - intersection );
		}

		return 0.;
	}

	private static double computeFinalScore( double[][] costMatrix )
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
