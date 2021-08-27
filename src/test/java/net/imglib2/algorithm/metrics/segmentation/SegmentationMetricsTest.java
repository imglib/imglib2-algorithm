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
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class SegmentationMetricsTest
{

	public static final long[] exampleIndexArrayDims = new long[] { 4, 5 };

	public static String[] exampleIntersectingLabels = new String[] { "A", "A,B", "C", "D", "D,E" };

	public static String[] exampleNonIntersectingLabels = new String[] { "A", "A,B", "C", "D", "E" };

	public static int[] exampleIndexArray = new int[] {
			1, 0, 0, 0, 0,
			0, 1, 0, 5, 0,
			0, 0, 0, 3, 3,
			0, 0, 3, 3, 0
	};

	public static List< Set< String > > getLabelingSet( String[] labels )
	{
		List< Set< String > > labelings = new ArrayList<>();

		labelings.add( new HashSet<>() );

		// Add label Sets
		for ( String entries : labels )
		{
			Set< String > subLabelSet = new HashSet<>();
			for ( String entry : entries.split( "," ) )
			{
				subLabelSet.add( entry );
			}
			labelings.add( subLabelSet );
		}

		return labelings;
	}

	@Test
	public void testStaticMethods()
	{
		final Img< IntType > img = ArrayImgs.ints( exampleIndexArray, exampleIndexArrayDims );
		final ImgLabeling< String, IntType > labelingIntersect = ImgLabeling.fromImageAndLabelSets( img, getLabelingSet( exampleIntersectingLabels ) );
		final ImgLabeling< String, IntType > labelingNonIntersect = ImgLabeling.fromImageAndLabelSets( img, getLabelingSet( exampleNonIntersectingLabels ) );

		Set< IntType > occIntersect = SegmentationHelper.getOccurringLabelSets( labelingIntersect );
		assertEquals( 3, occIntersect.size() );

		for ( IntType it : occIntersect )
		{
			int i = it.getInteger();
			assertTrue( i == 1 || i == 3 || i == 5 );
		}

		assertTrue( SegmentationHelper.hasIntersectingLabels( labelingIntersect ) );
		assertFalse( SegmentationHelper.hasIntersectingLabels( labelingNonIntersect ) );
	}
}
