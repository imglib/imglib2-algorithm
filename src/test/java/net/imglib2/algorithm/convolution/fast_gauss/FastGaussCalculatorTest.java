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
package net.imglib2.algorithm.convolution.fast_gauss;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests {@link FastGaussCalculator}.
 */
public class FastGaussCalculatorTest
{
	private final double sigma = 10.0;

	private final FastGaussCalculator.Parameters parameters = FastGaussCalculator.Parameters.exact( sigma );

	private final FastGaussCalculator calculator = new FastGaussCalculator( parameters );

	@Test
	public void testInitialize() {
		double boundaryValue = 1.0;
		calculator.initialize( boundaryValue );
		assertEquals( boundaryValue, calculator.getValue(), 1e-10 );
		for ( int i = 0; i < parameters.N; i++ )
		{
			calculator.update( boundaryValue + boundaryValue );
			assertEquals( boundaryValue, calculator.getValue(), 1e-10 );
		}
	}

	@Test
	public void testOverall() {
		calculator.initialize( 0.0 );
		for ( int x = - 2 * parameters.N; x < 2 * parameters.N; x++ )
		{
			double expected = gauss( sigma, x );
			double actual = calculator.getValue();
			assertEquals( expected, actual, 1e-4 );
			calculator.update( input( x - parameters.N ) + input( x + parameters.N) );
		}
	}

	private double input( long x )
	{
		return ( x == 0 ) ? 1.0 : 0.0;
	}

	private double gauss( double sigma, long x )
	{
		double a = 1. / Math.sqrt( 2 * Math.PI * Math.pow( sigma, 2 ) );
		double b = -0.5 / Math.pow( sigma, 2 );
		return a * Math.exp( b * Math.pow( x, 2 ) );
	}
}
