package net.imglib2.algorithm.math;

import java.util.Map;

import net.imglib2.algorithm.math.abstractions.IFunction;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.algorithm.math.execution.IterableRandomAccessibleFunction;
import net.imglib2.algorithm.math.execution.IterableRandomAccessibleFunctionDouble;
import net.imglib2.algorithm.math.execution.LetBinding;
import net.imglib2.algorithm.math.execution.NumericSource;
import net.imglib2.algorithm.math.execution.Variable;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;

public final class NumberSource implements IFunction
{
	private final Number number;

	public NumberSource( final Number number ) {
		this.number = number;
	}

	@Override
	public < O extends RealType< O > > NumericSource< O > reInit(
			final O tmp,
			final Map< String, LetBinding< O > > bindings,
			final Converter< RealType< ? >, O > converter,
			final Map< Variable< O >, OFunction< O > > imgSources )
	{
		return new NumericSource< O >( tmp.copy(), this.number );
	}

	@Override
	public < O extends RealType< O > > IterableRandomAccessibleFunction< O > view()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public < O extends RealType< O > > IterableRandomAccessibleFunction< O > view( final O outputType )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public < O extends RealType< O > > IterableRandomAccessibleFunction< O > view( final O outputType, final Converter< RealType< ? >, O > converter )
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public < O extends RealType< O > > IterableRandomAccessibleFunctionDouble< O > viewDouble()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public < O extends RealType< O > > IterableRandomAccessibleFunctionDouble< O > viewDouble( final O outputType )
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public < O extends RealType< O > > IterableRandomAccessibleFunctionDouble< O > viewDouble( final O outputType, final Converter< RealType< ? >, O > converter )
	{
		throw new UnsupportedOperationException();
	}
}