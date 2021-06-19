package net.imglib2.algorithm.astar;

class Cost
{

	static final int COST_ORTHOGONAL = 5; // 1 * 5

	static final int COST_DIAGONAL = 7; // 1.4 * 5

	static int hCost( final int x1, final int y1, final int x2, final int y2 )
	{
		return ( Math.abs( x2 - x1 ) + Math.abs( y2 - y1 ) ) * COST_ORTHOGONAL;
	}
}
