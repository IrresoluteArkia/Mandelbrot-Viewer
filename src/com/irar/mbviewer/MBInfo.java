package com.irar.mbviewer;

import java.math.BigDecimal;
import java.util.HashMap;

public class MBInfo {

	private BigDecimal x = new BigDecimal(0); 
	private BigDecimal y = new BigDecimal(0); 
	private BigDecimal prevX = new BigDecimal(0);
	private BigDecimal prevY = new BigDecimal(0);
	private SizedDouble zoom = new SizedDouble(1); 
	private SizedDouble prevZoom = new SizedDouble(1); 
	private int iterations = 256;
	private String name = "";
	private Palette palette = null;
	private int oversample = 1;
	private boolean shufflePoints = false;
	private Complex power = new Complex(2, 0);
	private double blur = 0.0;
	private boolean doHist = false;
	
	public MBInfo setX(BigDecimal x) {
		this.prevX = this.x;
		this.x = x;
		return this;
	}
	
	public MBInfo setY(BigDecimal y) {
		this.prevY = this.y;
		this.y = y;
		return this;
	}
	
	public MBInfo setZoom(SizedDouble zoom) {
		this.prevZoom = this.zoom;
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
	
	public BigDecimal getX() {
		return x;
	}
	
	public BigDecimal getY() {
		return y;
	}

	public Palette getPalette() {
		return palette;
	}
	
	public SizedDouble getZoom() {
		return zoom;
	}

	public MBInfo setOversample(int oversample) {
		this.oversample = oversample;
		return this;
	}

	public MBInfo setShufflePoints(boolean shufflePoints) {
		this.shufflePoints = shufflePoints;
		return this;
	}

	public MBInfo setPower(Complex power) {
		this.power = power;
		return this;
	}

	public MBInfo setBlur(double blur) {
		this.blur = blur;
		return this;
	}

	public MBInfo setDoHist(boolean doHist) {
		this.doHist = doHist;
		return this;
	}
	
	public int getIterations() {
		return iterations;
	}

	public int getOversample() {
		return oversample;
	}

	public boolean shouldShufflePoints() {
		return shufflePoints;
	}

	public Complex getPower() {
		return power;
	}

	public double getBlur() {
		return blur;
	}

	public boolean shouldDoHist() {
		return doHist;
	}

	public HashMap<String, String> getData() {
		HashMap<String, String> data = new HashMap<>();
		data.put("x", x.toString());
		data.put("y", y.toString());
		data.put("zoom", zoom.toString());
		data.put("iterations", iterations + "");
		data.put("name", name);
		data.put("palette", palette.toString());
		data.put("oversample", oversample + "");
		data.put("shufflePoints", shufflePoints + "");
		data.put("power", power.toString());
		data.put("blur", blur + "");
		data.put("doHist", doHist + "");
		return data;
	}
	
	public static MBInfo withData(HashMap<String, String> data) {
		MBInfo info = new MBInfo();
		try{
			for(String key : data.keySet()) {
				String value = data.get(key);
				switch(key) {
				case "x": info.x = new BigDecimal(value); break;
				case "y": info.y = new BigDecimal(value); break;
				case "zoom": info.zoom = SizedDouble.parseSizedDouble(value); break;
				case "iterations": info.iterations = Integer.parseInt(value); break;
				case "name": info.name = value; break;
				case "palette": info.palette = Palette.fromString(value); break;
				case "oversample": info.oversample = Integer.parseInt(value); break;
				case "shufflePoints": info.shufflePoints = Boolean.parseBoolean(value); break;
				case "power": info.power = Complex.parseString(value); break;
				case "blur": info.blur = Double.parseDouble(value); break;
				case "doHist": info.doHist = Boolean.parseBoolean(value); break;
				}
			}
		}catch(Exception e) {
			
		}
		return info;
	}

	public BigDecimal getPrevX() {
		return prevX;
	}

	public BigDecimal getPrevY() {
		return prevY;
	}

	public SizedDouble getPrevZoom() {
		return prevZoom;
	}

	public void syncPrev() {
		prevX = x;
		prevY = y;
		prevZoom = zoom;
	}
	
	
	
}
