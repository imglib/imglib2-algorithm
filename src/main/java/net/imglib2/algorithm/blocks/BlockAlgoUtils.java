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

import net.imglib2.blocks.PrimitiveBlocks;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.ReadOnlyCachedCellImgFactory;
import net.imglib2.cache.img.ReadOnlyCachedCellImgOptions;
import net.imglib2.type.NativeType;

/**
 * Utilities for applying block algorithms to images.
 * <p>
 * For example, {@link #cellImg(BlockSupplier, long[], int[]) cellImg(...)}
 * creates a {@code CachedCellImg} which computes cells using a given {@code
 * BlockSupplier}.
 */
public class BlockAlgoUtils
{
	public static < T extends NativeType< T > >
	CellLoader< T > cellLoader( final BlockSupplier< T > blocks )
	{
		final BlockSupplier< T > ts = blocks.threadSafe();
		return cell -> ts.copy( cell, cell.getStorageArray() );
	}

	/**
	 * Creates a {@code CachedCellImg} which copies cells from the specified
	 * {@code blocks}.
	 *
	 * @param blocks
	 * 		copies blocks from source data
	 * @param dimensions
	 * 		dimensions of the {@code CachedCellImg} to create
	 * @param cellDimensions
	 * 		block size of the {@code CachedCellImg} to create
	 * @param <T>
	 * 		target type (type of the returned CachedCellImg)
	 *
	 * @return a {@code CachedCellImg} which copies cells from {@code blocks}.
	 */
	public static < T extends NativeType< T > >
	CachedCellImg< T, ? > cellImg(
			final BlockSupplier< T > blocks,
			final long[] dimensions,
			final int[] cellDimensions )
	{
		return new ReadOnlyCachedCellImgFactory().create(
				dimensions,
				blocks.getType(),
				cellLoader( blocks ),
				ReadOnlyCachedCellImgOptions.options().cellDimensions( cellDimensions ) );
	}
}
