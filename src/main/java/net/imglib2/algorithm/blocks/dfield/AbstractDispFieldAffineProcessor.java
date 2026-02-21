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
import net.imglib2.algorithm.blocks.AbstractBlockProcessor;
import net.imglib2.algorithm.blocks.transform.Transform.Interpolation;
import net.imglib2.blocks.BlockInterval;
import net.imglib2.type.PrimitiveType;

/**
 * Abstract base class for {@link DispFieldAffine3DProcessor} and {@link
 * DispFieldAffine2DProcessor}. Implements source/target interval computation, and {@code
 * TempArray} and thread-safe setup.
 *
 * @param <P>
 * 		input/output primitive array type (i.e., float[] or double[])
 */
abstract class AbstractDispFieldAffineProcessor< P > extends AbstractBlockProcessor< P, P >
{
	PrimitiveType primitiveType;

	final int n;

	final long[] destPos;

	final int[] destSize;

	final BlockInterval inputBounds;

	final double[] inputOffset;

	/**
	 * The interpolation that will be used for sampling the input image with the
	 * position field created by this processor. This is needed for determining
	 * the padding of {@link #inputBounds}.
	 */
	final Interpolation inputInterpolation;

	AbstractDispFieldAffineProcessor( final int n, final Interpolation inputInterpolation, final PrimitiveType primitiveType )
	{
		super( primitiveType, n + 1 );
		this.primitiveType = primitiveType;
		this.n = n;
		destPos = new long[ n ];
		destSize = new int[ n ];
		inputBounds = new BlockInterval( n );
		inputOffset = new double[ n ];
		this.inputInterpolation = inputInterpolation;
	}

	AbstractDispFieldAffineProcessor( AbstractDispFieldAffineProcessor< P > transform )
	{
		super( transform );

		// re-use
		primitiveType = transform.primitiveType;
		n = transform.n;
		inputInterpolation = transform.inputInterpolation;

		// init empty
		destPos = new long[ n ];
		destSize = new int[ n ];
		inputBounds = new BlockInterval( n );
		inputOffset = new double[ n ];
	}

	/**
	 * Estimate (inverse-transformed) source bounds in nD space from the given
	 * nD target {@code interval}.
	 * <p>
	 * This is used by {@link #setTargetInterval} to derive the source bounds in
	 * dimensions (1, ..., n+1). This is augmented with dimension 0, which is
	 * always the full size n (number of dimensions of a displacement vector).
	 */
	abstract RealInterval estimateBounds( Interval interval );

	@Override
	public void setTargetInterval( final Interval interval )
	{
		BlockInterval.wrap( destPos, destSize ).setFrom( interval );
		final RealInterval bounds = estimateBounds( interval );
		sourcePos[ 0 ] = 0;
		sourceSize[ 0 ] = n;
		for ( int d = 0; d < n; ++d )
		{
			sourcePos[ d + 1 ] = ( long ) Math.floor( bounds.realMin( d ) - 0.5 );
			sourceSize[ d + 1 ] = ( int ) ( ( long ) Math.floor( bounds.realMax( d ) + 0.5 ) - sourcePos[ d + 1 ] ) + 2;
		}
	}

	/**
	 * Get the input image bounds required to render an output image with
	 * the position field obtained with the last {@link #compute} call.
	 * <p>
	 * (This depends on the displacement values, so it can be only computed
	 * after the position field block has been created.)
	 *
	 * @return the {@link BlockInterval} representing the input bounds.
	 */
	public BlockInterval getInputBounds()
	{
		return inputBounds;
	}

	/**
	 * Get the offset to apply to vectors of the position field obtained with
	 * the last {@link #compute} call, when interpolating into a source img
	 * block covering {@link #getInputBounds()}.
	 * <p>
	 * The offsets {@code d0, d1, d2} account for (scaled) offset of both the
	 * displacement field block and the src block. They should be computed like
	 * this:
	 * <pre>{@code
	 * d0 = displacementScale0 * fieldTransform.sourcePos[1] + displacementOffset0 - lookupTransform.sourcePos[0]
	 * d1 = displacementScale1 * fieldTransform.sourcePos[2] + displacementOffset1 - lookupTransform.sourcePos[1]
	 * d2 = displacementScale2 * fieldTransform.sourcePos[3] + displacementOffset2 - lookupTransform.sourcePos[2]
	 * }</pre>
	 * <p>
	 * (This depends on the displacement values, so it can be only computed
	 * after the position field block has been created.)
	 *
	 * @return the offset to apply to position field vectors when interpolating into the source block
	 */
	public double[] getInputOffset()
	{
		return inputOffset;
	}

	@Override
	public abstract AbstractDispFieldAffineProcessor< P > independentCopy();
}
