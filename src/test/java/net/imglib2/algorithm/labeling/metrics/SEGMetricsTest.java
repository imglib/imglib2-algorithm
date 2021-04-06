package net.imglib2.algorithm.labeling.metrics;

import net.imglib2.algorithm.labeling.metrics.old.SEG;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class SEGMetricsTest {

    private final long[] exampleIndexArrayDims = new long[] {4, 5};
    private final String[] exampleIntersectingLabels = new String[] { "A", "A,B", "C", "D", "D,E"};
    private final String[] exampleNonIntersectingLabels = new String[] { "A", "A,B", "C", "D", "E"};
    private final int[] exampleIndexArray = new int[] {
            1, 0, 0, 0, 0,
            0, 1, 0, 5, 0,
            0, 0, 0, 3, 3,
            0, 0, 3, 3, 0
    };

    @Test(expected = UnsupportedOperationException.class)
    public void testSEGException(){
        final Img<IntType> img = ArrayImgs.ints(exampleIndexArray, exampleIndexArrayDims);
        final ImgLabeling<String, IntType> labeling = ImgLabeling.fromImageAndLabelSets(img, SegmentationHelper.getLabelingSet(exampleIntersectingLabels));

        new SEG().computeMetrics(labeling, labeling, 0.5);
    }

    @Test
    public void testSEGNoException(){
        final Img<IntType> img = ArrayImgs.ints(exampleIndexArray, exampleIndexArrayDims);
        final ImgLabeling<String, IntType> labeling = ImgLabeling.fromImageAndLabelSets(img, SegmentationHelper.getLabelingSet(exampleNonIntersectingLabels));

        new SEG().computeMetrics(labeling, labeling, 0.5);
    }

    @Test
    public void testSEGIdentity(){
        long[] dims = {64,64};
        final Img<IntType> img = ArrayImgs.ints( dims );

        // paint
        SegmentationHelper.paintRectangle(img, 12, 28, 42, 56, 9);
        SegmentationHelper.paintRectangle(img, 43, 9, 52, 18, 12);

        assertEquals(1., new SEG().computeMetrics(img, img, 0.5), 0.0001);
    }

    @Test
    public void testSEGEmpty(){
        long[] dims = {64,64};
        final Img<IntType> nonEmpty = ArrayImgs.ints( dims );
        final Img<IntType> empty = ArrayImgs.ints( dims );

        // paint
        SegmentationHelper.paintRectangle(nonEmpty, 12, 28, 42, 56, 9);

        assertEquals(0., new SEG().computeMetrics(empty, nonEmpty, 0.5), 0.0001);
        assertEquals(0., new SEG().computeMetrics(nonEmpty, empty, 0.5), 0.0001);
        assertEquals(1., new SEG().computeMetrics(empty, empty, 0.5), 0.0001);
    }

    @Test
    public void testSEGNonOverlapping(){
        long[] dims = {64,64};
        final Img<IntType> groundtruth = ArrayImgs.ints( dims );
        final Img<IntType> prediction = ArrayImgs.ints( dims );

        // paint
        SegmentationHelper.paintRectangle(groundtruth, 12, 5, 25, 13, 9);
        SegmentationHelper.paintRectangle(prediction, 28, 15, 42, 32, 12);

        assertEquals(0., new SEG().computeMetrics(groundtruth, prediction,0.5), 0.0001);
    }

    @Test
    public void testSEGSimpleOverlapping(){
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

        assertEquals(seg, new SEG().computeMetrics(groundtruth, prediction, min_overlap), 0.0001);
    }

    @Test
    public void testSEGDoubleOverlapping(){
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
        SegmentationHelper.paintRectangle(prediction, min_pred, min_pred, max_pred, max_pred, 12);
        SegmentationHelper.paintRectangle(groundtruth, min_gt2, min_gt2, max_gt2, max_gt2, 2);
        SegmentationHelper.paintRectangle(prediction, min_pred2, min_pred2, max_pred2, max_pred2, 5);

        double min_overlap = 0.5;
        double seg1 = getSEGBetweenRectangles(min_gt, min_gt, max_gt, max_gt, min_pred, min_pred, max_pred, max_pred, min_overlap);
        double seg2 = getSEGBetweenRectangles(min_gt2, min_gt2, max_gt2, max_gt2, min_pred2, min_pred2, max_pred2, max_pred2, min_overlap);
        double seg = (seg1 + seg2)/2;

        assertEquals(seg, new SEG().computeMetrics(groundtruth, prediction, min_overlap), 0.0001);
    }

    @Test
    public void testSEGCutOff(){
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
            assertEquals(seg, new SEG().computeMetrics(groundtruth, prediction, overlap), 0.0001);
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
