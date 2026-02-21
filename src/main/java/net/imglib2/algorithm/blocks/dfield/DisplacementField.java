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

import net.imglib2.EuclideanSpace;
import net.imglib2.Typed;
import net.imglib2.algorithm.blocks.BlockSupplier;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

/**
 * A normalized displacement field, and its scale and offset with respect to a
 * source image.
 * <p>
 * A "normalized" field expresses displacements in units relative to the
 * displacement grid's own pixel spacing. Consequently, downsampling the grid by
 * a factor of N requires scaling the displacement vectors by 1/N to maintain
 * normalization.
 *
 * @param <T> displacement component type. should be {@code DoubleType} or {@code FloatType}
 */
public class DisplacementField< T extends NativeType< T > & RealType< T > >
	implements EuclideanSpace, Typed< T >
{
	private final BlockSupplier< T > displacements;

	private final double[] scale;

	private final double[] translation;

	/**
	 * Constructs a displacement field with the specified normalized displacements, scale, and translation.
	 * <p>
	 * A "normalized" field expresses displacements in units relative to the
	 * displacement grid's own pixel spacing. Consequently, downsampling the grid by
	 * a factor of N requires scaling the displacement vectors by 1/N to maintain
	 * normalization.
	 *
	 * @param displacements
	 * 		provides displacement vectors, with components of the
	 * 		displacement vector in dimension 0. (shifting all other
	 * 		dimensions by 1. That is, the {@code BlockSupplier} has {@link
	 *        #numDimensions()} + 1 dimensions.)
	 * @param scale
	 * 		displacement field coordinates and
	 * 		displacement vectors should be scaled by this factor when looking up
	 * 		intensities in a source image.
	 * @param translation
	 * 		when interpolating {@link #displacements()} into a position field for
	 * 		value look-up in the source image, this translation should be added.
	 * 		(This happens after scaling, so the translation is in units of source
	 * 		image pixels).
	 */
	public DisplacementField(
			final BlockSupplier< T > displacements,
			final double[] scale,
			final double[] translation )
	{
		final int n = displacements.numDimensions() - 1;
		if ( n != scale.length || n != translation.length ) {
			throw new IllegalArgumentException( "Dimensionality of scale and translation must match the number of dimensions in the displacement field" );
		}
		this.displacements = displacements;
		this.scale = scale;
		this.translation = translation;
	}

	@Override
	public int numDimensions()
	{
		return scale.length;
	}

	@Override
	public T getType()
	{
		return displacements.getType();
	}

	/**
	 * Displacement vector field.
	 * <p>
	 * The components of the displacement vector are in dimension 0, shifting
	 * all other dimensions by 1. (The returned {@code BlockSupplier} has {@link
	 * #numDimensions()} + 1 dimensions.)
	 *
	 * @return the displacement field
	 */
	public BlockSupplier< T > displacements()
	{
		return displacements;
	}

	/**
	 * The {@link #displacements() displacement field} coordinates and
	 * displacement vectors should be scaled by this factor when looking up
	 * intensities in a source image.
	 * <p>
	 * In other words, this is the spacing of the displacement field grid in
	 * units of source image pixels.
	 *
	 * @return relative scale of the displacement field wrt the source image
	 */
	public double[] scale()
	{
		return scale;
	}

	/**
	 * When interpolating {@link #displacements()} into a position field for
	 * value look-up in the source image, this translation vector is added.
	 * (This happens after scaling, so the translation is in units of source
	 * image pixels).
	 * <p>
	 * For example, this can be used to compensate for half-pixel offsets when
	 * applying downsampled displacement fields to full-resolution images.
	 *
	 * @return position field offset in source pixels
	 */
	public double[] translation()
	{
		return translation;
	}
}
