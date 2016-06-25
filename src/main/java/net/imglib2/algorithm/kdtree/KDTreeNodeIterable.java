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

package net.imglib2.algorithm.kdtree;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;

import net.imglib2.KDTreeNode;

public class KDTreeNodeIterable< T > implements Iterable< KDTreeNode< T > >
{
	private final ArrayList< KDTreeNode< T > > singleNodes;

	private final ArrayList< KDTreeNode< T > > subtrees;

	public KDTreeNodeIterable( final ArrayList< KDTreeNode< T > > singleNodes, final ArrayList< KDTreeNode< T > > subtrees )
	{
		this.singleNodes = singleNodes;
		this.subtrees = subtrees;

	}

	@Override
	public Iterator< KDTreeNode< T > > iterator()
	{
		return new KDTreeNodeIterable.Iter< T >( singleNodes, subtrees );
	}

	private static class Iter< T > implements Iterator< KDTreeNode< T > >
	{
		private final ArrayList< KDTreeNode< T > > nodes;

		private int nextNodeIndex;

		private final ArrayList< KDTreeNode< T > > subtrees;

		private int nextSubtreeIndex;

		private final ArrayDeque< KDTreeNode< T > > stack;

		private Iter( final ArrayList< KDTreeNode< T > > nodes, final ArrayList< KDTreeNode< T > > subtrees )
		{
			this.nodes = nodes;
			this.subtrees = subtrees;
			nextNodeIndex = 0;
			nextSubtreeIndex = 0;
			stack = new ArrayDeque< KDTreeNode< T > >();
		}

		@Override
		public boolean hasNext()
		{
			return !stack.isEmpty() || nextSubtreeIndex < subtrees.size() || nextNodeIndex < nodes.size();
		}

		@Override
		public KDTreeNode< T > next()
		{
			if ( !stack.isEmpty() )
			{
				final KDTreeNode< T > current = stack.pop();
				if ( current.left != null )
					stack.push( current.left );
				if ( current.right != null )
					stack.push( current.right );
				return current;
			}
			else if ( nextSubtreeIndex < subtrees.size() )
			{
				final KDTreeNode< T > current = subtrees.get( nextSubtreeIndex++ );
				if ( current.left != null )
					stack.push( current.left );
				if ( current.right != null )
					stack.push( current.right );
				return current;
			}
			else if ( nextNodeIndex < nodes.size() )
			{
				return nodes.get( nextNodeIndex++ );
			}
			else
				return null;
		}

		@Override
		public void remove()
		{}
	}
}
