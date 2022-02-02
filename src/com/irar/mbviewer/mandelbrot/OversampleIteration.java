package com.irar.mbviewer.mandelbrot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OversampleIteration {

	private List<Iteration> iterations;

	public OversampleIteration(Iteration... iterations) {
		this.iterations = new ArrayList<>();
		for(Iteration i : iterations) {
			this.iterations.add(i);
		}
	}

	public List<Iteration> getIterations() {
		return iterations;
	}
	
	public void addIteration(Iteration iteration) {
		iterations.add(iteration);
	}

}
