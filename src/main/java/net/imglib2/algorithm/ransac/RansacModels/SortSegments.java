package net.imglib2.algorithm.ransac.RansacModels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.imglib2.RealLocalizable;
import net.imglib2.util.Pair;

public class SortSegments {

	
	public static <T extends Comparable<T>>  void Sort(ArrayList<Pair<Ellipsoid, List<Pair<RealLocalizable, T>>>> segments) {
		
		
		Collections.sort(segments, new Comparator<Pair<Ellipsoid, List<Pair<RealLocalizable, T>>>>(){

			@Override
			public int compare(Pair<Ellipsoid, List<Pair<RealLocalizable, T>>> o1,
					Pair<Ellipsoid, List<Pair<RealLocalizable, T>>> o2) {

				
				
				int i = 0;
				int numDim = o1.getA().numDimensions();
				
				while(i < numDim) {
					
					if (o1!=o2) {
						
						
						return (int) Math.signum(o1.getA().getCenter()[i] - o2.getA().getCenter()[i]);
						
					}
					i++;
					
				}
				
				
				
				
				return o1.hashCode() - o2.hashCode();
			}

		
			
			
			
			
			
			
			
			
			
		});
		
		
		
	}
	
}
