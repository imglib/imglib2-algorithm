package net.imglib2.algorithm.ransac.RansacModels;

import ij.gui.Line;

public class Angleobject {

	public final Line lineA;
	public final Line lineB;
	public final double angle;
	
	
	public Angleobject(final Line lineA, final Line lineB, final double angle ) {
		
		this.lineA = lineA;
		this.lineB = lineB;
		this.angle = angle;
		
		
	}
	
	
}
