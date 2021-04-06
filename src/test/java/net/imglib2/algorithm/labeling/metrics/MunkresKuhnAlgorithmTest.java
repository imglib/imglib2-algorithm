package net.imglib2.algorithm.labeling.metrics;

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


    @Test
    public void test(){
        int j = 2;
        double r1 = IntStream.range(0, exampleAsymmetricalCost.length).mapToDouble(i -> exampleAsymmetricalCost[i][j]).sum();
        IntStream.range(0, exampleAsymmetricalCost.length).mapToDouble(i -> exampleAsymmetricalCost[i][j]).forEach(System.out::println);
        System.out.println("-------");
        System.out.println(r1);

        System.out.println("-------");
        double r2 = Arrays.stream(exampleAsymmetricalCost).mapToDouble(a -> exampleAsymmetricalCost[0][j]).sum();
        Arrays.stream(exampleAsymmetricalCost).mapToDouble(a -> exampleAsymmetricalCost[0][j]).forEach(System.out::println);
        System.out.println("-------");
        System.out.println(r2);


    }

    @Test
    public void testSimple(){
        int[][] mka = new MunkresKuhnAlgorithm().computeAssignments(exampleCost);

        assertTrue(compareAssignments(exampleAssignment, mka));
    }

    @Test
    public void testAsymmetrical(){
        int[][] mka = new MunkresKuhnAlgorithm().computeAssignments(exampleAsymmetricalCost);

        assertTrue(compareAssignments(exampleAsymmetricalAssignment, mka));
        //assertTrue(compareAssignments(exampleAsymmetricalAssignment, jva));

        int[][] mka2 = new MunkresKuhnAlgorithm().computeAssignments(exampleAsymmetrical2Cost);
        //int[][] jva2 = new JonkerVolgenantAlgorithm().computeAssignments(exampleAsymmetrical2Cost);

        assertTrue(compareAssignments(exampleAsymmetrical2Assignment, mka2));
        //assertTrue(compareAssignments(exampleAsymmetrical2Assignment, jva2));
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
