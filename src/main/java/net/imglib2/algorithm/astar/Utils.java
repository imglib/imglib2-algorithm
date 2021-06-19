package net.imglib2.algorithm.astar;

public class Utils
{

	public static void check( final boolean b )
	{
		if ( !b )
			throw new RuntimeException();
	}

	static void check( final boolean b, final String msg )
	{
		if ( !b )
			throw new RuntimeException( msg );
	}

	static void check( final boolean b, final String format, final Object... args )
	{
		if ( !b )
			throw new RuntimeException( String.format( format, args ) );
	}

	static int mask( final int nbit )
	{
		check( nbit >= 1 && nbit <= 32 );
		return nbit == 32 ? -1 : ( 1 << nbit ) - 1;
	}
}
