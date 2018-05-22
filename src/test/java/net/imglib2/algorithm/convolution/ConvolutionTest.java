package net.imglib2.algorithm.convolution;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.algorithm.convolution.Convolution;
import net.imglib2.algorithm.convolution.ConvolverFactory;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import net.imglib2.view.Views;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class ConvolutionTest
{
	@Test
	public void testConvolve1dSourceInterval() {
		Interval target = new FinalInterval( 10, 20 );
		ConvolverFactory<?, ?> convolver = new CopyConvolverFactory( ( long ) 3, ( long ) 5 );
		Interval source = Convolution.convolve1dSourceInterval( convolver, target, 1 );
		assertTrue( Intervals.equals( Intervals.createMinMax( 0, -3, 9, 24 ), source ) );
	}

	@Test
	public void testConvolveSourceInterval() {
		Interval target = new FinalInterval( 2, 2 );
		List< Pair< Integer, ? extends ConvolverFactory< ? super UnsignedByteType, ? super UnsignedByteType > > > convolvers = Arrays.asList(
				new ValuePair<>( 1, new CopyConvolverFactory( 3, 5 ) ),
				new ValuePair<>( 0, new CopyConvolverFactory( 2, 1 ) )
		);
		Interval source = Convolution.convolveSourceInterval( convolvers, target );
		assertTrue( Intervals.equals( Intervals.createMinMax( -2, -3, 2, 6 ), source ) );
	}

	@Test
	public void testConvolve1d() {
		byte[] result = new byte[6];
		byte[] expected = {1, 2, 3, 4, 5, 6};
		Img<UnsignedByteType> out = ArrayImgs.unsignedBytes(result, 2, 3);
		Img<UnsignedByteType> in = ArrayImgs.unsignedBytes(expected, 2, 3);
		Convolution.convolve1d( new CopyConvolverFactory( 0, 0 ), in, out, 1);
		assertArrayEquals( result, expected );
	}

	@Test
	public void testConvolve() {
		byte[] result = new byte[6];
		byte[] expected = {1, 2, 3, 4, 5, 6};
		Img<UnsignedByteType> out = ArrayImgs.unsignedBytes(result, 2, 3);
		Img<UnsignedByteType> in = ArrayImgs.unsignedBytes(expected, 2, 3);
		List< Pair< Integer, ? extends ConvolverFactory< ? super UnsignedByteType, ? super UnsignedByteType > > > convolvers = Arrays.asList(
				new ValuePair<>(1, new CopyConvolverFactory( 0, 0 )),
				new ValuePair<>(1, new CopyConvolverFactory( 0, 0 ))
		);
		Convolution.convolve( convolvers, new ArrayImgFactory<>( new UnsignedByteType() ), in, out );
		assertArrayEquals( result, expected );
	}

	@Test
	public void testConvolve3d() {
		// calculate edge enhancement in y direction
		// while the image is all 0, it is extended with a border of 1
		byte[] result = new byte[8];
		byte[] expected = {0, 0, 1, 1, 0, 0, 1, 1};
		Img<UnsignedByteType> out = ArrayImgs.unsignedBytes(result, 2, 2, 2);
		RandomAccessible< UnsignedByteType > in = Views.extendValue(ArrayImgs.unsignedBytes(2, 2, 2), new UnsignedByteType( 1 ));
		List< Pair< Integer, ? extends ConvolverFactory< ? super UnsignedByteType, ? super UnsignedByteType > > > convolvers = Arrays.asList(
				new ValuePair<>( 0, new CopyConvolverFactory( 0, 0 )),
				new ValuePair<>( 1, new ForwardDifferenceConvolverFactory()),
				new ValuePair<>( 2, new CopyConvolverFactory( 0, 0))
		);
		Convolution.convolve( convolvers, new ArrayImgFactory<>( new UnsignedByteType() ), in, out );
		assertArrayEquals( result, expected );
	}

	private static class ForwardDifferenceConvolverFactory extends AbstractConvolverFactory<UnsignedByteType, UnsignedByteType>
	{

		private ForwardDifferenceConvolverFactory()
		{
			super( 0, 1 );
		}

		@Override public Runnable getConvolver( RandomAccess< ? extends UnsignedByteType > in, RandomAccess< ? extends UnsignedByteType > out, int d, long lineLength )
		{
			return () -> {
				for ( int i = 0; i < lineLength; i++ )
				{
					int center = in.get().get();
					in.fwd(d);
					int front = in.get().get();
					out.get().set( front - center );
					out.fwd(d);
				}
			};
		}
	}

	private static class CopyConvolverFactory extends AbstractConvolverFactory<UnsignedByteType, UnsignedByteType>
	{

		private CopyConvolverFactory( long before, long after )
		{
			super( before, after );
		}

		@Override public Runnable getConvolver( RandomAccess< ? extends UnsignedByteType > in, RandomAccess< ? extends UnsignedByteType > out, int d, long lineLength )
		{
			return () -> {
				for ( int i = 0; i < lineLength; i++ )
				{
					out.get().set(in.get());
					out.fwd(d);
					in.fwd(d);
				}
			};
		}
	}

	private static abstract class AbstractConvolverFactory<S, T> implements ConvolverFactory<S, T> {

		private final long before;
		private final long after;

		AbstractConvolverFactory( long before, long after )
		{
			this.before = before;
			this.after = after;
		}

		@Override public long getBorderBefore()
		{
			return before;
		}

		@Override public long getBorderAfter()
		{
			return after;
		}

		@Override public abstract Runnable getConvolver( RandomAccess< ? extends S > in, RandomAccess< ? extends T > out, int d, long lineLength );
	}
}
