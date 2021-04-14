package net.imglib2.algorithm.metrics.segmentation;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.view.Views;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 *
 * @author Joran Deschamps
 */
@FunctionalInterface
public interface SegmentationMetrics {

    /**
     * Compute the global metrics score between labels from a ground-truth and a prediction image.
     * The labels are represented by the pixel values. A threshold can be applied to reject pairing
     * between labels below a certain relative score.
     * <p>
     * The methods throws an exception if either of the images has intersecting labels.
     *
     * @param groundTruth Ground-truth image
     * @param prediction  Prediction image
     * @param <T>         The type of labels assigned to the ground-truth pixels
     * @param <I>         The pixel type of the ground-truth image
     * @param <U>The      type of labels assigned to the prediction pixels
     * @param <J>         The pixel type of the prediction image
     * @return Metrics score
     */
    default <T, I extends IntegerType<I>, U, J extends IntegerType<J>> double computeMetrics(
            final ImgLabeling<T, I> groundTruth,
            final ImgLabeling<U, J> prediction
    ) {
        if (hasIntersectingLabels(groundTruth) || hasIntersectingLabels(prediction))
            throw new UnsupportedOperationException("ImgLabeling with intersecting labels are not supported.");

        return computeMetrics(groundTruth.getIndexImg(), prediction.getIndexImg());
    }

    /**
     * Compute the global metrics score between labels from a ground-truth and a prediction image in
     * which the labels are represented by the pixel values. The threshold can be used to reject pairing
     * between labels.
     *
     * @param groundTruth Ground-truth image
     * @param prediction  Prediction image
     * @param <I>         The pixel type of the ground-truth image
     * @param <J>         The pixel type of the prediction image
     * @return Metrics score
     */
    <I extends IntegerType<I>, J extends IntegerType<J>> double computeMetrics(
            RandomAccessibleInterval<I> groundTruth,
            RandomAccessibleInterval<J> prediction
    );


    /**
     * Return the Set of values or labels occurring in the ImgLabeling index image. Existing
     * labels that do not exist in the index image are absent from the occurring set.
     *
     * @param img Image from which to extract the occuring labels
     * @param <T> The type of labels assigned to pixels
     * @param <I> The pixel type of the backing image
     * @return Set of occurring labels
     */
    static <T, I extends IntegerType<I>> Set<I> getOccurringLabelSets(ImgLabeling<T, I> img) {
        Set<I> occurringValues = new HashSet<>();
        for (I pixel : Views.iterable(img.getIndexImg())) {
            if(pixel.getInteger() > 0) occurringValues.add(pixel.copy());
        }

        return occurringValues;
    }

    /**
     * Test if intersecting labels exist in the image labeling. Two labels intersect if there
     * is at least one pixel labeled with the two labels.
     *
     * @param img Image
     * @param <T> The type of labels assigned to pixels
     * @param <I> The pixel type of the backing image
     * @return True if the labeling has intersection labels, false otherwise.
     */
    static <T, I extends IntegerType<I>> boolean hasIntersectingLabels(ImgLabeling<T, I> img) {
        List<Set<T>> labelSets = img.getMapping().getLabelSets();
        for (I i : getOccurringLabelSets(img)) {
            if (labelSets.get(i.getInteger()).size() > 1) {
                return true;
            }
        }
        return false;
    }
}
