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

import mpicbg.models.Point;

/**
 * @author Varun Kapoor and Stephan Preibisch
 */
public interface Polynomial < F extends Polynomial< F, P >, P extends Point > extends Function< F, P >
{
	/**
	 * @return The degree of the polynomial
	 */
	public int degree();

	/**
	 * @param j - the j'th coefficient of the polynomial ( c_0 + c_1*x + c_2*x*x + ... + c_N*x^N)
	 * @return
	 */
	public double getCoefficient( final int j );

	/**
	 * @param x
	 * @return - the corresponding y value
	 */
	public double predict( final double x );
}
