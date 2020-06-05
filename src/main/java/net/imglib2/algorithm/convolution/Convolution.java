package net.imglib2.algorithm.convolution;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;

/**
 * This interface allows the client to perform a convolution. But also to query
 * for the required input image size and preferred input image type. The
 * ExectorService can be set, to allow multi or single threaded operation.
 * <p>
 * Very importantly, multiple {@link Convolution}s can be easily concatenated.
 *
 * @author Matthias Arzt
 */
public interface Convolution< T >
{
	/**
	 * Returns the required size for source image, to calculate the given target
	 * interval.
	 */
	Interval requiredSourceInterval( Interval targetInterval );

	/**
	 * What's the preferred type for the source image, when target should have
	 * the specified type?
	 */
	T preferredSourceType( T targetType );

	/**
	 * Set the {@link ExecutorService} to be used for convolution.
	 */
	@Deprecated
	default void setExecutor( final ExecutorService executor )
	{}

	/**
	 * Fills the target image, with the convolution result.
	 *
	 * @param source
	 *            Source image. It must allow pixel access in the interval
	 *            returned by {@code requiredSourceInterval(target)}
	 * @param target
	 *            Target image.
	 */
	void process( RandomAccessible< ? extends T > source, RandomAccessibleInterval< ? extends T > target );

	/**
	 * Concatenate multiple {@link Convolution}s to one convolution. (e.g.
	 * Concatenation of a gauss convolution in X, and a gauss convolution in Y
	 * will be a 2d gauss convolution).
	 */
	static < T > Convolution< T > concat( final Convolution< T >... steps )
	{
		return concat( Arrays.asList( steps ) );
	}

	static < T > Convolution< T > concat( final List< ? extends Convolution< T > > steps )
	{
		if ( steps.isEmpty() )
			throw new IllegalArgumentException( "Concat requires at least one convolution operation." );
		if ( steps.size() == 1 )
			return steps.get( 0 );
		return new Concatenation<>( steps );
	}
}
