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

package net.imglib2.algorithm.hough;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.algorithm.OutputAlgorithm;
import net.imglib2.algorithm.localextrema.LocalExtrema;
import net.imglib2.algorithm.localextrema.LocalExtrema.MaximumCheck;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.RealType;

/**
 * This abstract class provides some basic functionality for use with arbitrary
 * Hough-like transforms.
 *
 * @param <S>
 *            the data type used for storing votes, usually IntType, but
 *            possibly LongType or even DoubleType.
 * @param <T>
 *            the data type of the input image.
 * @author Larry Lindsey
 * @author Yili Zhao
 * @author Gabe Selzer
 */
public abstract class HoughTransform< S extends RealType< S > & NativeType< S >, T extends Type< T > & Comparable< T > > implements OutputAlgorithm< Img< S > >
{

	private String errorMsg;

	private final Img< T > image;

	private final Img< S > voteSpace;

	private RandomAccess< S > voteCursor;

	private ArrayList< long[] > peaks;

	private final double[] peakExclusion;

	private final S one;

	/**
	 * Constructor for a HoughTransform using an ArrayImageFactory to back the
	 * ImageFactory used to generate the voteSpace image.
	 *
	 * @param inputImage
	 *            the image for the HoughTransform to operate over.
	 * @param voteSize
	 *            array indicating the size of the voteSpace. This is passed
	 *            directly into ImageFactory to create a voteSpace image.
	 * @param type
	 *            the Type used for generating the voteSpace image.
	 */
	protected HoughTransform( final Img< T > inputImage, final long[] voteSize, final S type )
	{
		this( inputImage, voteSize, new ArrayImgFactory<>(), type );
	}

	/**
	 * Constructor for a HoughTransform with a specific ImageFactory. Use this
	 * if you have something specific in mind as to how the vote data should be
	 * stored.
	 *
	 * @param inputImage
	 *            the image for the HoughTransform to operate over.
	 * @param voteSize
	 *            array indicating the size of the voteSpace. This is passed
	 *            directly into ImageFactory to create a voteSpace image.
	 * @param voteFactory
	 *            the ImgFactory used to generate the voteSpace image.
	 */
	protected HoughTransform( final Img< T > inputImage, final long[] voteSize, final ImgFactory< S > voteFactory, final S type )
	{
		image = inputImage;
		voteCursor = null;
		voteSpace = voteFactory.create( voteSize, type );
		peaks = null;
		peakExclusion = new double[ voteSize.length ];
		one = type.createVariable();
		one.setOne();
		Arrays.fill( peakExclusion, 0 );
	}

	/**
	 * Place a vote with a specific value.
	 *
	 * @param loc
	 *            the integer array indicating the location where the vote is to
	 *            be placed in voteSpace.
	 * @param vote
	 *            the value of the vote
	 * @return whether the vote was successful. This here particular method
	 *         should always return true.
	 */
	protected boolean placeVote( final int[] loc, final S vote )
	{
		if ( voteCursor == null )
		{
			voteCursor = voteSpace.randomAccess();
		}

		voteCursor.setPosition( loc );
		voteCursor.get().add( vote );

		return true;
	}

	/**
	 * Place a vote of value 1.
	 *
	 * @param loc
	 *            the integer array indicating the location where the vote is to
	 *            be placed in voteSpace.
	 * @return whether the vote was successful. This here particular method
	 *         should always return true.
	 */
	protected boolean placeVote( final int[] loc )
	{
		return placeVote( loc, one );
	}

	/**
	 * Returns an ArrayList of long arrays, representing the positions in the
	 * vote space that correspond to peaks.
	 *
	 * @return an ArrayList of vote space peak locations.
	 */
	public ArrayList< long[] > getPeakList()
	{
		return peaks;
	}

	public boolean setExclusion( final double[] newExclusion )
	{
		if ( newExclusion.length >= peakExclusion.length )
		{
			System.arraycopy( newExclusion, 0, peakExclusion, 0, peakExclusion.length );
			return true;
		}
		return false;
	}

	protected void setErrorMsg( final String msg )
	{
		errorMsg = msg;
	}

	/**
	 * Pick vote space peaks with a {@link LocalExtrema}.
	 *
	 * @return whether peak picking was successful
	 */
	protected boolean pickPeaks()
	{
		S maxValue = voteSpace.firstElement().copy();
		maxValue.setReal( maxValue.getRealDouble() );
		final MaximumCheck< S > maxCheck = new MaximumCheck< S >( maxValue );

		List< Point > peaksList = LocalExtrema.findLocalExtrema( voteSpace, maxCheck );
		peaks = new ArrayList<>();
		
		long[] dims = new long[ image.numDimensions() ];

		for ( Point p : peaksList )
		{
			for ( int i = 0; i < p.numDimensions(); i++ )
			{
				dims[ i ] = p.getLongPosition( i );
			}
			peaks.add( Arrays.copyOf( dims, dims.length ) );
		}

		return true;
	}

	@Override
	public boolean checkInput()
	{
		return voteSpace != null;
	}

	@Override
	public String getErrorMessage()
	{
		return errorMsg;
	}

	public Img< T > getImage()
	{
		return image;
	}

	@Override
	public Img< S > getResult()
	{
		return voteSpace;
	}

}
