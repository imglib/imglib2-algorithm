package net.imglib2.algorithm.metrics.segmentation;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.IntType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConfusionMatrixTest
{

	@Test
	public void testSize()
	{
		long[] dims = { 15, 15 };
		final Img< IntType > gt = ArrayImgs.ints( dims );
		final Img< IntType > pred = ArrayImgs.ints( dims );

		int min_gt = 2;
		int max_gt = 9;
		int min_pred = 7;
		int max_pred = 11;

		// paint
		SegmentationMetricsHelper.paintRectangle( gt, min_gt, min_gt, max_gt, max_gt, 9 );
		SegmentationMetricsHelper.paintRectangle( pred, min_pred, min_pred, max_pred, max_pred, 5 );

		// confusion metrics
		ConfusionMatrix< IntType, IntType > cm = new ConfusionMatrix<>( gt, pred );

		// total sizes
		int gt_size = SegmentationMetricsHelper.getRectangleSize( min_gt, min_gt, max_gt, max_gt );
		int pred_size = SegmentationMetricsHelper.getRectangleSize( min_pred, min_pred, max_pred, max_pred );
		assertEquals( gt_size, cm.getGroundTruthLabelSize( 0 ) );
		assertEquals( pred_size, cm.getPredictionLabelSize( 0 ) );
	}

	@Test
	public void testNonIntersecting()
	{
		long[] dims = { 15, 15 };
		final Img< IntType > gt = ArrayImgs.ints( dims );
		final Img< IntType > pred = ArrayImgs.ints( dims );

		int min_gt = 2;
		int max_gt = 6;
		int min_pred = 8;
		int max_pred = 11;

		// paint
		SegmentationMetricsHelper.paintRectangle( gt, min_gt, min_gt, max_gt, max_gt, 9 );
		SegmentationMetricsHelper.paintRectangle( pred, min_pred, min_pred, max_pred, max_pred, 5 );

		// confusion metrics
		ConfusionMatrix< IntType, IntType > cm = new ConfusionMatrix<>( gt, pred );

		// intersection
		assertEquals( 0, cm.getIntersection( 0, 0 ) );
	}

	@Test
	public void testIntersecting()
	{
		long[] dims = { 15, 15 };
		final Img< IntType > gt = ArrayImgs.ints( dims );
		final Img< IntType > pred = ArrayImgs.ints( dims );

		int min_gt = 2;
		int max_gt = 10;
		int min_pred = 5;
		int max_pred = 11;

		// paint
		SegmentationMetricsHelper.paintRectangle( gt, min_gt, min_gt, max_gt, max_gt, 9 );
		SegmentationMetricsHelper.paintRectangle( pred, min_pred, min_pred, max_pred, max_pred, 5 );

		// confusion metrics
		ConfusionMatrix< IntType, IntType > cm = new ConfusionMatrix<>( gt, pred );

		// intersection
		int intersection = SegmentationMetricsHelper.getIntersectionBetweenRectangles( min_gt, min_gt, max_gt, max_gt, min_pred, min_pred, max_pred, max_pred );
		assertEquals( intersection, cm.getIntersection( 0, 0 ) );
	}
}
