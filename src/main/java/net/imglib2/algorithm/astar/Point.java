package net.imglib2.algorithm.astar;

public class Point
{

	public static int getX( final long p )
	{
		return ( int ) ( p >>> 32 );
	}

	public static int getY( final long p )
	{
		return ( int ) p;
	}

	public static long toPoint( final int x, final int y )
	{
		return ( ( ( long ) x ) << 32 ) | ( y & 0xFFFFFFFFL );
	}
}
