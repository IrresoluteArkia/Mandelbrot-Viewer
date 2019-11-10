package com.irar.mbviewer;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HistogramRenderer implements IMBRenderer{

	@Override
	public void drawIterations(OversampleIteration[][] allIterations, BufferedImage bi, MBInfo info) {
		List<OversampleIteration> sortedIterations = new ArrayList<>();
		List<OversampleIteration> inSet = new ArrayList<>();
		int total = 0;
		int x = 0;
		int y = 0;
		for(OversampleIteration[] iterTemp1 : allIterations) {
			for(OversampleIteration overIter : iterTemp1) {
				boolean set = overIter.getIterations().size() == 0;
				for(Iteration iter : overIter.getIterations()) {
					if(iter.iterations >= info.getIterations()) {
						set = true;
					}
					HashMap<String, Double> loc = new HashMap<>();
					loc.put("x", (double) x);
					loc.put("y", (double) y);
					iter.setExtraData(loc);
				}
				if(set) {
					inSet.add(overIter);
				}else {
					sortedIterations.add(overIter);
				}
				y++;
			}
			x++;
			y = 0;
		}
		sortedIterations.sort((iter1, iter2) -> {
			float avg1 = 0;
			float avg2 = 0;
			for(Iteration iter : iter1.getIterations()) {
				avg1 += iter.partial + iter.iterations;
			}
			for(Iteration iter : iter2.getIterations()) {
				avg2 += iter.partial + iter.iterations;
			}
			avg1 /= iter1.getIterations().size();
			avg2 /= iter2.getIterations().size();
			int result = 0;
			if(avg2 > avg1) {
				result = 1;
			}else if(avg1 > avg2) {
				result = -1;
			}
			return result;
		});
		int index = 0;
		BufferedImage bi2 = new BufferedImage(bi.getWidth(), bi.getHeight(), bi.getType());
		for(OversampleIteration iter : sortedIterations) {
			color((int) (double) iter.getIterations().get(0).getExtraData().get("x"), (int) (double) iter.getIterations().get(0).getExtraData().get("y"), 1 - ((double) (index + 1) / (sortedIterations.size() + 2)), info.getPalette(), bi2);
			index++;
		}
		for(OversampleIteration iter : inSet) {
			if(iter.getIterations().size() > 0) {
				color((int) (double) iter.getIterations().get(0).getExtraData().get("x"), (int) (double) iter.getIterations().get(0).getExtraData().get("y"), 0, info.getPalette(), bi2);
			}
		}
		Graphics g = bi.getGraphics();
		g.drawImage(bi2, 0, 0, null);
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
