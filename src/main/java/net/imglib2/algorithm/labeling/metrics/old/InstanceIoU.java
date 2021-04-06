package net.imglib2.algorithm.labeling.metrics.old;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.labeling.metrics.assignment.MunkresKuhnAlgorithm;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.view.Views;

import java.util.*;
import java.util.stream.IntStream;

public class InstanceIoU {

    public <I extends IntegerType<I>, J extends IntegerType<J>> double computeMetrics(RandomAccessibleInterval<I> groundTruth, RandomAccessibleInterval<J> prediction, double threshold) {

        // TODO sanity cheks

        final ConfusionMatrix confusionMatrix = new ConfusionMatrix(groundTruth, prediction);

        // compute cost matrix
        double[][] costMatrix = computeCostMatrix(confusionMatrix.getConfusionMatrix(), threshold);

        final Assignment assignment = new Assignment(costMatrix, threshold);

        return computeMetrics(confusionMatrix, assignment);
    }

    private double computeMetrics(ConfusionMatrix confusionMatrix, Assignment assignment){
        double tp = assignment.getNumberTruePositives();
        double fn = confusionMatrix.getGroundTruthLabelIndices().size() - tp;
        double fp = confusionMatrix.getPredictionLabelIndices().size() - tp;

        return tp / (tp + fn + fp);
    }

    private double[][] computeCostMatrix(int[][] confusionMatrix, double threshold){
        int M = confusionMatrix.length;
        int N = confusionMatrix[0].length;

        // empty cost matrix
        double[][] costMatrix = new double[M][Math.max(M, N)];

        // fill in cost matrix
        for (int i=0; i < M; i++) {
            for (int j=0; j < N; j++){
                double cost = computeLocalIoU(i, j, null);//confusionMatrix);
                costMatrix[i][j] = cost >= threshold ? - cost : 0;
            }
        }

        return costMatrix;
    }

    private double computeLocalIoU(int i, int j, ConfusionMatrix confusionMatrix) {
        double tp = confusionMatrix.getConfusionMatrix()[i][j];
       //double sumI = confusionMatrix.getGtHistogram();
       // double sumJ = IntStream.range(0, confusionMatrix.length).mapToDouble(k -> confusionMatrix[k][j]).sum();

        //double fn = sumI - tp;
        //double fp = sumJ - tp;

        return 0;//tp / (tp + fp + fn);
    }

    private class ConfusionMatrix<I extends IntegerType<I>, J extends IntegerType<J>>{

        final private ArrayList<Integer> groundTruthLabelIndices;
        final private ArrayList<Integer> predictionLabelIndices;

        final private Map<Integer,Integer> gtHistogram;
        final private Map<Integer,Integer> predHistogram;

        final private int[][] confusionMatrix;

        public ConfusionMatrix(RandomAccessibleInterval<I> groundTruth, RandomAccessibleInterval<J> prediction){

            // TODO sanity checks on RAIs

            // histograms label / number of pixels
            gtHistogram = new LinkedHashMap<>();
            predHistogram = new LinkedHashMap<>();

            final Cursor<I> cGT = Views.iterable(groundTruth).localizingCursor();
            final RandomAccess<J> cPD = prediction.randomAccess();
            while(cGT.hasNext()){
                // update gt histogram
                int gtLabel = cGT.next().getInteger();
                Integer count = gtHistogram.get(gtLabel);
                gtHistogram.put(gtLabel, count == null ? 1 : count+1);

                // update prediction histogram
                cPD.setPosition(cGT);
                int pdLabel = cPD.get().getInteger();
                count = predHistogram.get(pdLabel);
                predHistogram.put(pdLabel, count == null ? 1 : count+1);
            }

            // remove 0 / background
            if (gtHistogram.containsKey(0)) gtHistogram.remove(0);
            if (predHistogram.containsKey(0)) predHistogram.remove(0);

            // prepare confusion matrix
            confusionMatrix = new int[gtHistogram.size()][predHistogram.size()];
            groundTruthLabelIndices = new ArrayList<>(gtHistogram.keySet());
            predictionLabelIndices = new ArrayList<>(predHistogram.keySet());

            // populate confusion matrix
            cGT.reset();
            while (cGT.hasNext()) {
                cGT.next();
                cPD.setPosition(cGT);

                int gtLabel  = cGT.get().getInteger();
                int predLabel = cPD.get().getInteger();

                int i = groundTruthLabelIndices.indexOf(gtLabel);
                int j = predictionLabelIndices.indexOf(predLabel);
                confusionMatrix[i][j] += 1;
            }
        }

        public ArrayList<Integer> getGroundTruthLabelIndices() {
            return groundTruthLabelIndices;
        }

        public ArrayList<Integer> getPredictionLabelIndices() {
            return predictionLabelIndices;
        }

        public Map<Integer, Integer> getGtHistogram() {
            return gtHistogram;
        }

        public Map<Integer, Integer> getPredHistogram() {
            return predHistogram;
        }

        public int[][] getConfusionMatrix() {
            return confusionMatrix;
        }
    }

    private class Assignment {

        final private Map<Integer, Integer> groundTruthMatches;
        final private Map<Integer, Integer> predictionMatches;

        public Assignment(double[][] costMatrix, double threshold){
            groundTruthMatches = new HashMap<>();
            predictionMatches = new HashMap<>();

            int[][] assignment = new MunkresKuhnAlgorithm().computeAssignments(costMatrix);
            for(int i=0; i<assignment.length;i++){
                if(-costMatrix[ assignment[i][0] ][ assignment[i][1] ] >= threshold){ // not all assignments are good
                    groundTruthMatches.put(assignment[i][0], assignment[i][1]);
                    predictionMatches.put(assignment[i][1], assignment[i][0]);
                }
            }
        }

        public Map<Integer, Integer> getGroundTruthMatches(){
            return groundTruthMatches;
        }

        public Map<Integer, Integer> getPredictionMatches() {
            return predictionMatches;
        }

        public double getNumberTruePositives(){
            return groundTruthMatches.size();
        }
    }
}
