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

public class FunctionRandomAccessDouble< O extends RealType< O > > extends Point implements RandomAccess< O >
{
	private final FunctionSampler sampler;

	public FunctionRandomAccessDouble( final IFunction operation, final O outputType, final Converter< RealType< ? >, O > converter )
	{
		super( Util.findFirstImg( operation ).numDimensions() );
		this.sampler = new FunctionSampler( this, operation, outputType, converter );
	}
	
	private final class FunctionSampler implements Sampler< O >
	{
		private final Point point;
		private final IFunction operation;
		private final O scrap;
		private final Converter< RealType< ? >, O > converter;
		private final OFunction< O > f;
		
		FunctionSampler( final Point point, final IFunction operation, final O outputType, final Converter< RealType< ? >, O > converter )
		{
			this.point = point;
			this.operation = operation;
			this.scrap = outputType.createVariable();
			this.converter = converter;
			this.f = operation.reInit(
					outputType.copy(),
					new HashMap< String, LetBinding< O > >(),
					null == converter ? Util.genericRealTypeConverter() : converter,
					null );
		}
		
		@Override
		public final Sampler< O > copy()
		{
			return new FunctionSampler( this.point, this.operation, this.scrap, this.converter );
		}

		@Override
		public final O get()
		{
			this.scrap.setReal( this.f.evalDouble( this.point ) );
			return this.scrap;
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
		return new FunctionRandomAccessDouble< O >( this.sampler.operation, this.sampler.scrap, this.sampler.converter );
	}
}
