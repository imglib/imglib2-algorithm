package net.imglib2.algorithm.labeling.metrics;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class MultiMetricsTest {

    @Test(expected = UnsupportedOperationException.class)
    public void testException() {
        final Img<IntType> img = ArrayImgs.ints(SegmentationMetricsTest.exampleIndexArray,
                SegmentationMetricsTest.exampleIndexArrayDims);
        final ImgLabeling<String, IntType> labeling = ImgLabeling.fromImageAndLabelSets(img,
                SegmentationMetricsTest.getLabelingSet(SegmentationMetricsTest.exampleIntersectingLabels));
        final ImgLabeling<String, IntType> labeling2 = ImgLabeling.fromImageAndLabelSets(img,
                SegmentationMetricsTest.getLabelingSet(SegmentationMetricsTest.exampleNonIntersectingLabels));

        new MultiMetrics().computeMetrics(labeling, labeling2, 0.5);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testException2() {
        final Img<IntType> img = ArrayImgs.ints(SegmentationMetricsTest.exampleIndexArray,
                SegmentationMetricsTest.exampleIndexArrayDims);
        final ImgLabeling<String, IntType> labeling = ImgLabeling.fromImageAndLabelSets(img,
                SegmentationMetricsTest.getLabelingSet(SegmentationMetricsTest.exampleNonIntersectingLabels));
        final ImgLabeling<String, IntType> labeling2 = ImgLabeling.fromImageAndLabelSets(img,
                SegmentationMetricsTest.getLabelingSet(SegmentationMetricsTest.exampleIntersectingLabels));

        new MultiMetrics().computeMetrics(labeling, labeling2, 0.5);
    }

    @Test
    public void testIdentity() {
        long[] dims = {64, 64};
        final Img<IntType> img = ArrayImgs.ints(dims);

        // paint
        SegmentationMetricsTest.paintRectangle(img, 12, 28, 42, 56, 9);
        SegmentationMetricsTest.paintRectangle(img, 43, 9, 52, 18, 12);

        // default is the average precision
        assertEquals(1., new MultiMetrics().computeMetrics(img, img, 0.5), 0.0001);

        // get metrics
        MultiMetrics mm = new MultiMetrics();
        mm.computeMetrics(img, img, 0.5);

        assertEquals(2., mm.getMetrics(MultiMetrics.TP), 0.0001);
        assertEquals(0., mm.getMetrics(MultiMetrics.FP), 0.0001);
        assertEquals(0., mm.getMetrics(MultiMetrics.FN), 0.0001);
        assertEquals(1., mm.getMetrics(MultiMetrics.AV_PRECISION), 0.0001);
        assertEquals(1., mm.getMetrics(MultiMetrics.MEAN_MATCHED_IOU), 0.0001);
        assertEquals(1., mm.getMetrics(MultiMetrics.MEAN_TRUE_IOU), 0.0001);
        assertEquals(1., mm.getMetrics(MultiMetrics.PRECISION), 0.0001);
        assertEquals(1., mm.getMetrics(MultiMetrics.RECALL), 0.0001);
        assertEquals(1., mm.getMetrics(MultiMetrics.F1), 0.0001);

        // call with a specific metrics
        assertEquals(2., new MultiMetrics().computeMetrics(img, img, 0.5, MultiMetrics.TP), 0.0001);
        assertEquals(0., new MultiMetrics().computeMetrics(img, img, 0.5, MultiMetrics.FP), 0.0001);
        assertEquals(0., new MultiMetrics().computeMetrics(img, img, 0.5, MultiMetrics.FN), 0.0001);
        assertEquals(1., new MultiMetrics().computeMetrics(img, img, 0.5, MultiMetrics.AV_PRECISION), 0.0001);
        assertEquals(1., new MultiMetrics().computeMetrics(img, img, 0.5, MultiMetrics.MEAN_MATCHED_IOU), 0.0001);
        assertEquals(1., new MultiMetrics().computeMetrics(img, img, 0.5, MultiMetrics.MEAN_TRUE_IOU), 0.0001);
        assertEquals(1., new MultiMetrics().computeMetrics(img, img, 0.5, MultiMetrics.PRECISION), 0.0001);
        assertEquals(1., new MultiMetrics().computeMetrics(img, img, 0.5, MultiMetrics.RECALL), 0.0001);
        assertEquals(1., new MultiMetrics().computeMetrics(img, img, 0.5, MultiMetrics.F1), 0.0001);

        // compute all metrics
        Map<String, Double> metrics = new MultiMetrics().computeAllMetrics(img, img, 0.5);

        assertEquals(2., metrics.get(MultiMetrics.TP), 0.0001);
        assertEquals(0., metrics.get(MultiMetrics.FP), 0.0001);
        assertEquals(0., metrics.get(MultiMetrics.FN), 0.0001);
        assertEquals(1., metrics.get(MultiMetrics.AV_PRECISION), 0.0001);
        assertEquals(1., metrics.get(MultiMetrics.MEAN_MATCHED_IOU), 0.0001);
        assertEquals(1., metrics.get(MultiMetrics.MEAN_TRUE_IOU), 0.0001);
        assertEquals(1., metrics.get(MultiMetrics.PRECISION), 0.0001);
        assertEquals(1., metrics.get(MultiMetrics.RECALL), 0.0001);
        assertEquals(1., metrics.get(MultiMetrics.F1), 0.0001);
    }

    @Test
    public void testEmpty() {
        long[] dims = {64, 64};
        final Img<IntType> nonEmpty = ArrayImgs.ints(dims);
        final Img<IntType> empty = ArrayImgs.ints(dims);

        // paint
        SegmentationMetricsTest.paintRectangle(nonEmpty, 12, 28, 42, 56, 9);

        // empty gt, non empty pred
        Map<String, Double> metrics = new MultiMetrics().computeAllMetrics(empty, nonEmpty, 0.5);

        assertEquals(0., metrics.get(MultiMetrics.TP), 0.0001);
        assertEquals(1., metrics.get(MultiMetrics.FP), 0.0001);
        assertEquals(0., metrics.get(MultiMetrics.FN), 0.0001);
        assertEquals(0., metrics.get(MultiMetrics.AV_PRECISION), 0.0001);
        assertEquals(0., metrics.get(MultiMetrics.MEAN_MATCHED_IOU), 0.0001);
        assertEquals(0., metrics.get(MultiMetrics.MEAN_TRUE_IOU), 0.0001);
        assertEquals(0., metrics.get(MultiMetrics.PRECISION), 0.0001);
        assertEquals(0., metrics.get(MultiMetrics.RECALL), 0.0001);
        assertEquals(0., metrics.get(MultiMetrics.F1), 0.0001);

        // non empty gt, empty pred
        metrics = new MultiMetrics().computeAllMetrics(nonEmpty, empty, 0.5);

        assertEquals(0., metrics.get(MultiMetrics.TP), 0.0001);
        assertEquals(0., metrics.get(MultiMetrics.FP), 0.0001);
        assertEquals(1., metrics.get(MultiMetrics.FN), 0.0001);
        assertEquals(0., metrics.get(MultiMetrics.AV_PRECISION), 0.0001);
        assertEquals(0., metrics.get(MultiMetrics.MEAN_MATCHED_IOU), 0.0001);
        assertEquals(0., metrics.get(MultiMetrics.MEAN_TRUE_IOU), 0.0001);
        assertEquals(0., metrics.get(MultiMetrics.PRECISION), 0.0001);
        assertEquals(0., metrics.get(MultiMetrics.RECALL), 0.0001);
        assertEquals(0., metrics.get(MultiMetrics.F1), 0.0001);

        // both empty
        metrics = new MultiMetrics().computeAllMetrics(empty, empty, 0.5);

        assertEquals(0., metrics.get(MultiMetrics.TP), 0.0001);
        assertEquals(0., metrics.get(MultiMetrics.FP), 0.0001);
        assertEquals(0., metrics.get(MultiMetrics.FN), 0.0001);
        assertEquals(1., metrics.get(MultiMetrics.AV_PRECISION), 0.0001);
        assertEquals(1., metrics.get(MultiMetrics.MEAN_MATCHED_IOU), 0.0001);
        assertEquals(1., metrics.get(MultiMetrics.MEAN_TRUE_IOU), 0.0001);
        assertEquals(1., metrics.get(MultiMetrics.PRECISION), 0.0001);
        assertEquals(1., metrics.get(MultiMetrics.RECALL), 0.0001);
        assertEquals(1., metrics.get(MultiMetrics.F1), 0.0001);
    }

    @Test
    public void testNonOverlapping() {
        long[] dims = {64, 64};
        final Img<IntType> groundtruth = ArrayImgs.ints(dims);
        final Img<IntType> prediction = ArrayImgs.ints(dims);

        // paint
        SegmentationMetricsTest.paintRectangle(groundtruth, 12, 5, 25, 13, 9);
        SegmentationMetricsTest.paintRectangle(prediction, 28, 15, 42, 32, 12);

        Map<String, Double> metrics = new MultiMetrics().computeAllMetrics(groundtruth, prediction, 0.5);

        assertEquals(0., metrics.get(MultiMetrics.TP), 0.0001);
        assertEquals(1., metrics.get(MultiMetrics.FP), 0.0001);
        assertEquals(1., metrics.get(MultiMetrics.FN), 0.0001);
        assertEquals(0., metrics.get(MultiMetrics.AV_PRECISION), 0.0001);
        assertEquals(0., metrics.get(MultiMetrics.MEAN_MATCHED_IOU), 0.0001);
        assertEquals(0., metrics.get(MultiMetrics.MEAN_TRUE_IOU), 0.0001);
        assertEquals(0., metrics.get(MultiMetrics.PRECISION), 0.0001);
        assertEquals(0., metrics.get(MultiMetrics.RECALL), 0.0001);
        assertEquals(0., metrics.get(MultiMetrics.F1), 0.0001);
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
        SegmentationMetricsTest.paintRectangle(groundtruth, gtRect1, 9);
        SegmentationMetricsTest.paintRectangle(prediction, predRect1, 5);

        SegmentationMetricsTest.paintRectangle(groundtruth, gtRect2, 2);
        SegmentationMetricsTest.paintRectangle(prediction, predRect2, 8);

        double iou1 = AveragePrecisionTest.getIoUBetweenRectangles(gtRect1, predRect1);
        double iou2 = AveragePrecisionTest.getIoUBetweenRectangles(gtRect2, predRect2);
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

            Map<String, Double> metrics = new MultiMetrics().computeAllMetrics(groundtruth, prediction, t);

            assertEquals(tp, metrics.get(MultiMetrics.TP), 0.0001);
            assertEquals(fp, metrics.get(MultiMetrics.FP), 0.0001);
            assertEquals(fn, metrics.get(MultiMetrics.FN), 0.0001);
            assertEquals(avprec, metrics.get(MultiMetrics.AV_PRECISION), 0.0001);
            assertEquals(meanMatched, metrics.get(MultiMetrics.MEAN_MATCHED_IOU), 0.0001);
            assertEquals(meanTrue, metrics.get(MultiMetrics.MEAN_TRUE_IOU), 0.0001);
            assertEquals(prec, metrics.get(MultiMetrics.PRECISION), 0.0001);
            assertEquals(recall, metrics.get(MultiMetrics.RECALL), 0.0001);
            assertEquals(f1, metrics.get(MultiMetrics.F1), 0.0001);
        }
    }
}
