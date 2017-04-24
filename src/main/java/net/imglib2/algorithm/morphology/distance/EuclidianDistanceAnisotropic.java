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

import java.util.Arrays;
import java.util.stream.DoubleStream;

/**
 *
 * Implementation of weighted anisotropic Euclidian distance:
 *
 * D( p ) = min_q f(q) + sum_i w_i*(p_i - q_i)*(p_i - q_i).
 *
 * @author Philipp Hanslovsky
 *
 */
public class EuclidianDistanceAnisotropic implements Distance
{

	private final double[] weights;

	private final double[] oneOverTwoTimesWeights;

	/**
	 * When accounting for anisotropic image data, the ratios of the weights
	 * should be equal to the squared ratios of the voxel sizes, e.g. if voxel
	 * is twice as long along the y-axis, the weight for the y-axis should be
	 * four times as big as the weight for the x-axis.
	 *
	 */
	public EuclidianDistanceAnisotropic( final double... weights )
	{
		super();
		this.weights = weights;
		this.oneOverTwoTimesWeights = Arrays.stream( weights ).map( w -> 0.5 / w ).toArray();
	}

	public EuclidianDistanceAnisotropic( final int nDim, final double weight )
	{
		this( DoubleStream.generate( () -> weight ).limit( nDim ).toArray() );
	}

	@Override
	public double evaluate( final double x, final double xShift, final double yShift, final int dim )
	{
		final double diff = x - xShift;
		return weights[ dim ] * diff * diff + yShift;
	}

	@Override
	public double intersect( final double xShift1, final double yShift1, final double xShift2, final double yShift2, final int dim )
	{
		final double a = weights[ dim ];
		return oneOverTwoTimesWeights[ dim ] * ( a * xShift2 * xShift2 + yShift2 - ( a * xShift1 * xShift1 + yShift1 ) ) / ( xShift2 - xShift1 );
	}

}
