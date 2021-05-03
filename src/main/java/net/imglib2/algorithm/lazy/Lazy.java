/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2021 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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
package net.imglib2.algorithm.lazy;

import static net.imglib2.type.PrimitiveType.BYTE;
import static net.imglib2.type.PrimitiveType.DOUBLE;
import static net.imglib2.type.PrimitiveType.FLOAT;
import static net.imglib2.type.PrimitiveType.INT;
import static net.imglib2.type.PrimitiveType.LONG;
import static net.imglib2.type.PrimitiveType.SHORT;

import java.util.Set;
import java.util.function.Consumer;

import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.Cache;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.LoadedCellCacheLoader;
import net.imglib2.cache.ref.SoftRefLoaderCache;
import net.imglib2.img.basictypeaccess.AccessFlags;
import net.imglib2.img.basictypeaccess.ArrayDataAccessFactory;
import net.imglib2.img.cell.Cell;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.integer.GenericByteType;
import net.imglib2.type.numeric.integer.GenericIntType;
import net.imglib2.type.numeric.integer.GenericLongType;
import net.imglib2.type.numeric.integer.GenericShortType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

/**
 * Convenience methods to create lazy evaluated cached cell images with
 * consumers.
 *
 * Warning: this class or its functionality could be moved elsewhere in the near
 * future, likely imglib2-cache or imgli2-core.
 *
 * @author Stephan Saalfeld
 */
public class Lazy
{
	private Lazy()
	{}

	/**
	 * Create a memory {@link CachedCellImg} with a cell {@link Cache}.
	 *
	 * @param grid
	 * @param cache
	 * @param type
	 * @param accessFlags
	 * @return
	 */
	@SuppressWarnings( { "unchecked", "rawtypes" } )
	private static < T extends NativeType< T > > CachedCellImg< T, ? > createImg( final CellGrid grid, final Cache< Long, Cell< ? > > cache, final T type, final Set< AccessFlags > accessFlags )
	{
		final CachedCellImg< T, ? > img;

		if ( GenericByteType.class.isInstance( type ) )
		{
			img = new CachedCellImg( grid, type, cache, ArrayDataAccessFactory.get( BYTE, accessFlags ) );
		}
		else if ( GenericShortType.class.isInstance( type ) )
		{
			img = new CachedCellImg( grid, type, cache, ArrayDataAccessFactory.get( SHORT, accessFlags ) );
		}
		else if ( GenericIntType.class.isInstance( type ) )
		{
			img = new CachedCellImg( grid, type, cache, ArrayDataAccessFactory.get( INT, accessFlags ) );
		}
		else if ( GenericLongType.class.isInstance( type ) )
		{
			img = new CachedCellImg( grid, type, cache, ArrayDataAccessFactory.get( LONG, accessFlags ) );
		}
		else if ( FloatType.class.isInstance( type ) )
		{
			img = new CachedCellImg( grid, type, cache, ArrayDataAccessFactory.get( FLOAT, accessFlags ) );
		}
		else if ( DoubleType.class.isInstance( type ) )
		{
			img = new CachedCellImg( grid, type, cache, ArrayDataAccessFactory.get( DOUBLE, accessFlags ) );
		}
		else
		{
			img = null;
		}
		return img;
	}

	/**
	 * Create a memory {@link CachedCellImg} with a {@link CellLoader}.
	 *
	 * @param targetInterval
	 * @param blockSize
	 * @param type
	 * @param accessFlags
	 * @param loader
	 * @return
	 */
	private static < T extends NativeType< T > > CachedCellImg< T, ? > createImg( final Interval targetInterval, final int[] blockSize, final T type, final Set< AccessFlags > accessFlags, final Consumer< RandomAccessibleInterval< T > > loader )
	{
		final long[] dimensions = Intervals.dimensionsAsLongArray( targetInterval );
		final CellGrid grid = new CellGrid( dimensions, blockSize );

		final CellLoader< T > offsetLoader;
		if ( Views.isZeroMin( targetInterval ) )
			offsetLoader = loader::accept;
		else
		{
			final long[] offset = targetInterval.minAsLongArray();
			offsetLoader = cell -> loader.accept( Views.translate( cell, offset ) );
		}

		@SuppressWarnings( { "unchecked", "rawtypes" } )
		final Cache< Long, Cell< ? > > cache = new SoftRefLoaderCache().withLoader( LoadedCellCacheLoader.get( grid, offsetLoader, type, accessFlags ) );

		return createImg( grid, cache, type, accessFlags );
	}

	/**
	 * Create a memory {@link CachedCellImg} with a cell generator
	 * {@link Consumer}.
	 *
	 * @param targetInterval
	 * @param blockSize
	 * @param type
	 * @param accessFlags
	 * @param op
	 * @return
	 */
	public static < T extends NativeType< T > > CachedCellImg< T, ? > generate( final Interval targetInterval, final int[] blockSize, final T type, final Set< AccessFlags > accessFlags, final Consumer< RandomAccessibleInterval< T > > op )
	{
		return createImg( targetInterval, blockSize, type, accessFlags, op );
	}
}
