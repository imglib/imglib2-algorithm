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

/**
 *
 * Interface for handling different cases, e.g. square, symmetric, or 2
 * dimensional tensors.
 *
 */
public interface EigenValues< T extends RealType< T >, U extends ComplexType< U > >
{
	public void compute( final Composite< T > matrix, final Composite< U > evs );

	public default EigenValues< T, U > copy()
	{
		return this;
	}

	public static < T extends RealType< T >, U extends ComplexType< U > > EigenValues1D< T, U > oneDimensional()
	{
		return new EigenValues1D<>();
	}

	public static < T extends RealType< T >, U extends ComplexType< U > > EigenValues2DSquare< T, U > square2D()
	{
		return new EigenValues2DSquare<>();
	}

	public static < T extends RealType< T >, U extends ComplexType< U > > EigenValues2DSymmetric< T, U > symmetric2D()
	{
		return new EigenValues2DSymmetric<>();
	}

	public static < T extends RealType< T >, U extends ComplexType< U > > EigenValuesSquare< T, U > square( final int nDim )
	{
		return new EigenValuesSquare<>( nDim );
	}

	public static < T extends RealType< T >, U extends ComplexType< U > > EigenValuesSymmetric< T, U > symmetric( final int nDim )
	{
		return new EigenValuesSymmetric<>( nDim );
	}

	public static < T extends RealType< T >, U extends ComplexType< U > > EigenValues< T, U > invalid()
	{
		return ( m, evs ) -> {
			throw new UnsupportedOperationException( "EigenValues not implemented yet!" );
		};
	}

}