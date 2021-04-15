package net.imglib2.algorithm.metrics.segmentation.assignment;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MunkresKuhnAlgorithmTest
{

	private final double[][] exampleCost = {
			{ -1, 0, 0 },
			{ 0, -0, -1 },
			{ -0, -0.8, -0 }
	};

	private final int[][] exampleAssignment = {
			{ 0, 0 },
			{ 1, 2 },
			{ 2, 1 }
	};

	private final double[][] exampleAsymmetricalCost = {
			{ -1, 0, 0, -0.1 },
			{ 0, -0, -1, -0.2 },
			{ -0.1, -0.1, 0.2, -0.8 }
	};

	private final int[][] exampleAsymmetricalAssignment = {
			{ 0, 0 },
			{ 1, 2 },
			{ 2, 3 }
	};

	private final double[][] exampleAsymmetrical2Cost = {
			{ -1, 0, 0, 0 },
			{ 0, -0, -1, 0 },
			{ -0.1, -0.1, 0, 0 },
			{ -0.1, -0.9, -0.2, 0 }
	};

	private final int[][] exampleAsymmetrical2Assignment = {
			{ 0, 0 },
			{ 1, 2 },
			{ 2, 3 },
			{ 3, 1 }
	};

	private final double[][] exampleSingleGTCost = {
			{ -0.2, 0 }
	};

	private final int[][] exampleSingleAssignment = {
			{ 0, 0 }
	};

	@Test
	public void testSimple()
	{
		int[][] mka = new MunkresKuhnAlgorithm().computeAssignments( exampleCost );

		assertTrue( compareAssignments( exampleAssignment, mka ) );
	}

	@Test
	public void testAsymmetrical()
	{
		int[][] mka = new MunkresKuhnAlgorithm().computeAssignments( exampleAsymmetricalCost );

		assertTrue( compareAssignments( exampleAsymmetricalAssignment, mka ) );

		int[][] mka2 = new MunkresKuhnAlgorithm().computeAssignments( exampleAsymmetrical2Cost );

		assertTrue( compareAssignments( exampleAsymmetrical2Assignment, mka2 ) );
	}

	@Test
	public void testSingleGT()
	{
		int[][] mka = new MunkresKuhnAlgorithm().computeAssignments( exampleSingleGTCost );

		assertTrue( compareAssignments( exampleSingleAssignment, mka ) );
	}

	@Test
	public void testEmptyAssignment(){
		/*
		This is the motivation being making sure that cost matrix is rectangular, in the case
		where there is only a single ground-truth label and a single prediction label, then
		if the cost matrix is of size MxN -> 1x1, the MKA returns an empty assignment, which
		is not the case when the cost matrix is Mx(N+1) -> 1x2 (i.e. with an dummy prediction
		label added, that is to say an empty column).
		 */
		double[][] cost = {{-1}};

		int[][] mka = new MunkresKuhnAlgorithm().computeAssignments( cost );

		assertEquals(1,  mka.length ); // there is an assignment
		assertEquals(0,  mka[0].length ); // but it is empty

		double[][] cost2 = {{-1, 0}};

		mka = new MunkresKuhnAlgorithm().computeAssignments( cost2 );

		assertEquals(1,  mka.length ); // there is an assignment
		assertEquals(2,  mka[0].length ); // and it is not empty
	}

	private boolean compareAssignments( int[][] groundTruth, int[][] prediction )
	{
		boolean allOk = true;
		for ( int i = 0; i < groundTruth.length; i++ )
		{
			boolean found = false;
			for ( int j = 0; j < prediction.length; j++ )
			{
				if ( Arrays.equals( groundTruth[ i ], prediction[ j ] ) )
				{
					found = true;
				}
			}
			if ( !found )
			{
				allOk = false;
				break;
			}
		}

		return allOk;
	}
}
