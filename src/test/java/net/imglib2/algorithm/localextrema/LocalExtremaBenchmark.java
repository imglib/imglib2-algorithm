package net.imglib2.algorithm.localextrema;

import net.imglib2.Point;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.parallel.Parallelization;
import net.imglib2.test.RandomImgs;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.view.Views;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * Demonstrates how to use {@link Parallelization} to execute a algorithm
 * single threaded / multi threaded ....
 * And shows the execution time.
 */
@Fork( 0 )
@Warmup( iterations = 10, time = 200, timeUnit = TimeUnit.MILLISECONDS )
@Measurement( iterations = 10, time = 200, timeUnit = TimeUnit.MILLISECONDS )
@State( Scope.Benchmark )
@BenchmarkMode( { Mode.AverageTime } )
public class LocalExtremaBenchmark
{

	private final RandomAccessibleInterval< IntType > image = RandomImgs.seed( 42 ).nextImage( new IntType(), 100, 100, 100 );

	@Benchmark
	public List< Point > benchmark() throws ExecutionException, InterruptedException
	{
		LocalExtrema.LocalNeighborhoodCheck< Point, IntType > check = new LocalExtrema.MaximumCheck( new IntType( Integer.MIN_VALUE ));
		return LocalExtrema.findLocalExtrema( image, check, new RectangleShape( 5, true ) );
	}

	public static void main( String... args ) throws RunnerException
	{
		Options options = new OptionsBuilder()
				.include( LocalExtremaBenchmark.class.getName() )
				.build();
		new Runner( options ).run();
	}
}
