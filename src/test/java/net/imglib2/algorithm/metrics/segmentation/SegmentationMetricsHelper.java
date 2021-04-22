package net.imglib2.algorithm.metrics.segmentation;

import net.imglib2.img.Img;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

public class SegmentationMetricsHelper
{

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
