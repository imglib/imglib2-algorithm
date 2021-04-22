package net.imglib2.algorithm.metrics.segmentation;

import java.util.Arrays;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import org.junit.Test;

import static net.imglib2.algorithm.metrics.segmentation.SegmentationMetricsTest.exampleIndexArray;
import static net.imglib2.algorithm.metrics.segmentation.SegmentationMetricsTest.exampleIndexArrayDims;
import static net.imglib2.algorithm.metrics.segmentation.SegmentationMetricsTest.exampleIntersectingLabels;
import static net.imglib2.algorithm.metrics.segmentation.SegmentationMetricsTest.exampleNonIntersectingLabels;
import static net.imglib2.algorithm.metrics.segmentation.SegmentationMetricsTest.getLabelingSet;
import static org.junit.Assert.assertEquals;

public class SEGMetricsTest
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

		new SEGMetrics().computeMetrics( labeling, labeling2 );
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

		new SEGMetrics().computeMetrics( labeling, labeling2 );
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

		new SEGMetrics().computeMetrics( labeling, labeling2 );
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
		final double score = new SEGMetrics().computeMetrics( img, img );

		// check results
		assertEquals( 1., score, 0.0001 );
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
		double score = new SEGMetrics().computeMetrics( empty, nonEmpty );
		assertEquals( Double.NaN, score, 0.0001 );

		//////////////////////////////////
		// Non-empty gt, empty prediction
		score = new SEGMetrics().computeMetrics( nonEmpty, empty );
		assertEquals( 0., score, 0.0001 );

		//////////////////////////////////
		// Empty gt, empty prediction
		score = new SEGMetrics().computeMetrics( empty, empty );
		assertEquals( Double.NaN, score, 0.0001 );
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

		double score = new SEGMetrics().computeMetrics( groundtruth, prediction );
		assertEquals( 0., score, 0.0001 );
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
		double iou1 = getSEGBetweenRectangles( gtRect1, predRect1 );
		double iou2 = getSEGBetweenRectangles( gtRect2, predRect2 );
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
		double iou1 = getSEGBetweenRectangles( gtRect1, predRect1, 2 ); // calculates IoU for a rectangular volume
		double iou2 = getSEGBetweenRectangles( gtRect2, predRect2, 2 );
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
		double iou1 = getSEGBetweenRectangles( gtRect1, predRect1 );
		double iou2 = getSEGBetweenRectangles( gtRect2, predRect2 );
		double iou3 = getSEGBetweenRectangles( gtRect3, predRect3 );
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
		double iou1 = getSEGBetweenRectangles( gtRect1, predRect1, 2 );
		double iou2 = getSEGBetweenRectangles( gtRect2, predRect2, 2 );
		double iou3 = getSEGBetweenRectangles( gtRect3, predRect3, 2 );
		double[] ious = { iou1, iou2, iou2, iou3 };

		// Check the metrics versus the expected values
		checkMetrics( groundtruth, prediction, ious );
	}

	private static void checkMetrics( final Img< IntType > groundtruth, final Img< IntType > prediction, final double[] ious )
	{
		// Compute expected values
		double nGT = ious.length;
		double sumIoU = Arrays.stream( ious ).sum();

		// Compute metrics
		double score = new SEGMetrics().computeMetrics( groundtruth, prediction );

		assertEquals( sumIoU / nGT, score, 0.0001 );
	}

	protected static double getSEGBetweenRectangles( int[] a, int[] b )
	{
		return getSEGBetweenRectangles( a[ 0 ], a[ 1 ], a[ 2 ], a[ 3 ], b[ 0 ], b[ 1 ], b[ 2 ], b[ 3 ], 0, 0 );
	}

	protected static double getSEGBetweenRectangles( int[] a, int[] b, int depth )
	{
		return getSEGBetweenRectangles( a[ 0 ], a[ 1 ], a[ 2 ], a[ 3 ], b[ 0 ], b[ 1 ], b[ 2 ], b[ 3 ], 0, depth );
	}

	public static double getSEGBetweenRectangles( int a_x_min, int a_y_min, int a_x_max, int a_y_max,
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
			double a_volume = ( double ) ( a_x_max - a_x_min + 1 ) * ( a_y_max - a_y_min + 1 ) * depth;
			double b_volume = ( double ) ( b_x_max - b_x_min + 1 ) * ( b_y_max - b_y_min + 1 ) * depth;

			if ( intersection / a_volume > 0.5 )
			{
				return intersection / ( a_volume + b_volume - intersection );
			}
		}
		return 0;
	}
}
