/*-
 * #%L
 * Microtubule tracker.
 * %%
 * Copyright (C) 2017 MTrack developers.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package net.imglib2.algorithm.ransac.RansacModels;

import mpicbg.models.CoordinateTransform;
import mpicbg.models.Point;
import mpicbg.models.PointMatch;

/**
 * Replaces the PointMatch fitting a function to a set of Point instead of a set of point to a set of point
 * 
 * @author Stephan Preibisch (stephan.preibisch@gmx.de) & Timothee Lionnet
 */
public class PointFunctionMatch extends PointMatch
{
	private static final long serialVersionUID = -8070932126418631690L;

	//final protected Function<Point> function;

	double distance = 0;
	
	public PointFunctionMatch( final Point p1 )
	{
		super( p1, null );
	}
	
	//public Function<Point> getFunction() { return function; }

	/**
	 * 	Here one could compute and return the closest point on the function to p1,
	 *  but it is not well defined as there could be more than one...
	 */
	@Deprecated
	@Override
	public Point getP2() { return null; }
	
	@SuppressWarnings("unchecked")
	public void apply( final CoordinateTransform t )
	{
		distance = (float)((Function<?,Point>)t).distanceTo( p1 );
	}
	
	@SuppressWarnings("unchecked")
	public void apply( final CoordinateTransform t, final float amount )
	{
		distance = (float)((Function<?,Point>)t).distanceTo( p1 );
	}
	
	@Override
	public double getDistance() { return distance; }
}
