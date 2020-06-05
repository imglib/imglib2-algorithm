package net.imglib2.algorithm.convolution;

import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.parallel.Parallelization;
import net.imglib2.parallel.TaskExecutors;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Intervals;
import org.junit.Test;

import java.util.concurrent.Executors;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class LineConvolutionTest
{

	@Test
	public void testConvolveOneLine()
	{
		byte[] result = new byte[ 3 ];
		Img< UnsignedByteType > out = ArrayImgs.unsignedBytes( result, result.length );
		Img< UnsignedByteType > in = ArrayImgs.unsignedBytes( new byte[] { 1, 2, 0, 3 }, 4 );
		new LineConvolution<>( new ForwardDifferenceConvolverFactory(), 0 ).process( in, out );
		assertArrayEquals( new byte[] { 1, -2, 3 }, result );
	}

	@Test
	public void testConvolve()
	{
		Img< UnsignedByteType > source = ArrayImgs.unsignedBytes( new byte[] {
				0, 0, 0, 1,
				3, 4, 5, 0,
				0, 0, 0, 3
		}, 2, 2, 3 );
		byte[] result = new byte[ 8 ];
		byte[] expected = new byte[] {
				3, 4, 5, -1,
				-3, -4, -5, 3
		};
		Img< UnsignedByteType > target = ArrayImgs.unsignedBytes( result, 2, 2, 2 );
		Convolution< UnsignedByteType > convolver = new LineConvolution<>( new ForwardDifferenceConvolverFactory(), 2 );
		Interval requiredSource = convolver.requiredSourceInterval( target );
		convolver.process( source, target );
		assertTrue( Intervals.equals( source, requiredSource ) );
		assertArrayEquals( expected, result );
	}

	@Test
	public void testNumTasksEqualsIntegerMaxValue() {
		byte[] result = new byte[ 1 ];
		Img< UnsignedByteType > out = ArrayImgs.unsignedBytes( result, result.length );
		Img< UnsignedByteType > in = ArrayImgs.unsignedBytes( new byte[] { 1, 2 }, 2 );
		Runnable runnable = () -> {
			final LineConvolution< UnsignedByteType > convolution = new LineConvolution<>( new ForwardDifferenceConvolverFactory(), 0 );
			convolution.process( in, out );
		};
		Parallelization.runWithExecutor( TaskExecutors.forExecutorServiceAndNumTasks( Executors.newSingleThreadExecutor(), Integer.MAX_VALUE) , runnable );
		assertArrayEquals( new byte[] { 1 }, result );
	}

	static class ForwardDifferenceConvolverFactory implements LineConvolverFactory< UnsignedByteType >
	{

		@Override public long getBorderBefore()
		{
			return 0;
		}

		@Override public long getBorderAfter()
		{
			return 1;
		}

		@Override public Runnable getConvolver( RandomAccess< ? extends UnsignedByteType > in, RandomAccess< ? extends UnsignedByteType > out, int d, long lineLength )
		{
			return () -> {
				for ( int i = 0; i < lineLength; i++ )
				{
					int center = in.get().get();
					in.fwd( d );
					int front = in.get().get();
					out.get().set( front - center );
					out.fwd( d );
				}
			};
		}

		@Override
		public UnsignedByteType preferredSourceType( UnsignedByteType targetType )
		{
			return new UnsignedByteType();
		}
	}
}
