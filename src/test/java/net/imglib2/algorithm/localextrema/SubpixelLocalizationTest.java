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
package net.imglib2.algorithm.localextrema;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.imglib2.Cursor;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.FloatArray;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Util;

import org.junit.Test;

public class SubpixelLocalizationTest
{

	/**
	 * Tolerance, in pixel units, for each dimension, above which the test will
	 * fail.
	 */
	private static final double TOLERANCE = 0.03;

	@Test
	public void test2DforX()
	{
		final ArrayImg< FloatType, FloatArray > I = ArrayImgs.floats( 256, 64 );

		final double A = 128;
		final double spot_sigma = 1.7;

		final List< Point > peaks = new ArrayList< Point >( 11 );
		final List< RealLocalizable > truths = new ArrayList< RealLocalizable >( peaks.size() );

		for ( int j = 0; j < 11; j++ )
		{
			final double xf = 20. * j + 10. + j / 10.;
			final double yf = 32.4;
			final double[] posf = new double[] { xf, yf };
			final RealPoint rpos = new RealPoint( posf );
			truths.add( rpos );

			final long xi = Math.round( xf );
			final long yi = Math.round( yf );
			final long[] posi = new long[] { xi, yi };
			final Point pos = new Point( posi );
			peaks.add( pos );

			final RectangleShape shape = new RectangleShape( ( int ) Math.ceil( 3 * spot_sigma ), false );
			final RandomAccess< Neighborhood< FloatType >> nra = shape.neighborhoodsRandomAccessible( I ).randomAccess();
			nra.setPosition( posi );
			final Cursor< FloatType > cursor = nra.get().localizingCursor();
			while ( cursor.hasNext() )
			{
				cursor.fwd();
				cursor.get().setReal( gauss( cursor, rpos, A, spot_sigma ) );
			}
		}

		test( truths, peaks, I, 0 );
	}

	@Test
	public void test2DforY()
	{
		final ArrayImg< FloatType, FloatArray > I = ArrayImgs.floats( 64, 256 );

		final double A = 128;
		final double spot_sigma = 1.7;

		final List< Point > peaks = new ArrayList< Point >( 11 );
		final List< RealLocalizable > truths = new ArrayList< RealLocalizable >( peaks.size() );

		for ( int j = 0; j < 11; j++ )
		{
			final double yf = 20. * j + 10. + j / 10.;
			final double xf = 32.4;
			final double[] posf = new double[] { xf, yf };
			final RealPoint rpos = new RealPoint( posf );
			truths.add( rpos );

			final long xi = Math.round( xf );
			final long yi = Math.round( yf );
			final long[] posi = new long[] { xi, yi };
			final Point pos = new Point( posi );
			peaks.add( pos );

			final RectangleShape shape = new RectangleShape( ( int ) Math.ceil( 3 * spot_sigma ), false );
			final RandomAccess< Neighborhood< FloatType >> nra = shape.neighborhoodsRandomAccessible( I ).randomAccess();
			nra.setPosition( posi );
			final Cursor< FloatType > cursor = nra.get().localizingCursor();
			while ( cursor.hasNext() )
			{
				cursor.fwd();
				cursor.get().setReal( gauss( cursor, rpos, A, spot_sigma ) );
			}
		}

		test( truths, peaks, I, 1 );
	}

	@Test
	public void test3DforX()
	{
		final ArrayImg< FloatType, FloatArray > I = ArrayImgs.floats( 256, 64, 32 );

		final double A = 128;
		final double spot_sigma = 1.7;

		final List< Point > peaks = new ArrayList< Point >( 11 );
		final List< RealLocalizable > truths = new ArrayList< RealLocalizable >( peaks.size() );

		for ( int j = 0; j < 11; j++ )
		{
			final double xf = 20. * j + 10. + j / 10.;
			final double yf = 32.4;
			final double zf = 16.1;

			final double[] posf = new double[] { xf, yf, zf };
			final RealPoint rpos = new RealPoint( posf );
			truths.add( rpos );

			final long xi = Math.round( xf );
			final long yi = Math.round( yf );
			final long zi = Math.round( zf );
			final long[] posi = new long[] { xi, yi, zi };
			final Point pos = new Point( posi );
			peaks.add( pos );

			final RectangleShape shape = new RectangleShape( ( int ) Math.ceil( 3 * spot_sigma ), false );
			final RandomAccess< Neighborhood< FloatType >> nra = shape.neighborhoodsRandomAccessible( I ).randomAccess();
			nra.setPosition( posi );
			final Cursor< FloatType > cursor = nra.get().localizingCursor();
			while ( cursor.hasNext() )
			{
				cursor.fwd();
				cursor.get().setReal( gauss( cursor, rpos, A, spot_sigma ) );
			}
		}

		test( truths, peaks, I, 0 );
	}

	@Test
	public void test3DforY()
	{
		final ArrayImg< FloatType, FloatArray > I = ArrayImgs.floats( 64, 256, 32 );

		final double A = 128;
		final double spot_sigma = 1.7;

		final List< Point > peaks = new ArrayList< Point >( 11 );
		final List< RealLocalizable > truths = new ArrayList< RealLocalizable >( peaks.size() );

		for ( int j = 0; j < 11; j++ )
		{
			final double yf = 20. * j + 10. + j / 10.;
			final double xf = 32.4;
			final double zf = 16.1;

			final double[] posf = new double[] { xf, yf, zf };
			final RealPoint rpos = new RealPoint( posf );
			truths.add( rpos );

			final long xi = Math.round( xf );
			final long yi = Math.round( yf );
			final long zi = Math.round( zf );
			final long[] posi = new long[] { xi, yi, zi };
			final Point pos = new Point( posi );
			peaks.add( pos );

			final RectangleShape shape = new RectangleShape( ( int ) Math.ceil( 3 * spot_sigma ), false );
			final RandomAccess< Neighborhood< FloatType >> nra = shape.neighborhoodsRandomAccessible( I ).randomAccess();
			nra.setPosition( posi );
			final Cursor< FloatType > cursor = nra.get().localizingCursor();
			while ( cursor.hasNext() )
			{
				cursor.fwd();
				cursor.get().setReal( gauss( cursor, rpos, A, spot_sigma ) );
			}
		}

		test( truths, peaks, I, 1 );
	}

	@Test
	public void test3DforZ()
	{
		final ArrayImg< FloatType, FloatArray > I = ArrayImgs.floats( 64, 32, 256 );

		final double A = 128;
		final double spot_sigma = 1.7;

		final List< Point > peaks = new ArrayList< Point >( 11 );
		final List< RealLocalizable > truths = new ArrayList< RealLocalizable >( peaks.size() );

		for ( int j = 0; j < 11; j++ )
		{
			final double zf = 20. * j + 10. + j / 10.;
			final double xf = 32.4;
			final double yf = 16.1;

			final double[] posf = new double[] { xf, yf, zf };
			final RealPoint rpos = new RealPoint( posf );
			truths.add( rpos );

			final long xi = Math.round( xf );
			final long yi = Math.round( yf );
			final long zi = Math.round( zf );
			final long[] posi = new long[] { xi, yi, zi };
			final Point pos = new Point( posi );
			peaks.add( pos );

			final RectangleShape shape = new RectangleShape( ( int ) Math.ceil( 3 * spot_sigma ), false );
			final RandomAccess< Neighborhood< FloatType >> nra = shape.neighborhoodsRandomAccessible( I ).randomAccess();
			nra.setPosition( posi );
			final Cursor< FloatType > cursor = nra.get().localizingCursor();
			while ( cursor.hasNext() )
			{
				cursor.fwd();
				cursor.get().setReal( gauss( cursor, rpos, A, spot_sigma ) );
			}
		}

		test( truths, peaks, I, 2 );
	}

	private void test( final List< RealLocalizable > truths, final List< Point > peaks, final Img< FloatType > I, final int dim )
	{
		final SubpixelLocalization< Point, FloatType > localizer = new SubpixelLocalization< Point, FloatType >( I.numDimensions() );
		localizer.setAllowMaximaTolerance( true );
		localizer.setCanMoveOutside( true );
		localizer.setMaxNumMoves( 10 );
		localizer.setReturnInvalidPeaks( true );
		localizer.setNumThreads( 1 );

		final ArrayList< RefinedPeak< Point >> refined = localizer.process( peaks, I, I );

		final RefinedPeakComparator c = new RefinedPeakComparator( dim );
		Collections.sort( refined, c );
		Collections.sort( truths, c );

		for ( int j = 0; j < peaks.size(); j++ )
		{
			testAccuracy( truths.get( j ), refined.get( j ) );
		}
	}

	private void testAccuracy( final RealLocalizable truth, final RealLocalizable estimate )
	{
		for ( int d = 0; d < truth.numDimensions(); d++ )
		{
			final String str = String.format( "Estimated position for dimension %d is off by more than the tolerance. Truth = %s - Estimate = %s.",
					d, Util.printCoordinates( truth ), Util.printCoordinates( estimate ) );
			final double dx = Math.abs( truth.getDoublePosition( d ) - estimate.getDoublePosition( d ) );
			assertTrue( str, dx < TOLERANCE );
		}
	}

	private static final class RefinedPeakComparator implements Comparator< RealLocalizable >
	{
		private final int dim;

		public RefinedPeakComparator( final int dim )
		{
			this.dim = dim;
		}

		@Override
		public int compare( final RealLocalizable o1, final RealLocalizable o2 )
		{
			final double dx = o1.getDoublePosition( dim ) - o2.getDoublePosition( dim );
			return dx == 0 ? 0 : ( dx < 0 ? -1 : 1 );
		}

	}

	private static final double gauss( final RealLocalizable pos, final RealLocalizable center, final double a, final double sigma )
	{
		double dx2 = 0.;
		for ( int d = 0; d < pos.numDimensions(); d++ )
		{
			final double dx = pos.getDoublePosition( d ) - center.getDoublePosition( d );
			dx2 += dx * dx;
		}
		final double arg = -dx2 / ( 2 * sigma * sigma );
		return a * Math.exp( arg );
	}
}
