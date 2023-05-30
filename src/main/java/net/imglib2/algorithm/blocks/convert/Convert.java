package net.imglib2.algorithm.blocks.convert;

import net.imglib2.algorithm.blocks.DefaultUnaryBlockOperator;
import net.imglib2.algorithm.blocks.UnaryBlockOperator;
import net.imglib2.type.NativeType;

/**
 * Create {@link UnaryBlockOperator} to convert blocks between standard ImgLib2 {@code Type}s.
 * Provides rounding, optional clamping, and handling unsigned types.
 * <p>
 * Supported types:
 * <ul>
 * <li>{@code UnsignedByteType}</li>
 * <li>{@code UnsignedShortType}</li>
 * <li>{@code UnsignedIntType}</li>
 * <li>{@code ByteType}</li>
 * <li>{@code ShortType}</li>
 * <li>{@code IntType}</li>
 * <li>{@code LongType}</li>
 * <li>{@code FloatType}</li>
 * <li>{@code DoubleType}</li>
 * </ul>
 */
public class Convert
{
	/**
	 * Create {@link UnaryBlockOperator} to convert blocks between {@code
	 * sourceType} and {@code targetType}.
	 * No clamping.
	 */
	public static < S extends NativeType< S >, T extends NativeType< T > >
	UnaryBlockOperator< S, T > convert( final S sourceType, final T targetType )
	{
		return convert( sourceType, targetType, ClampType.NONE );
	}

	/**
	 * Create {@link UnaryBlockOperator} to convert blocks between {@code
	 * sourceType} and {@code targetType}.
	 * Clamp target values according to {@code clamp}.
	 */
	public static < S extends NativeType< S >, T extends NativeType< T > >
	UnaryBlockOperator< S, T > convert( final S sourceType, final T targetType, final ClampType clamp )
	{
		return new DefaultUnaryBlockOperator<>( sourceType, targetType, new ConvertBlockProcessor<>( sourceType, targetType, clamp ) );
	}
}
