package com.irar.mbviewer;

import java.awt.image.BufferedImage;
import java.util.HashMap;

public class GradientRenderer implements IMBRenderer{

	@Override
	public void drawIterations(OversampleIteration[][] allIterations, BufferedImage bi, MBInfo info) {
		for(int i = 0; i < allIterations.length; i++) {
			OversampleIteration[] overIter1 = allIterations[i];
			for(int j = 0; j < overIter1.length; j++) {
				OversampleIteration overIter = overIter1[j];
				int gradients[] = getGradient(allIterations, overIter, i, j);
				int colors[] = new int[gradients.length];
				int thinness = 10;
				for(int k = 0; k < gradients.length; k++) {
					int gradient = gradients[k];
					if(gradient >= thinness) {
						colors[k] = info.getPalette().paletteloop[(gradient-thinness) % info.getPalette().paletteloop.length];
					}
				}
				this.setColor(bi, i, j, colors);
			}
		}
	}

	private int[] getGradient(OversampleIteration[][] allIterations, OversampleIteration overIter, int x, int y) {
		int[] gradients = new int[overIter.getIterations().size()];
		int i = 0;
		for(Iteration iter : overIter.getIterations()) {
			gradients[i] = getGradient(allIterations, iter, x, y);
			i++;
		}
		if(gradients.length == 0) {
			return new int[] {-1};
		}
		return gradients;
	}

	private int getGradient(OversampleIteration[][] allIterations, Iteration iter, int x, int y) {
		int neighborD = getIter(allIterations, x, y-1);
		int neighborU = getIter(allIterations, x, y+1);
		int neighborR = getIter(allIterations, x-1, y);
		int neighborL = getIter(allIterations, x+1, y);
		neighborD = neighborD == -1 ? iter.iterations : neighborD;
		neighborU = neighborU == -1 ? iter.iterations : neighborU;
		neighborR = neighborR == -1 ? iter.iterations : neighborR;
		neighborL = neighborL == -1 ? iter.iterations : neighborL;
		int gradientX = Math.abs(iter.iterations - neighborR) + Math.abs(iter.iterations - neighborL);
		int gradientY = Math.abs(iter.iterations - neighborU) + Math.abs(iter.iterations - neighborD);
		return Math.min(gradientX, gradientY);
	}

	private int getIter(OversampleIteration[][] allIterations, int x, int y) {
		if(allIterations.length > x && x >= 0) {
			if(allIterations[x].length > y && y >= 0) {
				OversampleIteration overIter = allIterations[x][y];
				if(overIter.getIterations().size() > 0) {
					int iters = 0;
					for(Iteration iter : overIter.getIterations()) {
						iters += iter.iterations;
					}
					iters /= overIter.getIterations().size();
					return iters;
				}
			}
		}
		return -1;
	}

	@Override
	public void modifyIterData(HashMap<String, Double> iterData, Complex delta, Complex x, int curIter) {
		// TODO Auto-generated method stub
		
	}

}
