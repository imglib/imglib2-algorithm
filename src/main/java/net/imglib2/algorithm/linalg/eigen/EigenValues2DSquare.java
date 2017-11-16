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

package net.imglib2.algorithm.linalg.eigen;

import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.composite.Composite;

public class EigenValues2DSquare< T extends RealType< T >, U extends ComplexType< U > > implements EigenValues< T, U >
{
	@Override
	public void compute( final Composite< T > tensor, final Composite< U > evs )
	{
		final double x11 = tensor.get( 0 ).getRealDouble();
		final double x12 = tensor.get( 1 ).getRealDouble();
		final double x21 = tensor.get( 2 ).getRealDouble();
		final double x22 = tensor.get( 3 ).getRealDouble();
		final double sum = x11 + x22;
		final double diff = x11 - x22;
		final double radicand = 4 * x12 * x21 + diff * diff;
		if ( radicand < 0.0d )
		{
			final double halfSqrt = 0.5 * Math.sqrt( -radicand );
			final double halfSum = 0.5 * sum;
			evs.get( 0 ).setComplexNumber( halfSum, halfSqrt );
			evs.get( 1 ).setComplexNumber( halfSum, -halfSqrt );
		}
		else
		{
			final double sqrt = Math.sqrt( radicand );
			evs.get( 0 ).setComplexNumber( 0.5 * ( sum + sqrt ), 0.0d );
			evs.get( 1 ).setComplexNumber( 0.5 * ( sum - sqrt ), 0.0d );
		}
	}
}