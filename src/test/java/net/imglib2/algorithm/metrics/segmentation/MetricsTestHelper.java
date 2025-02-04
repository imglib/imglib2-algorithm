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
package net.imglib2.algorithm.metrics.segmentation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.imglib2.img.Img;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

/**
 * Helper class used in the segmentation metrics tests.
 */
public class MetricsTestHelper
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

	public static int getIntersectionBetweenRectangles( int a_x_min, int a_y_min, int a_x_max, int a_y_max,
			int b_x_min, int b_y_min, int b_x_max, int b_y_max )
	{
		int left = Math.max( a_x_min, b_x_min );
		int right = Math.min( a_x_max, b_x_max );
		int bottom = Math.max( a_y_min, b_y_min );
		int top = Math.min( a_y_max, b_y_max );

		if ( left < right && bottom < top )
		{
			int intersection = ( right - left + 1 ) * ( top - bottom + 1 );

			return intersection;
		}
		else
		{
			return 0;
		}
	}

	public static int getRectangleSize( int x_min, int y_min, int x_max, int y_max )
	{
		return ( x_max - x_min + 1 ) * ( y_max - y_min + 1 );
	}

	public static void paintRectangle( Img< IntType > img, int[] rect, int value )
	{
		long[] interval = { rect[ 0 ], rect[ 1 ], rect[ 2 ], rect[ 3 ] };
		IntervalView< IntType > intView = Views.interval( img, Intervals.createMinMax( interval ) );
		LoopBuilder.setImages( intView ).forEachPixel( p -> p.set( value ) );

	}

	public static void paintRectangle( Img< IntType > img, int[] rect, int zPlane, int value )
	{
		long[] interval = { rect[ 0 ], rect[ 1 ], zPlane, rect[ 2 ], rect[ 3 ], zPlane };
		final IntervalView< IntType > intView = Views.interval( img, Intervals.createMinMax( interval ) );
		LoopBuilder.setImages( intView ).forEachPixel( p -> p.set( value ) );
	}

	public static void paintRectangle( Img< IntType > img, int[] rect, int zPlane, int tPlane, int value )
	{
		long[] interval = { rect[ 0 ], rect[ 1 ], zPlane, tPlane, rect[ 2 ], rect[ 3 ], zPlane, tPlane };
		final IntervalView< IntType > intView = Views.interval( img, Intervals.createMinMax( interval ) );
		LoopBuilder.setImages( intView ).forEachPixel( p -> p.set( value ) );
	}

	public static void paintRectangle( Img< IntType > img, int min_x, int min_y, int max_x, int max_y, int value )
	{
		long[] interval = { min_x, min_y, max_x, max_y };
		IntervalView< IntType > intView = Views.interval( img, Intervals.createMinMax( interval ) );
		LoopBuilder.setImages( intView ).forEachPixel( p -> p.set( value ) );
	}
}
