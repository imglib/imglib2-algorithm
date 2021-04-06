package net.imglib2.algorithm.labeling.metrics.old;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.view.Views;

import java.util.*;

/**
 *
 * SEG: The SEG metrics compute the Jaccard / IoU metrics between classes that have
 * at least > threshold percentage overlap, where the threshold is a parameter. The score is averaged
 * over all ground truth labels.
 *
 * See: Ulman, V., Maška, M., Magnusson, K. et al. An objective comparison of cell-tracking
 * algorithms. Nat Methods 14, 1141–1152 (2017).
 *
 * Inspired from:
 * https://github.com/CellTrackingChallenge/CTC-FijiPlugins
 *
 */
public class SEG {

    public <T, I extends IntegerType<I>, U, J extends IntegerType<J>> double computeMetrics(
            final ImgLabeling<T, I> groundTruth,
            final ImgLabeling<U, J> prediction,
            final double minOverlap
    ) {
        if(hasIntersectingLabels(groundTruth) || hasIntersectingLabels(prediction))
            throw new UnsupportedOperationException("ImgLabeling with intersecting labels are not supported.");

        return computeMetrics(groundTruth.getIndexImg(), prediction.getIndexImg(), minOverlap);
    }

    public <I extends IntegerType<I>, J extends IntegerType<J>> double computeMetrics(
            final RandomAccessibleInterval<I> groundTruth,
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

        Cursor<I> cGT = Views.iterable(groundTruth).localizingCursor();
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
                double intersection = confusionMatrix[i + numGtLabels * matchingLabel];
                double n_gt = gtHist.get(gtLabels.get(i));
                double n_pred = predictionHist.get(predLabels.get(matchingLabel));

                precision += intersection / (n_gt + n_pred - intersection);
            }
        }

        return precision / (double) numGtLabels;
    }

    public static <T, I extends IntegerType<I>> Set<Integer> getOccurringValues(ImgLabeling<T, I> img){
        Set<Integer> occurringValues = new HashSet<>();
        for(I pixel : Views.iterable(img.getIndexImg())) {
            occurringValues.add(pixel.getInteger());
        }

        return occurringValues;
    }

    public static <T, I extends IntegerType<I>> boolean hasIntersectingLabels(ImgLabeling<T, I> img){
        List<Set<T>> labelSets = img.getMapping().getLabelSets();
        for(Integer i: getOccurringValues(img)){
            if(labelSets.get(i).size() > 1){
                return true;
            }
        }
        return false;
    }
}
