package net.imglib2.algorithm.convolution.kernel;

import net.imglib2.Dimensions;
import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.convolution.Convolution;
import net.imglib2.algorithm.convolution.ConvolverFactory;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.img.list.ListImgFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Pair;
import net.imglib2.util.Util;
import net.imglib2.util.ValuePair;
import net.imglib2.view.Views;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Matthias Arzt
 */
public class SeparableKernelConvolution
{

	/**
	 * Convolve source with a separable kernel and write the result to
	 * output. In-place operation (source==target) is supported.
	 *
	 * <p>
	 * If the target type T is {@link DoubleType}, all calculations are done in
	 * double precision. For all other target {@link RealType RealTypes} float
	 * precision is used. General {@link NumericType NumericTypes} are computed
	 * in their own precision. The source type S and target type T are either
	 * both {@link RealType RealTypes} or both the same type.
	 *
	 * @param kernels
	 *            an array containing kernels for every dimension.
	 * @param source
	 *            source image, must be sufficiently padded (use e.g.
	 *            {@link Views#extendMirrorSingle(RandomAccessibleInterval)})
	 *            {@link #convolveSourceInterval(Kernel1D[], Interval)} calculates
	 *            the required source interval.
	 * @param target
	 *            target image.
	 * @param service
	 *            service providing threads for multi-threading
	 * @param <S>
	 *            source type
	 * @param <T>
	 *            target type
	 * @throws IncompatibleTypeException
	 *             if source and target type are not compatible (they must be
	 *             either both {@link RealType RealTypes} or the same type).
	 */
	public static <S extends NumericType<S>, T extends NumericType<T>> void convolve(Kernel1D[] kernels, RandomAccessible<S> source, RandomAccessibleInterval<T> target, ExecutorService service) throws IncompatibleTypeException {
		final T targetType = Util.getTypeFromInterval( target );
		final S sourceType = Util.getTypeFromInterval( Views.interval( source, target ));
		final ImgFactory< T > imgfac = getImgFactory( target, kernels, targetType );
		List< Pair< Integer, ? extends ConvolverFactory< ? super T, ? super T > > > convolvers =
			IntStream.range(0, target.numDimensions())
					.mapToObj( d -> new ValuePair<>(d, KernelConvolverFactories.get( kernels[d], sourceType, targetType ) ) )
					.collect( Collectors.toList() );
		Convolution.convolve( convolvers, imgfac, uncheckedCast( source ), target );
	}

	public static Interval convolveSourceInterval( Kernel1D[] kernels, Interval targetInterval ) {
		List< Pair< Integer, ? extends ConvolverFactory< ? super Object, ? super Object > > > convolvers =
				IntStream.range(0, targetInterval.numDimensions())
						.mapToObj( d -> new ValuePair<>(d, new DummyConvolver( kernels[d]) ) )
						.collect( Collectors.toList() );
		return Convolution.convolveSourceInterval( convolvers, targetInterval );
	}

	private static < T extends NumericType< T > > ImgFactory< T > getImgFactory(final Dimensions targetsize, final Kernel1D[] fullKernels, final T type )
	{
		if(type instanceof NativeType)
			return uncheckedCast(getNativeImgFactory(targetsize, fullKernels, uncheckedCast(type)));
		return new ListImgFactory<>(type);
	}

	private static <T extends NativeType<T>> ImgFactory<T> getNativeImgFactory(Dimensions targetsize, Kernel1D[] fullKernels, T type) {
		if ( canUseArrayImgFactory( targetsize, fullKernels ) )
			return new ArrayImgFactory<>(type);
		final int cellSize = ( int ) Math.pow( Integer.MAX_VALUE / type.getEntitiesPerPixel().getRatio(), 1.0 / targetsize.numDimensions() );
		return new CellImgFactory<>(type, cellSize);
	}

	private static boolean canUseArrayImgFactory(final Dimensions targetsize, final Kernel1D[] fullKernels )
	{
		final int n = targetsize.numDimensions();
		long size = targetsize.dimension( 0 );
		for ( int d = 1; d < n; ++d )
			size *= targetsize.dimension( d ) + fullKernels[ d ].size();
		return size <= Integer.MAX_VALUE;
	}

	// TODO: move to a better place
	private static <T> T uncheckedCast(Object input) {
		@SuppressWarnings("unchecked")
		T output = (T) input;
		return output;
	}

	public static Interval convolve1dSourceInterval( Kernel1D kernel, Interval targetInterval, int d )
	{
		return Convolution.convolve1dSourceInterval( new DummyConvolver( kernel ), targetInterval, d );
	}

	private static class DummyConvolver implements ConvolverFactory<Object, Object>
	{
		private final Kernel1D kernel;

		public DummyConvolver( Kernel1D kernel )
		{
			this.kernel = kernel;
		}

		@Override public long getBorderBefore()
		{
			return - kernel.min();
		}

		@Override public long getBorderAfter()
		{
			return kernel.max();
		}

		@Override public Runnable getConvolver( RandomAccess< ? > in, RandomAccess< ? > out, int d, long lineLength )
		{
			return () -> {};
		}
	}
}
