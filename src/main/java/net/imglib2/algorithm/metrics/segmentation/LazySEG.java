package net.imglib2.algorithm.metrics.segmentation;

import java.util.Arrays;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.util.Pair;

import static net.imglib2.algorithm.metrics.segmentation.SegmentationHelper.hasIntersectingLabels;

public class LazySEG {

	private int nGT = 0;
	private double sumScores = 0.;

	public < T, I extends IntegerType< I >, U, J extends IntegerType< J > > void addPoint(
			final ImgLabeling< T, I > groundTruth,
			final ImgLabeling< U, J > prediction
	)
	{
		if ( hasIntersectingLabels( groundTruth ) || hasIntersectingLabels( prediction ) )
			throw new UnsupportedOperationException( "ImgLabeling with intersecting labels are not supported." );

		addPoint( groundTruth.getIndexImg(), prediction.getIndexImg() );
	}

	public < I extends IntegerType< I >, J extends IntegerType< J > > void addPoint(
			RandomAccessibleInterval< I > groundTruth,
			RandomAccessibleInterval< J > prediction){

		if ( !Arrays.equals( groundTruth.dimensionsAsLongArray(), prediction.dimensionsAsLongArray() ) )
			throw new IllegalArgumentException( "Image dimensions must match." );

		// compute SEG between the two images
		final Pair< Integer, Double > result = new SEG().runSingle( groundTruth, prediction );

		// ignore NaNs
		if ( Double.compare( result.getB(), Double.NaN ) != 0 )
		{
			nGT += result.getA();
			sumScores += result.getB();
		}
	}

	public double computeScore(){
		return nGT > 0 ? sumScores / (double) nGT : Double.NaN;
	}
}