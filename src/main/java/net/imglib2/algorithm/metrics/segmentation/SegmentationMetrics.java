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
public interface SegmentationMetrics
{
	/**
	 * Compute a global metrics score between labels from a ground-truth and a predicted image.
	 * <p>
	 * The methods throws an {@link UnsupportedOperationException} if either of the images has intersecting labels.
	 *
	 * @param groundTruth
	 * 		Ground-truth image
	 * @param prediction
	 * 		Predicted image
	 * @param <T>
	 * 		Label type associated to the ground-truth
	 * @param <I>
	 * 		Ground-truth pixel type
	 * @param <U>
	 * 		Label type associated to the prediction
	 * @param <J>
	 * 		Prediction pixel type
	 *
	 * @return Metrics score
	 */
	default < T, I extends IntegerType< I >, U, J extends IntegerType< J > > double computeMetrics(
			final ImgLabeling< T, I > groundTruth,
			final ImgLabeling< U, J > prediction
	)
	{
		if ( hasIntersectingLabels( groundTruth ) || hasIntersectingLabels( prediction ) )
			throw new UnsupportedOperationException( "ImgLabeling with intersecting labels are not supported." );

		return computeMetrics( groundTruth.getIndexImg(), prediction.getIndexImg() );
	}

	/**
	 * Compute a global metrics score between labels from a ground-truth and a predicted image, where the labels
	 * are the unique integer pixel values.
	 *
	 * @param groundTruth
	 * 		Ground-truth image
	 * @param prediction
	 * 		Predicted image
	 * @param <I>
	 * 		Ground-truth pixel type
	 * @param <J>
	 * 		Prediction pixel type
	 *
	 * @return Metrics score
	 */
	< I extends IntegerType< I >, J extends IntegerType< J > > double computeMetrics(
			final RandomAccessibleInterval< I > groundTruth,
			final RandomAccessibleInterval< J > prediction
	);

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
