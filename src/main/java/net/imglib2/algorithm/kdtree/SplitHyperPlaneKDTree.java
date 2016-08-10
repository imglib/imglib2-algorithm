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

package net.imglib2.algorithm.kdtree;

import java.util.ArrayList;

import net.imglib2.KDTree;
import net.imglib2.KDTreeNode;

/**
 * Partition nodes in a {@link KDTree} into disjoint sets of nodes that are
 * above and below a given hyperplane, respectively.
 *
 * <p>
 * Construct with the {@link KDTree}. Call {@link #split(HyperPlane)} to
 * partition with respect to a {@link HyperPlane}. Then call
 * {@link #getAboveNodes()} and {@link #getBelowNodes()} to get the sets of
 * nodes above and below the hyperplane, respectively.
 *
 * <p>
 * The algorithm is described in <a
 * href="http://fly.mpi-cbg.de/~pietzsch/polytope.pdf">this note</a>.
 *
 * @param <T>
 *            type of values stored in the tree.
 *
 * @author Tobias Pietzsch
 */
public class SplitHyperPlaneKDTree< T >
{
	private final KDTree< T > tree;

	private final int n;

	private final double[] normal;

	private double m;

	private final double[] xmin;

	private final double[] xmax;

	private final ArrayList< KDTreeNode< T > > aboveNodes;

	private final ArrayList< KDTreeNode< T > > aboveSubtrees;

	private final ArrayList< KDTreeNode< T > > belowNodes;

	private final ArrayList< KDTreeNode< T > > belowSubtrees;

	public SplitHyperPlaneKDTree( final KDTree< T > tree )
	{
		n = tree.numDimensions();
		xmin = new double[ n ];
		xmax = new double[ n ];
		normal = new double[ n ];
		this.tree = tree;
		aboveNodes = new ArrayList< KDTreeNode< T > >();
		aboveSubtrees = new ArrayList< KDTreeNode< T > >();
		belowNodes = new ArrayList< KDTreeNode< T > >();
		belowSubtrees = new ArrayList< KDTreeNode< T > >();
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
		split( tree.getRoot() );
	}

	public void split( final double[] plane )
	{
		initNewSearch();
		System.arraycopy( plane, 0, normal, 0, n );
		m = plane[ n ];
		split( tree.getRoot() );
	}

	private void initNewSearch()
	{
		aboveNodes.clear();
		aboveSubtrees.clear();
		belowNodes.clear();
		belowSubtrees.clear();
		tree.realMin( xmin );
		tree.realMax( xmax );
	}

	public Iterable< KDTreeNode< T > > getAboveNodes()
	{
		return new KDTreeNodeIterable< T >( aboveNodes, aboveSubtrees );
	}

	public Iterable< KDTreeNode< T > > getBelowNodes()
	{
		return new KDTreeNodeIterable< T >( belowNodes, belowSubtrees );
	}

	private static < T > void addAll( final KDTreeNode< T > node, final ArrayList< KDTreeNode< T > > list )
	{
		list.add( node );
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

	private void splitSubtree( final KDTreeNode< T > current, final boolean p, final boolean q )
	{
		if ( p && q && allAbove() )
			addAll( current, aboveSubtrees );
		else if ( !p && !q && allBelow() )
			addAll( current, belowSubtrees );
		else
			split( current );
	}

	private void split( final KDTreeNode< T > current )
	{
		final int sd = current.getSplitDimension();
		final double sc = current.getSplitCoordinate();

		double dot = 0;
		for ( int d = 0; d < n; ++d )
			dot += current.getDoublePosition( d ) * normal[ d ];
		final boolean p = dot >= m;

		// current
		if ( p )
			aboveNodes.add( current );
		else
			belowNodes.add( current );

		// left
		if ( current.left != null )
		{
			final double max = xmax[ sd ];
			xmax[ sd ] = sc;
			splitSubtree( current.left, p, normal[ sd ] < 0 );
			xmax[ sd ] = max;
		}

		// right
		if ( current.right != null )
		{
			final double min = xmin[ sd ];
			xmin[ sd ] = sc;
			splitSubtree( current.right, p, normal[ sd ] >= 0 );
			xmin[ sd ] = min;
		}
	}
}
