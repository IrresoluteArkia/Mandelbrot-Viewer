package com.irar.mbviewer;

public class Iteration {

	final int iterations;
	final float partial;
	
	public Iteration(int iterations, float partial) {
		while(partial < 0) {
			partial += 1;
			iterations -= 1;
		}
		while(partial > 1) {
			partial -= 1;
			iterations += 1;
		}
		this.iterations = iterations;
		this.partial = partial;
	}
	
}
