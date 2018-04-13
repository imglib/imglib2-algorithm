package net.imglib2.algorithm.ransac.RansacModels.CircleFits;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.imglib2.RealLocalizable;
import net.imglib2.algorithm.ransac.RansacModels.DistPointHyperEllipsoid;
import net.imglib2.algorithm.ransac.RansacModels.Ellipsoid;
import net.imglib2.algorithm.ransac.RansacModels.NumericalSolvers;
import net.imglib2.algorithm.ransac.RansacModels.RansacEllipsoid;
import net.imglib2.algorithm.ransac.RansacModels.DistPointHyperEllipsoid.Result;

import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;

public class PointSphere {

	// Fit a circle or a sphere over 3/ 4 points
	static int minpoints = 3;
	static int minpointssphere = 4;

	public static <T extends Comparable<T>> ArrayList<Pair<Ellipsoid, List<Pair<RealLocalizable, T>>>> Allsamples(
			final List<Pair<RealLocalizable, T>> points, final NumericalSolvers numsol, int maxiter, final int ndims) {

		boolean fitted;

		final List<Pair<RealLocalizable, T>> remainingPoints = new ArrayList<Pair<RealLocalizable, T>>();
		if (points != null)
			remainingPoints.addAll(points);

		final ArrayList<Pair<Ellipsoid, List<Pair<RealLocalizable, T>>>> segments = new ArrayList<Pair<Ellipsoid, List<Pair<RealLocalizable, T>>>>();

		do {

			if (remainingPoints.size() > 0) {
				fitted = false;

				final Pair<Ellipsoid, List<Pair<RealLocalizable, T>>> f = sample(remainingPoints,
						remainingPoints.size(), numsol, ndims);
				if (f != null) {
					List<double[]> pointlist = RansacEllipsoid.GetEllipsepoints(f.getA());

					double size = pointlist.size();

					if (size != 0) {

						int minps = 0;
						if (ndims == 2)
							minps = minpoints;
						else
							minps = minpointssphere;

						if (f.getB().size() > minps) {

							fitted = true;

							segments.add(f);

							final List<Pair<RealLocalizable, T>> inlierPoints = new ArrayList<Pair<RealLocalizable, T>>();
							for (final Pair<RealLocalizable, T> p : f.getB())
								inlierPoints.add(p);
							remainingPoints.removeAll(inlierPoints);

						}

					}
				}
			} else {

				fitted = true;
				break;
			}

		}

		while (fitted);

		return segments;

	}

	public static <T extends Comparable<T>> Pair<Ellipsoid, List<Pair<RealLocalizable, T>>> sample(
			final List<Pair<RealLocalizable, T>> points, final int numSamples, final NumericalSolvers numsol,
			final int ndims) {
		int minps = 0;
		if (ndims == 2)
			minps = minpoints;
		else
			minps = minpointssphere;
		int numPointsPerSample = minps;

		final Random rand = new Random(System.currentTimeMillis());
		final ArrayList<Integer> indices = new ArrayList<Integer>();
		final double[][] coordinates = new double[numPointsPerSample][ndims];

		Ellipsoid bestEllipsoid = null;

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

				final Ellipsoid ellipsoid = FitCircle.yuryPetrov(coordinates, ndims);
				if (ellipsoid != null) {
					bestEllipsoid = ellipsoid;

				}
			} catch (final IllegalArgumentException e) {

			} catch (final RuntimeException e) {

			}
		}
		if (bestEllipsoid != null) {
			Pair<Ellipsoid, List<Pair<RealLocalizable, T>>> refined = fitToInliers(bestEllipsoid, points, numsol,
					ndims);
			if (refined == null) {

				return new ValuePair<Ellipsoid, List<Pair<RealLocalizable, T>>>(bestEllipsoid, points);

			}
			return refined;
		}

		else
			return null;
	}

	public static <T extends Comparable<T>> Pair<Ellipsoid, List<Pair<RealLocalizable, T>>> fitToInliers(
			final Ellipsoid guess, final List<Pair<RealLocalizable, T>> points, final NumericalSolvers numsol,
			final int ndims) {
		final ArrayList<Pair<RealLocalizable, T>> inliers = new ArrayList<Pair<RealLocalizable, T>>();
		for (final Pair<RealLocalizable, T> point : points) {

			inliers.add(point);
		}

		final double[][] coordinates = new double[inliers.size()][ndims];
		for (int i = 0; i < inliers.size(); ++i)
			inliers.get(i).getA().localize(coordinates[i]);

		Ellipsoid ellipsoid = FitCircle.yuryPetrov(coordinates, ndims);

		Pair<Ellipsoid, List<Pair<RealLocalizable, T>>> Allellipsoids = null;
		if (ellipsoid != null)
			Allellipsoids = new ValuePair<Ellipsoid, List<Pair<RealLocalizable, T>>>(ellipsoid, inliers);

		return Allellipsoids;

	}

}
