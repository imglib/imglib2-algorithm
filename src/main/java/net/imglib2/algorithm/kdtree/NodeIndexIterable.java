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
