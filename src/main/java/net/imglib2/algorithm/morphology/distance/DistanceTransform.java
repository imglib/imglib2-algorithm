/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2016 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.DoubleStream;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.cell.CellImg;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Pair;
import net.imglib2.view.Views;
import net.imglib2.view.composite.RealComposite;

/**
 *
 * ImgLib2 implementation of n-dimensional distance transform D of sampled
 * functions f with distance measure d:
 * http://www.theoryofcomputing.org/articles/v008a019/ DOI:ch
 * 10.4086/toc.2012.v008a019
 *
 * D( p ) = min_q f(q) + d(p,q) where p,q are points on a grid/image.
 *
 * The implemented algorithm has complexity O(dn) where d is the number of
 * dimensions of the image, and n is the total number of pixels/voxels.
 *
 *
 * @author Philipp Hanslovsky
 *
 */
public class DistanceTransform
{

	/**
	 *
	 * Switch for calling convenience method with pre-defined distances.
	 *
	 */
	public static enum DISTANCE_TYPE
	{
		EUCLIDIAN,
		EUCLIDIAN_ANISOTROPIC,
		L1,
		L1_ANISOTROPIC
	}

	/**
	 * Create distance transform of source using Euclidian (L2) or L1 distance.
	 * Intermediate and final results will be stored in source (@{link
	 * DoubleType} recommended). The distance can be weighted (individually for
	 * each dimension, if desired) against the image values via the weights
	 * parameter.
	 *
	 * @param source
	 *            Input function on which distance transform should be computed.
	 * @param distanceType
	 *            Defines distance to be used: (an-)isotropic Euclidian or L1
	 * @param nThreads
	 *            Number of threads/parallelism
	 * @param weights
	 *            Individual weights for each dimension, balancing image values
	 *            and Euclidian distance.
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public static < T extends RealType< T > > void transform(
			final RandomAccessibleInterval< T > source,
			final DISTANCE_TYPE distanceType,
			final int nThreads,
			final double... weights ) throws InterruptedException, ExecutionException
	{
		final ExecutorService es = Executors.newFixedThreadPool( nThreads );
		transform( source, distanceType, es, nThreads, weights );
		es.shutdown();
	}

	/**
	 * Create distance transform of source using Euclidian (L2) or L1 distance.
	 * Intermediate and final results will be stored in source (@{link
	 * DoubleType} recommended). The distance can be weighted (individually for
	 * each dimension, if desired) against the image values via the weights
	 * parameter.
	 *
	 * @param source
	 *            Input function on which distance transform should be computed.
	 * @param distanceType
	 *            Defines distance to be used: (an-)isotropic Euclidian or L1
	 * @param es
	 *            {@link ExecutorService} for parallel execution.
	 * @param nTasks
	 *            Number of tasks/parallelism
	 * @param weights
	 *            Individual weights for each dimension, balancing image values
	 *            and Euclidian distance.
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public static < T extends RealType< T > > void transform(
			final RandomAccessibleInterval< T > source,
			final DISTANCE_TYPE distanceType,
			final ExecutorService es,
			final int nTasks,
			final double... weights ) throws InterruptedException, ExecutionException
	{
		transform( source, source, distanceType, es, nTasks, weights );
	}

	/**
	 * Create distance transform of source using Euclidian (L2) or L1 distance.
	 * Intermediate results will be stored in target (@{link DoubleType}
	 * recommended). The distance can be weighted (individually for each
	 * dimension, if desired) against the image values via the weights
	 * parameter.
	 *
	 * @param source
	 *            Input function on which distance transform should be computed.
	 * @param target
	 *            Intermediate and final results of distance transform.
	 * @param distanceType
	 *            Defines distance to be used: (an-)isotropic Euclidian or L1
	 * @param nThreads
	 *            Number of threads/parallelism
	 * @param weights
	 *            Individual weights for each dimension, balancing image values
	 *            and Euclidian distance.
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public static < T extends RealType< T >, U extends RealType< U > > void transform(
			final RandomAccessible< T > source,
			final RandomAccessibleInterval< U > target,
			final DISTANCE_TYPE distanceType,
			final int nThreads,
			final double... weights ) throws InterruptedException, ExecutionException
	{
		final ExecutorService es = Executors.newFixedThreadPool( nThreads );
		transform( source, target, distanceType, es, nThreads, weights );
		es.shutdown();
	}

	/**
	 * Create distance transform of source using Euclidian (L2) or L1 distance.
	 * Intermediate results will be stored in target (@{link DoubleType}
	 * recommended). The distance can be weighted (individually for each
	 * dimension, if desired) against the image values via the weights
	 * parameter.
	 *
	 * @param source
	 *            Input function on which distance transform should be computed.
	 * @param target
	 *            Intermediate and final results of distance transform.
	 * @param distanceType
	 *            Defines distance to be used: (an-)isotropic Euclidian or L1
	 * @param es
	 *            {@link ExecutorService} for parallel execution.
	 * @param nTasks
	 *            Number of tasks/parallelism
	 * @param weights
	 *            Individual weights for each dimension, balancing image values
	 *            and Euclidian distance.
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public static < T extends RealType< T >, U extends RealType< U > > void transform(
			final RandomAccessible< T > source,
			final RandomAccessibleInterval< U > target,
			final DISTANCE_TYPE distanceType,
			final ExecutorService es,
			final int nTasks,
			final double... weights ) throws InterruptedException, ExecutionException
	{
		transform( source, target, target, distanceType, es, nTasks, weights );
	}

	/**
	 * Create distance transform of source using Euclidian (L2) or L1 distance.
	 * Intermediate results will be stored in tmp (@{link DoubleType}
	 * recommended). The output will be written into target. The distance can be
	 * weighted (individually for each dimension, if desired) against the image
	 * values via the weights parameter.
	 *
	 * @param source
	 *            Input function on which distance transform should be computed.
	 * @param tmp
	 *            Storage for intermediate results.
	 * @param target
	 *            Final result of distance transform.
	 * @param distanceType
	 *            Defines distance to be used: (an-)isotropic Euclidian or L1
	 * @param nThreads
	 *            Number of threads/parallelism
	 * @param weights
	 *            Individual weights for each dimension, balancing image values
	 *            and Euclidian distance.
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public static < T extends RealType< T >, U extends RealType< U >, V extends RealType< V > > void transform(
			final RandomAccessible< T > source,
			final RandomAccessibleInterval< U > tmp,
			final RandomAccessibleInterval< V > target,
			final DISTANCE_TYPE distanceType,
			final int nThreads,
			final double... weights ) throws InterruptedException, ExecutionException
	{
		final ExecutorService es = Executors.newFixedThreadPool( nThreads );
		transform( source, tmp, target, distanceType, es, nThreads, weights );
		es.shutdown();
	}

	/**
	 * Create distance transform of source using Euclidian (L2) or L1 distance.
	 * Intermediate results will be stored in tmp (@{link DoubleType}
	 * recommended). The output will be written into target. The distance can be
	 * weighted (individually for each dimension, if desired) against the image
	 * values via the weights parameter.
	 *
	 * @param source
	 *            Input function on which distance transform should be computed.
	 * @param tmp
	 *            Storage for intermediate results.
	 * @param target
	 *            Final result of distance transform.
	 * @param distanceType
	 *            Defines distance to be used: (an-)isotropic Euclidian or L1
	 * @param es
	 *            {@link ExecutorService} for parallel execution.
	 * @param nTasks
	 *            Number of tasks/parallelism
	 * @param weights
	 *            Individual weights for each dimension, balancing image values
	 *            and Euclidian distance.
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public static < T extends RealType< T >, U extends RealType< U >, V extends RealType< V > > void transform(
			final RandomAccessible< T > source,
			final RandomAccessibleInterval< U > tmp,
			final RandomAccessibleInterval< V > target,
			final DISTANCE_TYPE distanceType,
			final ExecutorService es,
			final int nTasks,
			final double... weights ) throws InterruptedException, ExecutionException
	{

		final double[] w = weights.length == source.numDimensions() ? weights : DoubleStream.generate( () -> weights.length == 0 ? 1.0 : weights[ 0 ] ).limit( source.numDimensions() ).toArray();

		switch ( distanceType )
		{
		case EUCLIDIAN:
			transform( source, tmp, target, new EuclidianDistanceIsotropic( w[ 0 ] ), es, nTasks );
			break;
		case EUCLIDIAN_ANISOTROPIC:
			transform( source, tmp, target, new EuclidianDistanceAnisotropic( w ), es, nTasks );
			break;
		case L1:
			transformL1( source, tmp, target, es, nTasks, w );
			break;
		case L1_ANISOTROPIC:
			transformL1( source, tmp, target, es, nTasks, w );
		default:
			break;
		}
	}

	/**
	 * Create distance transform of source using arbitrary {@link Distance} d.
	 * Intermediate and final results will be stored in source (@{link
	 * DoubleType} recommended).
	 *
	 * @param source
	 *            Input function on which distance transform should be computed.
	 * @param d
	 *            {@link Distance} between two points.
	 * @param nThreads
	 *            Number of threads/parallelism
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public static < T extends RealType< T > > void transform(
			final RandomAccessibleInterval< T > source,
			final Distance d,
			final int nThreads ) throws InterruptedException, ExecutionException
	{
		final ExecutorService es = Executors.newFixedThreadPool( nThreads );
		transform( source, d, es, nThreads );
		es.shutdown();
	}

	/**
	 * Create distance transform of source using arbitrary {@link Distance} d.
	 * Intermediate and final results will be stored in source (@{link
	 * DoubleType} recommended).
	 *
	 * @param source
	 *            Input function on which distance transform should be computed.
	 * @param d
	 *            {@link Distance} between two points.
	 * @param es
	 *            {@link ExecutorService} for parallel execution.
	 * @param nTasks
	 *            Number of tasks/parallelism
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public static < T extends RealType< T > > void transform(
			final RandomAccessibleInterval< T > source,
			final Distance d,
			final ExecutorService es,
			final int nTasks ) throws InterruptedException, ExecutionException
	{
		transform( source, source, d, es, nTasks );
	}

	/**
	 * Create distance transform of source using arbitrary {@link Distance} d.
	 * Intermediate and final results will be stored in target (@{link
	 * DoubleType} recommended).
	 *
	 * @param source
	 *            Input function on which distance transform should be computed.
	 * @param target
	 *            Final result of distance transform.
	 * @param d
	 *            {@link Distance} between two points.
	 * @param nThreads
	 *            Number of threads/parallelism
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public static < T extends RealType< T >, U extends RealType< U > > void transform(
			final RandomAccessible< T > source,
			final RandomAccessibleInterval< U > target,
			final Distance d,
			final int nThreads ) throws InterruptedException, ExecutionException
	{
		final ExecutorService es = Executors.newFixedThreadPool( nThreads );
		transform( source, target, d, es, nThreads );
		es.shutdown();
	}

	/**
	 * Create distance transform of source using arbitrary {@link Distance} d.
	 * Intermediate and final results will be stored in target (@{link
	 * DoubleType} recommended).
	 *
	 * @param source
	 *            Input function on which distance transform should be computed.
	 * @param target
	 *            Final result of distance transform.
	 * @param d
	 *            {@link Distance} between two points.
	 * @param es
	 *            {@link ExecutorService} for parallel execution.
	 * @param nTasks
	 *            Number of tasks/parallelism
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public static < T extends RealType< T >, U extends RealType< U > > void transform(
			final RandomAccessible< T > source,
			final RandomAccessibleInterval< U > target,
			final Distance d,
			final ExecutorService es,
			final int nTasks ) throws InterruptedException, ExecutionException
	{
		transform( source, target, target, d, es, nTasks );
	}

	/**
	 * Create distance transform of source using arbitrary {@link Distance} d.
	 * Intermediate results will be stored in tmp (@{link DoubleType}
	 * recommended). The output will be written into target.
	 *
	 * @param source
	 *            Input function on which distance transform should be computed.
	 * @param tmp
	 *            Storage for intermediate results.
	 * @param target
	 *            Final result of distance transform.
	 * @param d
	 *            {@link Distance} between two points.
	 * @param nThreads
	 *            Number of threads/parallelism
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public static < T extends RealType< T >, U extends RealType< U >, V extends RealType< V > > void transform(
			final RandomAccessible< T > source,
			final RandomAccessibleInterval< U > tmp,
			final RandomAccessibleInterval< V > target,
			final Distance d,
			final int nThreads ) throws InterruptedException, ExecutionException
	{
		final ExecutorService es = Executors.newFixedThreadPool( nThreads );
		transform( source, tmp, target, d, es, nThreads );
		es.shutdown();
	}

	/**
	 * Create distance transform of source using arbitrary {@link Distance} d.
	 * Intermediate results will be stored in tmp (@{link DoubleType}
	 * recommended). The output will be written into target.
	 *
	 * @param source
	 *            Input function on which distance transform should be computed.
	 * @param tmp
	 *            Storage for intermediate results.
	 * @param target
	 *            Final result of distance transform.
	 * @param d
	 *            {@link Distance} between two points.
	 * @param es
	 *            {@link ExecutorService} for parallel execution.
	 * @param nTasks
	 *            Number of tasks/parallelism
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public static < T extends RealType< T >, U extends RealType< U >, V extends RealType< V > > void transform(
			final RandomAccessible< T > source,
			final RandomAccessibleInterval< U > tmp,
			final RandomAccessibleInterval< V > target,
			final Distance d,
			final ExecutorService es,
			final int nTasks ) throws InterruptedException, ExecutionException
	{

		assert source.numDimensions() == target.numDimensions(): "Dimension mismatch";
		final int nDim = source.numDimensions();

		if ( nDim == 1 )
			transformDimension(
					( RandomAccessible< T > ) Views.addDimension( source ),
					Views.interval( Views.addDimension( tmp ), new FinalInterval( target.dimension( 0 ), 1 ) ),
					d,
					0,
					es,
					nTasks );
		else
			transformDimension( source, tmp, d, 0, es, nTasks );

		for ( int dim = 1; dim < nDim; ++dim )
			transformDimension( tmp, tmp, d, dim, es, nTasks );

		if ( tmp != target )
			for ( final Pair< U, V > p : Views.interval( Views.pair( tmp, target ), tmp ) )
				p.getB().setReal( p.getA().getRealDouble() );

	}

	/**
	 * Create distance transform of source using L1 distance. Intermediate
	 * results will be stored in tmp (@{link DoubleType} recommended). The
	 * output will be written into target. The distance can be weighted
	 * (individually for each dimension, if desired) against the image values
	 * via the weights parameter.
	 *
	 * @param source
	 *            Input function on which distance transform should be computed.
	 * @param tmp
	 *            Storage for intermediate results.
	 * @param target
	 *            Final result of distance transform.
	 * @param es
	 *            {@link ExecutorService} for parallel execution.
	 * @param nTasks
	 *            Number of tasks/parallelism
	 * @param weights
	 *            Individual weights for each dimension, balancing image values
	 *            and L1 distance.
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	private static < T extends RealType< T >, U extends RealType< U >, V extends RealType< V > > void transformL1(
			final RandomAccessible< T > source,
			final RandomAccessibleInterval< U > tmp,
			final RandomAccessibleInterval< V > target,
			final ExecutorService es,
			final int nTasks,
			final double... weights ) throws InterruptedException, ExecutionException
	{
		assert source.numDimensions() == target.numDimensions(): "Dimension mismatch";
		final int nDim = source.numDimensions();

		if ( nDim == 1 )
			transformL1Dimension(
					( RandomAccessible< T > ) Views.addDimension( source ),
					Views.interval( Views.addDimension( tmp ), new FinalInterval( target.dimension( 0 ), 1 ) ),
					0,
					weights[ 0 ],
					es,
					nTasks );
		else
			transformL1Dimension( source, tmp, 0, weights[ 0 ], es, nTasks );

		for ( int dim = 1; dim < nDim; ++dim )
			transformL1Dimension( tmp, tmp, dim, weights[ dim ], es, nTasks );

		if ( tmp != target )
			for ( final Pair< U, V > p : Views.interval( Views.pair( tmp, target ), target ) )
				p.getB().setReal( p.getA().getRealDouble() );
	}

	private static < T extends RealType< T >, U extends RealType< U > > void transformDimension(
			final RandomAccessible< T > source,
			final RandomAccessibleInterval< U > target,
			final Distance d,
			final int dim,
			final ExecutorService es,
			final int nTasks ) throws InterruptedException, ExecutionException
	{
		final int lastDim = target.numDimensions() - 1;
		final long size = target.dimension( dim );
		final long nComposites = Views.flatIterable( Views.collapseReal( Views.permute( target, dim, lastDim ) ) ).size();
		final long nCompositesPerChunk = nComposites / nTasks;

		final ArrayList< Callable< Void > > tasks = new ArrayList<>();
		for ( long lower = 0; lower < nComposites; lower += nCompositesPerChunk )
		{

			final long fLower = lower;
			tasks.add( () -> {
				final RealComposite< DoubleType > tmp = Views.collapseReal( createAppropriateOneDimensionalImage( size, new DoubleType() ) ).randomAccess().get();
				final Cursor< RealComposite< T > > s = Views.flatIterable( Views.collapseReal( Views.permute( Views.interval( source, target ), dim, lastDim ) ) ).cursor();
				final Cursor< RealComposite< U > > t = Views.flatIterable( Views.collapseReal( Views.permute( target, dim, lastDim ) ) ).cursor();
				final RealComposite< LongType > lowerBoundDistanceIndex = Views.collapseReal( createAppropriateOneDimensionalImage( size, new LongType() ) ).randomAccess().get();
				final RealComposite< DoubleType > envelopeIntersectLocation = Views.collapseReal( createAppropriateOneDimensionalImage( size + 1, new DoubleType() ) ).randomAccess().get();
				s.jumpFwd( fLower );
				t.jumpFwd( fLower );

				for ( long count = 0; count < nCompositesPerChunk && t.hasNext(); ++count )
				{
					final RealComposite< T > sourceComp = s.next();
					final RealComposite< U > targetComp = t.next();
					for ( long i = 0; i < size; ++i )
						tmp.get( i ).set( sourceComp.get( i ).getRealDouble() );

					transform1D( tmp, targetComp, lowerBoundDistanceIndex, envelopeIntersectLocation, d, dim, size );

				}
				return null;
			} );
		}
		invokeAllAndWait( es, tasks );
	}

	private static < T extends RealType< T >, U extends RealType< U > > void transform1D(
			final RealComposite< T > source,
			final RealComposite< U > target,
			final RealComposite< LongType > lowerBoundDistanceIndex,
			final RealComposite< DoubleType > envelopeIntersectLocation,
			final Distance d,
			final int dim,
			final long size )
	{
		long k = 0;

		lowerBoundDistanceIndex.get( 0 ).set( 0 );
		envelopeIntersectLocation.get( 0 ).set( -1e20 );
		envelopeIntersectLocation.get( 1 ).set( +1e20 );
		for ( long position = 1; position < size; ++position )
		{
			long envelopeIndexAtK = lowerBoundDistanceIndex.get( k ).get();
			final double sourceAtPosition = source.get( position ).getRealDouble();
			double s = d.intersect( envelopeIndexAtK, source.get( envelopeIndexAtK ).getRealDouble(), position, sourceAtPosition, dim );

			for ( double envelopeValueAtK = envelopeIntersectLocation.get( k ).get(); s <= envelopeValueAtK; envelopeValueAtK = envelopeIntersectLocation.get( k ).get() )
			{
				--k;
				envelopeIndexAtK = lowerBoundDistanceIndex.get( k ).get();
				s = d.intersect( envelopeIndexAtK, source.get( envelopeIndexAtK ).getRealDouble(), position, sourceAtPosition, dim );
			}
			++k;
			lowerBoundDistanceIndex.get( k ).set( position );
			envelopeIntersectLocation.get( k ).set( s );
			envelopeIntersectLocation.get( k + 1 ).set( 1e20 );
		}

		k = 0;

		for ( long position = 0; position < size; ++position )
		{
			while ( envelopeIntersectLocation.get( k + 1 ).get() < position )
				++k;
			final long envelopeIndexAtK = lowerBoundDistanceIndex.get( k ).get();
			// copy necessary because of the following line, access to source
			// after write to source -> source and target cannot be the same
			target.get( position ).setReal( d.evaluate( position, envelopeIndexAtK, source.get( envelopeIndexAtK ).getRealDouble(), dim ) );
		}

	}

	private static < T extends RealType< T >, U extends RealType< U > > void transformL1Dimension(
			final RandomAccessible< T > source,
			final RandomAccessibleInterval< U > target,
			final int dim,
			final double weight,
			final ExecutorService es,
			final int nTasks ) throws InterruptedException, ExecutionException
	{
		final int lastDim = target.numDimensions() - 1;
		final long size = target.dimension( dim );
		final long nComposites = Views.flatIterable( Views.collapseReal( Views.permute( target, dim, lastDim ) ) ).size();
		final long nCompositesPerChunk = nComposites / nTasks;
		final int fDim = dim;

		final ArrayList< Callable< Void > > tasks = new ArrayList<>();
		for ( long lower = 0; lower < nComposites; lower += nCompositesPerChunk )
		{

			final long fLower = lower;
			tasks.add( () -> {
				final RealComposite< DoubleType > tmp = Views.collapseReal( createAppropriateOneDimensionalImage( size, new DoubleType() ) ).randomAccess().get();
				final Cursor< RealComposite< T > > s = Views.flatIterable( Views.collapseReal( Views.permute( Views.interval( source, target ), fDim, lastDim ) ) ).cursor();
				final Cursor< RealComposite< U > > t = Views.flatIterable( Views.collapseReal( Views.permute( target, fDim, lastDim ) ) ).cursor();
				s.jumpFwd( fLower );
				t.jumpFwd( fLower );

				for ( long count = 0; count < nCompositesPerChunk && t.hasNext(); ++count )
				{
					final RealComposite< T > sourceComp = s.next();
					final RealComposite< U > targetComp = t.next();
					for ( long i = 0; i < size; ++i )
						tmp.get( i ).set( sourceComp.get( i ).getRealDouble() );

					transformL1_1D( tmp, targetComp, weight, size );

				}
				return null;
			} );
		}
		invokeAllAndWait( es, tasks );
	}

	private static < T extends RealType< T >, U extends RealType< U > > void transformL1_1D(
			final RealComposite< T > source,
			final RealComposite< U > target,
			final double weight,
			final long size )
	{

		target.get( 0 ).setReal( source.get( 0 ).getRealDouble() );

		for ( long i = 1; i < size; ++i )
		{
			final double other = target.get( i - 1 ).getRealDouble();
			target.get( i ).setReal( Math.min( source.get( i ).getRealDouble(), other + weight ) );
		}

		for ( long i = size - 2; i > -1; --i )
		{
			final double other = target.get( i + 1 ).getRealDouble();
			final U t = target.get( i );
			t.setReal( Math.min( t.getRealDouble(), other + weight ) );
		}

	}

	/**
	 * Convenience method to invoke all tasks with a given
	 * {@link ExecutorService}.
	 */
	private static < T > void invokeAllAndWait( final ExecutorService es, final Collection< Callable< T > > tasks ) throws InterruptedException, ExecutionException
	{
		final List< Future< T > > futures = es.invokeAll( tasks );
		for ( final Future< T > f : futures )
			f.get();
	}

	/**
	 * Convenience method for creating an appropriate storage img:
	 * {@link ArrayImg} if size is less than {@link Integer#MAX_VALUE},
	 * {@link CellImg} otherwise.
	 */
	private static < T extends NativeType< T > & RealType< T > > Img< T > createAppropriateOneDimensionalImage( final long size, final T t )
	{
		final long[] dim = new long[] { 1, size };
		return size > Integer.MAX_VALUE ? new CellImgFactory< T >( Integer.MAX_VALUE ).create( dim, t ) : new ArrayImgFactory< T >().create( dim, t );
	}

}
