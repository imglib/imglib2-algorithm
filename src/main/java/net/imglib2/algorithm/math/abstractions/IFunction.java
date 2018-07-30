package net.imglib2.algorithm.math.abstractions;

import java.util.Map;

import net.imglib2.Localizable;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;

public interface IFunction
{
	public RealType< ? > eval();
	
	public RealType< ? > eval( Localizable loc );
	
	public IFunction reInit(
			RealType<?> tmp,
			Map<String, RealType<?>> bindings,
			Converter<RealType<?>, RealType<?>> converter
			);
}