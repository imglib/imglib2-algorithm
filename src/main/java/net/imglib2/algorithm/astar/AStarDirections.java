package net.imglib2.algorithm.astar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.imglib2.algorithm.astar.AStarDirections.AStarDirection;

public class AStarDirections implements Iterable< AStarDirection >
{

	static final int COST_ORTHOGONAL = 5; // 1 * 5

	static final int COST_DIAGONAL = 7; // 1.4 * 5

	public static final AStarDirections FOUR_CONNECTED;

	public static final AStarDirections HEIGHT_CONNECTED;

	public static final AStarDirection RIGHT = new AStarDirection( 1, 0, COST_ORTHOGONAL, Grid.DIRECTION_LEFT );

	public static final AStarDirection LEFT = new AStarDirection( -1, 0, COST_ORTHOGONAL, Grid.DIRECTION_RIGHT );

	public static final AStarDirection UP = new AStarDirection( 0, -1, COST_ORTHOGONAL, Grid.DIRECTION_UP );

	public static final AStarDirection DOWN = new AStarDirection( 0, 1, COST_ORTHOGONAL, Grid.DIRECTION_DOWN );

	public static final AStarDirection RIGHT_UP = new AStarDirection( 1, -1, COST_DIAGONAL, Grid.DIRECTION_LEFT_UP );

	public static final AStarDirection RIGHT_DOWN = new AStarDirection( 1, 1, COST_DIAGONAL, Grid.DIRECTION_LEFT_DOWN );

	public static final AStarDirection LEFT_UP = new AStarDirection( -1, -1, COST_DIAGONAL, Grid.DIRECTION_RIGHT_UP );

	public static final AStarDirection LEFT_DOWN = new AStarDirection( -1, 1, COST_DIAGONAL, Grid.DIRECTION_RIGHT_DOWN );

	static
	{
		FOUR_CONNECTED = create()
				.add( UP )
				.add( DOWN )
				.add( LEFT )
				.add( RIGHT )
				.get();
		HEIGHT_CONNECTED = create()
				.add( UP )
				.add( DOWN )
				.add( LEFT )
				.add( RIGHT )
				.add( LEFT_UP )
				.add( LEFT_DOWN )
				.add( RIGHT_UP )
				.add( RIGHT_DOWN )
				.get();
	}

	private final List< AStarDirection > directions;

	private AStarDirections( final List< AStarDirection > directions )
	{
		this.directions = Collections.unmodifiableList( directions );
	}

	@Override
	public Iterator< AStarDirection > iterator()
	{
		return directions.iterator();
	}

	public static final Builder create()
	{
		return new Builder();
	}

	public static class Builder
	{
		private final List< AStarDirection > directions = new ArrayList<>();

		public Builder add( final AStarDirection direction )
		{
			directions.add( direction );
			return this;
		}

		public Builder add( final int xoffset, final int yoffset, final int baseCost, final int direction )
		{
			directions.add( new AStarDirection( xoffset, yoffset, baseCost, direction ) );
			return this;
		}

		public AStarDirections get()
		{
			return new AStarDirections( directions );
		}
	}

	public static class AStarDirection
	{

		public final int xoffset;

		public final int yoffset;

		public final int baseCost;

		public final int direction;

		public AStarDirection( final int xoffset, final int yoffset, final int baseCost, final int direction )
		{
			this.xoffset = xoffset;
			this.yoffset = yoffset;
			this.baseCost = baseCost;
			this.direction = direction;
		}

		@Override
		public String toString()
		{
			return super.toString() + " xoffset=" + xoffset + ", yoffset=" + yoffset + ", baseCost=" + baseCost + ", pd=" + direction;
		}
	}
}
