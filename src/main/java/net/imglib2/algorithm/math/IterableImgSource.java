package net.imglib2.algorithm.math;

import java.util.Iterator;

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
		this.rai = rai;
		this.it = Views.iterable( rai ).iterator();
		this.ra = rai.randomAccess();
	}

	@Override
	public void eval( final RealType< ? > output ) {
		this.converter.convert( this.it.next(), output );
	}

	@Override
	public void eval( final RealType< ? > output, final Localizable loc ) {
		this.ra.setPosition( loc );
		this.converter.convert( this.ra.get(), output );
	}

	@Override
	public IterableImgSource< I > copy()
	{
		return new IterableImgSource< I >( this.rai );
	}

	@Override
	public void setScrap( final RealType< ? > output) {}
	
	public void setConverter( final Converter< RealType< ? >, RealType< ? > > converter ) {
		this.converter = converter;
	}
}