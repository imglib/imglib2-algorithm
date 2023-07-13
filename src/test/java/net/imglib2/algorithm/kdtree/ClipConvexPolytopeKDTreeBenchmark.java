package net.imglib2.algorithm.kdtree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import net.imglib2.RealPoint;
import net.imglib2.util.LinAlgHelpers;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

@State( Scope.Benchmark )
public class ClipConvexPolytopeKDTreeBenchmark
{
	public int n = 2;
	public int numDataVertices = 100000;
	final int w = 400;
	final int h = 400;

	List< RealPoint > dataVertices;
	double[][] planes;

	private net.imglib2.KDTree< RealPoint > kdtreeOld;
	private net.imglib2.kdtree.KDTree< RealPoint > kdtree;

	@Setup
	public void setup()
	{
		createVertices();
		createPlanes();
		kdtreeOld = new net.imglib2.KDTree<>( dataVertices, dataVertices );
		kdtree = new net.imglib2.kdtree.KDTree<>( dataVertices, dataVertices );
	}

	@Benchmark
	@BenchmarkMode( Mode.AverageTime )
	@OutputTimeUnit( TimeUnit.MILLISECONDS )
	public void clipConvexPolytopeNew()
	{
		final ClipConvexPolytopeKDTreeNew< RealPoint > clipper = new ClipConvexPolytopeKDTreeNew<>( kdtree );
		clipper.clip( planes );
		final Iterator< net.imglib2.kdtree.KDTreeNode< RealPoint > > iterator = clipper.getInsideNodes().iterator();
		int num = 0;
		while ( iterator.hasNext() )
		{
			iterator.next();
			++num;
		}
//		System.out.println( "num = " + num );
	}

	@Benchmark
	@BenchmarkMode( Mode.AverageTime )
	@OutputTimeUnit( TimeUnit.MILLISECONDS )
	public void clipConvexPolytope()
	{
		final ClipConvexPolytopeKDTree< RealPoint > clipper = new ClipConvexPolytopeKDTree<>( kdtreeOld );
		clipper.clip( planes );
		final Iterator< net.imglib2.KDTreeNode< RealPoint > > iterator = clipper.getInsideNodes().iterator();
		int num = 0;
		while ( iterator.hasNext() )
		{
			iterator.next();
			++num;
		}
//		System.out.println( "num = " + num );
	}

	private void createPlanes()
	{
		planes = new double[ 5 ][ 3 ]; // unit normal x, y; d

		double[] plane = planes[ 0 ];
		plane[ 0 ] = 1;
		plane[ 1 ] = 1;
		LinAlgHelpers.scale( plane, 1.0 / LinAlgHelpers.length( plane ), plane );
		plane[ 2 ] = 230;

		plane = planes[ 1 ];
		plane[ 0 ] = -1;
		plane[ 1 ] = 1;
		LinAlgHelpers.scale( plane, 1.0 / LinAlgHelpers.length( plane ), plane );
		plane[ 2 ] = -30;

		plane = planes[ 2 ];
		plane[ 0 ] = 0.1;
		plane[ 1 ] = -1;
		LinAlgHelpers.scale( plane, 1.0 / LinAlgHelpers.length( plane ), plane );
		plane[ 2 ] = -230;

		plane = planes[ 3 ];
		plane[ 0 ] = -0.5;
		plane[ 1 ] = -1;
		LinAlgHelpers.scale( plane, 1.0 / LinAlgHelpers.length( plane ), plane );
		plane[ 2 ] = -290;

		plane = planes[ 4 ];
		plane[ 0 ] = -1;
		plane[ 1 ] = 0.1;
		LinAlgHelpers.scale( plane, 1.0 / LinAlgHelpers.length( plane ), plane );
		plane[ 2 ] = -200;
	}

	private void createVertices()
	{
		final double[] p = new double[ n ];
		final Random rnd = new Random( 4379 );
		dataVertices = new ArrayList<>();
		for ( int i = 0; i < numDataVertices; ++i )
		{
			p[ 0 ] = rnd.nextInt( w );
			p[ 1 ] = rnd.nextInt( h );
			dataVertices.add( new RealPoint( p ) );
		}
	}

	public static void main( final String... args ) throws RunnerException
	{
		final Options opt = new OptionsBuilder()
				.include( ClipConvexPolytopeKDTreeBenchmark.class.getSimpleName() )
				.forks( 0 )
				.warmupIterations( 4 )
				.measurementIterations( 8 )
				.warmupTime( TimeValue.milliseconds( 100 ) )
				.measurementTime( TimeValue.milliseconds( 100 ) )
				.build();
		new Runner( opt ).run();
	}
}
