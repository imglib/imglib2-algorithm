package net.imglib2.algorithm.math;

import java.util.Map;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.math.abstractions.IFunction;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.algorithm.math.abstractions.ViewableFunction;
import net.imglib2.algorithm.math.execution.ImgSourceIterable;
import net.imglib2.algorithm.math.execution.ImgSourceIterableDirect;
import net.imglib2.algorithm.math.execution.Variable;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;

public class ImgSource< I extends RealType< I > > extends ViewableFunction implements IFunction
{
	private final RandomAccessibleInterval< I > rai;

	public ImgSource( final RandomAccessibleInterval< I > rai )
	{
		this.rai = rai;
	}

	@SuppressWarnings("unchecked")
	@Override
	public < O extends RealType< O > > OFunction< O > reInit(
			final O tmp,
			final Map< String, O > bindings,
			final Converter< RealType< ? >, O > converter,
			final Map< Variable< O >, OFunction< O > > imgSources )
	{
		// Optimization: if input image type is the same or a subclass of
		// the output image type (represented here by tmp), then avoid the converter
		// but only if the targetImg is different than this.rai: otherwise, an intermediate
		// computation result holder must be used (the scrap).
		final OFunction< O > s;
		if ( tmp.getClass().isAssignableFrom( this.rai.randomAccess().get().getClass() ) )
			s = new ImgSourceIterableDirect< O >( ( RandomAccessibleInterval< O > )this.rai );
		else
			s = new ImgSourceIterable< I, O >( tmp.copy(), ( Converter< I, O > )converter, this.rai );
		
		// Addressing "If" branching: replace with a Var
		if ( null != imgSources )
		{
			final Variable< O > var = new Variable< O >( "#imgSource#" + imgSources.size(), tmp.copy() );
			imgSources.put( var, s );
			return var;
		}
		
		return s;
	}
	
	public RandomAccessibleInterval< I > getRandomAccessibleInterval()
	{
		return this.rai;
	}
}