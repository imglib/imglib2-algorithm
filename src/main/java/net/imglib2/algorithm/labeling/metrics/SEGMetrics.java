package net.imglib2.algorithm.labeling.metrics;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.IntegerType;

import java.util.stream.IntStream;

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
public class SEGMetrics extends SegmentationMetrics {


    /**
     * Compute the SEG metrics score between labels from a ground-truth and a prediction image in
     * which the labels are represented by the pixel values. Only labels with an overlap greater or
     * equal to the threshold are considered potential matches.
     *
     * A threshold of 0.5 is recommended. For lower thresholds and in case of multiple candidates
     * for a match with a ground-truth label, then the maximum IoU is considered a match.
     *
     * @param groundTruth Ground-truth image
     * @param prediction  Prediction image
     * @param threshold   Threshold at which pairing between labels is rejected
     * @param <I>         The pixel type of the ground-truth image
     * @param <J>         The pixel type of the prediction image
     * @return Metrics score
     */
    @Override
    protected <I extends IntegerType<I>, J extends IntegerType<J>> double computeMetrics(
            RandomAccessibleInterval<I> groundTruth,
            RandomAccessibleInterval<J> prediction,
            double threshold) {
        return super.computeMetrics(groundTruth, prediction, threshold);
    }

    /**
     * Compute the metrics score as the sum of the IoU for all pairs of labels with at least
     * {@code threshold} percent overlap, divided by the number of ground-truth labels. If
     * there is no ground-truth label, nor prediction label, the metrics score is 1.
     *
     * @param confusionMatrix Confusion matrix
     * @param costMatrix Cost matrix
     * @param threshold Threshold
     * @return Metrics score
     */
    @Override
    protected double computeMetrics(ConfusionMatrix confusionMatrix, double[][] costMatrix, double threshold) {
        if(costMatrix.length != 0 && costMatrix[0].length != 0) {
            final int M = costMatrix.length;
            final int N = costMatrix[0].length;

            double precision = 0.;
            for (int i = 0; i < M; i++) {
                // TODO is this correct when threshold is not 0.5? For t=0.5 we expect at max one match, but what about t<.5?

                // get maximum over the row and add to precision
                final int index = i;
                precision += IntStream.range(0, N).mapToDouble(j -> costMatrix[index][j]).max().getAsDouble();
            }

            return precision / (double) M;
        } else if (confusionMatrix.getNumberGroundTruthLabels() == 0 &&
                confusionMatrix.getNumberPredictionLabels() == 0){
            return 1.;
        }
        return 0.;
    }

    /**
     * Compute the percentage of overlap between two labels and returns the IoU if the overlap
     * is greater or equal to the threshold, 0 otherwise.
     *
     * @param iGT Index of the ground-truth label
     * @param jPred Index of the prediction label
     * @param cM Confusion matrix
     * @param threshold Threshold
     * @return IoU between the two labels or 0 if their overlap is smaller than the threshold
     */
    @Override
    protected double computeLocalMetrics(int iGT, int jPred, ConfusionMatrix cM, double threshold) {
        double intersection = cM.getIntersection(iGT, jPred);
        double gtSize = cM.getGroundTruthLabelSize(iGT);

        double overlap = intersection / gtSize;
        if(overlap >= threshold ){
            double predSize = cM.getPredictionLabelSize(jPred);

            return intersection / (gtSize + predSize - intersection);
        } else {
            return 0.;
        }
    }
}
