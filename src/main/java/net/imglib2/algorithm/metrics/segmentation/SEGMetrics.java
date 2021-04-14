package net.imglib2.algorithm.metrics.segmentation;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.IntegerType;

import java.util.Arrays;

/**
 * The SEG metrics compute the Jaccard / IoU metrics between ground-truth and prediction
 * labels (same integer value in the image) that have a percentage of overlap superior
 * to a threshold. The final score is averaged over all ground truth label, including
 * unpaired labels.
 *
 * Reference: Ulman, V., Maška, M., Magnusson, K. et al. An objective comparison of cell-tracking
 * algorithms. Nat Methods 14, 1141–1152 (2017).
 *
 * Inspired by the original implementation by Martin Maška and Vladimír Ulman:
 * https://github.com/CellTrackingChallenge/CTC-FijiPlugins
 *
 * @author Joran Deschamps
 */
public class SEGMetrics implements SegmentationMetrics {

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
    public <I extends IntegerType<I>, J extends IntegerType<J>> double computeMetrics(
            RandomAccessibleInterval<I> groundTruth,
            RandomAccessibleInterval<J> prediction) {

        if (!Arrays.equals(groundTruth.dimensionsAsLongArray(), prediction.dimensionsAsLongArray()))
            throw new IllegalArgumentException("Image dimensions must match.");

        // compute confusion matrix
        final ConfusionMatrix confusionMatrix = new ConfusionMatrix(groundTruth, prediction);

        // compute cost matrix
        double[][] costMatrix = computeCostMatrix(confusionMatrix);

        return computeMetrics(confusionMatrix, costMatrix);
    }


    /**
     * Compute the metrics score as the sum of the IoU for all pairs of labels with at least
     * {@code threshold} percent overlap, divided by the number of ground-truth labels. If
     * there is no ground-truth label, nor prediction label, the metrics score is 1.
     *
     * @param confusionMatrix Confusion matrix
     * @param costMatrix Cost matrix
     * @return Metrics score
     */
    private double computeMetrics(ConfusionMatrix confusionMatrix, double[][] costMatrix) {
        if(costMatrix.length != 0 && costMatrix[0].length != 0) {
            final int M = costMatrix.length;
            final int N = costMatrix[0].length;

            double precision = 0.;
            for (int i = 0; i < M; i++) {
                for(int j=0; j < N; j++) {
                    precision += costMatrix[i][j];
                }
            }

            return precision / (double) M;
        } else if (confusionMatrix.getNumberGroundTruthLabels() == 0 &&
                confusionMatrix.getNumberPredictionLabels() == 0){
            return 1.;
        }
        return 0.;
    }

    private double[][] computeCostMatrix(ConfusionMatrix cM) {
        int M = cM.getNumberGroundTruthLabels();
        int N = cM.getNumberPredictionLabels();

        // empty cost matrix
        // make sure to obtain a rectangular matrix, with Npred > Ngt, in order
        // to avoid empty assignments if using Munkres-Kuhn
        double[][] costMatrix = new double[M][Math.max(M+1, N)];

        // fill in cost matrix
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < N; j++) {
                costMatrix[i][j] = getLocalIoUScore(cM, i, j);
            }
        }

        return costMatrix;
    }

    /**
     * Compute the percentage of overlap between two labels and returns the IoU if the overlap
     * is greater or equal to the threshold, 0 otherwise.
     *
     * @param cM Confusion matrix
     * @param i Index of the ground-truth label
     * @param j Index of the prediction label
     * @return IoU between the two labels or 0 if their overlap is smaller than the threshold
     */
    private double getLocalIoUScore(ConfusionMatrix cM, int i, int j) {
        double intersection = cM.getIntersection(i, j);
        double gtSize = cM.getGroundTruthLabelSize(i);

        double overlap = intersection / gtSize;
        if (overlap > 0.5) {
            double predSize = cM.getPredictionLabelSize(j);

            return intersection / (gtSize + predSize - intersection);
        } else {
            return 0.;
        }
    }

}
