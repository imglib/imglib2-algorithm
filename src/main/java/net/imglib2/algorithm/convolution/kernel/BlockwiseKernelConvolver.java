/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2026 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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
package net.imglib2.algorithm.convolution.kernel;

import static net.imglib2.type.PrimitiveType.FLOAT;

import java.util.function.Consumer;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.blocks.BlockAlgoUtils;
import net.imglib2.algorithm.blocks.BlockSupplier;
import net.imglib2.algorithm.blocks.ClampType;
import net.imglib2.algorithm.blocks.ComputationType;
import net.imglib2.algorithm.blocks.UnaryBlockOperator;
import net.imglib2.algorithm.blocks.convolve.Convolve;
import net.imglib2.blocks.PrimitiveBlocks;
import net.imglib2.loops.IntervalChunks;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.NativeType;
import net.imglib2.type.PrimitiveType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Cast;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

/**
 * Convolves blocks of lines with {@link Convolve}, i.e., many lines at once.
 * <p>
 * The source block is copied with {@link PrimitiveBlocks}, convolved in float
 * or double precision, and converted to the target type. Like
 * {@link ComputationType#AUTO}, this uses float only if it can represent all
 * values of the target type exactly.
 */
final class BlockwiseKernelConvolver
{
	/**
	 * Maximal number of pixels per block, which bounds the size of the
	 * temporary arrays.
	 */
	private static final long MAX_BLOCK_SIZE = 1 << 16;

	private BlockwiseKernelConvolver()
	{}

	/**
	 * Returns a thread-safe {@link Consumer} that convolves blocks of
	 * {@code target} with {@code kernel} along dimension {@code d}, or
	 * {@code null} if {@code source} cannot be copied with
	 * {@link PrimitiveBlocks} or the source or target type is not supported.
	 */
	static Consumer< Interval > create( final Kernel1D kernel, final RandomAccessible< ? > source, final RandomAccessibleInterval< ? > target, final int d )
	{
		final Object sourceType = source.getType();
		final Object targetType = target.getType();
		if ( !( sourceType instanceof NativeType ) || !( targetType instanceof NativeType ) || !( targetType instanceof RealType ) )
			return null;

		final Kernel1D[] kernels = new Kernel1D[ target.numDimensions() ];
		kernels[ d ] = kernel;
		final BlockSupplier< ? > blocks;
		try
		{
			blocks = convolvedBlocks( source, Cast.unchecked( targetType ), kernels );
		}
		catch ( final IllegalArgumentException e )
		{
			// source cannot be copied with PrimitiveBlocks, or its type is not supported
			return null;
		}

		return block -> {
			final long[] lineStartsMax = block.maxAsLongArray();
			lineStartsMax[ d ] = block.min( d );
			final Interval lineStarts = new FinalInterval( block.minAsLongArray(), lineStartsMax );
			final long numSubBlocks = ( Intervals.numElements( block ) + MAX_BLOCK_SIZE - 1 ) / MAX_BLOCK_SIZE;
			for ( final Interval chunk : IntervalChunks.chunkInterval( lineStarts, ( int ) Math.min( Integer.MAX_VALUE, numSubBlocks ) ) )
			{
				final long[] max = chunk.maxAsLongArray();
				max[ d ] = block.max( d );
				convolveBlock( blocks, new FinalInterval( chunk.minAsLongArray(), max ), target );
			}
		};
	}

	private static < S extends NativeType< S >, T extends NativeType< T > > BlockSupplier< T > convolvedBlocks(
			final RandomAccessible< ? > anySource, final T targetType, final Kernel1D[] kernels )
	{
		final RandomAccessible< S > source = Cast.unchecked( anySource );
		final UnaryBlockOperator< ?, ? > op = processAsFloat( targetType )
				? Convolve.createOperator( new FloatType(), ComputationType.FLOAT, ClampType.NONE, kernels )
				: Convolve.createOperator( new DoubleType(), ComputationType.DOUBLE, ClampType.NONE, kernels );
		final UnaryBlockOperator< S, T > convolve = op
				.adaptSourceType( source.getType(), ClampType.NONE )
				.adaptTargetType( targetType, ClampType.NONE );
		return BlockSupplier.of( source, PrimitiveBlocks.OnFallback.FAIL ).andThen( convolve ).threadSafe();
	}

	/**
	 * Same rule as {@link ComputationType#AUTO} in
	 * {@link Convolve#createOperator}: float if it represents all values of
	 * {@code type} exactly, double otherwise.
	 */
	private static boolean processAsFloat( final NativeType< ? > type )
	{
		final PrimitiveType pt = type.getNativeTypeFactory().getPrimitiveType();
		return pt.equals( FLOAT ) || pt.getByteCount() < FLOAT.getByteCount();
	}

	private static < T extends NativeType< T > > void convolveBlock( final BlockSupplier< T > blocks, final Interval block, final RandomAccessibleInterval< ? > target )
	{
		final RandomAccessibleInterval< T > values = BlockAlgoUtils.arrayImg( blocks, block );
		final RandomAccessibleInterval< T > targetBlock = Views.interval( Cast.unchecked( target ), block );
		// LoopBuilder compiles one loop per lambda class. Separate lambdas keep
		// the JIT profiles of the common target types apart. A shared lambda
		// makes whichever type runs second much slower on Java 8.
		final T type = blocks.getType();
		if ( type instanceof FloatType )
		{
			final RandomAccessibleInterval< FloatType > v = Cast.unchecked( values );
			final RandomAccessibleInterval< FloatType > t = Cast.unchecked( targetBlock );
			LoopBuilder.setImages( v, t ).forEachPixel( ( a, b ) -> b.set( a ) );
		}
		else if ( type instanceof DoubleType )
		{
			final RandomAccessibleInterval< DoubleType > v = Cast.unchecked( values );
			final RandomAccessibleInterval< DoubleType > t = Cast.unchecked( targetBlock );
			LoopBuilder.setImages( v, t ).forEachPixel( ( a, b ) -> b.set( a ) );
		}
		else if ( type instanceof UnsignedByteType )
		{
			final RandomAccessibleInterval< UnsignedByteType > v = Cast.unchecked( values );
			final RandomAccessibleInterval< UnsignedByteType > t = Cast.unchecked( targetBlock );
			LoopBuilder.setImages( v, t ).forEachPixel( ( a, b ) -> b.set( a ) );
		}
		else if ( type instanceof UnsignedShortType )
		{
			final RandomAccessibleInterval< UnsignedShortType > v = Cast.unchecked( values );
			final RandomAccessibleInterval< UnsignedShortType > t = Cast.unchecked( targetBlock );
			LoopBuilder.setImages( v, t ).forEachPixel( ( a, b ) -> b.set( a ) );
		}
		else
		{
			LoopBuilder.setImages( values, targetBlock ).forEachPixel( ( a, b ) -> b.set( a ) );
		}
	}
}
