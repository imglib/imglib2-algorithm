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
import net.imglib2.type.BooleanType;

/**
 * Applies the interaction rules from the Game of Life, an example of a cellular
 * automaton.
 * <p>
 * More specifically:
 * 
 * <pre>
 * 1. If a pixel has less than 2 neighbors are true, set to false.
 * 2. If a pixel has 2 or 3 neighbors are true, do not change.
 * 3. If a pixel has more than 3 neighbors are true, set to false.
 * </pre>
 * 
 * @author Lee Kamentsky
 */
public class Life extends Abstract3x3TableOperation
{
	@Override
	protected boolean[] getTable()
	{
		return table;
	}

	@Override
	protected boolean getExtendedValue()
	{
		return false;
	}

	public static < T extends BooleanType< T > > Img< T > life( final Img< T > source )
	{
		return new Life().calculate( source );
	}

	public static < T extends BooleanType< T > > void life( final RandomAccessible< T > source, final IterableInterval< T > target )
	{
		new Life().calculate( source, target );
	}

	private static final boolean[] table = {
			false, false, false, false, false, false, false, true,
			false, false, false, true, false, true, true, false,
			false, false, false, true, false, true, true, true,
			false, true, true, true, true, true, true, false,
			false, false, false, true, false, true, true, false,
			false, true, true, false, true, false, false, false,
			false, true, true, true, true, true, true, false,
			true, true, true, false, true, false, false, false,
			false, false, false, true, false, true, true, false,
			false, true, true, false, true, false, false, false,
			false, true, true, true, true, true, true, false,
			true, true, true, false, true, false, false, false,
			false, true, true, false, true, false, false, false,
			true, false, false, false, false, false, false, false,
			true, true, true, false, true, false, false, false,
			true, false, false, false, false, false, false, false,
			false, false, false, true, false, true, true, false,
			false, true, true, false, true, false, false, false,
			false, true, true, true, true, true, true, false,
			true, true, true, false, true, false, false, false,
			false, true, true, false, true, false, false, false,
			true, false, false, false, false, false, false, false,
			true, true, true, false, true, false, false, false,
			true, false, false, false, false, false, false, false,
			false, true, true, false, true, false, false, false,
			true, false, false, false, false, false, false, false,
			true, true, true, false, true, false, false, false,
			true, false, false, false, false, false, false, false,
			true, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			true, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, true, false, true, true, false,
			false, true, true, false, true, false, false, false,
			false, true, true, true, true, true, true, false,
			true, true, true, false, true, false, false, false,
			false, true, true, false, true, false, false, false,
			true, false, false, false, false, false, false, false,
			true, true, true, false, true, false, false, false,
			true, false, false, false, false, false, false, false,
			false, true, true, false, true, false, false, false,
			true, false, false, false, false, false, false, false,
			true, true, true, false, true, false, false, false,
			true, false, false, false, false, false, false, false,
			true, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			true, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, true, true, false, true, false, false, false,
			true, false, false, false, false, false, false, false,
			true, true, true, false, true, false, false, false,
			true, false, false, false, false, false, false, false,
			true, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			true, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			true, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			true, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false
	};
}
