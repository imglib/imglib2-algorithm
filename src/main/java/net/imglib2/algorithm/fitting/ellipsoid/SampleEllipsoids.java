package net.imglib2.algorithm.fitting.ellipsoid;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.imglib2.RealLocalizable;
import net.imglib2.algorithm.fitting.ellipsoid.DistPointHyperEllipsoid.Result;

public class SampleEllipsoids
{
	public static Ellipsoid sample( final List< ? extends RealLocalizable > points )
	{
		final int numSamples = 10000;
		final double outsideCutoffDistance = 5.0;
		final double insideCutoffDistance = 5.0;
		return sample( points, numSamples, outsideCutoffDistance, insideCutoffDistance );
	}

	public static Ellipsoid sample(
			final List< ? extends RealLocalizable > points,
			final int numSamples,
			final double outsideCutoffDistance,
			final double insideCutoffDistance )
	{
		final int numPointsPerSample = 9;

		final Random rand = new Random( System.currentTimeMillis() );
		final ArrayList< Integer > indices = new ArrayList< Integer >();
		final double[][] coordinates = new double[ numPointsPerSample ][ 3 ];

		Ellipsoid bestEllipsoid = null;
		double bestCost = Double.POSITIVE_INFINITY;
		final Cost costFunction = new AbsoluteDistanceCost( outsideCutoffDistance, insideCutoffDistance );

		for ( int sample = 0; sample < numSamples; ++sample )
		{
			try
			{
				indices.clear();
				for ( int s = 0; s < numPointsPerSample; ++s )
				{
					int i = rand.nextInt( points.size() );
					while ( indices.contains( i ) )
						i = rand.nextInt( points.size() );
					indices.add( i );
					points.get( i ).localize( coordinates[ s ] );
				}
				final Ellipsoid ellipsoid = net.imglib2.algorithm.fitting.ellipsoid.FitEllipsoid.yuryPetrov( coordinates );

				final double cost = costFunction.compute( ellipsoid, points );
				if ( cost < bestCost )
				{
					bestCost = cost;
					bestEllipsoid = ellipsoid;
				}
			}
			catch ( final IllegalArgumentException e )
			{
				e.printStackTrace();
				System.out.println( "oops" );
			}
			catch ( final RuntimeException e )
			{
				System.out.println( "psd" );
			}
		}

		final Ellipsoid refined = fitToInliers( bestEllipsoid, points, outsideCutoffDistance, insideCutoffDistance );
		if ( refined == null )
		{
			System.err.println( "refined ellipsoid == null! This shouldn't happen!");
			return bestEllipsoid;
		}
		return refined;
	}

	public static Ellipsoid fitToInliers(
			final Ellipsoid guess,
			final List< ? extends RealLocalizable > points,
			final double outsideCutoffDistance,
			final double insideCutoffDistance )
	{
		final ArrayList< RealLocalizable > inliers = new ArrayList< RealLocalizable >();
		for ( final RealLocalizable point : points )
		{
			final Result result = DistPointHyperEllipsoid.distPointHyperEllipsoid( point, guess );
			final double d = result.distance;
			final boolean inside = guess.contains( point );
			if ( ( inside && d <= insideCutoffDistance ) || ( !inside && d <= outsideCutoffDistance ) )
				inliers.add( point );
		}

		final double[][] coordinates = new double[ inliers.size() ][ 3 ];
		for ( int i = 0; i < inliers.size(); ++i )
			inliers.get( i ).localize( coordinates[ i ] );

		final Ellipsoid ellipsoid = net.imglib2.algorithm.fitting.ellipsoid.FitEllipsoid.yuryPetrov( coordinates );
		return ellipsoid;
	}

	static interface Cost
	{
		double compute( final Ellipsoid ellipsoid, final List< ? extends RealLocalizable > points );
	}

	static class AbsoluteDistanceCost implements Cost
	{
		private final double outsideCutoff;
		private final double insideCutoff;

		public AbsoluteDistanceCost( final double outsideCutoffDistance, final double insideCutoffDistance )
		{
			outsideCutoff = outsideCutoffDistance;
			insideCutoff = insideCutoffDistance;
		}

		@Override
		public double compute( final Ellipsoid ellipsoid, final List< ? extends RealLocalizable > points )
		{
			double cost = 0;
			for ( final RealLocalizable point : points )
			{
				final Result result = DistPointHyperEllipsoid.distPointHyperEllipsoid( point, ellipsoid );
				final double d = result.distance;
				if ( ellipsoid.contains( point ) )
					cost += Math.min( d, insideCutoff );
				else
					cost += Math.min( d, outsideCutoff );
			}
			return cost;
		}

	}

	static class SquaredDistanceCost implements Cost
	{
		private final double outsideCutoff;
		private final double insideCutoff;

		public SquaredDistanceCost( final double outsideCutoffDistance, final double insideCutoffDistance )
		{
			outsideCutoff = outsideCutoffDistance * outsideCutoffDistance;
			insideCutoff = insideCutoffDistance * insideCutoffDistance;
		}

		@Override
		public double compute( final Ellipsoid ellipsoid, final List< ? extends RealLocalizable > points )
		{
			double cost = 0;
			for ( final RealLocalizable point : points )
			{
				final Result result = DistPointHyperEllipsoid.distPointHyperEllipsoid( point, ellipsoid );
				final double d = result.distance * result.distance;
				if ( ellipsoid.contains( point ) )
					cost += Math.min( d, insideCutoff );
				else
					cost += Math.min( d, outsideCutoff );
			}
			return cost;
		}

	}
}
