package com.irar.mbviewer;

import java.util.HashMap;

public class Iteration {

	final int iterations;
	final float partial;
	private Complex2 baseLocation;
	private Complex3 offsetLocation;
	private int maxIter;
	
	private HashMap<String, Double> extraData = new HashMap<>();
	
	public Iteration(int iterations, float partial, int maxIter, Complex2 baseLocation, Complex3 offsetLocation) {
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
		this.baseLocation = baseLocation;
		this.offsetLocation = offsetLocation;
		this.maxIter = maxIter;
	}
	
	public Iteration(int iterations, float partial, int maxIter, Complex2 baseLocation, Complex2 actualLocation) {
		this(iterations, partial, maxIter, baseLocation, calculateOffset(baseLocation, actualLocation));
	}
	
	private static Complex3 calculateOffset(Complex2 baseLocation, Complex2 actualLocation) {
		return new Complex3(actualLocation.subtract(baseLocation));
	}
	
	public void changeBaseLocation(Complex2 newBase) {
		Complex2 actualLocation = new Complex2(offsetLocation, newBase.getScale()).add(baseLocation);
		offsetLocation = calculateOffset(newBase, actualLocation);
		baseLocation = newBase;
	}

	public HashMap<String, Double> getExtraData(){
		return this.extraData;
	}
	
	public void setExtraData(HashMap<String, Double> extraData) {
		this.extraData = extraData;
	}

	public Complex2 getActualLocation() {
		return new Complex2(offsetLocation, baseLocation.getScale()).add(baseLocation);
	}

	public int getMaxIter() {
		return this.maxIter;
	}
	
}
