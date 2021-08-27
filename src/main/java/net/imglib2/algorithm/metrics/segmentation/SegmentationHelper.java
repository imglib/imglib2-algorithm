/*-
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2021 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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

import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.view.Views;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Helper class used to check if an {@link ImgLabeling} has intersecting labels.
 *
 * @author Joran Deschamps
 */
public class SegmentationHelper
{

	/**
	 * Return a {@link Set} of occurring pixel values in the {@link ImgLabeling} index image.
	 *
	 * @param img
	 * 		Image labeling from which to extract the occurring pixel values
	 * @param <T>
	 * 		Label type associated to the ground-truth
	 * @param <I>
	 * 		Image pixel type
	 *
	 * @return {@link Set} of occurring pixel values
	 */
	static < T, I extends IntegerType< I > > Set< I > getOccurringLabelSets( ImgLabeling< T, I > img )
	{
		Set< I > occurringValues = new HashSet<>();
		for ( I pixel : Views.iterable( img.getIndexImg() ) )
		{
			if ( pixel.getInteger() > 0 )
				occurringValues.add( pixel.copy() );
		}

		return occurringValues;
	}

	/**
	 * Test if the image labeling {@code img} has intersecting labels. Two labels intersect if there
	 * is at least one pixel labeled with both labels.
	 *
	 * @param img
	 * 		Image labeling
	 * @param <T>
	 * 		Label type associated to the ground-truth
	 * @param <I>
	 * 		Image pixel type
	 *
	 * @return True if the image labeling has intersecting labels, false otherwise.
	 */
	static < T, I extends IntegerType< I > > boolean hasIntersectingLabels( ImgLabeling< T, I > img )
	{
		List< Set< T > > labelSets = img.getMapping().getLabelSets();
		for ( I i : getOccurringLabelSets( img ) )
		{
			if ( labelSets.get( i.getInteger() ).size() > 1 )
			{
				return true;
			}
		}
		return false;
	}
}
