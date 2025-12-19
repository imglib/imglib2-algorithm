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

import net.imglib2.Interval;
import net.imglib2.RealInterval;
import net.imglib2.algorithm.blocks.BlockProcessor;
import net.imglib2.algorithm.blocks.transform.Transform.Interpolation;
import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.type.PrimitiveType;

/**
 * A {@link BlockProcessor} for interpolation and affine transform, using {@link
 * AffineTransform2D} and (1+2)D displacement field source/target.
 *
 * @param <P>
 * 		input/output primitive array type (i.e., float[] or double[])
 */
public // TODO: make package private again (public for testing)
	// TODO: rename? "DisplacementFieldAffine2DProcessor"?
class Affine2DProcessor< P > extends AbstractTransformProcessor< P >
{
	private final AffineTransform2D transformToSource;

	private final TransformLine2D< P > transformLine;

	private final double pdest[] = new double[ 2 ];

	private final double psrc[] = new double[ 2 ];

	private final double displacementScale0;
	private final double displacementScale1;

	public // TODO: make package private again (public for testing)
	Affine2DProcessor(
			final AffineTransform2D transformToSource, // TODO: rename? "source" == "displacement field" here ...
			final double[] displacementScale, // for a "normalized" displacement field, this is the spacing (i.e. downsampling factor wrt input grid)
			final Interpolation inputInterpolation,
			final PrimitiveType primitiveType )
	{
		super( 2, inputInterpolation, primitiveType );
		this.displacementScale0 = displacementScale[ 0 ];
		this.displacementScale1 = displacementScale[ 1 ];
		this.transformToSource = transformToSource;
		this.transformLine = TransformLine2D.of( primitiveType );
	}

	private Affine2DProcessor( Affine2DProcessor< P > processor )
	{
		super( processor );
		displacementScale0 = processor.displacementScale0;
		displacementScale1 = processor.displacementScale1;
		transformToSource = processor.transformToSource;
		transformLine = processor.transformLine;
	}

	@Override
	public AbstractTransformProcessor< P > independentCopy()
	{
		return new Affine2DProcessor<>( this );
	}

	@Override
	RealInterval estimateBounds( final Interval interval )
	{
		return transformToSource.estimateBounds( interval );
	}

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
			transformLine.scale( dest, i, ds0, displacementScale0, displacementScale1 );
			i += 2 * ds0;
		}

		// now that we know the position vectors, compute input image bounds
		transformLine.sourceBounds( dest, ds0 * destSize[ 1 ], inputInterpolation, inputBounds );

		// now that we know the source bounds, compute the position vector offset
		inputOffset[ 0 ] = displacementScale0 * sourcePos[ 1 ] - inputBounds.min( 0 );
		inputOffset[ 1 ] = displacementScale1 * sourcePos[ 2 ] - inputBounds.min( 1 );
	}
}
