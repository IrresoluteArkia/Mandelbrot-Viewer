package com.irar.mbviewer;

import java.awt.image.BufferedImage;
import java.util.HashMap;

public class HistogramRenderer implements IMBRenderer{

	@Override
	public void drawIterations(OversampleIteration[][] allIterations, BufferedImage bi, MBInfo info) {
		int[] hist = new int[info.getIterations()];
		int total = 0;
		for(OversampleIteration[] iterTemp1 : allIterations) {
			for(OversampleIteration overIter : iterTemp1) {
				for(Iteration iter : overIter.getIterations()) {
					if(iter.iterations < info.getIterations()) {
						hist[iter.iterations]++;
						total++;
					}
				}
			}
		}
		double[] hues = new double[hist.length];
		double huetrack = 0;
		for(int i = 0; i < hist.length; i++) {
			huetrack += (double) hist[i] / total;
			hues[i] = huetrack;
		}
		int x = 0;
		int y = 0;
		for(OversampleIteration[] iterTemp1 : allIterations) {
			for(OversampleIteration overIter : iterTemp1) {
				boolean isBlack = false;
				double[] iterHues = new double[overIter.getIterations().size()];
				for(int i = 0; i < iterHues.length; i++) {
					Iteration iter = overIter.getIterations().get(i);
					if(iter.iterations >= info.getIterations()) {
						isBlack = true;
					}
					iterHues[i] = hues[iter.iterations - 1];
				}
				double hue = 0;
				for(double iterHue : iterHues) {
					hue += iterHue;
				}
				hue /= iterHues.length;
				hue = limit(0, 1, hue);
				if(isBlack) {
					hue = 1;
				}
				color(x, y, hue, info.getPalette(), bi);
				
				y++;
			}
			x++;
			y=0;
		}
	}

	private double limit(int min, int max, double num) {
		return Math.min(Math.max(min, num), max);
	}

	private void color(int x, int y, double hue, Palette palette, BufferedImage bi) {
		int color = 0;
		if(hue >= 1) {
			color = 0;
		}else {
			color = palette.palette[(int) (hue * palette.palette.length)];
		}
		bi.setRGB(x, y, color);
	}

	@Override
	public void modifyIterData(HashMap<String, Double> iterData, Complex delta, Complex x, int curIter) {
		// TODO Auto-generated method stub
		
	}

}
