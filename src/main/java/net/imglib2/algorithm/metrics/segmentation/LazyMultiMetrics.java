package net.imglib2.algorithm.metrics.segmentation;

import java.util.Arrays;
import java.util.HashMap;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.IntegerType;

import static net.imglib2.algorithm.metrics.segmentation.SegmentationHelper.hasIntersectingLabels;

public class LazyMultiMetrics
{
	final MultiMetrics.MetricsSummary summary;

	public LazyMultiMetrics(){
		summary = new MultiMetrics.MetricsSummary();
	}

	public < T, I extends IntegerType< I >, U, J extends IntegerType< J > > void addPoint(
			final ImgLabeling< T, I > groundTruth,
			final ImgLabeling< U, J > prediction,
			double threshold
	)
	{
		if ( hasIntersectingLabels( groundTruth ) || hasIntersectingLabels( prediction ) )
			throw new UnsupportedOperationException( "ImgLabeling with intersecting labels are not supported." );

		addPoint( groundTruth.getIndexImg(), prediction.getIndexImg(), threshold );
	}

	public < I extends IntegerType< I >, J extends IntegerType< J > > void addPoint(
			RandomAccessibleInterval< I > groundTruth,
			RandomAccessibleInterval< J > prediction,
			double threshold
	){

		if ( !Arrays.equals( groundTruth.dimensionsAsLongArray(), prediction.dimensionsAsLongArray() ) )
			throw new IllegalArgumentException( "Image dimensions must match." );

		// compute SEG between the two images
		final MultiMetrics.MetricsSummary result = new MultiMetrics().runSingle( groundTruth, prediction, threshold );

		// add results
		summary.addPoint( result );
	}

	public HashMap< MultiMetrics.Metrics, Double> computeScore(){
		return summary.getScores();
	}
}
