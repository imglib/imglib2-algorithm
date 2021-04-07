package net.imglib2.algorithm.labeling.metrics;

import net.imglib2.algorithm.labeling.metrics.assignment.MunkresKuhnAlgorithm;

/**
 * Computes the average precision at a certain threshold, corresponding to the Jaccard index (or
 * IoU) of the matched (i.e. labels with an IoU greater or equal to the threshold) and unmatched
 * labels.
 */
public class AveragePrecision extends SegmentationMetrics {

    /**
     * Compute the metrics score by running a minimum cost linear assignment (Munkres-Kuhn)
     * to pair ground-truth and prediction labels based on the cost matrix, then by calculating
     * the score as the Jaccard / IoU index:
     *
     * J = TP / (TP + FP + FN)
     *
     * Where TP is the number of ground-truth labels matched with an absolute score greater or equal
     * to the threshold, FP is the number of unassigned prediction labels and FN the number of
     * unassigned ground-truth labels.
     *
     * See average precision at threshold
     * https://www.kaggle.com/c/data-science-bowl-2018/overview/evaluation
     *
     * @param confusionMatrix Confusion matrix
     * @param costMatrix Cost matrix
     * @param threshold Threshold
     * @return Metrics score
     */
    @Override
    protected double computeMetrics(ConfusionMatrix confusionMatrix, double[][] costMatrix, double threshold) {
        // Note: MunkresKuhnAlgorithm, as implemented, does not change the cost matrix
        int[][] assignment = new MunkresKuhnAlgorithm().computeAssignments(costMatrix);

        if(assignment.length !=0 && assignment[0].length != 0) {
            int tp = 0;

            for (int i = 0; i < assignment.length; i++) {
                // cost matrix values were negative to obtain a minimum assignment problem
                // we retain only "good" assignments
                if (-costMatrix[assignment[i][0]][assignment[i][1]] >= threshold) {
                    tp++;
                }
            }

            double fn = confusionMatrix.getNumberGroundTruthLabels() - tp;
            double fp = confusionMatrix.getNumberPredictionLabels() - tp;

            return (tp + fn + fp) > 0 ? tp / (tp + fn + fp) : 0;
        }
        return 0.;
    }
}
