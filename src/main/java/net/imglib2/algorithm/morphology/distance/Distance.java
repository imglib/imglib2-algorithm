/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2016 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
 * John Bogovic, Albert Cardona, Barry DeZonia, Christian Dietz, Jan Funke,
 * Aivar Grislis, Jonathan Hale, Grant Harris, Stefan Helfrich, Mark Hiner,
 * Martin Horn, Steffen Jaensch, Lee Kamentsky, Larry Lindsey, Melissa Linkert,
 * Mark Longair, Brian Northan, Nick Perry, Curtis Rueden, Johannes Schindelin,
 * Jean-Yves Tinevez and Michael Zinsmaier.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package net.imglib2.algorithm.morphology.distance;

/**
 * Family of strictly convex, real valued functions that are separable in all
 * dimension. The interface thus specifies just a one-dimensional function that
 * is parameterized by an offset along both the x- and the y-axis. These
 * parameters are passed at evaluation along with the dimension in which the
 * function is to be evaluated.
 * <p>
 * Two distinct members of the same family, {@code d = f(x)} and
 * {@code d' = f(x - x0) + y0}, must have exactly one intersection point (for
 * each dimension):
 * </p>
 * 
 * <pre>
 * |{ x : f(x) = f(x -x0) + y0 }| = 1
 * </pre>
 * <p>
 * This interface is used in {@link DistanceTransform}:
 * </p>
 * <p>
 * {@code D( p ) = min_q f(q) + d(p,q)} where p,q are points on a grid/image.
 * </p>
 * 
 * @author Philipp Hanslovsky
 *
 */
public interface Distance
{

	/**
	 * Evaluate function with family parameters xShift and yShift at position x
	 * in dimension dim.
	 */
	double evaluate( double x, double xShift, double yShift, int dim );

	/**
	 *
	 * Determine the intersection point in dimension dim of two members of the
	 * function family. The members are parameterized by xShift1, yShift1,
	 * xShift2, yShift2, with xShift1 &lt; xShift2.
	 */
	double intersect( double xShift1, double yShift1, double xShift2, double yShift2, int dim );

}
