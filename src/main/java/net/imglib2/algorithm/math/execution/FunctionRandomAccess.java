package net.imglib2.algorithm.math.execution;

import java.util.HashMap;

import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.Sampler;
import net.imglib2.algorithm.math.abstractions.IFunction;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.algorithm.math.abstractions.Util;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;

public class FunctionRandomAccess< O extends RealType< O > > extends Point implements RandomAccess< O >
{
	private final FunctionSampler sampler;

	public FunctionRandomAccess( final IFunction operation, final O outputType, final Converter< RealType< ? >, O > converter )
	{
		super( Util.findFirstImg( operation ).numDimensions() );
		this.sampler = new FunctionSampler( this, operation, outputType, converter );
	}
	
	private final class FunctionSampler implements Sampler< O >
	{
		private final Point point;
		private final IFunction operation;
		private final O outputType;
		private final Converter< RealType< ? >, O > converter;
		private final OFunction< O > f;
		
		FunctionSampler( final Point point, final IFunction operation, final O outputType, final Converter< RealType< ? >, O > converter )
		{
			this.point = point;
			this.operation = operation;
			this.outputType = outputType.createVariable();
			this.converter = converter;
			this.f = operation.reInit(
					outputType.copy(),
					new HashMap< String, O >(),
					null == converter ? Util.genericRealTypeConverter() : converter,
					null );
		}
		
		@Override
		public final Sampler< O > copy()
		{
			return new FunctionSampler( this.point, this.operation, this.outputType, this.converter );
		}

		@Override
		public final O get()
		{
			return this.f.eval( this.point );
		}
	}
	
	@Override
	public Sampler< O > copy()
	{
		return this.sampler.copy();
	}

	@Override
	public O get()
	{
		return this.sampler.get();
	}

	@Override
	public RandomAccess< O > copyRandomAccess()
	{
		return new FunctionRandomAccess< O >( this.sampler.operation, this.sampler.outputType, this.sampler.converter );
	}
}
