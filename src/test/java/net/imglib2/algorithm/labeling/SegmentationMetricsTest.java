package net.imglib2.algorithm.labeling;

import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SegmentationMetricsTest {

    @Test
    public void testSEGIdentity(){
        long[] dims = {64,64};
        final Img<IntType> img = ArrayImgs.ints( dims );

        // paint
        paintRectangle(img, 12, 28, 42, 56, 9);
        paintRectangle(img, 43, 9, 52, 18, 12);

        ImgLabeling imgLabeling = new ImgLabeling<>(img);

        assertEquals(1., SegmentationMetrics.computeSEG(imgLabeling, imgLabeling, 0.5), 0.0001);
    }

    @Test
    public void testSEGEmptyPrediction(){
        long[] dims = {64,64};
        final Img<IntType> groundtruth = ArrayImgs.ints( dims );
        final Img<IntType> empty = ArrayImgs.ints( dims );

        // paint
        paintRectangle(groundtruth, 12, 28, 42, 56, 9);

        assertEquals(0., SegmentationMetrics.computeSEG(new ImgLabeling(groundtruth), new ImgLabeling(empty), 0.5), 0.0001);
    }

    @Test
    public void testSEGNonOverlapping(){
        long[] dims = {64,64};
        final Img<IntType> groundtruth = ArrayImgs.ints( dims );
        final Img<IntType> prediction = ArrayImgs.ints( dims );

        // paint
        paintRectangle(groundtruth, 12, 28, 42, 56, 9);
        paintRectangle(prediction, 12, 28, 42, 56, 12);

        assertEquals(0., SegmentationMetrics.computeSEG(new ImgLabeling(groundtruth), new ImgLabeling(prediction),0.5), 0.0001);
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
        paintRectangle(groundtruth, min_gt, min_gt, max_gt, max_gt, 9);
        paintRectangle(prediction, min_pred, min_pred, max_pred, max_pred, 12);

        double min_overlap = 0.5;
        double seg = getSEGBetweenRectangles(min_gt, min_gt, max_gt, max_gt, min_pred, min_pred, max_pred, max_pred, min_overlap);

        assertEquals(seg, SegmentationMetrics.computeSEG(new ImgLabeling(groundtruth), new ImgLabeling(prediction), min_overlap), 0.0001);
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
        paintRectangle(groundtruth, min_gt, min_gt, max_gt, max_gt, 9);
        paintRectangle(prediction, min_pred, min_pred, max_pred, max_pred, 12);
        paintRectangle(groundtruth, min_gt2, min_gt2, max_gt2, max_gt2, 2);
        paintRectangle(prediction, min_pred2, min_pred2, max_pred2, max_pred2, 5);

        double min_overlap = 0.5;
        double seg1 = getSEGBetweenRectangles(min_gt, min_gt, max_gt, max_gt, min_pred, min_pred, max_pred, max_pred, min_overlap);
        double seg2 = getSEGBetweenRectangles(min_gt2, min_gt2, max_gt2, max_gt2, min_pred2, min_pred2, max_pred2, max_pred2, min_overlap);
        double seg = (seg1 + seg2)/2;

        assertEquals(seg, SegmentationMetrics.computeSEG(new ImgLabeling(groundtruth), new ImgLabeling(prediction), min_overlap), 0.0001);
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
        paintRectangle(groundtruth, min_gt, min_gt, max_gt, max_gt, 9);
        paintRectangle(prediction, min_pred, min_pred, max_pred, max_pred, 12);

        for(double overlap = 0.1; overlap < 0.9; overlap += 0.05) {
            double seg = getSEGBetweenRectangles(min_gt, min_gt, max_gt, max_gt, min_pred, min_pred, max_pred, max_pred, overlap);
            assertEquals(seg, SegmentationMetrics.computeSEG(new ImgLabeling(groundtruth), new ImgLabeling(prediction), overlap), 0.0001);
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

    private static void paintRectangle(Img<IntType> img, int min_x, int min_y, int max_x, int max_y, int value){
        long[] interval = {min_x, min_y, max_x, max_y};
        IntervalView<IntType> intView = Views.interval(img, Intervals.createMinMax(interval));
        Cursor<IntType> cur = intView.cursor();
        while(cur.hasNext()){
            cur.next().set(value);
        }
    }

}
