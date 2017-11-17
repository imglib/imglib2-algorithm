package net.imglib2.algorithm.ransac.RansacModels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;


import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.algorithm.ransac.RansacModels.DistPointHyperEllipsoid.Result;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.FloatArray;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Pair;
import net.imglib2.util.Util;
import net.imglib2.util.ValuePair;

public class RansacEllipsoid {

	static int maxtolerance = 20;
	
	
	
	public static ArrayList<Pair<Ellipsoid, List<RealLocalizable>>> Allsamples(
			final List<? extends RealLocalizable> points, final int numSamples, final double outsideCutoffDistance,
			final double insideCutoffDistance, final double maxSize, final double minSize, final double minpoints, double maxdist) {

		boolean fitted;

		
		
		
		final List<RealLocalizable> remainingPoints = new ArrayList<RealLocalizable>();
		if (points != null)
			remainingPoints.addAll(points);
		
		SortRealLocalizable.SortPoints(remainingPoints);
		
		HashMap<Integer, List<RealLocalizable>> queue = QueueList(remainingPoints, maxdist);
		final ArrayList<Pair<Ellipsoid, List<RealLocalizable>>> segments = new ArrayList<Pair<Ellipsoid, List<RealLocalizable>>>();
		for (Integer key : queue.keySet()) {
			
			List<RealLocalizable> closePoints = queue.get(key);
		

			do {

				

				
				if (remainingPoints.size() > maxtolerance) {
					fitted = false;
				
				final Pair<Ellipsoid, List<RealLocalizable>> f = sample(closePoints, numSamples,
						outsideCutoffDistance, insideCutoffDistance, maxSize, minSize, minpoints);

				if (f != null && f.getB().size() > 0) {

					fitted = true;

					segments.add(f);

					final List<RealLocalizable> inlierPoints = new ArrayList<RealLocalizable>();
					for (final RealLocalizable p : f.getB())
						inlierPoints.add(p);
					remainingPoints.removeAll(inlierPoints);

					
				}
				
				
				
			} else {

				fitted = true;
				break;

			}

		}
		
		while (fitted);
			
		}
		
		

		
		return segments;

	}
	
	
	public static HashMap<Integer, List<RealLocalizable>> QueueList(final List<RealLocalizable> points, final double maxdist){
		
		
		List<RealLocalizable> sublist = new ArrayList<RealLocalizable>();
		List<RealLocalizable> copylist = points.subList(0, points.size() - 1);
		HashMap<Integer, List<RealLocalizable>> queue = new HashMap<Integer, List<RealLocalizable>>();
		
		do {
			
		int count = 0;	
		RealLocalizable firstPoint = copylist.get(0);
		sublist.add(firstPoint);
		for(int index = 1; index < copylist.size(); ++index) {
			
			RealLocalizable nextPoint = copylist.get(index);
			
			double dist = DistanceSq(firstPoint, nextPoint);
			
			if (dist < maxdist) {
				
				sublist.add(nextPoint);
				
			}
			
			
			
		}
		
		copylist.removeAll(sublist);
		
		queue.put(count, sublist);
		
		
		
		
		count++;
		}while(copylist.size() > 0);
		
		return queue;
		
	}
	

	public static Pair<Ellipsoid, List<RealLocalizable>> sample(final List<RealLocalizable> points,
			final int numSamples, final double outsideCutoffDistance, final double insideCutoffDistance, double maxSize,
			double minSize, double minpoints) {
		final int numPointsPerSample = 9;

		final Random rand = new Random(System.currentTimeMillis());
		final ArrayList<Integer> indices = new ArrayList<Integer>();
		final double[][] coordinates = new double[numPointsPerSample][3];

		Ellipsoid bestEllipsoid = null;
		double bestCost = Double.POSITIVE_INFINITY;
		final Cost costFunction = new AbsoluteDistanceCost(outsideCutoffDistance, insideCutoffDistance);

		for (int sample = 0; sample < numSamples; ++sample) {

			try {

				indices.clear();
				for (int s = 0; s < numPointsPerSample; ++s) {
					int i = rand.nextInt(points.size());
					while (indices.contains(i))
						i = rand.nextInt(points.size());
					indices.add(i);
					points.get(i).localize(coordinates[s]);

				}

				final Ellipsoid ellipsoid = FitEllipsoid.yuryPetrov(coordinates);

				final double cost = costFunction.compute(ellipsoid, points);
				if (cost < bestCost) {
					bestCost = cost;
					bestEllipsoid = ellipsoid;

				}
			} catch (final IllegalArgumentException e) {
				
			} catch (final RuntimeException e) {
				
			}
		}

		Pair<Ellipsoid, List<RealLocalizable>> refined = fitToInliers(bestEllipsoid, points,
				outsideCutoffDistance, insideCutoffDistance, maxSize, minSize, minpoints);
		if (refined == null) {

			
			
			return new ValuePair<Ellipsoid, List<RealLocalizable>>(bestEllipsoid, points);
			
			
		}
		return refined;
	}

	public static Pair<Ellipsoid, List<RealLocalizable>> fitToInliers(final Ellipsoid guess,
			final List<? extends RealLocalizable> points, final double outsideCutoffDistance,
			final double insideCutoffDistance, double maxSize, double minSize, double minpoints) {
		final ArrayList<RealLocalizable> inliers = new ArrayList<RealLocalizable>();
		for (final RealLocalizable point : points) {
			final Result result = DistPointHyperEllipsoid.distPointHyperEllipsoid(point, guess);
			final double d = result.distance;
			final boolean inside = guess.contains(point);
		
		//	System.out.println(point.getDoublePosition(0) + " " + point.getDoublePosition(1));
			if ((inside && d <= insideCutoffDistance)
					|| (!inside && d <= outsideCutoffDistance) )
				inliers.add(point);
			
			
		}
		
		
		if (inliers.size() < 9){
			
			for (final RealLocalizable point : points) {
			
				
					inliers.add(point);
				
				
			}
			
		}
		
		

		final double[][] coordinates = new double[inliers.size()][3];
		for (int i = 0; i < inliers.size(); ++i)
			inliers.get(i).localize(coordinates[i]);

		System.out.println("Fitting on Co-ordinates " + coordinates.length);
		Ellipsoid ellipsoid = FitEllipsoid.yuryPetrov(coordinates);

		final Pair<Ellipsoid, List<RealLocalizable>> Allellipsoids = new ValuePair<Ellipsoid, List<RealLocalizable>>(
				ellipsoid, inliers);
		return Allellipsoids;

	}

	static interface Cost {
		double compute(final Ellipsoid ellipsoid, final List<? extends RealLocalizable> points);
	}

	static class AbsoluteDistanceCost implements Cost {
		private final double outsideCutoff;
		private final double insideCutoff;

		public AbsoluteDistanceCost(final double outsideCutoffDistance, final double insideCutoffDistance) {
			outsideCutoff = outsideCutoffDistance;
			insideCutoff = insideCutoffDistance;
		}

		@Override
		public double compute(final Ellipsoid ellipsoid, final List<? extends RealLocalizable> points) {
			double cost = 0;
			for (final RealLocalizable point : points) {
				final Result result = DistPointHyperEllipsoid.distPointHyperEllipsoid(point, ellipsoid);
				final double d = result.distance;
				if (ellipsoid.contains(point))
					cost += Math.min(d, insideCutoff);
				else
					cost += Math.min(d, outsideCutoff);
			}
			return cost;
		}

	}

	static class SquaredDistanceCost implements Cost {
		private final double outsideCutoff;
		private final double insideCutoff;

		public SquaredDistanceCost(final double outsideCutoffDistance, final double insideCutoffDistance) {
			outsideCutoff = outsideCutoffDistance * outsideCutoffDistance;
			insideCutoff = insideCutoffDistance * insideCutoffDistance;
		}

		@Override
		public double compute(final Ellipsoid ellipsoid, final List<? extends RealLocalizable> points) {
			double cost = 0;
			for (final RealLocalizable point : points) {
				final Result result = DistPointHyperEllipsoid.distPointHyperEllipsoid(point, ellipsoid);
				final double d = result.distance * result.distance;
				if (ellipsoid.contains(point))
					cost += Math.min(d, insideCutoff);
				else
					cost += Math.min(d, outsideCutoff);
			}
			return cost;
		}

	}
	
	
	
	public static double DistanceSq(final RealLocalizable pointA, final RealLocalizable pointB) {
		
		
		double distance = 0;
		int numDim = pointA.numDimensions();
		
		for (int d = 0; d < numDim; ++d) {
			
			distance+= (pointA.getDoublePosition(d) - pointB.getDoublePosition(d)) * (pointA.getDoublePosition(d) - pointB.getDoublePosition(d)); 
			
		}
		
		return distance;
	}
	

}
