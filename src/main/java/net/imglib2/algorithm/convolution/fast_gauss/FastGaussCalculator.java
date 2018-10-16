package net.imglib2.algorithm.convolution.fast_gauss;

/**
 * This class implements the fast Gauss transform to calculate a gaussian blur.
 * The approach is very different from an algorithm using a truncated
 * convolution kernel. Especially the runtime is independent of sigma.
 * <p>
 * The implemented algorithm is described in detail in: Charalampidis,
 * Dimitrios. "Recursive implementation of the Gaussian filter using truncated
 * cosine functions." IEEE Transactions on Signal Processing 64.14 (2016):
 * 3554-3565.
 *
 * @author Vladimir Ulman
 * @author Matthias Arzt
 */
public class FastGaussCalculator
{
	private final Parameters fc;
	// we will utilize ring buffers whose current positions will be driven
	// by the currently processed index inside the input array -- we will use
	// bit masking to achieve fast the effect of the operation modulo, furthermore
	// it does not suffer from sign effect: -5 % 8 = -5 in Java; I need it be +3
	// to make it work nicely for the ring buffer (which cannot have negative indices)
    //
	// remember current plus last two results for every yk-term (there's M of them),
	// so we need either 3*3 or 3*4=12 values => 4 bits = capacity for 16 values,
	//
	// actually, we will use 4 ring sub-buffers (each of length 4, 2 bits) for the
	// individual yks...: y1, y3, y5, y7 (the order in which they are touched during
	// the computation of the filter)
	//
	private final double[] yk0 = new double[ 4 ];

	private final double[] yk1 = new double[ 4 ];

	private final double[] yk2 = new double[ 4 ];

	private final double[] yk3 = new double[ 4 ];

	private int x;

	public FastGaussCalculator( final Parameters fc )
	{
		this.fc = fc;
	}

	public void initialize( final double boundaryValue )
	{
		//calculate yk that one would get on constant signal of 1.0
		//(Vlado's invention by solving eq. (35) assuming yk_n = yk_n-1 = yk_n-2)
		final double y1_mN = 2.0 * fc.nk_2[ 0 ] / ( fc.dk_1[ 0 ] + 2.0 );
		final double y3_mN = 2.0 * fc.nk_2[ 1 ] / ( fc.dk_1[ 1 ] + 2.0 );
		final double y5_mN = 2.0 * fc.nk_2[ 2 ] / ( fc.dk_1[ 2 ] + 2.0 );
		final double y7_mN = fc.M == 4 ? 2.0 * fc.nk_2[ 3 ] / ( fc.dk_1[ 3 ] + 2.0 ) : 0.0;

		//calculate yk that one would get on constant signal of array[0],
		//and use this value for yk[x-2] and yk[x-1] when x=-Nm1
		x = -1;
		yk0[ x & 3 ] = yk0[ ( x - 1 ) & 3 ] = boundaryValue * y1_mN;
		yk1[ x & 3 ] = yk1[ ( x - 1 ) & 3 ] = boundaryValue * y3_mN;
		yk2[ x & 3 ] = yk2[ ( x - 1 ) & 3 ] = boundaryValue * y5_mN;
		yk3[ x & 3 ] = yk3[ ( x - 1 ) & 3 ] = boundaryValue * y7_mN;
	}

	public void update( final double tmp )
	{
		++x;
		yk0[ x & 3 ] = fc.nk_2[ 0 ] * tmp - fc.dk_1[ 0 ] * yk0[ ( x - 1 ) & 3 ] - yk0[ ( x - 2 ) & 3 ];
		yk1[ x & 3 ] = fc.nk_2[ 1 ] * tmp - fc.dk_1[ 1 ] * yk1[ ( x - 1 ) & 3 ] - yk1[ ( x - 2 ) & 3 ];
		yk2[ x & 3 ] = fc.nk_2[ 2 ] * tmp - fc.dk_1[ 2 ] * yk2[ ( x - 1 ) & 3 ] - yk2[ ( x - 2 ) & 3 ];
		yk3[ x & 3 ] = fc.M == 4 ? fc.nk_2[ 3 ] * tmp - fc.dk_1[ 3 ] * yk3[ ( x - 1 ) & 3 ] - yk3[ ( x - 2 ) & 3 ] : 0;
	}

	public double getValue()
	{
		return yk0[ x & 3 ] + yk1[ x & 3 ] + yk2[ x & 3 ] + yk3[ x & 3 ];
	}

	/**
	 * Collects all coefficients required to carry on the filtering, see eq.
	 * (35) in the paper. If I(x) is your input array at offset x, and O(x) is
	 * supposed to be the filtered output, one should do:
	 * <p>
	 * tmp = I(x-N-1) + I(x+N-1); O(x) = nk_2[0] * tmp - dk_1[0] * yk(x-1) -
	 * yk(x-2); O(x) += nk_2[1] * tmp - dk_1[1] * yk(x-1) - yk(x-2); O(x) +=
	 * nk_2[2] * tmp - dk_1[2] * yk(x-1) - yk(x-2); if M == 4: O(x) += nk_2[3] *
	 * tmp - dk_1[3] * yk(x-1) - yk(x-2);
	 * <p>
	 * This way 2*M multiplications and 3*M additions are made per one 'x'. Note
	 * that for-loop is not welcome as it involves comparison-test and
	 * additional addition per one 'x'.
	 * <p>
	 * The class also stores Sigma for which the coefficients are valid (as long
	 * as user is not changing values arbitrarily...).
	 */
	public static class Parameters
	{
		private final int M;

		/**
		 * the width of the filter
		 */
		public int N = 0;

		/**
		 * the filtering coefficients
		 */
		private final double[] nk_2;

		private final double[] dk_1;

		/**
		 * just informational: Sigma for which the parameters were calculated
		 */
		private double Sigma = 0;

		public static Parameters fast( final double sigma )
		{
			return new Parameters( 3, sigma );
		}

		public static Parameters exact( final double sigma )
		{
			return new Parameters( 4, sigma );
		}

		/**
		 * Construct with the same accuracy as the Gauss1D for which you want to
		 * use it.
		 */
		private Parameters( final int _M, final double sigma )
		{
			if ( sigma <= 0 )
				throw new IllegalArgumentException( "Sigma must be positive." );
			M = ( _M == 3 || _M == 4 ) ? _M : 3;
			nk_2 = new double[ 4 ];
			dk_1 = new double[ 4 ];

			// eq. (57), the filter width
			final double N1 = ( int ) ( ( M == 3 ? 3.2795 * sigma + 0.25460 // M==3
					: 3.7210 * sigma + 0.20157 // M==4
			) + 0.5 );

			// Table I, 1st/top objective
			final double omega_1 = 1.0 * Math.PI / ( 2.0 * N1 );
			final double omega_3 = 3.0 * Math.PI / ( 2.0 * N1 );
			final double omega_5 = 5.0 * Math.PI / ( 2.0 * N1 );
			final double omega_7 = 7.0 * Math.PI / ( 2.0 * N1 );

			// eq. (37) i=1,3,5,7
			final double p_1 = 1.0 / Math.tan( 0.5 * omega_1 );
			final double p_3 = -1.0 / Math.tan( 0.5 * omega_3 );
			final double p_5 = 1.0 / Math.tan( 0.5 * omega_5 );
			final double p_7 = -1.0 / Math.tan( 0.5 * omega_7 );

			// eq. (44) i=1,3,5,7
			final double r_1 = 1.0 * p_1 * p_1 / Math.sin( omega_1 );
			final double r_3 = -1.0 * p_3 * p_3 / Math.sin( omega_3 );
			final double r_5 = 1.0 * p_5 * p_5 / Math.sin( omega_5 );
			final double r_7 = -1.0 * p_7 * p_7 / Math.sin( omega_7 );

			// approximate rho_i:
			// eq. (50) i=1,3,5,7
			final double rho_1 = Math.exp( -0.5 * sigma * sigma * omega_1 * omega_1 ) / N1;
			final double rho_3 = Math.exp( -0.5 * sigma * sigma * omega_3 * omega_3 ) / N1;
			final double rho_5 = Math.exp( -0.5 * sigma * sigma * omega_5 * omega_5 ) / N1;
			final double rho_7 = Math.exp( -0.5 * sigma * sigma * omega_7 * omega_7 ) / N1;
	/*
			//accurate rho_i:
			// eq. (50) i=1,3,5,7
			double rho_1 = 0, rho_3 = 0, rho_5 = 0, rho_7 = 0;
			for (double n = -N; n <= N; n += 1.0)
			{
				rho_1 += Math.cos(n*0.5*1.0*Math.PI /N) * Math.exp(-0.5*n*n/(Sigma*Sigma));
				rho_3 += Math.cos(n*0.5*3.0*Math.PI /N) * Math.exp(-0.5*n*n/(Sigma*Sigma));
				rho_5 += Math.cos(n*0.5*5.0*Math.PI /N) * Math.exp(-0.5*n*n/(Sigma*Sigma));
				rho_7 += Math.cos(n*0.5*7.0*Math.PI /N) * Math.exp(-0.5*n*n/(Sigma*Sigma));
			}
			rho_1 /= N * Math.sqrt(2.0*Math.PI) * Sigma;
			rho_3 /= N * Math.sqrt(2.0*Math.PI) * Sigma;
			rho_5 /= N * Math.sqrt(2.0*Math.PI) * Sigma;
			rho_7 /= N * Math.sqrt(2.0*Math.PI) * Sigma;
			//
			System.out.println("Rho_1 = "+Rho_1+",\trho_1 = "+rho_1);
			System.out.println("Rho_3 = "+Rho_3+",\trho_3 = "+rho_3);
			System.out.println("Rho_5 = "+Rho_5+",\trho_5 = "+rho_5);
			System.out.println("Rho_7 = "+Rho_7+",\trho_7 = "+rho_7);
	*/

			// eq. (52) a,b = 1,3; 3,5; 3,7; 5,1; 7,1
			final double D_13 = p_1 * r_3 - p_3 * r_1;
			final double D_35 = p_3 * r_5 - p_5 * r_3;
			final double D_37 = p_3 * r_7 - p_7 * r_3;
			final double D_51 = p_5 * r_1 - p_1 * r_5;
			final double D_71 = p_7 * r_1 - p_1 * r_7;

			// eq. (52) i=5,7
			final double C_15 = D_35 / D_13;
			final double C_17 = D_37 / D_13;
			final double C_35 = D_51 / D_13;
			final double C_37 = D_71 / D_13;

			// get beta_k
			double beta_1, beta_3;
			if ( M == 3 )
			{
				//build 6 minor 2x2 matrices to invA by excluding i-th row and j-th column from invA,
				//and calculate their determinant Dij with the Sarrus rule
				//
				//        | p_1  p_3  p_5 |
				// invA = | r_1  r_3  r_5 |
				//        | C_15 C_35  1  |

				final double D11 = r_3 - r_5 * C_35;
				final double D21 = p_3 - p_5 * C_35;
				final double D31 = D_35;

				final double D12 = r_1 - r_5 * C_15;
				final double D22 = p_1 - p_5 * C_15;
				final double D32 = -D_51;

				final double det_invA = p_1 * D11 - r_1 * D21 + C_15 * D31;

				// eq. (53), only first two rows of matrix A_N
				beta_1 = ( +D11 - ( N1 * N1 - sigma * sigma ) * D21
						+ ( C_15 * rho_1 + C_35 * rho_3 + rho_5 ) * D31 ) / det_invA;
				beta_3 = ( -D12 + ( N1 * N1 - sigma * sigma ) * D22
						- ( C_15 * rho_1 + C_35 * rho_3 + rho_5 ) * D32 ) / det_invA;
			}
			else
			{ // M == 4
				//build 8 minor 3x3 matrices to invA by excluding i-th row and j-th column from invA,
				//and calculate their determinant Dij with the Sarrus rule
				//
				//        | p_1  p_3  p_5  p_7 |
				//        | r_1  r_3  r_5  r_7 |
				// invA = | C_15 C_35  1    0  |
				//        | C_17 C_37  0    1  |

				final double D11 = r_3 - r_5 * C_35 - r_7 * C_37;
				final double D21 = p_3 - p_5 * C_35 - p_7 * C_37;
				final double D31 = p_3 * r_5 + p_5 * r_7 + C_37 - p_5 * r_3 - p_7 * r_5 * C_37;
				final double D41 = p_5 * r_7 * C_35 + p_7 * r_3 - p_3 * r_7 - p_7 * r_5 * C_35;

				final double D12 = r_1 - r_5 * C_15 - r_7 * C_17;
				final double D22 = p_1 - p_5 * C_15 - p_7 * C_17;
				final double D32 = p_1 * r_5 + p_5 * r_7 * C_17 - p_5 * r_1 - p_7 * r_5 * C_17;
				final double D42 = p_5 * r_7 * C_15 + p_7 * r_1 - p_1 * r_7 - p_7 * r_5 * C_15;

				final double det_invA = p_1 * D11 - r_1 * D21 + C_15 * D31 - C_17 * D41;

				// eq. (53), only first two rows of matrix A_N
				beta_1 = ( +D11 - ( N1 * N1 - sigma * sigma ) * D21 + ( C_15 * rho_1 + C_35 * rho_3 + rho_5 ) * D31
						- ( C_17 * rho_1 + C_37 * rho_3 + rho_7 ) * D41 ) / det_invA;
				beta_3 = ( -D12 + ( N1 * N1 - sigma * sigma ) * D22 - ( C_15 * rho_1 + C_35 * rho_3 + rho_5 ) * D32
						+ ( C_17 * rho_1 + C_37 * rho_3 + rho_7 ) * D42 ) / det_invA;
			}

			// eq. (49), since I didn't want to continue building A_N to be used in eq. (53),
			// I found it too expensive to calculate D[1234][34] to get beta_[57] in the same
			// way as beta_[13]
			final double beta_5 = rho_5 + C_15 * ( rho_1 - beta_1 ) + C_35 * ( rho_3 - beta_3 );
			final double beta_7 = rho_7 + C_17 * ( rho_1 - beta_1 ) + C_37 * ( rho_3 - beta_3 );

			// fill the output container FilteringCoeffs
			N = ( int ) N1;

			nk_2[ 0 ] = -beta_1 * Math.cos( omega_1 * ( N1 + 1.0 ) );
			nk_2[ 1 ] = -beta_3 * Math.cos( omega_3 * ( N1 + 1.0 ) );
			nk_2[ 2 ] = -beta_5 * Math.cos( omega_5 * ( N1 + 1.0 ) );

			dk_1[ 0 ] = -2.0 * Math.cos( omega_1 );
			dk_1[ 1 ] = -2.0 * Math.cos( omega_3 );
			dk_1[ 2 ] = -2.0 * Math.cos( omega_5 );

			if ( M == 4 )
			{
				nk_2[ 3 ] = -beta_7 * Math.cos( omega_7 * ( N1 + 1.0 ) );
				dk_1[ 3 ] = -2.0 * Math.cos( omega_7 );
				// NB: array length was guarded at the beginning of this function
			}

			// declare to what sigma the coefficients belong to
			Sigma = sigma;
		}
	}
}
