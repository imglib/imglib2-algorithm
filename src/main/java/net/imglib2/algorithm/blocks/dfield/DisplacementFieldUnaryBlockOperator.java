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

import static net.imglib2.util.Util.safeInt;

import net.imglib2.Interval;
import net.imglib2.algorithm.blocks.AbstractUnaryBlockOperator;
import net.imglib2.algorithm.blocks.BlockProcessor;
import net.imglib2.algorithm.blocks.BlockSupplier;
import net.imglib2.algorithm.blocks.UnaryBlockOperator;
import net.imglib2.blocks.BlockInterval;
import net.imglib2.blocks.TempArray;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;

/**
 * TODO: javadoc
 * <p>
 * {@code type} must be {@code DoubleType} of {@code FloatType}.
 *
 * @param <D>
 * 		displacement field type
 * @param <T>
 * 		the source/target type
 */
public class DisplacementFieldUnaryBlockOperator< D extends NativeType< D > & RealType< D >, T extends NativeType< T > > extends AbstractUnaryBlockOperator< T, T >
{
	@SuppressWarnings( "rawtypes" )
	private final AbstractTransformProcessor fieldProcessor;

	private final BlockSupplier< D > displacementField;

	private final TempArray< ? > tempArrayPositionField;

	@SuppressWarnings( "rawtypes" )
	private final AbstractLookupProcessor lookupProcessor;

	public DisplacementFieldUnaryBlockOperator(
			T type, int numDimensions,
			AbstractTransformProcessor fieldProcessor,
			BlockSupplier< D > displacementField,
			AbstractLookupProcessor lookupProcessor
	)
	{
		super( type, type, numDimensions, numDimensions );
		this.fieldProcessor = fieldProcessor;
		this.displacementField = displacementField;
		this.lookupProcessor = lookupProcessor;
		tempArrayPositionField = TempArray.forPrimitiveType( displacementField.getType().getNativeTypeFactory().getPrimitiveType() );
	}

	private DisplacementFieldUnaryBlockOperator( DisplacementFieldUnaryBlockOperator< D, T > op )
	{
		super( op );
		this.fieldProcessor = op.fieldProcessor.independentCopy();
		this.displacementField = op.displacementField.independentCopy();
		this.lookupProcessor = op.lookupProcessor.independentCopy();
		this.tempArrayPositionField = op.tempArrayPositionField.newInstance();
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public void compute( final BlockSupplier< T > src, final Interval interval, final Object dest )
	{
		fieldProcessor.setTargetInterval( interval );
		final Object bufField = fieldProcessor.getSourceBuffer();
		displacementField.copy( fieldProcessor.getSourceInterval(), bufField );
		final Object positions = tempArrayPositionField.get( safeInt( numTargetDimensions() * Intervals.numElements( interval ) ) );
		fieldProcessor.compute( bufField, positions );

		final BlockInterval bounds = fieldProcessor.getInputBounds();
		lookupProcessor.setTargetInterval( interval );
		lookupProcessor.setSourceInterval( bounds );
		final Object buf = lookupProcessor.getSourceBuffer();
		src.copy( bounds, buf );
		lookupProcessor.setPositionField( positions ); // TODO: add this method
		lookupProcessor.compute( buf, dest );
	}

	@Override
	public UnaryBlockOperator< T, T > independentCopy()
	{
		return new DisplacementFieldUnaryBlockOperator<>( this );
	}
}
