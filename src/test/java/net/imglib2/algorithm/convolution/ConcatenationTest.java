package net.imglib2.algorithm.convolution;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.convolution.kernel.SeparableKernelConvolution;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.ConstantUtils;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class ConcatenationTest
{
	@Test
	public void testDifferences()
	{
		Img< DoubleType > source = ArrayImgs.doubles( new double[] {
				0, 0, 0,
				0, 1, 0,
				0, 0, 0
		}, 3, 3 );
		ForwardDifference stepX = new ForwardDifference( 0 );
		ForwardDifference stepY = new ForwardDifference( 1 );
		Convolution< RealType< ? > > convolution = Convolution.concat( stepX, stepY );
		double[] targetPixels = new double[ 4 ];
		Img< DoubleType > target = ArrayImgs.doubles( targetPixels, 2, 2 );
		assertTrue( Intervals.equals( source, convolution.requiredSourceInterval( target ) ) );
		convolution.process( source, target );
		assertArrayEquals( new double[] { 1, -1, -1, 1 }, targetPixels, 0.0 );
	}

	@Test
	public void testDifferences2()
	{
		Img< IntType > source = ArrayImgs.ints( new int[] { 0, 0, 1, 0, 0 }, 5 );
		ForwardDifference step = new ForwardDifference( 0 );
		Convolution< RealType< ? > > convolution = Convolution.concat( step, step, step );
		int[] targetPixels = new int[ 2 ];
		Img< IntType > target = ArrayImgs.ints( targetPixels, 2 );
		convolution.process( source, target );
		assertArrayEquals( new int[] { -3, 3 }, targetPixels );
	}

	@Ignore( "takes to long" )
	@Test
	public void testHugeImage()
	{
		long width = 0x10000;
		long height = 0x10000;
		assertTrue( width * height > Integer.MAX_VALUE );
		RandomAccessible< UnsignedByteType > source = ConstantUtils.constantRandomAccessible( new UnsignedByteType(), 2 );
		RandomAccessibleInterval< UnsignedByteType > target = ConstantUtils.constantRandomAccessibleInterval(
				new UnsignedByteType(), 2, new FinalInterval( width, height ) );
		double[][] kernels = { { 2 }, { 3 } };
		try
		{
			Convolution.concat( new ForwardDifference( 0 ), new ForwardDifference( 1 ) )
					.process( source, target );
		}
		catch ( OutOfMemoryError acceptable )
		{
			// NB: It's ok if we run out of memory here. It still means CellImgFactory was used for the temporary image.
		}
	}

	private static class ForwardDifference implements Convolution< RealType< ? > >
	{

		private final int d;

		private ForwardDifference( int d )
		{
			this.d = d;
		}

		@Override
		public Interval requiredSourceInterval( Interval targetInterval )
		{
			long[] min = Intervals.minAsLongArray( targetInterval );
			long[] max = Intervals.maxAsLongArray( targetInterval );
			max[ d ]++;
			return new FinalInterval( min, max );
		}

		@Override
		public RealType< ? > preferredSourceType( RealType< ? > targetType )
		{
			return targetType;
		}

		@Override
		public void process( RandomAccessible< ? extends RealType< ? > > source, RandomAccessibleInterval< ? extends RealType< ? > > target )
		{
			IntervalView< ? extends RealType< ? > > back = Views.interval( source, target );
			IntervalView< ? extends RealType< ? > > front = Views.interval( source, Intervals.translate( target, 1, d ) );
			LoopBuilder.setImages( back, front, target ).forEachPixel( ( b, f, r ) ->
					r.setReal( f.getRealDouble() - b.getRealDouble() )
			);
		}
	}
}
