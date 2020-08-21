package net.imglib2.algorithm.math;

import java.util.Map;

import net.imglib2.algorithm.math.abstractions.ABinaryFunction;
import net.imglib2.algorithm.math.abstractions.ABooleanFunction;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.algorithm.math.execution.LetBinding;
import net.imglib2.algorithm.math.execution.LogicalOr;
import net.imglib2.algorithm.math.execution.LogicalOrBoolean;
import net.imglib2.algorithm.math.execution.Variable;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;

public class OrLogical extends ABinaryFunction
{	
	public OrLogical( final Object c1, final Object c2 )
	{
		super( c1, c2 );
	}
	
	public OrLogical( final Object... c )
	{
		super( c );
	}
	
	@Override
	public < O extends RealType< O > > OFunction< O > reInit(
			final O tmp,
			final Map< String, LetBinding< O > > bindings,
			final Converter< RealType< ? >, O > converter,
			final Map< Variable< O >, OFunction< O > > imgSources )
	{
		final OFunction< O > a = this.a.reInit( tmp, bindings, converter, imgSources );
		final OFunction< O > b = this.b.reInit( tmp, bindings, converter, imgSources );
	
		
		return a instanceof ABooleanFunction< ? > && b instanceof ABooleanFunction< ? > ?
			new LogicalOrBoolean< O >( tmp.createVariable(), a, b )
			: new LogicalOr< O >( tmp.createVariable(), a, b );
	}
}
