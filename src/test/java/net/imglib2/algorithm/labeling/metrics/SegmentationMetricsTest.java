package net.imglib2.algorithm.labeling.metrics;

import net.imglib2.algorithm.labeling.metrics.SEGMetrics;
import net.imglib2.algorithm.labeling.metrics.SegmentationHelper;
import net.imglib2.algorithm.labeling.metrics.SegmentationMetrics;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    public void testComputeLocalMetrics(){
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

        

    }



    private class DummyMetrics extends SegmentationMetrics{

        @Override
        protected double computeMetrics(ConfusionMatrix confusionMatrix, double[][] costMatrix, double threshold) {
            return 0;
        }
    }
}
