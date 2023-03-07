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
package net.imglib2.algorithm.bspline;

import java.util.*;
import java.util.function.*;

import net.imglib2.*;
import net.imglib2.algorithm.interpolation.randomaccess.BSplineCoefficientsInterpolatorFactory;
import net.imglib2.algorithm.interpolation.randomaccess.BSplineInterpolatorFactory;
import net.imglib2.img.*;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.DoubleArray;
import net.imglib2.interpolation.InterpolatorFactory;
import net.imglib2.interpolation.randomaccess.*;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.position.*;
import net.imglib2.type.numeric.*;
import net.imglib2.type.numeric.real.*;
import net.imglib2.util.*;
import net.imglib2.view.*;

public class BSplineBenchmark
{

	public static void main( String[] args )
	{
		benchmark2d();
		System.out.println( "\n\n\n" );
		benchmark3d();
		System.out.println( "\n\n\n" );
		blockSizeBenchmark3d();
	}

	public static void blockSizeBenchmark3d()
	{
		System.out.println( "BLOCK SIZE BENCHMARK 3D\n" );
		FinalInterval interval = new FinalInterval( 256, 256, 128 );
		RandomAccessibleInterval< DoubleType > img = expChirpImage( new DoubleType(), interval, 0.01, 0.25, true );

		BSplineCoefficientsInterpolatorFactory< DoubleType, DoubleType > cFactory = new BSplineCoefficientsInterpolatorFactory<>( img, 3 );

		double start = 16;
		double end = 112;
		double step = 0.5;

		int numRuns = 4;

		ArrayList< Long > coefTimes = benchmarkInterpolation3d( img, cFactory, numRuns, start, end, step );
		double coefAverage = average( coefTimes );

		System.out.println( "Interpolate coefficients: " );
		System.out.println( "  first time: " + coefTimes.get( 0 ) );
		System.out.println( "  average time: " + coefAverage );
		System.out.println( " " );

		for ( int blockWidth = 16; blockWidth <= 64; blockWidth *= 2 )
		{
			BSplineLazyCoefficientsInterpolatorFactory< DoubleType, DoubleType > lazyFactory = new BSplineLazyCoefficientsInterpolatorFactory<>( img, 3, new int[] { blockWidth, blockWidth, blockWidth } );
			ArrayList< Long > lazyTimes = benchmarkInterpolation3d( img, lazyFactory, numRuns, start, end, step );
			double lazyAverage = average( lazyTimes );
			System.out.println( "Interpolate lazy " + blockWidth + " : " );
			System.out.println( "  first time: " + lazyTimes.get( 0 ) );
			System.out.println( "  average time: " + lazyAverage );
			System.out.println( " " );
		}
	}

	public static void benchmark3d()
	{
		System.out.println( "BENCHMARK 3D\n" );
		FinalInterval interval = new FinalInterval( 256, 256, 128 );
		RandomAccessibleInterval< DoubleType > img = expChirpImage( new DoubleType(), interval, 0.01, 0.25, true );

		int N = 8;
		long[] times = benchmarkPreComputation( img, N );
		double averageTime = average( times );

		System.out.println( "Precomputation: " );
		System.out.println( "  first time: " + times[ 0 ] );
		System.out.println( "  average time: " + averageTime );
		System.out.println( " " );

		NLinearInterpolatorFactory< DoubleType > linInterp = new NLinearInterpolatorFactory<>();
		RealRandomAccessible< DoubleType > realImgLinear = Views.interpolate( img, linInterp );

		BSplineCoefficientsInterpolatorFactory< DoubleType, DoubleType > cFactory = new BSplineCoefficientsInterpolatorFactory<>( img, 3 );
		RealRandomAccessible< DoubleType > realImgPre = Views.interpolate( img, cFactory );

		BSplineInterpolatorFactory< DoubleType > factory = new BSplineInterpolatorFactory<>( 3 );
		RealRandomAccessible< DoubleType > realImgCard = Views.interpolate( Views.extendZero( img ), factory );

		BSplineLazyCoefficientsInterpolatorFactory< DoubleType, DoubleType > lazyFactory = new BSplineLazyCoefficientsInterpolatorFactory<>( img, 3, new int[] { 32, 32, 32 } );
		RealRandomAccessible< DoubleType > realImgLazy = Views.interpolate( Views.extendZero( img ), lazyFactory );

		double start = 16;
		double end = 112;
		double step = 0.5;

		ArrayList< Long > linearTimes = benchmarkInterpolation3d( realImgLinear, start, end, step );
		double linearAvg = average( linearTimes );

		System.out.println( "Interpolate linear: " );
		System.out.println( "  first time: " + linearTimes.get( 0 ) );
		System.out.println( "  average time: " + linearAvg );
		System.out.println( " " );

		ArrayList< Long > cardTimes = benchmarkInterpolation3d( realImgCard, start, end, step );
		double cardAvg = average( cardTimes );

		System.out.println( "Interpolate card: " );
		System.out.println( "  first time: " + cardTimes.get( 0 ) );
		System.out.println( "  average time: " + cardAvg );
		System.out.println( " " );

		ArrayList< Long > preTimes = benchmarkInterpolation3d( realImgPre, start, end, step );
		double preAvg = average( preTimes );

		System.out.println( "Interpolate pre: " );
		System.out.println( "  first time: " + preTimes.get( 0 ) );
		System.out.println( "  average time: " + preAvg );
		System.out.println( " " );

		ArrayList< Long > lazyTimes = benchmarkInterpolation2d( realImgLazy, start, end, step );
		double lazyAvg = average( cardTimes );

		System.out.println( "Interpolate lazy: " );
		System.out.println( "  first time: " + lazyTimes.get( 0 ) );
		System.out.println( "  average time: " + lazyAvg );
		System.out.println( " " );

		// double percentSpeedup = (1 - (preAvg / cardAvg)) * 100;
		double relativeSpeedupPercent = ( cardAvg / preAvg );
		System.out.println( "Relative speedup of precomputation: " + relativeSpeedupPercent );

		double relativeSlowdownToLinear = ( preAvg / linearAvg );
		System.out.println( "Relative slowdown of precomputation: " + relativeSlowdownToLinear );

	}

	public static void benchmark2d()
	{
		System.out.println( "BENCHMARK 2D\n" );
		FinalInterval interval = new FinalInterval( 512, 512 );
		RandomAccessibleInterval< DoubleType > img = expChirpImage( new DoubleType(), interval, 0.01, 0.25, true );

		int N = 100;
		long[] times = benchmarkPreComputation( img, N );
		double averageTime = average( times );

		System.out.println( "Precomputation: " );
		System.out.println( "  first time: " + times[ 0 ] );
		System.out.println( "  average time: " + averageTime );
		System.out.println( " " );

		NLinearInterpolatorFactory< DoubleType > linInterp = new NLinearInterpolatorFactory<>();
		RealRandomAccessible< DoubleType > realImgLinear = Views.interpolate( img, linInterp );

		BSplineCoefficientsInterpolatorFactory< DoubleType, DoubleType > cFactory = new BSplineCoefficientsInterpolatorFactory<>( img, 3 );
		RealRandomAccessible< DoubleType > realImgPre = Views.interpolate( img, cFactory );

		BSplineInterpolatorFactory< DoubleType > factory = new BSplineInterpolatorFactory<>( 3 );
		RealRandomAccessible< DoubleType > realImgCard = Views.interpolate( Views.extendZero( img ), factory );

		BSplineLazyCoefficientsInterpolatorFactory< DoubleType, DoubleType > lazyFactory = new BSplineLazyCoefficientsInterpolatorFactory<>( img, 3, new int[] { 32, 32 } );
		RealRandomAccessible< DoubleType > realImgLazy = Views.interpolate( Views.extendZero( img ), lazyFactory );

		double start = 32;
		double end = 256;
		double step = 0.25;

		ArrayList< Long > linearTimes = benchmarkInterpolation2d( realImgLinear, start, end, step );
		double linearAvg = average( linearTimes );

		System.out.println( "Interpolate linear: " );
		System.out.println( "  first time: " + linearTimes.get( 0 ) );
		System.out.println( "  average time: " + linearAvg );
		System.out.println( " " );

		ArrayList< Long > cardTimes = benchmarkInterpolation2d( realImgCard, start, end, step );
		double cardAvg = average( cardTimes );

		System.out.println( "Interpolate on-the-fly: " );
		System.out.println( "  first time: " + cardTimes.get( 0 ) );
		System.out.println( "  average time: " + cardAvg );
		System.out.println( " " );

		ArrayList< Long > preTimes = benchmarkInterpolation2d( realImgPre, start, end, step );
		double preAvg = average( preTimes );

		System.out.println( "Interpolate pre: " );
		System.out.println( "  first time: " + preTimes.get( 0 ) );
		System.out.println( "  average time: " + preAvg );
		System.out.println( " " );

		ArrayList< Long > lazyTimes = benchmarkInterpolation2d( realImgLazy, start, end, step );
		double lazyAvg = average( cardTimes );

		System.out.println( "Interpolate lazy: " );
		System.out.println( "  first time: " + lazyTimes.get( 0 ) );
		System.out.println( "  average time: " + lazyAvg );
		System.out.println( " " );

		// double percentSpeedup = (1 - (preAvg / cardAvg)) * 100;
//		double relativeSpeedupPercent = ( cardAvg / preAvg );
//		System.out.println( "Relative speedup of precomputation: " + relativeSpeedupPercent );
//
//		double relativeSlowdownToLinear = ( preAvg / linearAvg );
//		System.out.println( "Relative slowdown of precomputation: " + relativeSlowdownToLinear );

	}

	public static double average( final ArrayList< Long > times )
	{
		int N = times.size();
		double averageTime = 0;
		for ( int i = 0; i < N; i++ )
			averageTime += times.get( i );

		averageTime /= N;
		return averageTime;
	}

	public static double average( final long[] times )
	{
		int N = times.length;
		double averageTime = 0;
		for ( int i = 0; i < N; i++ )
			averageTime += times[ i ];

		averageTime /= N;
		return averageTime;
	}

	public static < T extends RealType< T > > ArrayList< Long > benchmarkInterpolation3d( final RandomAccessible< T > img, final InterpolatorFactory< DoubleType, RandomAccessible< T > > factory, int numRuns, double start, double end, double step )
	{

		ArrayList< Long > times = new ArrayList<>();
		for ( int i = 0; i < numRuns; i++ )
		{
			long startTime = System.currentTimeMillis();
			RealRandomAccess< DoubleType > ra = factory.create( img );

			for ( double x = start; x < end; x += step )
			{
				for ( double y = start; y < end; y += step )
				{
					for ( double z = start; z < end; z += step )
					{

						ra.setPosition( x, 0 );
						ra.setPosition( y, 1 );
						ra.setPosition( z, 2 );
						ra.get();
					}
				}
			}
			long endTime = System.currentTimeMillis();
			times.add( endTime - startTime );
		}
		return times;
	}

	public static < T extends RealType< T > > ArrayList< Long > benchmarkInterpolation3d( final RealRandomAccessible< T > img, double start, double end, double step )
	{
		RealRandomAccess< T > ra = img.realRandomAccess();

		ArrayList< Long > times = new ArrayList<>();
		for ( double x = start; x < end; x += step )
		{
			for ( double y = start; y < end; y += step )
			{
				for ( double z = start; z < end; z += step )
				{
					long startTime = System.currentTimeMillis();

					ra.setPosition( x, 0 );
					ra.setPosition( y, 1 );
					ra.setPosition( z, 2 );
					ra.get();

					long endTime = System.currentTimeMillis();
					times.add( endTime - startTime );
				}
			}
		}

		return times;
	}

	public static < T extends RealType< T > > ArrayList< Long > benchmarkInterpolation2d( final RealRandomAccessible< T > img, double start, double end, double step )
	{
		RealRandomAccess< T > ra = img.realRandomAccess();

		ArrayList< Long > times = new ArrayList<>();
		for ( double x = start; x < end; x += step )
		{
			for ( double y = start; y < end; y += step )
			{
				long startTime = System.currentTimeMillis();

				ra.setPosition( x, 0 );
				ra.setPosition( y, 1 );
				ra.get();

				long endTime = System.currentTimeMillis();
				times.add( endTime - startTime );
			}
		}

		return times;
	}

	public static < T extends RealType< T > > long[] benchmarkPreComputation( final RandomAccessibleInterval< T > img, final int N )
	{
		long[] times = new long[ N ];
		for ( int i = 0; i < N; i++ )
		{
			long startTime = System.currentTimeMillis();

			BSplineDecomposition< T, DoubleType > decomp = new BSplineDecomposition<>( 3, Views.extendZero( img ) );
			ArrayImg< DoubleType, DoubleArray > coefs = ArrayImgs.doubles( Intervals.dimensionsAsLongArray( img ) );
			decomp.accept( coefs );

			long endTime = System.currentTimeMillis();
			times[ i ] = endTime - startTime;
		}

		return times;
	}

	public static < T extends RealType< T > > RandomAccessibleInterval< T > expChirpImage( final T type, final Interval interval, double f0, double f1, final boolean copy )
	{
		// exp chirp
		final double w = interval.realMax( 0 );
		final double k = Math.pow( ( f1 / f0 ), 1 / w );

		BiConsumer< Localizable, T > fun = new BiConsumer< Localizable, T >()
		{
			@Override
			public void accept( Localizable p, T t )
			{
				t.setReal( 1 + Math.cos( p.getDoublePosition( 0 ) * f0 * Math.pow( k, p.getDoublePosition( 0 ) ) ) * Math.cos( p.getDoublePosition( 1 ) * f0 * Math.pow( k, p.getDoublePosition( 1 ) ) ) );
			}
		};
		FunctionRandomAccessible< T > freqSweep = new FunctionRandomAccessible<>( interval.numDimensions(), fun, type::createVariable );
		IntervalView< T > virtualimg = Views.interval( freqSweep, interval );
		if ( copy )
		{
			Img< T > memimg = Util.getSuitableImgFactory( interval, type ).create( interval );
			LoopBuilder.setImages( virtualimg, memimg ).forEachPixel( ( x, y ) -> y.set( x ) );
			return memimg;
		}
		else
			return virtualimg;
	}

}
