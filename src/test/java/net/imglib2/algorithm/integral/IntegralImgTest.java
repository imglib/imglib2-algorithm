package net.imglib2.algorithm.integral;

import static org.junit.Assert.assertEquals;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.RealDoubleConverter;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

import org.junit.Before;
import org.junit.Test;

public class IntegralImgTest {

	protected ArrayImg< ByteType, ByteArray > img;
	
	@Before
	public void setUp() throws Exception
	{
		this.img = generateKnownByteArrayTestImg();
	}
	
	@Test
	public void testResultIntegralImg() {
		// Create IntegralImg from input
		IntegralImg<ByteType, DoubleType> integralImg = new IntegralImg<ByteType, DoubleType>(img, new DoubleType(), new RealDoubleConverter<ByteType>(), 1);

		// integralImg will be larger by one pixel in each dimension than input due
		// to the computation of the integral image
		RandomAccessibleInterval<DoubleType> img = null;
		if (integralImg.process()) {
			img = integralImg.getResult();
		}
		
		IterableInterval<DoubleType> ii = Views.flatIterable(img);
		Cursor<DoubleType> cursor = ii.cursor();
		
		assertEquals(cursor.next(), new DoubleType(0.0d));
		assertEquals(cursor.next(), new DoubleType(0.0d));
		assertEquals(cursor.next(), new DoubleType(0.0d));
		
		assertEquals(cursor.next(), new DoubleType(0.0d));
		assertEquals(cursor.next(), new DoubleType(10.0d));
		assertEquals(cursor.next(), new DoubleType(30.0d));
		
		assertEquals(cursor.next(), new DoubleType(0.0d));
		assertEquals(cursor.next(), new DoubleType(40.0d));
		assertEquals(cursor.next(), new DoubleType(100.0d));
	}
	
	@Test
	public void testResultIntegralImgSquared() {
		// Create IntegralImg from input
		IntegralImg<ByteType, DoubleType> integralImg = new IntegralImg<ByteType, DoubleType>(img, new DoubleType(), new RealDoubleConverter<ByteType>(), 2);

		// integralImg will be larger by one pixel in each dimension than input due
		// to the computation of the integral image
		RandomAccessibleInterval<DoubleType> img = null;
		if (integralImg.process()) {
			img = integralImg.getResult();
		}
		
		IterableInterval<DoubleType> ii = Views.flatIterable(img);
		Cursor<DoubleType> cursor = ii.cursor();
		
		assertEquals(cursor.next(), new DoubleType(0.0d));
		assertEquals(cursor.next(), new DoubleType(0.0d));
		assertEquals(cursor.next(), new DoubleType(0.0d));
		
		assertEquals(cursor.next(), new DoubleType(0.0d));
		assertEquals(cursor.next(), new DoubleType(100.0d));
		assertEquals(cursor.next(), new DoubleType(500.0d));
		
		assertEquals(cursor.next(), new DoubleType(0.0d));
		assertEquals(cursor.next(), new DoubleType(1000.0d));
		assertEquals(cursor.next(), new DoubleType(3000.0d));
	}
	
	public ArrayImg<ByteType, ByteArray> generateKnownByteArrayTestImg() {
		final long[] dims = new long[] { 2, 2 };
		final byte[] array = new byte[4];

		array[0] = (byte) 10;
		array[1] = (byte) 20;
		array[2] = (byte) 30;
		array[3] = (byte) 40;

		return ArrayImgs.bytes(array, dims);
	}

}
