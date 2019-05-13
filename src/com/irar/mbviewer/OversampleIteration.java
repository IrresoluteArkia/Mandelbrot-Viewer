package com.irar.mbviewer;

import java.util.Arrays;
import java.util.List;

public class OversampleIteration {

	private Iteration[] iterations;

	public OversampleIteration(Iteration... iterations) {
		this.iterations = iterations;
	}

	public List<Iteration> getIterations() {
		return Arrays.asList(iterations);
	}

}
