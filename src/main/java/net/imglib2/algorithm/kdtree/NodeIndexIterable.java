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
package net.imglib2.algorithm.kdtree;

import gnu.trove.list.array.TIntArrayList;
import java.util.NoSuchElementException;
import net.imglib2.kdtree.KDTreeImpl;

class NodeIndexIterable
{
	private final TIntArrayList nodes;

	private final TIntArrayList subtrees;

	private final KDTreeImpl tree;

	NodeIndexIterable( final TIntArrayList singleNodes, final TIntArrayList subtrees, final KDTreeImpl tree )
	{
		this.nodes = singleNodes;
		this.subtrees = subtrees;
		this.tree = tree;
	}

	NodeIndexIterator iterator()
	{
		return new NodeIndexIterator();
	}

	class NodeIndexIterator
	{
		private int nextNodeIndex;

		private int nextSubtreeIndex;

		private final IntStack stack;

		NodeIndexIterator()
		{
			nextNodeIndex = 0;
			nextSubtreeIndex = 0;
			stack = new IntStack(tree.depth() + 1 );
		}

		int next()
		{
			if ( !stack.isEmpty() )
				return pushChildren( stack.pop() );
			else if ( nextSubtreeIndex < subtrees.size() )
				return pushChildren( subtrees.get( nextSubtreeIndex++ ) );
			else if ( nextNodeIndex < nodes.size() )
				return nodes.get( nextNodeIndex++ );
			else
				throw new NoSuchElementException();
		}

		boolean hasNext()
		{
			return !stack.isEmpty() || nextSubtreeIndex < subtrees.size() || nextNodeIndex < nodes.size();
		}

		// returns node
		private int pushChildren( final int node )
		{
			final int left = tree.left( node );
			final int right = tree.right( node );
			if ( left >= 0 )
				stack.push( left );
			if ( right >= 0 )
				stack.push( right );
			return node;
		}
	}

	private static class IntStack
	{
		private final int[] elements;

		private int ptr;

		IntStack( final int size )
		{
			elements = new int[ size ];
			ptr = 0;
		}

		void push( final int element )
		{
			elements[ ptr++ ] = element;
		}

		int pop()
		{
			return elements[ --ptr ];
		}

		boolean isEmpty()
		{
			return ptr == 0;
		}
	}
}
