package net.imglib2.algorithm.convolution;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Tests {@link AbstractMultiThreadedConvolution}
 *
 * @author Matthias Arzt
 */
public class AbstractMultiThreadedConvolutionTest
{
	@Test
	public void testSuggestNumTasksFixedThreadPool() {
		if ( Runtime.getRuntime().availableProcessors() < 3 )
			return;
		final ExecutorService executor = Executors.newFixedThreadPool( 3 );
		int result = AbstractMultiThreadedConvolution.getNumThreads( executor );
		assertEquals( 3, result );
	}

	@Test
	public void testSuggestNumTasksCachedThreadPool() {
		final ExecutorService executor = Executors.newCachedThreadPool();
		int result = AbstractMultiThreadedConvolution.getNumThreads( executor );
		assertTrue( Runtime.getRuntime().availableProcessors() >= result );
	}
}
