package demos;

import static net.imglib2.algorithm.blocks.dfield.DisplacementFieldTransform.displacementFieldAffine;
import static net.imglib2.view.fluent.RandomAccessibleIntervalView.Extension.zero;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

import ij.IJ;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.blocks.BlockAlgoUtils;
import net.imglib2.algorithm.blocks.BlockSupplier;
import net.imglib2.algorithm.blocks.dfield.DisplacementField;
import net.imglib2.algorithm.blocks.transform.Transform;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.DoubleArray;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.parallel.TaskExecutors;
import net.imglib2.realtransform.AffineTransform3D;
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
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import net.imglib2.view.fluent.RandomAccessibleIntervalView;
import net.imglib2.view.fluent.RandomAccessibleIntervalView.Extension;
import net.imglib2.view.fluent.RandomAccessibleView.Interpolation;

public class TpsToDfieldDemoAndBenchmark {

	static final Random random = new Random(7777);

	public static void main(String[] args) {

		validate();

		final RandomAccessibleInterval<UnsignedByteType> img = ArrayImgs.unsignedBytes(256, 256, 256);
		final int[] Nlist = {10, 25, 50, 100, 250};
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

		System.out.println("done");
	}

	public static void validate() {

		final RandomAccessibleInterval<UnsignedByteType> img = ArrayImgs.unsignedBytes(256, 256, 256);
		img.view().interval(Intervals.createMinSize(64, 64, 64, 64, 64, 64)).forEach(x -> x.set(255));

		int N = 50;
		RealTransform transform = dummyTps(N, 5, img);

		double[] imageSpacing = new double[]{1,1,1};
		double[] downsamplingFactors = new double[]{4,4,4};

		double[] dfspacing = new double[]{ 
				imageSpacing[0] * downsamplingFactors[0],
				imageSpacing[1] * downsamplingFactors[1],
				imageSpacing[2] * downsamplingFactors[2]
		};

		System.out.println("img: " + Intervals.toString(img));

		Img<DoubleType> df = renderDfield(transform, downsample(img, downsamplingFactors), dfspacing);
		DisplacementFieldTransform tform = new DisplacementFieldTransform(df, dfspacing);

		RandomAccessibleInterval<UnsignedByteType> imgTf = renderImage(img, img, imageSpacing, tform,  1);
		IJ.save(ImageJFunctions.wrapUnsignedByte(imgTf, "img"), "imgTf.tif");

		Img<DoubleType> dfNorm = renderDfieldNormalized(transform, downsample(img, downsamplingFactors), dfspacing);
		RandomAccessibleInterval<UnsignedByteType> imgTfBlk = renderImage(img, img, imageSpacing, dfNorm, dfspacing, 1);
		IJ.save(ImageJFunctions.wrapUnsignedByte(imgTfBlk, "imgBlk"), "imgTfBlk.tif");
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
				return imageSpacing[i] * dfieldDownsampling[i];
			}).toArray();

			System.out.println("image spacing : " + Arrays.toString(imageSpacing));
			System.out.println("dfield spacing: " + Arrays.toString(dfieldSpacing));

			Img<DoubleType> dfield = renderDfield(transform, downsample(img, dfieldDownsampling), dfieldSpacing);
			tform = new DisplacementFieldTransform(dfield, dfieldSpacing);
			RandomAccessibleInterval<T> imgTf = renderImage(img, img, imageSpacing, tform,  8);

			Img<DoubleType> dfieldNorm = renderDfieldNormalized(transform, downsample(img, dfieldDownsampling), dfieldSpacing );
			RandomAccessibleInterval<T> imgTfBlks = renderImage(img, img, imageSpacing, dfieldNorm, dfieldSpacing, 8);
		}
		else {
			tform = transform;
			RandomAccessibleInterval<T> imgTf = renderImage(img, img, imageSpacing, tform,  8);
		}

	}

	public static Interval downsample(final Interval itvl, final double[] downsample ) {

		final long[] dims = IntStream.of(0, 1, 2).mapToLong( i -> {
			return (long)Math.ceil(itvl.dimension(i) / downsample[i]);
		}).toArray();

		return new FinalInterval(dims);
	}

	/**
	 * 
	 * @param tform
	 * @param interval
	 * @param spacing
	 * @return
	 */
	public static Img<DoubleType> renderDfield(
			RealTransform tform, Interval interval, double[] spacing) {

		final long start = System.currentTimeMillis();
		final RandomAccessibleInterval<DoubleType> dfieldV = DisplacementFieldTransform.createDisplacementField(
				tform, interval, spacing);

		final ArrayImg<DoubleType, DoubleArray> dfield = ArrayImgs.doubles(dfieldV.dimensionsAsLongArray());
		LoopBuilder.setImages(dfieldV, dfield).forEachPixel((x,y) -> y.set(x.get()));	
		final long end = System.currentTimeMillis();

		System.out.println("rendering dfield took: " + (end-start) + "ms");
		return dfield;
	}

	public static Img<DoubleType> renderDfieldNormalized(
			RealTransform tform, Interval interval, double[] spacing) {

		final long start = System.currentTimeMillis();
		final RandomAccessibleInterval<DoubleType> dfieldV = DisplacementFieldTransform.createDisplacementField(
				tform, interval, spacing);

		final ArrayImg<DoubleType, DoubleArray> dfield = ArrayImgs.doubles(dfieldV.dimensionsAsLongArray());
		for( int i = 0; i < interval.numDimensions(); i++) {

			final double f = spacing[i];
			LoopBuilder.setImages(dfieldV.view().slice(0, i), dfield.view().slice(0, i))
					.forEachPixel((x, y) -> y.set(x.get() / f));
		}
		final long end = System.currentTimeMillis();
		System.out.println("rendering dfield took: " + (end-start) + "ms");
		return dfield;
	}

	public static <T extends NativeType<T> & RealType<T>> RandomAccessibleInterval<T> renderImage(
			RandomAccessibleInterval<T> img, Interval outputInterval, double[] outputResolution,
			RealTransform tform, int nThreads) {

		final long start = System.currentTimeMillis();

		final Scale3D s = new Scale3D(outputResolution);
		final RealTransformSequence seq = new RealTransformSequence();
		seq.add(s.inverse());
		seq.add(tform.copy());
		seq.add(s.copy());

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

	public static <T extends NativeType<T> & RealType<T>> RandomAccessibleInterval<T> renderImage(
			RandomAccessibleInterval<T> img, Interval outputInterval, double[] outputResolution,
			Img<DoubleType> dfieldArray, double[] dfieldResolution, int nThreads) {

		final long start = System.currentTimeMillis();

		final AffineTransform3D transformFromSource = new AffineTransform3D();
		transformFromSource.scale(
				dfieldResolution[0] / outputResolution[0],
				dfieldResolution[1] / outputResolution[1],
				dfieldResolution[2] / outputResolution[2]);

		final DisplacementField< DoubleType > dfield = new DisplacementField<>(
				BlockSupplier.of( dfieldArray ), dfieldResolution, new double[] { 0, 0, 0 } );

		final BlockSupplier< T > blocks = BlockSupplier
				.of( img.view().extend(zero()) )
				.andThen( displacementFieldAffine( transformFromSource, dfield, Transform.Interpolation.NLINEAR ) );

		ArrayImg<T, ?> imgTf = BlockAlgoUtils.arrayImg( blocks, outputInterval );

		final long end = System.currentTimeMillis();
		System.out.println("rendering image with blocks took: " + (end-start) + "ms");

		return imgTf;
	}

}
