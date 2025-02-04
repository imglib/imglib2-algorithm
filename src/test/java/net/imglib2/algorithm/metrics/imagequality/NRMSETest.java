/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2024 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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

