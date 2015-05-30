/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2015 Tobias Pietzsch, Stephan Preibisch, Barry DeZonia,
 * Stephan Saalfeld, Curtis Rueden, Albert Cardona, Christian Dietz, Jean-Yves
 * Tinevez, Johannes Schindelin, Jonathan Hale, Lee Kamentsky, Larry Lindsey, Mark
 * Hiner, Michael Zinsmaier, Martin Horn, Grant Harris, Aivar Grislis, John
 * Bogovic, Steffen Jaensch, Stefan Helfrich, Jan Funke, Nick Perry, Mark Longair,
 * Melissa Linkert and Dimiter Prodanov.
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

package net.imglib2.algorithm.kdtree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import net.imglib2.AbstractEuclideanSpace;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.LinAlgHelpers;

public class ConvexPolytope extends AbstractEuclideanSpace
{
	private final Collection< ? extends HyperPlane > hyperplanes;

	public ConvexPolytope( final Collection< ? extends HyperPlane > hyperplanes )
	{
		super( hyperplanes.iterator().next().numDimensions() );
		this.hyperplanes = hyperplanes;
	}

	public ConvexPolytope( final HyperPlane... hyperplanes )
	{
		this( Arrays.asList( hyperplanes ) );
	}

	public Collection< ? extends HyperPlane > getHyperplanes()
	{
		return hyperplanes;
	}

	/**
	 * Apply an {@link AffineTransform3D} to a 3D {@link ConvexPolytope}.
	 *
	 * @param polytope
	 *            a 3D polytope.
	 * @param transform
	 *            affine transformation to apply to the polytope.
	 * @return the transformed polytope.
	 */
	public static ConvexPolytope transform( final ConvexPolytope polytope, final AffineTransform3D transform )
	{
		assert polytope.numDimensions() == 3;

		final double[] O = new double[ 3 ];
		final double[] tO = new double[ 3 ];
		final double[] tN = new double[ 3 ];
		final double[][] m = new double[3][3];
		for ( int r = 0; r < 3; ++r )
			for ( int c = 0; c < 3; ++c )
				m[r][c] = transform.inverse().get( c, r );

		final ArrayList< HyperPlane > transformedPlanes = new ArrayList< HyperPlane >();
		for ( final HyperPlane plane : polytope.getHyperplanes() )
		{
			LinAlgHelpers.scale( plane.getNormal(), plane.getDistance(), O );
			transform.apply( O, tO );
			LinAlgHelpers.mult( m, plane.getNormal(), tN );
			LinAlgHelpers.normalize( tN );
			final double td = LinAlgHelpers.dot( tN, tO );
			transformedPlanes.add( new HyperPlane( tN, td ) );
		}
		return new ConvexPolytope( transformedPlanes );
	}
}
