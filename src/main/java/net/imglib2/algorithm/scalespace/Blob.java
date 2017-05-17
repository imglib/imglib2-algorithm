package net.imglib2.algorithm.scalespace;

import net.imglib2.RealPoint;
import net.imglib2.algorithm.localextrema.RefinedPeak;

public class Blob extends RealPoint
{

	public static enum SpecialPoint
	{
		INVALID, MIN, MAX
	}

	private SpecialPoint peakType;

	private final double value;

	private final double radius;

	Blob( final RefinedPeak< DifferenceOfGaussianPeak > peak, final double radius )
	{
		super( peak.numDimensions() - 1 );
		for ( int d = 0; d < n; d++ )
		{
			setPosition( peak.getDoublePosition( d ), d );
		}
		this.radius = radius;
		this.value = peak.getValue();
		this.peakType = peak.getOriginalPeak().getPeakType();
	}

	@Override
	public String toString()
	{
		return "Blob @" + super.toString() + ", val = " + value + ", radius = " + radius + ", type = " + peakType;
	}

	public double getValue()
	{
		return value;
	}

	public double getRadius()
	{
		return radius;
	}

	public SpecialPoint getPeakType()
	{
		return peakType;
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

	public void setPeakType( final SpecialPoint peakType )
	{
		this.peakType = peakType;

	}

}
