package net.imglib2.algorithm.corner;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.algorithm.gradient.PartialDerivative;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

public class HessianMatrix
{

	public static < T extends RealType< T >, U extends RealType< U > > Img< U > calculateMatrix(
			final RandomAccessible< T > source,
			final Interval interval,
			final double sigma,
			final OutOfBoundsFactory< U, ? super RandomAccessibleInterval< U > > outOfBounds,
			final ImgFactory< U > factory,
			final U u ) throws IncompatibleTypeException
	{
		final double[] sigmas = new double[ source.numDimensions() ];
		Arrays.fill( sigmas, sigma );
		return calculateMatrix( source, interval, sigmas, outOfBounds, factory, u );
	}

	public static < T extends RealType< T >, U extends RealType< U > > Img< U > calculateMatrix(
			final RandomAccessible< T > source,
			final Interval interval,
			final double sigma,
			final OutOfBoundsFactory< U, ? super RandomAccessibleInterval< U > > outOfBounds,
					final ImgFactory< U > factory,
					final U u,
					final int nThreads ) throws IncompatibleTypeException
	{
		final double[] sigmas = new double[ source.numDimensions() ];
		Arrays.fill( sigmas, sigma );
		return calculateMatrix( source, interval, sigmas, outOfBounds, factory, u, nThreads );
	}

	public static < T extends RealType< T >, U extends RealType< U > > Img< U > calculateMatrix(
			final RandomAccessible< T > source,
			final Interval interval,
			final double sigma,
			final OutOfBoundsFactory< U, ? super RandomAccessibleInterval< U > > outOfBounds,
					final ImgFactory< U > factory,
					final U u,
					final ExecutorService es ) throws IncompatibleTypeException
	{
		final double[] sigmas = new double[ source.numDimensions() ];
		Arrays.fill( sigmas, sigma );
		return calculateMatrix( source, interval, sigmas, outOfBounds, factory, u, es );
	}

	public static < T extends RealType< T >, U extends RealType< U > > Img< U > calculateMatrix(
			final RandomAccessible< T > source,
			final Interval interval,
			final double[] sigma,
			final OutOfBoundsFactory< U, ? super RandomAccessibleInterval< U > > outOfBounds,
					final ImgFactory< U > factory,
					final U u ) throws IncompatibleTypeException
	{
		final int nThreads = Runtime.getRuntime().availableProcessors();
		return calculateMatrix( source, interval, sigma, outOfBounds, factory, u, nThreads );
	}

	public static < T extends RealType< T >, U extends RealType< U > > Img< U > calculateMatrix(
			final RandomAccessible< T > source,
			final Interval interval,
			final double[] sigma,
			final OutOfBoundsFactory< U, ? super RandomAccessibleInterval< U > > outOfBounds,
					final ImgFactory< U > factory,
					final U u,
					final int nThreads ) throws IncompatibleTypeException
	{
		final ExecutorService es = Executors.newFixedThreadPool( nThreads );
		final Img< U > hessianMatrix = calculateMatrix( source, interval, sigma, outOfBounds, factory, u, es );
		es.shutdown();
		return hessianMatrix;
	}

	public static < T extends RealType< T >, U extends RealType< U > > Img< U > calculateMatrix(
			final RandomAccessible< T > source,
			final Interval interval,
			final double[] sigma,
			final OutOfBoundsFactory< U, ? super RandomAccessibleInterval< U > > outOfBounds,
					final ImgFactory< U > factory,
					final U u,
					final ExecutorService es ) throws IncompatibleTypeException
	{
		final int nDim = interval.numDimensions();
		final int nTargetDim = nDim + 1;
		final long[] dimensions = new long[ nTargetDim ];
		final long[] min = new long[ nTargetDim ];
		final long[] max = new long[ nTargetDim ];

		for ( int d = 0; d < nDim; ++d )
		{
			dimensions[ d ] = interval.dimension( d );
			min[ d ] = interval.min( d );
			max[ d ] = interval.max( d );
		}
		dimensions[ nDim ] = nDim * ( nDim + 1 ) / 2;
		min[ nDim ] = 0;
		max[ nDim ] = dimensions[ nDim ] - 1;

		final long[] gradientDim = dimensions.clone();
		gradientDim[ nDim ] = nDim;

		final Img< U > gaussianConvolved = factory.create( interval, u );
		final Img< U > gradient = factory.create( gradientDim, u );
		final Img< U > hessianMatrix = factory.create( dimensions, u );

		impl( source, gaussianConvolved, gradient, hessianMatrix, nDim, sigma, outOfBounds, es );

		return hessianMatrix;
	}

	private static < T extends RealType< T >, U extends RealType< U > > void impl(
			final RandomAccessible< T > source,
			final RandomAccessibleInterval< U > gaussianConvolved,
			final RandomAccessibleInterval< U > gradient,
			final RandomAccessibleInterval< U > hessianMatrix,
			final int nDim,
			final double[] sigma,
			final OutOfBoundsFactory< U, ? super RandomAccessibleInterval< U > > outOfBounds,
					final ExecutorService es ) throws IncompatibleTypeException
	{

		Gauss3.gauss( sigma, source, gaussianConvolved, es );

		for ( long d = 0; d < nDim; ++d )
		{
			PartialDerivative.gradientCentralDifference2( Views.extend( gaussianConvolved, outOfBounds ), Views.hyperSlice( gradient, nDim, d ), ( int ) d );
		}

		int count = 0;
		for ( long d1 = 0; d1 < nDim; ++d1 )
		{
			final IntervalView< U > hs1 = Views.hyperSlice( gradient, nDim, d1 );
			for ( long d2 = d1; d2 < nDim; ++d2 )
			{
				final IntervalView< U > hs2 = Views.hyperSlice( hessianMatrix, nDim, count );
				PartialDerivative.gradientCentralDifference2( Views.extend( hs1, outOfBounds ), hs2, ( int ) d2 );
				++count;
			}
		}
	}



}
