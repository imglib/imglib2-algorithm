/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2023 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
 * John Bogovic, Albert Cardona, Barry DeZonia, Christian Dietz, Jan Funke,
 * Aivar Grislis, Jonathan Hale, Grant Harris, Stefan Helfrich, Mark Hiner,
 * Martin Horn, Steffen Jaensch, Lee Kamentsky, Larry Lindsey, Melissa Linkert,
 * Mark Longair, Brian Northan, Nick Perry, Curtis Rueden, Johannes Schindelin,
 * Jean-Yves Tinevez and Michael Zinsmaier.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package net.imglib2.algorithm.gauss3;

import net.imglib2.IterableInterval;
import net.imglib2.Localizable;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.convolution.fast_gauss.FastGauss;
import net.imglib2.algorithm.convolution.kernel.Kernel1D;
import net.imglib2.algorithm.convolution.kernel.SeparableKernelConvolution;
import net.imglib2.algorithm.gradient.PartialDerivative;
import net.imglib2.converter.Converters;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Localizables;
import net.imglib2.util.Util;
import net.imglib2.view.Views;
import org.junit.Test;

import java.util.function.BiFunction;

import static org.junit.Assert.fail;

/**
 * Tests {@link Gauss3} and {@link FastGauss}.
 */
public class GaussTest< T extends RealType< T > & NativeType< T > >
{

	private T type = ( T ) new DoubleType();

	private double sigma = 4;

	private long center = (long) ( 12 * sigma );

	private long width = center * 2;

	private RandomAccessibleInterval< T > input = scaleAndAddOffset( dirac() );

	private RandomAccessibleInterval< T > expected = scaleAndAddOffset( idealGaussian( sigma ) );

	@Test
	public void testGauss3()
	{
		RandomAccessibleInterval< T > result = createEmptyImage();
		Gauss3.gauss( sigma, Views.extendBorder( input ), result );
		assertImagesEqual( 40, subtractOffset( expected ), subtractOffset( result ) );
		assertImagesEqual( 35, deriveX( expected ), deriveX( result ) );
		assertImagesEqual( 24, secondDerivativeX( expected ), secondDerivativeX( result ) );
	}

	@Test
	public void testFastGauss()
	{
		RandomAccessibleInterval< T > result = createEmptyImage();
		FastGauss.convolve( sigma, Views.extendBorder( input ), result );
		assertImagesEqual( 50, subtractOffset( expected ), subtractOffset( result ) );
		assertImagesEqual( 45, deriveX( expected ), deriveX( result ) );
		assertImagesEqual( 34, secondDerivativeX( expected ), secondDerivativeX( result ) );
	}

	// -- Helper methods --

	private RandomAccessibleInterval< T > subtractOffset( RandomAccessibleInterval< ? extends RealType< ? > > image )
	{
		return Converters.convert( image, ( i, o ) -> o.setReal( i.getRealDouble() - 80 ), type );
	}

	private RandomAccessibleInterval< T > scaleAndAddOffset( RandomAccessibleInterval< T > dirac )
	{
		LoopBuilder.setImages( dirac ).forEachPixel( pixel -> pixel.setReal( 20 * pixel.getRealDouble() + 80 ) );
		return dirac;
	}

	private RandomAccessibleInterval< T > idealGaussian( double sigma )
	{
		return createImage( ( x, y ) -> gauss( sigma, x ) * gauss( sigma, y ) );
	}

	private double gauss( double sigma, double x )
	{
		double a = 1. / Math.sqrt( 2 * Math.PI * Math.pow( sigma, 2 ) );
		double b = -0.5 / Math.pow( sigma, 2 );
		return a * Math.exp( b * Math.pow( x, 2 ) );
	}

	private RandomAccessibleInterval< T > dirac()
	{
		return createImage( ( x, y ) -> ( x == 0 ) && ( y == 0 ) ? 1. : 0. );
	}

	private RandomAccessibleInterval< T > createImage( BiFunction< Long, Long, Double > content )
	{
		Img< T > image = createEmptyImage();
		RandomAccessibleInterval< Localizable > positions = Views.interval( Localizables.randomAccessible( image.numDimensions() ), image );
		LoopBuilder.setImages( positions, image ).forEachPixel( ( p, pixel ) -> {
			long x = p.getLongPosition( 0 ) - center;
			long y = p.getLongPosition( 1 ) - center;
			pixel.setReal( content.apply( x, y ) );
		} );
		return image;
	}

	private Img< T > createEmptyImage()
	{
		return new ArrayImgFactory<>( type ).create( width, width );
	}

	private < T extends RealType< T > & NativeType< T > > RandomAccessibleInterval< T > deriveX( RandomAccessibleInterval< T > input )
	{
		Img< T > result = new ArrayImgFactory<>( Util.getTypeFromInterval( input ) ).create( Intervals.dimensionsAsLongArray( input ) );
		PartialDerivative.gradientCentralDifference( Views.extendBorder( input ), result, 0 );
		return result;
	}

	private RandomAccessibleInterval< T > secondDerivativeX( RandomAccessibleInterval< ? extends RealType< ? > > input )
	{
		Img< T > result = createEmptyImage();
		SeparableKernelConvolution.convolution1d( Kernel1D.centralAsymmetric( 1, -2, 1 ), 0 )
				.process( Views.extendBorder( input ), result );
		return result;
	}

	private void assertImagesEqual( int expectedSnr, RandomAccessibleInterval< ? extends RealType< ? > > a, RandomAccessibleInterval< T > b )
	{
		double actualSnr = snr( a, b );
		if ( Double.isNaN( actualSnr ) )
			fail( "Either the expected or the actual image contains NaN values." );
		if ( expectedSnr > actualSnr )
			fail( "The SNR is lower than expected, expected: " + expectedSnr + " dB actual: " + actualSnr + " dB" );
	}

	private static double snr( RandomAccessibleInterval< ? extends RealType< ? > > expected,
			RandomAccessibleInterval< ? extends RealType< ? > > actual )
	{
		double signal = meanSquaredSum( expected );
		double noise = meanSquaredSum( subtract( actual, expected ) );
		if ( signal == 0.0 )
			return Float.NEGATIVE_INFINITY;
		return 10 * ( Math.log10( signal / noise ) );
	}

	private static RandomAccessibleInterval< DoubleType > subtract(
			RandomAccessibleInterval< ? extends RealType > a,
			RandomAccessibleInterval< ? extends RealType > b )
	{
		return Views.interval( Converters.convert(
				Views.pair( a, b ),
				( pair, out ) -> out.setReal( pair.getA().getRealDouble() - pair.getB().getRealDouble() ),
				new DoubleType() ), a );
	}

	private static double meanSquaredSum( RandomAccessibleInterval< ? extends RealType< ? > > image )
	{
		double sum = 0;
		IterableInterval< ? extends RealType< ? > > iterable = Views.iterable( image );
		for ( RealType< ? > pixel : iterable )
			sum += square( pixel.getRealDouble() );
		return sum / iterable.size();
	}

	private static double square( double value )
	{
		return value * value;
	}
}
