package com.irar.mbviewer.mandelbrot;

import java.util.HashMap;

import com.irar.mbviewer.math.Complex2;
import com.irar.mbviewer.math.Complex3;

public class Iteration {

	public final int iterations;
	public final float partial;
	public final int skipped;
	private Complex2 baseLocation;
	private Complex3 offsetLocation;
	private int maxIter;
	
	private HashMap<String, Double> extraData = new HashMap<>();
	
	public Iteration(int iterations, float partial, int maxIter, int skipped, Complex2 baseLocation, Complex3 offsetLocation) {
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
		this.skipped = skipped;
	}
	
	public Iteration(int iterations, float partial, int maxIter, int skipped, Complex2 baseLocation, Complex2 actualLocation) {
		this(iterations, partial, maxIter, skipped, baseLocation, calculateOffset(baseLocation, actualLocation));
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

	public Complex2 getBaseLocation() {
		return baseLocation;
	}

	public Complex3 getOffsetLocation() {
		return offsetLocation;
	}

	public int getMaxIter() {
		return this.maxIter;
	}
	
}
