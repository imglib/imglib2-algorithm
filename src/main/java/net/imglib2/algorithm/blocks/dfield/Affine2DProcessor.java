/*-
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
package net.imglib2.algorithm.blocks.dfield;

import java.util.Arrays;

import net.imglib2.Interval;
import net.imglib2.RealInterval;
import net.imglib2.algorithm.blocks.BlockProcessor;
import net.imglib2.algorithm.blocks.transform.Transform.Interpolation;
import net.imglib2.blocks.BlockInterval;
import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.type.PrimitiveType;

/**
 * A {@link BlockProcessor} for interpolation and affine transform, using {@link
 * AffineTransform2D} and 2D source/target.
 *
 * @param <P>
 * 		input/output primitive array type (i.e., float[] or double[])
 */
public // TODO: make package private again (public for testing)
class Affine2DProcessor< P > extends AbstractTransformProcessor< P >
{
	private final AffineTransform2D transformToSource;

	private final TransformLine2D< P > transformLine;

	private final double pdest[] = new double[ 2 ];

	private final double psrc[] = new double[ 2 ];

	Affine2DProcessor(
			final AffineTransform2D transformToSource,
			final PrimitiveType primitiveType )
	{
		this( transformToSource, primitiveType, TransformLine2D.of( primitiveType ) );
	}

	private Affine2DProcessor(
			final AffineTransform2D transformToSource,
			final PrimitiveType primitiveType,
			final TransformLine2D< P > transformLine )
	{
		super( 2, primitiveType );
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

	// specific to 2D
	@Override
	public void compute( final P src, final P dest )
	{
		final float d0 = transformToSource.d( 0 ).getFloatPosition( 0 );
		final float d1 = transformToSource.d( 0 ).getFloatPosition( 1 );
		final int ds0 = destSize[ 0 ];
		final int ss0 = sourceSize[ 1 ];
		pdest[ 0 ] = destPos[ 0 ];
		int i = 0;
		for ( int y = 0; y < destSize[ 1 ]; ++y )
		{
			pdest[ 1 ] = y + destPos[ 1 ];
			transformToSource.apply( pdest, psrc );
			float sf0 = ( float ) ( psrc[ 0 ] - sourcePos[ 1 ] );
			float sf1 = ( float ) ( psrc[ 1 ] - sourcePos[ 2 ] );
			transformLine.apply( src, dest, i, ds0, d0, d1, ss0, sf0, sf1 );
			i += 2 * ds0;
		}

//		final int ds1 = destSize[ 1 ];
//		final int length = ds0 * ds1;
	}


	private static void sourceBounds( final double[] dest, final int length, final Interpolation interpolation, final BlockInterval sourceInterval )
	{
		double min0 = dest[ 0 ], max0 = min0;
		double min1 = dest[ 1 ], max1 = min1;
		for ( int i = 1; i < length; ++i )
		{
			final double v0 = dest[ 2 * i ];
			if ( v0 < min0 )
				min0 = v0;
			else if ( v0 > max0 )
				max0 = v0;
			final double v1 = dest[ 2 * i + 1 ];
			if ( v1 < min1 )
				min1 = v1;
			else if ( v1 > max1 )
				max1 = v1;
		}

		final long[] sourcePos = sourceInterval.min();
		final int[] sourceSize = sourceInterval.size();
		switch ( interpolation )
		{
		case NEARESTNEIGHBOR:
			sourcePos[ 0 ] = Math.round( min0 - 0.5 );
			sourcePos[ 1 ] = Math.round( min1 - 0.5 );
			sourceSize[ 0 ] = ( int ) ( Math.round( max0 + 0.5 ) - sourcePos[ 0 ] ) + 1;
			sourceSize[ 1 ] = ( int ) ( Math.round( max1 + 0.5 ) - sourcePos[ 1 ] ) + 1;
			break;
		case NLINEAR:
			sourcePos[ 0 ] = ( long ) Math.floor( min0 - 0.5 );
			sourcePos[ 1 ] = ( long ) Math.floor( min1 - 0.5 );
			sourceSize[ 0 ] = ( int ) ( ( long ) Math.floor( max0 + 0.5 ) - sourcePos[ 0 ] ) + 2;
			sourceSize[ 1 ] = ( int ) ( ( long ) Math.floor( max1 + 0.5 ) - sourcePos[ 1 ] ) + 2;
			break;
		}

	}
}
