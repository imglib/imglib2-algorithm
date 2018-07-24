package net.imglib2.algorithm.math;

import static net.imglib2.algorithm.math.ImgMath.Add;
import static net.imglib2.algorithm.math.ImgMath.Sub;
import static net.imglib2.algorithm.math.ImgMath.Mul;
import static net.imglib2.algorithm.math.ImgMath.Div;
import static net.imglib2.algorithm.math.ImgMath.Max;
import static net.imglib2.algorithm.math.ImgMath.Min;
import static net.imglib2.algorithm.math.ImgMath.Neg;
import static net.imglib2.algorithm.math.ImgMath.Let;
import static net.imglib2.algorithm.math.ImgMath.Var;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.math.ImgMath;
import net.imglib2.converter.Converters;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.cell.CellImg;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;


public class ImgMathTest
{
	protected static boolean testImgMath1( )
	{	
		final long[] dims = new long[]{ 100, 100, 100 };
		
		final ArrayImg< ARGBType, ? > rgb = new ArrayImgFactory< ARGBType >( new ARGBType() ).create( dims );
		
		final RandomAccessibleInterval< UnsignedByteType >
	    	red =  Converters.argbChannel( rgb, 1 ),
	    	green = Converters.argbChannel( rgb, 2 ),
	    	blue = Converters.argbChannel( rgb, 3 );
		
		for ( final UnsignedByteType t : Views.iterable( red ) ) t.set( 10 );
		for ( final UnsignedByteType t : Views.iterable( green ) ) t.set( 30 );
		for ( final UnsignedByteType t : Views.iterable( blue) ) t.set( 20 );
		
		final ArrayImg< FloatType, ? > brightness = new ArrayImgFactory< FloatType >( new FloatType() ).create( dims );
		
		try {
			new ImgMath< UnsignedByteType, FloatType >( new Div< FloatType >( new Max< FloatType >( red, new Max< FloatType >( green, blue ) ), 3.0 ) ).into( brightness );
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		double sum = 0;
		
		for ( final FloatType t: brightness )
			sum += t.getRealDouble();
		
		System.out.println( "Sum: " + sum );

		return 100 * 100 * 100 * 10 == sum;
	}
	
	protected static boolean testIterationOrder() {
		final UnsignedByteType type = new UnsignedByteType();
		final ArrayImg< UnsignedByteType, ? > img1 = new ArrayImgFactory<>( type ).create( new long[]{1024, 1024} );
		final CellImg< UnsignedShortType, ? > img2 = new CellImgFactory<>( new UnsignedShortType(), 20 ).create( img1 );
		
		// Write pixels with random, non-zero values
		for ( final UnsignedByteType t: img1 )
			t.set( 1 + (int)( Math.random() * ( type.getMaxValue() -1 ) ) );
		
		// Copy them to the CellImg
		final Cursor<UnsignedByteType> cursor = img1.cursor();
		final RandomAccess<UnsignedShortType> ra2 = img2.randomAccess();
		while (cursor.hasNext()) {
			cursor.fwd();
			ra2.setPosition(cursor);
			ra2.get().setInteger( cursor.get().getInteger() );
		}
		
		// Divide pixels from each other (better than subtract: zero sum would be the default in case of error)
		final ArrayImg< LongType, ? > img3 = new ArrayImgFactory<>( new LongType() ).create( img1 );
		try {
			new ImgMath< UnsignedByteType, LongType >( new Div< LongType >( img1, img2 ) ).into( img3 );
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Sum of pixels should add up to n_pixels
		long sum = 0;
		for ( final LongType t : img3 )
			sum += t.get();
		
		return img1.dimension( 0 ) * img1.dimension( 1 ) == sum;
	}
	
	protected static boolean comparePerformance( final int n_iterations ) {
		final long[] dims = new long[]{ 1024, 1024 };
		
		final ArrayImg< ARGBType, ? > rgb = new ArrayImgFactory< ARGBType >( new ARGBType() ).create( dims );
		
		final IterableInterval< UnsignedByteType >
			red = Views.iterable( Converters.argbChannel( rgb, 1 ) ),
			green = Views.iterable( Converters.argbChannel( rgb, 2 ) ),
			blue = Views.iterable( Converters.argbChannel( rgb, 3 ) );
		
		for ( final UnsignedByteType t : red ) t.set( 10 );
		for ( final UnsignedByteType t : green ) t.set( 30 );
		for ( final UnsignedByteType t : blue ) t.set( 20 );
		
		final ArrayImg< FloatType, ? > brightness = new ArrayImgFactory< FloatType >( new FloatType() ).create( dims );

		long minLM = Long.MAX_VALUE,
			 maxLM = 0;
		double meanLM = 0;
		
		for ( int i=0; i < n_iterations; ++i ) {
			final long t0 = System.nanoTime();
			try {
				new ImgMath< UnsignedByteType, FloatType >( new Div< FloatType >( new Max< FloatType >( red, new Max< FloatType >( green, blue ) ), 3.0 ) ).into( brightness );
			} catch (Exception e) {
				e.printStackTrace();
			}
			final long t1 = System.nanoTime();
			
			minLM = Math.min(minLM, t1 - t0);
			maxLM = Math.max(maxLM, t1 - t0);
			meanLM += (t1 - t0) / (double)(n_iterations);
		}
		
		System.out.println("ImgMath: min: " + (minLM / 1000.0) + " ms, max: " + (maxLM / 1000.0) + " ms, mean: " + (meanLM / 1000.0) + " ms");

		
		
		// Reset
		minLM = Long.MAX_VALUE;
		maxLM = 0;
		meanLM = 0;
		
		for ( int i=0; i < n_iterations; ++i ) {
			final long t0 = System.nanoTime();
			final Cursor< FloatType > co = brightness.cursor();
			final Cursor< UnsignedByteType > cr = red.cursor();
			final Cursor< UnsignedByteType > cg = green.cursor();
			final Cursor< UnsignedByteType > cb = blue.cursor();
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
	
	protected static boolean testVarags() {
		final long[] dims = new long[]{ 100, 100, 100 };
		
		final ArrayImg< ARGBType, ? > rgb = new ArrayImgFactory< ARGBType >( new ARGBType() ).create( dims );
		
		final IterableInterval< UnsignedByteType >
		    red = Views.iterable( Converters.argbChannel( rgb, 1 ) ),
			green = Views.iterable( Converters.argbChannel( rgb, 2 ) ),
			blue = Views.iterable( Converters.argbChannel( rgb, 3 ) );
		
		for ( final UnsignedByteType t : red ) t.set( 10 );
		for ( final UnsignedByteType t : green ) t.set( 30 );
		for ( final UnsignedByteType t : blue) t.set( 20 );
		
		final ArrayImg< FloatType, ? > brightness = new ArrayImgFactory< FloatType >( new FloatType() ).create( dims );
		
		try {
			new ImgMath< UnsignedByteType, FloatType >( new Div< FloatType >( new Max< FloatType >( red, green, blue ), 3.0 ) ).into( brightness );
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		double sum = 0;
		
		for ( final FloatType t: brightness )
			sum += t.getRealDouble();
		
		System.out.println( "Sum: " + sum );

		return 100 * 100 * 100 * 10 == sum;
	}
	
	static protected boolean testNeg() {
		final ArrayImg< FloatType, ? > in = new ArrayImgFactory< FloatType >( new FloatType() ).create( new long[]{ 10, 10 } );
		for ( final FloatType t : in )
			t.setOne();
		final ArrayImg< FloatType, ? > out = new ArrayImgFactory< FloatType >( new FloatType() ).create( new long[]{ 10, 10 } );
		
		try {
			new ImgMath< FloatType, FloatType >( new Neg< FloatType >( in ) ).into( out );
		} catch (Exception e) {
			e.printStackTrace();
		}

		double sum = 0;
		
		for ( final FloatType t: out )
			sum += t.getRealDouble();
		
		System.out.println( "Sum Neg: " + sum );

		
		return out.dimension( 0 ) * out.dimension( 1 ) == -sum;
	}
	
	protected boolean testLetOneLevel() {
		
		final ArrayImg< FloatType, ? > img = new ArrayImgFactory< FloatType >( new FloatType() ).create( new long[]{ 10, 10 } );
		final ArrayImg< FloatType, ? > target = new ArrayImgFactory< FloatType >(new FloatType() ).create( img );
		
		try {
			new ImgMath( new Let( "one", 1, new Add( img, new Var( "one" ) ) ) ).into( target );
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		double sum = 0;
		for ( final FloatType t : target )
			sum += t.getRealDouble();
		
		System.out.println( "Sum Let: " + sum );
		
		return 100 == sum;
	}
	
	protected boolean testLetTwoLevels() {
		
		final ArrayImg< FloatType, ? > img = new ArrayImgFactory< FloatType >( new FloatType() ).create( new long[]{ 10, 10 } );
		final ArrayImg< FloatType, ? > target = new ArrayImgFactory< FloatType >(new FloatType() ).create( img );
		
		try {
			new ImgMath( new Add( new Let( "one", 1,
					                       new Add( img, new Var( "one" ) ) ),
					              new Let( "two", 2,
					            		   new Add( new Var( "two" ), 0 ) ) ) ).into( target );
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		double sum = 0;
		for ( final FloatType t : target )
			sum += t.getRealDouble();
		
		System.out.println( "Two-level Let sum: " + sum );
		
		return 300 == sum;
	}
	
	protected boolean testMultiLet() {
		
		final ArrayImg< FloatType, ? > img = new ArrayImgFactory< FloatType >( new FloatType() ).create( new long[]{ 10, 10 } );
		final ArrayImg< FloatType, ? > target = new ArrayImgFactory< FloatType >(new FloatType() ).create( img );
		
		for ( final FloatType t : img )
			t.setReal( 100.0d );
		
		try {
			new ImgMath( new Let( "pixel", img,
					              "constant", 10.0d,
					              new Add( new Var( "pixel"), new Var( "constant" ) ) ) ).into( target );
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		double sum = 0;
		for ( final FloatType t : target )
			sum += t.getRealDouble();
		
		System.out.println( "Multi Let sum: " + sum );
		
		return (100 + 10) * 100 == sum;
	}
	
	protected boolean testNestedMultiLet() {
		
		final ArrayImg< FloatType, ? > img = new ArrayImgFactory< FloatType >( new FloatType() ).create( new long[]{ 10, 10 } );
		final ArrayImg< FloatType, ? > target = new ArrayImgFactory< FloatType >(new FloatType() ).create( img );
		
		for ( final FloatType t : img )
			t.setReal( 100.0d );
		
		try {
			new ImgMath( new Let( "pixel", img,
					              "constant", 10.0d,
					              new Add( new Var( "pixel"), new Let( "pixel2", img,
					            		                           new Sub( new Var( "pixel2" ), new Var( "constant" ) ) ) ) ) ).into( target );
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		double sum = 0;
		for ( final FloatType t : target )
			sum += t.getRealDouble();
		
		System.out.println( "Nested multi Let sum: " + sum );
		
		return (100 + 100 - 10) * 100 == sum;
	}
	
	
	//@Test
	public void test1() {
		assertTrue( testImgMath1() );
	}
	
	//@Test
	public void test2() {
		assertTrue( testIterationOrder() );
	}
	
	//@Test
	public void test3() {
		assertTrue ( comparePerformance( 30 ) );
	}
	
	//@Test
	public void test4() {
		assertTrue( testVarags() );
	}
	
	//@Test
	public void test5() {
		assertTrue( testNeg() );
	}
	
	@Test
	public void testLet1Simple() {
		assertTrue( testLetOneLevel() );
	}
	
	@Test
	public void testLet2Advanced() {
		assertTrue( testLetTwoLevels() );
	}
	
	@Test
	public void testLet3MultiVar() {
		assertTrue ( testMultiLet() );
	}
	
	@Test
	public void testLet4NestedMultiLet() {
		assertTrue ( testNestedMultiLet() );
	}
	
	static public void main(String[] args) {
		new ImgMathTest().test1();
	}
}
