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
 * Interpolation of two AbstractFunctions, inspired by Interpolated Models by Stephan Saalfeld
 * 
 * @author Stephan Preibisch
 */
public abstract class InterpolatedFunction< A extends AbstractFunction< A >, B extends AbstractFunction< B >, M extends InterpolatedFunction< A, B, M > > extends AbstractFunction2D< M >
{
	private static final long serialVersionUID = -8524786898599474286L;

	final protected A a;
	final protected B b;
	protected double lambda;
	protected double l1;

	public InterpolatedFunction( final A a, final B b, final double lambda )
	{
		this.a = a;
		this.b = b;
		this.lambda = lambda;
		l1 = 1.0 - lambda;
	}

	public A getA() { return a; }
	public B getB() { return b; }
	public double getLambda() { return lambda; }

	public void setLambda( final double lambda )
	{
		this.lambda = lambda;
		this.l1 = 1.0f - lambda;
	}

	@Override
	public int getMinNumPoints() { return Math.max( a.getMinNumPoints(), b.getMinNumPoints() ); }

	@Override
	public void set( final M m )
	{
		a.set( m.a );
		b.set( m.b );
		lambda = m.lambda;
		l1 = m.l1;
		cost = m.cost;
	}

	@Override
	public void fitFunction( final Collection< Point > points )
			throws NotEnoughDataPointsException, IllDefinedDataPointsException
	{
		a.fitFunction( points );
		b.fitFunction( points );

		interpolate( points );
	}

	protected abstract void interpolate( final Collection< Point > points )
			throws NotEnoughDataPointsException, IllDefinedDataPointsException;
}
