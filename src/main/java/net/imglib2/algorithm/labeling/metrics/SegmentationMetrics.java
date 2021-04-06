package net.imglib2.algorithm.labeling.metrics;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.view.Views;

import java.util.*;

/**
 * Segmentation metrics computed by building a confusion matrix (intersection between labels)
 * and a cost matrix (metrics score between two labels) between the labels of a ground-truth
 * image and a prediction image.
 *
 * @author Joran Deschamps
 */
public abstract class SegmentationMetrics {

    /**
     * Compute the global metrics score between labels from a ground-truth and a prediction image.
     * The labels are represented by the pixel values. A threshold can be applied to reject pairing
     * between labels below a certain relative score.
     * <p>
     * The methods throws an exception if either of the images has intersecting labels.
     *
     * @param groundTruth Ground-truth image
     * @param prediction  Prediction image
     * @param threshold   Threshold
     * @param <T>         The type of labels assigned to the ground-truth pixels
     * @param <I>         The pixel type of the ground-truth image
     * @param <U>The      type of labels assigned to the prediction pixels
     * @param <J>         The pixel type of the prediction image
     * @return Metrics score
     */
    public <T, I extends IntegerType<I>, U, J extends IntegerType<J>> double computeMetrics(
            final ImgLabeling<T, I> groundTruth,
            final ImgLabeling<U, J> prediction,
            final double threshold
    ) {
        if (hasIntersectingLabels(groundTruth) || hasIntersectingLabels(prediction))
            throw new UnsupportedOperationException("ImgLabeling with intersecting labels are not supported.");

        return computeMetrics(groundTruth.getIndexImg(), prediction.getIndexImg(), threshold);
    }

    /**
     * Compute the global metrics score between labels from a ground-truth and a prediction image.
     * The labels are represented by the pixel values. A threshold can be applied to reject pairing
     * between labels below a certain relative score.
     *
     * @param groundTruth Ground-truth image
     * @param prediction  Prediction image
     * @param threshold   Threshold at which
     * @param <I>         The pixel type of the ground-truth image
     * @param <J>         The pixel type of the prediction image
     * @return Metrics score
     */
    public <I extends IntegerType<I>, J extends IntegerType<J>> double computeMetrics(
            RandomAccessibleInterval<I> groundTruth,
            RandomAccessibleInterval<J> prediction,
            double threshold) {

        if (!Arrays.equals(groundTruth.dimensionsAsLongArray(), prediction.dimensionsAsLongArray()))
            throw new IllegalArgumentException("Image dimensions must match.");

        if (threshold < 0 || threshold > 1)
            throw new IllegalArgumentException("Threshold must be comprised between 0 and 1.");

        // compute confusion matrix
        final ConfusionMatrix confusionMatrix = new ConfusionMatrix(groundTruth, prediction);

        // compute cost matrix
        double[][] costMatrix = computeCostMatrix(confusionMatrix, threshold);

        return computeMetrics(confusionMatrix, costMatrix, threshold);
    }

    /**
     * Compute the global metrics value.
     *
     * @param confusionMatrix Confusion matrix
     * @param costMatrix      Cost matrix
     * @param threshold       Threshold
     * @return Metrics score
     */
    protected abstract double computeMetrics(ConfusionMatrix confusionMatrix, double[][] costMatrix, double threshold);

    /**
     * Compute the cost matrix, where each element eij is the score of the local metrics between
     * the ground-truth label i and the prediction label j. If the confusion matrix has more rows
     * than columns, then empty columns (= prediction labels) are added to the cost matrix to make
     * it square. Therefore, the cost matrix can have a different shape than the confusion matrix.
     *
     * @param cM        Confusion matrix
     * @param threshold Threshold to zero the local score
     * @return Cost matrix
     */
    protected double[][] computeCostMatrix(ConfusionMatrix cM, double threshold) {
        int M = cM.getNumberGroundTruthLabels();
        int N = cM.getNumberPredictionLabels();

        // empty cost matrix
        double[][] costMatrix = new double[M][Math.max(M, N)];

        // fill in cost matrix
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < N; j++) {
                costMatrix[i][j] = computeLocalMetrics(i, j, cM, threshold);
            }
        }

        return costMatrix;
    }

    /**
     * Compute the metrics between two labels and returns its negation (-m) so as to correspond
     * to a minimum cost problem. If the positive score is smaller than the threshold, the method
     * returns 0.
     *
     * @param iGT       Index of the ground-truth label
     * @param jPred     Index of the prediction label
     * @param cM        Confusion matrix
     * @param threshold Threshold
     * @return Score between the two labels or 0 if the score is smaller than the threshold
     */
    protected double computeLocalMetrics(int iGT, int jPred, ConfusionMatrix cM, double threshold) {
        // number of true positive pixels
        double tp = cM.getIntersection(iGT, jPred);

        // size of each label (number of pixels in each label)
        double sumI = cM.getGroundTruthLabelSize(iGT);
        double sumJ = cM.getPredictionLabelSize(jPred);

        // false positives and false negatives
        double fn = sumI - tp;
        double fp = sumJ - tp;

        double iou = (tp + fp + fn) > 0 ? tp / (tp + fp + fn) : 0;

        if (iou < threshold) {
            iou = 0;
        }

        return -iou;
    }

    /**
     * A confusion matrix represent the number of pixels shared between all labels of two
     * images: ground-truth and prediction. It is backed by a 2D array, with the ground-truth
     * labels indexing the rows, and the prediction labels indexing the columns.
     *
     * @param <I> The pixel type of the ground-truth image
     * @param <J> The pixel type of the prediction image
     */
    protected class ConfusionMatrix<I extends IntegerType<I>, J extends IntegerType<J>> {

        // key = label, element = corresponding number of pixels
        final private Map<I, Integer> gtHistogram;
        final private Map<J, Integer> predHistogram;

        // labels indexed by their position in the row/column of the confusion matrix
        final private ArrayList<I> groundTruthLabels;
        final private ArrayList<J> predictionLabels;

        // rows = ground-truth labels, columns = prediction labels
        final private int[][] confusionMatrix;

        /**
         * Constructor.
         *
         * @param groundTruth Ground-truth image
         * @param prediction  Prediction image
         */
        public ConfusionMatrix(RandomAccessibleInterval<I> groundTruth, RandomAccessibleInterval<J> prediction) {

            // histograms label / number of pixels
            gtHistogram = new LinkedHashMap<>();
            predHistogram = new LinkedHashMap<>();

            final Cursor<I> cGT = Views.iterable(groundTruth).localizingCursor();
            final RandomAccess<J> cPD = prediction.randomAccess();
            while (cGT.hasNext()) {
                // update gt histogram
                I gtLabel = cGT.next();
                Integer count = gtHistogram.get(gtLabel);
                gtHistogram.put(gtLabel, count == null ? 1 : count + 1);

                // update prediction histogram
                cPD.setPosition(cGT);
                J pdLabel = cPD.get();
                count = predHistogram.get(pdLabel);
                predHistogram.put(pdLabel, count == null ? 1 : count + 1);
            }

            // remove 0 / background
            if (gtHistogram.containsKey(0)) gtHistogram.remove(0);
            if (predHistogram.containsKey(0)) predHistogram.remove(0);

            // prepare confusion matrix
            confusionMatrix = new int[gtHistogram.size()][predHistogram.size()];

            // list of labels value, index corresponds to the confusion matrix indices
            groundTruthLabels = new ArrayList<>(gtHistogram.keySet());
            predictionLabels = new ArrayList<>(predHistogram.keySet());

            // populate confusion matrix
            cGT.reset();
            while (cGT.hasNext()) {
                cGT.next();
                cPD.setPosition(cGT);

                I gtLabel = cGT.get();
                J predLabel = cPD.get();

                int i = groundTruthLabels.indexOf(gtLabel);
                int j = predictionLabels.indexOf(predLabel);
                confusionMatrix[i][j] += 1;
            }
        }

        /**
         * Return the ground-truth label at index {@code labelIndex}.
         *
         * @param labelIndex Index of the label
         * @return Label at index {@code labelIndex}
         */
        private I getGroundTruthLabel(int labelIndex) {
            return groundTruthLabels.get(labelIndex);
        }

        /**
         * Return the prediction label at index {@code labelIndex}.
         *
         * @param labelIndex Index of the label
         * @return Label at index {@code labelIndex}
         */
        private J getPredictionIndex(int labelIndex) {
            return predictionLabels.get(labelIndex);
        }

        /**
         * Return the number of pixels corresponding to the ground-truth
         * label indexed by {@code labelIndex}.
         *
         * @param labelIndex Index of the label
         * @return Number of pixels.
         */
        public int getGroundTruthLabelSize(int labelIndex) {
            I label = getGroundTruthLabel(labelIndex);
            return gtHistogram.get(label);
        }

        /**
         * Return the number of pixels corresponding to the prediction
         * label indexed by {@code labelIndex}.
         *
         * @param labelIndex Index of the label
         * @return Number of pixels.
         */
        public int getPredictionLabelSize(int labelIndex) {
            J label = getPredictionIndex(labelIndex);
            return predHistogram.get(label);
        }

        /**
         * Return the number of pixels shared by the ground-truth label
         * indexed by {@code gtIndex} and the prediction label indexed
         * by {@code predIndex}.
         *
         * @param gtIndex   Index of the ground-truth label
         * @param predIndex Index of the prediction label
         * @return Number of pixels shared by the two labels
         */
        public int getIntersection(int gtIndex, int predIndex) {
            return confusionMatrix[gtIndex][predIndex];
        }

        /**
         * Return the number of ground-truth labels.
         *
         * @return Number of ground-truth labels
         */
        public int getNumberGroundTruthLabels() {
            return confusionMatrix.length;
        }

        /**
         * Return the number of prediction labels.
         *
         * @return Number of prediction labels
         */
        public int getNumberPredictionLabels() {
            return confusionMatrix[0].length;
        }
    }

    /**
     * Return the Set of values or labels occurring in the ImgLabeling index image. Existing
     * labels that do not exist in the index image are absent from the occurring set.
     *
     * @param img Image from which to extract the occuring labels
     * @param <T> The type of labels assigned to pixels
     * @param <I> The pixel type of the backing image
     * @return Set of occurring labels
     */
    public static <T, I extends IntegerType<I>> Set<I> getOccurringLabels(ImgLabeling<T, I> img) {
        Set<I> occurringValues = new HashSet<>();
        for (I pixel : Views.iterable(img.getIndexImg())) {
            occurringValues.add(pixel);
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
    public static <T, I extends IntegerType<I>> boolean hasIntersectingLabels(ImgLabeling<T, I> img) {
        List<Set<T>> labelSets = img.getMapping().getLabelSets();
        for (I i : getOccurringLabels(img)) {
            if (labelSets.get(i.getInteger()).size() > 1) {
                return true;
            }
        }
        return false;
    }
}
