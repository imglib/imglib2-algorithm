/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2021 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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
package net.imglib2.algorithm.math.execution;

import java.util.Iterator;

import net.imglib2.AbstractInterval;
import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.View;
import net.imglib2.algorithm.math.Compute;
import net.imglib2.algorithm.math.abstractions.IFunction;
import net.imglib2.algorithm.math.abstractions.Util;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

/**
 * A {@link View} of the computation defined by an {@link IFunction}.
 * 
 * The dimensions are those of the first input {@link RandomAccessibleInterval} found.
 * (the computation will fail if images do not have the same dimensions.)
 * 
 * If an output type is not defined, then the {@link RealType} of the first {@link RandomAccessibleInterval} found is used.
 * 
 * @author Albert Cardona
 *
 * @param <O> The {@link RealType}.
 */
public class IterableRandomAccessibleFunction< C extends RealType< C >, O extends RealType< O > >
extends AbstractInterval
implements RandomAccessibleInterval< O >, IterableInterval< O >, View
{
	protected final IFunction operation;
	private final RandomAccessibleInterval< ? > firstImg;
	protected final C computeType;
	protected final O outputType;
	protected final Converter< RealType< ? >, C > inConverter;
	protected final Converter< C, O > outConverter;

	/**
	 * 
	 * @param operation
	 * @param inConverter Can be null
	 * @param computeType
	 * @param outputType
	 * @param outConverter Can be null.
	 */
	public IterableRandomAccessibleFunction(
			final IFunction operation,
			final Converter< RealType< ? >, C > inConverter,
			final C computeType,
			final O outputType,
			final Converter< C, O > outConverter
			)
	{
		super( Util.findFirstInterval( operation ) );
		this.operation = operation;
		this.firstImg = Util.findFirstImg( operation );
		this.computeType = computeType;
		this.outputType = outputType;
		this.inConverter = inConverter;
		this.outConverter = outConverter;
	}
	
	/**
	 * Use a default {@link Converter} as defined by {@link Util#genericRealTypeConverter()}.
	 */
	@SuppressWarnings("unchecked")
	public IterableRandomAccessibleFunction( final IFunction operation, final O outputType )
	{
		this( operation, null, (C) outputType.createVariable(), outputType, null );
	}
	
	/**
	 * Use a default {@link Converter} as defined by {@link Util#genericRealTypeConverter()}.
	 */
	@SuppressWarnings("unchecked")
	public IterableRandomAccessibleFunction( final IFunction operation, final O outputType, final Converter< C, O > outConverter )
	{
		this( operation, null, (C) outputType.createVariable(), outputType, outConverter );
	}
	
	/**
	 * Use a the same {@link RealType} as the first input {@link RandomAccessibleInterval} found,
	 * and a default {@link Converter} as defined by {@link Util#genericRealTypeConverter()}.
	 */
	@SuppressWarnings("unchecked")
	public IterableRandomAccessibleFunction( final IFunction operation )
	{
		this( operation, ( ( O )Util.findFirstImg( operation ).randomAccess().get() ).createVariable() );
	}

	@Override
	public RandomAccess< O > randomAccess()
	{
		return new Compute( this.operation ).randomAccess( this.computeType, this.outputType, this.outConverter );
	}

	@Override
	public RandomAccess< O > randomAccess( final Interval interval )
	{
		return this.randomAccess();
	}

	@Override
	public O firstElement()
	{
		return this.randomAccess().get();
	}

	@Override
	public Object iterationOrder()
	{
		return Views.iterable( this.firstImg ).iterationOrder();
	}

	@Override
	public long size()
	{
		return Intervals.numElements( this.firstImg );
	}

	@Override
	public Iterator< O > iterator()
	{
		return this.cursor();
	}

	@Override
	public Cursor< O > cursor()
	{
		return new Compute( this.operation ).cursor( this.inConverter, this.computeType, this.outputType, this.outConverter );
	}

	@Override
	public Cursor< O > localizingCursor()
	{
		return this.cursor();
	}
}
