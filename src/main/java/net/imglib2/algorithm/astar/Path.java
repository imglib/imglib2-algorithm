package net.imglib2.algorithm.astar;

import static net.imglib2.algorithm.astar.Point.toPoint;

import java.util.Arrays;

public class Path
{

	private long[] ps;

	private int size;

	public Path()
	{
		this.ps = new long[ 8 ];
	}

	void add( final int x, final int y )
	{
		final long p = toPoint( x, y );
		if ( size >= ps.length )
			grow( size + 1 );

		ps[ size ] = p;
		size++;
	}

	void remove()
	{
		size--;
	}

	public long get( final int i )
	{
		assert i >= 0 && i < size;
		return ps[ size - 1 - i ];
	}

	public int size()
	{
		return size;
	}

	public boolean isEmpty()
	{
		return size < 2;
	}

	public void clear()
	{
		size = 0;
	}

	private void grow( final int minCapacity )
	{
		final int oldCapacity = ps.length;
		int newCapacity = oldCapacity + ( ( oldCapacity < 64 ) ? ( oldCapacity + 2 ) : ( oldCapacity >> 1 ) );

		if ( newCapacity < minCapacity )
			newCapacity = minCapacity;

		if ( newCapacity < 0 )
			throw new RuntimeException( "Overflow" );
		ps = Arrays.copyOf( ps, newCapacity );
	}
}
