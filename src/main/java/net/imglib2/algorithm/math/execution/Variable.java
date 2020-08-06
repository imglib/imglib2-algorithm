package net.imglib2.algorithm.math.execution;

import java.util.Arrays;
import java.util.List;

import net.imglib2.Localizable;
import net.imglib2.algorithm.math.abstractions.OFunction;
import net.imglib2.type.numeric.RealType;

public class Variable< O extends RealType< O > > implements OFunction< O >
{
	private final String name;
	private final LetBinding< O > let;
	private final O value;
	
	public Variable( final String name, final LetBinding< O > let )
	{
		this.name = name;
		this.let = let;
		this.value = let.getScrapValue();
	}
	
	/**
	 * Used by the ImgSource to replace itself with a Let-Var when inside the Then or Else of an If statement.
	 * This instance is ephemeral and won't need a let or a value.
	 */
	public Variable( final String name, final O value )
	{
		this.name = name;
		this.let = null;
		this.value = value;
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
	
	public final O getScrap()
	{
		return this.value;
	}
	
	public final String getName()
	{
		return this.name;
	}
	
	@Override
	public List< OFunction< O > > children()
	{
		return Arrays.asList();
	}
	
	@Override
	public final double evalDouble()
	{
		return this.let.getDoubleValue();
	}
	
	@Override
	public final double evalDouble( final Localizable loc )
	{
		return this.let.getDoubleValue();
	}
}
