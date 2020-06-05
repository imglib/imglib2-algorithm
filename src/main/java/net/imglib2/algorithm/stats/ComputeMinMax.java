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

package net.imglib2.algorithm.stats;

import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.Algorithm;
import net.imglib2.algorithm.Benchmark;
import net.imglib2.algorithm.MultiThreaded;
import net.imglib2.algorithm.loop.IterableLoopBuilder;
import net.imglib2.parallel.Parallelization;
import net.imglib2.type.Type;
import net.imglib2.util.Util;
import net.imglib2.util.ValuePair;
import net.imglib2.view.Views;

import java.util.List;

/**
 * TODO
 * 
 * @author Stephan Preibisch
 */
public class ComputeMinMax< T extends Type< T > & Comparable< T >> implements Algorithm, MultiThreaded, Benchmark
{
	/**
	 * Computes minimal and maximal value in a given interval
	 * 
	 * @param interval
	 * @param min
	 * @param max
	 */
	public static < T extends Comparable< T > & Type< T > > void computeMinMax( final RandomAccessibleInterval< T > interval, final T min, final T max )
	{
		final ComputeMinMax< T > c = new ComputeMinMax< T >( Views.iterable( interval ), min, max );
		c.process();

		min.set( c.getMin() );
		max.set( c.getMax() );
	}

	private final IterableInterval< T > image;

	private final T min, max;

	private String errorMessage = "";

	private int numThreads;

	private long processingTime;

	public ComputeMinMax( final IterableInterval< T > interval, final T min, final T max )
	{
		this.image = interval;

		this.min = min;
		this.max = max;
	}

	public T getMin()
	{
		return min;
	}

	public T getMax()
	{
		return max;
	}

	@Override
	public boolean process()
	{
		final long startTime = System.currentTimeMillis();

		if ( numThreads == 0 )
			computeMinAndMax();
		else
			Parallelization.runWithNumThreads( numThreads, () -> computeMinAndMax() );

		processingTime = System.currentTimeMillis() - startTime;

		return true;
	}

	private void computeMinAndMax()
	{
		List< ValuePair< T, T > > minAndMaxs = IterableLoopBuilder
				.setImages( image )
				.multithreaded()
				.forEachChunk(
						chunk -> {
							T minValue = image.firstElement().createVariable();
							T maxValue = image.firstElement().createVariable();
							chunk.forEachPixel( value -> {
								if ( Util.min( min, value ) == value )
									min.set( value );

								if ( Util.max( max, value ) == value )
									max.set( value );
							} );
							return new ValuePair<>( minValue, maxValue );
						}
				);

		// compute overall min and max
		computeOverAllMinAndMax( minAndMaxs );
	}

	public void computeOverAllMinAndMax( List< ValuePair< T, T > > minAndMaxs )
	{
		min.set( minAndMaxs.get( 0 ).getA() );
		max.set( minAndMaxs.get( 0 ).getB() );

		for ( ValuePair< T, T > minAndMax : minAndMaxs )
		{
			min.set( Util.min( min, minAndMax.getA() ) );
			max.set( Util.max( max, minAndMax.getB() ) );
		}
	}

	@Override
	public boolean checkInput()
	{
		if ( errorMessage.length() > 0 )
		{
			return false;
		}
		else if ( image == null )
		{
			errorMessage = "ScaleSpace: [Image<A> img] is null.";
			return false;
		}
		else
			return true;
	}

	@Override
	public long getProcessingTime()
	{
		return processingTime;
	}

	@Override
	public void setNumThreads()
	{
		this.numThreads = 0;
	}

	@Override
	public void setNumThreads( final int numThreads )
	{
		this.numThreads = numThreads;
	}

	@Override
	public int getNumThreads()
	{
		return numThreads == 0 ? Parallelization.getTaskExecutor().getParallelism() : numThreads;
	}

	@Override
	public String getErrorMessage()
	{
		return errorMessage;
	}
}
