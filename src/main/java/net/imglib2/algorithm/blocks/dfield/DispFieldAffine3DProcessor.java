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

import net.imglib2.Interval;
import net.imglib2.RealInterval;
import net.imglib2.algorithm.blocks.BlockProcessor;
import net.imglib2.algorithm.blocks.transform.Transform.Interpolation;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.PrimitiveType;

/**
 * A {@link BlockProcessor} for interpolation and affine transform of a
 * displacement field, using {@link AffineTransform3D} and (1+3)D displacement
 * field source/target.
 *
 * @param <P>
 * 		input/output primitive array type (i.e., float[] or double[])
 */
class DispFieldAffine3DProcessor< P > extends AbstractDispFieldAffineProcessor< P >
{
	private final AffineTransform3D transformToSource;

	private final DispFieldAffine3D< P > dispFieldAffine;

	private final double pdest[] = new double[ 3 ];

	private final double psrc[] = new double[ 3 ];

	private final double displacementScale0;
	private final double displacementScale1;
	private final double displacementScale2;
	private final double displacementOffset0;
	private final double displacementOffset1;
	private final double displacementOffset2;

	/**
	 * @param transformToSource
	 * 		transforms target coordinates into displacement field coordinates
	 * @param displacementScale
	 * 		displacement field coordinates and displacement vectors should be
	 * 		scaled by this factor when looking up intensities in a source image.
	 * 		For a "normalized" displacement field, this is the spacing, i.e.,
	 * 		downsampling factor wrt input grid.
	 * @param displacementOffset
	 * 		when interpolating displacements into a position field for value
	 * 		look-up in the source image, this translation should be added. (This
	 * 		happens after scaling, so the translation is in units of source image
	 * 		pixels).
	 * @param inputInterpolation
	 * 		the interpolation that will be applied later, when using position
	 * 		vectors to interpolate into the source image. (This determines
	 * 		required padding for source bounds.)
	 * @param primitiveType
	 * 		the component type of displacement vectors (float or double)
	 */
	DispFieldAffine3DProcessor(
			final AffineTransform3D transformToSource,
			final double[] displacementScale, // for a "normalized" displacement field, this is the spacing (i.e. downsampling factor wrt input grid)
			final double[] displacementOffset,
			final Interpolation inputInterpolation,
			final PrimitiveType primitiveType )
	{
		super( 3, inputInterpolation, primitiveType );
		this.displacementScale0 = displacementScale[ 0 ];
		this.displacementScale1 = displacementScale[ 1 ];
		this.displacementScale2 = displacementScale[ 2 ];
		this.displacementOffset0 = displacementOffset[ 0 ];
		this.displacementOffset1 = displacementOffset[ 1 ];
		this.displacementOffset2 = displacementOffset[ 2 ];
		this.transformToSource = transformToSource;
		this.dispFieldAffine = DispFieldAffine3D.of( primitiveType );
	}

	private DispFieldAffine3DProcessor( DispFieldAffine3DProcessor< P > processor )
	{
		super( processor );
		displacementScale0 = processor.displacementScale0;
		displacementScale1 = processor.displacementScale1;
		displacementScale2 = processor.displacementScale2;
		displacementOffset0 = processor.displacementOffset0;
		displacementOffset1 = processor.displacementOffset1;
		displacementOffset2 = processor.displacementOffset2;
		transformToSource = processor.transformToSource;
		dispFieldAffine = processor.dispFieldAffine;
	}

	@Override
	public AbstractDispFieldAffineProcessor< P > independentCopy()
	{
		return new DispFieldAffine3DProcessor<>( this );
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
		final float d2 = transformToSource.d( 0 ).getFloatPosition( 2 );
		final int ds0 = destSize[ 0 ];
		final int ss0 = sourceSize[ 1 ];
		final int ss1 = sourceSize[ 2 ] * ss0;
		pdest[ 0 ] = destPos[ 0 ];
		int i = 0;
		for ( int z = 0; z < destSize[ 2 ]; ++z )
		{
			pdest[ 2 ] = z + destPos[ 2 ];
			for ( int y = 0; y < destSize[ 1 ]; ++y )
			{
				pdest[ 1 ] = y + destPos[ 1 ];
				transformToSource.apply( pdest, psrc );
				float sf0 = ( float ) ( psrc[ 0 ] - sourcePos[ 1 ] );
				float sf1 = ( float ) ( psrc[ 1 ] - sourcePos[ 2 ] );
				float sf2 = ( float ) ( psrc[ 2 ] - sourcePos[ 3 ] );
				dispFieldAffine.transformLine( src, dest, i, ds0, d0, d1, d2, ss0, ss1, sf0, sf1, sf2 );
				dispFieldAffine.scale( dest, i, ds0, displacementScale0, displacementScale1, displacementScale2 );
				i += 3 * ds0;
			}
		}

		// now that we know the position vectors, compute input image bounds
		//
		// vector in dest = (
		// 						interpolated displacement
		// 					  + (real, not rounded) position on displacement grid, relative to sourcePos[1,2,3]
		//                  ) * displacementScale
		//                  + displacementOffset
		//
		// sourcePos[1,2,3] in input grid = ( sourcePos[1,2,3] * displacementScale )
		//
		// vector in dest will look up
		//					( sourcePos[1,2,3] * displacementScale + displacementOffset )
		// 					+ (   interpolated displacement
		// 					    + (real, not rounded) position on displacement grid, relative to sourcePos[1,2,3]
		//                    ) * displacementScale
		//
		final double o0 = displacementScale0 * sourcePos[ 1 ] + displacementOffset0;
		final double o1 = displacementScale1 * sourcePos[ 2 ] + displacementOffset1;
		final double o2 = displacementScale2 * sourcePos[ 3 ] + displacementOffset2;
		dispFieldAffine.sourceBounds( dest, ds0 * destSize[ 1 ] * destSize[ 2 ], o0, o1, o2, inputInterpolation, inputBounds );

		// now that we know the source bounds, compute the position vector offset
		inputOffset[ 0 ] = o0 - inputBounds.min( 0 );
		inputOffset[ 1 ] = o1 - inputBounds.min( 1 );
		inputOffset[ 2 ] = o2 - inputBounds.min( 2 );
	}
}
