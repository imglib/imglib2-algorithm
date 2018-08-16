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

package net.imglib2.algorithm.util.unionfind;

import java.util.Arrays;

/**
 *
 * @author Philipp Hanslovsky
 *
 *         Array based implementation of the ranked Union Find algorithm.
 *
 */
public class IntArrayRankedUnionFind implements UnionFind
{
	private final int[] parents;

	private final int[] ranks;

	private int nSets;

	/**
	 *
	 * @param size
	 *            Number of elements. (Initially, each element forms a single
	 *            element subset)
	 */
	public IntArrayRankedUnionFind( final int size )
	{
		this( intRange( new int[ size ] ), new int[ size ], size );
	}

	private IntArrayRankedUnionFind( final int[] parents, final int[] ranks, final int nSets )
	{
		this.parents = parents;
		this.ranks = ranks;
		this.nSets = nSets;
	}

	/**
	 * Find the root node (set identifier) for a specified id.
	 *
	 * @param id
	 * @return Root node (set identifier)
	 */
	public int findRoot( final int id )
	{

		int startIndex1 = id;
		int startIndex2 = id;
		int tmp = id;

		// find root
		while ( startIndex1 != parents[ startIndex1 ] )
		{
			startIndex1 = parents[ startIndex1 ];
		}

		// label all positions on the way to root as parent
		while ( startIndex2 != startIndex1 )
		{
			tmp = parents[ startIndex2 ];
			parents[ startIndex2 ] = startIndex1;
			startIndex2 = tmp;
		}

		return startIndex1;

	}

	/**
	 * Join two sets.
	 *
	 * @param id1
	 *            Root node of the first set.
	 * @param id2
	 *            Root node of the second set.
	 * @return Root node of the union of the two sets.
	 */
	public int join( final int id1, final int id2 )
	{

		if ( id1 == id2 ) { return id1; }

		--nSets;

		final int r1 = ranks[ id1 ];
		final int r2 = ranks[ id2 ];

		if ( r1 < r2 )
		{
			parents[ id1 ] = id2;
			return id2;
		}

		else
		{
			parents[ id2 ] = id1;
			if ( r1 == r2 )
			{
				ranks[ id1 ] = r1 + 1;
			}
			return id1;
		}

	}

	@Override
	public long findRoot( final long id )
	{
		return findRoot( ( int ) id );
	}

	@Override
	public long join( final long id1, final long id2 )
	{
		return join( ( int ) id1, ( int ) id2 );
	}

	@Override
	public long size()
	{
		return intSize();
	}

	@Override
	public long setCount()
	{
		return intSetCount();
	}

	/**
	 *
	 * @return Number of elements.
	 */
	public int intSize()
	{
		return parents.length;
	}

	/**
	 *
	 * @return Number of sets.
	 */
	public int intSetCount()
	{
		return nSets;
	}

	private static int[] intRange( final int[] data )
	{
		return intRange( data, 0 );
	}

	private static int[] intRange( final int[] data, final int offset )
	{
		Arrays.setAll( data, d -> d + offset );
		return data;
	}

}
