package net.imglib2.algorithm.math.optimizations;

import java.util.Iterator;
import java.util.Map;

import net.imglib2.Localizable;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.math.abstractions.IFunction;
import net.imglib2.algorithm.math.abstractions.IVar;
import net.imglib2.algorithm.math.abstractions.ImgSource;
import net.imglib2.algorithm.math.optimizations.IterableImgSourceDirect;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

public final class IterableImgSourceDirect< I extends RealType< I > > implements ImgSource< I >
{
	private final RandomAccessibleInterval< I > rai;
	private final Iterator< I > it;
	private final RandomAccess< I > ra;

	public IterableImgSourceDirect( final RandomAccessibleInterval< I > rai )
	{
		this.rai = rai;
		this.it = Views.iterable( rai ).iterator();
		this.ra = rai.randomAccess();
	}

	@Override
	public final RealType< ? > eval()
	{
		return this.it.next();
	}

	@Override
	public final RealType< ? > eval( final Localizable loc )
	{
		this.ra.setPosition( loc );
		return this.ra.get();
	}

	@Override
	public IterableImgSourceDirect< I > reInit(
			final RealType< ? > tmp,
			final Map< String, RealType< ? > > bindings,
			final Converter< RealType< ? >, RealType< ? > > converter,
			Map< IVar, IFunction > imgSources )
	{
		return new IterableImgSourceDirect< I >( this.rai );
	}
	
	@Override
	public RandomAccessibleInterval< I > getRandomAccessibleInterval()
	{
		return this.rai;
	}
}
