package net.imglib2.algorithm.metrics.segmentation;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.IntType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AveragePrecisionTest {

    @Test
    public void testIdentity(){
        long[] dims = {64,64};
        final Img<IntType> img = ArrayImgs.ints( dims );

        // paint
        SegmentationMetricsTestHelper.paintRectangle(img, 12, 28, 42, 56, 9);
        SegmentationMetricsTestHelper.paintRectangle(img, 43, 9, 52, 18, 12);

        assertEquals(1., new AveragePrecision(0.5).computeMetrics(img, img), 0.0001);
    }

    @Test
    public void testEmpty(){
        long[] dims = {64,64};
        final Img<IntType> nonEmpty = ArrayImgs.ints( dims );
        final Img<IntType> empty = ArrayImgs.ints( dims );

        // paint
        SegmentationMetricsTestHelper.paintRectangle(nonEmpty, 12, 28, 42, 56, 9);

        assertEquals(0., new AveragePrecision(0.5).computeMetrics(empty, nonEmpty), 0.0001);
        assertEquals(0., new AveragePrecision(0.5).computeMetrics(nonEmpty, empty), 0.0001);
        assertEquals(1, new AveragePrecision(0.5).computeMetrics(empty, empty), 0.0001);
    }

    @Test
    public void testNonOverlapping(){
        long[] dims = {64,64};
        final Img<IntType> groundtruth = ArrayImgs.ints( dims );
        final Img<IntType> prediction = ArrayImgs.ints( dims );

        // paint
        SegmentationMetricsTestHelper.paintRectangle(groundtruth, 12, 5, 25, 13, 9);
        SegmentationMetricsTestHelper.paintRectangle(prediction, 28, 15, 42, 32, 12);

        assertEquals(0., new AveragePrecision(0.5).computeMetrics(groundtruth, prediction), 0.0001);
    }

    @Test
    public void testOverlapping(){
        long[] dims = {32,32};
        final Img<IntType> groundtruth = ArrayImgs.ints( dims );
        final Img<IntType> prediction = ArrayImgs.ints( dims );

        int[] gtRect1 = {2, 2, 11, 11};
        int[] predRect1 = {6, 6, 15, 15};

        int[] gtRect2 = {15, 15, 20, 20};
        int[] predRect2 = {16, 16, 22, 22};

        // paint
        SegmentationMetricsTestHelper.paintRectangle(groundtruth, gtRect1, 9);
        SegmentationMetricsTestHelper.paintRectangle(prediction, predRect1, 5);

        SegmentationMetricsTestHelper.paintRectangle(groundtruth, gtRect2, 2);
        SegmentationMetricsTestHelper.paintRectangle(prediction, predRect2, 8);

        double iou1 = getIoUBetweenRectangles(gtRect1, predRect1);
        double iou2 = getIoUBetweenRectangles(gtRect2, predRect2);
        for(double t = 0.1; t < 0.9; t += 0.05) {

            double m;
            if(Double.compare(iou1, t) >= 0 && Double.compare(iou2, t) >= 0){ // both are matched
                m = 1;
            } else if((Double.compare(iou1, t) < 0 && Double.compare(iou2, t) < 0)){ // none is matched
                m = 0.;
            } else { // one is matched
                m = 1. / 3.;
            }

            assertEquals(m, new AveragePrecision(t).computeMetrics(groundtruth, prediction), 0.0001);
        }
    }


    @Test
    public void testLocalIoUOverlapping(){
        long[] dims = {15, 15};
        final Img<IntType> gt = ArrayImgs.ints( dims );
        final Img<IntType> pred = ArrayImgs.ints( dims );

        int[] gtRect = {2,4,8,6};
        int[] predRect = {3,4,11,5};

        // paint
        SegmentationMetricsTestHelper.paintRectangle(gt, gtRect, 9);
        SegmentationMetricsTestHelper.paintRectangle(pred, predRect, 5);

        // confusion metrics
        ConfusionMatrix<IntType, IntType> cm = new ConfusionMatrix<>(gt, pred);

        // Metrics
        AveragePrecision metrics = new AveragePrecision();

        // values
        double localIoU = getIoUBetweenRectangles(gtRect, predRect);

        // local iou
        for(double t = 0.; t < 1.; t+=0.05) {
            double iou = localIoU >= t ? localIoU : 0;
            assertEquals(iou, metrics.getLocalIoUScore(cm,0, 0, t), 0.00001);
        }
    }

    @Test
    public void testLocalIoUDisjoint(){
        long[] dims = {15, 15};
        final Img<IntType> gt = ArrayImgs.ints( dims );
        final Img<IntType> pred = ArrayImgs.ints( dims );

        int[] gtRect = {2,4,8,6};
        int[] predRect = {9,7,14,9};

        // paint
        SegmentationMetricsTestHelper.paintRectangle(gt, gtRect, 9);
        SegmentationMetricsTestHelper.paintRectangle(pred, predRect, 5);

        // confusion metrics
        ConfusionMatrix<IntType, IntType> cm = new ConfusionMatrix<>(gt, pred);

        // Metrics
        AveragePrecision metrics = new AveragePrecision();

        assertEquals(0., metrics.getLocalIoUScore(cm,0, 0, 0.), 0.00001);
    }

    @Test
    public void testLocalIoUEmptyGT(){
        long[] dims = {15, 15};
        final Img<IntType> gt = ArrayImgs.ints( dims );
        final Img<IntType> pred = ArrayImgs.ints( dims );

        int[] predRect = {9,7,14,9};

        // paint
        SegmentationMetricsTestHelper.paintRectangle(pred, predRect, 5);

        // confusion metrics
        ConfusionMatrix<IntType, IntType> cm = new ConfusionMatrix<>(gt, pred);

        // Metrics
        AveragePrecision metrics = new AveragePrecision();

        assertEquals(0, metrics.getLocalIoUScore(cm, 0, 0, 0.), 0.00001);
    }

    @Test
    public void testLocalIoUEmptyPrediction(){
        long[] dims = {15, 15};
        final Img<IntType> gt = ArrayImgs.ints( dims );
        final Img<IntType> pred = ArrayImgs.ints( dims );

        int[] gtRect = {2,4,8,6};

        // paint
        SegmentationMetricsTestHelper.paintRectangle(gt, gtRect, 9);

        // confusion metrics
        ConfusionMatrix<IntType, IntType> cm = new ConfusionMatrix<>(gt, pred);

        // Metrics
        AveragePrecision metrics = new AveragePrecision();

        assertEquals(0, metrics.getLocalIoUScore(cm, 0, 0, 0.), 0.00001);
    }

    @Test
    public void testLocalIoUWithMoreGTLabels(){
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
        SegmentationMetricsTestHelper.paintRectangle(gt, gtRect1, gtLabel1);
        SegmentationMetricsTestHelper.paintRectangle(gt, gtRect2, gtLabel2);
        SegmentationMetricsTestHelper.paintRectangle(pred, predRect, predLabel);

        // confusion metrics
        ConfusionMatrix<IntType, IntType> cm = new ConfusionMatrix<>(gt, pred);

        // Metrics
        AveragePrecision metrics = new AveragePrecision();

        // values
        double localIoU1 = getIoUBetweenRectangles(gtRect1, predRect);
        double localIoU2 = getIoUBetweenRectangles(gtRect2, predRect);

        // gtLabel2 encountered first, gtLabel1 second
        assertEquals(localIoU1, metrics.getLocalIoUScore(cm, 1, 0, 0.), 0.00001);
        assertEquals(localIoU2, metrics.getLocalIoUScore(cm, 0, 0, 0.), 0.00001);
    }

    @Test
    public void testLocalIoUWithMorePredLabels(){
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
        SegmentationMetricsTestHelper.paintRectangle(gt, gtRect, gtLabel);
        SegmentationMetricsTestHelper.paintRectangle(pred, predRect1, predLabel1);
        SegmentationMetricsTestHelper.paintRectangle(pred, predRect2, predLabel2);

        // confusion metrics
        ConfusionMatrix<IntType, IntType> cm = new ConfusionMatrix<>(gt, pred);

        // Metrics
        AveragePrecision metrics = new AveragePrecision();

        // values
        double localIoU1 = getIoUBetweenRectangles(gtRect, predRect1);
        double localIoU2 = getIoUBetweenRectangles(gtRect, predRect2);

        // predLabel2 encountered first, predLabel1 second
        assertEquals(localIoU1, metrics.getLocalIoUScore(cm, 0, 1, 0.), 0.00001);
        assertEquals(localIoU2, metrics.getLocalIoUScore(cm, 0, 0, 0.), 0.00001);
    }

    public static double getIoUBetweenRectangles(int[] a, int[] b){
        return getIoUBetweenRectangles(a[0],a[1],a[2],a[3],b[0],b[1],b[2],b[3]);
    }

    public static double getIoUBetweenRectangles(int a_x_min, int a_y_min, int a_x_max, int a_y_max,
                                                  int b_x_min, int b_y_min, int b_x_max, int b_y_max){
        int left = Math.max(a_x_min, b_x_min);
        int right = Math.min(a_x_max, b_x_max);
        int bottom = Math.max(a_y_min, b_y_min);
        int top = Math.min(a_y_max, b_y_max);

        if(left < right && bottom < top){
            double intersection = (double) (right-left+1)*(top-bottom+1);
            double a_area = (a_x_max-a_x_min+1)*(a_y_max-a_y_min+1);
            double b_area = (b_x_max-b_x_min+1)*(b_y_max-b_y_min+1);

            return intersection / (a_area + b_area - intersection);
        } else {
            return 0.;
        }
    }

}
