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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.cell.CellImg;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.parallel.Parallelization;
import net.imglib2.parallel.TaskExecutors;
import net.imglib2.type.BooleanType;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Util;
import net.imglib2.util.ValuePair;
import net.imglib2.view.Views;
import net.imglib2.view.composite.RealComposite;

/**
 * ImgLib2 implementation of n-dimensional distance transform D of sampled
 * functions f with distance measure d:
 * <a href="http://www.theoryofcomputing.org/articles/v008a019/">Distance
 * Transforms of Sampled Functions</a> (DOI:ch 10.4086/toc.2012.v008a019).
 * <p>
 * {@code D( p ) = min_q f(q) + d(p,q)} where p,q are points on a grid/image.
 * </p>
 * <p>
 * The implemented algorithm has complexity O(dn) where d is the number of
 * dimensions of the image, and n is the total number of pixels/voxels.
 * </p>
 *
 * @author Philipp Hanslovsky
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
	/**
	 * Squared Euclidian distance using {@link EuclidianDistanceIsotropic} or
	 * {@link EuclidianDistanceAnisotropic}.
	 */
	EUCLIDIAN,
	/**
	 * L1 distance using special case implementation.
	 */
	L1
	}

	/**
	 * Create
	 * <a href="http://www.theoryofcomputing.org/articles/v008a019/">distance
	 * transforms of sampled functions</a> on {@code source} using squared
	 * Euclidian (L2) or L1 distance. Intermediate and final results will be
	 * stored in {@code source} ({@link DoubleType} recommended). The distance
	 * can be weighted (individually for each dimension, if desired) against the
	 * image values via the weights parameter.
	 *
	 * @param source
	 *            Input function on which distance transform should be computed.
	 * @param distanceType
	 *            Defines distance to be used: squared Euclidian or L1
	 * @param weights
	 *            Individual weights for each dimension, balancing image values
	 *            and distance (when using squared Euclidian distance, weights
	 *            should be squared, too).
	 * @param <T>
	 *            {@link RealType} input
	 */
	public static < T extends RealType< T > > void transform(
			final RandomAccessibleInterval< T > source,
			final DISTANCE_TYPE distanceType,
			final double... weights )
	{
		transform( source, source, distanceType, weights );
	}

	/**
	 * Create
	 * <a href="http://www.theoryofcomputing.org/articles/v008a019/">distance
	 * transforms of sampled functions</a> on {@code source} using squared
	 * Euclidian (L2) or L1 distance. Intermediate and final results will be
	 * stored in {@code source} ({@link DoubleType} recommended). The distance
	 * can be weighted (individually for each dimension, if desired) against the
	 * image values via the weights parameter.
	 *
	 * @param source
	 *            Input function on which distance transform should be computed.
	 * @param distanceType
	 *            Defines distance to be used: squared Euclidian or L1
	 * @param es
	 *            {@link ExecutorService} for parallel execution.
	 * @param nTasks
	 *            Number of tasks/parallelism
	 * @param weights
	 *            Individual weights for each dimension, balancing image values
	 *            and distance (when using squared Euclidian distance, weights
	 *            should be squared, too).
	 * @param <T>
	 *            {@link RealType} input
	 * @throws InterruptedException
	 *             if interrupted while waiting, in which case unfinished tasks
	 *             are cancelled (distance transform may be computed only
	 *             partially)
	 * @throws ExecutionException
	 *             if the computation threw an exception (distance transform may
	 *             be computed only partially)
	 */
	public static < T extends RealType< T > > void transform(
			final RandomAccessibleInterval< T > source,
			final DISTANCE_TYPE distanceType,
			final ExecutorService es,
			final int nTasks,
			final double... weights ) throws InterruptedException, ExecutionException
	{
		Parallelization.runWithExecutor( TaskExecutors.forExecutorServiceAndNumTasks( es, nTasks ),
				() -> transform( source, source, distanceType, weights ) );
	}

	/**
	 * Create
	 * <a href="http://www.theoryofcomputing.org/articles/v008a019/">distance
	 * transforms of sampled functions</a> on {@code source} using squared
	 * Euclidian (L2) or L1 distance. Intermediate results will be stored in
	 * {@code target} ({@link DoubleType} recommended). The distance can be
	 * weighted (individually for each dimension, if desired) against the image
	 * values via the weights parameter.
	 *
	 * @param source
	 *            Input function on which distance transform should be computed.
	 * @param target
	 *            Intermediate and final results of distance transform.
	 * @param distanceType
	 *            Defines distance to be used: squared Euclidian or L1
	 * @param weights
	 *            Individual weights for each dimension, balancing image values
	 *            and distance (when using squared Euclidian distance, weights
	 *            should be squared, too).
	 * @param <T>
	 *            {@link RealType} input
	 * @param <U>
	 *            {@link RealType} intermediate results
	 */
	public static < T extends RealType< T >, U extends RealType< U > > void transform(
			final RandomAccessible< T > source,
			final RandomAccessibleInterval< U > target,
			final DISTANCE_TYPE distanceType,
			final double... weights )
	{
		transform( source, target, target, distanceType, weights );
	}

	/**
	 * Create
	 * <a href="http://www.theoryofcomputing.org/articles/v008a019/">distance
	 * transforms of sampled functions</a> on {@code source} using squared
	 * Euclidian (L2) or L1 distance. Intermediate results will be stored in
	 * {@code target} ({@link DoubleType} recommended). The distance can be
	 * weighted (individually for each dimension, if desired) against the image
	 * values via the weights parameter.
	 *
	 * @param source
	 *            Input function on which distance transform should be computed.
	 * @param target
	 *            Intermediate and final results of distance transform.
	 * @param distanceType
	 *            Defines distance to be used: squared Euclidian or L1
	 * @param es
	 *            {@link ExecutorService} for parallel execution.
	 * @param nTasks
	 *            Number of tasks/parallelism
	 * @param weights
	 *            Individual weights for each dimension, balancing image values
	 *            and distance (when using squared Euclidian distance, weights
	 *            should be squared, too).
	 * @param <T>
	 *            {@link RealType} input
	 * @param <U>
	 *            {@link RealType} intermediate results
	 * @throws InterruptedException
	 *             if interrupted while waiting, in which case unfinished tasks
	 *             are cancelled (distance transform may be computed only
	 *             partially)
	 * @throws ExecutionException
	 *             if the computation threw an exception (distance transform may
	 *             be computed only partially)
	 */
	public static < T extends RealType< T >, U extends RealType< U > > void transform(
			final RandomAccessible< T > source,
			final RandomAccessibleInterval< U > target,
			final DISTANCE_TYPE distanceType,
			final ExecutorService es,
			final int nTasks,
			final double... weights ) throws InterruptedException, ExecutionException
	{
		Parallelization.runWithExecutor( TaskExecutors.forExecutorServiceAndNumTasks( es, nTasks ),
				() -> transform( source, target, distanceType, weights ) );
	}

	/**
	 * Create
	 * <a href="http://www.theoryofcomputing.org/articles/v008a019/">distance
	 * transforms of sampled functions</a> on {@code source} using squared
	 * Euclidian (L2) or L1 distance. Intermediate results will be stored in
	 * {@code tmp} ({@link DoubleType} recommended). The output will be written
	 * into {@code target}. The distance can be weighted (individually for each
	 * dimension, if desired) against the image values via the weights
	 * parameter.
	 *
	 * @param source
	 *            Input function on which distance transform should be computed.
	 * @param tmp
	 *            Storage for intermediate results.
	 * @param target
	 *            Final result of distance transform.
	 * @param distanceType
	 *            Defines distance to be used: squared Euclidian or L1
	 * @param weights
	 *            Individual weights for each dimension, balancing image values
	 *            and distance (when using squared Euclidian distance, weights
	 *            should be squared, too).
	 * @param <T>
	 *            {@link RealType} input
	 * @param <U>
	 *            {@link RealType} intermediate results
	 * @param <V>
	 *            {@link RealType} output
	 */
	public static < T extends RealType< T >, U extends RealType< U >, V extends RealType< V > > void transform(
			final RandomAccessible< T > source,
			final RandomAccessibleInterval< U > tmp,
			final RandomAccessibleInterval< V > target,
			final DISTANCE_TYPE distanceType,
			final double... weights )
	{

		final boolean isIsotropic = weights.length <= 1;
		final double[] w = weights.length == source.numDimensions() ? weights : DoubleStream.generate( () -> weights.length == 0 ? 1.0 : weights[ 0 ] ).limit( source.numDimensions() ).toArray();

		switch ( distanceType )
		{
		case EUCLIDIAN:
			transform( source, tmp, target, isIsotropic ? new EuclidianDistanceIsotropic( w[ 0 ] ) : new EuclidianDistanceAnisotropic( w ) );
			break;
		case L1:
			transformL1( source, tmp, target, w );
			break;
		default:
			break;
		}
	}

	/**
	 * Create
	 * <a href="http://www.theoryofcomputing.org/articles/v008a019/">distance
	 * transforms of sampled functions</a> on {@code source} using squared
	 * Euclidian (L2) or L1 distance. Intermediate results will be stored in
	 * {@code tmp} ({@link DoubleType} recommended). The output will be written
	 * into {@code target}. The distance can be weighted (individually for each
	 * dimension, if desired) against the image values via the weights
	 * parameter.
	 *
	 * @param source
	 *            Input function on which distance transform should be computed.
	 * @param tmp
	 *            Storage for intermediate results.
	 * @param target
	 *            Final result of distance transform.
	 * @param distanceType
	 *            Defines distance to be used: squared Euclidian or L1
	 * @param es
	 *            {@link ExecutorService} for parallel execution.
	 * @param nTasks
	 *            Number of tasks/parallelism
	 * @param weights
	 *            Individual weights for each dimension, balancing image values
	 *            and distance (when using squared Euclidian distance, weights
	 *            should be squared, too).
	 * @param <T>
	 *            {@link RealType} input
	 * @param <U>
	 *            {@link RealType} intermediate results
	 * @param <V>
	 *            {@link RealType} output
	 * @throws InterruptedException
	 *             if interrupted while waiting, in which case unfinished tasks
	 *             are cancelled (distance transform may be computed only
	 *             partially)
	 * @throws ExecutionException
	 *             if the computation threw an exception (distance transform may
	 *             be computed only partially)
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
		Parallelization.runWithExecutor( TaskExecutors.forExecutorServiceAndNumTasks( es, nTasks ),
				() -> transform( source, tmp, target, distanceType, weights ) );
	}

	/**
	 * Create
	 * <a href="http://www.theoryofcomputing.org/articles/v008a019/">distance
	 * transforms of sampled functions</a> on {@code source} using arbitrary
	 * {@link Distance} d. Intermediate and final results will be stored in
	 * {@code source} ({@link DoubleType} recommended).
	 *
	 * @param source
	 *            Input function on which distance transform should be computed.
	 * @param d
	 *            {@link Distance} between two points.
	 * @param <T>
	 *            {@link RealType} input
	 */
	public static < T extends RealType< T > > void transform(
			final RandomAccessibleInterval< T > source,
			final Distance d )
	{
		transform( source, source, d );
	}

	/**
	 * Create
	 * <a href="http://www.theoryofcomputing.org/articles/v008a019/">distance
	 * transforms of sampled functions</a> on {@code source} using arbitrary
	 * {@link Distance} d. Intermediate and final results will be stored in
	 * {@code source} ({@link DoubleType} recommended).
	 *
	 * @param source
	 *            Input function on which distance transform should be computed.
	 * @param d
	 *            {@link Distance} between two points.
	 * @param es
	 *            {@link ExecutorService} for parallel execution.
	 * @param nTasks
	 *            Number of tasks/parallelism
	 * @param <T>
	 *            {@link RealType} input
	 * @throws InterruptedException
	 *             if interrupted while waiting, in which case unfinished tasks
	 *             are cancelled (distance transform may be computed only
	 *             partially)
	 * @throws ExecutionException
	 *             if the computation threw an exception (distance transform may
	 *             be computed only partially)
	 */
	public static < T extends RealType< T > > void transform(
			final RandomAccessibleInterval< T > source,
			final Distance d,
			final ExecutorService es,
			final int nTasks ) throws InterruptedException, ExecutionException
	{
		Parallelization.runWithExecutor( TaskExecutors.forExecutorServiceAndNumTasks( es, nTasks ),
				() -> transform( source, d ) );
	}

	/**
	 * Create
	 * <a href="http://www.theoryofcomputing.org/articles/v008a019/">distance
	 * transforms of sampled functions</a> on {@code source} using arbitrary
	 * {@link Distance} d. Intermediate and final results will be stored in
	 * {@code target} ({@link DoubleType} recommended).
	 *
	 * @param source
	 *            Input function on which distance transform should be computed.
	 * @param target
	 *            Final result of distance transform.
	 * @param d
	 *            {@link Distance} between two points.
	 * @param <T>
	 *            {@link RealType} input
	 * @param <U>
	 *            {@link RealType} intermediate results
	 */
	public static < T extends RealType< T >, U extends RealType< U > > void transform(
			final RandomAccessible< T > source,
			final RandomAccessibleInterval< U > target,
			final Distance d )
	{
		transform( source, target, target, d );
	}

	/**
	 * Create
	 * <a href="http://www.theoryofcomputing.org/articles/v008a019/">distance
	 * transforms of sampled functions</a> on {@code source} using arbitrary
	 * {@link Distance} d. Intermediate and final results will be stored in
	 * {@code target} ({@link DoubleType} recommended).
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
	 * @param <T>
	 *            {@link RealType} input
	 * @param <U>
	 *            {@link RealType} intermediate results
	 * @throws InterruptedException
	 *             if interrupted while waiting, in which case unfinished tasks
	 *             are cancelled (distance transform may be computed only
	 *             partially)
	 * @throws ExecutionException
	 *             if the computation threw an exception (distance transform may
	 *             be computed only partially)
	 */
	public static < T extends RealType< T >, U extends RealType< U > > void transform(
			final RandomAccessible< T > source,
			final RandomAccessibleInterval< U > target,
			final Distance d,
			final ExecutorService es,
			final int nTasks ) throws InterruptedException, ExecutionException
	{
		Parallelization.runWithExecutor( TaskExecutors.forExecutorServiceAndNumTasks( es, nTasks ),
				() -> transform( source, target, d ) );
	}

	/**
	 * Create
	 * <a href="http://www.theoryofcomputing.org/articles/v008a019/">distance
	 * transforms of sampled functions</a> on {@code source} using arbitrary
	 * {@link Distance} d. Intermediate results will be stored in {@code tmp}
	 * ({@link DoubleType} recommended). The output will be written into
	 * {@code target}.
	 *
	 * @param source
	 *            Input function on which distance transform should be computed.
	 * @param tmp
	 *            Storage for intermediate results.
	 * @param target
	 *            Final result of distance transform.
	 * @param d
	 *            {@link Distance} between two points.
	 * @param <T>
	 *            {@link RealType} input
	 * @param <U>
	 *            {@link RealType} intermediate results
	 * @param <V>
	 *            {@link RealType} output
	 */
	public static < T extends RealType< T >, U extends RealType< U >, V extends RealType< V > > void transform(
			final RandomAccessible< T > source,
			final RandomAccessibleInterval< U > tmp,
			final RandomAccessibleInterval< V > target,
			final Distance d )
	{

		assert source.numDimensions() == target.numDimensions(): "Dimension mismatch";
		final int nDim = source.numDimensions();
		final int lastDim = nDim - 1;

		if ( nDim == 1 )
		{
			transformAlongDimension(
					( RandomAccessible< T > ) Views.addDimension( source ),
					Views.interval( Views.addDimension( target ), new FinalInterval( target.dimension( 0 ), 1 ) ),
					d,
					0 );
		}
		else
		{
			transformAlongDimension( source, tmp, d, 0 );
		}

		for ( int dim = 1; dim < nDim; ++dim )
		{
			if ( dim == lastDim )
			{
				transformAlongDimension( tmp, target, d, dim );
			}
			else
			{
				transformAlongDimension( tmp, tmp, d, dim );
			}
		}
	}

	/**
	 * Create
	 * <a href="http://www.theoryofcomputing.org/articles/v008a019/">distance
	 * transforms of sampled functions</a> on {@code source} using arbitrary
	 * {@link Distance} d. Intermediate results will be stored in {@code tmp}
	 * ({@link DoubleType} recommended). The output will be written into
	 * {@code target}.
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
	 * @param <T>
	 *            {@link RealType} input
	 * @param <U>
	 *            {@link RealType} intermediate results
	 * @param <V>
	 *            {@link RealType} output
	 * @throws InterruptedException
	 *             if interrupted while waiting, in which case unfinished tasks
	 *             are cancelled (distance transform may be computed only
	 *             partially)
	 * @throws ExecutionException
	 *             if the computation threw an exception (distance transform may
	 *             be computed only partially)
	 */
	public static < T extends RealType< T >, U extends RealType< U >, V extends RealType< V > > void transform(
			final RandomAccessible< T > source,
			final RandomAccessibleInterval< U > tmp,
			final RandomAccessibleInterval< V > target,
			final Distance d,
			final ExecutorService es,
			final int nTasks ) throws InterruptedException, ExecutionException
	{
		Parallelization.runWithExecutor( TaskExecutors.forExecutorServiceAndNumTasks( es, nTasks ),
				() -> transform( source, tmp, target, d ) );
	}

	/**
	 * Create binary distance transform on {@code source} using squared
	 * Euclidian (L2) or L1 distance. Intermediate results will be stored in
	 * {@code target} ({@link DoubleType} recommended). The distance can be
	 * weighted (individually for each dimension, if desired) against the image
	 * values via the weights parameter.
	 *
	 * @param source
	 *            Input function on which distance transform should be computed.
	 * @param target
	 *            Intermediate and final results of distance transform.
	 * @param distanceType
	 *            Defines distance to be used: squared Euclidian or L1
	 * @param weights
	 *            Individual weights for each dimension, balancing image values
	 *            and distance (when using squared Euclidian distance, weights
	 *            should be squared, too).
	 * @param <B>
	 *            {@link BooleanType} binary mask input
	 * @param <U>
	 *            {@link RealType} intermediate results
	 */
	public static < B extends BooleanType< B >, U extends RealType< U > > void binaryTransform(
			final RandomAccessible< B > source,
			final RandomAccessibleInterval< U > target,
			final DISTANCE_TYPE distanceType,
			final double... weights )
	{
		binaryTransform( source, target, target, distanceType, weights );
	}

	/**
	 * Create binary distance transform on {@code source} using squared
	 * Euclidian (L2) or L1 distance. Intermediate results will be stored in
	 * {@code target} ({@link DoubleType} recommended). The distance can be
	 * weighted (individually for each dimension, if desired) against the image
	 * values via the weights parameter.
	 *
	 * @param source
	 *            Input function on which distance transform should be computed.
	 * @param target
	 *            Intermediate and final results of distance transform.
	 * @param distanceType
	 *            Defines distance to be used: squared Euclidian or L1
	 * @param es
	 *            {@link ExecutorService} for parallel execution.
	 * @param nTasks
	 *            Number of tasks/parallelism
	 * @param weights
	 *            Individual weights for each dimension, balancing image values
	 *            and distance (when using squared Euclidian distance, weights
	 *            should be squared, too).
	 * @param <B>
	 *            {@link BooleanType} binary mask input
	 * @param <U>
	 *            {@link RealType} intermediate results
	 * @throws InterruptedException
	 *             if interrupted while waiting, in which case unfinished tasks
	 *             are cancelled (distance transform may be computed only
	 *             partially)
	 * @throws ExecutionException
	 *             if the computation threw an exception (distance transform may
	 *             be computed only partially)
	 */
	public static < B extends BooleanType< B >, U extends RealType< U > > void binaryTransform(
			final RandomAccessible< B > source,
			final RandomAccessibleInterval< U > target,
			final DISTANCE_TYPE distanceType,
			final ExecutorService es,
			final int nTasks,
			final double... weights ) throws InterruptedException, ExecutionException
	{
		Parallelization.runWithExecutor( TaskExecutors.forExecutorServiceAndNumTasks( es, nTasks ),
				() -> binaryTransform( source, target, distanceType, weights ) );
	}

	/**
	 * Create binary distance transform on {@code source} using squared
	 * Euclidian (L2) or L1 distance. Intermediate results will be stored in
	 * {@code tmp} ({@link DoubleType} recommended). The output will be written
	 * into {@code target}. The distance can be weighted (individually for each
	 * dimension, if desired) against the image values via the weights
	 * parameter.
	 *
	 * @param source
	 *            Input function on which distance transform should be computed.
	 * @param tmp
	 *            Storage for intermediate results.
	 * @param target
	 *            Final result of distance transform.
	 * @param distanceType
	 *            Defines distance to be used: squared Euclidian or L1
	 * @param weights
	 *            Individual weights for each dimension, balancing image values
	 *            and distance (when using squared Euclidian distance, weights
	 *            should be squared, too).
	 * @param <B>
	 *            {@link BooleanType} binary mask input
	 * @param <U>
	 *            {@link RealType} intermediate results
	 * @param <V>
	 *            {@link RealType} output
	 */
	public static < B extends BooleanType< B >, U extends RealType< U >, V extends RealType< V > > void binaryTransform(
			final RandomAccessible< B > source,
			final RandomAccessibleInterval< U > tmp,
			final RandomAccessibleInterval< V > target,
			final DISTANCE_TYPE distanceType,
			final double... weights )
	{

		final U maxVal = Util.getTypeFromInterval( tmp ).createVariable();
		maxVal.setReal( maxVal.getMaxValue() );
		final Converter< B, U > converter = new BinaryMaskToCost<>( maxVal );
		final RandomAccessible< U > converted = Converters.convert( source, converter, maxVal.createVariable() );
		transform( converted, tmp, target, distanceType, weights );
	}

	/**
	 * Create binary distance transform on {@code source} using squared
	 * Euclidian (L2) or L1 distance. Intermediate results will be stored in
	 * {@code tmp} ({@link DoubleType} recommended). The output will be written
	 * into {@code target}. The distance can be weighted (individually for each
	 * dimension, if desired) against the image values via the weights
	 * parameter.
	 *
	 * @param source
	 *            Input function on which distance transform should be computed.
	 * @param tmp
	 *            Storage for intermediate results.
	 * @param target
	 *            Final result of distance transform.
	 * @param distanceType
	 *            Defines distance to be used: squared Euclidian or L1
	 * @param es
	 *            {@link ExecutorService} for parallel execution.
	 * @param nTasks
	 *            Number of tasks/parallelism
	 * @param weights
	 *            Individual weights for each dimension, balancing image values
	 *            and distance (when using squared Euclidian distance, weights
	 *            should be squared, too).
	 * @param <B>
	 *            {@link BooleanType} binary mask input
	 * @param <U>
	 *            {@link RealType} intermediate results
	 * @param <V>
	 *            {@link RealType} output
	 * @throws InterruptedException
	 *             if interrupted while waiting, in which case unfinished tasks
	 *             are cancelled (distance transform may be computed only
	 *             partially)
	 * @throws ExecutionException
	 *             if the computation threw an exception (distance transform may
	 *             be computed only partially)
	 */
	public static < B extends BooleanType< B >, U extends RealType< U >, V extends RealType< V > > void binaryTransform(
			final RandomAccessible< B > source,
			final RandomAccessibleInterval< U > tmp,
			final RandomAccessibleInterval< V > target,
			final DISTANCE_TYPE distanceType,
			final ExecutorService es,
			final int nTasks,
			final double... weights ) throws InterruptedException, ExecutionException
	{
		Parallelization.runWithExecutor( TaskExecutors.forExecutorServiceAndNumTasks( es, nTasks ),
				() -> binaryTransform( source, tmp, target, distanceType, weights ) );
	}

	/**
	 * Create binary distance transform on {@code source} using arbitrary
	 * {@link Distance} d. Intermediate and final results will be stored in
	 * source ({@link DoubleType} recommended).
	 *
	 * @param source
	 *            Input function on which distance transform should be computed.
	 * @param d
	 *            {@link Distance} between two points.
	 * @param es
	 *            {@link ExecutorService} for parallel execution.
	 * @param nTasks
	 *            Number of tasks/parallelism
	 * @param <B>
	 *            {@link BooleanType} binary mask input
	 * @throws InterruptedException
	 *             if interrupted while waiting, in which case unfinished tasks
	 *             are cancelled (distance transform may be computed only
	 *             partially)
	 * @throws ExecutionException
	 *             if the computation threw an exception (distance transform may
	 *             be computed only partially)
	 */
	public static < B extends BooleanType< B > > void binaryTransform(
			final RandomAccessibleInterval< B > source,
			final Distance d,
			final ExecutorService es,
			final int nTasks ) throws InterruptedException, ExecutionException
	{
		binaryTransform( source, source, d, es, nTasks );
	}

	/**
	 * Create binary distance transform on {@code source} using arbitrary
	 * {@link Distance} d. Intermediate and final results will be stored in
	 * {@code target} ({@link DoubleType} recommended).
	 *
	 * @param source
	 *            Input function on which distance transform should be computed.
	 * @param target
	 *            Final result of distance transform.
	 * @param d
	 *            {@link Distance} between two points.
	 * @param <B>
	 *            {@link BooleanType} binary mask input
	 * @param <U>
	 *            {@link RealType} intermediate results
	 */
	public static < B extends BooleanType< B >, U extends RealType< U > > void binaryTransform(
			final RandomAccessible< B > source,
			final RandomAccessibleInterval< U > target,
			final Distance d )
	{
		binaryTransform( source, target, target, d );
	}

	/**
	 * Create binary distance transform on {@code source} using arbitrary
	 * {@link Distance} d. Intermediate and final results will be stored in
	 * {@code target} ({@link DoubleType} recommended).
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
	 * @param <B>
	 *            {@link BooleanType} binary mask input
	 * @param <U>
	 *            {@link RealType} intermediate results
	 * @throws InterruptedException
	 *             if interrupted while waiting, in which case unfinished tasks
	 *             are cancelled (distance transform may be computed only
	 *             partially)
	 * @throws ExecutionException
	 *             if the computation threw an exception (distance transform may
	 *             be computed only partially)
	 */
	public static < B extends BooleanType< B >, U extends RealType< U > > void binaryTransform(
			final RandomAccessible< B > source,
			final RandomAccessibleInterval< U > target,
			final Distance d,
			final ExecutorService es,
			final int nTasks ) throws InterruptedException, ExecutionException
	{
		Parallelization.runWithExecutor( TaskExecutors.forExecutorServiceAndNumTasks( es, nTasks ),
				() -> binaryTransform( source, target, d ) );
	}

	/**
	 * Create binary distance transform on {@code source} using arbitrary
	 * {@link Distance} d. Intermediate results will be stored in {@code tmp}
	 * ({@link DoubleType} recommended). The output will be written into
	 * {@code target}.
	 *
	 * @param source
	 *            Input function on which distance transform should be computed.
	 * @param tmp
	 *            Storage for intermediate results.
	 * @param target
	 *            Final result of distance transform.
	 * @param d
	 *            {@link Distance} between two points.
	 * @param <B>
	 *            {@link BooleanType} binary mask input
	 * @param <U>
	 *            {@link RealType} intermediate results
	 * @param <V>
	 *            {@link RealType} output
	 */
	public static < B extends BooleanType< B >, U extends RealType< U >, V extends RealType< V > > void binaryTransform(
			final RandomAccessible< B > source,
			final RandomAccessibleInterval< U > tmp,
			final RandomAccessibleInterval< V > target,
			final Distance d )
	{
		final U maxVal = Util.getTypeFromInterval( tmp ).createVariable();
		maxVal.setReal( maxVal.getMaxValue() );
		final Converter< B, U > converter = new BinaryMaskToCost<>( maxVal );
		final RandomAccessible< U > converted = Converters.convert( source, converter, maxVal.createVariable() );
		transform( converted, tmp, target, d );
	}

	/**
	 * Create binary distance transform on {@code source} using arbitrary
	 * {@link Distance} d. Intermediate results will be stored in {@code tmp}
	 * ({@link DoubleType} recommended). The output will be written into
	 * {@code target}.
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
	 * @param <B>
	 *            {@link BooleanType} binary mask input
	 * @param <U>
	 *            {@link RealType} intermediate results
	 * @param <V>
	 *            {@link RealType} output
	 * @throws InterruptedException
	 *             if interrupted while waiting, in which case unfinished tasks
	 *             are cancelled (distance transform may be computed only
	 *             partially)
	 * @throws ExecutionException
	 *             if the computation threw an exception (distance transform may
	 *             be computed only partially)
	 */
	public static < B extends BooleanType< B >, U extends RealType< U >, V extends RealType< V > > void binaryTransform(
			final RandomAccessible< B > source,
			final RandomAccessibleInterval< U > tmp,
			final RandomAccessibleInterval< V > target,
			final Distance d,
			final ExecutorService es,
			final int nTasks ) throws InterruptedException, ExecutionException
	{
		Parallelization.runWithExecutor( TaskExecutors.forExecutorServiceAndNumTasks( es, nTasks ),
				() -> binaryTransform( source, tmp, target, d ) );
	}

	/**
	 * Create binary distance transform on {@code source} using L1 distance.
	 * Intermediate results will be stored in {@code tmp} ({@link DoubleType}
	 * recommended). The output will be written into {@code target}. The
	 * distance can be weighted (individually for each dimension, if desired)
	 * against the image values via the weights parameter.
	 *
	 * @param source
	 *            Input function on which distance transform should be computed.
	 * @param tmp
	 *            Storage for intermediate results.
	 * @param target
	 *            Final result of distance transform.
	 * @param weights
	 *            Individual weights for each dimension, balancing image values
	 *            and L1 distance.
	 * @param <T>
	 *            {@link BooleanType} binary mask input
	 * @param <U>
	 *            {@link RealType} intermediate results
	 */
	private static < T extends RealType< T >, U extends RealType< U >, V extends RealType< V > > void transformL1(
			final RandomAccessible< T > source,
			final RandomAccessibleInterval< U > tmp,
			final RandomAccessibleInterval< V > target,
			final double... weights )
	{
		assert source.numDimensions() == target.numDimensions(): "Dimension mismatch";
		final int nDim = source.numDimensions();
		final int lastDim = nDim - 1;

		if ( nDim == 1 )
		{
			transformL1AlongDimension(
					( RandomAccessible< T > ) Views.addDimension( source ),
					Views.interval( Views.addDimension( target ), new FinalInterval( target.dimension( 0 ), 1 ) ),
					0,
					weights[ 0 ] );
		}
		else
		{
			transformL1AlongDimension( source, tmp, 0, weights[ 0 ] );
		}

		for ( int dim = 1; dim < nDim; ++dim )
		{
			if ( dim == lastDim )
			{
				transformL1AlongDimension( tmp, target, dim, weights[ dim ] );
			}
			else
			{
				transformL1AlongDimension( tmp, tmp, dim, weights[ dim ] );
			}
		}
	}

	/**
	 * Create
	 * <a href="http://www.theoryofcomputing.org/articles/v008a019/">distance
	 * transforms of sampled functions</a> on {@code source} using L1 distance.
	 * Intermediate results will be stored in {@code tmp} ({@link DoubleType}
	 * recommended). The output will be written into {@code target}. The
	 * distance can be weighted (individually for each dimension, if desired)
	 * against the image values via the weights parameter.
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
	 *             if interrupted while waiting, in which case unfinished tasks
	 *             are cancelled (distance transform may be computed only
	 *             partially)
	 * @throws ExecutionException
	 *             if the computation threw an exception (distance transform may
	 *             be computed only partially)
	 */
	private static < T extends RealType< T >, U extends RealType< U >, V extends RealType< V > > void transformL1(
			final RandomAccessible< T > source,
			final RandomAccessibleInterval< U > tmp,
			final RandomAccessibleInterval< V > target,
			final ExecutorService es,
			final int nTasks,
			final double... weights ) throws InterruptedException, ExecutionException
	{
		Parallelization.runWithExecutor( TaskExecutors.forExecutorServiceAndNumTasks( es, nTasks ),
				() -> transformL1( source, tmp, target, weights ) );
	}

	private static < T extends RealType< T >, U extends RealType< U > > void transformAlongDimension(
			final RandomAccessible< T > source,
			final RandomAccessibleInterval< U > target,
			final Distance d,
			final int dim )
	{
		final long size = target.dimension( dim );
		final RandomAccessibleInterval< RealComposite< T > > s = collapseDimensions( Views.interval( source, target ), dim );
		final RandomAccessibleInterval< RealComposite< U > > t = collapseDimensions( target, dim );

		LoopBuilder.setImages( s, t ).multiThreaded().forEachChunk( chunk -> {
			final RealComposite< DoubleType > tmp = Views.collapseReal( createAppropriateOneDimensionalImage( size, new DoubleType() ) ).randomAccess().get();
			final RealComposite< LongType > lowerBoundDistanceIndex = Views.collapseReal( createAppropriateOneDimensionalImage( size, new LongType() ) ).randomAccess().get();
			final RealComposite< DoubleType > envelopeIntersectLocation = Views.collapseReal( createAppropriateOneDimensionalImage( size + 1, new DoubleType() ) ).randomAccess().get();
			chunk.forEachPixel(
					( sourceComp, targetComp ) -> {
						for ( long i = 0; i < size; ++i )
						{
							tmp.get( i ).set( sourceComp.get( i ).getRealDouble() );
						}
						transformSingleColumn( tmp, targetComp, lowerBoundDistanceIndex, envelopeIntersectLocation, d, dim, size );
					}
			);
			return null;
		} );
	}

	private static < T extends RealType< T >, U extends RealType< U > > void transformSingleColumn(
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
		envelopeIntersectLocation.get( 0 ).set( Double.NEGATIVE_INFINITY );
		envelopeIntersectLocation.get( 1 ).set( Double.POSITIVE_INFINITY );
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
			envelopeIntersectLocation.get( k + 1 ).set( Double.POSITIVE_INFINITY );
		}

		k = 0;

		for ( long position = 0; position < size; ++position )
		{
			while ( envelopeIntersectLocation.get( k + 1 ).get() < position )
			{
				++k;
			}
			final long envelopeIndexAtK = lowerBoundDistanceIndex.get( k ).get();
			// copy necessary because of the following line, access to source
			// after write to source -> source and target cannot be the same
			target.get( position ).setReal( d.evaluate( position, envelopeIndexAtK, source.get( envelopeIndexAtK ).getRealDouble(), dim ) );
		}

	}

	private static < T extends RealType< T >, U extends RealType< U > > void transformL1AlongDimension(
			final RandomAccessible< T > source,
			final RandomAccessibleInterval< U > target,
			final int dim,
			final double weight )
	{
		final long size = target.dimension( dim );
		// do not permute if we already work on last dimension
		final RandomAccessibleInterval< RealComposite< T > > s = collapseDimensions( Views.interval( source, target ), dim );
		final RandomAccessibleInterval< RealComposite< U > > t = collapseDimensions( target, dim );

		LoopBuilder.setImages( s, t ).multiThreaded().forEachChunk( chunk -> {
			final RealComposite< DoubleType > tmp = Views.collapseReal( createAppropriateOneDimensionalImage( size, new DoubleType() ) ).randomAccess().get();
			chunk.forEachPixel( ( sourceComp, targetComp ) -> {
				for ( long i = 0; i < size; ++i )
				{
					tmp.get( i ).set( sourceComp.get( i ).getRealDouble() );
				}
				transformL1SingleColumn( tmp, targetComp, weight, size );
			} );
			return null;
		} );
	}

	private static < U extends RealType< U > > RandomAccessibleInterval< RealComposite< U > > collapseDimensions( RandomAccessibleInterval< U > target, int dim )
	{
		final int lastDim = target.numDimensions() - 1;
		return Views.collapseReal( dim == lastDim ? target : Views.permute( target, dim, lastDim ) );
	}

	private static < T extends RealType< T >, U extends RealType< U > > void transformL1SingleColumn(
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
	 * Convenience method for creating an appropriate storage img:
	 * {@link ArrayImg} if size is less than {@link Integer#MAX_VALUE},
	 * {@link CellImg} otherwise.
	 */
	private static < T extends NativeType< T > & RealType< T > > Img< T > createAppropriateOneDimensionalImage( final long size, final T t )
	{
		final long[] dim = new long[] { 1, size };
		return size > Integer.MAX_VALUE ? new CellImgFactory<>( t, Integer.MAX_VALUE ).create( dim ) : new ArrayImgFactory<>( t ).create( dim );
	}

	/**
	 * Convenience method to find largest dimension of {@link Interval}
	 * interval.
	 *
	 * @param interval
	 *            input
	 * @return Return the largest dimension of {@code interval} (not the size of
	 *         that dimension).
	 */
	public static int getLargestDimension( final Interval interval )
	{
		return IntStream.range( 0, interval.numDimensions() ).mapToObj( i -> new ValuePair<>( i, interval.dimension( i ) ) ).max( ( p1, p2 ) -> Long.compare( p1.getB(), p2.getB() ) ).get().getA();
	}

	private static class BinaryMaskToCost< B extends BooleanType< B >, R extends RealType< R > > implements Converter< B, R >
	{

		private final R maxValForR;

		private final R zero;

		public BinaryMaskToCost( final R maxValForR )
		{
			this.maxValForR = maxValForR;
			this.zero = maxValForR.createVariable();
			zero.setZero();
		}

		@Override
		public void convert( final B input, final R output )
		{
			output.set( input.get() ? zero : maxValForR );
		}

	}

}
