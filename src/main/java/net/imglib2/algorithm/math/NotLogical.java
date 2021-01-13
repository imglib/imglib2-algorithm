package net.imglib2.algorithm.math;

import java.util.Map;

import net.imglib2.algorithm.math.abstractions.ABooleanFunction;
import net.imglib2.algorithm.math.abstractions.AUnaryFunction;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.algorithm.math.execution.LetBinding;
import net.imglib2.algorithm.math.execution.LogicalNot;
import net.imglib2.algorithm.math.execution.LogicalNotBoolean;
import net.imglib2.algorithm.math.execution.Variable;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;

public class NotLogical extends AUnaryFunction
{	
	public NotLogical( final Object c1 )
	{
		super( c1 );
	}
	
	@Override
	public < O extends RealType< O > > OFunction< O > reInit(
			final O tmp,
			final Map< String, LetBinding< O > > bindings,
			final Converter< RealType< ? >, O > converter,
			final Map< Variable< O >, OFunction< O > > imgSources )
	{
		final OFunction< O > a = this.a.reInit( tmp, bindings, converter, imgSources );
		
		return a instanceof ABooleanFunction< ? > ?
			new LogicalNotBoolean< O >( tmp.createVariable(), a )
			: new LogicalNot< O >( tmp.createVariable(), a );
	}
}
