package net.imglib2.algorithm.metrics.segmentation;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class SegmentationMetricsTest
{

	public static final long[] exampleIndexArrayDims = new long[] { 4, 5 };

	public static String[] exampleIntersectingLabels = new String[] { "A", "A,B", "C", "D", "D,E" };

	public static String[] exampleNonIntersectingLabels = new String[] { "A", "A,B", "C", "D", "E" };

	public static int[] exampleIndexArray = new int[] {
			1, 0, 0, 0, 0,
			0, 1, 0, 5, 0,
			0, 0, 0, 3, 3,
			0, 0, 3, 3, 0
	};

	public static List< Set< String > > getLabelingSet( String[] labels )
	{
		List< Set< String > > labelings = new ArrayList<>();

		labelings.add( new HashSet<>() );

		// Add label Sets
		for ( String entries : labels )
		{
			Set< String > subLabelSet = new HashSet<>();
			for ( String entry : entries.split( "," ) )
			{
				subLabelSet.add( entry );
			}
			labelings.add( subLabelSet );
		}

		return labelings;
	}

	@Test
	public void testStaticMethods()
	{
		final Img< IntType > img = ArrayImgs.ints( exampleIndexArray, exampleIndexArrayDims );
		final ImgLabeling< String, IntType > labelingIntersect = ImgLabeling.fromImageAndLabelSets( img, getLabelingSet( exampleIntersectingLabels ) );
		final ImgLabeling< String, IntType > labelingNonIntersect = ImgLabeling.fromImageAndLabelSets( img, getLabelingSet( exampleNonIntersectingLabels ) );

		Set< IntType > occIntersect = SegmentationMetrics.getOccurringLabelSets( labelingIntersect );
		assertEquals( 3, occIntersect.size() );

		for ( IntType it : occIntersect )
		{
			int i = it.getInteger();
			assertTrue( i == 1 || i == 3 || i == 5 );
		}

		assertTrue( SegmentationMetrics.hasIntersectingLabels( labelingIntersect ) );
		assertFalse( SegmentationMetrics.hasIntersectingLabels( labelingNonIntersect ) );
	}

	@Test( expected = UnsupportedOperationException.class )
	public void testException()
	{
		final Img< IntType > img = ArrayImgs.ints( exampleIndexArray, exampleIndexArrayDims );
		final ImgLabeling< String, IntType > labeling = ImgLabeling.fromImageAndLabelSets( img, getLabelingSet( exampleIntersectingLabels ) );
		final ImgLabeling< String, IntType > labeling2 = ImgLabeling.fromImageAndLabelSets( img, getLabelingSet( exampleNonIntersectingLabels ) );

		new DummyMetrics().computeMetrics( labeling, labeling2 );
	}

	@Test( expected = UnsupportedOperationException.class )
	public void testException2()
	{
		final Img< IntType > img = ArrayImgs.ints( exampleIndexArray, exampleIndexArrayDims );
		final ImgLabeling< String, IntType > labeling = ImgLabeling.fromImageAndLabelSets( img, getLabelingSet( exampleNonIntersectingLabels ) );
		final ImgLabeling< String, IntType > labeling2 = ImgLabeling.fromImageAndLabelSets( img, getLabelingSet( exampleIntersectingLabels ) );

		new DummyMetrics().computeMetrics( labeling, labeling2 );
	}

	@Test
	public void testNoException()
	{
		final Img< IntType > img = ArrayImgs.ints( exampleIndexArray, exampleIndexArrayDims );
		final ImgLabeling< String, IntType > labeling = ImgLabeling.fromImageAndLabelSets( img, getLabelingSet( exampleNonIntersectingLabels ) );

		new DummyMetrics().computeMetrics( labeling, labeling );
	}

	@Test
	public void testFunctionalInterface()
	{
		long[] dims = { 32, 32 };
		final Img< IntType > groundtruth = ArrayImgs.ints( dims );
		final Img< IntType > prediction = ArrayImgs.ints( dims );

		int min_gt = 2;
		int max_gt = 11;
		int min_pred = min_gt + 1;
		int max_pred = max_gt + 1;

		// paint
		SegmentationMetricsTestHelper.paintRectangle( groundtruth, min_gt, min_gt, max_gt, max_gt, 9 );
		SegmentationMetricsTestHelper.paintRectangle( prediction, min_pred, min_pred, max_pred, max_pred, 5 );

		// metrics
		double seg = SEGTest.getSEGBetweenRectangles( min_gt, min_gt, max_gt, max_gt, min_pred, min_pred, max_pred, max_pred );
		double iou = AccuracyTest.getIoUBetweenRectangles( min_gt, min_gt, max_gt, max_gt, min_pred, min_pred, max_pred, max_pred );
		double avprec = 1.;

		List< Pair< Img< IntType >, Img< IntType > > > images = new ArrayList<>();
		for ( int i = 0; i < 10; i++ )
			images.add( new ValuePair<>( groundtruth.copy(), prediction.copy() ) );

		final SegmentationMetrics segMetrics = new SEG();
		images.stream().mapToDouble( p -> Consumer.computeMetrics( p, segMetrics ) ).forEach( d -> assertEquals( seg, d, 0.0001 ) );

		final SegmentationMetrics avPrecMetrics = new Accuracy( 0.5 );
		images.stream().mapToDouble( p -> Consumer.computeMetrics( p, avPrecMetrics ) ).forEach( d -> assertEquals( avprec, d, 0.0001 ) );

		final SegmentationMetrics meanTrueMetrics = new MultiMetrics( MultiMetrics.Metrics.MEAN_TRUE_IOU, 0.5 );
		images.stream().mapToDouble( p -> Consumer.computeMetrics( p, meanTrueMetrics ) ).forEach( d -> assertEquals( iou, d, 0.0001 ) );
	}

	public static class DummyMetrics implements SegmentationMetrics
	{
		@Override
		public < I extends IntegerType< I >, J extends IntegerType< J > > double computeMetrics( RandomAccessibleInterval< I > groundTruth, RandomAccessibleInterval< J > prediction )
		{
			return 0;
		}
	}

	public static class Consumer
	{
		public static double computeMetrics( Pair< Img< IntType >, Img< IntType > > pair, SegmentationMetrics metrics )
		{
			return metrics.computeMetrics( pair.getA(), pair.getB() );
		}
	}
}
