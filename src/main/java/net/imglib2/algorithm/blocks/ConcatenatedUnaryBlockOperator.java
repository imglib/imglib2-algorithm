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
package net.imglib2.algorithm.blocks;

import net.imglib2.Interval;
import net.imglib2.type.NativeType;

class ConcatenatedUnaryBlockOperator<
		S extends NativeType< S >,
		T extends NativeType< T >,
		U extends NativeType< U > >
		implements UnaryBlockOperator< S, U >
{
	private final UnaryBlockOperator< S, T > op1;
	private final UnaryBlockOperator< T, U > op2;
	private final int numSourceDimensions;
	private final int numTargetDimensions;

	ConcatenatedUnaryBlockOperator( UnaryBlockOperator< S, T > op1, UnaryBlockOperator< T, U > op2 )
	{
		this.op1 = op1;
		this.op2 = op2;

		final boolean op1HasDims = op1.numSourceDimensions() > 0;
		final boolean op2HasDims = op2.numSourceDimensions() > 0;
		if ( op2HasDims && op1HasDims && op1.numTargetDimensions() != op2.numSourceDimensions() ) {
			throw new IllegalArgumentException( "UnaryBlockOperator cannot be concatenated: number of dimensions mismatch." );
		}
		this.numSourceDimensions = op1HasDims ? op1.numSourceDimensions() : op2.numSourceDimensions();
		this.numTargetDimensions = op2HasDims ? op2.numTargetDimensions() : op1.numTargetDimensions();
	}

	private ConcatenatedUnaryBlockOperator( ConcatenatedUnaryBlockOperator< S, T, U > op )
	{
		this.op1 = op.op1.independentCopy();
		this.op2 = op.op2.independentCopy();
		this.numSourceDimensions = op.numSourceDimensions;
		this.numTargetDimensions = op.numTargetDimensions;
	}

	@Override
	public void compute( final BlockSupplier< S > src, final Interval interval, final Object dest )
	{
		applyTo( src ).copy( interval, dest );
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
	public UnaryBlockOperator< S, U > independentCopy()
	{
		return new ConcatenatedUnaryBlockOperator<>( this );
	}

	@Override
	public BlockSupplier< U > applyTo( final BlockSupplier< S > blocks )
	{
		return op2.applyTo( op1.applyTo( blocks ) );
	}
}
