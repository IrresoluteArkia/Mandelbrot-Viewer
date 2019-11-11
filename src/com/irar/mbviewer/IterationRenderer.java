package com.irar.mbviewer;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;

public class IterationRenderer implements IMBRenderer{

	@Override
	public void drawIterations(OversampleIteration[][] iterations, BufferedImage bi, MBInfo info) {
		BufferedImage bi2 = new BufferedImage(bi.getWidth(), bi.getHeight(), bi.getType());
		for(int i = 0; i < iterations.length; i++) {
			OversampleIteration[] i2 = iterations[i];
			for(int j = 0; j < i2.length; j++) {
				OversampleIteration iterSamples = i2[j];
				int[] sampleColors = getColors(iterSamples, info.getPalette(), info.getIterations());
				setColor(bi2, i, j, sampleColors);
			}
		}
		Graphics g = bi.getGraphics();
		g.drawImage(bi2, 0, 0, null);
	}

	private int[] getColors(OversampleIteration iteration, Palette palette, int maxIter) {
		List<Iteration> iterSamples = iteration.getIterations();
		int[] colors;
		if(iterSamples == null) {
			colors = new int[] {255 * 256 * 256};
		}else {
			colors = new int[iterSamples.size()];
			if(iterSamples.size() > 0 && iterSamples.get(0).iterations < maxIter) {
				for(int i = 0; i < iterSamples.size(); i++) {
					colors[i] = getColor(iterSamples.get(i), palette);
				}
			}
		}
		return colors;
	}

	private int getColor(Iteration iteration, Palette palette) {
		int[] colors = palette.paletteloop;
		int color1 = colors[iteration.iterations % colors.length];
		int color2 = colors[(iteration.iterations + 1) % colors.length];
		int color = interColors(color1, color2, iteration.partial % 1);
		return color;
	}

	private static int rm = 0x00ff0000;
	private static int gm = 0x0000ff00;
	private static int bm = 0x000000ff;
	private int interColors(int color1, int color2, float f) {
		int red1 = (rm & color1) >> 16;
		int green1 = (gm & color1) >> 8;
		int blue1 = (bm & color1);
		int red2 = (rm & color2) >> 16;
		int green2 = (gm & color2) >> 8;
		int blue2 = (bm & color2);
		int difRed = Math.abs(red1 - red2);
		int difGreen = Math.abs(green1 - green2);
		int difBlue = Math.abs(blue1 - blue2);
		int resRed = (red1 < red2 ? (int) (f * difRed) : (int) ((1 - f) * difRed)) + Math.min(red1, red2);
		int resGreen = (green1 < green2 ? (int) (f * difGreen) : (int) ((1 - f) * difGreen)) + Math.min(green1, green2);
		int resBlue = (blue1 < blue2 ? (int) (f * difBlue) : (int) ((1 - f) * difBlue)) + Math.min(blue1, blue2);
		return (resRed << 16) + (resGreen << 8) + resBlue;
	}


	@Override
	public void modifyIterData(HashMap<String, Double> iterData, Complex delta, Complex x, int curIter) {
		// TODO Auto-generated method stub
		
	}


}
