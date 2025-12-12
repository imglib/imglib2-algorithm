package demos;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.DoubleArray;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.parallel.TaskExecutors;
import net.imglib2.realtransform.DisplacementFieldTransform;
import net.imglib2.realtransform.RealTransform;
import net.imglib2.realtransform.RealTransformRandomAccessible;
import net.imglib2.realtransform.RealTransformSequence;
import net.imglib2.realtransform.Scale3D;
import net.imglib2.realtransform.ThinplateSplineTransform;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;
import net.imglib2.view.fluent.RandomAccessibleIntervalView;
import net.imglib2.view.fluent.RandomAccessibleIntervalView.Extension;
import net.imglib2.view.fluent.RandomAccessibleView.Interpolation;

public class TpsToDfieldDemoAndBenchmark {

	static final Random random = new Random(7777);

	public static void main(String[] args) {

		final RandomAccessibleInterval<UnsignedByteType> img = ArrayImgs.unsignedBytes(256, 256, 256);
		final int[] Nlist = {10, 25, 50, 100, 250 };
		for( int N : Nlist) {

			System.out.println("N: " + N);
			RealTransform transform = dummyTps(N, 0.1, img);

			// no resampling to a displacement field
			System.out.println("raw TPS");
			run(img, transform, null);
			System.out.println("");

			double[] downsamplingFactors = new double[]{1, 1, 1};
			for( double downsampling = 1; downsampling < 8.1; downsampling *= 2) {
				System.out.println("dfield downsampled by: " + downsampling);
				Arrays.fill(downsamplingFactors, downsampling);
				run(img, transform, downsamplingFactors);
				System.out.println("");
			}
			System.out.println("");
			System.out.println("");
			System.out.println("");
		}
	}

	public static <T extends NativeType<T> & RealType<T>> RandomAccessibleInterval<T> materialize(
			final RandomAccessibleInterval<T> img, int nThreads) {

		final ArrayImg<T, ?> imgOut = new ArrayImgFactory<>(img.getType()).create(img);
		LoopBuilder.setImages(img, imgOut)
				.multiThreaded(TaskExecutors.fixedThreadPool(nThreads))
				.forEachPixel((x, y) -> y.set(x));

		return imgOut;
	}

	public static RealTransform dummyTps(int N, double jitter, Interval itvl) {

		// TPS that differs the identity a little
		double[][] p = new double[3][N];
		double[][] q = new double[3][N];

		for( int i = 0; i < N; i ++) {
			p[0][i] = random.nextDouble() * itvl.dimension(0);
			p[1][i] = random.nextDouble() * itvl.dimension(1);
			p[2][i] = random.nextDouble() * itvl.dimension(2);

			q[0][i] = p[0][i] + (jitter * random.nextDouble());
			q[1][i] = p[1][i] + (jitter * random.nextDouble());
			q[2][i] = p[2][i] + (jitter * random.nextDouble());
		}
		return new ThinplateSplineTransform(p, q);
	}

	public static <T extends NativeType<T> & RealType<T>> void run(
			final RandomAccessibleInterval<T> img, 
			final RealTransform transform,
			final double[] dfieldDownsampling) {

		final double[] imageSpacing = new double[] {1.2, 1.2, 1.2};
		RealTransform tform;
		if( dfieldDownsampling != null ) {

			double[] dfieldSpacing = IntStream.of(0, 1, 2).mapToDouble( i -> {
				return imageSpacing[i] / dfieldDownsampling[i];
			}).toArray();

			RandomAccessibleInterval<DoubleType> dfield = renderDfield(transform, downsample(img, dfieldDownsampling), dfieldSpacing);
			tform = new DisplacementFieldTransform(dfield, dfieldSpacing);
		}
		else
			tform = transform;

		@SuppressWarnings("unused")
		RandomAccessibleInterval<T> imgTf = renderImage(img, img, tform, imageSpacing, 8);
	}

	public static Interval downsample(final Interval itvl, final double[] downsample ) {

		final long[] dims = IntStream.of(0, 1, 2).mapToLong( i -> {
			return (long)Math.ceil(itvl.dimension(i) / downsample[i]);
		}).toArray();

		return new FinalInterval(dims);
	}

	public static RandomAccessibleInterval<DoubleType> renderDfield(
			RealTransform tform, Interval interval, double[] spacing ) {

		final long start = System.currentTimeMillis();
		final RandomAccessibleInterval<DoubleType> dfieldV = DisplacementFieldTransform.createDisplacementField(
				tform, interval, spacing);

		final ArrayImg<DoubleType, DoubleArray> dfield = ArrayImgs.doubles(dfieldV.dimensionsAsLongArray());
		LoopBuilder.setImages(dfieldV, dfield).forEachPixel((x,y) -> y.set(x.get()));	
		final long end = System.currentTimeMillis();

		System.out.println("rendering dfield took: " + (end-start) + "ms");
		return dfield;
	}

	public static <T extends NativeType<T> & RealType<T>> RandomAccessibleInterval<T> renderImage(
			RandomAccessibleInterval<T> img, Interval outputInterval,
			RealTransform tform, double[] outputResolution,
			int nThreads) {

		final long start = System.currentTimeMillis();

		final Scale3D s = new Scale3D(outputResolution);
		final RealTransformSequence seq = new RealTransformSequence();
		seq.add(s.inverse());
		seq.add(tform);
		seq.add(s);

		final RealTransformRandomAccessible<T,?> imgTfVReal = new RealTransformRandomAccessible< >( 
				img.view().extend(Extension.zero()).interpolate(Interpolation.nLinear()),
				seq);

		final RandomAccessibleIntervalView<T> imgTfV = Views.raster(imgTfVReal).view().interval(outputInterval);
		final ArrayImg<T,?> imgTf = new ArrayImgFactory<>(img.getType()).create(outputInterval);
		LoopBuilder.setImages(imgTfV, imgTf)
			.multiThreaded(TaskExecutors.fixedThreadPool(nThreads))
			.forEachPixel((x,y) -> y.set(x));

		final long end = System.currentTimeMillis();
		System.out.println("rendering image took: " + (end-start) + "ms");

		return imgTf;
	}

}
