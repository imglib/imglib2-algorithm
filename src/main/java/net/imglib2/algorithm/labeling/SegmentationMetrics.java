package net.imglib2.algorithm.labeling;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.view.Views;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class SegmentationMetrics {

    /**
     *
     * Inspired by Ulman, V., Maška, M., Magnusson, K. et al. An objective comparison of cell-tracking algorithms. Nat Methods 14, 1141–1152 (2017).
     *
     * Originally developed by Martin Maška and ported to Java by Vladimír Ulman.
     *
     * @param groundTruthLabeling
     * @param predictionLabeling
     * @param <T>
     * @param <I>
     * @param <U>
     * @param <J>
     * @return
     */
    public static <T, I extends IntegerType<I>, U, J extends  IntegerType<J>> double computeSEG(
            final ImgLabeling<T,I> groundTruthLabeling,
            final ImgLabeling<U,J> predictionLabeling,
            final double minOverlap
            ){

        // TODO check dimensions are equal and give an illegalargumentexception

        // TODO check 2D or extend to stacks

        RandomAccessibleInterval<I> groundtruth = groundTruthLabeling.getIndexImg();
        RandomAccessibleInterval<J> prediction = predictionLabeling.getIndexImg();

        double precision = 0.;

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

        // remove 0 / background
        Integer zero = new Integer(0);
        if(gtHist.containsKey(zero)){
            gtHist.remove(zero);
        }
        if(predictionHist.containsKey(zero)){
            predictionHist.remove(zero);
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

        return precision / (double) numGtLabels;
    }
}
