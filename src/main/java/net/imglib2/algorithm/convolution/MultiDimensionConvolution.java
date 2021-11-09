/*-
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
