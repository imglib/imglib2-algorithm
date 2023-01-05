package net.imglib2.algorithm.metrics.imagequality;

import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MSETest
{
	private final static double delta = 0.00001;

	/**
	 * Compare the metrics score of a toy sample with result from scikit-image (python library).
	 */
	@Test
	public void testToySample()
	{
		long[] dims = { 32, 32 };

		final Img< UnsignedByteType > r = ArrayImgs.unsignedBytes( dims );
		final Img< UnsignedByteType > p = ArrayImgs.unsignedBytes( dims );

		final RandomAccess< UnsignedByteType > raR = r.randomAccess();
		final RandomAccess< UnsignedByteType > raP = p.randomAccess();

		for ( int i = 0; i < dims[ 0 ]; i++ )
		{
			for ( int j = 0; j < dims[ 1 ]; j++ )
			{
				raR.setPositionAndGet( i, j ).set( i );
				raP.setPositionAndGet( i, j ).set( i + j );
			}
		}

		// Same toy sample run with scikit-image (python):
		// mean_squared_error(r, p)
		double skimageMSE = 325.5;

		assertEquals( skimageMSE, MSE.computeMetrics( r, p ), delta );
	}
}
