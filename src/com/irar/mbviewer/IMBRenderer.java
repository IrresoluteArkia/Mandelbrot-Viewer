package com.irar.mbviewer;

import java.awt.image.BufferedImage;
import java.util.HashMap;

public interface IMBRenderer {

	public void drawIterations(OversampleIteration[][] allIterations, BufferedImage bi, MBInfo info);

	public void modifyIterData(HashMap<String, Double> iterData, Complex delta, Complex x, int curIter);
	
	default void setColor(BufferedImage bi, int x, int y, int[] color) {
		int totalA = 0;
		int totalR = 0;
		int totalG = 0;
		int totalB = 0;
		for(int i = 0; i < color.length; i++) {
			int a = (color[i] & 0xFF000000) >> 24;
			int r = (color[i] & 0x00FF0000) >> 16;
			int g = (color[i] & 0x0000FF00) >> 8;
			int b = (color[i] & 0x000000FF);
			totalA += a;
			totalR += r;
			totalG += g;
			totalB += b;
		}
		int clf = Math.max(1, color.length);
		int avgA = totalA / clf;
		int avgR = totalR / clf;
		int avgG = totalG / clf;
		int avgB = totalB / clf;
		int avg = (avgA << 24) + (avgR << 16) + (avgG << 8) + avgB;
		bi.setRGB(x, y, avg);
	}

}
