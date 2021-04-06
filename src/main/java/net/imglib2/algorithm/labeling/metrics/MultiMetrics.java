package net.imglib2.algorithm.labeling.metrics;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.labeling.metrics.assignment.MunkresKuhnAlgorithm;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.IntegerType;

import java.util.HashMap;
import java.util.Map;

/**
 * Compute a set of metrics between the labels of a ground-truth and a prediction image at a certain
 * IoU threshold. The labels are paired using minimum cost linear assignment (Munkres-Kuhn) with a
 * cost matrix corresponding to the IoU between labels, if the IoU is greater or equal to the
 * threshold. The following metrics are then calculated based on the assignment result:
 *
 * Number of true positive labels TP
 * Number of false positive labels FP
 * Number of false negative labels FN
 * Precision = TP / (TP + FP)
 * Recall = TP / (TP + FN)
 * F1 = 2*Precision*Recall / (Precision + Recall)
 * Average precision = TP / (TP + FP + FN)
 * Mean matched IoU = sum of the matched labels score / TP
 * Mean true IoU = sum of the matched labels score / number of ground-truth labels
 *
 * Adapted from matching.py
 * in https://github.com/stardist/stardist
 * by Uwe Schmidt and Martin Weigert
 *
 * @author Joran Deschamps
 */
public class MultiMetrics  extends SegmentationMetrics {

    public static String AV_PRECISION = "Average precision";
    public static String MEAN_MATCHED_IOU = "Mean matched IoU";
    public static String MEAN_TRUE_IOU = "Mean true IoU";
    public static String TP = "True positives";
    public static String FP = "False positives";
    public static String FN = "False negatives";
    public static String PRECISION = "Precision";
    public static String RECALL = "Recall";
    public static String F1 = "F1";

    private Map<String, Double> metrics;

    /**
     * Compute all metrics score by running a minimum cost linear assignment (Munkres-Kuhn)
     * to pair ground-truth and prediction labels based on the cost matrix, then by calculating
     * the metrics scores.
     *
     * @param groundTruth Ground-truth image
     * @param prediction Prediction image
     * @param threshold Threshold
     * @param <T> The type of labels assigned to the ground-truth pixels
     * @param <I> The pixel type of the ground-truth image
     * @param <U>The type of labels assigned to the prediction pixels
     * @param <J> The pixel type of the prediction image
     * @return Metrics scores summary
     */
    public <T, I extends IntegerType<I>, U, J extends IntegerType<J>> Map<String, Double> computeAllMetrics(
            final ImgLabeling<T, I> groundTruth,
            final ImgLabeling<U, J> prediction,
            final double threshold
    ) {
        if(hasIntersectingLabels(groundTruth) || hasIntersectingLabels(prediction))
            throw new UnsupportedOperationException("ImgLabeling with intersecting labels are not supported.");

        return computeAllMetrics(groundTruth.getIndexImg(), prediction.getIndexImg(), threshold);
    }

    /**
     * Compute all metrics score by running a minimum cost linear assignment (Munkres-Kuhn)
     * to pair ground-truth and prediction labels based on the cost matrix, then by calculating
     * the metrics scores.
     *
     * @param groundTruth Ground-truth image
     * @param prediction Prediction image
     * @param threshold Threshold
     * @return Metrics scores summary
     */
    public <I extends IntegerType<I>, J extends IntegerType<J>> Map<String, Double> computeAllMetrics(
            RandomAccessibleInterval<I> groundTruth,
            RandomAccessibleInterval<J> prediction,
            double threshold) {

        this.computeMetrics(groundTruth, prediction, threshold);

        return metrics;
    }

    /**
     * Compute the specified metrics score by running a minimum cost linear assignment (Munkres-Kuhn)
     * to pair ground-truth and prediction labels based on the cost matrix, then by calculating
     * the metrics score.
     *
     * All other metrics can be accessed afterwards by calling {@see getMetrics(String)}.
     *
     * @param groundTruth Ground-truth image
     * @param prediction Prediction image
     * @param threshold Threshold
     * @param metricsName Name of the metrics
     * @return Metrics score
     */
    public <I extends IntegerType<I>, J extends IntegerType<J>> double computeMetrics(
            RandomAccessibleInterval<I> groundTruth,
            RandomAccessibleInterval<J> prediction,
            double threshold,
            String metricsName) {

        this.computeMetrics(groundTruth, prediction, threshold);

        return metrics.get(metricsName);
    }

    /**
     * Returns the score of the specified metrics. The metrics must have been computed beforehand
     * otherwise the methods will throw an exception.
     *
     * @param metricsName Name of the metrics score to be returned
     * @return Metrics score
     */
    public double getMetrics(String metricsName) {
        if(metrics == null)
            throw new NullPointerException("No metrics has been calculated yet");

        return metrics.get(metricsName);
    }


    /**
     * Compute all metrics score by running a minimum cost linear assignment (Munkres-Kuhn)
     * to pair ground-truth and prediction labels based on the cost matrix, then by calculating
     * the metrics scores. It returns the average precision at the threshold:
     *
     * J = TP / (TP + FP + FN)
     *
     * Where TP is the number of ground-truth labels matched with an absolute score greater or equal
     * to the threshold, FP is the number of unassigned prediction labels and FN the number of
     * unassigned ground-truth labels.
     *
     * All other metrics can be accessed afterwards by calling {@see getMetrics(String)}.
     *
     * @param confusionMatrix Confusion matrix
     * @param costMatrix Cost matrix
     * @param threshold Threshold
     * @return Metrics score
     */
    @Override
    protected double computeMetrics(ConfusionMatrix confusionMatrix, double[][] costMatrix, double threshold) {
        metrics = new HashMap<>();
        int tp = 0;

        // Note: MunkresKuhnAlgorithm, as implemented, does not change the cost matrix
        int[][] assignment = new MunkresKuhnAlgorithm().computeAssignments(costMatrix);
        double sumIoU = 0;
        for (int i = 0; i < assignment.length; i++) {
            // cost matrix values were negative to obtain a minimum assignment problem
            // we retain only "good" assignments
            if(-costMatrix[ assignment[i][0] ][ assignment[i][1] ] >= threshold) {
                tp++;
                sumIoU += -costMatrix[ assignment[i][0] ][ assignment[i][1] ];
            }
        }

        // compute all metrics
        double fn = confusionMatrix.getNumberGroundTruthLabels() - tp;
        double fp = confusionMatrix.getNumberPredictionLabels() - tp;
        double meanMatched = sumIoU / tp;
        double meanTrue = sumIoU / confusionMatrix.getNumberGroundTruthLabels();
        double precision = (tp + fp) > 0 ? tp / (fp + fp) : 0;
        double recall = (tp + fn) > 0 ? tp / (fp + fn) : 0;
        double avPrecision = (tp + fn + fp) > 0 ? tp / (tp + fn + fp) : 0;
        double f1 = (precision + recall) > 0 ? 2*precision*recall / (precision + recall) : 0;

        // add to the map
        metrics.put(TP, (double) tp);
        metrics.put(FP, fp);
        metrics.put(FN, fn);
        metrics.put(MEAN_MATCHED_IOU, meanMatched);
        metrics.put(MEAN_TRUE_IOU, meanTrue);
        metrics.put(PRECISION, precision);
        metrics.put(RECALL, recall);
        metrics.put(AV_PRECISION, avPrecision);
        metrics.put(F1, f1);

        return avPrecision;
    }
}
