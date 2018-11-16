package com.irar.mbviewer;

import java.math.BigDecimal;

public class MBInfo {

	public BigDecimal x = new BigDecimal(0); 
	public BigDecimal y = new BigDecimal(0); 
	public SizedDouble zoom = new SizedDouble(-1); 
	public int iterations = -1;
	public String name = null;
	public Palette palette;
	
	public MBInfo setX(BigDecimal x) {
		this.x = x;
		return this;
	}
	
	public MBInfo setY(BigDecimal y) {
		this.y = y;
		return this;
	}
	
	public MBInfo setZoom(SizedDouble zoom) {
		this.zoom = zoom;
		return this;
	}
	
	public MBInfo setIterations(int iterations) {
		this.iterations = iterations;
		return this;
	}

	public MBInfo setOutput(String name) {
		this.name = name;
		return this;
	}

	public boolean wasInitialized() {
		return name != null && zoom.compareTo(new SizedDouble(-1)) != 0 && iterations != -1;
	}

	public MBInfo setPalette(Palette p) {
		this.palette = p;
		return this;
	}
	
}
