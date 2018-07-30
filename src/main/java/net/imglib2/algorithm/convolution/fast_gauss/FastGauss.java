/*
 * To the extent possible under law, the ImageJ developers have waived
 * all copyright and related or neighboring rights to this code.
 *
 * See the CC0 1.0 Universal license for details:
 *     http://creativecommons.org/publicdomain/zero/1.0/
 */
package net.imglib2.algorithm.convolution.fast_gauss;

import net.imglib2.RandomAccessible;
import net.imglib2.algorithm.convolution.Convolution;
import net.imglib2.algorithm.convolution.LineConvolution;
import net.imglib2.algorithm.convolution.MultiDimensionConvolution;
import net.imglib2.type.numeric.RealType;
import net.imglib2.RandomAccessibleInterval;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Faster alternative to {@link net.imglib2.algorithm.gauss3.Gauss3}.
 * It's especially faster if sigma is larger than 3.
 * <p>
 * It is normally expensive to calculate the exact result of the gauss convolution.
 * That's why approximations are almost always used.
 * {@link net.imglib2.algorithm.gauss3.Gauss3} does an approximation
 * by cutting of the gauss kernel at 3*sigma + 1.
 * {@link FastGauss} uses a different approach known as Fast Gauss
 * Transformation. The runtime is thereby independent of sigma.
 *
 * @author Vladimir Ulman
 * @author Matthias Arzt
 */
public class FastGauss
{

	public static Convolution< RealType< ? > > convolution( double[] sigma )
	{
		List< Convolution< RealType< ? > > > steps = IntStream.range( 0, sigma.length )
				.mapToObj( i -> convolution1d( sigma[ i ], i ) )
				.collect( Collectors.toList() );
		return Convolution.concat( steps );
	}

	public static Convolution< RealType< ? > > convolution( double sigma )
	{
		return new MultiDimensionConvolution<>( k -> convolution( nCopies( k, sigma ) ) );
	}

	public static Convolution< RealType< ? > > convolution1d( double sigma, int direction )
	{
		return new LineConvolution<>( new FastGaussConvolverRealType( sigma ), direction );
	}

	public static void convolve( double[] sigmas, final RandomAccessible< ? extends RealType< ? > > input, final RandomAccessibleInterval< ? extends RealType< ? > > output )
	{
		convolution( sigmas ).process( input, output );
	}

	public static void convolve( double sigma, final RandomAccessible< ? extends RealType< ? > > input, final RandomAccessibleInterval< ? extends RealType< ? > > output )
	{
		convolution( sigma ).process( input, output );
	}

	private static double[] nCopies( int n, double sigma )
	{
		double[] sigmas = new double[ n ];
		Arrays.fill( sigmas, sigma );
		return sigmas;
	}

}
