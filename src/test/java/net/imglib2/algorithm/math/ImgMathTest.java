package net.imglib2.algorithm.math;

import static net.imglib2.algorithm.math.ImgMath.compute;
import static net.imglib2.algorithm.math.ImgMath.add;
import static net.imglib2.algorithm.math.ImgMath.sub;
import static net.imglib2.algorithm.math.ImgMath.mul;
import static net.imglib2.algorithm.math.ImgMath.div;
import static net.imglib2.algorithm.math.ImgMath.max;
import static net.imglib2.algorithm.math.ImgMath.min;
import static net.imglib2.algorithm.math.ImgMath.let;
import static net.imglib2.algorithm.math.ImgMath.var;
import static net.imglib2.algorithm.math.ImgMath.IF;
import static net.imglib2.algorithm.math.ImgMath.THEN;
import static net.imglib2.algorithm.math.ImgMath.ELSE;
import static net.imglib2.algorithm.math.ImgMath.EQ;
import static net.imglib2.algorithm.math.ImgMath.NEQ;
import static net.imglib2.algorithm.math.ImgMath.LT;
import static net.imglib2.algorithm.math.ImgMath.GT;

import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import javax.imageio.ImageIO;

import org.junit.Test;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.math.abstractions.IFunction;
import net.imglib2.algorithm.math.abstractions.Util;
import net.imglib2.converter.Converters;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.cell.CellImg;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;


public class ImgMathTest
{
	protected static boolean testImgMath1( )
	{	
		final long[] dims = new long[]{ 2, 2, 2 };
		
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
			compute( div( max( red, max( green, blue ) ), 3.0 ) ).into( brightness );
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		double sum = 0;
		
		for ( final FloatType t: brightness )
			sum += t.getRealDouble();
		
		System.out.println( "Sum: " + sum );

		return 2 * 2 * 2 * 10 == sum;
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
		compute( div( img1, img2 ) ).into( img3 );
		
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
			compute( div( max( red, max( green, blue ) ), 3.0 ) ).into( brightness );
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
		
		compute( div( max( red, green, blue ), 3.0 ) ).into( brightness );
		
		double sum = 0;
		
		for ( final FloatType t: brightness )
			sum += t.getRealDouble();
		
		System.out.println( "Sum: " + sum );

		return 100 * 100 * 100 * 10 == sum;
	}
	
	protected boolean testLetOneLevel() {
		
		final ArrayImg< FloatType, ? > img = new ArrayImgFactory< FloatType >( new FloatType() ).create( new long[]{ 10, 10 } );
		final ArrayImg< FloatType, ? > target = new ArrayImgFactory< FloatType >(new FloatType() ).create( img );
		
		compute( let( "one", 1, add( img, var( "one" ) ) ) ).into( target );
		
		double sum = 0;
		for ( final FloatType t : target )
			sum += t.getRealDouble();
		
		System.out.println( "Sum Let: " + sum );
		
		return 100 == sum;
	}
	
	protected boolean testLetTwoLets() {
		
		final ArrayImg< FloatType, ? > img = new ArrayImgFactory< FloatType >( new FloatType() ).create( new long[]{ 10, 10 } );
		final ArrayImg< FloatType, ? > target = new ArrayImgFactory< FloatType >(new FloatType() ).create( img );
		
		compute( add( let( "one", 1,
					       add( img, var( "one" ) ) ),
					  let( "two", 2,
					       add( var( "two" ), 0 ) ) ) ).into( target );
		
		double sum = 0;
		for ( final FloatType t : target )
			sum += t.getRealDouble();
		
		System.out.println( "Two Let sum: " + sum );
		
		return 300 == sum;
	}
	
	protected boolean testMultiLet() {
		
		final ArrayImg< FloatType, ? > img = new ArrayImgFactory< FloatType >( new FloatType() ).create( new long[]{ 10, 10 } );
		final ArrayImg< FloatType, ? > target = new ArrayImgFactory< FloatType >(new FloatType() ).create( img );
		
		for ( final FloatType t : img )
			t.setReal( 100.0d );
		
		compute( let( "pixel", img,
					  "constant", 10.0d,
					  add( var( "pixel"), var( "constant" ) ) ) ).into( target );
		
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
		
		compute( let( "pixel", img,
					  "constant", 10.0d,
					  add( var( "pixel"), let( "pixel2", img,
					            		       sub( var( "pixel2" ), var( "constant" ) ) ) ) ) ).into( target );
		
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
		
		final RandomAccessibleInterval< UnsignedByteType >
		    red = Converters.argbChannel( rgb, 1 ),
			green = Converters.argbChannel( rgb, 2 ),
			blue = Converters.argbChannel( rgb, 3 );
		
		final ArrayImg< FloatType, ? > saturation = new ArrayImgFactory< FloatType >( new FloatType() ).create( dims );
		
		// Compute saturation
		final IFunction f = let( "red", red,
					  "green", green,
					  "blue", blue,
					  "max", max( var( "red" ), var( "green" ), var( "blue" ) ),
					  "min", min( var( "red" ), var( "green" ), var( "blue" ) ),
					  IF ( EQ( 0, var( "max" ) ),
					       0,
					       div( sub( var( "max" ), var( "min" ) ),
					        	var( "max" ) ) ) );
		System.out.println("=== Hierarchy ===\n" + Util.hierarchy( f ) );
		compute( f ).into( saturation );
		
		final long sum = sumAsInts( saturation, 255.0 );
		
		// Ignore floating-point error
		System.out.println( "IfThenElse: saturation sum is " + sum + ", expected: " + saturation_sum );
		
		return sum == saturation_sum;
	}
	
	/**
	 * Emulate what ImageJ ij.process.ColorProcessor does: multiply by 255.0d and cast to int.
	 * 
	 * @param rai
	 * @param factor
	 * @return
	 */
	static private final < O extends RealType< O > > long sumAsInts( final RandomAccessibleInterval< O > rai, final double factor )
	{
		long sum = 0;
		
		for ( final O t : Views.iterable( rai ) )
			sum += (int)(t.getRealFloat() * factor);
		
		return sum;
	}
	
	protected boolean testSaturationPerformance( final int n_iterations, final boolean print )
	{	
		// Small cut out
		//final int[] leaf_pix = new int[]{-7430893, -6115530, -4275105, -3354241, -2960497, -3026034, -4012171, -4735905, -4538526, -3814794, -8219392, -7101930, -5589438, -3946389, -3223163, -3419766, -4538005, -5524656, -5589681, -4735134, -8153344, -7890174, -6773213, -5392819, -4406935, -4274320, -5260715, -6247114, -6444498, -6181573, -7890176, -8087552, -7562481, -6576333, -5590446, -5918133, -6509772, -7429350, -7495918, -7364580, -7955694, -8415744, -8349952, -8086528, -7626752, -7560684, -7955442, -8284407, -8218869, -7955692, -7692270, -8022011, -8219390, -8350201, -8022262, -8021490, -8350200, -8547579, -8481786, -8087283, -7561208, -7956218, -8351225, -8680439, -8812276, -8811001, -8876796, -8942336, -8744959, -8415994, -8087552, -8087552, -8679166, -9075199, -9536000, -9337600, -9271808, -9139968, -8876544, -8547584, -8679168, -8415744, -8415744, -9008384, -9666560, -9666816, -9534976, -9205760, -8942336, -8679168, -8548352, -8087552, -8086528, -8547328, -9074176, -9732608, -9534976, -9271552, -8942592, -8744960};
		//final int width = 10,
		//          height = 10;
		//final long saturation_sum = 21367;

		// Full image 507x446 pixels
		final int[] leaf_pix;
		final int width = 507,
				  height = 446;
		final long saturation_sum = 23641758; // Sum of saturation values * 255.0, each cast to int.
		
		try {
			final URL url = new URL( "https://imagej.nih.gov/ij/images/leaf.jpg" );
			//final URL url = new URL( "http://127.0.0.1/images/leaf.jpg" );
			final ReadableByteChannel rbc = Channels.newChannel( url.openStream() );
			final byte[] bytes = new byte[ 36642 ];
			final ByteBuffer bb = ByteBuffer.wrap( bytes );
			rbc.read( bb );
			
			final BufferedImage bi = ImageIO.read( new ByteArrayInputStream( bytes ) );
			leaf_pix = new int[width * height];
			final PixelGrabber pg = new PixelGrabber( bi, 0, 0, width, height, leaf_pix, 0, width );
			try {
				pg.grabPixels();
			} catch (InterruptedException e){};
			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		final long[] dims = new long[]{ width, height };
		
		// Fails, don't know why
		//final ArrayImg< ARGBType, IntArray > rgb = new ArrayImg< ARGBType, IntArray >( new IntArray( leaf_pix ), dims, new Fraction() );
		
		final ArrayImg< ARGBType, ? > rgb = new ArrayImgFactory< ARGBType >( new ARGBType() ).create( dims );
		int k = 0;
		for ( final ARGBType t : rgb )
		{
			t.set( leaf_pix[ k++ ] );
		}
		
		final IterableInterval< UnsignedByteType >
		    red = Views.iterable( Converters.argbChannel( rgb, 1 ) ),
			green = Views.iterable( Converters.argbChannel( rgb, 2 ) ),
			blue = Views.iterable( Converters.argbChannel( rgb, 3 ) );
		
		final ArrayImg< FloatType, ? > saturation = new ArrayImgFactory< FloatType >( new FloatType() ).create( dims );
		
		long minLM = Long.MAX_VALUE,
			 maxLM = 0;
		double meanLM = 0;
		
		// Compute saturation
		final Compute op1 =
		compute( let( "red", red,
					  "green", green,
					  "blue", blue,
					  "max", max( var( "red" ), var( "green" ), var( "blue" ) ),
					  "min", min( var( "red" ), var( "green" ), var( "blue" ) ),
					  IF ( EQ( 0, var( "max" ) ),
						   0,
						   mul( div( sub( var( "max" ), var( "min" ) ),
								     var( "max" ) ),
								255.0f ) ) ) );
			
		for ( int i=0; i < n_iterations; ++i )
		{
			final long t0 = System.nanoTime();
			
			op1.into( saturation );

			final long t1 = System.nanoTime();
			minLM = Math.min(minLM, t1 - t0);
			maxLM = Math.max(maxLM, t1 - t0);
			meanLM += (t1 - t0) / (double)(n_iterations);
		}
		
		assertTrue( "op1", sumAsInts( saturation, 1.0 ) == saturation_sum );

		if ( print ) System.out.println("ImgMath 1 saturation performance: min: " + (minLM / 1000.0) + " ms, max: " + (maxLM / 1000.0) + " ms, mean: " + (meanLM / 1000.0) + " ms");

		// Reset
		minLM = Long.MAX_VALUE;
		maxLM = 0;
		meanLM = 0;
		
		// Compute saturation: very slightly slower than with vars (unsurprisingly), but easier to read
		final Compute op2 =
		compute( let( "max", max( red, green, blue ),
					  "min", min( red, green, blue ),
					  IF ( EQ( 0, var( "max" ) ),
						   0,
						   div( sub( var( "max" ), var( "min" ) ),
							    var( "max" ) ) ) ) );
				
		for ( int i=0; i < n_iterations; ++i ) {
			final long t0 = System.nanoTime();

			op2.into( saturation );

			final long t1 = System.nanoTime();
			minLM = Math.min(minLM, t1 - t0);
			maxLM = Math.max(maxLM, t1 - t0);
			meanLM += (t1 - t0) / (double)(n_iterations);
		}
		
		if ( print ) System.out.println( "Sum: " + sumAsInts( saturation, 255.0 ) + ", saturation_sum: " + saturation_sum );
		
		assertTrue( "op2", sumAsInts( saturation, 255.0 ) == saturation_sum );

		if ( print ) System.out.println("ImgMath 2 saturation performance: min: " + (minLM / 1000.0) + " ms, max: " + (maxLM / 1000.0) + " ms, mean: " + (meanLM / 1000.0) + " ms");

		// Reset
		minLM = Long.MAX_VALUE;
		maxLM = 0;
		meanLM = 0;

		// Compute saturation: easier to read without vars
		final Compute op3 =
		compute( let( "max", max( red, green, blue ),
					  "min", min( red, green, blue ),
					  IF ( EQ( 0, var( "max" ) ),
				        THEN( 0 ),
					    ELSE ( div( sub( var( "max" ), var( "min" ) ),
						            var( "max" ) ) ) ) ) );
		
		for ( int i=0; i < n_iterations; ++i ) {
			final long t0 = System.nanoTime();
			
			op3.into( saturation );

			final long t1 = System.nanoTime();
			minLM = Math.min(minLM, t1 - t0);
			maxLM = Math.max(maxLM, t1 - t0);
			meanLM += (t1 - t0) / (double)(n_iterations);
		}
		
		assertTrue( "op3", sumAsInts( saturation, 255.0 ) == saturation_sum );

		if ( print ) System.out.println("ImgMath 3 saturation performance: min: " + (minLM / 1000.0) + " ms, max: " + (maxLM / 1000.0) + " ms, mean: " + (meanLM / 1000.0) + " ms");

		// Reset
		minLM = Long.MAX_VALUE;
		maxLM = 0;
		meanLM = 0;
		
		// Make color channels of the same type as output image
		{
			final ArrayImg< FloatType, ? >
				redF = new ArrayImgFactory< FloatType >( new FloatType() ).create( dims ),
				greenF = new ArrayImgFactory< FloatType >( new FloatType() ).create( dims ),
				blueF = new ArrayImgFactory< FloatType >( new FloatType() ).create( dims );
			final Cursor< FloatType >
				cr = redF.cursor(),
				cg = greenF.cursor(),
				cb = blueF.cursor();
			final Cursor< UnsignedByteType >
				c = red.cursor(),
				g = green.cursor(),
				b = blue.cursor();
			while ( cr.hasNext() ) {
				cr.next().setReal( c.next().getRealDouble() );
				cg.next().setReal( g.next().getRealDouble() );
				cb.next().setReal( b.next().getRealDouble() );
			}

			// Compute saturation:
			final Compute op4 =
			compute( let( "max", max( redF, greenF, blueF ),
						  "min", min( redF, greenF, blueF ),
						  IF ( EQ( 0, var( "max" ) ),
							  THEN( 0 ),
							  ELSE ( div( sub( var( "max" ), var( "min" ) ),
									 var( "max" ) ) ) ) ) );
			
			for ( int i=0; i < n_iterations; ++i ) {
				final long t0 = System.nanoTime();

				op4.into( saturation );

				final long t1 = System.nanoTime();
				minLM = Math.min(minLM, t1 - t0);
				maxLM = Math.max(maxLM, t1 - t0);
				meanLM += (t1 - t0) / (double)(n_iterations);
			}
			
			assertTrue( "op4", sumAsInts( saturation, 255.0 ) == saturation_sum );
			
			if ( print ) System.out.println("ImgMath 4 saturation without vars performance: min: " + (minLM / 1000.0) + " ms, max: " + (maxLM / 1000.0) + " ms, mean: " + (meanLM / 1000.0) + " ms");
		
			// Reset
			minLM = Long.MAX_VALUE;
			maxLM = 0;
			meanLM = 0;

			// Compute saturation:
			final Compute op5 =
			compute( let( "red", redF,
					      "green", greenF,
					      "blue", blueF,
					      "max", max( var("red"), var("green"), var("blue") ),
					      "min", min( var("red"), var("green"), var("blue") ),
					      IF ( EQ( 0, var( "max" ) ),
					    	THEN( 0 ),
					    	ELSE ( div( sub( var( "max" ), var( "min" ) ),
					    		   var( "max" ) ) ) ) ) );
			
			for ( int i=0; i < n_iterations; ++i ) {
				final long t0 = System.nanoTime();

				op5.into( saturation );

				final long t1 = System.nanoTime();
				minLM = Math.min(minLM, t1 - t0);
				maxLM = Math.max(maxLM, t1 - t0);
				meanLM += (t1 - t0) / (double)(n_iterations);
			}
			
			assertTrue( "op5", sumAsInts( saturation, 255.0 ) == saturation_sum );
			
			if ( print ) System.out.println("ImgMath 5 saturation with vars performance: min: " + (minLM / 1000.0) + " ms, max: " + (maxLM / 1000.0) + " ms, mean: " + (meanLM / 1000.0) + " ms");
		
		}
		
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
				final int r = cr.next().getInteger(),
						  g = cg.next().getInteger(),
						  b = cb.next().getInteger();
				final float max = Math.max( r, Math.max( g, b ) ),
						    min = Math.min( r, Math.min( g, b ) );
				cs.next().set( 0.0f == max ? 0.0f : (max - min) / max );
			}

			final long t1 = System.nanoTime();
			minLM = Math.min(minLM, t1 - t0);
			maxLM = Math.max(maxLM, t1 - t0);
			meanLM += (t1 - t0) / (double)(n_iterations);
		}
		
		assertTrue( "low-level", sumAsInts( saturation, 255.0 ) == saturation_sum );

		if ( print ) System.out.println("Low-level saturation performance: min: " + (minLM / 1000.0) + " ms, max: " + (maxLM / 1000.0) + " ms, mean: " + (meanLM / 1000.0) + " ms");
		
		return true;
	}
	
	protected boolean testLT()
	{
		final ArrayImg< FloatType, ? > img1 = new ArrayImgFactory< FloatType >( new FloatType() ).create( new long[]{ 10, 10 } );
		final ArrayImg< FloatType, ? > img2 = new ArrayImgFactory< FloatType >( new FloatType() ).create( new long[]{ 10, 10 } );

		// Fill half the image only
		for ( final FloatType t : Views.interval( img1, new long[]{ 0, 0 }, new long[] { 9, 4 } ) ) // inclusive
			t.setReal( 5.0f );

		final ArrayImg< FloatType, ? > target = new ArrayImgFactory< FloatType >(new FloatType() ).create( img1 );
		
		compute( LT( img2, img1 ) ).into( target );
		
		double sum = 0;
		for ( final FloatType t : target )
			sum += t.getRealDouble();
		
		System.out.println( "Sum LT 1/0: " + sum );
		
		// A "1" for every true comparison, one per pixel for half of all pixels
		return 50 == sum;
	}
	
	protected boolean testGT()
	{
		final ArrayImg< FloatType, ? > img1 = new ArrayImgFactory< FloatType >( new FloatType() ).create( new long[]{ 10, 10 } );
		final ArrayImg< FloatType, ? > img2 = new ArrayImgFactory< FloatType >( new FloatType() ).create( new long[]{ 10, 10 } );

		// Fill half the image only
		for ( final FloatType t : Views.interval( img1, new long[]{ 0, 0 }, new long[] { 9, 4 } ) ) // inclusive
			t.setReal( 5.0f );

		final ArrayImg< FloatType, ? > target = new ArrayImgFactory< FloatType >(new FloatType() ).create( img1 );
		
		compute( GT( img1, img2 ) ).into( target );
		
		double sum = 0;
		for ( final FloatType t : target )
			sum += t.getRealDouble();
		
		System.out.println( "Sum GT 1/0: " + sum );
		
		// A "1" for every true comparison, one per pixel for half of all pixels
		return 50 == sum;
	}
	
	
	@Test
	public void test1() {
		System.out.println("test1");
		assertTrue( testImgMath1() );
	}
	
	@Test
	public void test2() {
		System.out.println("test2");
		assertTrue( testIterationOrder() );
	}
	
	//@Test
	public void test3() {
		assertTrue ( comparePerformance( 30, false ) ); // warm up
		assertTrue ( comparePerformance( 30, true ) );
	}
	
	@Test
	public void test4() {
		System.out.println("test4");
		assertTrue( testVarags() );
	}
	
	@Test
	public void testLet1Simple() {
		System.out.println("testLet1Simple");
		assertTrue( testLetOneLevel() );
	}
	
	@Test
	public void testLet2Advanced() {
		System.out.println("testLet2Advanced");
		assertTrue( testLetTwoLets() );
	}
	
	@Test
	public void testLet3MultiVar() {
		System.out.println("testLet3MultiVar");
		assertTrue ( testMultiLet() );
	}
	
	@Test
	public void testLet4NestedMultiLet() {
		System.out.println("testLet4NestedMultiLet");
		assertTrue ( testNestedMultiLet() );
	}
	
	@Test
	public void test1IfThenElse() {
		System.out.println("test1IfThenElse");
		assertTrue ( testIfThenElse() );
	}
	
	//@Test
	public void test1IfThenElsePerformance() {
		//assertTrue ( testSaturationPerformance( 200, false ) ); // warm-up
		assertTrue ( testSaturationPerformance( 30, true ) );
	}
	
	@Test
	public void test5LT() {
		System.out.println("test5LT");
		assertTrue( testLT() );
	}
	
	@Test
	public void test6GT() {
		System.out.println("test6GT");
		assertTrue( testGT() );
	}
	
	static public void main(String[] args) {
		new ImgMathTest().test1();
	}
}
