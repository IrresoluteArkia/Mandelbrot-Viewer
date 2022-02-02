package com.irar.mbviewer.math;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import ch.obermuhlner.math.big.BigDecimalMath;

public class Complex2 {

	public static final Complex2 ZERO = new Complex2(new BigDecimal(0), new BigDecimal(0), 10);
	public BigDecimal x;
	public BigDecimal y;
	private int pre;
	
	public Complex2(BigDecimal x, BigDecimal y, int pre) {
		x.setScale(pre, RoundingMode.DOWN);
		y.setScale(pre, RoundingMode.DOWN);
		this.x = x;
		this.y = y;
		this.pre = pre;
	}
	
	public Complex2(Complex complex, int pre) {
		this.x = new BigDecimal(complex.x);
		this.y = new BigDecimal(complex.y);
		this.pre = pre;
	}

	public Complex2(Complex3 complex3, int pre) {
		this.x = complex3.x.asBigDecimal(pre);
		this.y = complex3.y.asBigDecimal(pre);
		this.pre = pre;
	}

	public Complex2 pow(double pow) {
		if((double) (int) pow == pow && pow >= 2 && pow < 30) {
			return pow((int) pow);
		}
		MathContext c = new MathContext(pre);
		BigDecimal theta = BigDecimalMath.atan2(y, x, c).setScale(pre, RoundingMode.DOWN);
		BigDecimal powtheta = theta.multiply(new BigDecimal(pow).setScale(pre, RoundingMode.DOWN)).setScale(pre, RoundingMode.DOWN);
		BigDecimal cpt = BigDecimalMath.cos(powtheta, c).setScale(pre, RoundingMode.DOWN);
		BigDecimal spt = BigDecimalMath.sin(powtheta, c).setScale(pre, RoundingMode.DOWN);
		BigDecimal rpow = BigDecimalMath.pow(BigDecimalMath.sqrt(x.multiply(x).setScale(pre, RoundingMode.DOWN).add(y.multiply(y).setScale(pre, RoundingMode.DOWN)), c), new BigDecimal(pow).setScale(pre, RoundingMode.DOWN), c).setScale(pre, RoundingMode.DOWN);
		BigDecimal r = cpt.multiply(rpow).setScale(pre, RoundingMode.DOWN);
		BigDecimal i = spt.multiply(rpow).setScale(pre, RoundingMode.DOWN);
		return new Complex2(r, i, pre);
	}
	
	public Complex2 pow(int pow) {
		Complex2 prod = this;
		for(int i = 1; i < pow; i++) {
			prod = this.multiply(prod);
		}
		return prod;
	}
	
	public Complex2 multiply(Complex2 c) {
		BigDecimal x1 = c.x;
		BigDecimal y1 = c.y;
		BigDecimal x2 = x.multiply(x1).setScale(pre, RoundingMode.DOWN).subtract(y.multiply(y1).setScale(pre, RoundingMode.DOWN));
		BigDecimal y2 = x.multiply(y1).setScale(pre, RoundingMode.DOWN).add(x1.multiply(y).setScale(pre, RoundingMode.DOWN));
		return new Complex2(x2, y2, pre);
	}
	
	public Complex2 multiply(double d) {
		BigDecimal db = new BigDecimal(d);
		BigDecimal x2 = x.multiply(db).setScale(pre, RoundingMode.DOWN);
		BigDecimal y2 = y.multiply(db).setScale(pre, RoundingMode.DOWN);
		return new Complex2(x2, y2, pre);
	}
	
	public Complex2 add(Complex2 c) {
		BigDecimal x1 = c.x;
		BigDecimal y1 = c.y;
		BigDecimal x2 = x.add(x1);
		BigDecimal y2 = y.add(y1);
		return new Complex2(x2, y2, pre);
	}

	public double mag() {
		return Math.sqrt(magSqu());
	}

	public double magSqu() {
		double dx = x.doubleValue();
		double dy = y.doubleValue();
		return dx * dx + dy * dy;
	}

	public Complex2 addReal(int i) {
		return new Complex2(x.add(new BigDecimal(i)), y, pre);
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof Complex2) {
			Complex2 c = (Complex2) o;
			if(c.x.compareTo(this.x) == 0 && c.y.compareTo(this.y) == 0) {
				return true;
			}
		}
		return false;
	}

	public Complex2 subtract(Complex2 c) {
		BigDecimal x1 = c.x;
		BigDecimal y1 = c.y;
		BigDecimal x2 = x.subtract(x1);
		BigDecimal y2 = y.subtract(y1);
		return new Complex2(x2, y2, pre);
	}
	
	public Complex2 setScale(int pre) {
		return new Complex2(x, y, pre);
	}

	public int getScale() {
		return pre;
	}
	
	public Complex2 divide(Complex2 c) {
		BigDecimal den = new BigDecimal(Math.pow(c.mag(), 2));
        return new Complex2((x.multiply(c.x).add(y.multiply(c.y))).divide(den, pre, RoundingMode.DOWN), (y.multiply(c.x).subtract(x.multiply(c.y))).divide(den, pre, RoundingMode.DOWN), pre);
	}

	public BigDecimal cross(Complex2 c) {
		return y.multiply(c.x).subtract(x.multiply(c.y));
	}
	
	public BigDecimal magBD() {
		MathContext c = new MathContext(pre * 2);
		return BigDecimalMath.sqrt(x.multiply(x).add(y.multiply(y)), c);
	}

	public SizedDouble magSD() {
		return SizedDouble.parseSizedDouble(magBD());
	}

	static BigDecimal two = new BigDecimal(2);
	public Complex2 pow(Complex pow) {
		if(pow.y == 0) {
			return pow(pow.x);
		}
		MathContext con = new MathContext(pre);
		BigDecimal a = x;
		BigDecimal b = y;
		BigDecimal c = new BigDecimal(pow.x).setScale(pre, RoundingMode.DOWN);
		BigDecimal d = new BigDecimal(pow.y).setScale(pre, RoundingMode.DOWN);
		BigDecimal aabb = a.multiply(a).setScale(pre, RoundingMode.DOWN).add(b.multiply(b).setScale(pre, RoundingMode.DOWN)).setScale(pre, RoundingMode.DOWN);
		BigDecimal m1 = BigDecimalMath.pow(aabb, c.divide(two, RoundingMode.DOWN), con);
		BigDecimal arg = BigDecimalMath.atan2(b, a, con);
		BigDecimal m2 = BigDecimalMath.exp(d.negate().multiply(arg).setScale(pre, RoundingMode.DOWN), con);
		BigDecimal ms = c.multiply(arg).setScale(pre, RoundingMode.DOWN).add(d.divide(two, RoundingMode.DOWN)).setScale(pre, RoundingMode.DOWN).multiply(BigDecimalMath.log(aabb, con)).setScale(pre, RoundingMode.DOWN);
		BigDecimal m12 = m2.multiply(m1).setScale(pre, RoundingMode.DOWN);
		BigDecimal m3r = BigDecimalMath.cos(ms, con);
		BigDecimal m3i = BigDecimalMath.sin(ms, con);
		return new Complex2(m12.multiply(m3r), m12.multiply(m3i), pre);
	}
	
}
