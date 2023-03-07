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
 * <pre>{@code
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
 * }</pre>
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
