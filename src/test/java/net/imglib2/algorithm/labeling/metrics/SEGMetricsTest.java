package net.imglib2.algorithm.labeling.metrics;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.IntType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class SEGMetricsTest {

    @Test
    public void testIdentity(){
        long[] dims = {64,64};
        final Img<IntType> img = ArrayImgs.ints( dims );

        // paint
        SegmentationHelper.paintRectangle(img, 12, 28, 42, 56, 9);
        SegmentationHelper.paintRectangle(img, 43, 9, 52, 18, 12);

        assertEquals(1., new SEGMetrics().computeMetrics(img, img, 0.5), 0.0001);
    }

    @Test
    public void testIdentical(){
        long[] dims = {64,64};
        final Img<IntType> img = ArrayImgs.ints( dims );
        final Img<IntType> img2 = ArrayImgs.ints( dims );

        // paint
        SegmentationHelper.paintRectangle(img, 12, 28, 42, 56, 9);
        SegmentationHelper.paintRectangle(img2, 12, 28, 42, 56, 2);
        SegmentationHelper.paintRectangle(img, 43, 9, 52, 18, 12);
        SegmentationHelper.paintRectangle(img2, 43, 9, 52, 18, 10);

        assertEquals(1., new SEGMetrics().computeMetrics(img, img2, 0.5), 0.0001);
    }

    @Test
    public void testEmpty(){
        long[] dims = {64,64};
        final Img<IntType> nonEmpty = ArrayImgs.ints( dims );
        final Img<IntType> empty = ArrayImgs.ints( dims );

        // paint
        SegmentationHelper.paintRectangle(nonEmpty, 12, 28, 42, 56, 9);

        assertEquals(0., new SEGMetrics().computeMetrics(empty, nonEmpty, 0.5), 0.0001);
        assertEquals(0., new SEGMetrics().computeMetrics(nonEmpty, empty, 0.5), 0.0001);
        assertEquals(0., new SEGMetrics().computeMetrics(empty, empty, 0.5), 0.0001);
    }

    @Test
    public void testNonOverlapping(){
        long[] dims = {64,64};
        final Img<IntType> groundtruth = ArrayImgs.ints( dims );
        final Img<IntType> prediction = ArrayImgs.ints( dims );

        // paint
        SegmentationHelper.paintRectangle(groundtruth, 12, 5, 25, 13, 9);
        SegmentationHelper.paintRectangle(prediction, 28, 15, 42, 32, 12);

        assertEquals(0., new SEGMetrics().computeMetrics(groundtruth, prediction,0.5), 0.0001);
    }

    @Test
    public void testSimpleOverlapping(){
        long[] dims = {16,16};
        final Img<IntType> groundtruth = ArrayImgs.ints( dims );
        final Img<IntType> prediction = ArrayImgs.ints( dims );

        int min_gt = 2;
        int max_gt = 11;
        int min_pred = min_gt+1;
        int max_pred = max_gt+1;

        // paint
        SegmentationHelper.paintRectangle(groundtruth, min_gt, min_gt, max_gt, max_gt, 9);
        SegmentationHelper.paintRectangle(prediction, min_pred, min_pred, max_pred, max_pred, 12);

        double min_overlap = 0.5;
        double seg = getSEGBetweenRectangles(min_gt, min_gt, max_gt, max_gt, min_pred, min_pred, max_pred, max_pred, min_overlap);

        assertEquals(seg, new SEGMetrics().computeMetrics(groundtruth, prediction, min_overlap), 0.0001);
    }

    @Test
    public void testDoubleOverlapping(){
        long[] dims = {32,32};
        final Img<IntType> groundtruth = ArrayImgs.ints( dims );
        final Img<IntType> prediction = ArrayImgs.ints( dims );

        int min_gt = 2;
        int max_gt = 11;
        int min_pred = min_gt+1;
        int max_pred = max_gt+1;

        int min_gt2 = 15;
        int max_gt2 = 20;
        int min_pred2 = min_gt+1;
        int max_pred2 = max_gt+1;

        // paint
        SegmentationHelper.paintRectangle(groundtruth, min_gt, min_gt, max_gt, max_gt, 9);
        SegmentationHelper.paintRectangle(prediction, min_pred, min_pred, max_pred, max_pred, 5);
        SegmentationHelper.paintRectangle(groundtruth, min_gt2, min_gt2, max_gt2, max_gt2, 2);
        SegmentationHelper.paintRectangle(prediction, min_pred2, min_pred2, max_pred2, max_pred2, 8);

        double min_overlap = 0.5;
        double seg1 = getSEGBetweenRectangles(min_gt, min_gt, max_gt, max_gt, min_pred, min_pred, max_pred, max_pred, min_overlap);
        double seg2 = getSEGBetweenRectangles(min_gt2, min_gt2, max_gt2, max_gt2, min_pred2, min_pred2, max_pred2, max_pred2, min_overlap);
        double seg = (seg1 + seg2)/2;

        assertEquals(seg, new SEGMetrics().computeMetrics(groundtruth, prediction, min_overlap), 0.0001);
    }

    @Test
    public void testCutOff(){
        long[] dims = {16,16};
        final Img<IntType> groundtruth = ArrayImgs.ints( dims );
        final Img<IntType> prediction = ArrayImgs.ints( dims );

        int min_gt = 2;
        int max_gt = 11;
        int min_pred = min_gt+3;
        int max_pred = max_gt+1;

        // paint
        SegmentationHelper.paintRectangle(groundtruth, min_gt, min_gt, max_gt, max_gt, 9);
        SegmentationHelper.paintRectangle(prediction, min_pred, min_pred, max_pred, max_pred, 12);

        for(double overlap = 0.1; overlap < 0.9; overlap += 0.05) {
            double seg = getSEGBetweenRectangles(min_gt, min_gt, max_gt, max_gt, min_pred, min_pred, max_pred, max_pred, overlap);
            assertEquals(seg, new SEGMetrics().computeMetrics(groundtruth, prediction, overlap), 0.0001);
        }
    }

    private static double getSEGBetweenRectangles(int a_x_min, int a_y_min, int a_x_max, int a_y_max,
                                                  int b_x_min, int b_y_min, int b_x_max, int b_y_max, double min_overlap){
        int left = Math.max(a_x_min, b_x_min);
        int right = Math.min(a_x_max, b_x_max);
        int bottom = Math.max(a_y_min, b_y_min);
        int top = Math.min(a_y_max, b_y_max);

        if(left < right && bottom < top){
            double intersection = (double) (right-left+1)*(top-bottom+1);
            double a_area = (double) (a_x_max-a_x_min+1)*(a_y_max-a_y_min+1);
            double b_area = (double) (b_x_max-b_x_min+1)*(b_y_max-b_y_min+1);

            if(intersection / a_area > min_overlap){
                return intersection / (a_area + b_area - intersection);
            } else {
                return 0.;
            }
        } else {
            return 0.;
        }
    }
}
