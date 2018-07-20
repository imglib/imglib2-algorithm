package net.imglib2.loops;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.loops.LoopMath;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import static net.imglib2.loops.LoopMath.Max;
import static net.imglib2.loops.LoopMath.Div;

public class LoopMathTest
{
	protected static boolean testLoopMath1( )
	{	
		final long[] dims = new long[]{ 100, 100, 100 };
		
		final ArrayImg< ARGBType, ? > rgb = new ArrayImgFactory< ARGBType >( new ARGBType() ).create( dims );
		
		final RandomAccessibleInterval< UnsignedByteType >
			red = Converters.argbChannel( rgb, 1 ),
			green = Converters.argbChannel( rgb, 2 ),
			blue = Converters.argbChannel( rgb, 3 );
		
		for ( final UnsignedByteType t : Views.iterable(red) ) t.set( 10 );
		for ( final UnsignedByteType t : Views.iterable(green) ) t.set( 30 );
		for ( final UnsignedByteType t : Views.iterable(blue) ) t.set( 20 );
		
		final ArrayImg< FloatType, ? > brightness = new ArrayImgFactory< FloatType >( new FloatType() ).create( dims );
		
		try {
			LoopMath.compute( brightness, new Div( new Max( red, new Max( green, blue ) ), 3.0 ) );
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		double sum = 0;
		
		for ( final FloatType t: brightness )
			sum += t.getRealDouble();
		
		System.out.println( "Sum: " + sum );

		return 100 * 100 * 100 * 10 == sum;
	}
	
	protected static boolean comparePerformance( final int n_iterations ) {
		final long[] dims = new long[]{ 1024, 1024 };
		
		final ArrayImg< ARGBType, ? > rgb = new ArrayImgFactory< ARGBType >( new ARGBType() ).create( dims );
		
		final RandomAccessibleInterval< UnsignedByteType >
			red = Converters.argbChannel( rgb, 1 ),
			green = Converters.argbChannel( rgb, 2 ),
			blue = Converters.argbChannel( rgb, 3 );
		
		for ( final UnsignedByteType t : Views.iterable(red) ) t.set( 10 );
		for ( final UnsignedByteType t : Views.iterable(green) ) t.set( 30 );
		for ( final UnsignedByteType t : Views.iterable(blue) ) t.set( 20 );
		
		final ArrayImg< FloatType, ? > brightness = new ArrayImgFactory< FloatType >( new FloatType() ).create( dims );

		long minLM = Long.MAX_VALUE,
			 maxLM = 0;
		double meanLM = 0;
		
		for ( int i=0; i < n_iterations; ++i ) {
			final long t0 = System.nanoTime();
			try {
				LoopMath.compute( brightness, new Div( new Max( red, new Max( green, blue ) ), 3.0 ) );
			} catch (Exception e) {
				e.printStackTrace();
			}
			final long t1 = System.nanoTime();
			
			minLM = Math.min(minLM, t1 - t0);
			maxLM = Math.max(maxLM, t1 - t0);
			meanLM += (t1 - t0) / (double)(n_iterations);
		}
		
		System.out.println("LoopMath: min: " + (minLM / 1000.0) + " ms, max: " + (maxLM / 1000.0) + " ms, mean: " + (meanLM / 1000.0) + " ms");

		
		
		// Reset
		minLM = Long.MAX_VALUE;
		maxLM = 0;
		meanLM = 0;
		
		for ( int i=0; i < n_iterations; ++i ) {
			final long t0 = System.nanoTime();
			final Cursor< FloatType > co = brightness.cursor();
			final Cursor< UnsignedByteType > cr = Views.iterable(red).cursor();
			final Cursor< UnsignedByteType > cg = Views.iterable(green).cursor();
			final Cursor< UnsignedByteType > cb = Views.iterable(blue).cursor();
			while ( co.hasNext() )
			{
				co.fwd();
				cr.fwd();
				cg.fwd();
				cb.fwd();
				co.get().setReal( Math.max(cr.get().get(), Math.max( cg.get().get(), cb.get().get() ) ) / 3.0 );
			}
			final long t1 = System.nanoTime();
			
			minLM = Math.min(minLM, t1 - t0);
			maxLM = Math.max(maxLM, t1 - t0);
			meanLM += (t1 - t0) / (double)(n_iterations);
		}
		
		System.out.println("Low-level math: min: " + (minLM / 1000.0) + " ms, max: " + (maxLM / 1000.0) + " ms, mean: " + (meanLM / 1000.0) + " ms");

		return true;
	}
	
	
	@Test
	public void test1() {
		assertTrue( testLoopMath1() );
	}
	
	@Test
	public void test2() {
		assertTrue ( comparePerformance( 30 ) );
	}
	
	static public void main(String[] args) {
		new LoopMathTest().test1();
	}
}
