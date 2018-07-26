package net.imglib2.algorithm.math;

import static net.imglib2.algorithm.math.ImgMath.add;
import static net.imglib2.algorithm.math.ImgMath.sub;
import static net.imglib2.algorithm.math.ImgMath.mul;
import static net.imglib2.algorithm.math.ImgMath.div;
import static net.imglib2.algorithm.math.ImgMath.max;
import static net.imglib2.algorithm.math.ImgMath.min;
import static net.imglib2.algorithm.math.ImgMath.let;
import static net.imglib2.algorithm.math.ImgMath.var;
import static net.imglib2.algorithm.math.ImgMath.IF;
import static net.imglib2.algorithm.math.ImgMath.EQ;
import static net.imglib2.algorithm.math.ImgMath.NEQ;
import static net.imglib2.algorithm.math.ImgMath.LT;
import static net.imglib2.algorithm.math.ImgMath.GT;

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
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.img.cell.CellImg;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Fraction;
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
			new ImgMath( div( max( red, max( green, blue ) ), 3.0 ) ).into( brightness );
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
			new ImgMath( div( img1, img2 ) ).into( img3 );
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Sum of pixels should add up to n_pixels
		long sum = 0;
		for ( final LongType t : img3 )
			sum += t.get();
		
		return img1.dimension( 0 ) * img1.dimension( 1 ) == sum;
	}
	
	protected static boolean comparePerformance( final int n_iterations, final boolean print ) {
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
				new ImgMath( div( max( red, max( green, blue ) ), 3.0 ) ).into( brightness );
			} catch (Exception e) {
				e.printStackTrace();
			}
			final long t1 = System.nanoTime();
			
			minLM = Math.min(minLM, t1 - t0);
			maxLM = Math.max(maxLM, t1 - t0);
			meanLM += (t1 - t0) / (double)(n_iterations);
		}
		
		if ( print ) System.out.println("ImgMath: min: " + (minLM / 1000.0) + " ms, max: " + (maxLM / 1000.0) + " ms, mean: " + (meanLM / 1000.0) + " ms");

		
		
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
		
		if ( print ) System.out.println("Low-level math: min: " + (minLM / 1000.0) + " ms, max: " + (maxLM / 1000.0) + " ms, mean: " + (meanLM / 1000.0) + " ms");

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
			new ImgMath( div( max( red, green, blue ), 3.0 ) ).into( brightness );
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		double sum = 0;
		
		for ( final FloatType t: brightness )
			sum += t.getRealDouble();
		
		System.out.println( "Sum: " + sum );

		return 100 * 100 * 100 * 10 == sum;
	}
	
	protected boolean testLetOneLevel() {
		
		final ArrayImg< FloatType, ? > img = new ArrayImgFactory< FloatType >( new FloatType() ).create( new long[]{ 10, 10 } );
		final ArrayImg< FloatType, ? > target = new ArrayImgFactory< FloatType >(new FloatType() ).create( img );
		
		try {
			new ImgMath( let( "one", 1, add( img, var( "one" ) ) ) ).into( target );
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
			new ImgMath( add( let( "one", 1,
					               add( img, var( "one" ) ) ),
					               let( "two", 2,
					            		add( var( "two" ), 0 ) ) ) ).into( target );
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
			new ImgMath( let( "pixel", img,
					          "constant", 10.0d,
					          add( var( "pixel"), var( "constant" ) ) ) ).into( target );
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
			new ImgMath( let( "pixel", img,
					          "constant", 10.0d,
					          add( var( "pixel"), let( "pixel2", img,
					            		                sub( var( "pixel2" ), var( "constant" ) ) ) ) ) ).into( target );
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		double sum = 0;
		for ( final FloatType t : target )
			sum += t.getRealDouble();
		
		System.out.println( "Nested multi Let sum: " + sum );
		
		return (100 + 100 - 10) * 100 == sum;
	}
	
	protected boolean testIfThenElse() {
		
		final int[] leaf_crop_pix = new int[]{-7430893, -6115530, -4275105, -3354241, -2960497, -3026034, -4012171, -4735905, -4538526, -3814794, -8219392, -7101930, -5589438, -3946389, -3223163, -3419766, -4538005, -5524656, -5589681, -4735134, -8153344, -7890174, -6773213, -5392819, -4406935, -4274320, -5260715, -6247114, -6444498, -6181573, -7890176, -8087552, -7562481, -6576333, -5590446, -5918133, -6509772, -7429350, -7495918, -7364580, -7955694, -8415744, -8349952, -8086528, -7626752, -7560684, -7955442, -8284407, -8218869, -7955692, -7692270, -8022011, -8219390, -8350201, -8022262, -8021490, -8350200, -8547579, -8481786, -8087283, -7561208, -7956218, -8351225, -8680439, -8812276, -8811001, -8876796, -8942336, -8744959, -8415994, -8087552, -8087552, -8679166, -9075199, -9536000, -9337600, -9271808, -9139968, -8876544, -8547584, -8679168, -8415744, -8415744, -9008384, -9666560, -9666816, -9534976, -9205760, -8942336, -8679168, -8548352, -8087552, -8086528, -8547328, -9074176, -9732608, -9534976, -9271552, -8942592, -8744960};
		final long saturation_sum = 21367;
		
		final long[] dims = new long[]{ 10, 10 };
		
		// Fails, don't know why
		//final ArrayImg< ARGBType, IntArray > rgb = new ArrayImg< ARGBType, IntArray >( new IntArray( leaf_crop_pix ), dims, new Fraction() );
		
		final ArrayImg< ARGBType, ? > rgb = new ArrayImgFactory< ARGBType >( new ARGBType() ).create( dims );
		int i = 0;
		for ( final ARGBType t : rgb )
		{
			t.set( leaf_crop_pix[ i++ ] );
		}
		
		final IterableInterval< UnsignedByteType >
		    red = Views.iterable( Converters.argbChannel( rgb, 1 ) ),
			green = Views.iterable( Converters.argbChannel( rgb, 2 ) ),
			blue = Views.iterable( Converters.argbChannel( rgb, 3 ) );
		
		final ArrayImg< FloatType, ? > saturation = new ArrayImgFactory< FloatType >( new FloatType() ).create( dims );
		
		// Compute saturation
		try {
			new ImgMath( let( "red", red,
					          "green", green,
					          "blue", blue,
					          "max", max( var( "red" ), var( "green" ), var( "blue" ) ),
					          "min", min( var( "red" ), var( "green" ), var( "blue" ) ),
					          IF ( EQ( 0, var( "max" ) ),
					        	   0,
					        	   div( sub( var( "max" ), var( "min" ) ),
					        			var( "max" ) ) ) ) )
			.into( saturation );

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		float sum = 0;
		
		for ( final FloatType t : saturation )
			sum += t.get();
		
		// Ignore floating-point error
		System.out.println( "IfThenElse: saturation sum is " + (int)sum + ", expected: " + (int)(saturation_sum / 255.0f) );
		
		return (int)sum == (int)(saturation_sum / 255.0f);
	}
	
	protected boolean testSaturationPerformance( final int n_iterations, final boolean print ) {
		final int[] leaf_crop_pix = new int[]{-7430893, -6115530, -4275105, -3354241, -2960497, -3026034, -4012171, -4735905, -4538526, -3814794, -8219392, -7101930, -5589438, -3946389, -3223163, -3419766, -4538005, -5524656, -5589681, -4735134, -8153344, -7890174, -6773213, -5392819, -4406935, -4274320, -5260715, -6247114, -6444498, -6181573, -7890176, -8087552, -7562481, -6576333, -5590446, -5918133, -6509772, -7429350, -7495918, -7364580, -7955694, -8415744, -8349952, -8086528, -7626752, -7560684, -7955442, -8284407, -8218869, -7955692, -7692270, -8022011, -8219390, -8350201, -8022262, -8021490, -8350200, -8547579, -8481786, -8087283, -7561208, -7956218, -8351225, -8680439, -8812276, -8811001, -8876796, -8942336, -8744959, -8415994, -8087552, -8087552, -8679166, -9075199, -9536000, -9337600, -9271808, -9139968, -8876544, -8547584, -8679168, -8415744, -8415744, -9008384, -9666560, -9666816, -9534976, -9205760, -8942336, -8679168, -8548352, -8087552, -8086528, -8547328, -9074176, -9732608, -9534976, -9271552, -8942592, -8744960};
		final long saturation_sum = 21367;
		
		final long[] dims = new long[]{ 10, 10 };
		
		// Fails, don't know why
		//final ArrayImg< ARGBType, IntArray > rgb = new ArrayImg< ARGBType, IntArray >( new IntArray( leaf_crop_pix ), dims, new Fraction() );
		
		final ArrayImg< ARGBType, ? > rgb = new ArrayImgFactory< ARGBType >( new ARGBType() ).create( dims );
		int k = 0;
		for ( final ARGBType t : rgb )
		{
			t.set( leaf_crop_pix[ k++ ] );
		}
		
		final IterableInterval< UnsignedByteType >
		    red = Views.iterable( Converters.argbChannel( rgb, 1 ) ),
			green = Views.iterable( Converters.argbChannel( rgb, 2 ) ),
			blue = Views.iterable( Converters.argbChannel( rgb, 3 ) );
		
		final ArrayImg< FloatType, ? > saturation = new ArrayImgFactory< FloatType >( new FloatType() ).create( dims );
		
		long minLM = Long.MAX_VALUE,
			 maxLM = 0;
		double meanLM = 0;
			
		for ( int i=0; i < n_iterations; ++i ) {
			final long t0 = System.nanoTime();

			// Compute saturation
			try {
				new ImgMath( let( "red", red,
						"green", green,
						"blue", blue,
						"max", max( var( "red" ), var( "green" ), var( "blue" ) ),
						"min", min( var( "red" ), var( "green" ), var( "blue" ) ),
						IF ( EQ( 0, var( "max" ) ),
								0,
								div( sub( var( "max" ), var( "min" ) ),
										var( "max" ) ) ) ) )
				.into( saturation );

			} catch (Exception e) {
				e.printStackTrace();
			}

			final long t1 = System.nanoTime();
			minLM = Math.min(minLM, t1 - t0);
			maxLM = Math.max(maxLM, t1 - t0);
			meanLM += (t1 - t0) / (double)(n_iterations);
		}

		if ( print ) System.out.println("ImgMath 1 saturation performance: min: " + (minLM / 1000.0) + " ms, max: " + (maxLM / 1000.0) + " ms, mean: " + (meanLM / 1000.0) + " ms");

		// Reset
		minLM = Long.MAX_VALUE;
		maxLM = 0;
		meanLM = 0;
				
		for ( int i=0; i < n_iterations; ++i ) {
			final long t0 = System.nanoTime();

			// Compute saturation: slightly faster than above (surprisingly), and easier to read
			try {
				new ImgMath( let( "max", max( red, green, blue ),
								  "min", min( red, green, blue ),
								  IF ( EQ( 0, var( "max" ) ),
									   0,
								       div( sub( var( "max" ), var( "min" ) ),
										    var( "max" ) ) ) ) )
				.into( saturation );

			} catch (Exception e) {
				e.printStackTrace();
			}

			final long t1 = System.nanoTime();
			minLM = Math.min(minLM, t1 - t0);
			maxLM = Math.max(maxLM, t1 - t0);
			meanLM += (t1 - t0) / (double)(n_iterations);
		}

		if ( print ) System.out.println("ImgMath 2 saturation performance: min: " + (minLM / 1000.0) + " ms, max: " + (maxLM / 1000.0) + " ms, mean: " + (meanLM / 1000.0) + " ms");

		
		// Reset
		minLM = Long.MAX_VALUE;
		maxLM = 0;
		meanLM = 0;
						
		for ( int i=0; i < n_iterations; ++i ) {
			final long t0 = System.nanoTime();

			// Compute saturation
			final Cursor< UnsignedByteType > cr = red.cursor(),
					                         cg = green.cursor(),
					                         cb = blue.cursor();
			
			final Cursor< FloatType > cs = saturation.cursor(); 
			
			while (cs.hasNext()) {
				final UnsignedByteType r = cr.next(),
						               g = cg.next(),
						               b = cb.next();
				final float max = Math.max( r.get(), Math.max( g.get(), b.get() ) ),
						    min = Math.min( r.get(), Math.min( g.get(), b.get() ) );
				cs.next().set( 0.0f == max ? 0.0f : (max - min) / max );
			}

			final long t1 = System.nanoTime();
			minLM = Math.min(minLM, t1 - t0);
			maxLM = Math.max(maxLM, t1 - t0);
			meanLM += (t1 - t0) / (double)(n_iterations);
		}

		if ( print ) System.out.println("Low-level saturation performance: min: " + (minLM / 1000.0) + " ms, max: " + (maxLM / 1000.0) + " ms, mean: " + (meanLM / 1000.0) + " ms");
		
		return true;
	}
	
	
	@Test
	public void test1() {
		assertTrue( testImgMath1() );
	}
	
	@Test
	public void test2() {
		assertTrue( testIterationOrder() );
	}
	
	@Test
	public void test3() {
		assertTrue ( comparePerformance( 30, false ) ); // warm up
		assertTrue ( comparePerformance( 30, true ) );
	}
	
	@Test
	public void test4() {
		assertTrue( testVarags() );
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
	
	@Test
	public void test1IfThenElse() {
		assertTrue ( testIfThenElse() );
	}
	
	@Test
	public void test1IfThenElsePerformance() {
		assertTrue ( testSaturationPerformance( 30, false ) ); // warm-up
		assertTrue ( testSaturationPerformance( 30, true ) );

	}
	
	static public void main(String[] args) {
		new ImgMathTest().test1();
	}
}
