package net.imglib2.algorithm.util;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.Localizable;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.img.Img;
import net.imglib2.img.ImgView;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Cast;
import net.imglib2.util.Intervals;
import net.imglib2.util.Localizables;
import net.imglib2.view.Views;

/**
 * Utility class to help create test images.
 */
public class TestImages
{
	public static <T extends NativeType<T>> Img<T> createImg( T type, Interval interval ) {
		ArrayImgFactory<T> factory = new ArrayImgFactory<>( type );
		Img<T> zeroMinImg = factory.create( interval );
		long[] offset = Intervals.minAsLongArray( interval );
		RandomAccessibleInterval<T> rai = Views.translate( zeroMinImg, offset );
		return ImgView.wrap( rai, factory );
	}

	/**
	 * @returns an image of given dimensions. The pixel values are set as
	 * specified in the content function. The image is read only.
	 */
	public static Img<DoubleType> doubles2d( int width, int height, DoubleFunction2d content ) {
		Interval interval = new FinalInterval( width, height );
		return doubles2d( interval, content );
	}

	/**
	 * @returns an image of given dimensions. The pixel values are set as
	 * specified in the content function. The image is read only.
	 */
	public static Img<DoubleType> doubles2d( Interval interval, DoubleFunction2d content )
	{
		return generateImg2d( interval, new DoubleType(), (x, y, pixel) -> pixel.set( content.apply( x, y ) ) );
	}

	/**
	 * @returns an image of given dimensions. The pixel values are set as
	 * specified in the content function. The image is read only.
	 */
	public static Img<BitType> bits2d( int width, int height, BooleanFunction2d content ) {
		Interval interval = new FinalInterval( width, height );
		return bits2d( interval, content );
	}

	/**
	 * @returns an image of given dimensions. The pixel values are set as
	 * specified in the content function. The image is read only.
	 */
	private static Img<BitType> bits2d( Interval interval, BooleanFunction2d content )
	{
		return generateImg2d( interval, new BitType(), (x, y, pixel) -> pixel.set( content.apply( x, y ) ) );
	}

	/**
	 * @returns an image of given dimensions. The pixel values are set as
	 * specified in the content function. The image is read only.
	 */
	public static Img<IntType> ints2d( int width, int height, IntFunction2d content )
	{
		Interval interval = new FinalInterval( width, height );
		return generateImg2d( interval, new IntType(), (x, y, pixel) -> pixel.set( content.apply( x, y ) ) );
	}

	public static <T extends Type<T>> Img<T> generateImg2d( Interval interval, T type, PixelFunction2d<T> function )
	{
		Converter<Localizable, T> converter = (position, pixel) -> {
			int x = position.getIntPosition( 0 );
			int y = position.getIntPosition( 1 );
			function.apply( x, y, pixel );
		};
		return ImgView.wrap( Converters.convert( Intervals.positions( interval ), converter, type ), Cast.unchecked( new ArrayImgFactory<>( Cast.unchecked( type ) ) ) );
	}

	/**
	 * @return a two dimensional infinitely large image. (see {@link RandomAccessible})
	 * The pixel a the origin is 1. All other pixels are zero.
	 */
	public static RandomAccessible<DoubleType> dirac2d()
	{
		return doubles2d( (x, y) -> (x == 0 && y == 0) ? 1 : 0 );
	}

	/**
	 * @returns a two dimensional infinitely large image. The pixel values are
	 * as given by the specified content function.
	 */
	public static RandomAccessible<DoubleType> doubles2d( DoubleFunction2d content ) {
		Converter<Localizable, DoubleType> converter = (position, pixel) -> {
			int x = position.getIntPosition( 0 );
			int y = position.getIntPosition( 1 );
			pixel.setReal( content.apply( x, y ) );
		};
		return Converters.convert( Localizables.randomAccessible( 2 ), converter, new DoubleType() );
	}

	public interface PixelFunction2d<T>
	{
		void apply( int x, int y, T pixel );
	}

	public interface DoubleFunction2d
	{
		double apply( int x, int y );
	}

	public interface BooleanFunction2d
	{
		boolean apply( int x, int y );
	}

	public interface IntFunction2d
	{
		int apply( int x, int y );
	}
}
