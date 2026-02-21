/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2026 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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
package net.imglib2.algorithm.blocks.dfield;

import net.imglib2.algorithm.blocks.transform.Transform.Interpolation;
import net.imglib2.type.PrimitiveType;

class Lookup3DProcessor< F, P > extends AbstractLookupProcessor< F, P >
{
	private final Lookup3D< F, P > lookup;

	Lookup3DProcessor(
			final PrimitiveType dfieldPrimitiveType,
			final Interpolation interpolation,
			final PrimitiveType primitiveType )
	{
		super( primitiveType, 3 );
		lookup = Lookup3D.of( dfieldPrimitiveType, interpolation, primitiveType );
	}

	private Lookup3DProcessor( Lookup3DProcessor< F, P > processor )
	{
		super( processor );
		lookup = processor.lookup;
	}

	@Override
	public Lookup3DProcessor< F, P > independentCopy()
	{
		return new Lookup3DProcessor<>( this );
	}

	@Override
	public void compute( final P src, final P dest )
	{
		final double d0 = positionOffset[ 0 ];
		final double d1 = positionOffset[ 1 ];
		final double d2 = positionOffset[ 2 ];
		final int length = destSize[ 0 ] * destSize[ 1 ] * destSize[ 2 ];
		final int ss0 = sourceSize[ 0 ];
		final int ss1 = sourceSize[ 1 ] * ss0;
		lookup.apply( positionField, d0, d1, d2, src, dest, length, ss0, ss1 );
	}
}
