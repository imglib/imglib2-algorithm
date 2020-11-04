package net.imglib2.algorithm.stats;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.histogram.BinMapper1d;
import net.imglib2.histogram.Histogram1d;
import net.imglib2.histogram.HistogramNd;
import net.imglib2.histogram.Real1dBinMapper;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

/**
 * This class provides method for computing information metrics (entropy, mutual information)
 * for imglib2.
 * 
 * @author John Bogovic
 */
public class InformationMetrics
{

	/**
	 * Returns the normalized mutual information of the inputs
	 * @param rai the RandomAccessibleInterval
	 * @param ra the RandomAccessible
	 * @return the normalized mutual information
	 */
	public static <T extends RealType< T >> double normalizedMutualInformation(
			IterableInterval< T > dataA,
			IterableInterval< T > dataB,
			double histmin, double histmax, int numBins )
	{
		HistogramNd< T > jointHist = jointHistogram( dataA, dataB, histmin, histmax, numBins );

		double HA = marginalEntropy( jointHist, 0 );
		double HB = marginalEntropy( jointHist, 1 );
		double HAB = entropy( jointHist );
		return ( HA + HB ) / HAB;
	}
	
	/**
	 * Returns the normalized mutual information of the inputs
	 * @param rai the RandomAccessibleInterval
	 * @param ra the RandomAccessible
	 * @return the normalized mutual information
	 */
	public static <T extends RealType< T >> double mutualInformation(
			IterableInterval< T > dataA,
			IterableInterval< T > dataB,
			double histmin, double histmax, int numBins )
	{
		HistogramNd< T > jointHist = jointHistogram( dataA, dataB, histmin, histmax, numBins );

		double HA = marginalEntropy( jointHist, 0 );
		double HB = marginalEntropy( jointHist, 1 );
		double HAB = entropy( jointHist );

		return HA + HB - HAB;
	}
	
	public static <T extends RealType< T >> HistogramNd<T> jointHistogram(
			IterableInterval< T > dataA,
			IterableInterval< T > dataB,
			double histmin, double histmax, int numBins )
	{
		Real1dBinMapper<T> binMapper = new Real1dBinMapper<T>( histmin, histmax, numBins, false );
		ArrayList<BinMapper1d<T>> binMappers = new ArrayList<BinMapper1d<T>>( 2 );
		binMappers.add( binMapper );
		binMappers.add( binMapper );

		List<Iterable<T>> data = new ArrayList<Iterable<T>>( 2 );
		data.add( dataA );
		data.add( dataB );
		return new HistogramNd<T>( data, binMappers );
	}
	
	/**
	 * Returns the joint entropy of the inputs
	 * @param rai the RandomAccessibleInterval
	 * @param ra the RandomAccessible
	 * @return the joint entropy 
	 */
	public static <T extends RealType< T >> double jointEntropy(
			IterableInterval< T > dataA,
			IterableInterval< T > dataB,
			double histmin, double histmax, int numBins )
	{
		return entropy( jointHistogram( dataA, dataB, histmin, histmax, numBins ));
	}

	/**
	 * Returns the entropy of the input.
	 * 
	 * @param data the data
	 * @return the entropy 
	 */
	public static <T extends RealType< T >> double entropy(
			IterableInterval< T > data,
			double histmin, double histmax, int numBins )
	{
		Real1dBinMapper<T> binMapper = new Real1dBinMapper<T>(
				histmin, histmax, numBins, false );
		final Histogram1d<T> hist = new Histogram1d<T>( binMapper );
		hist.countData( data );

		return entropy( hist );
	}

	/**
	 * Computes the entropy of the input 1d histogram.
	 * @param hist the histogram
	 * @return the entropy
	 */ 
	public static < T > double entropy( Histogram1d< T > hist )
	{
		double entropy = 0.0;
		for( int i = 0; i < hist.getBinCount(); i++ )
		{
			double p = hist.relativeFrequency( i, false );
			if( p > 0 )
				entropy -= p * Math.log( p );

		}
		return entropy;
	}

	/**
	 * Computes the entropy of the input nd histogram.
	 * @param hist the histogram
	 * @return the entropy
	 */ 
	public static < T > double entropy( HistogramNd< T > hist )
	{
		double entropy = 0.0;
		Cursor< LongType > hc = hist.cursor();
		long[] pos = new long[ hc.numDimensions() ];

		while( hc.hasNext() )
		{
			hc.fwd();
			hc.localize( pos );
			double p = hist.relativeFrequency( pos, false );
			if( p > 0 )
				entropy -= p * Math.log( p );

		}
		return entropy;
	}
	
	public static < T > double marginalEntropy( HistogramNd< T > hist, int dim )
	{
		
		final long ni = hist.dimension( dim );
		final long total = hist.valueCount();
		long count = 0;
		double entropy = 0.0;
		long ctot = 0;
		for( int i = 0; i < ni; i++ )
		{
			count = subHistCount( hist, dim, i );
			ctot += count;
			double p = 1.0 * count / total;

			if( p > 0 )
				entropy -= p * Math.log( p );
		}
		return entropy;
	}
	
	private static < T > long subHistCount( HistogramNd< T > hist, int dim, int pos )
	{
		long count = 0;
		IntervalView< LongType > hs = Views.hyperSlice( hist, dim, pos );
		Cursor< LongType > c = hs.cursor();
		while( c.hasNext() )
		{
			count += c.next().get();
		}
		return count;
	}
}
