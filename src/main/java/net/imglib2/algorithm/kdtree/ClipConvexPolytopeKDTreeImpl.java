/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2023 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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

package net.imglib2.algorithm.kdtree;

import gnu.trove.list.array.TIntArrayList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import net.imglib2.kdtree.KDTreeImpl;

/**
 * Partition nodes in a {@link KDTreeImpl} into disjoint sets of nodes that are
 * inside and outside a given convex polytope, respectively.
 *
 * <p>
 * Construct with the {@link KDTreeImpl}. Call {@link #clip(ConvexPolytope)} to
 * partition with respect to a {@link ConvexPolytope}. Then call
 * {@link #getInsideNodes()} and {@link #getOutsideNodes()} to get the sets of
 * nodes inside and outside the polytope, respectively.
 *
 * <p>
 * The algorithm is described in <a
 * href="https://fly.mpi-cbg.de/~pietzsch/polytope.pdf">this note</a>.
 *
 * @author Tobias Pietzsch
 */
class ClipConvexPolytopeKDTreeImpl
{
	private final KDTreeImpl tree;

	private final int n;

	private int nPlanes;

	private double[][] normals;

	private double[] ms;

	private final double[] xmin;

	private final double[] xmax;

	private boolean[] qR;

	private boolean[] qL;

	private final ArrayList< boolean[] > activeStack;

	private final ArrayList< boolean[] > psStack;

	private final TIntArrayList inNodes;

	private final TIntArrayList inSubtrees;

	private final TIntArrayList outNodes;

	private final TIntArrayList outSubtrees;

	public ClipConvexPolytopeKDTreeImpl( final KDTreeImpl tree )
	{
		this.tree = tree;
		n = tree.numDimensions();
		xmin = new double[ n ];
		xmax = new double[ n ];
		activeStack = new ArrayList<>();
		psStack = new ArrayList<>();
		inNodes = new TIntArrayList();
		inSubtrees = new TIntArrayList();
		outNodes = new TIntArrayList();
		outSubtrees = new TIntArrayList();
	}

	public int numDimensions()
	{
		return n;
	}

	public void clip( final ConvexPolytope polytope )
	{
		final Collection< ? extends HyperPlane > planes = polytope.getHyperplanes();
		initNewSearch( planes.size() );
		int i = 0;
		for ( final HyperPlane plane : planes )
		{
			final double[] normal = normals[ i ];
			System.arraycopy( plane.getNormal(), 0, normal, 0, n );
			ms[ i ] = plane.getDistance();
			for ( int d = 0; d < n; ++d )
			{
				qL[ d * nPlanes + i ] = normal[ d ] < 0;
				qR[ d * nPlanes + i ] = normal[ d ] >= 0;
			}
			++i;
		}
		clip( tree.root(), 0 );
	}

	public void clip( final double[][] planes )
	{
		initNewSearch( planes.length );
		for ( int i = 0; i < nPlanes; ++i )
		{
			final double[] normal = normals[ i ];
			System.arraycopy( planes[ i ], 0, normal, 0, n );
			ms[ i ] = planes[ i ][ n ];
			for ( int d = 0; d < n; ++d )
			{
				qL[ d * nPlanes + i ] = normal[ d ] < 0;
				qR[ d * nPlanes + i ] = normal[ d ] >= 0;
			}
		}
		clip( tree.root(), 0 );
	}

	public NodeIndexIterable getInsideNodes()
	{
		return new NodeIndexIterable( inNodes, inSubtrees, tree );
	}

	public NodeIndexIterable getOutsideNodes()
	{
		return new NodeIndexIterable( outNodes, outSubtrees, tree );
	}

	private void initNewSearch( final int nPlanes )
	{
		this.nPlanes = nPlanes;
		normals = new double[ nPlanes ][];
		for ( int i = 0; i < nPlanes; ++i )
			normals[ i ] = new double[ n ];
		ms = new double[ nPlanes ];
		qR = new boolean[ n * nPlanes ];
		qL = new boolean[ n * nPlanes ];
		inNodes.clear();
		inSubtrees.clear();
		outNodes.clear();
		outSubtrees.clear();
		activeStack.clear();
		psStack.clear();
		Arrays.fill( xmin, Double.NEGATIVE_INFINITY );
		Arrays.fill( xmax, Double.POSITIVE_INFINITY );
		Arrays.fill( getActiveArray( 0 ), true );
	}

	private boolean[] getActiveArray( final int i )
	{
		if ( i >= activeStack.size() )
		{
			activeStack.add( new boolean[ nPlanes ] );
			psStack.add( new boolean[ nPlanes ] );
		}
		return activeStack.get( i );
	}

	private boolean[] getPsArray( final int i )
	{
		return psStack.get( i );
	}

	private boolean allAbove( final int i )
	{
		final double[] normal = normals[ i ];
		double dot = 0;
		for ( int d = 0; d < n; ++d )
			dot += normal[ d ] * ( normal[ d ] >= 0 ? xmin[ d ] : xmax[ d ] );
		return dot >= ms[ i ];
	}

	private boolean allBelow( final int i )
	{
		final double[] normal = normals[ i ];
		double dot = 0;
		for ( int d = 0; d < n; ++d )
			dot += normal[ d ] * ( normal[ d ] < 0 ? xmin[ d ] : xmax[ d ] );
		return dot < ms[ i ];
	}

	private void clipSubtree( final int currentNodeIndex, final boolean[] ps, final boolean[] qs, final int qoff, final int recursionDepth )
	{
		final boolean[] active = getActiveArray( recursionDepth );
		final boolean[] stillActive = getActiveArray( recursionDepth + 1 );
		System.arraycopy( active, 0, stillActive, 0, nPlanes );
		boolean noneActive = true;
		for ( int i = 0; i < nPlanes; ++i )
		{
			if ( active[ i ] )
			{
				if ( ps[ i ] && qs[ qoff + i ] && allAbove( i ) )
					stillActive[ i ] = false;
				else
				{
					noneActive = false;
					if ( !ps[ i ] && !qs[ qoff + i ] && allBelow( i ) )
					{
						outSubtrees.add( currentNodeIndex );
						return;
					}
				}
			}
		}
		if ( noneActive )
			inSubtrees.add( currentNodeIndex );
		else
			clip( currentNodeIndex, recursionDepth + 1 );
	}

	private void clip( final int currentNodeIndex, final int recursionDepth )
	{
		final int sd = recursionDepth % n;
		final double sc = tree.getDoublePosition( currentNodeIndex, sd );
		final int left = tree.left( currentNodeIndex );
		final int right = tree.right( currentNodeIndex );

		final boolean[] active = getActiveArray( recursionDepth );
		final boolean[] ps = getPsArray( recursionDepth );

		boolean p = true;
		for ( int i = 0; i < nPlanes; ++i )
		{
			if ( active[ i ] )
			{
				final double[] normal = normals[ i ];
				double dot = 0;
				for ( int d = 0; d < n; ++d )
					dot += tree.getDoublePosition( currentNodeIndex, d ) * normal[ d ];
				ps[ i ] = dot >= ms[ i ];
				p &= ps[ i ];
			}
		}

		if ( p )
			inNodes.add( currentNodeIndex );
		else
			outNodes.add( currentNodeIndex );

		final int qoff = sd * nPlanes;
		if ( left >= 0 )
		{
			final double max = xmax[ sd ];
			xmax[ sd ] = sc;
			clipSubtree( left, ps, qL, qoff, recursionDepth );
			xmax[ sd ] = max;
		}

		if ( right >= 0 )
		{
			final double min = xmin[ sd ];
			xmin[ sd ] = sc;
			clipSubtree( right, ps, qR, qoff, recursionDepth );
			xmin[ sd ] = min;
		}
	}
}
