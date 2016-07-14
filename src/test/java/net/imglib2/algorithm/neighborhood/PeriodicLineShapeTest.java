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
package net.imglib2.algorithm.neighborhood;

import static org.junit.Assert.assertTrue;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.algorithm.neighborhood.PeriodicLineShape;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.util.Intervals;

import org.junit.Test;

public class PeriodicLineShapeTest extends AbstractShapeTest
{

	private static final int[] INCREMENTS = new int[] { -2, 1, 0 };

	private static final long SPAN = 2;

	@Override
	protected Shape createShape()
	{
		return new PeriodicLineShape( SPAN, INCREMENTS );
	}

	@Override
	protected boolean isInside( final long[] pos, final long[] center )
	{
		long previousN = Long.MAX_VALUE;
		for ( int d = 0; d < center.length; d++ )
		{
			if ( INCREMENTS[ d ] == 0 )
			{
				if ( pos[ d ] != center[ d ] ) { return false; }
			}
			else
			{
				final long dx = pos[ d ] - center[ d ];
				final long rem = dx % INCREMENTS[ d ];
				if ( rem != 0 ) { return false; }
				final long n = dx / INCREMENTS[ d ];
				if ( n < -SPAN || n > SPAN ) { return false; }
				if ( previousN == Long.MAX_VALUE )
				{
					previousN = n;
				}
				else if ( previousN != n ) { return false; }
			}
		}
		return true;
	}

	@Test
	public void testStructuringElementBoundingBox() {
		Interval boundingBox = shape.getStructuringElementBoundingBox(
			INCREMENTS.length);
		assertTrue(Intervals.equals(boundingBox, new FinalInterval(new long[] { -4,
			-2, 0 }, new long[] { 4, 2, 0 })));
	}

}
