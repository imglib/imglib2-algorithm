package net.imglib2.algorithm.metrics.segmentation;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.IntType;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class MultiMetricsTest {

    @Test
    public void testIdentity() {
        long[] dims = {64, 64};
        final Img<IntType> img = ArrayImgs.ints(dims);

        // paint
        SegmentationMetricsTestHelper.paintRectangle(img, 12, 28, 42, 56, 9);
        SegmentationMetricsTestHelper.paintRectangle(img, 43, 9, 52, 18, 12);

        // default is the average precision
        assertEquals(1., new MultiMetrics(0.5).computeMetrics(img, img), 0.0001);

        // get metrics
        MultiMetrics mm = new MultiMetrics(0.5);
        mm.computeMetrics(img, img);

        assertEquals(2., mm.getMetrics(MultiMetrics.Metrics.TP), 0.0001);
        assertEquals(0., mm.getMetrics(MultiMetrics.Metrics.FP), 0.0001);
        assertEquals(0., mm.getMetrics(MultiMetrics.Metrics.FN), 0.0001);
        assertEquals(1., mm.getMetrics(MultiMetrics.Metrics.AV_PRECISION), 0.0001);
        assertEquals(1., mm.getMetrics(MultiMetrics.Metrics.MEAN_MATCHED_IOU), 0.0001);
        assertEquals(1., mm.getMetrics(MultiMetrics.Metrics.MEAN_TRUE_IOU), 0.0001);
        assertEquals(1., mm.getMetrics(MultiMetrics.Metrics.PRECISION), 0.0001);
        assertEquals(1., mm.getMetrics(MultiMetrics.Metrics.RECALL), 0.0001);
        assertEquals(1., mm.getMetrics(MultiMetrics.Metrics.F1), 0.0001);

        // call with a specific metrics
        assertEquals(2., new MultiMetrics(MultiMetrics.Metrics.TP, 0.5).computeMetrics(img, img), 0.0001);
        assertEquals(0., new MultiMetrics(MultiMetrics.Metrics.FP, 0.5).computeMetrics(img, img), 0.0001);
        assertEquals(0., new MultiMetrics(MultiMetrics.Metrics.FN, 0.5).computeMetrics(img, img), 0.0001);
        assertEquals(1., new MultiMetrics(MultiMetrics.Metrics.AV_PRECISION, 0.5).computeMetrics(img, img), 0.0001);
        assertEquals(1., new MultiMetrics(MultiMetrics.Metrics.MEAN_MATCHED_IOU, 0.5).computeMetrics(img, img), 0.0001);
        assertEquals(1., new MultiMetrics(MultiMetrics.Metrics.MEAN_TRUE_IOU, 0.5).computeMetrics(img, img), 0.0001);
        assertEquals(1., new MultiMetrics(MultiMetrics.Metrics.PRECISION, 0.5).computeMetrics(img, img), 0.0001);
        assertEquals(1., new MultiMetrics(MultiMetrics.Metrics.RECALL, 0.5).computeMetrics(img, img), 0.0001);
        assertEquals(1., new MultiMetrics(MultiMetrics.Metrics.F1, 0.5).computeMetrics(img, img), 0.0001);

        // compute all metrics
        Map<MultiMetrics.Metrics, Double> metrics = new MultiMetrics(0.5).computeAllMetrics(img, img);

        assertEquals(2., metrics.get(MultiMetrics.Metrics.TP), 0.0001);
        assertEquals(0., metrics.get(MultiMetrics.Metrics.FP), 0.0001);
        assertEquals(0., metrics.get(MultiMetrics.Metrics.FN), 0.0001);
        assertEquals(1., metrics.get(MultiMetrics.Metrics.AV_PRECISION), 0.0001);
        assertEquals(1., metrics.get(MultiMetrics.Metrics.MEAN_MATCHED_IOU), 0.0001);
        assertEquals(1., metrics.get(MultiMetrics.Metrics.MEAN_TRUE_IOU), 0.0001);
        assertEquals(1., metrics.get(MultiMetrics.Metrics.PRECISION), 0.0001);
        assertEquals(1., metrics.get(MultiMetrics.Metrics.RECALL), 0.0001);
        assertEquals(1., metrics.get(MultiMetrics.Metrics.F1), 0.0001);
    }

    @Test
    public void testEmpty() {
        long[] dims = {64, 64};
        final Img<IntType> nonEmpty = ArrayImgs.ints(dims);
        final Img<IntType> empty = ArrayImgs.ints(dims);

        // paint
        SegmentationMetricsTestHelper.paintRectangle(nonEmpty, 12, 28, 42, 56, 9);

        // empty gt, non empty pred
        Map<MultiMetrics.Metrics, Double> metrics = new MultiMetrics(0.5).computeAllMetrics(empty, nonEmpty);

        assertEquals(0., metrics.get(MultiMetrics.Metrics.TP), 0.0001);
        assertEquals(1., metrics.get(MultiMetrics.Metrics.FP), 0.0001);
        assertEquals(0., metrics.get(MultiMetrics.Metrics.FN), 0.0001);
        assertEquals(0., metrics.get(MultiMetrics.Metrics.AV_PRECISION), 0.0001);
        assertEquals(0., metrics.get(MultiMetrics.Metrics.MEAN_MATCHED_IOU), 0.0001);
        assertEquals(0., metrics.get(MultiMetrics.Metrics.MEAN_TRUE_IOU), 0.0001);
        assertEquals(0., metrics.get(MultiMetrics.Metrics.PRECISION), 0.0001);
        assertEquals(0., metrics.get(MultiMetrics.Metrics.RECALL), 0.0001);
        assertEquals(0., metrics.get(MultiMetrics.Metrics.F1), 0.0001);

        // non empty gt, empty pred
        metrics = new MultiMetrics(0.5).computeAllMetrics(nonEmpty, empty);

        assertEquals(0., metrics.get(MultiMetrics.Metrics.TP), 0.0001);
        assertEquals(0., metrics.get(MultiMetrics.Metrics.FP), 0.0001);
        assertEquals(1., metrics.get(MultiMetrics.Metrics.FN), 0.0001);
        assertEquals(0., metrics.get(MultiMetrics.Metrics.AV_PRECISION), 0.0001);
        assertEquals(0., metrics.get(MultiMetrics.Metrics.MEAN_MATCHED_IOU), 0.0001);
        assertEquals(0., metrics.get(MultiMetrics.Metrics.MEAN_TRUE_IOU), 0.0001);
        assertEquals(0., metrics.get(MultiMetrics.Metrics.PRECISION), 0.0001);
        assertEquals(0., metrics.get(MultiMetrics.Metrics.RECALL), 0.0001);
        assertEquals(0., metrics.get(MultiMetrics.Metrics.F1), 0.0001);

        // both empty
        metrics = new MultiMetrics(0.5).computeAllMetrics(empty, empty);

        assertEquals(0., metrics.get(MultiMetrics.Metrics.TP), 0.0001);
        assertEquals(0., metrics.get(MultiMetrics.Metrics.FP), 0.0001);
        assertEquals(0., metrics.get(MultiMetrics.Metrics.FN), 0.0001);
        assertEquals(1., metrics.get(MultiMetrics.Metrics.AV_PRECISION), 0.0001);
        assertEquals(1., metrics.get(MultiMetrics.Metrics.MEAN_MATCHED_IOU), 0.0001);
        assertEquals(1., metrics.get(MultiMetrics.Metrics.MEAN_TRUE_IOU), 0.0001);
        assertEquals(1., metrics.get(MultiMetrics.Metrics.PRECISION), 0.0001);
        assertEquals(1., metrics.get(MultiMetrics.Metrics.RECALL), 0.0001);
        assertEquals(1., metrics.get(MultiMetrics.Metrics.F1), 0.0001);
    }

    @Test
    public void testNonOverlapping() {
        long[] dims = {64, 64};
        final Img<IntType> groundtruth = ArrayImgs.ints(dims);
        final Img<IntType> prediction = ArrayImgs.ints(dims);

        // paint
        SegmentationMetricsTestHelper.paintRectangle(groundtruth, 12, 5, 25, 13, 9);
        SegmentationMetricsTestHelper.paintRectangle(prediction, 28, 15, 42, 32, 12);

        Map<MultiMetrics.Metrics, Double> metrics = new MultiMetrics(0.5).computeAllMetrics(groundtruth, prediction);

        assertEquals(0., metrics.get(MultiMetrics.Metrics.TP), 0.0001);
        assertEquals(1., metrics.get(MultiMetrics.Metrics.FP), 0.0001);
        assertEquals(1., metrics.get(MultiMetrics.Metrics.FN), 0.0001);
        assertEquals(0., metrics.get(MultiMetrics.Metrics.AV_PRECISION), 0.0001);
        assertEquals(0., metrics.get(MultiMetrics.Metrics.MEAN_MATCHED_IOU), 0.0001);
        assertEquals(0., metrics.get(MultiMetrics.Metrics.MEAN_TRUE_IOU), 0.0001);
        assertEquals(0., metrics.get(MultiMetrics.Metrics.PRECISION), 0.0001);
        assertEquals(0., metrics.get(MultiMetrics.Metrics.RECALL), 0.0001);
        assertEquals(0., metrics.get(MultiMetrics.Metrics.F1), 0.0001);
    }

    @Test
    public void testOverlapping() {
        long[] dims = {32, 32};
        final Img<IntType> groundtruth = ArrayImgs.ints(dims);
        final Img<IntType> prediction = ArrayImgs.ints(dims);

        int[] gtRect1 = {2, 2, 11, 11};
        int[] predRect1 = {6, 6, 15, 15};

        int[] gtRect2 = {15, 15, 20, 20};
        int[] predRect2 = {16, 16, 22, 22};

        // paint
        SegmentationMetricsTestHelper.paintRectangle(groundtruth, gtRect1, 9);
        SegmentationMetricsTestHelper.paintRectangle(prediction, predRect1, 5);

        SegmentationMetricsTestHelper.paintRectangle(groundtruth, gtRect2, 2);
        SegmentationMetricsTestHelper.paintRectangle(prediction, predRect2, 8);

        double iou1 = AveragePrecisionTest.getIoUBetweenRectangles(gtRect1, predRect1);
        double iou2 = AveragePrecisionTest.getIoUBetweenRectangles(gtRect2, predRect2);

        MultiMetrics multiMetrics = new MultiMetrics();
        for (double t = 0.1; t < 0.9; t += 0.05) {
            double tp, fp, fn, f1, avprec, prec, recall, meanTrue, meanMatched;
            if (Double.compare(iou1, t) >= 0 && Double.compare(iou2, t) >= 0) { // both are matched
                tp = 2;
                fp = 0;
                fn = 0;
                avprec = 1;
                meanMatched = (iou1+iou2)/2;
                meanTrue = (iou1+iou2)/2;
                prec = 1;
                recall = 1;
                f1 = 1;
            } else if ((Double.compare(iou1, t) < 0 && Double.compare(iou2, t) < 0)) { // none is matched
                tp = 0;
                fp = 2;
                fn = 2;
                avprec = 0;
                meanMatched = 0;
                meanTrue = 0;
                prec = 0;
                recall = 0;
                f1 = 0;
            } else { // a single one is matched
                tp = 1;
                fp = 1;
                fn = 1;
                avprec = 1. / 3.;
                meanMatched = Math.max(iou1, iou2);
                meanTrue = Math.max(iou1, iou2)/2;
                prec = 1. / 2.;
                recall = 1. / 2.;
                f1 = 1. / 2.;
            }

            Map<MultiMetrics.Metrics, Double> metrics = multiMetrics.setThreshold(t).computeAllMetrics(groundtruth, prediction);

            assertEquals(tp, metrics.get(MultiMetrics.Metrics.TP), 0.0001);
            assertEquals(fp, metrics.get(MultiMetrics.Metrics.FP), 0.0001);
            assertEquals(fn, metrics.get(MultiMetrics.Metrics.FN), 0.0001);
            assertEquals(avprec, metrics.get(MultiMetrics.Metrics.AV_PRECISION), 0.0001);
            assertEquals(meanMatched, metrics.get(MultiMetrics.Metrics.MEAN_MATCHED_IOU), 0.0001);
            assertEquals(meanTrue, metrics.get(MultiMetrics.Metrics.MEAN_TRUE_IOU), 0.0001);
            assertEquals(prec, metrics.get(MultiMetrics.Metrics.PRECISION), 0.0001);
            assertEquals(recall, metrics.get(MultiMetrics.Metrics.RECALL), 0.0001);
            assertEquals(f1, metrics.get(MultiMetrics.Metrics.F1), 0.0001);
        }
    }
}
