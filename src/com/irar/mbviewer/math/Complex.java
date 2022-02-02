package com.irar.mbviewer.math;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Complex {

	public double x;
	public double y;
	
	public Complex(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public Complex(Complex2 cx2) {
		this(cx2.x.doubleValue(), cx2.y.doubleValue());
	}

	public Complex(Complex3 complex3) {
		this(complex3.x.asDouble(), complex3.y.asDouble());
	}

	public Complex pow(double pow) {
		if((double) (int) pow == pow && pow >= 2 && pow < 30) {
			return pow((int) pow);
		}
		double theta = Math.atan2(y, x);
		double powtheta = theta * pow;
		double cpt = Math.cos(powtheta);
		double spt = Math.sin(powtheta);
		double rpow = Math.pow(x*x + y*y, pow / 2);
		double r = cpt * rpow;
		double i = spt * rpow;
		return new Complex(r, i);
	}
	
	public Complex pow(int pow) {
		Complex prod = this;
		for(int i = 1; i < pow; i++) {
			prod = this.multiply(prod);
		}
		return prod;
	}
	
	public Complex multiply(Complex c) {
		double x1 = c.x;
		double y1 = c.y;
		double x2 = x * x1 - y * y1;
		double y2 = x * y1 + x1 * y;
		return new Complex(x2, y2);
	}
	
	public Complex multiply(double d) {
		double x2 = x * d;
		double y2 = y * d;
		return new Complex(x2, y2);
	}
	
	public Complex add(Complex c) {
		double x1 = c.x;
		double y1 = c.y;
		double x2 = x + x1;
		double y2 = y + y1;
		return new Complex(x2, y2);
	}

	public double mag() {
		return Math.sqrt(x * x + y * y);
	}

	public double magSqu() {
		return x * x + y * y;
	}

	public Complex addReal(int i) {
		return new Complex(x + i, y);
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof Complex) {
			Complex c = (Complex) o;
			if(c.x == this.x && c.y == this.y) {
				return true;
			}
		}
		return false;
	}

	public BigDecimal magSquBig(int pre) {
		BigDecimal bX = new BigDecimal(x).setScale(pre, RoundingMode.DOWN);
		BigDecimal bY = new BigDecimal(y).setScale(pre, RoundingMode.DOWN);
		return bX.multiply(bX).setScale(pre, RoundingMode.DOWN).add(bY.multiply(bY).setScale(pre, RoundingMode.DOWN));
	}

	public SizedDouble magSquSD() {
		SizedDouble sdX = new SizedDouble(x);
		SizedDouble sdY = new SizedDouble(y);
		return sdX.multiply(sdX).add(sdY.multiply(sdY));
	}
	
	@Override
	public String toString() {
		return x + " + " + y + "i";
	}

	public Complex subtract(Complex c) {
		double x1 = c.x;
		double y1 = c.y;
		double x2 = x - x1;
		double y2 = y - y1;
		return new Complex(x2, y2);
	}

	public Complex pow(Complex pow) {
		if(pow.y == 0) {
			return pow(pow.x);
		}
		double a = x;
		double b = y;
		double c = pow.x;
		double d = pow.y;
		double aabb = a*a + b*b;
		double m1 = Math.pow(aabb, c/2);
		double arg = Math.atan2(b, a);
		double m2 = Math.exp(-d * arg);
		double ms = c * arg + (d/2) * Math.log(aabb);
		double m12 = m2 * m1;
		double m3r = Math.cos(ms);
		double m3i = Math.sin(ms);
		return new Complex(m12 * m3r, m12 * m3i);
	}

	public static Complex parseString(String value) {
		String[] split = value.split(" ");
		return new Complex(Double.parseDouble(split[0]), Double.parseDouble(split[2].substring(0, split[2].length()-1)));
	}
	
}
