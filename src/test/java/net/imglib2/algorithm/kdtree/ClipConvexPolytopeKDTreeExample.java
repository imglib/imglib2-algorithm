package net.imglib2.algorithm.kdtree;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.imglib2.RandomAccess;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.algorithm.neighborhood.HyperSphereShape;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.KDTree;
import net.imglib2.position.transform.Round;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.view.Views;

public class ClipConvexPolytopeKDTreeExample
{
	public static void main( final String[] args )
	{
		final int w = 800;
		final int h = 800;
		final int nPoints = 10000;

		// make random 2D Points
		final Random rand = new Random( 123124 );
		final List< RealPoint > points = new ArrayList<>();
		for ( int i = 0; i < nPoints; ++i )
		{
			final long x = rand.nextInt( w );
			final long y = rand.nextInt( h );
			points.add( new RealPoint( x, y ) );
		}

		// clip on convex polytope
		final ConvexPolytope polytope = new ConvexPolytope(
				new HyperPlane(    1,   1,  460 ),
				new HyperPlane(   -1,   1,  -60 ),
				new HyperPlane(  0.1,  -1, -460 ),
				new HyperPlane( -0.5,  -1, -580 ),
				new HyperPlane(   -1, 0.1, -400 ) );
		final KDTree< RealPoint > kdtree = new KDTree<>( points, points );
		final ClipConvexPolytopeKDTree< RealPoint > clip = new ClipConvexPolytopeKDTree<>( kdtree );
		clip.clip( polytope );

		// show all points
		final Img< ARGBType > pointsImg = ArrayImgs.argbs( w, h );
		paint( points, pointsImg, new ARGBType( 0x00ff00 ) );
		ImageJFunctions.show( pointsImg );

		// show inside/outside points
		final Img< ARGBType > clipImg = ArrayImgs.argbs( w, h );
		paint( clip.getInsideNodes(), clipImg, new ARGBType( 0xffff00 ) );
		paint( clip.getOutsideNodes(), clipImg, new ARGBType( 0x0000ff ) );
		ImageJFunctions.show( clipImg );
	}

	static void paint( final Iterable< ? extends RealLocalizable > points, final Img< ARGBType > output, final ARGBType color )
	{
		final int radius = 2;
		final RandomAccess< Neighborhood< ARGBType > > na = new HyperSphereShape( radius ).neighborhoodsRandomAccessible( Views.extendZero( output ) ).randomAccess();
		final Round< RandomAccess< Neighborhood< ARGBType > > > rna = new Round<>( na );
		for ( final RealLocalizable l : points )
		{
			rna.setPosition( l );
			for ( final ARGBType t : na.get() )
				t.set( color );
		}
	}
}
