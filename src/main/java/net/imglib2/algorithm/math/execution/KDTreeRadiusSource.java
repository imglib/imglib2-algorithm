/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2025 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
 * John Bogovic, Albert Cardona, Barry DeZonia, Christian Dietz, Jan Funke,
 * Aivar Grislis, Jonathan Hale, Grant Harris, Stefan Helfrich, Mark Hiner,
 * Martin Horn, Steffen Jaensch, Lee Kamentsky, Larry Lindsey, Melissa Linkert,
 * Mark Longair, Brian Northan, Nick Perry, Curtis Rueden, Johannes Schindelin,
 * Jean-Yves Tinevez and Michael Zinsmaier.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
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
	public O getType()
	{
		return this.scrap;
	}

	@Override
	public RandomAccess< O > copy()
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
