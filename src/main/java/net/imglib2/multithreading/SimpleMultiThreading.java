/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2025 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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

package net.imglib2.multithreading;

import java.util.Vector;

/**
 * TODO
 * 
 * @author Stephan Preibisch
 */
@Deprecated
public class SimpleMultiThreading
{
	/*
	 * final int start = 0; final int end = 10;
	 * 
	 * final AtomicInteger ai = new AtomicInteger(start);
	 * 
	 * Thread[] threads = newThreads(); for (int ithread = 0; ithread <
	 * threads.length; ++ithread) { threads[ithread] = new Thread(new Runnable()
	 * { public void run() { // do something.... // for example: for (int i3 =
	 * ai.getAndIncrement(); i3 < end; i3 = ai.getAndIncrement()) { } } }); }
	 * startAndJoin(threads);
	 */

	public static Vector< Chunk > divideIntoChunks( final long imageSize, final int numThreads )
	{
		final long threadChunkSize = imageSize / numThreads;
		final long threadChunkMod = imageSize % numThreads;

		final Vector< Chunk > chunks = new Vector< Chunk >();

		for ( int threadID = 0; threadID < numThreads; ++threadID )
		{
			// move to the starting position of the current thread
			final long startPosition = threadID * threadChunkSize;

			// the last thread may has to run longer if the number of pixels
			// cannot be divided by the number of threads
			final long loopSize;
			if ( threadID == numThreads - 1 )
				loopSize = threadChunkSize + threadChunkMod;
			else
				loopSize = threadChunkSize;

			chunks.add( new Chunk( startPosition, loopSize ) );
		}

		return chunks;
	}

	public static void startTask( final Runnable run )
	{
		final Thread[] threads = newThreads();

		for ( int ithread = 0; ithread < threads.length; ++ithread )
			threads[ ithread ] = new Thread( run );

		startAndJoin( threads );
	}

	public static void startTask( final Runnable run, final int numThreads )
	{
		if ( 1 == numThreads )
		{
			run.run();
			return;
		}

		final Thread[] threads = newThreads( numThreads );

		for ( int ithread = 0; ithread < threads.length; ++ithread )
			threads[ ithread ] = new Thread( run );

		startAndJoin( threads );
	}

	public static Thread[] newThreads()
	{
		final int nthread = Runtime.getRuntime().availableProcessors();
		return new Thread[ nthread ];
	}

	public static Thread[] newThreads( final int numThreads )
	{
		return new Thread[ numThreads ];
	}

	public static void startAndJoin( final Thread[] threads )
	{
		if ( 1 == threads.length )
		{
			threads[ 0 ].run();
			return;
		}

		for ( int ithread = 0; ithread < threads.length; ++ithread )
		{
			threads[ ithread ].setPriority( Thread.NORM_PRIORITY );
			threads[ ithread ].start();
		}

		try
		{
			for ( int ithread = 0; ithread < threads.length; ++ithread )
				threads[ ithread ].join();
		}
		catch ( final InterruptedException ie )
		{
			throw new RuntimeException( ie );
		}
	}

	public static void start( final Thread[] threads )
	{
		for ( int ithread = 0; ithread < threads.length; ++ithread )
		{
			threads[ ithread ].setPriority( Thread.MIN_PRIORITY );
			threads[ ithread ].start();
		}
	}

	public static void threadHaltUnClean()
	{
		final int i = 0;

		while ( i == 0 )
		{}
	}

	public static void threadWait( final long milliseconds )
	{
		try
		{
			Thread.sleep( milliseconds );
		}
		catch ( final InterruptedException e )
		{
			System.err.println( "MultiThreading.threadWait(): Thread woken up: " + e );
		}
	}
}
