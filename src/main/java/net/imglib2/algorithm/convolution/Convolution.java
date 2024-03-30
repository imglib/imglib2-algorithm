/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2024 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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
