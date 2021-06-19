package net.imglib2.algorithm.astar;

import static net.imglib2.algorithm.astar.Grid.MAX_OPEN_NODE_SIZE;
import static net.imglib2.algorithm.astar.Node.getF;
import static net.imglib2.algorithm.astar.Node.getX;
import static net.imglib2.algorithm.astar.Node.getY;
import static net.imglib2.algorithm.astar.Node.toNode;

import java.util.Arrays;

class Nodes
{
	Grid map;

	private long[] nodes;

	private int size;

	Nodes()
	{
		this.nodes = new long[ 16 ];
	}

	void open( final int x, final int y, final int g, final int h, final int pd )
	{
		if ( size >= MAX_OPEN_NODE_SIZE )
			throw new RuntimeException( "TooManyOpenNodes! max: " + MAX_OPEN_NODE_SIZE );

		if ( size >= nodes.length )
			grow( size + 1 );

		final long node = node( x, y, g, h, pd );
		siftUp( size, node );
		size++;
	}

	long close()
	{
		if ( size == 0 )
			return 0;

		final long r = nodes[ 0 ];
		size--;
		if ( size > 0 )
		{
			final long n = nodes[ size ];
			siftDown( 0, n );
		}
		map.nodeClosed( getX( r ), getY( r ) );
		return r;
	}

	long getOpenNode( final int i )
	{
		assert i >= 0 && i < size;
		return nodes[ i ];
	}

	void openNodeParentChanged( final long n, final int idx, final int pd )
	{
		siftUp( idx, n );
		map.nodeParentDirectionUpdate( getX( n ), getY( n ), pd );
	}

	void clear()
	{
		size = 0;
		map.clear();
		map = null;
	}

	boolean isClean()
	{
		return size == 0;
	}

	private static final int HEAP_SHIFT = 2;

	private void siftUp( int i, final long n )
	{
		final int nf = getF( n );
		while ( i > 0 )
		{
			final int pi = ( i - 1 ) >>> HEAP_SHIFT;
			final long p = nodes[ pi ];
			if ( nf >= getF( p ) )
				break;

			setNode( i, p );
			i = pi;
		}
		setNode( i, n );
	}

	private void siftDown( int i, final long n )
	{
		final int nf = getF( n );
		while ( i < size )
		{
			// 找children中最小的
			int ci = ( i << HEAP_SHIFT ) + 1;
			if ( ci >= size )
				break;

			long c = nodes[ ci ];

			int cj = ci + 1;
			if ( cj < size )
			{
				if ( getF( nodes[ cj ] ) < getF( c ) )
					c = nodes[ ci = cj ];

				if ( ++cj < size )
				{
					if ( getF( nodes[ cj ] ) < getF( c ) )
						c = nodes[ ci = cj ];

					if ( ++cj < size )
					{
						if ( getF( nodes[ cj ] ) < getF( c ) )
							c = nodes[ ci = cj ];
					}
				}
			}

			if ( nf <= getF( c ) )
				break;

			setNode( i, c );
			i = ci;
		}
		setNode( i, n );
	}

	private void setNode( final int i, final long n )
	{
		nodes[ i ] = n;
		map.openNodeIdxUpdate( getX( n ), getY( n ), i );
	}

	private long node( final int x, final int y, final int g, final int h, final int pd )
	{
		final long node = toNode( x, y, g, g + h );
		map.nodeParentDirectionUpdate( x, y, pd );
		return node;
	}

	private void grow( final int minCapacity )
	{
		final int oldCapacity = nodes.length;
		int newCapacity = oldCapacity + ( ( oldCapacity < 64 ) ? ( oldCapacity + 2 ) : ( oldCapacity >> 1 ) );

		if ( newCapacity < minCapacity )
			newCapacity = minCapacity;

		if ( newCapacity < 0 )
		{ throw new RuntimeException( "Overflow" ); }
		nodes = Arrays.copyOf( nodes, newCapacity );
	}
}
