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
package net.imglib2.algorithm.math;

import java.util.Map;
import java.util.stream.LongStream;

import net.imglib2.RandomAccessible;
import net.imglib2.algorithm.math.abstractions.IFunction;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.algorithm.math.abstractions.RandomAccessOnly;
import net.imglib2.algorithm.math.abstractions.ViewableFunction;
import net.imglib2.algorithm.math.execution.BlockReadingSource;
import net.imglib2.algorithm.math.execution.BlockReadingDirect;
import net.imglib2.algorithm.math.execution.LetBinding;
import net.imglib2.algorithm.math.execution.Variable;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;

/**
 * Intended for reading cuboid blocks out of an integral image.
 * 
 * @author Albert Cardona
 *
 * @param <I> The {@code Type} of the {@code RandomAccessible} from which to read blocks.
 */
public final class BlockReadSource< I extends RealType< I > > extends ViewableFunction implements IFunction, RandomAccessOnly< I >
{
	final private RandomAccessible< I > src;
	final private long[][] corners;
	final private byte[] signs;
	
	/**
	 * A block centered on a particular pixel.
	 * 
	 * @param src A {@code RandomAccessible} such as an @{code IntegralImg}, presumably a {@code RandomAccessibleInterval} that was extended with an {@code OutOfBounds} strategy.
	 * @param blockRadius Array of half of the length of each side of the block.
	 */
	public BlockReadSource( final RandomAccessible< I > src, final long[] blockRadius )
	{
		this.src = src;
		this.corners = new long[ ( int )Math.pow( 2,  blockRadius.length ) ][ blockRadius.length ];

		// All possible combinations to define all corners, sorted with lower dimensions being the slower-moving,
		// following Gray code (see https://en.wikipedia.org/wiki/Gray_code )
		for (int d = 0; d < src.numDimensions(); ++d )
		{
			final int cycle = corners.length / ( int )Math.pow( 2, d + 1 );
			long inc = blockRadius[ d ];
			for (int i = 0; i < corners.length; ++i )
			{
				if ( 0 == i % cycle) inc *= -1;
				corners[ i ][ d ] = inc;
				//System.out.println("corners[" + i + "][" + d + "] = " + corners[i][d]);
			}
		}
		this.signs = BlockReadSource.signsArray( src );
		//for (int i=0; i<signs.length; ++i)
		//	System.out.println("signs[" + i + "] = " + signs[i]);
	}
	
	/**
	 * A block centered on a particular pixel.
	 * 
	 * @param src A {@code RandomAccessible} such as an @{code IntegralImg}, presumably a {@code RandomAccessibleInterval} that was extended with an {@code OutOfBounds} strategy.
	 * @param blockRadius Half of the length of the side of the block in every dimension.
	 */
	public BlockReadSource( final RandomAccessible< I > src, final long blockRadius )
	{
		this( src, LongStream.generate( () -> blockRadius ).limit( src.numDimensions() ).toArray() );
	}
	
	static public byte[] signsArray( final RandomAccessible< ? > src )
	{
		switch ( src.numDimensions() )
		{
		case 1:
			return new byte[]{ -1, 1 };
		case 2:
			// 2D: S( (x1,y1), (x2,y2) = C( x1,y1 )
			//                          -C( x1,y2 )
			//                          -C( x2,y1 )
			//                          +C( x2,y2 )
			// Corners as: (x1, y1), (x1, y2), (x2, y1), (x2, y2)
			return new byte[]{ 1, -1, -1, 1 };
		case 3:
			// 3D: S( (x1,y1,z1) to (x2,y2,z2) ) =  - C( x1, y1, z1 )
			//                                      + C( x1, y1, z2 )
			//                                      + C( x1, y2, z1 )
			//                                      - C( x1, y2, z2 )
			//                                      + C( x2, y1, z1 )
			//                                      - C( x2, y1, z2 )
			//                                      - C( x2, y2, z1 )
			//                                      + C( x2, y2, z2 )
			return new byte[]{ -1, 1, 1, -1, 1, -1, -1, 1 };
		default:
			// There's a clear pattern, but I can't find the time now to break through it
			// Must re-read Tapias 2011 doi:10.1016/j.patrec.2010.10.007
			// for the use of the Mobius function for determining the sign
			throw new UnsupportedOperationException( "Sorry, numDimensions " + src.numDimensions() + " not supported yet." );
		}
	}
	
	/**
	 * 
	 * @param src
	 * @param corners In coordinate moves relative to a pixel's location.
	 */
	public BlockReadSource( final RandomAccessible< I > src, final long[][] corners )
	{
		this.src = src;
		this.corners = corners;
		this.signs = BlockReadSource.signsArray( src );
	}

	@SuppressWarnings("unchecked")
	@Override
	public < O extends RealType< O > > OFunction< O > reInit(
			final O tmp,
			final Map< String, LetBinding< O > > bindings,
			final Converter< RealType< ? >, O > converter,
			final Map< Variable< O >, OFunction< O > > imgSources )
	{
		if ( tmp.getClass() == src.randomAccess().get().getClass() )
			return new BlockReadingDirect< O >( tmp.copy(), ( RandomAccessible< O > )this.src, this.corners, this.signs );
		return new BlockReadingSource< I, O >(
				tmp.copy(),
				converter,
				this.src,
				this.corners,
				this.signs );
	}
	
	public RandomAccessible< I > getRandomAccessible()
	{
		return this.src;
	}
}
