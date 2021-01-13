package net.imglib2.algorithm.math.execution;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.algorithm.math.Compute;
import net.imglib2.algorithm.math.abstractions.IFunction;
import net.imglib2.algorithm.math.abstractions.Util;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;

public class IterableRandomAccessibleFunctionDouble< O extends RealType< O >> extends IterableRandomAccessibleFunction< O, O >
{
	public IterableRandomAccessibleFunctionDouble( final IFunction operation, final O outputType, final Converter< RealType< ? >, O > converter )
	{
		super( operation, null, outputType.createVariable(), outputType, ( Converter< O, O > )converter );
	}
	
	/**
	 * Use a default {@link Converter} as defined by {@link Util#genericRealTypeConverter()}.
	 */
	public IterableRandomAccessibleFunctionDouble( final IFunction operation, final O outputType )
	{
		this( operation, outputType, null );
	}
	
	public IterableRandomAccessibleFunctionDouble( final IFunction operation )
	{
		super( operation );
	}
	
	@Override
	public RandomAccess< O > randomAccess()
	{
		return new Compute( this.operation ).randomAccessDouble( this.outputType, ( Converter< RealType< ? >, O > )this.outConverter );
	}

	@Override
	public Cursor< O > cursor()
	{
		return new Compute( this.operation ).cursorDouble( this.outputType, ( Converter< RealType< ? >, O > )this.outConverter );
	}
}
