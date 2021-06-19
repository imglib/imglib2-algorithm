package net.imglib2.algorithm.astar;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static net.imglib2.algorithm.astar.Cost.COST_DIAGONAL;
import static net.imglib2.algorithm.astar.Cost.COST_ORTHOGONAL;
import static net.imglib2.algorithm.astar.Cost.hCost;
import static net.imglib2.algorithm.astar.Grid.DIRECTION_DOWN;
import static net.imglib2.algorithm.astar.Grid.DIRECTION_LEFT;
import static net.imglib2.algorithm.astar.Grid.DIRECTION_LEFT_DOWN;
import static net.imglib2.algorithm.astar.Grid.DIRECTION_LEFT_UP;
import static net.imglib2.algorithm.astar.Grid.DIRECTION_RIGHT;
import static net.imglib2.algorithm.astar.Grid.DIRECTION_RIGHT_DOWN;
import static net.imglib2.algorithm.astar.Grid.DIRECTION_RIGHT_UP;
import static net.imglib2.algorithm.astar.Grid.DIRECTION_UP;
import static net.imglib2.algorithm.astar.Grid.isClosedNode;
import static net.imglib2.algorithm.astar.Grid.isNullNode;
import static net.imglib2.algorithm.astar.Grid.isUnwalkable;
import static net.imglib2.algorithm.astar.Grid.openNodeIdx;
import static net.imglib2.algorithm.astar.Node.getF;
import static net.imglib2.algorithm.astar.Node.getG;
import static net.imglib2.algorithm.astar.Node.getX;
import static net.imglib2.algorithm.astar.Node.getY;
import static net.imglib2.algorithm.astar.Node.setGF;
import static net.imglib2.algorithm.astar.Reachability.isReachable;

public class AStar
{

	private final Nodes nodes;

	public AStar()
	{
		this.nodes = new Nodes();
	}

	public Path search( final int sx, final int sy, final int ex, final int ey, final Grid map )
	{
		return search( sx, sy, ex, ey, map, false );
	}

	public Path search( final int sx, final int sy, final int ex, final int ey, final Grid map, final boolean smooth )
	{
		final Path p = new Path();
		search( sx, sy, ex, ey, map, p, smooth );
		return p;
	}

	public void search( final int sx, final int sy, final int ex, final int ey, final Grid map, final Path path )
	{
		search( sx, sy, ex, ey, map, path, false );
	}

	public void search( final int sx, final int sy, final int ex, final int ey, final Grid map, final Path path, final boolean smooth )
	{
		assert isClean( map );
		path.clear();

		if ( !map.isWalkable( sx, sy ) )
			return;

		if ( !map.isWalkable( ex, ey ) )
			return;

		if ( sx == ex && sy == ey )
			return;

		final int endX = map.getWidth() - 1;
		final int endY = map.getHeight() - 1;

		try
		{
			nodes.map = map;
			nodes.open( sx, sy, 0, hCost( sx, sy, ex, ey ), DIRECTION_UP );

			while ( true )
			{
				final long n = nodes.close();
				if ( n == 0 )
					return;

				final int x = getX( n );
				final int y = getY( n );

				if ( x == ex && y == ey )
				{
					fillPath( ex, ey, sx, sy, path, map, smooth );
					return;
				}

				final int pg = getG( n );

				final int x1 = max( x - 1, 0 );
				final int x2 = min( x + 1, endX );
				final int y1 = max( y - 1, 0 );
				final int y2 = min( y + 1, endY );

				open( x, y1, pg + COST_ORTHOGONAL, DIRECTION_UP, ex, ey, map );
				open( x, y2, pg + COST_ORTHOGONAL, DIRECTION_DOWN, ex, ey, map );
				open( x2, y, pg + COST_ORTHOGONAL, DIRECTION_LEFT, ex, ey, map );
				open( x1, y, pg + COST_ORTHOGONAL, DIRECTION_RIGHT, ex, ey, map );
				open( x2, y1, pg + COST_DIAGONAL, DIRECTION_LEFT_UP, ex, ey, map );
				open( x2, y2, pg + COST_DIAGONAL, DIRECTION_LEFT_DOWN, ex, ey, map );
				open( x1, y1, pg + COST_DIAGONAL, DIRECTION_RIGHT_UP, ex, ey, map );
				open( x1, y2, pg + COST_DIAGONAL, DIRECTION_RIGHT_DOWN, ex, ey, map );
			}
		}
		catch ( final Exception e )
		{
			path.clear();
			throw e;
		}
		finally
		{
			clear();
			assert isClean( map );
		}
	}

	private void open( final int x, final int y, final int g, final int pd, final int ex, final int ey, final Grid map )
	{
		final int info = map.info( x, y );

		if ( isUnwalkable( info ) )
			return;

		switch ( pd )
		{
		case DIRECTION_RIGHT_DOWN:
			if ( !map.isWalkable( x + 1, y ) )
				return;
			break;

		case DIRECTION_LEFT_UP:
			if ( !map.isWalkable( x, y + 1 ) )
				return;
			break;
		}

		if ( isNullNode( info ) )
		{
			nodes.open( x, y, g, hCost( x, y, ex, ey ), pd );
			return;
		}

		if ( isClosedNode( info ) )
			return;

		final int idx = openNodeIdx( info );
		long n = nodes.getOpenNode( idx );

		final int ng = getG( n );
		if ( g >= ng )
		{ return; }

		n = setGF( n, g, getF( n ) - ng + g );
		nodes.openNodeParentChanged( n, idx, pd );
	}

	private void fillPath( int ex, int ey, final int sx, final int sy, final Path path, final Grid map, final boolean smooth )
	{
		fillPath( ex, ey, path, map, smooth );
		int pd = map.nodeParentDirection( ex, ey );

		while ( true )
		{
			switch ( pd )
			{
			case DIRECTION_UP:
				ey++;
				break;

			case DIRECTION_DOWN:
				ey--;
				break;

			case DIRECTION_LEFT:
				ex--;
				break;

			case DIRECTION_RIGHT:
				ex++;
				break;

			case DIRECTION_LEFT_UP:
				ex--;
				ey++;
				break;

			case DIRECTION_LEFT_DOWN:
				ex--;
				ey--;
				break;

			case DIRECTION_RIGHT_UP:
				ex++;
				ey++;
				break;

			case DIRECTION_RIGHT_DOWN:
				ex++;
				ey--;
				break;

			default:
				throw new RuntimeException( "illegal direction: " + pd );
			}

			if ( ex == sx && ey == sy )
			{
				fillPath( ex, ey, path, map, smooth );
				return;
			}

			final int ppd = map.nodeParentDirection( ex, ey );
			if ( ppd != pd )
			{
				fillPath( ex, ey, path, map, smooth );
				pd = ppd;
			}
		}
	}

	private void fillPath( final int x, final int y, final Path path, final Grid map, final boolean smooth )
	{
		if ( !smooth )
		{
			path.add( x, y );
			return;
		}

		while ( path.size() >= 2 )
		{
			final long p = path.get( 1 );
			final int x2 = Point.getX( p );
			final int y2 = Point.getY( p );

			if ( !isReachable( x, y, x2, y2, map ) )
			{
				path.add( x, y );
				return;
			}

			path.remove();
		}
		path.add( x, y );
	}

	private void clear()
	{
		nodes.clear();
	}

	private boolean isClean( final Grid map )
	{
		return nodes.isClean() && map.isClean();
	}
}
