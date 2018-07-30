package net.imglib2.algorithm.math;

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

public class IterableImgSource< I extends RealType< I > > implements ImgSource< I >
{
	private final RandomAccessibleInterval< I > rai;
	private final Iterator< I > it;
	private final RandomAccess< I > ra;
	private Converter< RealType< ? >, RealType< ? > > converter;
	private final RealType< ? > scrap;

	public IterableImgSource( final RandomAccessibleInterval< I > rai )
	{
		this( null, null, rai );
	}
	
	private IterableImgSource( final RealType< ? > scrap, final Converter< RealType< ? >, RealType< ? > > converter, final RandomAccessibleInterval< I > rai )
	{
		this.rai = rai;
		this.it = Views.iterable( rai ).iterator();
		this.ra = rai.randomAccess();
		this.converter = converter;
		this.scrap = scrap;
	}

	@Override
	public RealType< ? > eval()
	{
		this.converter.convert( this.it.next(), this.scrap );
		return this.scrap;
	}

	@Override
	public RealType< ? > eval( final Localizable loc )
	{
		this.ra.setPosition( loc );
		this.converter.convert( this.ra.get(), this.scrap );
		return this.scrap;
	}

	@Override
	public IFunction reInit(
			final RealType< ? > tmp,
			final Map< String, RealType< ? > > bindings,
			final Converter< RealType< ? >, RealType< ? > > converter,
			final Map< IVar, IFunction > imgSources )
	{
		// Optimization: if input image type is the same or a subclass of
		// the output image time (represented here by tmp), then avoid the converter.
		final ImgSource< ? > s;
		if ( tmp.getClass().isAssignableFrom( this.rai.randomAccess().get().getClass() ) )
			s = new IterableImgSourceDirect< I >( this.rai );
		else
			s = new IterableImgSource< I >( tmp.copy(), converter, this.rai );
		
		// Addressing "If" branching: replace with a Var
		if ( null != imgSources )
		{
			final Var var = new Var( tmp.copy(), "#imgSource#" + imgSources.size() );
			imgSources.put( var, s );
			return var;
		}
		
		return ( IFunction )s;
	}
	
	@Override
	public RandomAccessibleInterval< I > getRandomAccessibleInterval()
	{
		return this.rai;
	}
}