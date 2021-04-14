package net.imglib2.algorithm.metrics.segmentation;


import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.view.Views;

import java.util.*;

/**
 * A confusion matrix represent the number of pixels shared between all labels of two
 * images: ground-truth and prediction. It is backed by a 2D array, with the ground-truth
 * labels indexing the rows, and the prediction labels indexing the columns.
 *
 * @param <I> The pixel type of the ground-truth image
 * @param <J> The pixel type of the prediction image
 */
public class ConfusionMatrix<I extends IntegerType<I>, J extends IntegerType<J>> {

    // key = index in the matrix, element = corresponding number of pixels
    final private ArrayList<Integer> gtCMHistogram;
    final private ArrayList<Integer> predCMHistogram;

    // rows = ground-truth labels, columns = prediction labels
    final private int[][] confusionMatrix;

    /**
     * Constructor.
     *
     * @param groundTruth Ground-truth image
     * @param prediction  Prediction image
     */
    public ConfusionMatrix(RandomAccessibleInterval<I> groundTruth, RandomAccessibleInterval<J> prediction) {

        // histograms: label / number of pixels
        final Map<Integer, Integer> gtHistogram = new LinkedHashMap<>();
        final Map<Integer, Integer> predHistogram = new LinkedHashMap<>();

        final Cursor<I> cGT = Views.iterable(groundTruth).localizingCursor();
        final RandomAccess<J> cPD = prediction.randomAccess();
        while (cGT.hasNext()) {
            // update gt histogram
            gtHistogram.compute(cGT.next().getInteger(), (k, v) -> v == null ? 1 : v+1);

            // update prediction histogram
            cPD.setPosition(cGT);
            predHistogram.compute(cPD.get().getInteger(), (k, v) -> v == null ? 1 : v+1);
        }

        // remove 0 / background
        gtHistogram.remove(0);
        predHistogram.remove(0);

        // prepare confusion matrix
        confusionMatrix = new int[gtHistogram.size()][predHistogram.size()];

        // maps: labels value / confusion matrix indices
        final LinkedHashMap<Integer, Integer> groundTruthLUT = new LinkedHashMap<>();
        gtHistogram.keySet().forEach( key -> groundTruthLUT.put(key, groundTruthLUT.size() ));

        final LinkedHashMap<Integer, Integer> predictionLUT = new LinkedHashMap<>();
        predHistogram.keySet().forEach( key -> predictionLUT.put(key, predictionLUT.size() ));

        // histograms: matrix index / number of pixels
        // LikedHashMap ensures that the order is respected and we will traverse the images in the same way
        // we will build the confusion matrix
        gtCMHistogram = new ArrayList<>(gtHistogram.values());
        predCMHistogram = new ArrayList<>(predHistogram.values());

        // populate confusion matrix
        cGT.reset();
        while (cGT.hasNext()) {
            cGT.next();
            cPD.setPosition(cGT);

            int gtLabel = cGT.get().getInteger();
            int predLabel = cPD.get().getInteger();

            // ignore background (absent from the lists)
            if(gtLabel > 0 && predLabel > 0) {
                int i = groundTruthLUT.get(gtLabel);
                int j = predictionLUT.get(predLabel);

                confusionMatrix[i][j] += 1;
            }
        }
    }

    /**
     * Return the number of pixels corresponding to the ground-truth
     * label indexed by {@code labelIndex}.
     *
     * @param labelIndex Index of the label
     * @return Number of pixels, or -1 if the index is out of bounds
     */
    public int getGroundTruthLabelSize(int labelIndex) {
        if(labelIndex < 0 || labelIndex >= gtCMHistogram.size())
            return -1;

        return gtCMHistogram.get(labelIndex);
    }

    /**
     * Return the number of pixels corresponding to the prediction
     * label indexed by {@code labelIndex}.
     *
     * @param labelIndex Index of the label
     * @return Number of pixels, or -1 if the index is out of bounds
     */
    public int getPredictionLabelSize(int labelIndex) {
        if(labelIndex < 0 || labelIndex >= predCMHistogram.size())
            return -1;

        return predCMHistogram.get(labelIndex);
    }

    /**
     * Return the number of pixels shared by the ground-truth label
     * indexed by {@code gtIndex} and the prediction label indexed
     * by {@code predIndex}.
     *
     * @param gtLabelIndex   Index of the ground-truth label
     * @param predLabelIndex Index of the prediction label
     * @return Number of pixels shared by the two labels
     */
    public int getIntersection(int gtLabelIndex, int predLabelIndex) {
        if(getNumberGroundTruthLabels() == 0 || getNumberPredictionLabels() == 0)
            return 0;

        return confusionMatrix[gtLabelIndex][predLabelIndex];
    }

    /**
     * Return the number of ground-truth labels.
     *
     * @return Number of ground-truth labels
     */
    public int getNumberGroundTruthLabels() {
        return gtCMHistogram.size();
    }

    /**
     * Return the number of prediction labels.
     *
     * @return Number of prediction labels
     */
    public int getNumberPredictionLabels() { return predCMHistogram.size(); }
}
