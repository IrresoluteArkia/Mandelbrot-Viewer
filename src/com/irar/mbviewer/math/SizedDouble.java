package com.irar.mbviewer.math;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class SizedDouble {

	public static final SizedDouble ZERO = new SizedDouble(0);
	public final double d;
	public final int size;
	
	public SizedDouble(double d, int size) {
		while(d >= 10) {
			d /= 10;
			size++;
		}
		while(d < 1 && d > 0) {
			d *= 10;
			size--;
		}
		while(d <= -10) {
			d /= 10;
			size++;
		}
		while(d > -1 && d < 0) {
			d *= 10;
			size--;
		}
		if(d == 0) {
			size = 0;
		}
		this.d = d;
		this.size = size;
	}
	
	public SizedDouble(double d) {
		this(d, 0);
	}
	
	public SizedDouble add(SizedDouble sd) {
		SizedDouble result;
		if(sd.size == size) {
			result = new SizedDouble(d + sd.d, size);
		}else {
			if(size > sd.size) {
				int dif = size - sd.size;
/*				if(dif >= 24) {
					result = new SizedDouble(d, size);
				}else {*/
					result = new SizedDouble(d + sd.d / Math.pow(10, dif), size);
//				}
			}else {
				int dif = sd.size - size;
/*				if(dif >= 24) {
					result = new SizedDouble(sd.d, sd.size);
				}else {*/
					result = new SizedDouble(d / Math.pow(10, dif) + sd.d, sd.size);
//				}
			}
		}
		return result;
	}
	
	public SizedDouble negative() {
		return new SizedDouble(-d, size);
	}
	
	public SizedDouble multiply(SizedDouble sd) {
		int newSize = size + sd.size;
		return new SizedDouble(sd.d * d, newSize);
	}
	
	public SizedDouble divide(SizedDouble sd) {
		int newSize = size - sd.size;
		return new SizedDouble(d / sd.d, newSize);
	}
	
	public int compareTo(SizedDouble sd) {
		boolean p1 = d > 0;
		boolean p2 = sd.d > 0;
		if(p1 && !p2) {
			return 1;
		}else if(!p1 && p2) {
			return -1;
		}else if(p1 && p2) {
			if(size == sd.size) {
				if(d == sd.d) {
					return 0;
				}else if(d > sd.d) {
					return 1;
				}else{
					return -1;
				}
			}else if(size > sd.size) {
				return 1;
			}else {
				return -1;
			}
		}else {
			if(size == sd.size) {
				if(d == sd.d) {
					return 0;
				}else if(d < sd.d) {
					return 1;
				}else{
					return -1;
				}
			}else if(size > sd.size) {
				return -1;
			}else {
				return 1;
			}
		}
	}

	public static void test() {
		SizedDouble sd1 = new SizedDouble(8, 0);
		SizedDouble sd2 = new SizedDouble(8, 0);
		SizedDouble sd3 = sd1.multiply(sd2);
		System.out.println(sd1 + " * " + sd2 + " = " + sd3);
		double dTest = new SizedDouble(1.64334, -56).asDouble();
		long bits = Double.doubleToLongBits(dTest);
		long exp = ((bits & 0x7ff0000000000000L) >> 52) - 1023;
		System.out.println(exp);
//		Math.log10(a)
//		System.out.println(parseSizedDouble(new BigDecimal(Viewer.zoom)));
	}
	
	public static SizedDouble parseSizedDouble(String s) {
		String[] split = s.split("E");
		double d = Double.parseDouble(split[0]);
		int size = Integer.parseInt(split[1]);
		return new SizedDouble(d, size);
	}
	public static int digits = (Math.PI + "").length();
	public static SizedDouble parseSizedDouble(BigDecimal bd) {
		String s = bd.toString();
		String[] split = s.split("E");
		double d = 0;
		int size = 0;
		String ds = split[0];
		if(ds.length() > digits) {
			ds = ds.substring(0, digits);
		}
		d = Double.parseDouble(ds);
		if(split.length == 1) {
			size = 0;
		}else if(split.length == 2) {
			size = Integer.parseInt(split[1]);
		}
		return new SizedDouble(d, size);
	}
	
	@Override
	public String toString() {
		return d + "E" + size;
	}

	public String toString(int figs) {
		String dou = d + "";
		if(figs < dou.length() - 1) {
			dou = dou.substring(0, figs + 1);
		}
		return dou + "E" + size;
	}

	public SizedDouble multiply(double d) {
		return this.multiply(new SizedDouble(d));
	}

	public BigDecimal asBigDecimal(int pre) {
		return new BigDecimal(this.toString()).setScale(pre, RoundingMode.DOWN);
	}

	public SizedDouble divide(double d) {
		return this.divide(new SizedDouble(d));
	}

	public SizedDouble add(double d) {
		return this.add(new SizedDouble(d));
	}
	
	public double asDouble() {
		return d * Math.pow(10, size);
	}
	
	private static double sqTen = Math.sqrt(10);
	public SizedDouble sqrt() {
		double dPart = Math.sqrt(d);
		int sPart = size / 2;
		if(size % 2 == 1) {
			if(size > 0) {
				dPart *= sqTen;
			}else {
				dPart /= sqTen;
			}
		}
		return new SizedDouble(dPart, sPart);
	}

	public double log() {
		// log = log(d) + size*log(10)
		return Math.log(d) + size * Math.log(10);
	}
	
}
