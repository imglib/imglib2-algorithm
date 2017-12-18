package net.imglib2.algorithm.ransac.RansacModels;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ij.gui.EllipseRoi;
import ij.gui.Roi;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.algorithm.ransac.RansacModels.DistPointHyperEllipsoid.Result;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.imagej.DrawingTool;
import net.imglib2.Cursor;
import net.imglib2.KDTree;
import net.imglib2.RandomAccess;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.FloatArray;
import net.imglib2.neighborsearch.NearestNeighborSearchOnKDTree;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Pair;
import net.imglib2.util.Util;
import net.imglib2.util.ValuePair;

public class RansacEllipsoid {

	
	
	public static List<double[]> GetEllipsepoints(Ellipsoid ellipse){
		
		List<double[]> pointlist = new ArrayList<double[]>();
		
		EllipseRoi roi = DisplayasROI.create2DEllipse(ellipse.getCenter(), new double[] {ellipse.getCovariance()[0][0], ellipse.getCovariance()[0][1], ellipse.getCovariance()[1][1] });
		
   	
    Rectangle bounds = roi.getBounds();
     int[] xCord = roi.getXCoordinates();
	 int[] yCord = roi.getYCoordinates();	
	 int N = xCord.length; 
	
	 
	 for (int index = 0; index < N; ++index) {
	
	 
	 pointlist.add(new double[] {bounds.x + xCord[index], bounds.y + yCord[index]});
	 
	 }
		return pointlist;
	}
	
	
	public static <T extends Comparable<T>>  double GetnearestPoint(List<Pair<RealLocalizable, T>> targetlist, double[] sourcepoint) {
		
		
		ArrayList<RealLocalizable> pointlist = new ArrayList<RealLocalizable>();
		
		for (Pair<RealLocalizable, T> point: targetlist) {
			
			pointlist.add(point.getA());
			
		}
		
		
		final KDTree<RealLocalizable> tree = new KDTree<RealLocalizable>(pointlist, pointlist);
		
		
		NearestNeighborSearchOnKDTree<RealLocalizable> Search = new NearestNeighborSearchOnKDTree<>(tree);
		final RealPoint rpos = new RealPoint(sourcepoint);
		
		Search.search(rpos);
		double distance = Search.getSquareDistance();
		
		return distance;
		
		
	}
	
	public static <T extends Comparable<T>>  ArrayList<Pair<Ellipsoid, List<Pair<RealLocalizable, T>>>> Allsamples(
			final List<Pair<RealLocalizable, T>> points, final double outsideCutoffDistance,
			final double insideCutoffDistance, double minpercent, final NumericalSolvers numsol, int maxiter, final int ndims) {

		boolean fitted;

		final List<Pair<RealLocalizable, T>> remainingPoints = new ArrayList<Pair<RealLocalizable, T>>();
		if (points != null)
			remainingPoints.addAll(points);

		final ArrayList<Pair<Ellipsoid, List<Pair<RealLocalizable, T>>>> segments = 
				new ArrayList<Pair<Ellipsoid, List<Pair<RealLocalizable, T>>>>();

		int iter = 0;
		do {

			if (remainingPoints.size() > 0) {
				fitted = false;

				++iter;
				final Pair<Ellipsoid, List<Pair<RealLocalizable, T>>> f = sample(remainingPoints,
						remainingPoints.size(), outsideCutoffDistance, insideCutoffDistance, numsol, ndims);

				if (f!=null) {
				List<double[]> pointlist = GetEllipsepoints(f.getA());
				double size = pointlist.size();
				double count = 0;
				for (int index = 0; index < pointlist.size(); ++index) {
					
					double distance = GetnearestPoint(remainingPoints, pointlist.get(index));
					
					if (distance > outsideCutoffDistance || distance > insideCutoffDistance)
						count++;
				}
				
				
				
				
				if ( f.getB().size() > 9) {

					fitted = true;

					segments.add(f);

					final List<Pair<RealLocalizable, T>> inlierPoints = new ArrayList<Pair<RealLocalizable, T>>();
					for (final Pair<RealLocalizable, T> p : f.getB())
						inlierPoints.add(p);
					remainingPoints.removeAll(inlierPoints);
					

				}
				double percent = (size - count) / size;
				
				if (percent < minpercent) {
					
					System.out.println("Wrong Ellipse detected, removing" );

					segments.remove(f);
					final List<Pair<RealLocalizable, T>> inlierPoints = new ArrayList<Pair<RealLocalizable, T>>();
					for (final Pair<RealLocalizable, T> p : f.getB())
						inlierPoints.add(p);
					remainingPoints.addAll(inlierPoints);
					
					
				}
				}
				else
					break;
			} else {

				fitted = true;
				break;

			}
            if (iter >= maxiter)
	            break;
            
			
		}

		while (fitted);

		return segments;

	}

	public static <T extends Comparable<T>>  Pair<Ellipsoid, List<Pair<RealLocalizable, T>>> sample(
			final List<Pair<RealLocalizable, T>> points, final int numSamples, final double outsideCutoffDistance,
			final double insideCutoffDistance, final NumericalSolvers numsol, final int ndims) {
		final int numPointsPerSample = 9;

		final Random rand = new Random(System.currentTimeMillis());
		final ArrayList<Integer> indices = new ArrayList<Integer>();
		final double[][] coordinates = new double[numPointsPerSample][ndims];

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
					points.get(i).getA().localize(coordinates[s]);

				}

				final Ellipsoid ellipsoid = FitEllipsoid.yuryPetrov(coordinates, ndims);
				if (ellipsoid != null) {
					final double cost = costFunction.compute(ellipsoid, points, numsol);
					if (cost < bestCost) {
						bestCost = cost;
						bestEllipsoid = ellipsoid;
						
					}
				}
			} catch (final IllegalArgumentException e) {

			} catch (final RuntimeException e) {

			}
		}
		if (bestEllipsoid != null) {
			Pair<Ellipsoid, List<Pair<RealLocalizable, T>>> refined = fitToInliers(bestEllipsoid, points,
					outsideCutoffDistance, insideCutoffDistance, numsol, ndims);
			if (refined == null) {

				return new ValuePair<Ellipsoid, List<Pair<RealLocalizable, T>>>(
						bestEllipsoid, points);

			}
			return refined;
		}

		else
			return null;
	}

	public static <T extends Comparable<T>>  Pair<Ellipsoid, List<Pair<RealLocalizable, T>>> fitToInliers(final Ellipsoid guess,
			final List<Pair<RealLocalizable, T>> points, final double outsideCutoffDistance,
			final double insideCutoffDistance, final NumericalSolvers numsol, final int ndims) {
		final ArrayList<Pair<RealLocalizable, T>> inliers = new ArrayList<Pair<RealLocalizable, T>>();
		for (final Pair<RealLocalizable, T> point : points) {
			final Result result = DistPointHyperEllipsoid.distPointHyperEllipsoid(point.getA(), guess, numsol);

			final double d = result.distance;
			final boolean inside = guess.test(point.getA());

			// System.out.println(point.getDoublePosition(0) + " " +
			// point.getDoublePosition(1));
			if ((inside && d <= insideCutoffDistance) || (!inside && d <= outsideCutoffDistance))
				inliers.add(point);
		}

		final double[][] coordinates = new double[inliers.size()][ndims];
		for (int i = 0; i < inliers.size(); ++i)
			inliers.get(i).getA().localize(coordinates[i]);

		System.out.println("Fitting on Co-ordinates " + coordinates.length);
		Ellipsoid ellipsoid = FitEllipsoid.yuryPetrov(coordinates, ndims);
		Pair<Ellipsoid, List<Pair<RealLocalizable, T>>> Allellipsoids = null;
		if (ellipsoid != null)
			Allellipsoids = new ValuePair<Ellipsoid, List<Pair<RealLocalizable, T>>>(ellipsoid, inliers);

		return Allellipsoids;

	}

	

	static  interface    Cost  {
		<T extends Comparable<T>>  double compute(final Ellipsoid ellipsoid, final List<Pair<RealLocalizable, T>> points,
				final NumericalSolvers numsol);
	}

	static class AbsoluteDistanceCost implements Cost {
		private final double outsideCutoff;
		private final double insideCutoff;

		public AbsoluteDistanceCost(final double outsideCutoffDistance, final double insideCutoffDistance) {
			outsideCutoff = outsideCutoffDistance;
			insideCutoff = insideCutoffDistance;
		}

		@Override
		public <T extends Comparable<T>>   double compute(final Ellipsoid ellipsoid, final List<Pair<RealLocalizable, T>> points,
				final NumericalSolvers numsol) {
			double cost = 0;
			for (final Pair<RealLocalizable, T> point : points) {
				final Result result = DistPointHyperEllipsoid.distPointHyperEllipsoid(point.getA(), ellipsoid, numsol);
				final double d = result.distance;
				if (ellipsoid.test(point.getA()))
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
		public <T extends Comparable<T>>   double  compute(final Ellipsoid ellipsoid, final List<Pair<RealLocalizable, T>> points,
				final NumericalSolvers numsol) {
			double cost = 0;
			for (final Pair<RealLocalizable, T> point : points) {
				final Result result = DistPointHyperEllipsoid.distPointHyperEllipsoid(point.getA(), ellipsoid, numsol);
				final double d = result.distance * result.distance;
				if (ellipsoid.test(point.getA()))
					cost += Math.min(d, insideCutoff);
				else
					cost += Math.min(d, outsideCutoff);
			}
			return cost;
		}

	}

	public static double DistanceX(final RealLocalizable pointA, final RealLocalizable pointB) {

		double distance = 0;

		distance += (pointA.getDoublePosition(0) - pointB.getDoublePosition(0))
				* (pointA.getDoublePosition(0) - pointB.getDoublePosition(0));

		return distance;
	}

	public static double DistanceSq(final RealLocalizable pointA, final RealLocalizable pointB) {

		double distance = 0;
		int numDim = pointA.numDimensions();

		for (int d = 0; d < numDim; ++d) {

			distance += (pointA.getDoublePosition(d) - pointB.getDoublePosition(d))
					* (pointA.getDoublePosition(d) - pointB.getDoublePosition(d));

		}
		return distance;
	}

	
	
}
