package net.imglib2.algorithm.math.execution;

import java.util.Arrays;
import java.util.List;

import net.imglib2.Cursor;
import net.imglib2.Localizable;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.math.abstractions.IImgSourceIterable;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

public class ImgSourceIterable< I extends RealType< I >, O extends RealType< O > > implements OFunction< O >, IImgSourceIterable
{
	private final RandomAccessibleInterval< I > rai;
	private final Cursor< I > it;
	private final RandomAccess< I > ra;
	private Converter< I, O > converter;
	private final O scrap;
	
	public ImgSourceIterable( final O scrap, final Converter< I, O > converter, final RandomAccessibleInterval< I > rai )
	{
		this.rai = rai;
		this.it = Views.iterable( rai ).cursor();
		this.ra = rai.randomAccess();
		this.converter = converter;
		this.scrap = scrap;
	}
	
	@Override
	public final O eval()
	{
		this.converter.convert( this.it.next(), this.scrap );
		return this.scrap;
	}

	@Override
	public final O eval( final Localizable loc )
	{
		this.ra.setPosition( loc );
		this.converter.convert( this.ra.get(), this.scrap );
		return this.scrap;
	}
	
	public RandomAccessibleInterval< I > getRandomAccessibleInterval()
	{
		return this.rai;
	}

	@Override
	public boolean hasNext()
	{
		return this.it.hasNext();
	}

	@Override
	public Localizable localizable()
	{
		return this.it;
	}

	@Override
	public void localize(long[] position)
	{
		this.it.localize( position );
	}
	
	@Override
	public List< OFunction< O > > children()
	{
		return Arrays.asList();
	}
}
