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
package net.imglib2.algorithm.blocks;

import net.imglib2.Interval;
import net.imglib2.Volatile;
import net.imglib2.blocks.VolatileArray;
import net.imglib2.blocks.VolatilePrimitiveBlocks.AllValid;
import net.imglib2.type.NativeType;

class VolatileConcatenatedUnaryBlockOperator<
		S extends Volatile< ? > & NativeType< S >,
		T extends Volatile< ? > & NativeType< T >,
		U extends Volatile< ? > & NativeType< U > >
		implements VolatileUnaryBlockOperator< S, U >
{
	private final VolatileUnaryBlockOperator< S, T > op1;
	private final VolatileUnaryBlockOperator< T, U > op2;
	private final int numSourceDimensions;
	private final int numTargetDimensions;

	VolatileConcatenatedUnaryBlockOperator( VolatileUnaryBlockOperator< S, T > op1, VolatileUnaryBlockOperator< T, U > op2 )
	{
		this.op1 = op1;
		this.op2 = op2;

		final boolean op1HasDims = op1.numSourceDimensions() > 0;
		final boolean op2HasDims = op2.numSourceDimensions() > 0;
		if ( op2HasDims && op1HasDims && op1.numTargetDimensions() != op2.numSourceDimensions() ) {
			throw new IllegalArgumentException( "VolatileUnaryBlockOperator cannot be concatenated: number of dimensions mismatch." );
		}
		this.numSourceDimensions = op1HasDims ? op1.numSourceDimensions() : op2.numSourceDimensions();
		this.numTargetDimensions = op2HasDims ? op2.numTargetDimensions() : op1.numTargetDimensions();
	}

	private VolatileConcatenatedUnaryBlockOperator( VolatileConcatenatedUnaryBlockOperator< S, T, U > op )
	{
		this.op1 = op.op1.independentCopy();
		this.op2 = op.op2.independentCopy();
		this.numSourceDimensions = op.numSourceDimensions;
		this.numTargetDimensions = op.numTargetDimensions;
	}

	@Override
	public void compute( final VolatileBlockSupplier< S > src, final Interval interval, final VolatileArray< ? > dest )
	{
		applyTo( src ).copy( interval, dest );
	}

	@Override
	public AllValid allValid( final VolatileBlockSupplier< S > src, final Interval interval, final boolean failFast )
	{
		return applyTo( src ).allValid( interval, failFast );
	}

	@Override
	public S getSourceType()
	{
		return op1.getSourceType();
	}

	@Override
	public U getTargetType()
	{
		return op2.getTargetType();
	}

	@Override
	public int numSourceDimensions()
	{
		return 0;
	}

	@Override
	public int numTargetDimensions()
	{
		return 0;
	}

	@Override
	public VolatileUnaryBlockOperator< S, U > independentCopy()
	{
		return new VolatileConcatenatedUnaryBlockOperator<>( this );
	}

	@Override
	public VolatileBlockSupplier< U > applyTo( final VolatileBlockSupplier< S > blocks )
	{
		return op2.applyTo( op1.applyTo( blocks ) );
	}
}
