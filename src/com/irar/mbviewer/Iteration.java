package com.irar.mbviewer;

import java.util.HashMap;

public class Iteration {

	final int iterations;
	final float partial;
	
	private HashMap<String, Double> extraData = new HashMap<>();
	
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
	
	public HashMap<String, Double> getExtraData(){
		return this.extraData;
	}
	
	public void setExtraData(HashMap<String, Double> extraData) {
		this.extraData = extraData;
	}
	
}
