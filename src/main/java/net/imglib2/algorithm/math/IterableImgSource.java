package net.imglib2.algorithm.math;

import java.util.Iterator;
import java.util.Map;

import net.imglib2.Localizable;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.math.abstractions.IFunction;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

public class IterableImgSource< I extends RealType< I > > implements IFunction
{
	final RandomAccessibleInterval< I > rai;
	private final Iterator< I > it;
	private final RandomAccess< I > ra;
	private Converter< RealType< ? >, RealType< ? > > converter;

	public IterableImgSource( final RandomAccessibleInterval< I > rai )
	{
		this( null, rai );
	}
	
	private IterableImgSource( final Converter< RealType< ? >, RealType< ? > > converter, final RandomAccessibleInterval< I > rai )
	{
		this.rai = rai;
		this.it = Views.iterable( rai ).iterator();
		this.ra = rai.randomAccess();
		this.converter = converter;
	}

	@Override
	public void eval( final RealType< ? > output )
	{
		this.converter.convert( this.it.next(), output );
	}

	@Override
	public void eval( final RealType< ? > output, final Localizable loc )
	{
		this.ra.setPosition( loc );
		this.converter.convert( this.ra.get(), output );
	}

	@Override
	public IterableImgSource< I > reInit( final RealType<?> tmp, final Map<String, RealType<?>> bindings, final Converter<RealType<?>, RealType<?>> converter )
	{
		return new IterableImgSource< I >( converter, this.rai );
	}
}