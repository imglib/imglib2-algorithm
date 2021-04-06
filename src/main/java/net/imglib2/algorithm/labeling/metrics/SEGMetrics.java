package net.imglib2.algorithm.labeling.metrics;

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
     * Compute the metrics score as the sum of the IoU for all pairs of labels with at least
     * {@code threshold} percent overlap, divided by the number of ground-truth labels.
     *
     * @param confusionMatrix Confusion matrix
     * @param costMatrix Cost matrix
     * @param threshold Threshold
     * @return Metrics score
     */
    @Override
    protected double computeMetrics(ConfusionMatrix confusionMatrix, double[][] costMatrix, double threshold) {
        final int M = costMatrix.length;
        final int N = costMatrix[0].length;

        double precision = 0.;
        for(int i=0; i<M; i++){
            for(int j=0; j<N; j++){
                precision += costMatrix[i][j];
            }
        }

        return precision / (double) M;
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

            return intersection / (intersection + gtSize + predSize);
        } else {
            return 0.;
        }
    }
}
