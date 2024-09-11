/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2024 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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
package net.imglib2.algorithm.blocks;

import net.imglib2.blocks.SubArrayCopy;
import net.imglib2.blocks.TempArray;
import net.imglib2.type.NativeType;
import net.imglib2.type.PrimitiveType;
import net.imglib2.util.Cast;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;

/**
 * {@code TilingBlockSupplier} wraps a source {@code BlockSupplier}, and splits
 * large {@link BlockSupplier#copy} requests into several smaller {@code copy}
 * calls on the source {@code BlockSupplier}.
 * <p>
 * Each {@code copy} on the source {@code BlockSupplier} requests (at most) an
 * interval of the {@code tileSize} specified in the constructor. The source
 * {@code BlockSupplier.copy} writes to a temporary buffer, which is then copied
 * into the appropriate portion of the {@code dest} buffer.
 * <p>
 * {@link BlockSupplier#copy} requests that are smaller or equal to {@code
 * tileSize} are passed directly to the source {@code BlockSupplier}.
 * <p>
 * Example use cases:
 * <ul>
 * <li>Computing large outputs (e.g. to write to N5 or wrap as {@code ArrayImg})
 * with operators that have better performance with smaller block sizes.</li>
 * <li>Avoiding excessively large blocks when chaining downsampling
 * operators.</li>
 * </ul>
 *
 * @param <T>
 * 		pixel type
 * @param <P>
 * 		corresponding primitive array type
 */
class TilingBlockSupplier< T extends NativeType< T >, P > extends AbstractBlockSupplier< T >
{
	private final BlockSupplier< T > p0;

	private final int[] innerTileSize;
	private final int[] borderTileSize;
	private final int[] numTiles;

	private final TempArray< P > tempArray;
	private final int innerTileNumElements;

	private final SubArrayCopy.Typed< P, P > subArrayCopy;

	final int[] tile_pos_in_dest;
	final long[] tile_pos_in_src;
	final int[] tile_origin;
	final int[] tile_size;

	/**
	 * Create a {@code BlockSupplier} that handles {@link BlockSupplier#copy}
	 * requests by splitting into {@code tileSize} portions that are each
	 * handled by the given {@code srcSupplier} and assembled into the final
	 * result.
	 *
	 * @param srcSupplier
	 * 		source {@code BlockSupplier} to wrap.
	 * @param tileSize
	 * 		(maximum) dimensions of a request to the {@code srcSupplier}.
	 *      {@code tileSize} is expanded or truncated to the necessary size.
	 * 		For example, if {@code tileSize=={64}} when wrapping a 3D {@code
	 * 		srcSupplier}, {@code tileSize} is expanded to {@code {64, 64,
	 * 		64}}.
	 */
	public TilingBlockSupplier(
			final BlockSupplier< T > srcSupplier,
			final int... tileSize )
	{
		this.p0 = srcSupplier;
		final int n = srcSupplier.numDimensions();
		innerTileSize = Util.expandArray( tileSize, n );
		innerTileNumElements = Util.safeInt( Intervals.numElements( innerTileSize ) );
		borderTileSize = new int[ n ];
		numTiles = new int[ n ];

		final PrimitiveType primitiveType = srcSupplier.getType().getNativeTypeFactory().getPrimitiveType();
		tempArray = TempArray.forPrimitiveType( primitiveType );
		subArrayCopy = SubArrayCopy.forPrimitiveType( primitiveType );

		tile_pos_in_dest = new int[ n ];
		tile_pos_in_src = new long[ n ];
		tile_origin = new int[ n ];
		tile_size = new int[ n ];
	}

	private TilingBlockSupplier( final TilingBlockSupplier< T, P > s )
	{
		p0 = s.p0.independentCopy();
		innerTileSize = s.innerTileSize;
		innerTileNumElements = s.innerTileNumElements;
		tempArray = s.tempArray.newInstance();
		subArrayCopy = s.subArrayCopy;

		final int n = numDimensions();
		borderTileSize = new int[ n ];
		numTiles = new int[ n ];
		tile_pos_in_dest = new int[ n ];
		tile_pos_in_src = new long[ n ];
		tile_origin = new int[ n ];
		tile_size = new int[ n ];
	}

	@Override
	public T getType()
	{
		return p0.getType();
	}

	@Override
	public int numDimensions()
	{
		return p0.numDimensions();
	}

	@Override
	public void copy( final long[] srcPos, final Object dest, final int[] size )
	{
		final int n = numDimensions();
		boolean singleTile = true;
		for ( int d = 0; d < n; ++d )
		{
			numTiles[ d ] = ( size[ d ] - 1 ) / innerTileSize[ d ] + 1;
			if ( numTiles[ d ] > 1 )
				singleTile = false;
			borderTileSize[ d ] = size[ d ] - ( numTiles[ d ] - 1 ) * innerTileSize[ d ];
		}
		if ( singleTile )
		{
			p0.copy( srcPos, dest, size );
		}
		else
		{
			final P tile_buf = tempArray.get( innerTileNumElements );
			compute_tiles_recursively( n - 1, srcPos, Cast.unchecked( dest ), size, tile_buf );
		}
	}

	private void compute_tiles_recursively( final int d, final long[] srcPos, final P dest, final int[] dest_size, final P tile_buf ) {
		final int numTiles = this.numTiles[ d ];
		for ( int i = 0; i < numTiles; ++i )
		{
			tile_pos_in_dest[ d ] = innerTileSize[ d ] * i;
			tile_pos_in_src[ d ] = srcPos[ d ] + tile_pos_in_dest[ d ];
			tile_size[ d ] = ( i == numTiles - 1 ) ? borderTileSize[ d ] : innerTileSize[ d ];
			if ( d == 0 )
			{
				p0.copy( tile_pos_in_src, tile_buf, tile_size );
				subArrayCopy.copy( tile_buf, tile_size, tile_origin, dest, dest_size, tile_pos_in_dest, tile_size );
			}
			else
			{
				compute_tiles_recursively( d - 1, srcPos, dest, dest_size, tile_buf );
			}
		}
	}

	@Override
	public BlockSupplier< T > independentCopy()
	{
		return new TilingBlockSupplier<>( this );
	}
}
