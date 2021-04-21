package net.imglib2.algorithm.metrics.segmentation;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.view.Views;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Interface for segmentation metrics computed by comparing a predicted image to a ground-truth
 * one. Images are expected to be {@link ImgLabeling} or {@link RandomAccessibleInterval} of type {@link IntegerType}.
 * <p>
 * Currently, there is no support for {@link ImgLabeling} with intersecting labels.
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
