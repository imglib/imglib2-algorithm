/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2025 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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
import java.util.Arrays;
import net.imglib2.KDTree;
import net.imglib2.kdtree.KDTreeImpl;

/**
 * Partition nodes in a {@link KDTreeImpl} into disjoint sets of nodes that are
 * above and below a given hyperplane, respectively.
 *
 * <p>
 * Construct with the {@link KDTreeImpl}. Call {@link #split(HyperPlane)} to
 * partition with respect to a {@link HyperPlane}. Then call
 * {@link #getAboveNodes()} and {@link #getBelowNodes()} to get the sets of
 * nodes above and below the hyperplane, respectively.
 *
 * <p>
 * The algorithm is described in <a
 * href="https://fly.mpi-cbg.de/~pietzsch/polytope.pdf">this note</a>.
 *
 * @author Tobias Pietzsch
 */
class SplitHyperPlaneKDTreeImpl
{
	private final KDTreeImpl tree;

	private final int n;

	private final double[] normal;

	private double m;

	private final double[] xmin;

	private final double[] xmax;

	private final TIntArrayList aboveNodes;

	private final TIntArrayList aboveSubtrees;

	private final TIntArrayList belowNodes;

	private final TIntArrayList belowSubtrees;

	public SplitHyperPlaneKDTreeImpl( final KDTreeImpl tree )
	{
		this.tree = tree;
		n = tree.numDimensions();
		normal = new double[ n ];
		xmin = new double[ n ];
		xmax = new double[ n ];
		aboveNodes = new TIntArrayList();
		aboveSubtrees = new TIntArrayList();
		belowNodes = new TIntArrayList();
		belowSubtrees = new TIntArrayList();
	}

	public int numDimensions()
	{
		return n;
	}

	public void split( final HyperPlane plane )
	{
		initNewSearch();
		System.arraycopy( plane.getNormal(), 0, normal, 0, n );
		m = plane.getDistance();
		split( tree.root(), 0 );
	}

	public void split( final double[] plane )
	{
		initNewSearch();
		System.arraycopy( plane, 0, normal, 0, n );
		m = plane[ n ];
		split( tree.root(), 0 );
	}

	private void initNewSearch()
	{
		aboveNodes.clear();
		aboveSubtrees.clear();
		belowNodes.clear();
		belowSubtrees.clear();
		Arrays.fill( xmin, Double.NEGATIVE_INFINITY );
		Arrays.fill( xmax, Double.POSITIVE_INFINITY );
	}

	public NodeIndexIterable getAboveNodes()
	{
		return new NodeIndexIterable( aboveNodes, aboveSubtrees, tree );
	}

	public NodeIndexIterable getBelowNodes()
	{
		return new NodeIndexIterable( belowNodes, belowSubtrees, tree );
	}

	private boolean allAbove()
	{
		double dot = 0;
		for ( int d = 0; d < n; ++d )
			dot += normal[ d ] * ( normal[ d ] >= 0 ? xmin[ d ] : xmax[ d ] );
		return dot >= m;
	}

	private boolean allBelow()
	{
		double dot = 0;
		for ( int d = 0; d < n; ++d )
			dot += normal[ d ] * ( normal[ d ] < 0 ? xmin[ d ] : xmax[ d ] );
		return dot < m;
	}

	private void splitSubtree( final int currentNodeIndex, final int parentsd, final boolean p, final boolean q )
	{
		if ( p && q && allAbove() )
			aboveSubtrees.add( currentNodeIndex );
		else if ( !p && !q && allBelow() )
			belowSubtrees.add( currentNodeIndex );
		else
			split( currentNodeIndex, ( parentsd + 1 ) % n );
	}

	private void split( final int currentNodeIndex, final int sd )
	{
		// consider the current node
		final double sc = tree.getDoublePosition( currentNodeIndex, sd );
		final int left = tree.left( currentNodeIndex );
		final int right = tree.right( currentNodeIndex );

		double dot = 0;
		for ( int d = 0; d < n; ++d )
			dot += tree.getDoublePosition( currentNodeIndex, d ) * normal[ d ];
		final boolean p = dot >= m;

		// current
		if ( p )
			aboveNodes.add( currentNodeIndex );
		else
			belowNodes.add( currentNodeIndex );

		// left
		if ( left >= 0 )
		{
			final double max = xmax[ sd ];
			xmax[ sd ] = sc;
			splitSubtree( left, sd, p, normal[ sd ] < 0 );
			xmax[ sd ] = max;
		}

		// right
		if ( right >= 0 )
		{
			final double min = xmin[ sd ];
			xmin[ sd ] = sc;
			splitSubtree( right, sd, p, normal[ sd ] >= 0 );
			xmin[ sd ] = min;
		}
	}
}
