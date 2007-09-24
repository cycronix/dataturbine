/**
  * An immutable Complex implementation.
  * Note: this class is not efficient.  For efficiency, see C++.
  */
class Complex
{
	public Complex() { real = imag = 0; }
	public Complex(double real, double imag)
	{ this.real = real; this.imag = imag; }
	
	// *** Scalar Operations ***
	public Complex add(double x)
	{ return new Complex(real+x, imag); }
	public Complex mul(double x)
	{ return new Complex(real*x, imag*x); }
	public Complex div(double x)
	{ return new Complex(real/x, imag/x); }
	
	// *** Complex Operations ***
	public Complex add(Complex c)
	{ return new Complex(real+c.real, imag+c.imag); }
	public Complex mul(Complex c)
	{
		return new Complex(
				real * c.real - imag * c.imag,
				real * c.imag + imag * c.real
		);
	}

	public Complex div(Complex c)
	{
		double _Yre = c.real, _Yim = c.imag;
		// Find most robust scaling.
		if ((_Yim < 0 ? -_Yim : +_Yim)
		  < (_Yre < 0 ? -_Yre : +_Yre)) {
			// Divide by real first.
			double _Wr = _Yim / _Yre;
			double _Wd = _Yre + _Wr * _Yim;				
			double _W = (real + imag * _Wr) / _Wd;
			
			return new Complex(
					_W,
					(imag - real * _Wr) / _Wd
			);
		}
		// Divide by imag first.
		double _Wr = _Yre / _Yim,
				_Wd = _Yim + _Wr * _Yre,
				_W = (real * _Wr + imag) / _Wd;
		return new Complex(
				_W,
				(imag * _Wr - real) / _Wd
		);
	}
	
	// *** Unary Operations ***
	public Complex neg() { return new Complex(-real, -imag); }
	public double abs() { return Math.sqrt(real*real+imag*imag); }
	public double real() { return real; }
	public double imag() { return imag; }
	
	public String toString() { return ""+real+" + "+imag+"i"; } 
	
	// *** Utility ***
	public static double[] poly(Complex[] e)
	{
		Complex[] c = new Complex[e.length+1]; 

		c[0] = new Complex(1, 0);
		
		for (int j = 0; j < e.length; ++j) {
			c[j+1] = new Complex();
			for (int k = j+1; k >= 1; --k) {
				c[k] = c[k].add(e[j].neg().mul(c[k-1]));

//System.err.println(c[k]+", ");
			}				
//System.err.println();
		}
		
		double[] p = new double[c.length];
		for (int ii = 0; ii < c.length; ++ii)
			p[ii] = c[ii].real;
		
		return p;
	}
	
	
	private final double real, imag;
} // end class Complex

/**
  * A low pass filter implementaion.
  */
class Filter
{
	/**
	  * Prototype Butterworth 8th order filter.
	  */
	private static final Complex[] protoPoles = {
			new Complex(-0.1950903220,  0.9807852804),
			new Complex(-0.1950903220, -0.9807852804),
			new Complex(-0.5555702330,  0.8314696123),
			new Complex(-0.5555702330, -0.8314696123),
			new Complex(-0.8314696123,  0.5555702330),
			new Complex(-0.8314696123, -0.5555702330),
			new Complex(-0.9807852804,  0.1950903220),
			new Complex(-0.9807852804, -0.1950903220)
	};       
	
	private static final Complex minusOne = new Complex(-1, 0);
	/**
	  * These are already in the Z domain.
	  */
	private static final Complex[] protoZeros = {
			minusOne,
			minusOne,
			minusOne,
			minusOne,
			minusOne,
			minusOne,
			minusOne,
			minusOne
	};
		
	/**
	  * Low pass filter.  Wn is normalized breakpoint frequency,
	  *  where Wn = 1 equals Nyquist.
	  */
	public Filter(double Wn)
	{
//System.err.println("Filter.  Wn = "+Wn+"\nContinuous:");
		final double fs = 2,
			u = 2*fs*Math.tan(Math.PI*Wn/fs);
			
		Complex[] poles = new Complex[protoPoles.length];
		
		double kn = 1, kd = 1;
		for (int ii = 0; ii < poles.length; ++ii) {
			poles[ii] = protoPoles[ii].mul(u);
			kn *= poles[ii].abs();
			kd *= poles[ii].neg().add(2*fs).abs();
//System.err.println("poles["+ii+"] = "+poles[ii]+", kn = "+kn+", kd = "+kd);				
		}
		bilinear(poles);
//System.err.println("Discrete:");
//for (int ii = 0; ii < poles.length; ++ii) System.err.println("poles["+ii+"] = "+poles[ii]);
		den = Complex.poly(poles);
//for (int ii = 0; ii < den.length; ++ii) System.err.println("den["+ii+"] = "+den[ii]);

		double k = kn / kd;
//System.err.println("Gain: "+k);			
		num = Complex.poly(protoZeros);
		for (int ii = 0; ii < num.length; ++ii) {
			num[ii] *= k;
//System.err.println("num["+ii+"] = "+num[ii]);				
		}
	}
	
	/**
	  * Perform filtering.  The vector src and dest should be the same
	  *  length, and cannot be the same vector.
	  */
	public void filter(double[] src, double[] dest)
	{
		// This code assumes the numerator and denominator are the 
		//  same length, and den[0] == 1.0.
		int n = src.length, nb = n < num.length ? n : num.length;
		
		// Startup.  src and outputs are zero before start of array.
		dest[0] = num[0] * src[0];
		for (int ii = 1; ii < nb; ++ii) {
			double y = num[0] * src[ii];
			for (int iii = 1; iii <= ii; ++iii)
				y += num[iii] * src[ii - iii] - den[iii] * dest[ii - iii];
			dest[ii] = y;
		}
		
		for (int ii = nb; ii < n; ++ii) {
			double y = num[0] * src[ii - 0];
			for (int iii = 1; iii < nb; ++iii)
				y += num[iii] * src[ii - iii] - den[iii] * dest[ii - iii];
			dest[ii] = y;
		}			
	}
	
	private static void bilinear(Complex[] p)
	{
/*
	fs = 2*fs;
end
z = z(finite(z));	 % Strip infinities from zeros
pd = (1+p/fs)./(1-p/fs); % Do bilinear transformation
zd = (1+z/fs)./(1-z/fs);
% real(kd) or just kd?
kd = (k*prod(fs-z)./prod(fs-p));
zd = [zd;-ones(length(pd)-length(zd),1)];  % Add extra zeros at -1
*/			
		int fs = 2 * 2;
		double kc = 1, kd = 1;
		for (int ii = 0; ii < p.length; ++ii) {
			kc *= p[ii].abs();
			kd *= p[ii].neg().add(fs).abs();
			p[ii] = p[ii].div(fs).add(1).div(p[ii].div(fs).neg().add(1));
		}
	}
	
	private final double[] num, den;
} // end class Filter		
