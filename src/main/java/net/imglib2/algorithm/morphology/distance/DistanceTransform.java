package net.imglib2.algorithm.morphology.distance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import net.imglib2.Cursor;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Pair;
import net.imglib2.view.Views;
import net.imglib2.view.composite.RealComposite;

/**
 *
 * ImgLib2 implementation of distance transform D of sampled functions f with
 * distance measure d: http://www.theoryofcomputing.org/articles/v008a019/ DOI:
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

	public static < T extends RealType< T > > void transform(
			final RandomAccessibleInterval< T > source,
			final DISTANCE_TYPE distanceType,
			final ExecutorService es,
			final int nTasks,
			final double... weights ) throws InterruptedException, ExecutionException
	{
		transform( source, source, distanceType, es, nTasks, weights );
	}

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
	 *
	 * Write distance transform of source into target. This method will create
	 * an {@link ExecutorService} with nThreads through
	 * {@link Executors#newFixedThreadPool(int)}.
	 *
	 *
	 * @param source
	 *            {@link RandomAccessibleInterval} on which distance transform
	 *            should be computed.
	 * @param target
	 *            Distance transform will be stored in this
	 *            {@link RandomAccessibleInterval}.
	 * @param distanceType
	 *            Distance measure measure to be used, euclidian or L1
	 * @param weights
	 *
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

	public static < T extends RealType< T >, U extends RealType< U >, V extends RealType< V > > void transform(
			final RandomAccessible< T > source,
			final RandomAccessibleInterval< U > tmp,
			final RandomAccessibleInterval< V > target,
			final DISTANCE_TYPE distanceType,
			final ExecutorService es,
			final int nTasks,
			final double... weights ) throws InterruptedException, ExecutionException
	{

		switch ( distanceType )
		{
		case EUCLIDIAN:
			transform( source, tmp, target, new EuclidianDistanceIsotropic( weights.length > 0 ? weights[ 0 ] : 1.0 ), es, nTasks );
			break;
		case EUCLIDIAN_ANISOTROPIC:
			transform( source, tmp, target, new EuclidianDistanceAnisotropic( weights.length > 0 ? weights : new double[] { 1.0 } ), es, nTasks );
			break;
		case L1:
			transformL1( source, tmp, target, es, nTasks, weights.length > 0 ? new double[] { weights[ 0 ] } : new double[] { 1.0 } );
			break;
		case L1_ANISOTROPIC:
			transformL1( source, tmp, target, es, nTasks, weights.length > 0 ? weights : new double[] { 1.0 } );
		default:
			break;
		}
	}

	public static < T extends RealType< T > > void transform(
			final RandomAccessibleInterval< T > source,
			final Distance d,
			final int nThreads ) throws InterruptedException, ExecutionException
	{
		final ExecutorService es = Executors.newFixedThreadPool( nThreads );
		transform( source, d, es, nThreads );
		es.shutdown();
	}

	public static < T extends RealType< T > > void transform(
			final RandomAccessibleInterval< T > source,
			final Distance d,
			final ExecutorService es,
			final int nTasks ) throws InterruptedException, ExecutionException
	{
		transform( source, source, d, es, nTasks );
	}

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

	public static < T extends RealType< T >, U extends RealType< U > > void transform(
			final RandomAccessible< T > source,
			final RandomAccessibleInterval< U > target,
			final Distance d,
			final ExecutorService es,
			final int nTasks ) throws InterruptedException, ExecutionException
	{
		transform( source, target, target, d, es, nTasks );
	}

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

		transformDimension( source, tmp, d, 0, es, nTasks );

		for ( int dim = 1; dim < nDim; ++dim )
			transformDimension( tmp, tmp, d, dim, es, nTasks );

		if ( tmp != target )
			for ( final Pair< U, V > p : Views.interval( Views.pair( tmp, target ), tmp ) )
				p.getB().setReal( p.getA().getRealDouble() );

//		final ExecutorService es = Executors.newFixedThreadPool( nTasks );

		// transform along first dimension
//		{
//			final ArrayList< Callable< Void > > tasks = new ArrayList<>();
//			final long size = target.dimension( 0 );
//			final long nComposites = Views.flatIterable( Views.collapseReal( Views.permute( source, 0, lastDim ) ) ).size();
//			final long nCompositesPerChunk = nComposites / nTasks;
//			for ( long lower = 0; lower < nComposites; lower += nCompositesPerChunk )
//			{
//				final long fLower = lower;
//				tasks.add( () -> {
//					final Cursor< RealComposite< T > > s = Views.flatIterable( Views.collapseReal( Views.permute( source, 0, lastDim ) ) ).cursor();
//					final Cursor< RealComposite< U > > t = Views.flatIterable( Views.collapseReal( Views.permute( target, 0, lastDim ) ) ).cursor();
//					final RealComposite< LongType > lowerBoundDistanceIndex = Views.collapseReal( ArrayImgs.longs( 1, size ) ).randomAccess().get();
//					final RealComposite< DoubleType > envelopeIntersectLocation = Views.collapseReal( ArrayImgs.doubles( 1, size + 1 ) ).randomAccess().get();
//					s.jumpFwd( fLower );
//					t.jumpFwd( fLower );
//					for ( long count = 0; count < nCompositesPerChunk && s.hasNext(); ++count )
//						transform1D( s.next(), t.next(), lowerBoundDistanceIndex, envelopeIntersectLocation, d, 0, size );
//					return null;
//				} );
//
//			}
//
//			invokeAllAndWait( es, tasks );
//		}

		// transform along subsequent dimensions
//		for ( int dim = 1; dim < nDim; ++dim )
//		{
//
//			// would like to avoid copy to tmp but seems unavoidable
//			final long size = target.dimension( dim );
//			final long nComposites = Views.flatIterable( Views.collapseReal( Views.permute( target, dim, lastDim ) ) ).size();
//			final long nCompositesPerChunk = nComposites / nTasks;
//			final int fDim = dim;
//
//			final ArrayList< Callable< Void > > tasks = new ArrayList<>();
//			for ( long lower = 0; lower < nComposites; lower += nCompositesPerChunk )
//			{
//
//				final long fLower = lower;
//				tasks.add( () -> {
//					final RealComposite< DoubleType > tmp = Views.collapseReal( ArrayImgs.doubles( 1, size ) ).randomAccess().get();
//					final Cursor< RealComposite< V > > v = Views.flatIterable( Views.collapseReal( Views.permute( target, fDim, lastDim ) ) ).cursor();
//					final RealComposite< LongType > lowerBoundDistanceIndex = Views.collapseReal( ArrayImgs.longs( 1, size ) ).randomAccess().get();
//					final RealComposite< DoubleType > envelopeIntersectLocation = Views.collapseReal( ArrayImgs.doubles( 1, size + 1 ) ).randomAccess().get();
//					v.jumpFwd( fLower );
//
//					for ( long count = 0; count < nCompositesPerChunk && v.hasNext(); ++count )
//					{
//						final RealComposite< V > composite = v.next();
//						for ( long i = 0; i < size; ++i )
//							tmp.get( i ).set( composite.get( i ).getRealDouble() );
//
//						transform1D( tmp, composite, lowerBoundDistanceIndex, envelopeIntersectLocation, d, fDim, size );
//
//					}
//					return null;
//				} );
//			}
//			invokeAllAndWait( es, tasks );
//		}

//		es.shutdown();

	}

	public static < T extends RealType< T > > void transformL1(
			final RandomAccessibleInterval< T > source,
			final int nThreads,
			final double... weights ) throws InterruptedException, ExecutionException
	{
		final ExecutorService es = Executors.newFixedThreadPool( nThreads );
		transformL1( source, source, es, nThreads, weights );
		es.shutdown();
	}

	public static < T extends RealType< T > > void transformL1(
			final RandomAccessibleInterval< T > source,
			final ExecutorService es,
			final int nTasks,
			final double... weights ) throws InterruptedException, ExecutionException
	{
		transformL1( source, source, es, nTasks, weights );
	}

	public static < T extends RealType< T >, U extends RealType< U > > void transformL1(
			final RandomAccessible< T > source,
			final RandomAccessibleInterval< U > target,
			final int nThreads,
			final double... weights ) throws InterruptedException, ExecutionException
	{
		final ExecutorService es = Executors.newFixedThreadPool( nThreads );
		transformL1( source, target, target, es, nThreads, weights );
		es.shutdown();
	}

	public static < T extends RealType< T >, U extends RealType< U > > void transformL1(
			final RandomAccessible< T > source,
			final RandomAccessibleInterval< U > target,
			final ExecutorService es,
			final int nTasks,
			final double... weights ) throws InterruptedException, ExecutionException
	{
		transformL1( source, target, target, es, nTasks, weights );
	}

	public static < T extends RealType< T >, U extends RealType< U >, V extends RealType< V > > void transformL1(
			final RandomAccessible< T > source,
			final RandomAccessibleInterval< U > tmp,
			final RandomAccessibleInterval< V > target,
			final int nThreads,
			final double... weights ) throws InterruptedException, ExecutionException
	{
		final ExecutorService es = Executors.newFixedThreadPool( nThreads );
		transformL1( source, tmp, target, es, nThreads, weights );
		es.shutdown();
	}

	public static < T extends RealType< T >, U extends RealType< U >, V extends RealType< V > > void transformL1(
			final RandomAccessible< T > source,
			final RandomAccessibleInterval< U > tmp,
			final RandomAccessibleInterval< V > target,
			final ExecutorService es,
			final int nTasks,
			final double... weights ) throws InterruptedException, ExecutionException
	{

		assert source.numDimensions() == target.numDimensions(): "Dimension mismatch";
		final int nDim = source.numDimensions();
		final int lastDim = nDim - 1;

		transformL1Dimension( source, tmp, 0, weights[ 0 ], es, nTasks );

		for ( int dim = 0; dim < nDim; ++dim )
		{
			transformL1Dimension( tmp, tmp, lastDim, weights.length > 1 ? weights[ dim ] : weights[ 0 ], es, nTasks );
		}

//		// transform along first dimension
//		{
//			final ArrayList< Callable< Void > > tasks = new ArrayList<>();
//			final long size = target.dimension( 0 );
//			final long nComposites = Views.flatIterable( Views.collapseReal( Views.permute( source, 0, lastDim ) ) ).size();
//			final long nCompositesPerChunk = nComposites / nTasks;
//			for ( long lower = 0; lower < nComposites; lower += nCompositesPerChunk )
//			{
//				final long fLower = lower;
//				tasks.add( () -> {
//					final Cursor< RealComposite< T > > s = Views.flatIterable( Views.collapseReal( Views.permute( source, 0, lastDim ) ) ).cursor();
//					final Cursor< RealComposite< U > > t = Views.flatIterable( Views.collapseReal( Views.permute( target, 0, lastDim ) ) ).cursor();
//					s.jumpFwd( fLower );
//					t.jumpFwd( fLower );
//					for ( long count = 0; count < nCompositesPerChunk && s.hasNext(); ++count )
//						transformL1_1D( s.next(), t.next(), weights[ 0 ], size );
//					return null;
//				} );
//			}
//			invokeAllAndWait( es, tasks );
//		}
//
//		// transform along subsequent dimensions
//		for ( int dim = 1; dim < nDim; ++dim )
//		{
//			final int fDim = dim;
//
//			// would like to avoid copy to tmp but seems unavoidable
//			final long size = target.dimension( dim );
//			final ArrayList< Callable< Void > > tasks = new ArrayList<>();
//			final long nComposites = Views.flatIterable( Views.collapseReal( Views.permute( source, fDim, lastDim ) ) ).size();
//			final long nCompositesPerChunk = nComposites / nTasks;
//
//			for ( long lower = 0; lower < nComposites; lower += nCompositesPerChunk )
//			{
//				final long fLower = lower;
//				tasks.add( () -> {
//					final RealComposite< DoubleType > tmp = Views.collapseReal( ArrayImgs.doubles( 1, size ) ).randomAccess().get();
//					final Cursor< RealComposite< U > > t = Views.flatIterable( Views.collapseReal( Views.permute( target, fDim, lastDim ) ) ).cursor();
//					t.jumpFwd( fLower );
//					for ( long count = 0; count < nCompositesPerChunk && t.hasNext(); ++count )
//					{
//						final RealComposite< U > composite = t.next();
//						for ( long i = 0; i < size; ++i )
//							tmp.get( i ).set( composite.get( i ).getRealDouble() );
//
//						transformL1_1D( tmp, composite, weights.length > 1 ? weights[ fDim ] : weights[ 0 ], size );
//
//					}
//					return null;
//				} );
//			}
//			invokeAllAndWait( es, tasks );
//		}

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
				final RealComposite< DoubleType > tmp = Views.collapseReal( ArrayImgs.doubles( 1, size ) ).randomAccess().get();
				final Cursor< RealComposite< T > > s = Views.flatIterable( Views.collapseReal( Views.permute( Views.interval( source, target ), dim, lastDim ) ) ).cursor();
				final Cursor< RealComposite< U > > t = Views.flatIterable( Views.collapseReal( Views.permute( target, dim, lastDim ) ) ).cursor();
				final RealComposite< LongType > lowerBoundDistanceIndex = Views.collapseReal( ArrayImgs.longs( 1, size ) ).randomAccess().get();
				final RealComposite< DoubleType > envelopeIntersectLocation = Views.collapseReal( ArrayImgs.doubles( 1, size + 1 ) ).randomAccess().get();
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

	// source and target may not be the same?
	public static < T extends RealType< T >, U extends RealType< U > > void transform1D(
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
				final RealComposite< DoubleType > tmp = Views.collapseReal( ArrayImgs.doubles( 1, size ) ).randomAccess().get();
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

	public static < T extends RealType< T >, U extends RealType< U > > void transformL1_1D(
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

	public static < T > void invokeAllAndWait( final ExecutorService es, final Collection< Callable< T > > tasks ) throws InterruptedException, ExecutionException
	{
		final List< Future< T > > futures = es.invokeAll( tasks );
		for ( final Future< T > f : futures )
			f.get();
	}

}
