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
	 * @param interval Necessary only when there aren't any intervals already as inputs to the operations, or to crop
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
