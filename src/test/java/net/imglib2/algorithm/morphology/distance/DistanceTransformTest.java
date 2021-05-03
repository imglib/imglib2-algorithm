/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2021 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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

package net.imglib2.algorithm.morphology.distance;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import org.junit.Assert;
import org.junit.Test;

import net.imglib2.Cursor;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.morphology.distance.DistanceTransform.DISTANCE_TYPE;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.DoubleArray;
import net.imglib2.img.basictypeaccess.array.LongArray;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Pair;
import net.imglib2.view.Views;

/**
 *
 * @author Philipp Hanslovsky
 *
 */
public class DistanceTransformTest
{

	private final int minNumDimensions = 1;

	private final int maxNumDimensions = 4;

	private final int dimensionSize = 6;

	private final Random rng = new Random( 100 );

	private final int nThreads = Runtime.getRuntime().availableProcessors();

	private final ExecutorService es = Executors.newFixedThreadPool( nThreads );

	@Test
	public void test() throws InterruptedException, ExecutionException
	{
		for ( int numDimensions = minNumDimensions; numDimensions <= maxNumDimensions; ++numDimensions )
		{

			final long[] dim = new long[ numDimensions ];
			for ( int d = 0, dimSize = dimensionSize; d < dim.length; ++d, --dimSize )
			{
				dim[ d ] = dimSize;
			}

			final ArrayImg< DoubleType, DoubleArray > source = ArrayImgs.doubles( dim );
			for ( final DoubleType s : source )
			{
				s.set( rng.nextDouble() );
			}

			testEuclidian( source, rng );
			testEuclidian( source, rng, 3 * nThreads );

			testL1( source, rng );
			testL1( source, rng, 3 * nThreads );

		}
	}

	@Test
	public void testBinaryEuclidian()
	{
		testBinary( DISTANCE_TYPE.EUCLIDIAN, DistanceTransformTest::calcSquaredEuclidianDist );
	}

	@Test
	public void testBinaryL1()
	{
		testBinary( DISTANCE_TYPE.L1, DistanceTransformTest::calcL1Dist );
	}

	private void testBinary( final DISTANCE_TYPE dt, final DistanceCalculator distanceCalculator )
	{
		for ( int numDimensions = minNumDimensions; numDimensions <= maxNumDimensions; ++numDimensions )
		{
			final long[] dim = LongStream.generate( () -> dimensionSize ).limit( numDimensions ).toArray();
			final double[] weights = IntStream.range( 1, numDimensions + 1 ).asDoubleStream().toArray();

			final ArrayImg< BitType, LongArray > img0 = ArrayImgs.bits( dim );
			final ArrayImg< BitType, LongArray > img1 = ArrayImgs.bits( dim );
			final ArrayImg< BitType, LongArray > img2 = ArrayImgs.bits( dim );

			final ArrayImg< DoubleType, DoubleArray > dist0 = ArrayImgs.doubles( dim );
			final ArrayImg< DoubleType, DoubleArray > dist1 = ArrayImgs.doubles( dim );
			final ArrayImg< DoubleType, DoubleArray > dist2 = ArrayImgs.doubles( dim );

			final Point p0 = new Point( Intervals.minAsLongArray( img0 ) );
			final Point p1 = new Point( Intervals.maxAsLongArray( img1 ) );
			final Point p2 = new Point( average( Intervals.minAsLongArray( img2 ), Intervals.maxAsLongArray( img2 ), new long[ numDimensions ] ) );

			getAt( img0, p0 ).set( true );
			getAt( img1, p1 ).set( true );
			getAt( img2, p2 ).set( true );

			DistanceTransform.binaryTransform( img0, dist0, dt, weights );
			DistanceTransform.binaryTransform( img1, dist1, dt, weights );
			DistanceTransform.binaryTransform( img2, dist2, dt, weights );

			checkDistance( dist0, p0, weights, distanceCalculator );
			checkDistance( dist1, p1, weights, distanceCalculator );
			checkDistance( dist2, p2, weights, distanceCalculator );

		}
	}

	private static void compareRAIofRealType( final RandomAccessibleInterval< ? extends RealType< ? > > ref, final RandomAccessibleInterval< ? extends RealType< ? > > comp, final double tolerance )
	{
		Assert.assertArrayEquals( Intervals.dimensionsAsLongArray( ref ), Intervals.dimensionsAsLongArray( comp ) );
		Assert.assertArrayEquals( Intervals.minAsLongArray( ref ), Intervals.minAsLongArray( comp ) );
		Assert.assertArrayEquals( Intervals.maxAsLongArray( ref ), Intervals.maxAsLongArray( comp ) );
		for ( final Pair< ? extends RealType< ? >, ? extends RealType< ? > > p : Views.flatIterable( Views.interval( Views.pair( ref, comp ), ref ) ) )
		{
			Assert.assertEquals( p.getA().getRealDouble(), p.getB().getRealDouble(), tolerance );
		}
	}

	private < S extends RealType< S >, I extends RealType< I >, TMP extends RealType< TMP >, T extends RealType< T > > void runDistanceTransform(
			final RandomAccessible< S > source,
			final RandomAccessibleInterval< I > inPlace,
			final RandomAccessibleInterval< TMP > tmp,
			final RandomAccessibleInterval< T > target1,
			final RandomAccessibleInterval< T > target2,
			final DISTANCE_TYPE DT,
			final int nTasks,
			final double... w ) throws InterruptedException, ExecutionException
	{

		if ( nTasks > 1 )
		{
			DistanceTransform.transform( inPlace, DT, es, nTasks, w );
			DistanceTransform.transform( source, target1, DT, es, nTasks, w );
			DistanceTransform.transform( source, tmp, target2, DT, es, nTasks, w );
		}
		else
		{
			DistanceTransform.transform( inPlace, DT, w );
			DistanceTransform.transform( source, target1, DT, w );
			DistanceTransform.transform( source, tmp, target2, DT, w );
		}
	}

	private static interface DistanceFunctor
	{
		double dist( Localizable l1, Localizable l2 );
	}

	private static < T extends RealType< T >, U extends RealType< U > > void distanceTransform(
			final Img< T > source,
			final Img< U > target,
			final DistanceFunctor d )
	{
		for ( final Cursor< U > targetCursor = target.cursor(); targetCursor.hasNext(); )
		{
			final U t = targetCursor.next();
			double tmp = Double.MAX_VALUE;
			for ( final Cursor< T > sourceCursor = source.cursor(); sourceCursor.hasNext(); )
			{
				final double s = sourceCursor.next().getRealDouble();
				tmp = Math.min( d.dist( targetCursor, sourceCursor ) + s, tmp );
			}
			t.setReal( tmp );
		}
	}

	private < T extends RealType< T > > void testEuclidian( final Img< T > source, final Random rng ) throws InterruptedException, ExecutionException
	{
		testEuclidian( source, rng, 1 );
	}

	private < T extends RealType< T > > void testEuclidian( final Img< T > source, final Random rng, final int nTasks ) throws InterruptedException, ExecutionException
	{
		final ArrayImg< DoubleType, DoubleArray > target1 = ArrayImgs.doubles( Intervals.dimensionsAsLongArray( source ) );
		final ArrayImg< DoubleType, DoubleArray > target2 = ArrayImgs.doubles( Intervals.dimensionsAsLongArray( source ) );
		final ArrayImg< DoubleType, DoubleArray > ref = ArrayImgs.doubles( Intervals.dimensionsAsLongArray( source ) );
		final ArrayImg< DoubleType, DoubleArray > tmp = ArrayImgs.doubles( Intervals.dimensionsAsLongArray( source ) );

		final int nDim = source.numDimensions();
		final DISTANCE_TYPE DT = DISTANCE_TYPE.EUCLIDIAN;

		{
			final Img< T > inPlace = source.factory().create( source );
			for ( final Pair< T, T > p : Views.interval( Views.pair( source, inPlace ), source ) )
			{
				p.getB().set( p.getA() );
			}
			final double w = rng.nextDouble() * 1e-4;
			final DistanceFunctor functorIsotropic = ( l1, l2 ) -> {
				double result = 0.0;
				for ( int d = 0; d < nDim; ++d )
				{
					final double diff = l1.getDoublePosition( d ) - l2.getDoublePosition( d );
					result += diff * diff;
				}
				return w * result;
			};

			distanceTransform( source, ref, functorIsotropic );
			runDistanceTransform( source, inPlace, tmp, target1, target2, DT, nTasks, w );

			final double tolerance = 1e-15;
			compareRAIofRealType( ref, inPlace, tolerance );
			compareRAIofRealType( ref, target1, tolerance );
			compareRAIofRealType( ref, target2, tolerance );

		}

		{
			final Img< T > inPlace = source.factory().create( source );
			for ( final Pair< T, T > p : Views.interval( Views.pair( source, inPlace ), source ) )
			{
				p.getB().set( p.getA() );
			}
			final double[] w = new double[ nDim ];
			for ( int d = 0; d < w.length; ++d )
			{
				w[ d ] = rng.nextDouble() * 1e-4;
			}
			final DistanceFunctor functorAnisotropic = ( l1, l2 ) -> {
				double result = 0.0;
				for ( int d = 0; d < nDim; ++d )
				{
					final double diff = l1.getDoublePosition( d ) - l2.getDoublePosition( d );
					result += w[ d ] * diff * diff;
				}
				return result;
			};

			distanceTransform( source, ref, functorAnisotropic );
			runDistanceTransform( source, inPlace, tmp, target1, target2, DT, nTasks, w );

			final double tolerance = 1e-15;
			compareRAIofRealType( ref, inPlace, tolerance );
			compareRAIofRealType( ref, target1, tolerance );
			compareRAIofRealType( ref, target2, tolerance );
		}

	}

	private < T extends RealType< T > > void testL1( final Img< T > source, final Random rng ) throws InterruptedException, ExecutionException
	{
		testL1( source, rng, 1 );
	}

	private < T extends RealType< T > > void testL1(
			final Img< T > source,
			final Random rng,
			final int nTasks ) throws InterruptedException, ExecutionException
	{
		final ArrayImg< DoubleType, DoubleArray > target1 = ArrayImgs.doubles( Intervals.dimensionsAsLongArray( source ) );
		final ArrayImg< DoubleType, DoubleArray > target2 = ArrayImgs.doubles( Intervals.dimensionsAsLongArray( source ) );
		final ArrayImg< DoubleType, DoubleArray > ref = ArrayImgs.doubles( Intervals.dimensionsAsLongArray( source ) );
		final ArrayImg< DoubleType, DoubleArray > tmp = ArrayImgs.doubles( Intervals.dimensionsAsLongArray( source ) );

		final int nDim = source.numDimensions();
		final DISTANCE_TYPE DT = DISTANCE_TYPE.L1;

		{
			final Img< T > inPlace = source.factory().create( source );
			for ( final Pair< T, T > p : Views.interval( Views.pair( source, inPlace ), source ) )
			{
				p.getB().set( p.getA() );
			}

			final double w = rng.nextDouble() * 1e-4;
			final DistanceFunctor functorIsotropic = ( l1, l2 ) -> {
				double result = 0.0;
				for ( int d = 0; d < nDim; ++d )
				{
					final double diff = l1.getDoublePosition( d ) - l2.getDoublePosition( d );
					result += Math.abs( diff );
				}
				return w * result;
			};

			distanceTransform( source, ref, functorIsotropic );
			runDistanceTransform( source, inPlace, tmp, target1, target2, DT, nTasks, w );

			final double tolerance = 1e-15;
			compareRAIofRealType( ref, inPlace, tolerance );
			compareRAIofRealType( ref, target1, tolerance );
			compareRAIofRealType( ref, target2, tolerance );

		}

		{
			final Img< T > inPlace = source.factory().create( source );
			for ( final Pair< T, T > p : Views.interval( Views.pair( source, inPlace ), source ) )
			{
				p.getB().set( p.getA() );
			}

			final double[] w = new double[ nDim ];
			for ( int d = 0; d < w.length; ++d )
			{
				w[ d ] = rng.nextDouble() * 1e-4;
			}
			final DistanceFunctor functorAnisotropic = ( l1, l2 ) -> {
				double result = 0.0;
				for ( int d = 0; d < nDim; ++d )
				{
					final double diff = l1.getDoublePosition( d ) - l2.getDoublePosition( d );
					result += w[ d ] * Math.abs( diff );
				}
				return result;
			};

			distanceTransform( source, ref, functorAnisotropic );
			runDistanceTransform( source, inPlace, tmp, target1, target2, DT, nTasks, w );

			final double tolerance = 1e-15;
			compareRAIofRealType( ref, inPlace, tolerance );
			compareRAIofRealType( ref, target1, tolerance );
			compareRAIofRealType( ref, target2, tolerance );
		}

	}

	private static double calcSquaredEuclidianDist( final Localizable l1, final Localizable l2, final double[] weights )
	{
		assert l1.numDimensions() == l2.numDimensions();
		assert l1.numDimensions() == weights.length;

		double sum = 0.0;
		for ( int d = 0; d < weights.length; ++d )
		{
			final long diff = l1.getLongPosition( d ) - l2.getLongPosition( d );
			sum += weights[ d ] * diff * diff;
		}

		return sum;
	}

	private static double calcL1Dist( final Localizable l1, final Localizable l2, final double[] weights )
	{
		assert l1.numDimensions() == l2.numDimensions();
		assert l1.numDimensions() == weights.length;

		double sum = 0.0;
		for ( int d = 0; d < weights.length; ++d )
		{
			final long diff = l1.getLongPosition( d ) - l2.getLongPosition( d );
			sum += weights[ d ] * Math.abs( diff );
		}
		return sum;
	}

	private static long[] average( final long[] a1, final long[] a2, final long[] average )
	{
		Arrays.setAll( average, d -> ( a1[ d ] + a2[ d ] ) / 2 );
		return average;
	}

	private static < T > T getAt( final RandomAccessible< T > data, final Localizable at )
	{

		assert data.numDimensions() == at.numDimensions();

		final RandomAccess< T > access = data.randomAccess();
		access.setPosition( at );
		return access.get();
	}

	private static interface DistanceCalculator
	{

		public double dist( Localizable l1, Localizable l2, double[] weights );

	}

	private static boolean atSamePosition( final Localizable l1, final Localizable l2 )
	{
		assert l1.numDimensions() == l2.numDimensions();

		final int nDim = l1.numDimensions();

		for ( int d = 0; d < nDim; ++d )
		{
			if ( l1.getLongPosition( d ) != l2.getLongPosition( d ) ) { return false; }
		}
		return true;
	}

	private static < T extends RealType< T > > void checkDistance(
			final RandomAccessibleInterval< T > dist,
			final Localizable foreground,
			final double[] weights,
			final DistanceCalculator distanceCalculator )
	{
		for ( final Cursor< T > c = Views.iterable( dist ).localizingCursor(); c.hasNext(); )
		{
			final double actual = c.next().getRealDouble();
			final double expected = atSamePosition( foreground, c ) ? 0.0 : distanceCalculator.dist( foreground, c, weights );
			Assert.assertEquals( expected, actual, 0.0 );
		}
	}

}
