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
package net.imglib2.algorithm.blocks.transform;

import net.imglib2.Interval;
import net.imglib2.RealInterval;
import net.imglib2.algorithm.blocks.BlockProcessor;
import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.type.PrimitiveType;

/**
 * A {@link BlockProcessor} for interpolation and affine transform, using {@link
 * AffineTransform2D} and 2D source/target.
 *
 * @param <P>
 * 		input/output primitive array type (i.e., float[] or double[])
 */
class Affine2DProcessor< P > extends AbstractTransformProcessor< Affine2DProcessor< P >, P >
{
	private final AffineTransform2D transformToSource;

	private final TransformLine2D< P > transformLine;

	private final double pdest[] = new double[ 2 ];

	private final double psrc[] = new double[ 2 ];

	Affine2DProcessor(
			final AffineTransform2D transformToSource,
			final Transform.Interpolation interpolation,
			final PrimitiveType primitiveType )
	{
		this( transformToSource, interpolation, primitiveType, TransformLine2D.of( interpolation, primitiveType ) );
	}

	private Affine2DProcessor(
			final AffineTransform2D transformToSource,
			final Transform.Interpolation interpolation,
			final PrimitiveType primitiveType,
			final TransformLine2D< P > transformLine )
	{
		super( 2, interpolation, primitiveType );
		this.transformToSource = transformToSource;
		this.transformLine = transformLine;
	}

	private Affine2DProcessor( Affine2DProcessor< P > processor )
	{
		super( processor );
		transformToSource = processor.transformToSource;
		transformLine = processor.transformLine;
	}

	@Override
	public BlockProcessor< P, P > independentCopy()
	{
		return new Affine2DProcessor<>( this );
	}

	@Override
	RealInterval estimateBounds( final Interval interval )
	{
		return transformToSource.estimateBounds( interval );
	}

	// specific to 3D
	@Override
	public void compute( final P src, final P dest )
	{
		final float d0 = transformToSource.d( 0 ).getFloatPosition( 0 );
		final float d1 = transformToSource.d( 0 ).getFloatPosition( 1 );
		final int ds0 = destSize[ 0 ];
		final int ss0 = sourceSize[ 0 ];
		pdest[ 0 ] = destPos[ 0 ];
		int i = 0;
		for ( int y = 0; y < destSize[ 1 ]; ++y )
		{
			pdest[ 1 ] = y + destPos[ 1 ];
			transformToSource.apply( pdest, psrc );
			float sf0 = ( float ) ( psrc[ 0 ] - sourcePos[ 0 ] );
			float sf1 = ( float ) ( psrc[ 1 ] - sourcePos[ 1 ] );
			transformLine.apply( src, dest, i, ds0, d0, d1, ss0, sf0, sf1 );
			i += ds0;
		}
	}

}
