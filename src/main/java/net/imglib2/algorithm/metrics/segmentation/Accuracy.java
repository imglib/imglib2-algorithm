package net.imglib2.algorithm.metrics.segmentation;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.metrics.segmentation.assignment.MunkresKuhnAlgorithm;
import net.imglib2.type.numeric.IntegerType;

import java.util.Arrays;

/**
 * Computes the average precision at a certain threshold, corresponding to the Jaccard index (or
 * IoU) of the matched (i.e. labels with an IoU greater or equal to the threshold) and unmatched
 * labels.
 */
public class Accuracy implements SegmentationMetrics {

    private double threshold;

    public Accuracy(){
        this(0.5);
    }

    public Accuracy(double threshold){
        this.threshold = threshold;
    }

    public double getThreshold(){
        return threshold;
    }

    public Accuracy setThreshold(double threshold) {
        this.threshold = threshold;
        return this;
    }

    /**
     * Compute the average precision score between labels from a ground-truth and a prediction image in
     * which the labels are represented by the pixel values. The threshold is the minimum IoU between
     * two labels for them to be considered a potential match.
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

    protected double[][] computeCostMatrix(ConfusionMatrix cM) {
        int M = cM.getNumberGroundTruthLabels();
        int N = cM.getNumberPredictionLabels();

        // empty cost matrix
        // make sure to obtain a rectangular matrix, with Npred > Ngt, in order
        // to avoid empty assignments if using Munkres-Kuhn
        double[][] costMatrix = new double[M][Math.max(M+1, N)];

        // fill in cost matrix
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < N; j++) {
                // take the negation to obtain minimum cost assignment problem
                costMatrix[i][j] = -getLocalIoUScore(cM, i, j, threshold);
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
     * @return Score between the two labels or 0 if the score is smaller than the threshold
     */
    protected double getLocalIoUScore(ConfusionMatrix cM, int iGT, int jPred, double threshold) {
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

        return iou;
    }

    /**
     * Compute the metrics score by running a minimum cost linear assignment (Munkres-Kuhn)
     * to pair ground-truth and prediction labels based on the cost matrix, then by calculating
     * the score as the Jaccard index of the assignment:
     *
     * J = TP / (TP + FP + FN)
     *
     * Where TP is the number of ground-truth labels matched with a prediction label with an absolute
     * score (IoU) greater or equal to the threshold, FP is the number of unassigned prediction labels
     * and FN the number of unassigned ground-truth labels.
     *
     * See description of the "average precision at a specific threshold"
     * https://www.kaggle.com/c/data-science-bowl-2018/overview/evaluation
     *
     * @param confusionMatrix Confusion matrix
     * @param costMatrix Cost matrix
     * @return Metrics score
     */
    protected double computeMetrics(ConfusionMatrix confusionMatrix, double[][] costMatrix) {
        // Note: MunkresKuhnAlgorithm, as implemented, does not change the cost matrix
        int[][] assignment = new MunkresKuhnAlgorithm().computeAssignments(costMatrix);

        if(assignment.length !=0 && assignment[0].length != 0) {
            int tp = 0;

            for (int i = 0; i < assignment.length; i++) {
                // cost matrix values were negative to obtain a minimum assignment problem
                // we retain only "good" assignments
                if (-costMatrix[ assignment[i][0] ][ assignment[i][1] ] >= threshold) {
                    tp++;
                }
            }

            double fn = confusionMatrix.getNumberGroundTruthLabels() - tp;
            double fp = confusionMatrix.getNumberPredictionLabels() - tp;

            return (tp + fn + fp) > 0 ? tp / (tp + fn + fp) : 0;
        } else if (confusionMatrix.getNumberGroundTruthLabels() == 0 &&
                confusionMatrix.getNumberPredictionLabels() == 0){
            return 1.;
        }
        return 0.;
    }
}
