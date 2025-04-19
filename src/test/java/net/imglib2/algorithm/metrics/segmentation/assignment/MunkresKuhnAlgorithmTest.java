/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2025 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
 * John Bogovic, Albert Cardona, Barry DeZonia, Christian Dietz, Jan Funke,
 * Aivar Grislis, Jonathan Hale, Grant Harris, Stefan Helfrich, Mark Hiner,
 * Martin Horn, Steffen Jaensch, Lee Kamentsky, Larry Lindsey, Melissa Linkert,
 * Mark Longair, Brian Northan, Nick Perry, Curtis Rueden, Johannes Schindelin,
 * Jean-Yves Tinevez and Michael Zinsmaier.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
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
