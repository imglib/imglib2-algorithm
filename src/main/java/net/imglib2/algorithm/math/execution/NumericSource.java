package net.imglib2.algorithm.math.execution;

import net.imglib2.Localizable;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.type.numeric.RealType;

public class NumericSource< O extends RealType< O > > implements OFunction< O >
{
	private final O value;
	
	public NumericSource( final O scrap, final Number number )
	{
		this.value = scrap;
		if ( number instanceof Float )
			this.value.setReal( number.floatValue() );
		else
			this.value.setReal( number.doubleValue() );
	}
	
	@Override
	public final O eval()
	{
		return this.value;
	}

	@Override
	public final O eval( final Localizable loc )
	{
		return this.value;
	}
}
