package net.imglib2.algorithm.math.execution;

import java.util.Arrays;
import java.util.List;

import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.KDTree;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.Sampler;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.converter.Converter;
import net.imglib2.neighborsearch.NearestNeighborSearchOnKDTree;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

public class KDTreeRadiusSource< I extends RealType< I >, O extends RealType< O > >
extends Point
implements OFunction< O >, RandomAccess< O >, RandomAccessible< O >
{
	private final KDTree< I > kdtree;
	private final NearestNeighborSearchOnKDTree< I > search;
	private final Cursor< O > cursor;
	private final O scrap;
	private final double radius, radius_squared;
	private final I outside;
	private final O outsideO;
	private final Converter< RealType< ? >, O > converter;
	private final Interval interval;
	
	public KDTreeRadiusSource(
			final O scrap,
			final Converter< RealType< ? >, O > converter,
			final KDTree< I > kdtree,
			final double radius,
			final I outside,
			final Interval interval
			)
	{
		super( kdtree.numDimensions() );
		this.scrap = scrap;
		this.converter = converter;
		this.kdtree = kdtree;
		this.radius = radius;
		this.radius_squared = radius * radius;
		this.outside = outside;
		this.search = new NearestNeighborSearchOnKDTree< I >( kdtree );
		this.outsideO = scrap.createVariable();
		converter.convert( outside, this.outsideO );
		this.interval = interval;
		this.cursor = Views.zeroMin( Views.interval( this, interval ) ).cursor();
	}
	
	// Methods from interface RandomAccess
	@Override
	public O get()
	{
		this.search.search( this );
		if ( this.search.getSquareDistance() < this.radius_squared )
		{
			this.converter.convert( this.search.getSampler().get(), this.scrap );
			return this.scrap;
		}
		return this.outsideO;
	}

	@Override
	public Sampler< O > copy()
	{
		return new KDTreeRadiusSource< I, O >( this.scrap.createVariable(), this.converter, this.kdtree, this.radius, this.outside, this.interval );
	}

	@Override
	public RandomAccess< O > copyRandomAccess()
	{
		return new KDTreeRadiusSource< I, O >( this.scrap.createVariable(), this.converter, this.kdtree, this.radius, this.outside, this.interval );
	}
	
	// Methods from interface RandomAccessible
	@Override
	public RandomAccess< O > randomAccess()
	{
		return this;
	}

	@Override
	public RandomAccess< O > randomAccess( final Interval interval )
	{
		return this;
	}
	
	// Methods from interface OFunction
	@Override
	public final O eval()
	{
		return this.cursor.next();
	}

	@Override
	public O eval( final Localizable loc )
	{
		this.setPosition( loc );
		return this.get();
	}
	
	@Override
	public List< OFunction< O > > children()
	{
		return Arrays.asList();
	}
	
	@Override
	public final double evalDouble()
	{
		return this.cursor.next().getRealDouble();
	}
	
	@Override
	public final double evalDouble( final Localizable loc )
	{
		this.setPosition( loc );
		return this.get().getRealDouble();
	}
}