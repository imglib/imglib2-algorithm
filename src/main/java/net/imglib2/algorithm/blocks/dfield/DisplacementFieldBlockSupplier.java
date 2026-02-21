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
import net.imglib2.algorithm.blocks.AbstractBlockSupplier;
import net.imglib2.algorithm.blocks.BlockSupplier;
import net.imglib2.blocks.BlockInterval;
import net.imglib2.blocks.TempArray;
import net.imglib2.realtransform.AffineGet;
import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.PrimitiveType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;

import static net.imglib2.algorithm.blocks.transform.Transform.Interpolation.NLINEAR;
import static net.imglib2.algorithm.blocks.transform.Transform.invert;
import static net.imglib2.util.Util.safeInt;

/**
 * A {@code BlockSupplier} that combines a {@code
 * AbstractDispFieldAffineProcessor} and a {@code PositionFieldFunction} to
 * compute (blocks of) the transformation of a function with a displacement
 * field.
 * <p>
 * The {@code AbstractDispFieldAffineProcessor} interpolates and affine
 * transforms the displacement field to get a position field.
 * <p>
 * The {@code PositionFieldFunction} computes target values using the position
 * field vectors.
 *
 * @param <D>
 * 		displacement field type
 * @param <T>
 * 		target pixel type
 */
public class DisplacementFieldBlockSupplier< D extends NativeType< D > & RealType< D >, T extends NativeType< T > > extends AbstractBlockSupplier< T >
{
	/**
	 * Create a {@code BlockSupplier} that transforms {@code displacementField}
	 * into a position field and produces target values by applying
	 * {@code positionFieldFunction} to the resulting position vectors.
	 * <p>
	 * {@code transformFromField} is an affine transform from {@code
	 * displacementField} coordinates to target coordinates. For example, this
	 * can be used to upscale a downsampled displacement field.
	 *
	 * @param transformFromField
	 * 		a 2D or 3D affine transform from displacementField coordinates to
	 * 		target coordinates
	 * @param displacementField
	 * 		the (normalized) displacement field
	 * @param positionFieldFunction
	 * 		maps position vectors to target values
	 * @param <D>
	 * 		displacement field type
	 * @param <T>
	 * 		the target type
	 *
	 * @return a {@code BlockSupplier} that transforms {@code displacementField}
	 * into a position field and produces target values by applying
	 * positionFieldFunction to the resulting position vectors
	 */
	public static < D extends NativeType< D > & RealType< D >, T extends NativeType< T > >
	BlockSupplier< T > create(
			final AffineGet transformFromField,
			final DisplacementField< D > displacementField,
			final PositionFieldFunction< D, T, ?, ? > positionFieldFunction )
	{
		final int n = transformFromField.numDimensions();
		if ( n < 2 || n > 3 )
		{
			throw new IllegalArgumentException( "Only 2D and 3D affine transforms are supported currently" );
		}
		if ( displacementField.numDimensions() != n )
		{
			throw new IllegalArgumentException( "Number of dimension must be the same for the affine transform and the displacement field" );
		}

		final AffineGet transformToField = invert( transformFromField );
		final PrimitiveType dfieldPrimitiveType = displacementField.getType().getNativeTypeFactory().getPrimitiveType();
		final double[] scale = displacementField.scale();
		final double[] translation = displacementField.translation();
		final BlockSupplier< D > displacements = displacementField.displacements();
		final AbstractDispFieldAffineProcessor< ? > fieldProcessor = ( n == 2 )
				? new DispFieldAffine2DProcessor<>( ( AffineTransform2D ) transformToField, scale, translation, NLINEAR, dfieldPrimitiveType )
				: new DispFieldAffine3DProcessor<>( ( AffineTransform3D ) transformToField, scale, translation, NLINEAR, dfieldPrimitiveType );
		return new DisplacementFieldBlockSupplier<>( n, fieldProcessor, displacements, positionFieldFunction );
	}

	private final T type;

	private final int numDimensions;

	@SuppressWarnings( "rawtypes" )
	private final AbstractDispFieldAffineProcessor fieldProcessor;

	@SuppressWarnings( "rawtypes" )
	private final PositionFieldFunction positionFieldFunction;

	private final BlockSupplier< D > displacementField;

	private final TempArray< ? > tempArrayPositionField;

	/**
	 *
	 * @param numDimensions
	 * 		number of dimensions (source and target) of this operator
	 * @param fieldProcessor
	 * 		interpolates and affine-transforms the {@code displacementField} to get a position field
	 * @param displacementField
	 * 		a normalized displacement field and its mapping to the source image
	 * @param positionFieldFunction
	 * 		maps position vectors to target values
	 */
	DisplacementFieldBlockSupplier(
			int numDimensions,
			AbstractDispFieldAffineProcessor< ? > fieldProcessor,
			BlockSupplier< D > displacementField,
			PositionFieldFunction< D, T, ?, ? > positionFieldFunction )
	{
		this.type = positionFieldFunction.getType();
		this.numDimensions = numDimensions;
		this.fieldProcessor = fieldProcessor;
		this.positionFieldFunction = positionFieldFunction;
		this.displacementField = displacementField;
		tempArrayPositionField = TempArray.forPrimitiveType( displacementField.getType().getNativeTypeFactory().getPrimitiveType() );
	}

	private DisplacementFieldBlockSupplier(DisplacementFieldBlockSupplier< D, T > op )
	{
		this.type = op.type;
		this.numDimensions = op.numDimensions;
		this.fieldProcessor = op.fieldProcessor.independentCopy();
		this.positionFieldFunction = op.positionFieldFunction.independentCopy();
		this.displacementField = op.displacementField.independentCopy();
		this.tempArrayPositionField = op.tempArrayPositionField.newInstance();
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public void copy( Interval interval, Object dest )
	{
		final int destLength = ( int ) Intervals.numElements( interval );

		fieldProcessor.setTargetInterval( interval );
		final Object bufField = fieldProcessor.getSourceBuffer();
		displacementField.copy( fieldProcessor.getSourceInterval(), bufField );
		final Object positions = tempArrayPositionField.get( safeInt( numDimensions() * ( long ) destLength ) );
		fieldProcessor.compute( bufField, positions );

		final double[] offset = fieldProcessor.getInputOffset();
		// NB: We don't operate on a translated interval of the input, but use
		// input coordinates directly. Therefore, we need to undo the shift to
		// inputBounds.min().
		final BlockInterval bounds = fieldProcessor.getInputBounds();
		for ( int d = 0; d < numDimensions; d++ )
			offset[ d ] += bounds.min( d );

		positionFieldFunction.compute( dest, destLength, positions, offset );
	}

	@Override
	public int numDimensions() {
		return numDimensions;
	}

	@Override
	public T getType() {
		return type;
	}

	@Override
	public BlockSupplier<T> independentCopy() {
		return new DisplacementFieldBlockSupplier<>(this);
	}
}

