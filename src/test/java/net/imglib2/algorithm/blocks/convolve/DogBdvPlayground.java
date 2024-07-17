/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2024 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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
package net.imglib2.algorithm.blocks.convolve;

import static net.imglib2.util.Util.safeInt;

import java.util.Arrays;
import java.util.function.Function;

import bdv.cache.SharedQueue;
import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvSource;
import bdv.util.volatiles.VolatileViews;
import bdv.viewer.DisplayMode;
import ij.IJ;
import ij.ImagePlus;
import net.imglib2.EuclideanSpace;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.algorithm.blocks.BlockAlgoUtils;
import net.imglib2.algorithm.blocks.BlockProcessor;
import net.imglib2.algorithm.blocks.BlockSupplier;
import net.imglib2.algorithm.blocks.DefaultUnaryBlockOperator;
import net.imglib2.algorithm.blocks.UnaryBlockOperator;
import net.imglib2.algorithm.blocks.util.BlockProcessorSourceInterval;
import net.imglib2.blocks.SubArrayCopy;
import net.imglib2.blocks.TempArray;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.PrimitiveType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

public class DogBdvPlayground
{
	public static void main( String[] args )
	{
		System.setProperty("apple.laf.useScreenMenuBar", "true");

		final String fn = "/Users/pietzsch/workspace/data/e002_stack_fused-8bit.tif";
		final ImagePlus imp = IJ.openImage( fn );
		final Img< UnsignedByteType > img = ImageJFunctions.wrapByte( imp );



		final int sensitivity = 4;
		final double sigmaSmaller = 2;
		final double sigmaLarger = computeSigma2( sigmaSmaller, sensitivity );

		System.out.println( "sigmaSmaller = " + sigmaSmaller );
		System.out.println( "sigmaLarger = " + sigmaLarger );


		final BlockSupplier< UnsignedByteType > blocks = BlockSupplier.of( Views.extendMirrorDouble( img ) );

		final Function< BlockSupplier< UnsignedByteType >, UnaryBlockOperator< UnsignedByteType, UnsignedByteType > >
				g1 = Convolve.gauss( sigmaSmaller );
		final Function< BlockSupplier< UnsignedByteType >, UnaryBlockOperator< UnsignedByteType, UnsignedByteType > >
				g2 = Convolve.gauss( sigmaLarger );

		final UnaryBlockOperator< UnsignedByteType, UnsignedByteType > gOp1 = g1.apply( blocks );
		final UnaryBlockOperator< UnsignedByteType, UnsignedByteType > gOp2 = g2.apply( blocks );

		final BlockProcessor< ?, ? > gProc1 = gOp1.blockProcessor();
		final BlockProcessor< ?, ? > gProc2 = gOp2.blockProcessor();

		final FinalInterval targetInterval = Intervals.createMinSize( 0, 0, 0, 10, 10, 10 );
		System.out.println( "targetInterval = " + Intervals.toString( targetInterval ) );

		gProc1.setTargetInterval( targetInterval );
		System.out.println( "gProc1.getSourceInterval() = " + Intervals.toString( gProc1.getSourceInterval() ) );;

		gProc2.setTargetInterval( targetInterval );
		System.out.println( "gProc2.getSourceInterval() = " + Intervals.toString( gProc2.getSourceInterval() ) );;

		final JoinedlockProcessor jp = new JoinedlockProcessor(
				gOp1.getSourceType(), gOp1.getTargetType(), gProc1, gProc2 );
		jp.setTargetInterval( targetInterval );
		System.out.println( "jp.getSourceInterval() = " + Intervals.toString( jp.getSourceInterval() ) );


		final UnsignedByteType type = new UnsignedByteType();
		final UnaryBlockOperator< UnsignedByteType, UnsignedByteType > joined = new DefaultUnaryBlockOperator<>( type, type, 3, 3, jp );






		final long[] dimensions = img.dimensionsAsLongArray();
		final int[] cellDimensions = { 64, 64, 64 };
		final Img< UnsignedByteType > convolved = BlockAlgoUtils.cellImg(
				blocks.andThen( joined ),
				dimensions,
				cellDimensions );

		final BdvSource bdv = BdvFunctions.show(
				img,
				"img",
				Bdv.options() );
		bdv.setColor( new ARGBType( 0xffffff ) );
		bdv.setDisplayRange( 0, 255 );
		bdv.getBdvHandle().getViewerPanel().setDisplayMode( DisplayMode.SINGLE );

		final BdvSource out = BdvFunctions.show(
				VolatileViews.wrapAsVolatile( convolved, new SharedQueue( 8, 1 ) ),
				"DoG",
				Bdv.options().addTo( bdv )
		);
		out.setDisplayRange( 0, 255 );
//		out.setColor( new ARGBType( 0x00ff00 ) );
		out.setColor( new ARGBType( 0xffffff ) );

	}

	private static double computeSigma2(final double sigma1, final int stepsPerOctave) {
		final double k = Math.pow(2f, 1f / stepsPerOctave);
		return sigma1 * k;
	}


	/*
	 * TODO:
	 *   Do we want a generic joining processor that always does the right thing?
	 *   That is:
	 *     - Take the union of the source intervals of the merged processors.
	 *     - Use a separate sourceBuffer TempArray (union could be bigger than
	 *       both of the processors' source intervals.
	 *     - If the union matches the source interval of one processor (or
	 *       both), use it directly as input.
	 *     - Otherwise SubArrayCopy.
	 *   ==>
	 *     Now that I wrote that... Yes! Let's do it
	 *
	 *
	 * TODO:
	 *   [+] Create appropriate sourceBuffer (TempArray? mechanics?)
	 * 	     - just pass in PrimitiveType...?
	 * 	 [ ] will that also work for SubArrayCopy?
	 *   [ ] Implement logic
	 *   [ ] Verify that types do match (?)
	 */





	// TODO make generic:
	//  O0, O1, T0, T1,
	//  join O0, O1 --> O
	static class JoinedlockProcessor< I, O > implements BlockProcessor< I, O >
	{
		private final BlockProcessor< I, O > p0;
		private final BlockProcessor< I, O > p1;

		private final TempArray< I > tempArraySrc;
		private final TempArray< O > tempArrayDest0;
		private final TempArray< O > tempArrayDest1;
		private final SubArrayCopy.Typed< I, I > copy;

		private long[] sourcePos;
		private int[] sourceSize;
		private int sourceLength;

		private long[] destPos;
		private int[] destSize;
		private int destLength;

		private final BlockProcessorSourceInterval sourceInterval;

		public < S extends NativeType< S >, T extends NativeType< T > > JoinedlockProcessor(
				final S sourceType, final T targetType,
				final BlockProcessor< I, O > p0,
				final BlockProcessor< I, O > p1 )
		{
			this.p0 = p0; // TODO .independentCopy() ???
			this.p1 = p1; // TODO .independentCopy() ???

			final PrimitiveType sourcePrimitiveType = sourceType.getNativeTypeFactory().getPrimitiveType();
			final PrimitiveType targetPrimitiveType = targetType.getNativeTypeFactory().getPrimitiveType();
			tempArraySrc = TempArray.forPrimitiveType( sourcePrimitiveType );
			tempArrayDest0 = TempArray.forPrimitiveType( targetPrimitiveType );
			tempArrayDest1 = TempArray.forPrimitiveType( targetPrimitiveType );
			copy = SubArrayCopy.forPrimitiveType( sourcePrimitiveType );

			sourceInterval = new BlockProcessorSourceInterval( this );
		}

		private JoinedlockProcessor( JoinedlockProcessor< I, O > processor )
		{
			p0 = processor.p0.independentCopy();
			p1 = processor.p1.independentCopy();
			tempArraySrc = processor.tempArraySrc.newInstance();
			tempArrayDest0 = processor.tempArrayDest0.newInstance();
			tempArrayDest1 = processor.tempArrayDest1.newInstance();
			copy = processor.copy;
			sourceInterval = new BlockProcessorSourceInterval( this );
		}

		@Override
		public JoinedlockProcessor< I, O > independentCopy()
		{
			return new JoinedlockProcessor<>( this );
		}

		@Override
		public void setTargetInterval( final Interval interval )
		{
			p0.setTargetInterval( interval );
			p1.setTargetInterval( interval );

			final long[] sourcePos0 = p0.getSourcePos();
			final long[] sourcePos1 = p1.getSourcePos();
			final int[] sourceSize0 = p0.getSourceSize();
			final int[] sourceSize1 = p1.getSourceSize();

			final int m = interval.numDimensions();
			if ( destPos == null || destPos.length != m )
			{
				destPos = new long[ m ];
				destSize = new int[ m ];
			}
			interval.min( destPos );
			Arrays.setAll(destSize, d -> Util.safeInt( interval.dimension( d ) ) );
			destLength = safeInt( Intervals.numElements( destSize ) );

			// Take the union of the source intervals of the merged processors.
			// Use a separate sourceBuffer TempArray (because the union could be
			// bigger than both of the processors' source intervals).
			final int n = sourcePos0.length;
			assert n == sourcePos1.length;
			if ( sourcePos == null || sourcePos.length != n )
			{
				sourcePos = new long[ n ];
				sourceSize = new int[ n ];
			}
			Arrays.setAll(sourcePos, d -> Math.min( sourcePos0[ d ], sourcePos1[ d ] ) );
			Arrays.setAll(sourceSize, d -> Util.safeInt(Math.max(
					sourcePos0[ d ] + sourceSize0[ d ],
					sourcePos1[ d ] + sourceSize1[ d ] ) - sourcePos[ d ] ) );
			sourceLength = safeInt( Intervals.numElements( sourceSize ) );
		}

		// TODO
//		@Override
//		public void setTargetInterval( final long[] srcPos, final int[] size )
//		{
//		}

		@Override
		public long[] getSourcePos()
		{
			return sourcePos;
		}

		@Override
		public int[] getSourceSize()
		{
			return sourceSize;
		}

		@Override
		public Interval getSourceInterval()
		{
			return sourceInterval;
		}

		@Override
		public I getSourceBuffer()
		{
			return tempArraySrc.get( sourceLength );
		}


		@Override
		public void compute( final I src, final O dest )
		{
			// If the source interval union matches the source interval of one
			// processor (or both), use src directly as input. Otherwise, use
			// SubArrayCopy.

			// TODO
			//  [+] for pi in {p0, p1)
			//    [+] extract srci from src
			//    [+] get temp desti
			//    [+] compute into desti
			//  [ ] extract to separate class (allocates everything in constructor)
			//          inputs:
			//          src, sourceInterval, p0
			//  [ ] compute dest1 - dest0 (or the other way around, figure that out)


			// p0
			final I src0 = new SubIntervalSource<>( sourcePos.length, copy ).extract( src, sourcePos, sourceSize, p0 );
			final O dest0 = tempArrayDest0.get( destLength );
			p0.compute( src0, dest0 );

			// TODO -- for checking the above, we just copy dest0 to dest
			System.arraycopy( dest0, 0, dest, 0, destLength );




			// TODO
//			p0.compute( src, p1.getSourceBuffer() );
//			p1.compute( p1.getSourceBuffer(), dest );
		}

		static final class SubIntervalSource< I > implements EuclideanSpace
		{
			private final int[] zeros;
			private final int[] relativeSrcPos; // TODO rename to "offset" / "pos" / ...?
			private final SubArrayCopy.Typed< I, I > copy;

			SubIntervalSource( final int numDimensions, final SubArrayCopy.Typed< I, I > copy )
			{
				relativeSrcPos = new int[ numDimensions ];
				zeros = new int[ numDimensions ];
				this.copy = copy;
			}

			@Override
			public int numDimensions()
			{
				return zeros.length;
			}

			I extract( final I src, final long[] sourcePos, final int[] sourceSize, final BlockProcessor< I, ? > p0 )
			{
				if ( Arrays.equals( sourcePos, p0.getSourcePos() ) && Arrays.equals( sourceSize, p0.getSourceSize() ) )
				{
					return src;
				}
				else
				{
					I buf = p0.getSourceBuffer();
					Arrays.setAll( relativeSrcPos, d -> ( int ) ( p0.getSourcePos()[ d ] - sourcePos[ d ] ) );
					copy.copy( src, sourceSize, relativeSrcPos, buf, p0.getSourceSize(), zeros, p0.getSourceSize() );
	//				copy.copyNDRangeRecursive(  ); // TODO: avoid allocating some arrays that could be allocated per processor
					return buf;
				}
			}
		}
	}
}
