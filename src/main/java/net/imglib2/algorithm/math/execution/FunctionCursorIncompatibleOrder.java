package net.imglib2.algorithm.math.execution;

import net.imglib2.AbstractCursor;
import net.imglib2.Cursor;
import net.imglib2.algorithm.math.abstractions.IFunction;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;

public class FunctionCursorIncompatibleOrder< C extends RealType< C >, O extends RealType< O > > extends FunctionCursor< C, O > implements Cursor< O >
{
	public FunctionCursorIncompatibleOrder(
			final IFunction operation,
			final Converter< RealType< ? >, C > inConverter,
			final C computeType,
			final O outputType,
			final Converter< C, O > outConverter )
	{
		super( operation, inConverter, computeType, outputType, outConverter );
	}

	@Override
	public void fwd()
	{
		this.scrapC = this.f.eval( this.cursor );
	}
	
	@Override
	public AbstractCursor< O > copy()
	{
		return new FunctionCursorIncompatibleOrder< C, O >( this.operation, this.inConverter, this.scrapC, this.scrapO, this.outConverter );
	}
}
