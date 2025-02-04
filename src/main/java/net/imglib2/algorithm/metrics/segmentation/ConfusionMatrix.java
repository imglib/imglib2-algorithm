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
package net.imglib2.algorithm.metrics.segmentation;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.view.Views;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * In an instance segmentation problem, where each instance is represented by an ensemble
 * of pixels with equal value, the confusion matrix between the ground-truth and a predicted
 * image is a 2D matrix whose elements Cij are equal to the number of pixels shared between
 * ground-truth label i and predicted label j.
 * <p>
 * Additionally, a ConfusionMatrix gives access to the total number of pixels labeled by
 * ground-truth label i or predicted label j.
 * <p>
 * Pixel of value 0 are considered background and ignored in confusion matrix. Therefore, if
 * ground-truth image has M labels, including background, and the prediction image has N, then
 * the confusion matrix is of size (M-1)x(N-1).
 *
 * @param <I>
 * 		Ground-truth pixel type
 * @param <J>
 * 		Predicted pixel type
 */
public class ConfusionMatrix < I extends IntegerType< I >, J extends IntegerType< J > >
{

	// key = index in the matrix, element = corresponding number of pixels
	final private ArrayList< Integer > gtCMHistogram;

	final private ArrayList< Integer > predCMHistogram;

	// rows = ground-truth labels, columns = prediction labels
	final private int[][] confusionMatrix;

	/**
	 * Constructor. Compute the histogram for all ground-truth and predicted labels,
	 * then the 2D array representing the confusion matrix.
	 *
	 * @param groundTruth
	 * 		Ground-truth image
	 * @param prediction
	 * 		Prediction image
	 */
	public ConfusionMatrix( RandomAccessibleInterval< I > groundTruth, RandomAccessibleInterval< J > prediction )
	{

		// histograms: label / number of pixels
		final Map< Integer, Integer > gtHistogram = new LinkedHashMap<>();
		final Map< Integer, Integer > predHistogram = new LinkedHashMap<>();

		final Cursor< I > cGT = Views.iterable( groundTruth ).localizingCursor();
		final RandomAccess< J > cPD = prediction.randomAccess();
		while ( cGT.hasNext() )
		{
			// update gt histogram
			gtHistogram.compute( cGT.next().getInteger(), ( k, v ) -> v == null ? 1 : v + 1 );

			// update prediction histogram
			cPD.setPosition( cGT );
			predHistogram.compute( cPD.get().getInteger(), ( k, v ) -> v == null ? 1 : v + 1 );
		}

		// remove background
		gtHistogram.remove( 0 );
		predHistogram.remove( 0 );

		// prepare confusion matrix
		confusionMatrix = new int[ gtHistogram.size() ][ predHistogram.size() ];

		// maps: labels value / confusion matrix indices
		final LinkedHashMap< Integer, Integer > groundTruthLUT = new LinkedHashMap<>();
		gtHistogram.keySet().forEach( key -> groundTruthLUT.put( key, groundTruthLUT.size() ) );

		final LinkedHashMap< Integer, Integer > predictionLUT = new LinkedHashMap<>();
		predHistogram.keySet().forEach( key -> predictionLUT.put( key, predictionLUT.size() ) );

		// histograms: matrix index / number of pixels
		// LikedHashMap ensures that the order is respected and we will traverse the images in the same way
		// we will build the confusion matrix
		gtCMHistogram = new ArrayList<>( gtHistogram.values() );
		predCMHistogram = new ArrayList<>( predHistogram.values() );

		// populate confusion matrix
		cGT.reset();
		while ( cGT.hasNext() )
		{
			cGT.next();
			cPD.setPosition( cGT );

			int gtLabel = cGT.get().getInteger();
			int predLabel = cPD.get().getInteger();

			// ignore background (absent from the lists)
			if ( gtLabel > 0 && predLabel > 0 )
			{
				int i = groundTruthLUT.get( gtLabel );
				int j = predictionLUT.get( predLabel );

				confusionMatrix[ i ][ j ] += 1;
			}
		}
	}

	/**
	 * Return the number of pixels corresponding to the ground-truth
	 * label indexed by {@code labelIndex}.
	 *
	 * @param labelIndex
	 * 		Index of the label
	 *
	 * @return Number of pixels, or -1 if the index is out of bounds
	 */
	public int getGroundTruthLabelSize( int labelIndex )
	{
		if ( labelIndex < 0 || labelIndex >= gtCMHistogram.size() )
			return -1;

		return gtCMHistogram.get( labelIndex );
	}

	/**
	 * Return the number of pixels corresponding to the prediction
	 * label indexed by {@code labelIndex}.
	 *
	 * @param labelIndex
	 * 		Index of the label
	 *
	 * @return Number of pixels, or -1 if the index is out of bounds
	 */
	public int getPredictionLabelSize( int labelIndex )
	{
		if ( labelIndex < 0 || labelIndex >= predCMHistogram.size() )
			return -1;

		return predCMHistogram.get( labelIndex );
	}

	/**
	 * Return the number of pixels shared by the ground-truth label
	 * indexed by {@code gtIndex} and the prediction label indexed
	 * by {@code predIndex}.
	 *
	 * @param gtLabelIndex
	 * 		Index of the ground-truth label
	 * @param predLabelIndex
	 * 		Index of the prediction label
	 *
	 * @return Number of pixels shared by the two labels
	 */
	public int getIntersection( int gtLabelIndex, int predLabelIndex )
	{
		if ( getNumberGroundTruthLabels() == 0 || getNumberPredictionLabels() == 0 )
			return 0;

		return confusionMatrix[ gtLabelIndex ][ predLabelIndex ];
	}

	/**
	 * Return the number of ground-truth labels.
	 *
	 * @return Number of ground-truth labels
	 */
	public int getNumberGroundTruthLabels()
	{
		return gtCMHistogram.size();
	}

	/**
	 * Return the number of prediction labels.
	 *
	 * @return Number of prediction labels
	 */
	public int getNumberPredictionLabels()
	{
		return predCMHistogram.size();
	}
}
