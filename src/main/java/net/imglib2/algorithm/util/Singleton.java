/**
 *
 */
package net.imglib2.algorithm.util;

import java.util.HashMap;

/**
 * Manage named singleton instances.
 *
 * Useful for sharing instances of objects between threads.
 *
 * Our most relevant use case for this is to share cached ImgLib2 cell images
 * such as N5 datasets and lazily generated cell images between tasks of a Spark
 * cluster running on the same executor.
 *
 * Example:
 *
 * <pre>
 * final String url = "https://janelia-cosem.s3.amazonaws.com/jrc_hela-2/jrc_hela-2.n5";
 * final String dataset = "/em/fibsem-uint16/s4";
 *
 * final N5Reader n5 = Singleton.get(
 * 		url,
 * 		() -> new N5Factory().openReader( url ) );
 *
 * final RandomAccessibleInterval< T > img = Singleton.get(
 * 		url + ":" + dataset,
 * 		() -> N5Utils.open( n5, dataset ) );
 * </pre>
 *
 * @author Stephan Saalfeld
 *
 */
public class Singleton
{
	@FunctionalInterface
	public static interface ThrowingSupplier< T, E extends Exception >
	{
		public T get() throws E;
	}

	private Singleton()
	{}

	static private HashMap< String, Object > singletons = new HashMap<>();

	/**
	 * Remove and retrieve a named singleton instance.
	 *
	 * @param key
	 * @return
	 */
	public static synchronized Object remove( final String key )
	{
		return singletons.remove( key );
	}

	/**
	 * Get or create the named singleton instance of T.
	 *
	 * @param <T>
	 * @param <E>
	 * @param key
	 * @param supplier
	 * @return
	 * @throws E
	 */
	public static synchronized < T, E extends Exception > T get( final String key, final ThrowingSupplier< T, E > supplier ) throws E
	{
		@SuppressWarnings( "unchecked" )
		final T t = ( T ) singletons.get( key );
		if ( t == null )
		{
			final T s = supplier.get();
			singletons.put( key, s );
			return s;
		}
		else
			return t;
	}

	/**
	 * Clear all named singletons.
	 */
	public static synchronized void clear()
	{
		singletons.clear();
	}
}
