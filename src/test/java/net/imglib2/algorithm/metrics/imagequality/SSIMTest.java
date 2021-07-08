package net.imglib2.algorithm.metrics.imagequality;

import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SSIMTest
{
	private final static double delta = 0.00001;

	@Test( expected = IllegalArgumentException.class )
	public void testNot3D()
	{
		long[] dims = { 32, 32, 12, 21 };

		final Img< UnsignedByteType > r = ArrayImgs.unsignedBytes( dims );
		final Img< UnsignedByteType > p = ArrayImgs.unsignedBytes( dims );

		SSIM.computeMetrics( r, p, 1.5 );
	}

	@Test( expected = IllegalArgumentException.class )
	public void testSingularDimension()
	{
		long[] dims = { 32, 32, 1 };

		final Img< UnsignedByteType > r = ArrayImgs.unsignedBytes( dims );
		final Img< UnsignedByteType > p = ArrayImgs.unsignedBytes( dims );

		SSIM.computeMetrics( r, p, 1.5 );
	}

	@Test( expected = IllegalArgumentException.class )
	public void testDimensionTooSmall()
	{
		double sigma = 1.5;
		int minsize = 2 * ( int ) ( 3 * sigma + 0.5 ) + 1;

		long[] dims = { 32, minsize - 1 };

		final Img< UnsignedByteType > r = ArrayImgs.unsignedBytes( dims );
		final Img< UnsignedByteType > p = ArrayImgs.unsignedBytes( dims );

		SSIM.computeMetrics( r, p, 1.5 );
	}

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

		double result = SSIM.computeMetrics( r, p, 1.5 );
		assertEquals( skimageSSIM, result, delta );
	}
}
