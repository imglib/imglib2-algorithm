package net.imglib2.algorithm.metrics.segmentation;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.integer.IntType;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

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

    @Test
    public void testStaticMethods(){
        final Img<IntType> img = ArrayImgs.ints(exampleIndexArray, exampleIndexArrayDims);
        final ImgLabeling<String, IntType> labelingIntersect = ImgLabeling.fromImageAndLabelSets(img, getLabelingSet(exampleIntersectingLabels));
        final ImgLabeling<String, IntType> labelingNonIntersect = ImgLabeling.fromImageAndLabelSets(img, getLabelingSet(exampleNonIntersectingLabels));

        Set<IntType> occIntersect = SegmentationMetrics.getOccurringLabelSets(labelingIntersect);
        assertEquals(3, occIntersect.size());

        for(IntType it: occIntersect){
            int i = it.getInteger();
            assertTrue(i == 1 || i == 3 || i == 5);
        }

        assertTrue(SegmentationMetrics.hasIntersectingLabels(labelingIntersect));
        assertFalse(SegmentationMetrics.hasIntersectingLabels(labelingNonIntersect));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testException(){
        final Img<IntType> img = ArrayImgs.ints(exampleIndexArray, exampleIndexArrayDims);
        final ImgLabeling<String, IntType> labeling = ImgLabeling.fromImageAndLabelSets(img, getLabelingSet(exampleIntersectingLabels));
        final ImgLabeling<String, IntType> labeling2 = ImgLabeling.fromImageAndLabelSets(img, getLabelingSet(exampleNonIntersectingLabels));

        new DummyMetrics().computeMetrics(labeling, labeling2);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testException2(){
        final Img<IntType> img = ArrayImgs.ints(exampleIndexArray, exampleIndexArrayDims);
        final ImgLabeling<String, IntType> labeling = ImgLabeling.fromImageAndLabelSets(img, getLabelingSet(exampleNonIntersectingLabels));
        final ImgLabeling<String, IntType> labeling2 = ImgLabeling.fromImageAndLabelSets(img, getLabelingSet(exampleIntersectingLabels));

        new DummyMetrics().computeMetrics(labeling, labeling2);
    }

    @Test
    public void testNoException(){
        final Img<IntType> img = ArrayImgs.ints(exampleIndexArray, exampleIndexArrayDims);
        final ImgLabeling<String, IntType> labeling = ImgLabeling.fromImageAndLabelSets(img, getLabelingSet(exampleNonIntersectingLabels));

        new DummyMetrics().computeMetrics(labeling, labeling);
    }

    public static class DummyMetrics implements SegmentationMetrics {
        @Override
        public <I extends IntegerType<I>, J extends IntegerType<J>> double computeMetrics(RandomAccessibleInterval<I> groundTruth, RandomAccessibleInterval<J> prediction) {
            return 0;
        }
    }
}
