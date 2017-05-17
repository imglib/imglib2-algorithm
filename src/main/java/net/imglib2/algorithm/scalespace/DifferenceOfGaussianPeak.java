package net.imglib2.algorithm.scalespace;

import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.algorithm.scalespace.Blob.SpecialPoint;

/**
 * Intermediate use class needed to compute and subpixel localize blobs found by
 * the scale-space algorithm. This class needs not to be used by end-user, which
 * will be return a collection of Blob.
 */
class DifferenceOfGaussianPeak extends Point
{

	private SpecialPoint peakType;

	private final double value;


	DifferenceOfGaussianPeak( final Localizable position, final double value, final SpecialPoint peakType )
	{
		super( position );
		this.value = value;
		this.peakType = peakType;
	}

	public double getValue()
	{
		return value;
	}

	public SpecialPoint getPeakType()
	{
		return peakType;
	}

	public void setPeakType( final SpecialPoint peakType )
	{
		this.peakType = peakType;
	}

	public boolean isMin()
	{
		return peakType == SpecialPoint.MIN;
	}

	public boolean isMax()
	{
		return peakType == SpecialPoint.MAX;
	}

	public boolean isValid()
	{
		return peakType != SpecialPoint.INVALID;
	}

	@Override
	public String toString()
	{
		return "Peak @" + super.toString() + ", val = " + value + ", type = " + peakType;
	}

}
