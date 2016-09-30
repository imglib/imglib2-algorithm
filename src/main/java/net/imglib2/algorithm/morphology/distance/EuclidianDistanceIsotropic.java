package net.imglib2.algorithm.morphology.distance;

public class EuclidianDistanceIsotropic implements Distance
{

	private final double weight;

	private final double oneOverTwoTimesWeight;

	public EuclidianDistanceIsotropic( final double weight )
	{
		super();
		this.weight = weight;
		this.oneOverTwoTimesWeight = 0.5 / weight;
	}

	@Override
	public double evaluate( final double x, final double xShift, final double yShift, final int dim )
	{
		final double diff = x - xShift;
		return weight * diff * diff + yShift;
	}

	@Override
	public double intersect( final double xShift1, final double yShift1, final double xShift2, final double yShift2, final int dim )
	{
		return oneOverTwoTimesWeight * ( weight * xShift2 * xShift2 + yShift2 - ( weight * xShift1 * xShift1 + yShift1 ) ) / ( xShift2 - xShift1 );
	}

}
