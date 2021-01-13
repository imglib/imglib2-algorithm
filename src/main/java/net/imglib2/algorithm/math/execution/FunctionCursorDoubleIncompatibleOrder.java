package net.imglib2.algorithm.math.execution;

import net.imglib2.AbstractCursor;
import net.imglib2.Cursor;
import net.imglib2.algorithm.math.abstractions.IFunction;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;

public class FunctionCursorDoubleIncompatibleOrder< O extends RealType< O > > extends FunctionCursor< O, O > implements Cursor< O >
{
	public FunctionCursorDoubleIncompatibleOrder( final IFunction operation, final O outputType, final Converter< RealType< ? >, O > converter )
	{
		super( operation, null, outputType.createVariable(), outputType, ( Converter< O, O > )converter );
	}

	@Override
	public void fwd()
	{
		this.scrapC.setReal( this.f.evalDouble( this.cursor ) );
	}
	
	@Override
	public AbstractCursor< O > copy()
	{
		return new FunctionCursorDoubleIncompatibleOrder< O >( this.operation, this.scrapO, ( Converter< RealType< ? >, O > )this.outConverter );
	}
}