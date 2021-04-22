package net.imglib2.algorithm.metrics.segmentation;

import java.util.HashMap;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.view.Views;
import org.junit.Test;

import static net.imglib2.algorithm.metrics.segmentation.MultiMetrics.Metrics;
import static net.imglib2.algorithm.metrics.segmentation.MultiMetricsTest.getIoUBetweenRectangles;
import static net.imglib2.algorithm.metrics.segmentation.SegmentationMetricsTest.exampleIndexArray;
import static net.imglib2.algorithm.metrics.segmentation.SegmentationMetricsTest.exampleIndexArrayDims;
import static net.imglib2.algorithm.metrics.segmentation.SegmentationMetricsTest.exampleIntersectingLabels;
import static net.imglib2.algorithm.metrics.segmentation.SegmentationMetricsTest.exampleNonIntersectingLabels;
import static net.imglib2.algorithm.metrics.segmentation.SegmentationMetricsTest.getLabelingSet;
import static org.junit.Assert.assertEquals;

public class LaszyMultiMetricsTest
{
	/**
	 * Test that passing a ground-truth image with intersecting labels throws an exception.
	 */
	@Test( expected = UnsupportedOperationException.class )
	public void testException()
	{
		final Img< IntType > img = ArrayImgs.ints( exampleIndexArray, exampleIndexArrayDims );
		final ImgLabeling< String, IntType > labeling = ImgLabeling.fromImageAndLabelSets( img, getLabelingSet( exampleIntersectingLabels ) );
		final ImgLabeling< String, IntType > labeling2 = ImgLabeling.fromImageAndLabelSets( img, getLabelingSet( exampleNonIntersectingLabels ) );

		new LazyMultiMetrics( 0.5 ).addTimePoint( labeling, labeling2 );
	}

	/**
	 * Test that passing a predicted image with intersecting labels throws an exception.
	 */
	@Test( expected = UnsupportedOperationException.class )
	public void testException2()
	{
		final Img< IntType > img = ArrayImgs.ints( exampleIndexArray, exampleIndexArrayDims );
		final ImgLabeling< String, IntType > labeling = ImgLabeling.fromImageAndLabelSets( img, getLabelingSet( exampleNonIntersectingLabels ) );
		final ImgLabeling< String, IntType > labeling2 = ImgLabeling.fromImageAndLabelSets( img, getLabelingSet( exampleIntersectingLabels ) );

		new LazyMultiMetrics( 0.5 ).addTimePoint( labeling, labeling2 );
	}

	/**
	 * Test that passing images without intersecting labels throws no exception.
	 */
	@Test
	public void testNoException()
	{
		final Img< IntType > img = ArrayImgs.ints( exampleIndexArray, exampleIndexArrayDims );
		final ImgLabeling< String, IntType > labeling = ImgLabeling.fromImageAndLabelSets( img, getLabelingSet( exampleNonIntersectingLabels ) );
		final ImgLabeling< String, IntType > labeling2 = ImgLabeling.fromImageAndLabelSets( img, getLabelingSet( exampleNonIntersectingLabels ) );

		new LazyMultiMetrics( 0.5 ).addTimePoint( labeling, labeling2 );
	}

	/**
	 * Test metrics values when two labels are overlapping, in particular depending on
	 * the threshold. This test uses XY images only (single frame, slice).
	 */
	@Test
	public void testOverlapping()
	{
		long[] dims = { 32, 32 };
		final Img< IntType > groundtruth = ArrayImgs.ints( dims );
		final Img< IntType > prediction = ArrayImgs.ints( dims );

		int[] gtRect1 = { 2, 2, 11, 11 };
		int[] predRect1 = { 6, 6, 15, 15 };

		int[] gtRect2 = { 15, 15, 20, 20 };
		int[] predRect2 = { 15, 16, 21, 21 };

		// Paint overlapping labels
		SegmentationMetricsHelper.paintRectangle( groundtruth, gtRect1, 9 );
		SegmentationMetricsHelper.paintRectangle( prediction, predRect1, 5 );

		SegmentationMetricsHelper.paintRectangle( groundtruth, gtRect2, 2 );
		SegmentationMetricsHelper.paintRectangle( prediction, predRect2, 8 );

		// Calculate IoUs
		double iou1 = getIoUBetweenRectangles( gtRect1, predRect1 );
		double iou2 = getIoUBetweenRectangles( gtRect2, predRect2 );
		double[] ious = { iou1, iou2 };

		// Check the metrics versus the expected values
		for ( double t = 0.1; t < 1.; t += 0.05 )
		{
			LazyMultiMetrics lazyMetrics = new LazyMultiMetrics( t );
			lazyMetrics.addTimePoint( groundtruth, prediction );

			final HashMap< Metrics, Double > metrics = getMetrics( ious, t );
			final HashMap< Metrics, Double > lazyResults = lazyMetrics.computeScore();

			assertEquals( metrics, lazyResults );
		}
	}

	/**
	 * Test metrics values for a Z stack (XYZ), where labels are considered 3D, with respect to the threshold.
	 * In this case, the 3D labels are split between two slices with an empty slice in between.
	 */
	@Test
	public void testXYZ()
	{
		long[] dims = { 32, 32, 3 };
		final Img< IntType > groundtruth = ArrayImgs.ints( dims );
		final Img< IntType > prediction = ArrayImgs.ints( dims );

		int[] gtRect1 = { 2, 2, 11, 11 };
		int[] predRect1 = { 6, 6, 15, 15 };

		int[] gtRect2 = { 15, 15, 20, 20 };
		int[] predRect2 = { 15, 16, 21, 20 };

		// Paint rectangles on the first slice
		SegmentationMetricsHelper.paintRectangle( groundtruth, gtRect1, 0, 9 );
		SegmentationMetricsHelper.paintRectangle( prediction, predRect1, 0, 5 );
		SegmentationMetricsHelper.paintRectangle( groundtruth, gtRect2, 0, 2 );
		SegmentationMetricsHelper.paintRectangle( prediction, predRect2, 0, 8 );

		// Paint rectangle with the same label on the last slice
		SegmentationMetricsHelper.paintRectangle( groundtruth, gtRect1, 2, 9 );
		SegmentationMetricsHelper.paintRectangle( prediction, predRect1, 2, 5 );
		SegmentationMetricsHelper.paintRectangle( groundtruth, gtRect2, 2, 2 );
		SegmentationMetricsHelper.paintRectangle( prediction, predRect2, 2, 8 );

		// Rectangles with same label on all slices belong to the same 3D label
		double iou1 = getIoUBetweenRectangles( gtRect1, predRect1, 2 ); // calculates IoU for a rectangular volume
		double iou2 = getIoUBetweenRectangles( gtRect2, predRect2, 2 );
		double[] ious = { iou1, iou2 };

		// Check the metrics versus the expected values
		for ( double t = 0.1; t < 1.; t += 0.05 )
		{
			LazyMultiMetrics lazyMetrics = new LazyMultiMetrics( t );
			lazyMetrics.addTimePoint( groundtruth, prediction );

			final HashMap< Metrics, Double > metrics = getMetrics( ious, t );
			final HashMap< Metrics, Double > lazyResults = lazyMetrics.computeScore();

			assertEquals( metrics, lazyResults );
		}
	}

	/**
	 * Test metrics values for a movie (XYT, where T is nonetheless the 4th dimension), where labels
	 * are considered 2D, with respect to the threshold. In this case, the 2D labels are split between
	 * two time frames with an empty frame in between. The labels on each time frames have the same
	 * value in order to check that they are not aggregated.
	 */
	@Test
	public void testXYT()
	{
		long[] dims = { 32, 32, 1, 3 };
		final Img< IntType > groundtruth = ArrayImgs.ints( dims );
		final Img< IntType > prediction = ArrayImgs.ints( dims );

		int[] gtRect1 = { 2, 2, 11, 11 };
		int[] predRect1 = { 6, 6, 13, 14 };

		int[] gtRect2 = { 15, 15, 20, 20 };
		int[] predRect2 = { 15, 15, 22, 22 };

		int[] gtRect3 = { 4, 5, 11, 14 };
		int[] predRect3 = { 4, 5, 11, 15 };

		// paint rectangles in the first frame
		SegmentationMetricsHelper.paintRectangle( groundtruth, gtRect1, 0, 0, 9 );
		SegmentationMetricsHelper.paintRectangle( prediction, predRect1, 0, 0, 5 );
		SegmentationMetricsHelper.paintRectangle( groundtruth, gtRect2, 0, 0, 2 );
		SegmentationMetricsHelper.paintRectangle( prediction, predRect2, 0, 0, 6 );

		// paint other rectangles in the last frame
		// We use same labels to test if they are not considered 3D
		SegmentationMetricsHelper.paintRectangle( groundtruth, gtRect2, 0, 2, 9 );
		SegmentationMetricsHelper.paintRectangle( prediction, predRect2, 0, 2, 5 );
		SegmentationMetricsHelper.paintRectangle( groundtruth, gtRect3, 0, 2, 2 );
		SegmentationMetricsHelper.paintRectangle( prediction, predRect3, 0, 2, 6 );

		// Calculate the IoUs
		double iou1 = getIoUBetweenRectangles( gtRect1, predRect1 );
		double iou2 = getIoUBetweenRectangles( gtRect2, predRect2 );
		double iou3 = getIoUBetweenRectangles( gtRect3, predRect3 );
		double[] ious = { iou1, iou2, iou2, iou3 };

		// Check the metrics versus the expected values
		for ( double t = 0.1; t < 1.; t += 0.05 )
		{
			LazyMultiMetrics lazyMetrics = new LazyMultiMetrics( t );
			for ( int i = 0; i < 3; i++ )
			{
				final RandomAccessibleInterval< IntType > gt = Views.hyperSlice( groundtruth, 3, i );
				final RandomAccessibleInterval< IntType > p = Views.hyperSlice( prediction, 3, i );

				lazyMetrics.addTimePoint( gt, p );
			}

			final HashMap< Metrics, Double > metrics = getMetrics( ious, t );
			final HashMap< Metrics, Double > lazyResults = lazyMetrics.computeScore();

			assertEquals( metrics, lazyResults );
		}
	}

	/**
	 * Test metrics values for a 4D movie (XYZT), where labels are considered 2D, with respect to the
	 * threshold. Here, we paint 3D boxes in the firs time frame with an empty slice in the middle of
	 * the boxes. On the last frame, we do the same with different 3D boxes. The boxes in the first
	 * time frame have the same label as in the last time frame, to make sure that they are not
	 * aggregated. We put an empty time frame in between.
	 */
	@Test
	public void testXYZT()
	{
		long[] dims = { 32, 32, 3, 3 };
		final Img< IntType > groundtruth = ArrayImgs.ints( dims );
		final Img< IntType > prediction = ArrayImgs.ints( dims );

		int[] gtRect1 = { 2, 2, 11, 11 };
		int[] predRect1 = { 6, 6, 13, 14 };

		int[] gtRect2 = { 15, 15, 20, 20 };
		int[] predRect2 = { 15, 15, 22, 22 };

		int[] gtRect3 = { 4, 5, 11, 14 };
		int[] predRect3 = { 4, 5, 11, 15 };

		// paint 3D boxes on first frame
		SegmentationMetricsHelper.paintRectangle( groundtruth, gtRect1, 0, 0, 9 );
		SegmentationMetricsHelper.paintRectangle( prediction, predRect1, 0, 0, 5 );
		SegmentationMetricsHelper.paintRectangle( groundtruth, gtRect2, 0, 0, 2 );
		SegmentationMetricsHelper.paintRectangle( prediction, predRect2, 0, 0, 6 );
		SegmentationMetricsHelper.paintRectangle( groundtruth, gtRect1, 2, 0, 9 );
		SegmentationMetricsHelper.paintRectangle( prediction, predRect1, 2, 0, 5 );
		SegmentationMetricsHelper.paintRectangle( groundtruth, gtRect2, 2, 0, 2 );
		SegmentationMetricsHelper.paintRectangle( prediction, predRect2, 2, 0, 6 );

		// paint last frame (we use same labels to test if they get confused)
		SegmentationMetricsHelper.paintRectangle( groundtruth, gtRect2, 0, 2, 9 );
		SegmentationMetricsHelper.paintRectangle( prediction, predRect2, 0, 2, 5 );
		SegmentationMetricsHelper.paintRectangle( groundtruth, gtRect3, 0, 2, 2 );
		SegmentationMetricsHelper.paintRectangle( prediction, predRect3, 0, 2, 6 );
		SegmentationMetricsHelper.paintRectangle( groundtruth, gtRect2, 2, 2, 9 );
		SegmentationMetricsHelper.paintRectangle( prediction, predRect2, 2, 2, 5 );
		SegmentationMetricsHelper.paintRectangle( groundtruth, gtRect3, 2, 2, 2 );
		SegmentationMetricsHelper.paintRectangle( prediction, predRect3, 2, 2, 6 );

		// Calculate IoUs
		double iou1 = getIoUBetweenRectangles( gtRect1, predRect1, 2 );
		double iou2 = getIoUBetweenRectangles( gtRect2, predRect2, 2 );
		double iou3 = getIoUBetweenRectangles( gtRect3, predRect3, 2 );
		double[] ious = { iou1, iou2, iou2, iou3 };

		// Check the metrics versus the expected values
		for ( double t = 0.1; t < 1.; t += 0.05 )
		{
			LazyMultiMetrics lazyMetrics = new LazyMultiMetrics( t );
			for ( int i = 0; i < 3; i++ )
			{
				final RandomAccessibleInterval< IntType > gt = Views.hyperSlice( groundtruth, 3, i );
				final RandomAccessibleInterval< IntType > p = Views.hyperSlice( prediction, 3, i );

				lazyMetrics.addTimePoint( gt, p );
			}

			final HashMap< Metrics, Double > metrics = getMetrics( ious, t );
			final HashMap< Metrics, Double > lazyResults = lazyMetrics.computeScore();

			assertEquals( metrics, lazyResults );
		}
	}

	private static HashMap< Metrics, Double > getMetrics( final double[] ious, double t )
	{
		// Compute expected values
		double tp = 0, sumIoU = 0.;
		double fp, fn, f1, accur, prec, recall, meanTrue, meanMatched;
		for ( double iou : ious )
		{
			if ( iou >= t )
			{
				tp++;
				sumIoU += iou;
			}
		}
		fp = ious.length - tp;
		fn = ious.length - tp;

		meanTrue = ( tp + fn ) > 0 ? sumIoU / ( tp + fn ) : Double.NaN;
		meanMatched = tp > 0 ? sumIoU / tp : Double.NaN;
		accur = ( tp + fp + fn ) > 0 ? tp / ( tp + fp + fn ) : Double.NaN;
		prec = ( tp + fp ) > 0 ? tp / ( tp + fp ) : Double.NaN;
		recall = ( tp + fn ) > 0 ? tp / ( tp + fn ) : Double.NaN;
		f1 = ( prec + recall ) > 0 ? 2 * prec * recall / ( prec + recall ) : Double.NaN;

		// Compute metrics
		HashMap< Metrics, Double > metrics = new HashMap<>();
		metrics.put( Metrics.TP, tp );
		metrics.put( Metrics.FP, fp );
		metrics.put( Metrics.FN, fn );
		metrics.put( Metrics.MEAN_MATCHED_IOU, meanMatched );
		metrics.put( Metrics.MEAN_TRUE_IOU, meanTrue );
		metrics.put( Metrics.ACCURACY, accur );
		metrics.put( Metrics.PRECISION, prec );
		metrics.put( Metrics.RECALL, recall );
		metrics.put( Metrics.F1, f1 );

		return metrics;
	}
}
