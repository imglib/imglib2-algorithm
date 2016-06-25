/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2016 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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
