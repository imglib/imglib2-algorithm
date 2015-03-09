package net.imglib2.algorithm.scalespace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.imglib2.KDTree;
import net.imglib2.algorithm.Algorithm;
import net.imglib2.algorithm.Benchmark;
import net.imglib2.algorithm.scalespace.Blob.SpecialPoint;
import net.imglib2.neighborsearch.RadiusNeighborSearch;
import net.imglib2.neighborsearch.RadiusNeighborSearchOnKDTree;

/**
 * @author Stephan Preibisch
 */
public class AdaptiveNonMaximalSuppression implements Algorithm, Benchmark
{
	private final Collection< DifferenceOfGaussianPeak > detections;

	private long processingTime;

	private String errorMessage = "";

	private final double radiusFactor;

	/**
	 * Performs adaptive non maximal suppression in the local neighborhood of
	 * each detection, separately for minima and maxima. It sets all extrema to
	 * invalid if their value is absolutely smaller than any other in the local
	 * neighborhood of a point. The method getClearedList() can also return an
	 * {@link ArrayList} that only contains valid remaining
	 * {@link DifferenceOfGaussianPeak}s.
	 *
	 * @param detections
	 *            the {@link Collection} of {@link DifferenceOfGaussianPeak}s to
	 *            suppress.
	 * @param radiusFactor
	 *            how far, as multiple of the inspected peak radius, should we
	 *            search for neighbors.
	 */
	public AdaptiveNonMaximalSuppression( final Collection< DifferenceOfGaussianPeak > detections, final double radiusFactor )
	{
		this.detections = detections;
		this.radiusFactor = radiusFactor;

		processingTime = -1;
	}

	/**
	 * Creates a new {@link List} that only contains all
	 * {@link DifferenceOfGaussianPeak}s that are valid.
	 *
	 * @return a new {@link List} of {@link DifferenceOfGaussianPeak}s
	 */
	public List< DifferenceOfGaussianPeak > getClearedList()
	{
		final ArrayList< DifferenceOfGaussianPeak > clearedList = new ArrayList< DifferenceOfGaussianPeak >();

		for ( final DifferenceOfGaussianPeak peak : detections )
			if ( peak.isValid() )
				clearedList.add( peak );

		return clearedList;
	}

	@Override
	public boolean process()
	{
		final long startTime = System.currentTimeMillis();
		if ( detections.size() > 0 )
		{
			// Prepare KDTree inputs
			final List< DifferenceOfGaussianPeak > ldet = new ArrayList< DifferenceOfGaussianPeak >( detections );
			final KDTree< DifferenceOfGaussianPeak > tree = new KDTree< DifferenceOfGaussianPeak >( ldet, ldet );
			final RadiusNeighborSearch< DifferenceOfGaussianPeak > searcher =
					new RadiusNeighborSearchOnKDTree< DifferenceOfGaussianPeak >( tree );

			for ( final DifferenceOfGaussianPeak det : ldet )
			{
				// if it has not been invalidated before we look if it is the
				// highest detection
				if ( det.isValid() )
				{
					final double lradius = radiusFactor * det.getDoublePosition( det.numDimensions() - 1 );
					searcher.search( det, lradius, false );
					final ArrayList< DifferenceOfGaussianPeak > extrema = new ArrayList< DifferenceOfGaussianPeak >();
					for ( int i = 0; i < searcher.numNeighbors(); i++ )
					{
						final DifferenceOfGaussianPeak peak = searcher.getSampler( i ).get();
						if ( ( det.isMax() && peak.isMax() ) || ( det.isMin() && peak.isMin() ) )
						{
							extrema.add( peak );
						}
					}
					invalidateLowerEntries( extrema, det );
				}
			}
		}

		processingTime = System.currentTimeMillis() - startTime;
		return true;
	}

	protected void invalidateLowerEntries( final ArrayList< DifferenceOfGaussianPeak > extrema, final DifferenceOfGaussianPeak centralPeak )
	{
		final double centralValue = Math.abs( centralPeak.getValue() );

		for ( final DifferenceOfGaussianPeak peak : extrema )
			if ( Math.abs( peak.getValue() ) < centralValue )
				peak.setPeakType( SpecialPoint.INVALID );
	}

	@Override
	public boolean checkInput()
	{
		if ( detections == null )
		{
			errorMessage = "List<DifferenceOfGaussianPeak<T>> detections is null.";
			return false;
		}
		else if ( Double.isNaN( radiusFactor ) || radiusFactor <= 0 )
		{
			errorMessage = "Invalud value for radiusFactor: " + radiusFactor + ".";
			return false;
		}
		return true;
	}

	@Override
	public String getErrorMessage()
	{
		return errorMessage;
	}

	@Override
	public long getProcessingTime()
	{
		return processingTime;
	}
}
