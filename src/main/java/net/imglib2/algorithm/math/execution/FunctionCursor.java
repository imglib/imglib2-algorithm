package net.imglib2.algorithm.math.execution;

import java.util.HashMap;

import net.imglib2.AbstractCursor;
import net.imglib2.Cursor;
import net.imglib2.Sampler;
import net.imglib2.algorithm.math.abstractions.IFunction;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.algorithm.math.abstractions.Util;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;

public class FunctionCursor< C extends RealType< C >, O extends RealType< O > > extends AbstractCursor< O >
{
	protected final IFunction operation;
	protected final Converter< RealType< ? >, C > inConverter;
	protected final Converter< C, O > outConverter;
	
	protected OFunction< C > f;
	protected C scrapC;
	protected O scrapO;
	protected Cursor< ? > cursor;
	protected final Sampler< O > sampler;
 	
	/**
	 * 
	 * @param operation
	 * @param inConverter Can be null, and if so, a generic {@code Util#genericRealTypeConverter()} will be used.
	 * @param computeType
	 * @param outputType
	 * @param outConverter Can be null, and if so, a generic integer or real converter will be used.
	 */
	@SuppressWarnings("unchecked")
	public FunctionCursor(
			final IFunction operation,
			final Converter< RealType< ? >, C > inConverter,
			final C computeType,
			final O outputType,
			final Converter< C, O > outConverter )
	{
		super( Util.findFirstInterval( operation ).numDimensions() );
		this.operation = operation;
		this.inConverter = inConverter == null ? Util.genericRealTypeConverter() : inConverter;
		if ( computeType.getClass() == outputType.getClass() )
		{
			this.sampler = ( Sampler< O > )new FunctionSamplerDirect();
			this.outConverter = null;
		}
		else
		{
			if ( null == outConverter )
			{
				if ( computeType instanceof IntegerType && outputType instanceof IntegerType )
					this.outConverter = ( Converter< C, O > )Util.genericIntegerTypeConverter();
				else
					this.outConverter = ( Converter< C, O > )Util.genericRealTypeConverter();
			}
			else
				this.outConverter = outConverter;
			this.sampler = new FunctionSamplerConverter();
		}
		this.scrapC = computeType.createVariable();
		this.scrapO = outputType.createVariable();
		this.reset();
	}
	
	private final class FunctionSamplerConverter implements Sampler< O >
	{	
		@Override
		public final O get()
		{
			outConverter.convert( scrapC, scrapO ); 
			return scrapO;
		}

		@Override
		public final Sampler< O > copy() { return null; }
	}
	
	private final class FunctionSamplerDirect implements Sampler< C >
	{
		@Override
		public final C get()
		{ 
			return scrapC;
		}

		@Override
		public final Sampler< C > copy() { return null; }
	}

	@Override
	public final O get()
	{
		return this.sampler.get();
	}

	@Override
	public void fwd()
	{
		this.scrapC = this.f.eval();
	}

	@Override
	public final boolean hasNext()
	{
		return this.cursor.hasNext();
	}

	@Override
	public void reset()
	{
		this.f = this.operation.reInit( this.scrapC.createVariable() , new HashMap< String, LetBinding< C > >(), this.inConverter, null );
		this.cursor = Util.findFirstIterableImgSource( this.f ).getCursor();
	}

	@Override
	public long getLongPosition( final int d )
	{
		return this.cursor.getLongPosition( d );
	}

	@Override
	public void localize( final long[] position )
	{
			this.cursor.localize( position );
	}

	@Override
	public AbstractCursor< O > copy()
	{
		return new FunctionCursor< C, O >( this.operation, this.inConverter, this.scrapC, this.scrapO, this.outConverter );
	}

	@Override
	public AbstractCursor< O > copyCursor()
	{
		return this.copy();
	}
}