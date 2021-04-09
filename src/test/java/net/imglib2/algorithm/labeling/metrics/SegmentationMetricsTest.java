package net.imglib2.algorithm.labeling.metrics;

import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class SegmentationMetricsTest {

    public static final long[] exampleIndexArrayDims = new long[] {4, 5};
    public static String[] exampleIntersectingLabels = new String[] { "A", "A,B", "C", "D", "D,E"};
    public static String[] exampleNonIntersectingLabels = new String[] { "A", "A,B", "C", "D", "E"};
    public static int[] exampleIndexArray = new int[] {
            1, 0, 0, 0, 0,
            0, 1, 0, 5, 0,
            0, 0, 0, 3, 3,
            0, 0, 3, 3, 0
    };

    public static List<Set<String>> getLabelingSet(String[] labels){
        List< Set<String> > labelings = new ArrayList<>();

        labelings.add(new HashSet<>());

        // Add label Sets
        for(String entries: labels){
            Set<String> subLabelSet = new HashSet<>();
            for(String entry: entries.split(",")){
                subLabelSet.add(entry);
            }
            labelings.add(subLabelSet);
        }

        return labelings;
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testException(){
        final Img<IntType> img = ArrayImgs.ints(exampleIndexArray, exampleIndexArrayDims);
        final ImgLabeling<String, IntType> labeling = ImgLabeling.fromImageAndLabelSets(img, getLabelingSet(exampleIntersectingLabels));
        final ImgLabeling<String, IntType> labeling2 = ImgLabeling.fromImageAndLabelSets(img, getLabelingSet(exampleNonIntersectingLabels));

        new DummyMetrics().computeMetrics(labeling, labeling2, 0.5);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testException2(){
        final Img<IntType> img = ArrayImgs.ints(exampleIndexArray, exampleIndexArrayDims);
        final ImgLabeling<String, IntType> labeling = ImgLabeling.fromImageAndLabelSets(img, getLabelingSet(exampleNonIntersectingLabels));
        final ImgLabeling<String, IntType> labeling2 = ImgLabeling.fromImageAndLabelSets(img, getLabelingSet(exampleIntersectingLabels));

        new DummyMetrics().computeMetrics(labeling, labeling2, 0.5);
    }

    @Test
    public void testNoException(){
        final Img<IntType> img = ArrayImgs.ints(exampleIndexArray, exampleIndexArrayDims);
        final ImgLabeling<String, IntType> labeling = ImgLabeling.fromImageAndLabelSets(img, getLabelingSet(exampleNonIntersectingLabels));

        new DummyMetrics().computeMetrics(labeling, labeling, 0.5);
    }

    @Test
    public void testConfusionMatrixSizes(){
        long[] dims = {15,15};
        final Img<IntType> gt = ArrayImgs.ints( dims );
        final Img<IntType> pred = ArrayImgs.ints( dims );

        int min_gt = 2;
        int max_gt = 9;
        int min_pred = 7;
        int max_pred = 11;

        // paint
        paintRectangle(gt, min_gt, min_gt, max_gt, max_gt, 9);
        paintRectangle(pred, min_pred, min_pred, max_pred, max_pred, 5);

        // confusion metrics
        SegmentationMetrics.ConfusionMatrix<IntType, IntType> cm = new SegmentationMetrics.ConfusionMatrix<>(gt, pred);

        // total sizes
        int gt_size = getRectangleSize(min_gt, min_gt, max_gt, max_gt);
        int pred_size = getRectangleSize(min_pred, min_pred, max_pred, max_pred);
        assertEquals(gt_size, cm.getGroundTruthLabelSize(0));
        assertEquals(pred_size, cm.getPredictionLabelSize(0));
    }

    @Test
    public void testConfusionMatrixNonIntersecting(){
        long[] dims = {15,15};
        final Img<IntType> gt = ArrayImgs.ints( dims );
        final Img<IntType> pred = ArrayImgs.ints( dims );

        int min_gt = 2;
        int max_gt = 6;
        int min_pred = 8;
        int max_pred = 11;

        // paint
        paintRectangle(gt, min_gt, min_gt, max_gt, max_gt, 9);
        paintRectangle(pred, min_pred, min_pred, max_pred, max_pred, 5);

        // confusion metrics
        SegmentationMetrics.ConfusionMatrix<IntType, IntType> cm = new SegmentationMetrics.ConfusionMatrix<>(gt, pred);

        // intersection
        assertEquals(0, cm.getIntersection(0, 0));
    }

    @Test
    public void testConfusionMatrixIntersecting(){
        long[] dims = {15,15};
        final Img<IntType> gt = ArrayImgs.ints( dims );
        final Img<IntType> pred = ArrayImgs.ints( dims );

        int min_gt = 2;
        int max_gt = 10;
        int min_pred = 5;
        int max_pred = 11;

        // paint
        paintRectangle(gt, min_gt, min_gt, max_gt, max_gt, 9);
        paintRectangle(pred, min_pred, min_pred, max_pred, max_pred, 5);

        // confusion metrics
        SegmentationMetrics.ConfusionMatrix<IntType, IntType> cm = new SegmentationMetrics.ConfusionMatrix<>(gt, pred);

        // intersection
        int intersection = getIntersectionBetweenRectangles(min_gt, min_gt, max_gt, max_gt, min_pred, min_pred, max_pred, max_pred);
        assertEquals(intersection, cm.getIntersection(0, 0));
    }

    @Test
    public void testComputeLocalMetricsOverlapping(){
        long[] dims = {15, 15};
        final Img<IntType> gt = ArrayImgs.ints( dims );
        final Img<IntType> pred = ArrayImgs.ints( dims );

        int[] gtRect = {2,4,8,6};
        int[] predRect = {3,4,11,5};

        // paint
        paintRectangle(gt, gtRect, 9);
        paintRectangle(pred, predRect, 5);

        // confusion metrics
        SegmentationMetrics.ConfusionMatrix<IntType, IntType> cm = new SegmentationMetrics.ConfusionMatrix<>(gt, pred);

        // Metrics
        SegmentationMetrics metrics = new DummyMetrics();

        // values
        double localIoU = getIoUBetweenRectangles(gtRect, predRect);

        // local iou
        for(double t = 0.; t < 1.; t+=0.05) {
            double iou = -localIoU > t ? localIoU : 0;
            assertEquals(iou, metrics.computeLocalMetrics(0, 0, cm, t), 0.00001);
        }
    }

    @Test
    public void testComputeLocalMetricsDisjoint(){
        long[] dims = {15, 15};
        final Img<IntType> gt = ArrayImgs.ints( dims );
        final Img<IntType> pred = ArrayImgs.ints( dims );

        int[] gtRect = {2,4,8,6};
        int[] predRect = {9,7,14,9};

        // paint
        paintRectangle(gt, gtRect, 9);
        paintRectangle(pred, predRect, 5);

        // confusion metrics
        SegmentationMetrics.ConfusionMatrix<IntType, IntType> cm = new SegmentationMetrics.ConfusionMatrix<>(gt, pred);

        // Metrics
        SegmentationMetrics metrics = new DummyMetrics();

        assertEquals(0., metrics.computeLocalMetrics(0, 0, cm, 0.), 0.00001);
    }

    @Test
    public void testComputeLocalMetricsEmptyGT(){
        long[] dims = {15, 15};
        final Img<IntType> gt = ArrayImgs.ints( dims );
        final Img<IntType> pred = ArrayImgs.ints( dims );

        int[] predRect = {9,7,14,9};

        // paint
        paintRectangle(pred, predRect, 5);

        // confusion metrics
        SegmentationMetrics.ConfusionMatrix<IntType, IntType> cm = new SegmentationMetrics.ConfusionMatrix<>(gt, pred);

        // Metrics
        SegmentationMetrics metrics = new DummyMetrics();

        assertEquals(0, metrics.computeLocalMetrics(0, 0, cm, 0.), 0.00001);
    }

    @Test
    public void testComputeLocalMetricsEmptyPrediction(){
        long[] dims = {15, 15};
        final Img<IntType> gt = ArrayImgs.ints( dims );
        final Img<IntType> pred = ArrayImgs.ints( dims );

        int[] gtRect = {2,4,8,6};

        // paint
        paintRectangle(gt, gtRect, 9);

        // confusion metrics
        SegmentationMetrics.ConfusionMatrix<IntType, IntType> cm = new SegmentationMetrics.ConfusionMatrix<>(gt, pred);

        // Metrics
        SegmentationMetrics metrics = new DummyMetrics();

        assertEquals(0, metrics.computeLocalMetrics(0, 0, cm, 0.), 0.00001);
    }

    @Test
    public void testComputeLocalMetricsWithMoreGTLabels(){
        long[] dims = {15, 15};
        final Img<IntType> gt = ArrayImgs.ints( dims );
        final Img<IntType> pred = ArrayImgs.ints( dims );

        int[] gtRect1 = {12,7,14,11};
        int[] gtRect2 = {2,4,8,6};

        int[] predRect = {3,4,11,5};

        int gtLabel1 = 9;
        int gtLabel2 = 5;
        int predLabel = 5;

        // paint
        paintRectangle(gt, gtRect1, gtLabel1);
        paintRectangle(gt, gtRect2, gtLabel2);
        paintRectangle(pred, predRect, predLabel);

        // confusion metrics
        SegmentationMetrics.ConfusionMatrix<IntType, IntType> cm = new SegmentationMetrics.ConfusionMatrix<>(gt, pred);

        // Metrics
        SegmentationMetrics metrics = new DummyMetrics();

        // values
        double localIoU1 = getIoUBetweenRectangles(gtRect1, predRect);
        double localIoU2 = getIoUBetweenRectangles(gtRect2, predRect);

        // gtLabel2 encountered first, gtLabel1 second
        assertEquals(localIoU1, metrics.computeLocalMetrics(1, 0, cm, 0.), 0.00001);
        assertEquals(localIoU2, metrics.computeLocalMetrics(0, 0, cm, 0.), 0.00001);
    }

    @Test
    public void testComputeLocalMetricsWithMorePredLabels(){
        long[] dims = {15, 15};
        final Img<IntType> gt = ArrayImgs.ints( dims );
        final Img<IntType> pred = ArrayImgs.ints( dims );

        int[] gtRect = {2,4,8,6};

        int[] predRect1 = {12,7,14,11};
        int[] predRect2 = {3,4,11,5};

        int gtLabel = 5;
        int predLabel1 = 9;
        int predLabel2 = 5;

        // paint
        paintRectangle(gt, gtRect, gtLabel);
        paintRectangle(pred, predRect1, predLabel1);
        paintRectangle(pred, predRect2, predLabel2);

        // confusion metrics
        SegmentationMetrics.ConfusionMatrix<IntType, IntType> cm = new SegmentationMetrics.ConfusionMatrix<>(gt, pred);

        // Metrics
        SegmentationMetrics metrics = new DummyMetrics();

        // values
        double localIoU1 = getIoUBetweenRectangles(gtRect, predRect1);
        double localIoU2 = getIoUBetweenRectangles(gtRect, predRect2);

        // predLabel2 encountered first, predLabel1 second
        assertEquals(localIoU1, metrics.computeLocalMetrics(0, 1, cm, 0.), 0.00001);
        assertEquals(localIoU2, metrics.computeLocalMetrics(0, 0, cm, 0.), 0.00001);
    }

    private static int getIntersectionBetweenRectangles(int[] a_rect, int[] b_rect){
        return getIntersectionBetweenRectangles(a_rect[0],a_rect[1],a_rect[2],a_rect[3],
                b_rect[0],b_rect[1],b_rect[2],b_rect[3]);
    }

    private static int getIntersectionBetweenRectangles(int a_x_min, int a_y_min, int a_x_max, int a_y_max,
                                                        int b_x_min, int b_y_min, int b_x_max, int b_y_max){
        int left = Math.max(a_x_min, b_x_min);
        int right = Math.min(a_x_max, b_x_max);
        int bottom = Math.max(a_y_min, b_y_min);
        int top = Math.min(a_y_max, b_y_max);

        if(left < right && bottom < top){
            int intersection = (right-left+1)*(top-bottom+1);

            return intersection;
        } else {
            return 0;
        }
    }

    private static int getRectangleSize(int[] rect){
        return getRectangleSize(rect[0],rect[1],rect[2],rect[3]);
    }

    private static int getRectangleSize(int x_min, int y_min, int x_max, int y_max){
        return (x_max-x_min+1)*(y_max-y_min+1);
    }

    private static double getIoUBetweenRectangles(int[] a_rect, int[] b_rect){
        return getIoUBetweenRectangles(a_rect[0],a_rect[1],a_rect[2],a_rect[3],
                b_rect[0],b_rect[1],b_rect[2],b_rect[3]);
    }

    private static double getIoUBetweenRectangles(int a_x_min, int a_y_min, int a_x_max, int a_y_max,
                                                  int b_x_min, int b_y_min, int b_x_max, int b_y_max){
        int left = Math.max(a_x_min, b_x_min);
        int right = Math.min(a_x_max, b_x_max);
        int bottom = Math.max(a_y_min, b_y_min);
        int top = Math.min(a_y_max, b_y_max);

        if(left < right && bottom < top){
            double intersection = (double) (right-left+1)*(top-bottom+1);
            double a_area = getRectangleSize(a_x_min, a_y_min, a_x_max, a_y_max);
            double b_area = getRectangleSize(b_x_min, b_y_min, b_x_max, b_y_max);

            return -intersection / (a_area + b_area - intersection);
        } else {
            return 0.;
        }
    }

    private class DummyMetrics extends SegmentationMetrics{

        @Override
        protected double computeMetrics(ConfusionMatrix confusionMatrix, double[][] costMatrix, double threshold) {
            return 0;
        }
    }

    public static void paintRectangle(Img<IntType> img, int[] rect, int value) {
        long[] interval = {rect[0], rect[1], rect[2], rect[3]};
        IntervalView<IntType> intView = Views.interval(img, Intervals.createMinMax(interval));
        Cursor<IntType> cur = intView.cursor();
        while (cur.hasNext()) {
            cur.next().set(value);
        }
    }

    public static void paintRectangle(Img<IntType> img, int min_x, int min_y, int max_x, int max_y, int value){
        long[] interval = {min_x, min_y, max_x, max_y};
        IntervalView<IntType> intView = Views.interval(img, Intervals.createMinMax(interval));
        Cursor<IntType> cur = intView.cursor();
        while(cur.hasNext()){
            cur.next().set(value);
        }
    }
}
