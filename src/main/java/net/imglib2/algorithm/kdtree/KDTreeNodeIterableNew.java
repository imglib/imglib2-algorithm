package net.imglib2.algorithm.kdtree;

import java.util.Iterator;
import net.imglib2.kdtree.KDTree;
import net.imglib2.kdtree.KDTreeNode;

class KDTreeNodeIterableNew< T > implements Iterable< KDTreeNode< T > >
{
	private final NodeIndexIterable nodes;

	private final KDTree< T > tree;

	public KDTreeNodeIterableNew( final NodeIndexIterable nodes, final KDTree< T > tree )
	{
		this.nodes = nodes;
		this.tree = tree;
	}

	@Override
	public Iterator< KDTreeNode< T > > iterator()
	{
		return new KDTreeNodeIterator();
	}

	class KDTreeNodeIterator implements Iterator< KDTreeNode< T > >
	{
		private final NodeIndexIterable.NodeIndexIterator indices = nodes.iterator();
		private final KDTreeNode< T > node = new KDTreeNode<>( tree );

		@Override
		public boolean hasNext()
		{
			return indices.hasNext();
		}

		@Override
		public KDTreeNode< T > next()
		{
			return node.setNodeIndex( indices.next() );
		}
	}
}
