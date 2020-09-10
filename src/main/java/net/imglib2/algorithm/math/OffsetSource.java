package net.imglib2.algorithm.math;

import java.util.Map;

import net.imglib2.RandomAccessible;
import net.imglib2.algorithm.math.abstractions.IFunction;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.algorithm.math.abstractions.RandomAccessOnly;
import net.imglib2.algorithm.math.abstractions.ViewableFunction;
import net.imglib2.algorithm.math.execution.IterableRandomAccessibleFunction;
import net.imglib2.algorithm.math.execution.LetBinding;
import net.imglib2.algorithm.math.execution.RandomAccessibleOffsetSourceDirect;
import net.imglib2.algorithm.math.execution.Variable;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;

public class OffsetSource< I extends RealType< I > > extends ViewableFunction implements RandomAccessOnly< I >
{
	private final IFunction f;
	private final long[] offset;
	
	public OffsetSource( final IFunction f, final long[] offset )
	{
		this.f = f;
		this.offset = offset;
	}
	
	@Override
	public < O extends RealType< O > > OFunction< O > reInit(
			final O tmp,
			final Map< String, LetBinding< O > > bindings,
			final Converter< RealType< ? >, O > converter,
			final Map< Variable< O >, OFunction< O > > imgSources )
	{
		return new RandomAccessibleOffsetSourceDirect< O >(
				tmp.copy(),
				new IterableRandomAccessibleFunction< O, O >( this.f, converter,  tmp.createVariable(), tmp.createVariable(), null ),
				this.offset );
	}

	@Override
	public RandomAccessible< I > getRandomAccessible()
	{
		return this.view();
	}
}
