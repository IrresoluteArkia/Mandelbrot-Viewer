package com.irar.mbviewer;

import java.awt.image.BufferedImage;
import java.util.HashMap;

public class CloseOrbitRenderer implements IMBRenderer {

	@Override
	public void drawIterations(Iteration[][][] iterations, BufferedImage bi, MBInfo info) {
		for(int i = 0; i < iterations.length; i++) {
			for(int j = 0; j < iterations[i].length; j++) {
				drawIteration(iterations[i][j], bi, info.getIterations(), info.getPalette(), i, j);
			}
		}
	}

	private void drawIteration(Iteration[] iterations, BufferedImage bi, int iterations2, Palette p, int x, int y) {
		if(iterations == null || iterations.length == 0 || !iterations[0].getExtraData().containsKey("orbit_dist") || !iterations[0].getExtraData().containsKey("closest_iter")) {
			bi.setRGB(x, y, 255 * 256 * 256);
		}
		else if(iterations[0].iterations == iterations2){
			bi.setRGB(x, y, 0);
		}
		else{
			int[] colors = new int[iterations.length];
			for(int i = 0; i < iterations.length; i++) {
//				double dist = Math.log(iterations[i].getExtraData().get("orbit_dist"));
//				while(dist < 0) {
//					dist++;
//				}
//				int num = (int) (dist * p.paletteloop.length);
//				colors[i] = p.paletteloop[num % p.paletteloop.length];
				colors[i] = p.paletteloop[(int) (double) (iterations[i].getExtraData().get("closest_iter")) % p.paletteloop.length];
				this.setColor(bi, x, y, colors);
			}
		}
	}

	@Override
	public void modifyIterData(HashMap<String, Double> iterData, Complex delta, Complex x, int curIter) {
		Complex trap = new Complex(0, 0);
		double dist = delta.add(x).subtract(trap).mag();
//		double dist = Math.min(Math.abs(delta.add(x).x), Math.abs(delta.add(x).y));
		if(iterData.containsKey("orbit_dist")) {
			double oldDist = iterData.get("orbit_dist");
			if(dist < oldDist) {
				iterData.put("orbit_dist", dist);
				iterData.put("closest_iter", (double) curIter);
			}
		}else {
			iterData.put("orbit_dist", dist);
			iterData.put("closest_iter", (double) curIter);
		}
	}

}
