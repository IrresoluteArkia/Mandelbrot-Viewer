package com.irar.mbviewer;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.HashMap;

public class RawRenderer implements IMBRenderer {

	@Override
	public void drawIterations(OversampleIteration[][] iterations, BufferedImage bi, MBInfo info) {
		BufferedImage bi2 = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_ARGB);
		for(int i = 0; i < iterations.length; i++) {
			OversampleIteration[] i2 = iterations[i];
			for(int j = 0; j < i2.length; j++) {
				OversampleIteration iterSamples = i2[j];
				int[] sampleColors = getColors(iterSamples);
				if(sampleColors.length == 0) {
					sampleColors = new int[] {(255 << 24) + (255 << 16)};
				}
				setColor(bi2, i, j, sampleColors);
			}
		}
		Graphics g = bi.getGraphics();
		g.clearRect(0, 0, bi.getWidth(), bi.getHeight());
		g.drawImage(bi2, 0, 0, null);
	}

	private int[] getColors(OversampleIteration iterSamples) {
		int[] colors = new int[iterSamples.getIterations().size()];
		for(int i = 0; i < colors.length; i++) {
			Iteration iter = iterSamples.getIterations().get(i);
//			int a = (int) (255*(iter.partial%1));
			colors[i] = iter.iterations | (255 << 24);// | (a << 24);
		}
		return colors;
	}

	@Override
	public void modifyIterData(HashMap<String, Double> iterData, Complex delta, Complex x, int curIter) {}

}
