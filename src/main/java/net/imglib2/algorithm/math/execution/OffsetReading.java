package net.imglib2.algorithm.math.execution;

import java.util.Arrays;
import java.util.List;

import net.imglib2.Localizable;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.algorithm.math.abstractions.RandomAccessOnly;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

public class OffsetReading< I extends RealType< I >, O extends RealType< O > > implements OFunction< O >, RandomAccessOnly
{
	private final RandomAccessible< I > src;
	protected final RandomAccess< I > ra;
	protected final O scrap;
	private final Converter< RealType< ? >, O > converter;
	
	public OffsetReading( final O scrap,  final Converter< RealType< ? >, O > converter, final RandomAccessible< I > src, final long[] offset )
	{
		this.scrap = scrap;
		this.src = src;
		this.converter = converter;
		this.ra = Views.translate( src, offset ).randomAccess();
	}
	
	@Override
	public final O eval()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public O eval( final Localizable loc )
	{
		this.ra.setPosition( loc );
		this.converter.convert( this.ra.get(), this.scrap );
		return this.scrap;
	}
	
	@Override
	public List< OFunction< O > > children()
	{
		return Arrays.asList();
	}
	
	@Override
	public final double evalDouble()
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public final double evalDouble( final Localizable loc )
	{
		this.ra.setPosition( loc );
		return this.ra.get().getRealDouble();
	}
	
	public RandomAccessible< I > getRandomAccessible()
	{
		return this.src;
	}
}
