package net.imglib2.algorithm.astar;

import static net.imglib2.algorithm.astar.Point.toPoint;

public class Reachability
{

	public static boolean isReachable( final int x1, final int y1, final int x2, final int y2, final Grid grid )
	{
		return isReachable( x1, y1, x2, y2, 1, grid );
	}

	public static boolean isReachable( final int x1, final int y1, final int x2, final int y2, final int scale, final Grid grid )
	{
		return getClosestWalkablePointToTarget( x1, y1, x2, y2, scale, grid ) == toPoint( x2, y2 );
	}

	public static long getClosestWalkablePointToTarget( final int x1, final int y1, final int x2, final int y2, final Grid grid )
	{
		return getClosestWalkablePointToTarget( x1, y1, x2, y2, 1, grid );
	}

	public static long getClosestWalkablePointToTarget(
			final int x1, final int y1, final int x2, final int y2, final int scale, final Grid grid )
	{
		return getClosestWalkablePointToTarget( x1, y1, x2, y2, scale, grid, null );
	}

	public static long getClosestWalkablePointToTarget(
			final int x1, final int y1, final int x2, final int y2, final int scale, final Grid grid, Fence fence )
	{
		if ( scale < 1 )
			throw new IllegalArgumentException( "Illegal scale: " + scale );

		if ( fence != null && fence.isReachable( x1, y1, x2, y2 ) )
			fence = null;

		double cx1 = scaleDown( x1 + 0.5, scale );
		double cy1 = scaleDown( y1 + 0.5, scale );

		int gx1 = ( int ) cx1;
		int gy1 = ( int ) cy1;

		if ( !grid.isWalkable( gx1, gy1 ) )
			return toPoint( x1, y1 );

		final double cx2 = scaleDown( x2 + 0.5, scale );
		final double cy2 = scaleDown( y2 + 0.5, scale );

		final int gx2 = ( int ) cx2;
		final int gy2 = ( int ) cy2;

		if ( gx1 == gx2 && gy1 == gy2 )
		{
			if ( fence != null && !fence.isReachable( x1, y1, x2, y2 ) )
				return toPoint( x1, y1 );
			return toPoint( x2, y2 );
		}

		if ( y1 == y2 )
		{
			final int inc = gx2 > gx1 ? 1 : -1;
			for ( int gx = gx1 + inc;; gx += inc )
			{
				if ( !grid.isWalkable( gx, gy1 )
						|| ( fence != null
								&& !fence.isReachable( x1, y1, gx == gx2 ? x2 : scaleUp( gx, scale ), y2 ) ) )
				{
					if ( gx - inc == gx1 )
						return toPoint( x1, y1 );
					return toPoint( scaleUp( gx - inc, scale ), y1 );
				}
				if ( gx == gx2 )
					return toPoint( x2, y2 );
			}
		}

		if ( x1 == x2 )
		{
			final int inc = gy2 > gy1 ? 1 : -1;
			for ( int gy = gy1 + inc;; gy += inc )
			{
				if ( !grid.isWalkable( gx1, gy )
						|| ( fence != null
								&& !fence.isReachable( x1, y1, x2, gy == gy2 ? y2 : scaleUp( gy, scale ) ) ) )
				{
					if ( gy - inc == gy1 )
						return toPoint( x1, y1 );
					return toPoint( x1, scaleUp( gy - inc, scale ) );
				}
				if ( gy == gy2 )
					return toPoint( x2, y2 );
			}
		}

		final double dx = cx2 - cx1;
		final double dy = cy2 - cy1;

		final double k = dy / dx;
		final double b = cy1 - k * cx1;

		final boolean stepX;
		final double addDx, addDy;

		if ( Math.abs( dx ) > Math.abs( dy ) )
		{
			stepX = true;
			addDx = dx > 0 ? 1 : -1;
			addDy = addDx * k;
		}
		else
		{
			stepX = false;
			addDy = dy > 0 ? 1 : -1;
			addDx = addDy / k;
		}

		double cx = cx1;
		double cy = cy1;

		while ( true )
		{
			cx += addDx;
			cy += addDy;

			int gx = ( int ) cx;
			int gy = ( int ) cy;

			if ( stepX
					? ( addDx > 0 ? gx >= gx2 : gx <= gx2 )
					: ( addDy > 0 ? gy >= gy2 : gy <= gy2 ) )
			{
				gx = gx2;
				gy = gy2;
			}

			if ( !grid.isWalkable( gx, gy ) )
				break;

			if ( gx != gx1 && gy != gy1 )
			{
				final int x0 = dx > 0 ? gx : gx1;
				final double y0 = k * x0 + b;

				if ( Math.rint( y0 ) != y0 || k < 0 )
				{
					final int gy0 = ( int ) y0;
					if ( gy0 == gy )
					{
						if ( !grid.isWalkable( gx1, gy ) )
							break;
					}
					else
					{
						if ( !grid.isWalkable( gx, gy1 ) )
							break;
					}
				}
			}

			if ( gx == gx2 && gy == gy2 )
			{
				if ( fence != null && !fence.isReachable( x1, y1, x2, y2 ) )
					break;
				return toPoint( x2, y2 );
			}

			if ( fence != null && !fence.isReachable( x1, y1, scaleUp( cx, scale ), scaleUp( cy, scale ) ) )
				break;

			cx1 = cx;
			cy1 = cy;

			gx1 = gx;
			gy1 = gy;
		}

		return scaleUpPoint( cx1, cy1, scale );
	}

	private static double scaleDown( final double d, final int scale )
	{
		return d / scale;
	}

	private static int scaleUp( final int i, final int scale )
	{
		return i * scale + scale / 2;
	}

	private static int scaleUp( final double d, final int scale )
	{
		return ( int ) ( d * scale );
	}

	private static long scaleUpPoint( final double x, final double y, final int scale )
	{
		return toPoint( scaleUp( x, scale ), scaleUp( y, scale ) );
	}
}
