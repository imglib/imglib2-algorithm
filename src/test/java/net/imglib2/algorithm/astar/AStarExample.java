package net.imglib2.algorithm.astar;

import java.util.function.IntToLongFunction;

import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.algorithm.morphology.Dilation;
import net.imglib2.algorithm.morphology.StructuringElements;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.view.Views;

public class AStarExample
{

	@SuppressWarnings( "unused" )
	public static void main( final String[] args )
	{
//		ij.ImageJ.main( args );

		// Get test image.
		final int width = 512;
		final int height = width;

		final Point start = Point.wrap( new long[] { ( long ) ( 0.2 * width ), ( long ) ( 0.2 * height ) } );
		final Point end = Point.wrap( new long[] { ( long ) ( 0.8 * width ), ( long ) ( 0.8 * height ) } );
//		final Img< UnsignedShortType > img = testImage( width, height, start, end );
		final Img< UnsignedShortType > img = testImageSin( width, height, start, end );
//		final ij.ImagePlus imp = net.imglib2.img.display.imagej.ImageJFunctions.show( img );
//		imp.setOverlay( new ij.gui.Overlay() );

		final AStar2D< UnsignedShortType > astar = new AStar2D<>( img, img );

		System.out.println( "Blue: Classic search, follow image intensities." );
		final long t1a = System.currentTimeMillis();
		final Path path1 = astar.search( start, end );
		final long t1b = System.currentTimeMillis();
		System.out.println( "Search completed in " + ( t1b - t1a ) + " ms." );
//		final ij.gui.PolygonRoi roi1 = toRoi( path1 );
//		roi1.setStrokeColor( Color.BLUE );
//		imp.getOverlay().add( roi1 );
//		imp.updateAndDraw();

		System.out.println( "\nRed: Classic search, follow image intensities with Euclidean heuristics." );
		astar.setHeuristics( AStarHeuristics.EUCLIDEAN );
		final long t5a = System.currentTimeMillis();
		final Path path5 = astar.search( start, end );
		final long t5b = System.currentTimeMillis();
		System.out.println( "Search completed in " + ( t5b - t5a ) + " ms." );
//		final ij.gui.PolygonRoi roi5 = toRoi( path5 );
//		roi5.setStrokeColor( Color.RED);
//		imp.getOverlay().add( roi5 );
//		imp.updateAndDraw();

		System.out.println( "\nGreen: Ignore image." );
		astar.setHeuristics( AStarHeuristics.CHEBYSHEV );
		astar.setIntensityPenalty( 0. );
		astar.setThreshold( 0. );
		final long t2a = System.currentTimeMillis();
		final Path path2 = astar.search( start, end );
		final long t2b = System.currentTimeMillis();
		System.out.println( "Search completed in " + ( t2b - t2a ) + " ms." );
//		final ij.gui.PolygonRoi roi2 = toRoi( path2 );
//		roi2.setStrokeColor( Color.GREEN );
//		imp.getOverlay().add( roi2 );
//		imp.updateAndDraw();

		System.out.println( "\nCyan: Follow thresholded image but no penalty on intensities." );
		astar.setIntensityPenalty( 0. );
		astar.setThreshold( 0.1 );
		final long t3a = System.currentTimeMillis();
		final Path path3 = astar.search( start, end );
		final long t3b = System.currentTimeMillis();
		System.out.println( "Search completed in " + ( t3b - t3a ) + " ms." );
//		final ij.gui.PolygonRoi roi3 = toRoi( path3 );
//		roi3.setStrokeColor( Color.CYAN);
//		imp.getOverlay().add( roi3 );
//		imp.updateAndDraw();

		System.out.println( "\nMagenta: Ignore image, forbid going up and left and diagonally." );
		astar.setIntensityPenalty( 0. );
		astar.setThreshold( 0. );
		astar.setDirections( AStarDirections.create()
				.add( AStarDirections.DOWN )
				.add( AStarDirections.RIGHT )
				.get() );
		final long t4a = System.currentTimeMillis();
		final Path path4 = astar.search( start, end );
		final long t4b = System.currentTimeMillis();
		System.out.println( "Search completed in " + ( t4b - t4a ) + " ms." );
//		final ij.gui.PolygonRoi roi4 = toRoi( path4 );
//		roi4.setStrokeColor( Color.MAGENTA );
//		imp.getOverlay().add( roi4 );
//		imp.updateAndDraw();
	}

//	private static final ij.gui.PolygonRoi toRoi( final Path path )
//	{
//		final int[] xpoints = new int[ path.size() ];
//		final int[] ypoints = new int[ path.size() ];
//		int i = 0;
//		for ( final Localizable point : path )
//		{
//			xpoints[ i ] = point.getIntPosition( 0 );
//			ypoints[ i ] = point.getIntPosition( 1 );
//			i++;
//		}
//		final ij.gui.PolygonRoi roi = new ij.gui.PolygonRoi( xpoints, ypoints, xpoints.length, ij.gui.PolygonRoi.POLYLINE );
//		roi.setStrokeWidth( 2. );
//		return roi;
//	}

	static final Img< UnsignedShortType > testImageSin( final int width, final int height, final Point start, final Point end )
	{
		final Img< UnsignedShortType > img = ArrayImgs.unsignedShorts( width, height );

		final IntToLongFunction f = x -> {
			final double omega = 2. * Math.PI * ( x - start.getDoublePosition( 0 ) ) / ( end.getDoublePosition( 0 ) - start.getDoublePosition( 0 ) );
			final double amp = Math.abs( end.getDoublePosition( 1 )- start.getDoublePosition( 1 ) ) * Math.sin( omega );
			final double slope = ( end.getDoublePosition( 1 ) - start.getDoublePosition( 1 ) ) / ( end.getDoublePosition( 0 ) - start.getDoublePosition( 0 ) );
			final double y = slope * ( x - start.getDoublePosition( 0 ) ) + start.getDoublePosition( 1 ) + amp;
			return ( long ) y;
		};

		final RandomAccess< UnsignedShortType > ra = img.randomAccess( img );
		for ( int x = start.getIntPosition( 0 ); x <= end.getIntPosition( 0 ); x++ )
		{
			ra.setPosition( x, 0 );
			ra.setPosition( f.applyAsLong( x ), 1 );
			ra.get().set( 1000 );
		}

		final Img< UnsignedShortType > dilated = Dilation.dilate( img, StructuringElements.square( 3, 2, true ), 5 );
		Gauss3.gauss( 3, Views.extendMirrorSingle( dilated ), dilated );
		return dilated;
	}

	static final Img< UnsignedShortType > testImage( final int width, final int height, final Point start, final Point end )
	{
		final Img< UnsignedShortType > img = ArrayImgs.unsignedShorts( width, height );
		final IntToLongFunction f = x -> Math.round( ( end.getDoublePosition( 1 ) - start.getDoublePosition( 1 ) ) / ( end.getDoublePosition( 0 ) - start.getDoublePosition( 0 ) )
				/ ( end.getDoublePosition( 0 ) - start.getDoublePosition( 0 ) ) * ( x - start.getDoublePosition( 0 ) ) * ( x - start.getDoublePosition( 0 ) ) + start.getDoublePosition( 0 ) );

		final RandomAccess< UnsignedShortType > ra = img.randomAccess( img );
		for ( int x = start.getIntPosition( 0 ); x <= end.getIntPosition( 0 ); x++ )
		{
			ra.setPosition( x, 0 );
			ra.setPosition( f.applyAsLong( x ), 1 );
			ra.get().set( 1000 );
		}

		final Img< UnsignedShortType > dilated = Dilation.dilate( img, StructuringElements.square( 3, 2, true ), 5 );
		Gauss3.gauss( 3, Views.extendMirrorSingle( dilated ), dilated );
		return dilated;
	}
}
