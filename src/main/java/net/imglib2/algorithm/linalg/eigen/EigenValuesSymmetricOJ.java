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

package net.imglib2.algorithm.linalg.eigen;

import org.ojalgo.commons.math3.linear.RealMatrixWrapper;
import org.ojalgo.matrix.decomposition.Eigenvalue;
import org.ojalgo.matrix.decomposition.RawEigenvalue;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.MatrixStore.LogicalBuilder;

import net.imglib2.algorithm.linalg.matrix.RealCompositeSymmetricMatrix;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.composite.Composite;

public class EigenValuesSymmetricOJ< T extends RealType< T >, U extends ComplexType< U > > implements EigenValues< T, U >
{
	private final int nDim;

	private final RealCompositeSymmetricMatrix< T > m;

	private final RealMatrixWrapper w;

	private final Eigenvalue< Double > ed;

	private final LogicalBuilder< Double > s;

	public EigenValuesSymmetricOJ( final int nDim )
	{
		super();
		this.nDim = nDim;
		this.m = new RealCompositeSymmetricMatrix<>( null, nDim );
		this.w = RealMatrixWrapper.of( this.m );
		this.ed = Eigenvalue.PRIMITIVE.make( this.w, true );
		this.s = MatrixStore.PRIMITIVE.makeWrapper( w );
	}

	@Override
	public void compute( final Composite< T > tensor, final Composite< U > evs )
	{
		m.setData( tensor );
		ed.computeValuesOnly( s );
		final double[] result = ( ( RawEigenvalue ) ed ).getRealEigenvalues();
		for ( int z = 0; z < nDim; ++z )
			evs.get( z ).setReal( result[ z ] );
	}

	@Override
	public EigenValuesSymmetricOJ< T, U > copy()
	{
		return new EigenValuesSymmetricOJ<>( nDim );
	}
}