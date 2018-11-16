package com.irar.mbviewer;

import java.math.BigDecimal;

public class Complex3 {

	public SizedDouble x;
	public SizedDouble y;
	
	public Complex3(SizedDouble x, SizedDouble y) {
		this.x = x;
		this.y = y;
	}
	
	public Complex3(Complex2 cx2) {
		this(SizedDouble.parseSizedDouble(cx2.x), SizedDouble.parseSizedDouble(cx2.y));
	}

/*	public Complex3 pow(double pow) {
		if((double) (int) pow == pow && pow >= 2 && pow < 30) {
			return pow((int) pow);
		}
		double theta = Math.atan2(y, x);
		double powtheta = theta * pow;
		double cpt = Math.cos(powtheta);
		double spt = Math.sin(powtheta);
		double rpow = Math.pow(Math.sqrt(x*x + y*y), pow);
		double r = cpt * rpow;
		double i = spt * rpow;
		return new Complex3(r, i);
	}*/
	
	public Complex3(double x, double y) {
		this(new SizedDouble(x), new SizedDouble(y));
	}

	public Complex3(Complex c) {
		this(c.x, c.y);
	}

	public Complex3 pow(int pow) {
		Complex3 prod = this;
		for(int i = 1; i < pow; i++) {
			prod = this.multiply(prod);
		}
		return prod;
	}
	
	public Complex3 multiply(Complex3 c) {
		SizedDouble x1 = c.x;
		SizedDouble y1 = c.y;
		SizedDouble x2 = x.multiply(x1).add(y.multiply(y1).negative());
		SizedDouble y2 = x.multiply(y1).add(x1.multiply(y));
		return new Complex3(x2, y2);
	}
	
	public Complex3 multiply(double d) {
		SizedDouble x2 = x.multiply(d);
		SizedDouble y2 = y.multiply(d);
		return new Complex3(x2, y2);
	}
	
	public Complex3 add(Complex3 c) {
		SizedDouble x1 = c.x;
		SizedDouble y1 = c.y;
		SizedDouble x2 = x.add(x1);
		SizedDouble y2 = y.add(y1);
		return new Complex3(x2, y2);
	}

	public SizedDouble magSqu() {
		return x.multiply(x).add(y.multiply(y));
	}

	public SizedDouble mag() {
		return magSqu().sqrt();
	}

	public Complex3 addReal(SizedDouble d) {
		return new Complex3(x.add(d), y);
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof Complex3) {
			Complex3 c = (Complex3) o;
			if(c.x.compareTo(this.x) == 0 && c.y.compareTo(this.y) == 0) {
				return true;
			}
		}
		return false;
	}

	public Complex3 addReal(double d) {
		return this.addReal(new SizedDouble(d));
	}
	
	@Override
	public String toString() {
		return x.toString() + " + " + y.toString() + "i";
	}
	
}
