package net.imglib2.algorithm.morphology.distance;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.type.numeric.real.DoubleType;

@State( Scope.Benchmark )
public class DistanceTransformBenchmark {

	private int numPoints = 4000;
	private long[] dim = new long[] {256, 256, 128};
	DistanceTransform.DISTANCE_TYPE distanceType = DistanceTransform.DISTANCE_TYPE.EUCLIDIAN;

	Random random;
	private ArrayImg<DoubleType, ?> distanceImg;
	private ArrayImg<LongType, ?> labelImg;
	ExecutorService es;
	int nThreads;
	int numTasks;

	@Setup
	public void setup()
	{
		random = new Random(0);

		final int N = Arrays.stream(dim).mapToInt( i -> (int)i).reduce(1, (x,y) -> x*y);

		final double[] initValues = new double[N];
		Arrays.fill(initValues, Double.MAX_VALUE);
		for( int i = 0; i < numPoints; i++ ) {
			initValues[random.nextInt(N)] = 0.0;
		}
		distanceImg = ArrayImgs.doubles(initValues, dim);

		final long[] initLabels = new long[N];
		for( int i = 0; i < numPoints; i++ ) {
			initLabels[random.nextInt(N)] = 1 + random.nextInt(numPoints);
		}
		labelImg = ArrayImgs.longs(initLabels, dim);

		int nThreads = 1;
		es = Executors.newFixedThreadPool(nThreads);
		numTasks = 2*nThreads;
	}

	@Benchmark
	@BenchmarkMode( Mode.AverageTime )
	@OutputTimeUnit( TimeUnit.MILLISECONDS )
	public void distanceTransform()
	{
		if( nThreads == 1 ) {
			DistanceTransform.transform(distanceImg, distanceType);
		} else {
			try {
				DistanceTransform.transform(distanceImg, distanceType, es, numTasks);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
	}

	@Benchmark
	@BenchmarkMode( Mode.AverageTime )
	@OutputTimeUnit( TimeUnit.MILLISECONDS )
	public void voronoiDistanceTransform()
	{
		if( nThreads == 1 ) {
			DistanceTransform.voronoiDistanceTransform(labelImg, 0l);
		} else {
			try {
				DistanceTransform.voronoiDistanceTransform(labelImg, 0l, es, numTasks);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main( final String... args ) throws RunnerException
	{
		final Options opt = new OptionsBuilder()
				.include( DistanceTransformBenchmark.class.getSimpleName() )
				.forks( 0 )
				.warmupIterations( 5 )
				.measurementIterations( 25 )
				.measurementTime(TimeValue.seconds( 2 ))
				.warmupTime(TimeValue.seconds( 2 ))
				.build();
		new Runner( opt ).run();
	}
}
