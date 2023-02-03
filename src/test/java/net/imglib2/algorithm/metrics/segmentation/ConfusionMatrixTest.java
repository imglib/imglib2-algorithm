/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2021 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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
package net.imglib2.algorithm.metrics.segmentation;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.IntType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConfusionMatrixTest
{

	@Test
	public void testSize()
	{
		long[] dims = { 15, 15 };
		final Img< IntType > gt = ArrayImgs.ints( dims );
		final Img< IntType > pred = ArrayImgs.ints( dims );

		int min_gt = 2;
		int max_gt = 9;
		int min_pred = 7;
		int max_pred = 11;

		// paint
		MetricsTestHelper.paintRectangle( gt, min_gt, min_gt, max_gt, max_gt, 9 );
		MetricsTestHelper.paintRectangle( pred, min_pred, min_pred, max_pred, max_pred, 5 );

		// confusion metrics
		ConfusionMatrix< IntType, IntType > cm = new ConfusionMatrix<>( gt, pred );

		// total sizes
		int gt_size = MetricsTestHelper.getRectangleSize( min_gt, min_gt, max_gt, max_gt );
		int pred_size = MetricsTestHelper.getRectangleSize( min_pred, min_pred, max_pred, max_pred );
		assertEquals( gt_size, cm.getGroundTruthLabelSize( 0 ) );
		assertEquals( pred_size, cm.getPredictionLabelSize( 0 ) );
	}

	@Test
	public void testNonIntersecting()
	{
		long[] dims = { 15, 15 };
		final Img< IntType > gt = ArrayImgs.ints( dims );
		final Img< IntType > pred = ArrayImgs.ints( dims );

		int min_gt = 2;
		int max_gt = 6;
		int min_pred = 8;
		int max_pred = 11;

		// paint
		MetricsTestHelper.paintRectangle( gt, min_gt, min_gt, max_gt, max_gt, 9 );
		MetricsTestHelper.paintRectangle( pred, min_pred, min_pred, max_pred, max_pred, 5 );

		// confusion metrics
		ConfusionMatrix< IntType, IntType > cm = new ConfusionMatrix<>( gt, pred );

		// intersection
		assertEquals( 0, cm.getIntersection( 0, 0 ) );
	}

	@Test
	public void testIntersecting()
	{
		long[] dims = { 15, 15 };
		final Img< IntType > gt = ArrayImgs.ints( dims );
		final Img< IntType > pred = ArrayImgs.ints( dims );

		int min_gt = 2;
		int max_gt = 10;
		int min_pred = 5;
		int max_pred = 11;

		// paint
		MetricsTestHelper.paintRectangle( gt, min_gt, min_gt, max_gt, max_gt, 9 );
		MetricsTestHelper.paintRectangle( pred, min_pred, min_pred, max_pred, max_pred, 5 );

		// confusion metrics
		ConfusionMatrix< IntType, IntType > cm = new ConfusionMatrix<>( gt, pred );

		// intersection
		int intersection = MetricsTestHelper.getIntersectionBetweenRectangles( min_gt, min_gt, max_gt, max_gt, min_pred, min_pred, max_pred, max_pred );
		assertEquals( intersection, cm.getIntersection( 0, 0 ) );
	}
}
