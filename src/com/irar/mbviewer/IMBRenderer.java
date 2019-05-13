package com.irar.mbviewer;

import java.awt.image.BufferedImage;
import java.util.HashMap;

public interface IMBRenderer {

	public void drawIterations(OversampleIteration[][] allIterations, BufferedImage bi, MBInfo info);

	public void modifyIterData(HashMap<String, Double> iterData, Complex delta, Complex x, int curIter);
	
	default void setColor(BufferedImage bi, int x, int y, int[] color) {
		int totalR = 0;
		int totalG = 0;
		int totalB = 0;
		for(int i = 0; i < color.length; i++) {
			int r = (color[i] & 0xFF0000) >> 16;
			int g = (color[i] & 0x00FF00) >> 8;
			int b = (color[i] & 0x0000FF);
			totalR += r;
			totalG += g;
			totalB += b;
		}
		int avgR = totalR / color.length;
		int avgG = totalG / color.length;
		int avgB = totalB / color.length;
		int avg = (avgR << 16) + (avgG << 8) + avgB;
		bi.setRGB(x, y, avg);
	}

}
