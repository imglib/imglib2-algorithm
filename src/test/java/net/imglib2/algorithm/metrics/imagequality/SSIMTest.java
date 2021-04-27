package net.imglib2.algorithm.metrics.imagequality;

import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SSIMTest
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
				raR.setPositionAndGet( new int[] { i, j } ).set( ( short ) i );
				raP.setPositionAndGet( new int[] { i, j } ).set( ( short ) ( i + j ) );
			}
		}

		// Same toy sample run with scikit-image (python):
		// structural_similarity(r, p, gaussian_weights=True, use_sample_covariance=False, sigma=1.5, data_range=255)
		double skimageSSIM = 0.7433402662472663;

		assertEquals( skimageSSIM, SSIM.computeMetrics( r, p, SSIM.Filter.GAUSS, 1.5 ), delta );
	}

	// TODO proper speed comparison, without the JNI optimizing the loop
	@Test
	public void testCompareSpeed()
	{
		double start, end;
		int M = 10;

		long[] dims = { 2048, 2048 };

		final Img< UnsignedByteType > r = ArrayImgs.unsignedBytes( dims );
		final Img< UnsignedByteType > p = ArrayImgs.unsignedBytes( dims );

		final RandomAccess< UnsignedByteType > raR = r.randomAccess();
		final RandomAccess< UnsignedByteType > raP = p.randomAccess();

		for ( int i = 0; i < dims[ 0 ]; i++ )
		{
			for ( int j = 0; j < dims[ 1 ]; j++ )
			{
				raR.setPositionAndGet( new int[] { i, j } ).set( ( short ) i );
				raP.setPositionAndGet( new int[] { i, j } ).set( ( short ) ( i + j ) );
			}
		}

		for ( int k = 0; k < M; k++ )
		{
			// Gauss
			start = System.currentTimeMillis();
			double gauss = SSIM.computeMetrics( r, p, SSIM.Filter.GAUSS, 1.5 );
			end = System.currentTimeMillis();
			double timeGauss = end - start;

			// FastGauss
			start = System.currentTimeMillis();
			double fastGauss = SSIM.computeMetrics( r, p, SSIM.Filter.FASTGAUSS, 1.5 );
			end = System.currentTimeMillis();
			double timeFastGauss = end - start;

			System.out.println( "Time: " + timeGauss + " vs " + timeFastGauss );

			assertEquals( gauss, fastGauss, 0.001 );
		}
	}
}
