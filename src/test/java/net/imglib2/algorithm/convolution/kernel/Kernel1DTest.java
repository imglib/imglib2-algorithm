package net.imglib2.algorithm.convolution.kernel;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class Kernel1DTest
{

	@Test
	public void testSymmetric()
	{
		Kernel1D kernel = Kernel1D.symmetric( 2, 1 );
		assertEquals( -1, kernel.min() );
		assertEquals( 1, kernel.max() );
		assertArrayEquals( new double[] { 1, 2, 1 }, kernel.fullKernel(), 0.0 );
	}

	@Test
	public void testAsymmetric()
	{
		Kernel1D kernel = Kernel1D.asymmetric( new double[] { 1, 2, 0, 3 }, 1 );
		assertEquals( -1, kernel.min() );
		assertEquals( 2, kernel.max() );
		assertArrayEquals( new double[] { 1, 2, 0, 3 }, kernel.fullKernel(), 0.0 );
	}

	@Test
	public void testCentralAsymmetric()
	{
		Kernel1D kernel = Kernel1D.centralAsymmetric( -1, 0, 1 );
		assertEquals( -1, kernel.min() );
		assertEquals( 1, kernel.max() );
		assertArrayEquals( new double[] { -1, 0, 1 }, kernel.fullKernel(), 0.0 );
	}
}
