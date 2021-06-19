package net.imglib2.algorithm.astar;

class Node
{

	private static final int F_BITS = 16;

	private static final int F_MASK = Utils.mask( F_BITS );

	private static final int G_BITS = 16;

	private static final int G_MASK = Utils.mask( G_BITS );

	private static final int G_SHIFT = F_BITS;

	private static final long G_F_MASK_COMPLEMENT = ~( ( long ) G_MASK << G_SHIFT | F_MASK );

	private static final int Y_BITS = 16;

	static final int Y_MASK = Utils.mask( Y_BITS );

	private static final int Y_SHIFT = G_SHIFT + G_BITS;

	private static final int X_BITS = 16;

	static final int X_MASK = Utils.mask( X_BITS );

	private static final int X_SHIFT = Y_SHIFT + Y_BITS;

	static long toNode( final int x, final int y, final int g, final int f )
	{
		if ( f < 0 )
			throw new RuntimeException( "Path too long" );
		return ( long ) x << X_SHIFT | ( long ) y << Y_SHIFT | ( long ) g << G_SHIFT | f;
	}

	static int getX( final long l )
	{
		return ( int ) ( l >>> X_SHIFT );
	}

	static int getY( final long l )
	{
		return ( int ) ( l >>> Y_SHIFT & Y_MASK );
	}

	static int getG( final long l )
	{
		return ( int ) ( l >> G_SHIFT & G_MASK );
	}

	static int getF( final long l )
	{
		return ( int ) ( l & F_MASK );
	}

	static long setGF( final long l, final int g, final int f )
	{
		return l & G_F_MASK_COMPLEMENT | ( ( long ) g << G_SHIFT ) | f;
	}
}
