package net.imglib2.algorithm.math.execution;

import java.util.HashMap;
import java.util.List;

import net.imglib2.AbstractCursor;
import net.imglib2.algorithm.math.abstractions.IFunction;
import net.imglib2.algorithm.math.abstractions.IImgSourceIterable;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.algorithm.math.abstractions.Util;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;

public class FunctionCursor< O extends RealType< O > > extends AbstractCursor< O >
{
	private final O outputType;
	private final IFunction operation;
	private final Converter< RealType< ? >, O > converter;
	
	protected OFunction< O > f;
	protected O scrap;
	protected IImgSourceIterable ii;
	protected List< IImgSourceIterable > all_ii;
 	
	public FunctionCursor( final IFunction operation, final O outputType, final Converter< RealType< ? >, O > converter ) {
		super( Util.findFirstImg( operation ).numDimensions() );
		this.operation = operation;
		this.outputType = outputType;
		this.converter = null == converter ? Util.genericRealTypeConverter() : converter;
		this.reset();
	}

	@Override
	public O get()
	{
		return this.scrap;
	}

	@Override
	public void fwd()
	{
		this.scrap = this.f.eval();
	}

	@Override
	public boolean hasNext()
	{
		return this.ii.hasNext();
	}

	@Override
	public void reset() {
		this.f = this.operation.reInit( this.outputType.createVariable() , new HashMap< String, O >(), this.converter, null );
		this.ii = Util.findFirstIterableImgSource( this.f );
		this.all_ii = Util.findAllIterableImgSource( this.f );
	}

	@Override
	public long getLongPosition( final int d )
	{
		return this.ii.localizable().getLongPosition( d );
	}

	@Override
	public void localize( final long[] position ) {
		for ( final IImgSourceIterable ii : this.all_ii )
			ii.localize( position );
	}

	@Override
	public AbstractCursor< O > copy()
	{
		return new FunctionCursor< O >( this.operation, this.outputType, this.converter );
	}

	@Override
	public AbstractCursor< O > copyCursor()
	{
		return this.copy();
	}

}
