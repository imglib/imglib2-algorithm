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
import java.util.function.LongBinaryOperator;

import gnu.trove.map.hash.TLongLongHashMap;

/**
 *
 * @author Philipp Hanslovsky
 *
 *
 */
public class LongHashMapUnionFind implements UnionFind
{
	private final TLongLongHashMap parents;

	private int nSets;

	private final LongBinaryOperator comparator;

	public LongHashMapUnionFind()
	{
		this( new TLongLongHashMap(), 0, Long::compare );
	}

	/**
	 *
	 *
	 * @param parents
	 *            assignments from id to representative of set
	 * @param nSets
	 *            number of sets in {@code parents}. This should be identical
	 *            with {@code parents.valueCollection().size()}.
	 * @param comparator
	 *            When joining to sets, the new representative id will be
	 *            determined by comparator:
	 *            {@code id = comparator.comparator(id1, id2) < 0 ? id1 : id2}
	 */
	public LongHashMapUnionFind( final TLongLongHashMap parents, final int nSets, final LongBinaryOperator comparator )
	{
		this.parents = parents;
		this.nSets = nSets;
		this.comparator = comparator;
	}

	@Override
	public long findRoot( final long id )
	{
		if ( !this.parents.contains( id ) )
		{
			this.parents.put( id, id );
			++nSets;
			return id;
		}

		long startIndex1 = id;
		long startIndex2 = id;
		long tmp = id;

		// find root
		while ( startIndex1 != parents.get( startIndex1 ) )
		{
			startIndex1 = parents.get( startIndex1 );
		}

		// label all positions on the way to root as parent
		while ( startIndex2 != startIndex1 )
		{
			tmp = parents.get( startIndex2 );
			parents.put( startIndex2, startIndex1 );
			startIndex2 = tmp;
		}

		return startIndex1;
	}

	@Override
	public long join( final long id1, final long id2 )
	{

		if ( !parents.contains( id1 ) )
		{
			parents.put( id1, id1 );
			++nSets;
		}

		if ( !parents.contains( id2 ) )
		{
			parents.put( id2, id2 );
			++nSets;
		}

		if ( id1 == id2 )
		{
			// assert this.parents.contains( id1 ) && this.parents.contains( id2
			// );
			return id1;
		}

		--nSets;

		if ( comparator.applyAsLong( id1, id2 ) < 0 )
		{
			parents.put( id2, id1 );
			return id1;
		}

		else
		{
			parents.put( id1, id2 );
			return id2;
		}

	}

	@Override
	public long size()
	{
		return parents.size();
	};

	@Override
	public long setCount()
	{
		return nSets;
	}

	@Override
	public LongHashMapUnionFind clone()
	{
		return new LongHashMapUnionFind( new TLongLongHashMap( this.parents ), nSets, comparator );
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
