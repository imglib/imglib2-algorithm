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
import net.imglib2.algorithm.convolution.ConvolverFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
This class utilizes Gauss1D class to carry on 3-dimensional image filtering.
Essentially, the filtering coefficients are calculated with the code from
the class Gauss1D. The filtering routines itself are, however, rewritten
specifically for the 3D image situation.

Author: Vladimir Ulman, ulman@mpi-cbg.de, 2018
*/
public class FastGauss
{

	public static void convolve1d( final double sigma, final RandomAccessible< ? extends RealType<?>> input, final RandomAccessibleInterval< ? extends RealType< ? > > output, int d )
	{
		FastGaussConvolverRealType convolverFactory = new FastGaussConvolverRealType( sigma );
		Convolution.convolve1d( convolverFactory, input, output, d );
	}

	public static void convolve( double sigma, final RandomAccessible< ? extends RealType< ? > > input, final RandomAccessibleInterval< ? extends RealType< ? > > output ) {
		FastGaussConvolverRealType convolver = new FastGaussConvolverRealType( sigma );
		List< Pair< Integer, ? extends ConvolverFactory< ? super RealType< ? >, ? super RealType< ? > > > > convolvers =
				IntStream.range( 0, output.numDimensions() )
						.mapToObj( d -> new ValuePair<>( d, convolver ) )
						.collect( Collectors.toList() );
		Convolution.convolve( convolvers, new ArrayImgFactory<>( new DoubleType() ), input, output );
	}

	public static void convolve( double[] sigmas, final RandomAccessible< ? extends RealType< ? > > input, final RandomAccessibleInterval< ? extends RealType< ? > > output ) {
		if(sigmas.length == 1)
		{
			convolve( sigmas[ 0 ], input, output );
			return;
		}
		List< Pair< Integer, ? extends ConvolverFactory< ? super RealType< ? >, ? super RealType< ? > > > > convolvers =
				IntStream.range( 0, output.numDimensions() )
						.mapToObj( d -> new ValuePair<>( d, new FastGaussConvolverRealType( sigmas[d] ) ) )
						.collect( Collectors.toList() );
		Convolution.convolve( convolvers, new ArrayImgFactory<>( new DoubleType() ), input, output );
	}
}
