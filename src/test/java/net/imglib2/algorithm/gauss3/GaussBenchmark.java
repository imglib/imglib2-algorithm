package net.imglib2.algorithm.gauss3;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.gradient.PartialDerivative;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.img.planar.PlanarImgFactory;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Performance benchmark for {@link Gauss3}.
 *
 * @author Matthias Arzt
 */
@State(Scope.Benchmark)
public class GaussBenchmark {

	private final long[] dim = {1000, 1000};
	private final long[] dim2 = {1000, 10, 100};

	private RandomAccessibleInterval<DoubleType> in = ArrayImgs.doubles(dim);
	private RandomAccessibleInterval<DoubleType> out = shrink(ArrayImgs.doubles(dim), 50);
	private CellImgFactory<DoubleType> factory = new CellImgFactory<>(5);
	private RandomAccessible<DoubleType> cellIn = Views.extendBorder(factory.create(dim, new DoubleType()));
	private RandomAccessibleInterval<DoubleType> cellOut = factory.create(dim, new DoubleType());
	private PlanarImgFactory<DoubleType> planerFactory = new PlanarImgFactory<>();
	private RandomAccessible<DoubleType> planarIn = Views.extendBorder(planerFactory.create(dim2, new DoubleType()));
	private RandomAccessibleInterval<DoubleType> planarOut = planerFactory.create(dim2, new DoubleType());

	private RandomAccessibleInterval<DoubleType> shrink(RandomAccessibleInterval<DoubleType> img, int border) {
		return Views.interval(img, Intervals.expand(img, -border));
	}

	@Benchmark
	public void gauss() throws IncompatibleTypeException {
		Gauss3.gauss(10, in, out);
	}

	@Benchmark
	public void deprecated() throws IncompatibleTypeException {
		deprecatedGauss(10, in, out);
	}

	@Benchmark
	public void gaussOnCellImg() throws IncompatibleTypeException {
		Gauss3.gauss(10, cellIn, cellOut);
	}

	@Benchmark
	public void deprecatedOnCellImg() throws IncompatibleTypeException {
		deprecatedGauss(10, cellIn, cellOut);
	}

	@Benchmark
	public void gaussOnPlanarImg() throws IncompatibleTypeException {
		Gauss3.gauss(10, planarIn, planarOut);
	}

	@Benchmark
	public void deprecatedOnPlanarImg() throws IncompatibleTypeException {
		deprecatedGauss(10, planarIn, planarOut);
	}

	void deprecatedGauss(double sigma, RandomAccessible<DoubleType> source, RandomAccessibleInterval<DoubleType> target) throws IncompatibleTypeException {
		final int n = source.numDimensions();
		final double[] s = new double[ n ];
		for ( int d = 0; d < n; ++d )
			s[ d ] = sigma;
		final int numthreads = Runtime.getRuntime().availableProcessors();
		final ExecutorService service = Executors.newFixedThreadPool( numthreads );
		final double[][] halfkernels = Gauss3.halfkernels(s);
		SeparableSymmetricConvolution.convolve( halfkernels, source, target, service);
		service.shutdown();
	}

	public static void main(String[] args) throws RunnerException {
		Options opt = new OptionsBuilder()
				.include(GaussBenchmark.class.getSimpleName())
				.forks(1)
				.warmupIterations(4)
				.measurementIterations(8)
				.warmupTime(TimeValue.milliseconds(100))
				.measurementTime(TimeValue.milliseconds(100))
				.build();
		new Runner(opt).run();
	}
}
