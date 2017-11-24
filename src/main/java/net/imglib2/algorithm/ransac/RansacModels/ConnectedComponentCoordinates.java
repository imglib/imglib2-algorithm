package net.imglib2.algorithm.ransac.RansacModels;

import java.util.ArrayList;

import ij.IJ;
import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

public class ConnectedComponentCoordinates {

	
	
	public static <T extends Comparable<T>> ArrayList<RealLocalizable> GetCoordinates(RandomAccessibleInterval<T> source, final T threshold) {
		
		ArrayList<RealLocalizable> coordinatelist = new ArrayList<RealLocalizable>();
	 
		
		Interval interval = Intervals.expand(source, -1);
		int ndims = source.numDimensions();
		if (ndims > 3)
			IJ.error("Only three dimensional Ellipsoids are supported");
		
		source = Views.interval(source, interval);
		
		final Cursor<T> center = Views.iterable(source).localizingCursor();
		
		final RectangleShape shape = new RectangleShape(1, true);
		
		for (final Neighborhood<T> localNeighborhood : shape.neighborhoods(source))
		{
			
			double[] posf = new double[3];
			final T centerValue = center.next();
			center.localize(posf);
			final RealPoint rpos = new RealPoint(posf);
			// We are looking for pixels which are connected to pixels in the neighborhood
			// having intensity above a certain threshold
			boolean isConnected = false;
			
			
			if (centerValue.compareTo(threshold) >= 0) {
				
				
				for (final T value : localNeighborhood)
				{
					
					if (centerValue.compareTo(value) >=0) {
						
						isConnected = true;
						
					
						
						
						break;
					}
					
				}
				
				
				if (isConnected) {
					
					coordinatelist.add(rpos);
					
				}
				
				
			}
			
			
			
			
		}
		
		
		return coordinatelist;
	}
	
	
	
}
