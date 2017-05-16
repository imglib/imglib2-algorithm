/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2016 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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
package net.imglib2.algorithm.morphology.table2d;

import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.list.ListImgFactory;
import net.imglib2.type.BooleanType;
import net.imglib2.type.NativeType;
import net.imglib2.util.Util;

/**
 * Thin lines preserving the Euler number using the thinning algorithm # 1
 * described in Guo, "Parallel Thinning with Two Subiteration Algorithms",
 * <i>Communications of the ACM</i>, Vol 32 #3, page 359. The result generally
 * preserves the lines in an image while eroding their thickness.
 * <p>
 * For example:
 * 
 * <pre>
 * 1 1 1 1 1    1 1 1 1 1
 * 1 1 0 1 0    1 1 0 1 0
 * 1 1 1 1 0 -&gt; 1 1 1 0 0
 * 1 1 0 1 0    1 0 0 1 0
 * 1 0 0 0 0    1 0 0 0 0
 * </pre>
 * 
 * Notice that this implementation decides to returns the above result, instead
 * of this following one, which is equivalently valid:
 * 
 * <pre>
 * 1 1 1 1 1
 * 1 1 0 1 0
 * 1 1 1 1 0
 * 1 0 0 0 0
 * 1 0 0 0 0
 * </pre>
 * 
 * @author Lee Kamentsky
 */
public class Thin
{
	public static < T extends BooleanType< T > > Img< T > thin( final Img< T > source )
	{
		return new Thin2().calculate( new Thin1().calculate( source ) );
	}

	@SuppressWarnings( { "unchecked", "rawtypes" } )
	public static < T extends BooleanType< T > > void thin( final RandomAccessible< T > source, final IterableInterval< T > target )
	{
		long[] targetDims = new long[] { target.dimension( 0 ), target.dimension( 1 ) };
		final T extendedVal = target.firstElement().createVariable();
		extendedVal.set( false );

		final ImgFactory< T > factory;
		if ( extendedVal instanceof NativeType )
			factory = ( ImgFactory< T > ) Util.getArrayOrCellImgFactory( target, ( NativeType ) extendedVal );
		else
			factory = new ListImgFactory< T >();
		Img< T > temp = factory.create( targetDims, extendedVal );

		new Thin1().calculate( source, temp );
		new Thin2().calculate( temp, target );
	}

}
