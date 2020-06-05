package net.imglib2.algorithm.convolution;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.function.IntFunction;

import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;

/**
 * It's useful if there are multi {@link Convolution}s, each of which only works
 * for a particular dimensionality. (e.g. gauss 1d, gauss 2d, gauss 3d, ...)
 * {@link MultiDimensionConvolution} can be used to bundle them into one
 * {@link Convolution} (e.g. gauss Nd).
 *
 * @author Matthias Arzt
 */
public class MultiDimensionConvolution< T > implements Convolution< T >
{
	private ExecutorService executor;

	@Deprecated
	@Override
	public void setExecutor( final ExecutorService executor )
	{
		this.executor = executor;
		cache.values().forEach( convolution -> convolution.setExecutor( executor ) );
	}

	private final IntFunction< Convolution< T > > factory;

	private final HashMap< Integer, Convolution< T > > cache = new HashMap<>();

	/**
	 * Constructor
	 *
	 * @param numDimensionToConvolution
	 *            Function, when applied to a certain number (e.g. 2), it must
	 *            return a {@link Convolution} that can be applied to images of
	 *            that dimensions (e.g gauss 2d).
	 */
	public MultiDimensionConvolution( final IntFunction< Convolution< T > > numDimensionToConvolution )
	{
		this.factory = numDimensionToConvolution;
	}

	private Convolution< T > getCachedConvolution( final int nDimensions )
	{
		return cache.computeIfAbsent( nDimensions, n -> {
			final Convolution< T > c = factory.apply( n );
			c.setExecutor( executor );
			return c;
		} );
	}

	/**
	 * @see Convolution#requiredSourceInterval(Interval)
	 */
	@Override
	public Interval requiredSourceInterval( final Interval targetInterval )
	{
		return getCachedConvolution( targetInterval.numDimensions() ).requiredSourceInterval( targetInterval );
	}

	/**
	 * @see Convolution#preferredSourceType(Object)
	 */
	@Override
	public T preferredSourceType( final T targetType )
	{
		return getCachedConvolution( 2 ).preferredSourceType( targetType );
	}

	/**
	 * @see Convolution#process(RandomAccessible, RandomAccessibleInterval)
	 */
	@Override
	public void process( final RandomAccessible< ? extends T > source, final RandomAccessibleInterval< ? extends T > target )
	{
		getCachedConvolution( target.numDimensions() ).process( source, target );
	}
}
