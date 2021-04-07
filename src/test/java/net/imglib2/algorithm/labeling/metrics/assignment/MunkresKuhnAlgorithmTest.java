package net.imglib2.algorithm.labeling.metrics.assignment;

import net.imglib2.algorithm.labeling.metrics.assignment.MunkresKuhnAlgorithm;
import org.junit.Test;

import java.util.Arrays;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MunkresKuhnAlgorithmTest {


    private final double[][] exampleCost = {
            {-1, 0, 0},
            {0, -0, -1},
            {-0, -0.8, -0}
    };

    private final int[][] exampleAssignment = {
            {0, 0},
            {1, 2},
            {2, 1}
    };

    private final double[][] exampleAsymmetricalCost = {
            {-1, 0, 0, -0.1},
            {0, -0, -1, -0.2},
            {-0.1, -0.1, 0.2, -0.8}
    };

    private final int[][] exampleAsymmetricalAssignment = {
            {0, 0},
            {1, 2},
            {2, 3}
    };

    private final double[][] exampleAsymmetrical2Cost = {
            {-1, 0, 0, 0},
            {0, -0, -1, 0},
            {-0.1, -0.1, 0, 0},
            {-0.1, -0.9, -0.2, 0}
    };

    private final int[][] exampleAsymmetrical2Assignment = {
            {0, 0},
            {1, 2},
            {2, 3},
            {3, 1}
    };

    private final double[][] exampleSingleGTCost = {
            {-0.2, 0}
    };

    private final int[][] exampleSingleAssignment = {
            {0, 0}
    };


    @Test
    public void testSimple(){
        int[][] mka = new MunkresKuhnAlgorithm().computeAssignments(exampleCost);

        assertTrue(compareAssignments(exampleAssignment, mka));
    }

    @Test
    public void testAsymmetrical(){
        int[][] mka = new MunkresKuhnAlgorithm().computeAssignments(exampleAsymmetricalCost);

        assertTrue(compareAssignments(exampleAsymmetricalAssignment, mka));

        int[][] mka2 = new MunkresKuhnAlgorithm().computeAssignments(exampleAsymmetrical2Cost);

        assertTrue(compareAssignments(exampleAsymmetrical2Assignment, mka2));
    }

    @Test
    public void testSingleGT(){
        int[][] mka = new MunkresKuhnAlgorithm().computeAssignments(exampleSingleGTCost);

        assertTrue(compareAssignments(exampleSingleAssignment, mka));
    }


    private boolean compareAssignments(int[][] groundTruth, int[][] prediction){
        boolean allOk = true;
        for(int i=0; i<groundTruth.length; i++) {
            boolean found = false;
            for (int j = 0; j < prediction.length; j++) {
                if (Arrays.equals(groundTruth[i], prediction[j])) {
                    found = true;
                }
            }
            if(!found){
                allOk = false;
                break;
            }
        }

        return allOk;
    }
}
