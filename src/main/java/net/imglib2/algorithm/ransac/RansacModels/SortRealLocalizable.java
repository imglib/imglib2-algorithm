package net.imglib2.algorithm.ransac.RansacModels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.imglib2.RealLocalizable;

public class SortRealLocalizable {
	
	
	public static void SortPoints(List<RealLocalizable> points) {
		
		Collections.sort(points, new Comparator<RealLocalizable>() {
			
			
			
			@Override
			public int compare(final RealLocalizable o1, final RealLocalizable o2) {
				
				int i= 0;
				
				int numDim = o1.numDimensions();
				while(i < numDim) {
					if(o1.getDoublePosition(i) != o2.getDoublePosition(i)) {
						
						
						return (int) Math.signum(o1.getDoublePosition(i) - o2.getDoublePosition(i));
					}
					i++;
					
				}
				
			return o1.hashCode() - o2.hashCode();
				
				
			}
			
			
		});
	}
		
	
		
		
		
		public static void SortPointsX(List<RealLocalizable> points) {
			
			Collections.sort(points, new Comparator<RealLocalizable>() {
				
				
				
				@Override
				public int compare(final RealLocalizable o1, final RealLocalizable o2) {
					
					
					final double X1 = o1.getDoublePosition(0);
					final double X2 = o2.getDoublePosition(0);
					
					if (X1 < X2)
						return -1;
					else if (X1 == X2)
						return 0;
					else 
						return 1;
					
					
					
				}
				
				
			});
		}
			
			
		
	public static void SortPointsY(List<RealLocalizable> points) {
			
			Collections.sort(points, new Comparator<RealLocalizable>() {
				
				
				
				@Override
				public int compare(final RealLocalizable o1, final RealLocalizable o2) {
					
					
					final double Y1 = o1.getDoublePosition(1);
					final double Y2 = o2.getDoublePosition(1);
					
					if (Y1 < Y2)
						return -1;
					else if (Y1 == Y2)
						return 0;
					else 
						return 1;
					
					
					
				}
				
				
			});
			
			
			
		}
		
		
		
	}
	
	
	
	


