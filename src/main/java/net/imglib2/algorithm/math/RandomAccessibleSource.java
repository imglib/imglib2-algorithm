package net.imglib2.algorithm.math;

import java.util.Map;
import net.imglib2.RandomAccessible;
import net.imglib2.algorithm.math.abstractions.IFunction;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.algorithm.math.abstractions.RandomAccessOnly;
import net.imglib2.algorithm.math.abstractions.ViewableFunction;
import net.imglib2.algorithm.math.execution.RandomAccessibleOffsetSource;
import net.imglib2.algorithm.math.execution.RandomAccessibleOffsetSourceDirect;
import net.imglib2.algorithm.math.execution.LetBinding;
import net.imglib2.algorithm.math.execution.Variable;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;

public final class RandomAccessibleSource< I extends RealType< I > > extends ViewableFunction implements IFunction, RandomAccessOnly
{
	final private RandomAccessible< I > src;
	final private long[] offset;
	
	public RandomAccessibleSource( final RandomAccessible< I > src )
	{
		this.src = src;
		this.offset = new long[ src.numDimensions() ]; // all zeros
	}
	
	public RandomAccessibleSource( final RandomAccessible< I > src, final long[] offset )
	{
		this.src = src;
		this.offset = offset;
	}

	@SuppressWarnings("unchecked")
	@Override
	public < O extends RealType< O > > OFunction< O > reInit(
			final O tmp,
			final Map< String, LetBinding< O > > bindings,
			final Converter< RealType< ? >, O > converter,
			final Map< Variable< O >, OFunction< O > > imgSources )
	{
		if ( tmp.getClass().isAssignableFrom( this.src.randomAccess().get().getClass() ) )
			return new RandomAccessibleOffsetSourceDirect< O >(
					tmp.copy(),
					( RandomAccessible< O > )this.src,
					this.offset );
		return new RandomAccessibleOffsetSource< I, O >(
				tmp.copy(),
				converter,
				this.src,
				this.offset );
	}
	
	public RandomAccessible< I > getRandomAccessible()
	{
		return this.src;
	}
}