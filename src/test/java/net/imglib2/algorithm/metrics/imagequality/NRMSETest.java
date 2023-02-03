package net.imglib2.algorithm.metrics.imagequality;

import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NRMSETest
{
	private final static double delta = 0.00001;

	public static final long[] arrayDims = new long[] { 2, 2 };

	private final byte[] ref = new byte[] {
			1, 0,
			5, 1,
	};

	private final byte[] proc = new byte[] {
			1, 1,
			4, 2,
	};

	@Test
	public void testEuclideanNorm(){
		final Img< UnsignedByteType > reference = ArrayImgs.unsignedBytes( ref, arrayDims );

		double euclNorm = Math.sqrt( ( 5*5 + 1 + 1 ) / 4. );

		assertEquals( euclNorm, NRMSE.getEuclideanNorm( reference ), delta);
	}

	@Test
	public void testMinMaxNorm(){
		final Img< UnsignedByteType > reference = ArrayImgs.unsignedBytes( ref, arrayDims );

		double minMaxNorm = 5;

		assertEquals( minMaxNorm, NRMSE.getMinMaxNorm( reference ), delta);
	}

	@Test
	public void testMeanNorm(){
		final Img< UnsignedByteType > reference = ArrayImgs.unsignedBytes( ref, arrayDims );

		double meanNorm = ( 5 + 1 + 1 ) / 4.;

		assertEquals( meanNorm, NRMSE.getMean( reference ), delta);
	}

	@Test( expected = IllegalArgumentException.class )
	public void testDifferentDims()
	{
		long[] dims1 = { 64, 64, 1 };
		long[] dims2 = { 64, 63 };
		final Img< UnsignedByteType > im1 = ArrayImgs.unsignedBytes( dims1 );
		final Img< UnsignedByteType > im2 = ArrayImgs.unsignedBytes( dims2 );

		NRMSE.computeMetrics( im1, im2, NRMSE.Normalization.EUCLIDEAN );
	}

	@Test
	public void testEmpty()
	{
		long[] dims1 = { 64, 64 };
		long[] dims2 = { 64, 64 };
		final Img< UnsignedByteType > im1 = ArrayImgs.unsignedBytes( dims1 );
		final Img< UnsignedByteType > im2 = ArrayImgs.unsignedBytes( dims2 );

		assertEquals( Double.NaN, NRMSE.computeMetrics( im1, im2, NRMSE.Normalization.EUCLIDEAN ), delta );
		assertEquals( Double.NaN, NRMSE.computeMetrics( im1, im2, NRMSE.Normalization.MEAN ), delta );
		assertEquals( Double.NaN, NRMSE.computeMetrics( im1, im2, NRMSE.Normalization.MINMAX ), delta );
	}

	@Test
	public void testNRMSE()
	{
		final Img< UnsignedByteType > reference = ArrayImgs.unsignedBytes( ref, arrayDims );
		final Img< UnsignedByteType > processed = ArrayImgs.unsignedBytes( proc, arrayDims );

		double mse = 0;
		double ms = 0;
		double m = 0;
		for ( int i = 0; i < 4; i++ )
		{
			mse += ( ref[ i ] - proc[ i ] ) * ( ref[ i ] - proc[ i ] );
			ms += ref[ i ]*ref[ i ];
			m += ref[ i ];
		}
		mse /= 4;
		ms /= 4;
		m /= 4;

		double euclNRMSE = Math.sqrt( mse / ms );
		double minmaxNRMSE = Math.sqrt( mse ) / 5;
		double meanNRMSE = Math.sqrt( mse ) / m;

		assertEquals( mse, MSE.computeMetrics( reference, processed ), delta );
		assertEquals( euclNRMSE, NRMSE.computeMetrics( reference, processed, NRMSE.Normalization.EUCLIDEAN ), delta );
		assertEquals( minmaxNRMSE, NRMSE.computeMetrics( reference, processed, NRMSE.Normalization.MINMAX ), delta );
		assertEquals( meanNRMSE, NRMSE.computeMetrics( reference, processed, NRMSE.Normalization.MEAN ), delta );
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
				raP.setPositionAndGet( new int[] { i, j } ).set( ( short ) ( i + j + 1 ) );
			}
		}

		// Same toy sample run with scikit-image (python):
		// normalized_root_mse(r, p, normalization='euclidean')
		double skimageEuclNRMSE = 1.0480030018366737;

		// normalized_root_mse(r, p, normalization='min-max')
		double skimageMinMaxlNRMSE = 0.6099248516512593;

		// normalized_root_mse(r, p, normalization='mean')
		double skimageMeanNRMSE = 1.2198497033025186;

		assertEquals( skimageEuclNRMSE, NRMSE.computeMetrics( r, p, NRMSE.Normalization.EUCLIDEAN ), delta );
		assertEquals( skimageMinMaxlNRMSE, NRMSE.computeMetrics( r, p, NRMSE.Normalization.MINMAX ), delta );
		assertEquals( skimageMeanNRMSE, NRMSE.computeMetrics( r, p, NRMSE.Normalization.MEAN ), delta );
	}
}

