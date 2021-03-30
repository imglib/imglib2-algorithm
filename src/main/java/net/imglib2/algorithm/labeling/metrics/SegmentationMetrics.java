package net.imglib2.algorithm.labeling.metrics;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.view.Views;

import java.util.*;

/**
 * Instance segmentation metrics
 * The task at hand is to compute metrics comparing two IntType images and their classes,
 * where the pixel values indicate class membership. The reference image will be referred
 * to as the ground truth, and the other classification as the prediction. We consider here
 * that labels are not ordered, label A in the ground truth might be label B in the prediction.
 * Label 0 always corresponds to background.
 *
 * However, in the following examples and metrics definitions, label correspondences is
 * assumed to have been already established.
 *
 * Given a confusion matrix CM:
 *   |  A |  B | <- predicted labels
 * A | aa | ab |
 * B | ba | bb |
 *
 * It is interpreted for the ground truth label A as:
 *
 *   |  A  |  B  |
 * A | TPa | FNa |
 * B | FPa | TNa |
 *
 * Then the metrics are as follow:
 *
 * ----------------
 * --- Accuracy ---
 * ----------------
 * Accuracy: sum(TP) / sum(CM)
 *
 *  --> acc = (aa + bb) / (aa + ab + ba + bb)
 *
 * ---------------------
 * --- Jaccard / IOU ---
 * ---------------------
 * Jaccard / IoU: mean( TP / (TP+FP+FN) )
 *
 *  --> jacc = [ aa /(aa + ba + ab) + ( bb/(bb + ba + ab) ) ]/2
 *
 * -------------------------
 * --- Average precision ---
 * -------------------------
 * Average precision: mean( TP/(TP+FP) )
 *
 *  --> avprec = [ aa /(aa + ba) + ( bb/(bb + ab) ) ]/2
 *
 * ----------------------
 * --- Average recall ---
 * ----------------------
 * Average recall: mean( TP/(TP+FN) )
 *
 *  --> avrec = [ aa /(aa + ab) + ( bb/(bb + ba) ) ]/2
 *
 * -----------------
 * --- F1 / Dice ---
 * -----------------
 * F1 / Dice: (2 * Precision * Recall) / (Precision + Recall)
 *
 *  --> f1 = (2 * avrec * avprec) / (avrec + avprec)
 *
 * -------------
 * --- Fbeta ---
 * -------------
 * Fbeta: ((1 + beta^2) * Precision * Recall) / (beta^2 * Precision + Recall)
 *
 *  --> fbeta = ((1 + beta^2) * avprec * avrec) / (beta^2 * avprec + avrec)
 *
 * -----------
 * --- SEG ---
 * -----------
 * SEG: The SEG metrics compute the Jaccard / IoU metrics between classes that have
 * at least > beta percentage overlap, where beta is a parameter. The score is averaged
 * over all ground truth labels.
 * See: Ulman, V., Maška, M., Magnusson, K. et al. An objective comparison of cell-tracking
 * algorithms. Nat Methods 14, 1141–1152 (2017).
 * https://github.com/CellTrackingChallenge/CTC-FijiPlugins
 *
 *
 * @author Joran Deschamps
 */


// TODO : think about the followinG:

/**
 * Pixel wise measures vs Instance metrics: instance metrics just match labels and compute TP,FP,FN at the instance level
 * one can also have a criterium and include pixel numbers in those, in instance metrics the pixel numbers contribute to
 * the score that is used to match labels but not directly to the metrics value itself.
 * check stardist implementation: https://github.com/stardist/stardist/blob/810dec4727e8e8bf05bd9620f91a3a0dd70de289/stardist/matching.py#L42
 * they use linear sum assignment
 */
public class SegmentationMetrics {

    public static <T, I extends IntegerType<I>, U, J extends IntegerType<J>> double computeSEG(
            final ImgLabeling<T, I> groundTruth,
            final ImgLabeling<U, J> prediction,
            final double minOverlap
    ) {
        if(hasIntersectingLabels(groundTruth) || hasIntersectingLabels(prediction))
            throw new UnsupportedOperationException("ImgLabeling with intersecting labels are not supported.");

        return computeSEG(groundTruth.getIndexImg(), prediction.getIndexImg(), minOverlap);
    }

    public static <I extends IntegerType<I>, J extends IntegerType<J>> double computeSEG(
            final RandomAccessibleInterval<I> groundtruth,
            final RandomAccessibleInterval<J> prediction,
            final double minOverlap
    ) {

        // TODO check dimensions are equal and give an illegalargumentexception
        // TODO check for null and empty images
        // TODO check 2D or extend to stacks

        double precision = 0.;

        // histograms pixels / label
        LinkedHashMap<Integer, Integer> gtHist = new LinkedHashMap<Integer, Integer>();
        LinkedHashMap<Integer, Integer> predictionHist = new LinkedHashMap<Integer, Integer>();

        Cursor<I> cGT = Views.iterable(groundtruth).localizingCursor();
        RandomAccess<J> cPD = prediction.randomAccess();
        while (cGT.hasNext()) {
            // update gt histogram
            int gtLabel = cGT.next().getInteger();
            Integer count = gtHist.get(gtLabel);
            gtHist.put(gtLabel, count == null ? 1 : count + 1);

            // update prediction histogram
            cPD.setPosition(cGT);
            int pdLabel = cPD.get().getInteger();
            count = predictionHist.get(pdLabel);
            predictionHist.put(pdLabel, count == null ? 1 : count + 1);
        }

        // remove 0 / background
        Integer zero = new Integer(0);
        if (gtHist.containsKey(zero)) {
            gtHist.remove(zero);
        }
        if (predictionHist.containsKey(zero)) {
            predictionHist.remove(zero);
        }

        // if the histograms are empty
        if(gtHist.isEmpty() || predictionHist.isEmpty()){
            boolean result = !(gtHist.isEmpty() ^ predictionHist.isEmpty());
            return result ? 1 : 0;
        }

        // prepare confusion matrix
        final int numGtLabels = gtHist.size();
        final int numPredLabels = predictionHist.size();
        final int[] confusionMatrix = new int[numGtLabels * numPredLabels];
        final ArrayList<Integer> gtLabels = new ArrayList<Integer>(gtHist.keySet());
        final ArrayList<Integer> predLabels = new ArrayList<Integer>(predictionHist.keySet());

        // calculate intersection
        cGT.reset();
        while (cGT.hasNext()) {
            cGT.next();
            cPD.setPosition(cGT);

            int gtLabel  = cGT.get().getInteger();
            int predLabel = cPD.get().getInteger();

            // if the pixel belongs to an instance in both cases
            if (gtLabel > 0 && predLabel > 0)
                confusionMatrix[gtLabels.indexOf(gtLabel) + numGtLabels * predLabels.indexOf(predLabel)] += 1;
        }

        // for every gt label, find the pred label with >0.5
        for (int i=0; i < numGtLabels; i++) {
            int matchingLabel = -1;
            for (int j=0; j < numPredLabels; j++){

                double overlap = (double) confusionMatrix[i + numGtLabels * j] / (double) gtHist.get(gtLabels.get(i));
                if(overlap > minOverlap){
                    matchingLabel = j;
                    break;
                }
            }

            if (matchingLabel >= 0) {
                double intersection = (double) confusionMatrix[i + numGtLabels * matchingLabel];
                double n_gt = gtHist.get(gtLabels.get(i));
                double n_pred = predictionHist.get(predLabels.get(matchingLabel));

                precision += intersection / (double) (n_gt + n_pred - intersection);
            }
        }

        // TODO check original code, do they devide by number of gt labels?
        return precision / (double) numGtLabels;
    }

    public static double computePrecision(long tp, long fp){
        return tp+fp>0 ? (double) tp / (double) (tp + fp) : 0.;
    }

    public static double computeRecall(long tp, long fn){
        return tp+fn>0 ? (double) tp / (double) (tp + fn) : 0.;
    }

    public static double computeJaccard(long tp, long fp, long fn){
        return tp+fn+fp>0 ? (double) tp / (double) (tp + fp + fn) : 0.;
    }

    public static double computeF1(long tp, long fp, long fn){
        double precision = computePrecision(tp, fp);
        double recall = computeRecall(tp, fn);

        return precision+recall>0 ? 2 * precision * recall / (precision + recall) : 0.;
    }

    public static double computeF1(double precision, double recall){
        return precision+recall>0 ? 2 * precision * recall / (precision + recall) : 0.;
    }

    public static double computeFbeta(long tp, long fp, long fn, double beta){
        double precision = computePrecision(tp, fp);
        double recall = computeRecall(tp, fn);

        return beta*beta*precision+recall>0 ? (1+beta*beta) * precision * recall / (beta*beta*precision + recall) : 0.;
    }

    public static double computeFbeta(double precision, double recall, double beta){
        return beta*beta*precision+recall>0 ? (1+beta*beta) * precision * recall / (beta*beta*precision + recall) : 0.;
    }

    private class ConfusionMatrix<I extends IntegerType<I>, J extends  IntegerType<J>> {

        // IntType images
        final RandomAccessibleInterval<I> groundtruth;
        final RandomAccessibleInterval<J> prediction;

        // Label to int look-up table
        final ArrayList<Integer> gtLabelsOrder;
        final ArrayList<Integer> predLabelsOrder;

        // Confusion matrix
        final int[] confusionMatrix;

        // Label matching (max)
        final HashMap<Integer, Integer> matchingPredLabels;
        final HashMap<Integer, Integer> matchingGTLabels;

        public ConfusionMatrix(RandomAccessibleInterval<I> groundtruth, RandomAccessibleInterval<J> prediction, double iou_threshold) {

            // TODO sanity cheks

            this.groundtruth = groundtruth;
            this.prediction = prediction;

            // histograms pixels / label
            LinkedHashMap<Integer,Integer> gtHist = new LinkedHashMap<Integer,Integer>();
            LinkedHashMap<Integer,Integer> predictionHist = new LinkedHashMap<Integer,Integer>();

            Cursor<I> cGT = Views.iterable(groundtruth).localizingCursor();
            RandomAccess<J> cPD = prediction.randomAccess();
            while(cGT.hasNext()){
                // update gt histogram
                int gtLabel = cGT.next().getInteger();
                Integer count = gtHist.get(gtLabel);
                gtHist.put(gtLabel, count == null ? 1 : count+1);

                // update prediction histogram
                cPD.setPosition(cGT);
                int pdLabel = cPD.get().getInteger();
                count = predictionHist.get(pdLabel);
                predictionHist.put(pdLabel, count == null ? 1 : count+1);
            }

            // prepare confusion matrix
            final int numGtLabels = gtHist.size();
            final int numPredLabels = predictionHist.size();
            confusionMatrix = new int[numGtLabels * numPredLabels];
            gtLabelsOrder = new ArrayList<Integer>(gtHist.keySet());
            predLabelsOrder = new ArrayList<Integer>(predictionHist.keySet());

            // populate confusion matrix
            cGT.reset();
            while (cGT.hasNext()) {
                cGT.next();
                cPD.setPosition(cGT);

                int gtLabel  = cGT.get().getInteger();
                int predLabel = cPD.get().getInteger();

                confusionMatrix[gtLabelsOrder.indexOf(gtLabel) + numGtLabels * predLabelsOrder.indexOf(predLabel)] += 1;
            }

            // match labels with max intersection
            matchingPredLabels = new HashMap<Integer, Integer>();
            matchingGTLabels = new HashMap<Integer, Integer>();

            // TODO linear assignment instead of first found? what happens when one prediction matches well with two gt? or the inverse?


            // first find maximum intersect
            for (int i=0; i < numGtLabels; i++) {

                int matchingLabel = -1;
                int matchingMax = -1;
                for (int j=0; j < numPredLabels; j++){
                    int overlap = confusionMatrix[i + numGtLabels * j];

                    // TODO
                }
            }
        }
    }

    // TODO: would this belong to imglabeling (imglib2-roi)?
    private static <T, I extends IntegerType<I>> Set<Integer> getOccurringValues(ImgLabeling<T, I> img){
        Set<Integer> occurringValues = new HashSet<>();
        for(I pixel : Views.iterable(img.getIndexImg())) {
            occurringValues.add(pixel.getInteger());
        }

        return occurringValues;
    }

    private static <T, I extends IntegerType<I>> boolean hasIntersectingLabels(ImgLabeling<T, I> img){
        List<Set<T>> labelSets = img.getMapping().getLabelSets();
        for(Integer i: getOccurringValues(img)){
            if(labelSets.get(i).size() > 1){
                return true;
            }
        }
        return false;
    }
}
