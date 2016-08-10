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
import java.util.Arrays;
import java.util.Collection;

import net.imglib2.KDTree;
import net.imglib2.KDTreeNode;

/**
 * Partition nodes in a {@link KDTree} into disjoint sets of nodes that are
 * inside and outside a given convex polytope, respectively.
 *
 * <p>
 * Construct with the {@link KDTree}. Call {@link #clip(ConvexPolytope)} to
 * partition with respect to a {@link ConvexPolytope}. Then call
 * {@link #getInsideNodes()} and {@link #getOutsideNodes()} to get the sets of
 * nodes inside and outside the polytope, respectively.
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
public class ClipConvexPolytopeKDTree< T >
{
	private final KDTree< T > tree;

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

	private final ArrayList< KDTreeNode< T > > inNodes;

	private final ArrayList< KDTreeNode< T > > inSubtrees;

	private final ArrayList< KDTreeNode< T > > outNodes;

	private final ArrayList< KDTreeNode< T > > outSubtrees;

	public ClipConvexPolytopeKDTree( final KDTree< T > tree )
	{
		this.tree = tree;
		n = tree.numDimensions();
		xmin = new double[ n ];
		xmax = new double[ n ];
		activeStack = new ArrayList< boolean[] >();
		psStack = new ArrayList< boolean[] >();
		inNodes = new ArrayList< KDTreeNode< T > >();
		inSubtrees = new ArrayList< KDTreeNode< T > >();
		outNodes = new ArrayList< KDTreeNode< T > >();
		outSubtrees = new ArrayList< KDTreeNode< T > >();
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
		clip( tree.getRoot(), 0 );
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
		clip( tree.getRoot(), 0 );
	}

	public Iterable< KDTreeNode< T > > getInsideNodes()
	{
		return new KDTreeNodeIterable< T >( inNodes, inSubtrees );
	}

	public Iterable< KDTreeNode< T > > getOutsideNodes()
	{
		return new KDTreeNodeIterable< T >( outNodes, outSubtrees );
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
		tree.realMin( xmin );
		tree.realMax( xmax );
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

	private void addAll( final KDTreeNode< T > node, final ArrayList< KDTreeNode< T > > list )
	{
		list.add( node );
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

	private void clipSubtree( final KDTreeNode< T > current, final boolean[] ps, final boolean[] qs, final int qoff, final int recursionDepth )
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
						addAll( current, outSubtrees );
						return;
					}
				}
			}
		}
		if ( noneActive )
			addAll( current, inSubtrees );
		else
			clip( current, recursionDepth + 1 );
	}

	private void clip( final KDTreeNode< T > current, final int recursionDepth )
	{
		final int sd = current.getSplitDimension();
		final double sc = current.getSplitCoordinate();

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
					dot += current.getDoublePosition( d ) * normal[ d ];
				ps[ i ] = dot >= ms[ i ];
				p &= ps[ i ];
			}
		}

		if ( p )
			inNodes.add( current );
		else
			outNodes.add( current );

		final int qoff = sd * nPlanes;
		if ( current.left != null )
		{
			final double max = xmax[ sd ];
			xmax[ sd ] = sc;
			clipSubtree( current.left, ps, qL, qoff, recursionDepth );
			xmax[ sd ] = max;
		}

		if ( current.right != null )
		{
			final double min = xmin[ sd ];
			xmin[ sd ] = sc;
			clipSubtree( current.right, ps, qR, qoff, recursionDepth );
			xmin[ sd ] = min;
		}
	}
}
