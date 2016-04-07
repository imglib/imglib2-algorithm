/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2016 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
 * John Bogovic, Albert Cardona, Barry DeZonia, Christian Dietz, Jan Funke,
 * Aivar Grislis, Jonathan Hale, Grant Harris, Stefan Helfrich, Mark Hiner,
 * Martin Horn, Steffen Jaensch, Lee Kamentsky, Larry Lindsey, Melissa Linkert,
 * Mark Longair, Brian Northan, Nick Perry, Dimiter Prodanov, Curtis Rueden,
 * Johannes Schindelin, Jean-Yves Tinevez and Michael Zinsmaier.
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

import net.imglib2.AbstractEuclideanSpace;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.LinAlgHelpers;

public class HyperPlane extends AbstractEuclideanSpace
{
	private final double[] normal;

	private final double distance;

	public HyperPlane( final double[] normal, final double distance )
	{
		super( normal.length );
		this.normal = normal.clone();
		this.distance = distance;
		LinAlgHelpers.normalize( this.normal );
	}

	public HyperPlane( final double... normalAndDistance )
	{
		super( normalAndDistance.length - 1 );
		this.normal = new double[ n ];
		System.arraycopy( normalAndDistance, 0, normal, 0, n );
		this.distance = normalAndDistance[ n ];
		LinAlgHelpers.normalize( this.normal );
	}

	public double[] getNormal()
	{
		return normal;
	}

	public double getDistance()
	{
		return distance;
	}

	/**
	 * Apply an {@link AffineTransform3D} to a 3D {@link HyperPlane}.
	 *
	 * @param plane
	 *            a 3D plane.
	 * @param transform
	 *            affine transformation to apply to the plane.
	 * @return the transformed plane.
	 */
	public static HyperPlane transform( final HyperPlane plane, final AffineTransform3D transform )
	{
		assert plane.numDimensions() == 3;

		final double[] O = new double[ 3 ];
		final double[] tO = new double[ 3 ];
		LinAlgHelpers.scale( plane.getNormal(), plane.getDistance(), O );
		transform.apply( O, tO );

		final double[][] m = new double[ 3 ][ 3 ];
		for ( int r = 0; r < 3; ++r )
			for ( int c = 0; c < 3; ++c )
				m[ r ][ c ] = transform.inverse().get( c, r );
		final double[] tN = new double[ 3 ];
		LinAlgHelpers.mult( m, plane.getNormal(), tN );
		LinAlgHelpers.normalize( tN );
		final double td = LinAlgHelpers.dot( tN, tO );

		return new HyperPlane( tN, td );
	}
}
