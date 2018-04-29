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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.Point;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.localextrema.LocalExtrema.MaximumCheck;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.converter.Converters;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.DoubleArray;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

/**
 *
 * @author Philipp Hanslovsky
 *
 */
public class LocalExtremaTest
{

	public static Comparator< Point > POINT_COMPARATOR = ( p1, p2 ) -> {
		final int nDim = p1.numDimensions();
		final int dimComp = Integer.compare( nDim, p2.numDimensions() );
		if ( dimComp == 0 )
		{
			for ( int d = 0; d < nDim; ++d )
			{
				final int comp = Long.compare( p1.getLongPosition( d ), p2.getLongPosition( d ) );
				if ( comp == 0 )
					continue;
				else
					return comp;
			}
			return 0;
		}
		else
			return dimComp;
	};

	public static int N_THREADS = Runtime.getRuntime().availableProcessors();

	public static ExecutorService ES = Executors.newFixedThreadPool( N_THREADS );

	public static int N_TASKS = 3 * N_THREADS;

	@Test
	public void test1D()
	{
		final double[] data = { 3.0, 0.0, 0.0, 1.0, 0.0, 1.0 };
		final Point[] pointsNoBorder = { new Point( 3l ) };
		final Point[] points = { new Point( 0l ), new Point( 3l ), new Point( 5l ) };

		final List< Point > pnbList = Arrays.asList( pointsNoBorder );
		final List< Point > pList = Arrays.asList( points );

		Collections.sort( pnbList, POINT_COMPARATOR );
		Collections.sort( pList, POINT_COMPARATOR );

		final RectangleShape shape = new RectangleShape( 1, true );

		final MaximumCheck< DoubleType > check = new LocalExtrema.MaximumCheck<>( new DoubleType( 0.0 ) );

		final ArrayImg< DoubleType, DoubleArray > img = ArrayImgs.doubles( data, data.length );

		test( img, check, shape, pnbList, POINT_COMPARATOR );
		test( Views.extendValue( img, new DoubleType( -1.0 ) ), img, check, shape, pList, POINT_COMPARATOR );
	}

	// reference must be sorted
	public static < T, P > void test(
			final RandomAccessibleInterval< T > img,
			final LocalExtrema.LocalNeighborhoodCheck< P, T > check,
			final Shape shape,
			final List< P > reference,
			final Comparator< P > comp )
	{
		final List< P > extrema = LocalExtrema.findLocalExtrema( img, check, shape );
		Assert.assertEquals( reference.size(), extrema.size() );
		Collections.sort( extrema, comp );
		Assert.assertEquals( reference, extrema );
	}

	public static < T, P > void test(
			final RandomAccessible< T > img,
			final Interval interval,
			final LocalExtrema.LocalNeighborhoodCheck< P, T > check,
			final Shape shape,
			final List< P > reference,
			final Comparator< P > comp )
	{
		final List< P > extrema = LocalExtrema.findLocalExtrema( img, interval, check, shape );
		Assert.assertEquals( reference.size(), extrema.size() );
		Collections.sort( extrema, comp );
		Assert.assertEquals( reference, extrema );
	}

	@Test
	public void testCheckBoard()
	{
		final int size = 4;
		final RandomAccessibleInterval< IntType > cb = checkerBoard( new IntType(), size );

		final Point[] pointsNoBorder = { new Point( 1, 2 ), new Point( 2, 1 ) };
		final Point[] points = {
				new Point( 0, 1 ), new Point( 0, 3 ),
				new Point( 1, 0 ), new Point( 1, 2 ),
				new Point( 2, 1 ), new Point( 2, 3 ),
				new Point( 3, 0 ), new Point( 3, 2 )
		};

		final MaximumCheck< IntType > check = new LocalExtrema.MaximumCheck<>( new IntType( 0 ) );
		final RectangleShape shape = new RectangleShape( 1, true );

		final RandomAccessibleInterval< IntType > inverted = Converters.convert( cb, ( s, t ) -> {
			t.set( -s.get() );
		}, new IntType() );
		compare( Arrays.asList( pointsNoBorder ), LocalExtrema.findLocalExtrema( cb, check, shape ), POINT_COMPARATOR );
		compare( Arrays.asList( pointsNoBorder ), LocalExtrema.findLocalExtrema( inverted, new LocalExtrema.MinimumCheck<>( new IntType( 0 ) ), shape ), POINT_COMPARATOR );
		compare( Arrays.asList( pointsNoBorder ), LocalExtrema.findLocalExtrema( cb, LocalExtrema.shrink( cb, new long[] { 1, 1 } ), check ), POINT_COMPARATOR );

		compare( Arrays.asList( points ), LocalExtrema.findLocalExtrema( Views.extendValue( cb, new IntType( 0 ) ), cb, check, shape ), POINT_COMPARATOR );
		compare( Arrays.asList( points ), LocalExtrema.findLocalExtrema( Views.extendValue( cb, new IntType( 0 ) ), cb, check ), POINT_COMPARATOR );

		final List< Point > pointsOnBorder = Arrays.stream( points ).filter( p -> p.getLongPosition( 0 ) == 0 || p.getLongPosition( 1 ) == 0 || p.getLongPosition( 0 ) == size - 1 || p.getLongPosition( 1 ) == size - 1 ).collect( Collectors.toList() );

		final RandomAccessibleInterval< IntType > emptyInside = chessBoardBorderOnly( new IntType(), size );
		Assert.assertEquals( 0, LocalExtrema.findLocalExtrema( emptyInside, check, shape ).size() );
		compare( pointsOnBorder, LocalExtrema.findLocalExtrema( Views.extendValue( emptyInside, new IntType( 0 ) ), emptyInside, check, shape ), POINT_COMPARATOR );

		compare( Arrays.asList( pointsNoBorder ), LocalExtrema.findLocalExtrema( cb, Intervals.expand( cb, -1 ), check, new RectangleShape( 1, true ) ), POINT_COMPARATOR );
		compare( Arrays.asList( pointsNoBorder ), LocalExtrema.findLocalExtrema( cb, check ), POINT_COMPARATOR );
		compare( Arrays.asList( pointsNoBorder ), LocalExtrema.findLocalExtrema( cb, check, new RectangleShape( 1, true ) ), POINT_COMPARATOR );

		compare( new ArrayList<>(), LocalExtrema.findLocalExtrema( inverted, new LocalExtrema.MinimumCheck<>( new IntType( Integer.MIN_VALUE ) ), shape ), POINT_COMPARATOR );
		compare( new ArrayList<>(), LocalExtrema.findLocalExtrema( cb, new LocalExtrema.MaximumCheck<>( new IntType( Integer.MAX_VALUE ) ), shape ), POINT_COMPARATOR );
	}

	public static < P > void compare( final List< P > reference, final List< P > comparison, final Comparator< P > comp )
	{
		Assert.assertEquals( reference.size(), comparison.size() );
		Collections.sort( reference, comp );
		Collections.sort( comparison, comp );
		Assert.assertEquals( reference, comparison );
	}

	@Test
	public void testMultiThreaded() throws InterruptedException, ExecutionException
	{
		final int size = 10;

		final RandomAccessibleInterval< IntType > cb = checkerBoard( new IntType(), size );
		final MaximumCheck< IntType > check = new LocalExtrema.MaximumCheck<>( new IntType( 0 ) );
		final RectangleShape shape = new RectangleShape( 1, true );

		final long nExtrema = Arrays.stream( Intervals.dimensionsAsLongArray( cb ) ).map( v -> ( v - 2 ) ).reduce( 1, ( i, j ) -> i * j ) / 2;

		final List< Point > extrema1 = LocalExtrema.findLocalExtrema( cb, LocalExtrema.shrink( cb, LocalExtrema.getRequiredBorderSize( shape, cb.numDimensions() ) ), check, shape, ES, N_TASKS );
		final List< Point > extrema2 = LocalExtrema.findLocalExtrema( cb, check, shape, ES, N_TASKS );
		final List< Point > extrema3 = LocalExtrema.findLocalExtrema( cb, check, shape, ES, 1 );
		@SuppressWarnings( "deprecation" )
		final List< Point > extrema4 = LocalExtrema.findLocalExtrema( cb, check, ES );
		final List< Point > reference = LocalExtrema.findLocalExtrema( cb, check, shape );

		Assert.assertEquals( nExtrema, reference.size() );
		Assert.assertEquals( reference.size(), extrema1.size() );
		Assert.assertEquals( reference.size(), extrema2.size() );
		Assert.assertEquals( reference.size(), extrema3.size() );
		Assert.assertEquals( reference.size(), extrema4.size() );

		Collections.sort( extrema1, POINT_COMPARATOR );
		Collections.sort( extrema2, POINT_COMPARATOR );
		Collections.sort( extrema3, POINT_COMPARATOR );
		Collections.sort( extrema4, POINT_COMPARATOR );
		Collections.sort( reference, POINT_COMPARATOR );

		Assert.assertEquals( reference, extrema1 );
		Assert.assertEquals( reference, extrema2 );
		Assert.assertEquals( reference, extrema3 );
		Assert.assertEquals( reference, extrema4 );

	}

	// works for 2D only?
	public static < T extends IntegerType< T > & NativeType< T > > RandomAccessibleInterval< T > checkerBoard( final T t, final long size )
	{
		final ArrayImg< T, ? > img = new ArrayImgFactory<>( t ).create( size, size );
		for ( long x = 0; x < size; ++x )
		{
			final IntervalView< T > hs = Views.hyperSlice( img, 0, x );
			final Cursor< T > c = hs.cursor();
			for ( long val = x; c.hasNext(); ++val )
				c.next().setInteger( val % 2 );
		}
		return img;
	}

	public static < T extends IntegerType< T > & NativeType< T > > RandomAccessibleInterval< T > chessBoardBorderOnly( final T t, final long size )
	{
		final RandomAccessibleInterval< T > cb = checkerBoard( t, size );
		for ( final T i : Views.interval( cb, Intervals.expand( cb, -1 ) ) )
			i.setZero();
		return cb;
	}

	@Test
	public void testGetBiggestDimension()
	{
		final int nDimensions = 5;
		for ( final int d : IntStream.range( 0, nDimensions ).toArray() )
			Assert.assertEquals( d, LocalExtrema.getBiggestDimension( new FinalInterval( IntStream.range( 0, nDimensions ).mapToLong( i -> i == d ? 2 : 1 ).toArray() ) ) );
	}

}
