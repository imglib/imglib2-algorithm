package net.imglib2.algorithm.metrics.imagequality;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PSNRTest
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

	@Test( expected = IllegalArgumentException.class )
	public void testDifferentDims()
	{
		long[] dims1 = { 64, 64, 1 };
		long[] dims2 = { 64, 63 };
		final Img< UnsignedByteType > im1 = ArrayImgs.unsignedBytes( dims1 );
		final Img< UnsignedByteType > im2 = ArrayImgs.unsignedBytes( dims2 );

		PSNR.computeMetrics( im1, im2 );
	}

	@Test
	public void testEmpty()
	{
		long[] dims1 = { 64, 64 };
		long[] dims2 = { 64, 64 };
		final Img< UnsignedByteType > im1 = ArrayImgs.unsignedBytes( dims1 );
		final Img< UnsignedByteType > im2 = ArrayImgs.unsignedBytes( dims2 );

		assertEquals( Double.NaN, PSNR.computeMetrics( im1, im2 ), delta );
	}

	@Test
	public void testPSNR()
	{
		final Img< UnsignedByteType > reference = ArrayImgs.unsignedBytes( ref, arrayDims );
		final Img< UnsignedByteType > processed = ArrayImgs.unsignedBytes( proc, arrayDims );

		double mse = 0;
		for(int i=0; i<4; i++){
			mse += (ref[i]-proc[i])*(ref[i]-proc[i]);
		}
		mse /= 4;

		double psnr = 10*Math.log10(255*255 / mse);

		assertEquals( psnr, PSNR.computeMetrics( reference, processed ), delta );
	}
}
