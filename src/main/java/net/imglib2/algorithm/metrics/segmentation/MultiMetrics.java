package net.imglib2.algorithm.metrics.segmentation;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.metrics.segmentation.assignment.MunkresKuhnAlgorithm;
import net.imglib2.type.numeric.IntegerType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

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
 * EXAMPLE USAGE // TODO
 *
 *
 * Adapted from matching.py
 * in https://github.com/stardist/stardist
 * by Uwe Schmidt and Martin Weigert
 *
 * @author Joran Deschamps
 */
public class MultiMetrics extends Accuracy {

    public enum Metrics {
        ACCURACY("Accuracy"),
        MEAN_MATCHED_IOU("Mean matched IoU"),
        MEAN_TRUE_IOU("Mean true IoU"),
        TP("True positives"),
        FP("False positives"),
        FN("False negatives"),
        PRECISION("Precision"),
        RECALL("Recall"),
        F1("F1");

        final private String name;

        Metrics(String name) {
            this.name = name;
        }

        public String getName(){
            return name;
        }

        public static Stream<Metrics> stream(){
            Metrics[] all = {ACCURACY, MEAN_MATCHED_IOU, MEAN_TRUE_IOU, TP, FP, FN, PRECISION, RECALL, F1};
            return Arrays.stream(all);
        }
    }

    private Map<Metrics, Double> metricsResult;

    private Metrics defaultMetrics;

    public MultiMetrics(){
        this(Metrics.ACCURACY, 0.5);
    }

    public MultiMetrics(double defaultThreshold){
        this(Metrics.ACCURACY, defaultThreshold);
    }

    public MultiMetrics(Metrics defaultMetrics, double defaultThreshold){
        super(defaultThreshold);
        this.defaultMetrics = defaultMetrics;
    }

    public Metrics getCurrentMetrics() {
        return defaultMetrics;
    }

    public MultiMetrics setMetrics(Metrics metrics){
        this.defaultMetrics = metrics;
        return this;
    }

    @Override
    public MultiMetrics setThreshold(double threshold) {
        return (MultiMetrics) super.setThreshold(threshold);
    }

    /**
     * Compute all metrics score by running a minimum cost linear assignment (Munkres-Kuhn)
     * to pair ground-truth and prediction labels based on the cost matrix, then by calculating
     * the metrics scores.
     *
     * @param groundTruth Ground-truth image
     * @param prediction Prediction image
     * @return Metrics scores summary
     */
    public <I extends IntegerType<I>, J extends IntegerType<J>> Map<Metrics, Double> computeAllMetrics(
            RandomAccessibleInterval<I> groundTruth,
            RandomAccessibleInterval<J> prediction) {

        this.computeMetrics(groundTruth, prediction);

        return metricsResult;
    }

    /**
     * Returns the score of the specified metrics. The metrics must have been computed beforehand
     * otherwise the methods will throw an exception.
     *
     * @param metrics Metrics score to be returned
     * @return Metrics score
     */
    public double getScore(Metrics metrics) {
        if(metricsResult == null)
            throw new NullPointerException("No metrics has been calculated yet.");

        return metricsResult.get(metrics);
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
     * @return Metrics score
     */
    @Override
    protected double computeMetrics(ConfusionMatrix confusionMatrix, double[][] costMatrix) {
        metricsResult = new HashMap<>();

        // Note: MunkresKuhnAlgorithm, as implemented, does not change the cost matrix
        int[][] assignment = new MunkresKuhnAlgorithm().computeAssignments(costMatrix);

        if(assignment.length !=0 && assignment[0].length != 0) {
            int tp = 0;
            double sumIoU = 0;

            for (int i = 0; i < assignment.length; i++) {
                // cost matrix values were negative to obtain a minimum assignment problem
                // we retain only "good" assignments, i.e. with -cost > threshold
                if (-costMatrix[assignment[i][0]][assignment[i][1]] >= getThreshold()) {
                    tp++;
                    sumIoU += -costMatrix[assignment[i][0]][assignment[i][1]];
                }
            }

            // compute all metrics
            double fn = confusionMatrix.getNumberGroundTruthLabels() - tp;
            double fp = confusionMatrix.getNumberPredictionLabels() - tp;
            double meanMatched = tp > 0 ? sumIoU / tp : 0;
            double meanTrue = sumIoU / confusionMatrix.getNumberGroundTruthLabels();
            double precision = (tp + fp) > 0 ? tp / (tp + fp) : 0;
            double recall = (tp + fn) > 0 ? tp / (tp + fn) : 0;
            double accuracy = (tp + fn + fp) > 0 ? tp / (tp + fn + fp) : 0;
            double f1 = (precision + recall) > 0 ? 2 * precision * recall / (precision + recall) : 0;

            // add to the map
            metricsResult.put(Metrics.TP, (double) tp);
            metricsResult.put(Metrics.FP, fp);
            metricsResult.put(Metrics.FN, fn);
            metricsResult.put(Metrics.MEAN_MATCHED_IOU, meanMatched);
            metricsResult.put(Metrics.MEAN_TRUE_IOU, meanTrue);
            metricsResult.put(Metrics.PRECISION, precision);
            metricsResult.put(Metrics.RECALL, recall);
            metricsResult.put(Metrics.ACCURACY, accuracy);
            metricsResult.put(Metrics.F1, f1);
        } else if (confusionMatrix.getNumberGroundTruthLabels() == 0 &&
                confusionMatrix.getNumberPredictionLabels() == 0){
            metricsResult.put(Metrics.TP, 0.);
            metricsResult.put(Metrics.FP, 0.);
            metricsResult.put(Metrics.FN, 0.);
            metricsResult.put(Metrics.MEAN_MATCHED_IOU, 1.);
            metricsResult.put(Metrics.MEAN_TRUE_IOU, 1.);
            metricsResult.put(Metrics.PRECISION, 1.);
            metricsResult.put(Metrics.RECALL, 1.);
            metricsResult.put(Metrics.ACCURACY, 1.);
            metricsResult.put(Metrics.F1, 1.);
        }else {
            double fn = confusionMatrix.getNumberGroundTruthLabels();
            double fp = confusionMatrix.getNumberPredictionLabels();

            metricsResult.put(Metrics.TP, 0.);
            metricsResult.put(Metrics.FP, fp);
            metricsResult.put(Metrics.FN, fn);
            metricsResult.put(Metrics.MEAN_MATCHED_IOU, 0.);
            metricsResult.put(Metrics.MEAN_TRUE_IOU, 0.);
            metricsResult.put(Metrics.PRECISION, 0.);
            metricsResult.put(Metrics.RECALL, 0.);
            metricsResult.put(Metrics.ACCURACY, 0.);
            metricsResult.put(Metrics.F1, 0.);
        }

        return metricsResult.get(defaultMetrics);
    }
}
