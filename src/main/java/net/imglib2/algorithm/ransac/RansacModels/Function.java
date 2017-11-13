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

import java.util.Collection;

import mpicbg.models.IllDefinedDataPointsException;
import mpicbg.models.NotEnoughDataPointsException;
import mpicbg.models.Point;

/**
 * Interface for a {@link Function} that can be fit to {@link Point}s
 * 
 * @author Stephan Preibisch
 */
public interface Function< F extends Function< F, P >, P extends Point >
{
	/**
	 * @return - how many points are at least necessary to fit the function
	 */
	public int getMinNumPoints();

	/**
	 * Fits this Function to the set of {@link Point}s.

	 * @param points - {@link Collection} of {@link Point}s
	 * @throws NotEnoughDataPointsException - thrown if not enough {@link Point}s are in the {@link Collection}
	 */
	public void fitFunction( final Collection<P> points ) throws NotEnoughDataPointsException, IllDefinedDataPointsException;
	
	/**
	 * Computes the minimal distance of a {@link Point} to this function
	 *  
	 * @param point - the {@link Point}
	 * @return - distance to the {@link Function}
	 */
	public double distanceTo( final P point );

	/**
	 * @return - a copy of the function object
	 */
	public F copy();
}
