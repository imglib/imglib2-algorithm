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
public class IterableRandomAccessibleFunction< O extends RealType< O > >
extends AbstractInterval
implements RandomAccessibleInterval< O >, IterableInterval< O >, View
{
	private final IFunction operation;
	private final RandomAccessibleInterval< ? > firstImg;
	private final O outputType;
	private final Converter< RealType< ? >, O > converter;

	public IterableRandomAccessibleFunction( final IFunction operation, final O outputType, final Converter< RealType< ? >, O > converter )
	{
		super( Util.findFirstImg( operation ) );
		this.operation = operation;
		this.firstImg = Util.findFirstImg( operation ); // Twice: unavoidable
		this.outputType = outputType;
		this.converter = converter;
	}
	
	/**
	 * Use a default {@link Converter} as defined by {@link Util#genericRealTypeConverter()}.
	 */
	public IterableRandomAccessibleFunction( final IFunction operation, final O outputType )
	{
		this( operation, outputType, null );
	}
	
	/**
	 * Use a the same {@link RealType} as the first input {@link RandomAccessibleInterval} found,
	 * and a default {@link Converter} as defined by {@link Util#genericRealTypeConverter()}.
	 */
	@SuppressWarnings("unchecked")
	public IterableRandomAccessibleFunction( final IFunction operation )
	{
		super( Util.findFirstImg( operation ) );
		this.operation = operation;
		this.firstImg = Util.findFirstImg( operation ); // Twice: unavoidable
		this.outputType = ( ( O ) this.firstImg.randomAccess().get() ).createVariable();
		this.converter = null;
	}

	@Override
	public RandomAccess< O > randomAccess()
	{
		return new Compute( this.operation ).randomAccess( this.outputType, this.converter );
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
		return new Compute( this.operation ).cursor( this.outputType, this.converter );
	}

	@Override
	public Cursor< O > localizingCursor()
	{
		return this.cursor();
	}
}
