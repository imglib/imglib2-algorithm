package net.imglib2.algorithm.metrics.segmentation;

import java.util.HashMap;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import org.junit.Test;

import java.util.Map;

import static net.imglib2.algorithm.metrics.segmentation.MultiMetrics.Metrics;
import static net.imglib2.algorithm.metrics.segmentation.SegmentationMetricsTest.exampleIndexArray;
import static net.imglib2.algorithm.metrics.segmentation.SegmentationMetricsTest.exampleIndexArrayDims;
import static net.imglib2.algorithm.metrics.segmentation.SegmentationMetricsTest.exampleIntersectingLabels;
import static net.imglib2.algorithm.metrics.segmentation.SegmentationMetricsTest.exampleNonIntersectingLabels;
import static net.imglib2.algorithm.metrics.segmentation.SegmentationMetricsTest.getLabelingSet;
import static org.junit.Assert.assertEquals;

public class MultiMetricsTest
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

		new MultiMetrics().computeMetrics( labeling, labeling2, 0.5 );
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

		new MultiMetrics().computeMetrics( labeling, labeling2, 0.5 );
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

		new MultiMetrics().computeMetrics( labeling, labeling2, 0.5 );
	}

	/**
	 * Test metrics values when comparing an image against itself.
	 */
	@Test
	public void testIdentity()
	{
		long[] dims = { 64, 64 };
		final Img< IntType > img = ArrayImgs.ints( dims );

		// paint
		SegmentationMetricsHelper.paintRectangle( img, 12, 28, 42, 56, 9 );
		SegmentationMetricsHelper.paintRectangle( img, 43, 9, 52, 18, 12 );

		// default is the average precision
		final HashMap< Metrics, Double > metrics = new MultiMetrics().computeMetrics( img, img, 0.5 );

		// check results
		assertEquals( 2., metrics.get( Metrics.TP ), 0.0001 );
		assertEquals( 0., metrics.get( Metrics.FP ), 0.0001 );
		assertEquals( 0., metrics.get( Metrics.FN ), 0.0001 );
		assertEquals( 1., metrics.get( Metrics.ACCURACY ), 0.0001 );
		assertEquals( 1., metrics.get( Metrics.MEAN_MATCHED_IOU ), 0.0001 );
		assertEquals( 1., metrics.get( Metrics.MEAN_TRUE_IOU ), 0.0001 );
		assertEquals( 1., metrics.get( Metrics.PRECISION ), 0.0001 );
		assertEquals( 1., metrics.get( Metrics.RECALL ), 0.0001 );
		assertEquals( 1., metrics.get( Metrics.F1 ), 0.0001 );
	}

	/**
	 * Test metrics values when either or both ground-truth and prediction image are empty.
	 */
	@Test
	public void testEmpty()
	{
		long[] dims = { 64, 64 };
		final Img< IntType > nonEmpty = ArrayImgs.ints( dims );
		final Img< IntType > empty = ArrayImgs.ints( dims );

		// paint
		SegmentationMetricsHelper.paintRectangle( nonEmpty, 12, 28, 42, 56, 9 );

		//////////////////////////////////
		// Empty gt, non-empty prediction
		Map< Metrics, Double > metrics = new MultiMetrics().computeMetrics( empty, nonEmpty, 0.5 );

		assertEquals( 0., metrics.get( Metrics.TP ), 0.0001 );
		assertEquals( 1., metrics.get( Metrics.FP ), 0.0001 );
		assertEquals( 0., metrics.get( Metrics.FN ), 0.0001 );

		// tp / (tp + fp + fn)
		assertEquals( 0., metrics.get( Metrics.ACCURACY ), 0.0001 );

		// sumIoU / tp
		assertEquals( Double.NaN, metrics.get( Metrics.MEAN_MATCHED_IOU ), 0.0001 );

		// sumIoU / (nGT) = sumIoU / (tp + fn)
		assertEquals( Double.NaN, metrics.get( Metrics.MEAN_TRUE_IOU ), 0.0001 );

		// tp / (tp + fp)
		assertEquals( 0., metrics.get( Metrics.PRECISION ), 0.0001 );

		// tp / (tp + fn)
		assertEquals( Double.NaN, metrics.get( Metrics.RECALL ), 0.0001 );

		// 2*p*r / (p+r)
		assertEquals( Double.NaN, metrics.get( Metrics.F1 ), 0.0001 );

		//////////////////////////////////
		// Non-empty gt, empty prediction
		metrics = new MultiMetrics().computeMetrics( nonEmpty, empty, 0.5 );

		assertEquals( 0., metrics.get( Metrics.TP ), 0.0001 );
		assertEquals( 0., metrics.get( Metrics.FP ), 0.0001 );
		assertEquals( 1., metrics.get( Metrics.FN ), 0.0001 );

		// tp / (tp + fp + fn)
		assertEquals( 0., metrics.get( Metrics.ACCURACY ), 0.0001 );

		// sumIoU / tp
		assertEquals( Double.NaN, metrics.get( Metrics.MEAN_MATCHED_IOU ), 0.0001 );

		// sumIoU / (nGT) = sumIoU / (tp + fn)
		assertEquals( 0., metrics.get( Metrics.MEAN_TRUE_IOU ), 0.0001 );

		// tp / (tp + fp)
		assertEquals( Double.NaN, metrics.get( Metrics.PRECISION ), 0.0001 );

		// tp / (tp + fn)
		assertEquals( 0., metrics.get( Metrics.RECALL ), 0.0001 );

		// 2*p*r / (p+r)
		assertEquals( Double.NaN, metrics.get( Metrics.F1 ), 0.0001 );

		//////////////////////////////////
		// Empty gt, empty prediction
		metrics = new MultiMetrics().computeMetrics( empty, empty, 0.5 );

		assertEquals( 0., metrics.get( Metrics.TP ), 0.0001 );
		assertEquals( 0., metrics.get( Metrics.FP ), 0.0001 );
		assertEquals( 0., metrics.get( Metrics.FN ), 0.0001 );

		// tp / (tp + fp + fn)
		assertEquals( Double.NaN, metrics.get( Metrics.ACCURACY ), 0.0001 );

		// sumIoU / tp
		assertEquals( Double.NaN, metrics.get( Metrics.MEAN_MATCHED_IOU ), 0.0001 );

		// sumIoU / (nGT) = sumIoU / (tp + fn)
		assertEquals( Double.NaN, metrics.get( Metrics.MEAN_TRUE_IOU ), 0.0001 );

		// tp / (tp + fp)
		assertEquals( Double.NaN, metrics.get( Metrics.PRECISION ), 0.0001 );

		// tp / (tp + fn)
		assertEquals( Double.NaN, metrics.get( Metrics.RECALL ), 0.0001 );

		// 2*p*r / (p+r)
		assertEquals( Double.NaN, metrics.get( Metrics.F1 ), 0.0001 );
	}

	/**
	 * Test metrics values when the labels are not overlapping.
	 */
	@Test
	public void testNonOverlapping()
	{
		long[] dims = { 64, 64 };
		final Img< IntType > groundtruth = ArrayImgs.ints( dims );
		final Img< IntType > prediction = ArrayImgs.ints( dims );

		// paint
		SegmentationMetricsHelper.paintRectangle( groundtruth, 12, 5, 25, 13, 9 );
		SegmentationMetricsHelper.paintRectangle( prediction, 28, 15, 42, 32, 12 );

		Map< Metrics, Double > metrics = new MultiMetrics().computeMetrics( groundtruth, prediction, 0.5 );

		assertEquals( 0., metrics.get( Metrics.TP ), 0.0001 );
		assertEquals( 1., metrics.get( Metrics.FP ), 0.0001 );
		assertEquals( 1., metrics.get( Metrics.FN ), 0.0001 );

		// tp / (tp + fp + fn)
		assertEquals( 0., metrics.get( Metrics.ACCURACY ), 0.0001 );

		// sumIoU / tp
		assertEquals( Double.NaN, metrics.get( Metrics.MEAN_MATCHED_IOU ), 0.0001 );

		// sumIoU / (nGT) = sumIoU / (tp + fn)
		assertEquals( 0., metrics.get( Metrics.MEAN_TRUE_IOU ), 0.0001 );

		// tp / (tp + fp)
		assertEquals( 0., metrics.get( Metrics.PRECISION ), 0.0001 );

		// tp / (tp + fn)
		assertEquals( 0., metrics.get( Metrics.RECALL ), 0.0001 );

		// 2*p*r / (p+r)
		assertEquals( Double.NaN, metrics.get( Metrics.F1 ), 0.0001 );
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
		checkMetrics( groundtruth, prediction, ious );
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
		checkMetrics( groundtruth, prediction, ious );
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
		checkMetrics( groundtruth, prediction, ious );
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
		checkMetrics( groundtruth, prediction, ious );
	}

	@Test
	public void testLocalIoUOverlapping()
	{
		long[] dims = { 15, 15 };
		final Img< IntType > gt = ArrayImgs.ints( dims );
		final Img< IntType > pred = ArrayImgs.ints( dims );

		int[] gtRect = { 2, 4, 8, 6 };
		int[] predRect = { 3, 4, 11, 5 };

		// paint
		SegmentationMetricsHelper.paintRectangle( gt, gtRect, 9 );
		SegmentationMetricsHelper.paintRectangle( pred, predRect, 5 );

		// confusion metrics
		ConfusionMatrix< IntType, IntType > cm = new ConfusionMatrix<>( gt, pred );

		// Metrics
		MultiMetrics metrics = new MultiMetrics();

		// values
		double localIoU = getIoUBetweenRectangles( gtRect, predRect );

		// local iou
		for ( double t = 0.; t < 1.; t += 0.05 )
		{
			double iou = localIoU >= t ? localIoU : 0;
			assertEquals( iou, metrics.getLocalIoUScore( cm, 0, 0, t ), 0.00001 );
		}
	}

	@Test
	public void testLocalIoUDisjoint()
	{
		long[] dims = { 15, 15 };
		final Img< IntType > gt = ArrayImgs.ints( dims );
		final Img< IntType > pred = ArrayImgs.ints( dims );

		int[] gtRect = { 2, 4, 8, 6 };
		int[] predRect = { 9, 7, 14, 9 };

		// paint
		SegmentationMetricsHelper.paintRectangle( gt, gtRect, 9 );
		SegmentationMetricsHelper.paintRectangle( pred, predRect, 5 );

		// confusion metrics
		ConfusionMatrix< IntType, IntType > cm = new ConfusionMatrix<>( gt, pred );

		// Metrics
		MultiMetrics metrics = new MultiMetrics();

		assertEquals( 0., metrics.getLocalIoUScore( cm, 0, 0, 0. ), 0.00001 );
	}

	@Test
	public void testLocalIoUEmptyGT()
	{
		long[] dims = { 15, 15 };
		final Img< IntType > gt = ArrayImgs.ints( dims );
		final Img< IntType > pred = ArrayImgs.ints( dims );

		int[] predRect = { 9, 7, 14, 9 };

		// paint
		SegmentationMetricsHelper.paintRectangle( pred, predRect, 5 );

		// confusion metrics
		ConfusionMatrix< IntType, IntType > cm = new ConfusionMatrix<>( gt, pred );

		// Metrics
		MultiMetrics metrics = new MultiMetrics();

		assertEquals( 0, metrics.getLocalIoUScore( cm, 0, 0, 0. ), 0.00001 );
	}

	@Test
	public void testLocalIoUEmptyPrediction()
	{
		long[] dims = { 15, 15 };
		final Img< IntType > gt = ArrayImgs.ints( dims );
		final Img< IntType > pred = ArrayImgs.ints( dims );

		int[] gtRect = { 2, 4, 8, 6 };

		// paint
		SegmentationMetricsHelper.paintRectangle( gt, gtRect, 9 );

		// confusion metrics
		ConfusionMatrix< IntType, IntType > cm = new ConfusionMatrix<>( gt, pred );

		// Metrics
		MultiMetrics metrics = new MultiMetrics();

		assertEquals( 0, metrics.getLocalIoUScore( cm, 0, 0, 0. ), 0.00001 );
	}

	@Test
	public void testLocalIoUWithMoreGTLabels()
	{
		long[] dims = { 15, 15 };
		final Img< IntType > gt = ArrayImgs.ints( dims );
		final Img< IntType > pred = ArrayImgs.ints( dims );

		int[] gtRect1 = { 12, 7, 14, 11 };
		int[] gtRect2 = { 2, 4, 8, 6 };

		int[] predRect = { 3, 4, 11, 5 };

		int gtLabel1 = 9;
		int gtLabel2 = 5;
		int predLabel = 5;

		// paint
		SegmentationMetricsHelper.paintRectangle( gt, gtRect1, gtLabel1 );
		SegmentationMetricsHelper.paintRectangle( gt, gtRect2, gtLabel2 );
		SegmentationMetricsHelper.paintRectangle( pred, predRect, predLabel );

		// confusion metrics
		ConfusionMatrix< IntType, IntType > cm = new ConfusionMatrix<>( gt, pred );

		// Metrics
		MultiMetrics metrics = new MultiMetrics();

		// values
		double localIoU1 = getIoUBetweenRectangles( gtRect1, predRect );
		double localIoU2 = getIoUBetweenRectangles( gtRect2, predRect );

		// gtLabel2 encountered first, gtLabel1 second
		assertEquals( localIoU1, metrics.getLocalIoUScore( cm, 1, 0, 0. ), 0.00001 );
		assertEquals( localIoU2, metrics.getLocalIoUScore( cm, 0, 0, 0. ), 0.00001 );
	}

	@Test
	public void testLocalIoUWithMorePredLabels()
	{
		long[] dims = { 15, 15 };
		final Img< IntType > gt = ArrayImgs.ints( dims );
		final Img< IntType > pred = ArrayImgs.ints( dims );

		int[] gtRect = { 2, 4, 8, 6 };

		int[] predRect1 = { 12, 7, 14, 11 };
		int[] predRect2 = { 3, 4, 11, 5 };

		int gtLabel = 5;
		int predLabel1 = 9;
		int predLabel2 = 5;

		// paint
		SegmentationMetricsHelper.paintRectangle( gt, gtRect, gtLabel );
		SegmentationMetricsHelper.paintRectangle( pred, predRect1, predLabel1 );
		SegmentationMetricsHelper.paintRectangle( pred, predRect2, predLabel2 );

		// confusion metrics
		ConfusionMatrix< IntType, IntType > cm = new ConfusionMatrix<>( gt, pred );

		// Metrics
		MultiMetrics metrics = new MultiMetrics();

		// values
		double localIoU1 = getIoUBetweenRectangles( gtRect, predRect1 );
		double localIoU2 = getIoUBetweenRectangles( gtRect, predRect2 );

		// predLabel2 encountered first, predLabel1 second
		assertEquals( localIoU1, metrics.getLocalIoUScore( cm, 0, 1, 0. ), 0.00001 );
		assertEquals( localIoU2, metrics.getLocalIoUScore( cm, 0, 0, 0. ), 0.00001 );
	}

	/**
	 * Test metrics values by computing an expected value from an IoU array and comparing them to the
	 * result of the metrics computation.
	 *
	 * @param groundtruth
	 * @param prediction
	 * @param ious
	 * 		Array of IoUs between corresponding pairs of GT-prediction labels
	 */
	private static void checkMetrics( final Img< IntType > groundtruth, final Img< IntType > prediction, final double[] ious )
	{
		MultiMetrics multiMetrics = new MultiMetrics();
		// For each threshold value
		for ( double t = 0.1; t < 1.; t += 0.05 )
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
			Map< Metrics, Double > metrics = multiMetrics.computeMetrics( groundtruth, prediction, t );

			// Check values
			assertEquals( tp, metrics.get( Metrics.TP ), 0.0001 );
			assertEquals( fp, metrics.get( Metrics.FP ), 0.0001 );
			assertEquals( fn, metrics.get( Metrics.FN ), 0.0001 );
			assertEquals( accur, metrics.get( Metrics.ACCURACY ), 0.0001 );
			assertEquals( meanMatched, metrics.get( Metrics.MEAN_MATCHED_IOU ), 0.0001 );
			assertEquals( meanTrue, metrics.get( Metrics.MEAN_TRUE_IOU ), 0.0001 );
			assertEquals( prec, metrics.get( Metrics.PRECISION ), 0.0001 );
			assertEquals( recall, metrics.get( Metrics.RECALL ), 0.0001 );
			assertEquals( f1, metrics.get( Metrics.F1 ), 0.0001 );
		}
	}

	protected static double getIoUBetweenRectangles( int[] a, int[] b )
	{
		return getIoUBetweenRectangles( a[ 0 ], a[ 1 ], a[ 2 ], a[ 3 ], b[ 0 ], b[ 1 ], b[ 2 ], b[ 3 ], 0, 0 );
	}

	protected static double getIoUBetweenRectangles( int[] a, int[] b, int depth )
	{
		return getIoUBetweenRectangles( a[ 0 ], a[ 1 ], a[ 2 ], a[ 3 ], b[ 0 ], b[ 1 ], b[ 2 ], b[ 3 ], 0, depth );
	}

	protected static double getIoUBetweenRectangles( int a_x_min, int a_y_min, int a_x_max, int a_y_max,
			int b_x_min, int b_y_min, int b_x_max, int b_y_max, int c_min, int c_max )
	{
		int left = Math.max( a_x_min, b_x_min );
		int right = Math.min( a_x_max, b_x_max );
		int bottom = Math.max( a_y_min, b_y_min );
		int top = Math.min( a_y_max, b_y_max );

		int depth = c_max - c_min + 1;

		if ( left < right && bottom < top )
		{
			double intersection = ( double ) ( right - left + 1 ) * ( top - bottom + 1 ) * depth;
			double a_volume = ( a_x_max - a_x_min + 1 ) * ( a_y_max - a_y_min + 1 ) * depth;
			double b_volume = ( b_x_max - b_x_min + 1 ) * ( b_y_max - b_y_min + 1 ) * depth;

			return intersection / ( a_volume + b_volume - intersection );
		}
		return 0.;
	}
}
