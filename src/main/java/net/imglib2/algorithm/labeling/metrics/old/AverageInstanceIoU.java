package net.imglib2.algorithm.labeling.metrics.old;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.labeling.metrics.assignment.MunkresKuhnAlgorithm;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.view.Views;

import java.util.*;
import java.util.stream.IntStream;


public class AverageInstanceIoU {

    public <I extends IntegerType<I>, J extends IntegerType<J>> double computeMetrics(RandomAccessibleInterval<I> groundTruth, RandomAccessibleInterval<J> prediction) {

        // TODO sanity cheks

        final ConfusionMatrix confusionMatrix = new ConfusionMatrix(groundTruth, prediction);

        // compute cost matrix
        double[][] costMatrix = computeCostMatrix(confusionMatrix.getConfusionMatrix());

        final Assignment assignment = new Assignment(costMatrix);

        return computeMetrics(costMatrix, assignment);
    }

    private double computeMetrics(double[][] costMatrix, Assignment assignment){
        Map<Integer, Integer> matches = assignment.getGroundTruthMatches();

        double iou_sum = matches.entrySet().stream().mapToDouble( e -> costMatrix[e.getKey()][e.getValue()]).sum();

        return iou_sum/matches.size();
    }

    private double[][] computeCostMatrix(int[][] confusionMatrix){
        int M = confusionMatrix.length;
        int N = confusionMatrix[0].length;

        // empty cost matrix
        double[][] costMatrix = new double[M][Math.max(M, N)];

        // fill in cost matrix
        for (int i=0; i < M; i++) {
            for (int j=0; j < N; j++){
                costMatrix[i][j] = -computeLocalIoU(i, j, confusionMatrix);
            }
        }

        return costMatrix;
    }

    private double computeLocalIoU(int i, int j, int[][] confusionMatrix) {
        double tp = confusionMatrix[i][j];
        double sumI = Arrays.stream(confusionMatrix[i]).sum();
        double sumJ = IntStream.range(0, confusionMatrix.length).mapToDouble(k -> confusionMatrix[k][j]).sum();

        double fn = sumI - tp;
        double fp = sumJ - tp;

        return tp / (tp + fp + fn);
    }

    private class ConfusionMatrix<I extends IntegerType<I>, J extends IntegerType<J>>{

        final private ArrayList<Integer> groundTruthLabelIndices;
        final private ArrayList<Integer> predictionLabelIndices;
        final private int[][] confusionMatrix;

        public ConfusionMatrix(RandomAccessibleInterval<I> groundtruth, RandomAccessibleInterval<J> prediction){

            // TODO sanity checks on RAIs

            // histograms pixels / label
            final LinkedHashMap<Integer,Integer> gtHist = new LinkedHashMap<>();
            final LinkedHashMap<Integer,Integer> predictionHist = new LinkedHashMap<>();

            final Cursor<I> cGT = Views.iterable(groundtruth).localizingCursor();
            final RandomAccess<J> cPD = prediction.randomAccess();
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
            final int M = gtHist.size();
            final int N = predictionHist.size();
            confusionMatrix = new int[M][N];
            groundTruthLabelIndices = new ArrayList<>(gtHist.keySet());
            predictionLabelIndices = new ArrayList<>(predictionHist.keySet());

            // populate confusion matrix
            cGT.reset();
            while (cGT.hasNext()) {
                cGT.next();
                cPD.setPosition(cGT);

                int gtLabel  = cGT.get().getInteger();
                int predLabel = cPD.get().getInteger();

                confusionMatrix[groundTruthLabelIndices.indexOf(gtLabel)][predictionLabelIndices.indexOf(predLabel)] += 1;
            }
        }

        public ArrayList<Integer> getGroundTruthLabelIndices() {
            return groundTruthLabelIndices;
        }

        public ArrayList<Integer> getPredictionLabelIndices() {
            return predictionLabelIndices;
        }

        public int[][] getConfusionMatrix() {
            return confusionMatrix;
        }
    }

    private class Assignment {

        final private Map<Integer, Integer> groundTruthMatches;
        final private Map<Integer, Integer> predictionMatches;

        public Assignment(double[][] costMatrix){
            groundTruthMatches = new HashMap<>();
            predictionMatches = new HashMap<>();

            int[][] assignment = new MunkresKuhnAlgorithm().computeAssignments(costMatrix);
            for(int i=0; i<assignment.length;i++){
                groundTruthMatches.put(assignment[i][0], assignment[i][1]);
                predictionMatches.put(assignment[i][1], assignment[i][0]);
            }
        }

        public Map<Integer, Integer> getGroundTruthMatches(){
            return groundTruthMatches;
        }

        public Map<Integer, Integer> getPredictionMatches() {
            return predictionMatches;
        }
    }
}
