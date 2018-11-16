package com.irar.mbviewer;

public class Approx {

	Complex A;
	Complex B;
	Complex C;
	Complex X;
	
	public Approx(Approx prev, Complex X) {
		A = prev.X.multiply(prev.A).multiply(2).addReal(1);
		B = prev.X.multiply(prev.B).multiply(2).add(prev.A.multiply(prev.A));
		C = prev.X.multiply(prev.C).add(prev.A.multiply(prev.B)).multiply(2);
		this.X = X;
	}
	
	public Approx(Complex X, Complex X0) {
		this.X = X;
		A = new Complex(1, 0);
		B = new Complex(0, 0);
		C = new Complex(0, 0);
		A = X0.multiply(A).multiply(2).addReal(1);
		B = X0.multiply(B).multiply(2).add(A.multiply(A));
		C = X0.multiply(C).add(A.multiply(B)).multiply(2);
	}
	
	public Complex getDelta(Complex delta0) {
		Complex delpow2 = delta0.multiply(delta0);
		Complex delpow3 = delpow2.multiply(delta0);
		Complex delpow4 = delpow3.multiply(delta0);
		
		Complex delta = A.multiply(delta0).add(B.multiply(delpow2)).add(C.multiply(delpow3)).add(delpow4);
		
		return delta;
	}
	
}
