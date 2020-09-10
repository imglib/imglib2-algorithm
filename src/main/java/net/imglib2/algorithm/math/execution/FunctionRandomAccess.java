package net.imglib2.algorithm.math.execution;

import java.util.HashMap;

import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.Sampler;
import net.imglib2.algorithm.math.abstractions.IFunction;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.algorithm.math.abstractions.Util;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;

public class FunctionRandomAccess< C extends RealType< C >, O extends RealType< O > > extends Point implements RandomAccess< O >
{
	private final IFunction operation;
	private final C computeType;
	private final O outputType;
	private final Converter< RealType< ? >, C > inConverter;
	private final Converter< C, O > outConverter;
	private final Sampler< O > sampler;

	@SuppressWarnings("unchecked")
	public FunctionRandomAccess(
			final IFunction operation,
			Converter< RealType< ? >, C > inConverter,
			final C computeType,
			final O outputType,
			Converter< C, O > outConverter )
	{
		super( Util.findFirstInterval( operation ).numDimensions() );
		if ( null == inConverter )
			inConverter = Util.genericRealTypeConverter();
		
		final boolean are_same_type = computeType.getClass() == outputType.getClass();
		
		if ( null == outConverter && !are_same_type )
		{
			if ( computeType instanceof IntegerType && outputType instanceof IntegerType )
				outConverter = ( Converter< C, O > )Util.genericIntegerTypeConverter();
			else
				outConverter = ( Converter< C, O > )Util.genericRealTypeConverter();
		}
		
		if ( are_same_type )
			this.sampler = new FunctionSamplerDirect( this, operation, ( Converter< RealType< ? >, O > )inConverter, outputType );
		else
			this.sampler = new FunctionSampler( this, operation, inConverter, computeType, outputType, outConverter );
		
		this.operation = operation;
		this.inConverter = inConverter;
		this.outConverter = outConverter;
		this.computeType = computeType;
		this.outputType = outputType;
	}
	
	private final class FunctionSampler implements Sampler< O >
	{
		private final Point point;
		private final IFunction operation;
		private final C computeType;
		private final O outputType;
		private final Converter< RealType< ? >, C > inConverter;
		private final Converter< C, O > outConverter;
		private final OFunction< C > f;
		
		FunctionSampler(
				final Point point,
				final IFunction operation,
				final Converter< RealType< ? >, C > inConverter,
				final C computeType,
				final O outputType,
				final Converter< C, O > outConverter
				)
		{
			this.point = point;
			this.operation = operation;
			this.computeType = computeType;
			this.outputType = outputType.createVariable();
			this.inConverter = inConverter;
			this.outConverter = outConverter;
			this.f = operation.reInit(
					computeType.createVariable(),
					new HashMap< String, LetBinding< C > >(),
					inConverter,
					null );
		}
		
		@Override
		public final Sampler< O > copy()
		{
			return new FunctionSampler( this.point, this.operation, this.inConverter, this.computeType, this.outputType, this.outConverter );
		}

		@Override
		public O get()
		{
			this.outConverter.convert( this.f.eval( this.point ), outputType );
			return this.outputType;
		}
	}
	
	private final class FunctionSamplerDirect implements Sampler< O >
	{
		private final Point point;
		private final IFunction operation;
		private final O outputType;
		private final Converter< RealType< ? >, O > inConverter;
		private final OFunction< O > f;
		
		FunctionSamplerDirect(
				final Point point,
				final IFunction operation,
				final Converter< RealType< ? >, O > inConverter,
				final O outputType
				)
		{
			this.point = point;
			this.operation = operation;
			this.outputType = outputType.createVariable();
			this.inConverter = inConverter;
			this.f = operation.reInit(
					outputType.createVariable(),
					new HashMap< String, LetBinding< O > >(),
					inConverter,
					null );
		}
		
		@Override
		public final Sampler< O > copy()
		{
			return new FunctionSamplerDirect( this.point, this.operation, this.inConverter, this.outputType );
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
		return new FunctionRandomAccess< C, O >(
				this.operation,
				this.inConverter,
				this.computeType,
				this.outputType,
				this.outConverter );
	}
}
