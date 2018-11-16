package com.irar.mbviewer;

import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class MBHelper {

	static final double epsilon2 = 1.9721522630525295e-31;
	public static MBHelper helper;
	public static MBHelper lastHelper = null;
	public ZoomPoint bestPoint = null;
	public HashMap<ZoomPoint, Integer> rPoints = new HashMap<>();
	public int[][][] iterations;
	
	public MBHelper() {
		helper = this;
	}
	
	public void getSet(BufferedImage bi, Palette palette, double x, double y, SizedDouble zoom, int iterLimit, double pow) {
		int width = bi.getWidth();
		int height = bi.getHeight();
		iterations = new int[width][height][];
		fillIter(-1);
		SizedDouble scale = zoom.multiply(4.0 / height);
		for(int i = width / 2 - 1, acti = 0; acti < width + 1; i += ((acti % 2 == 1) ? acti : -acti), acti++) {
			if(acti == 1) {
				continue;
			}
			for(int j = 0; j < height; j++) {
				Complex c0 = new Complex(scale.multiply(i - width / 2).add(x).asDouble(), scale.multiply(j - height / 2).add(y).asDouble());
				Complex c = c0.addReal(0);
				int curIter = 0;
				double mag = c.magSqu();
				while(mag < 10000 && curIter < iterLimit) {
					c = c.pow(pow).add(c0);
					
					mag = c.magSqu();
					curIter++;
					if(!this.equals(helper)) {
						return;
					}
					if(c.equals(c0)) {
						curIter = iterLimit;
						break;
					}
				}
//				iterations[i][j] = curIter;
				int color = 0;
				if(curIter < iterLimit) {
					color = palette.paletteloop[curIter % palette.paletteloop.length];
				}
				bi.setRGB(i, j, color);
			}
		}
	}

	private void fillIter(int i) {
		for(int[][] iter : iterations) {
			for(int[] iter2 : iter) {
				Arrays.fill(iter2, -1);
			}
		}
	}

	public void getSet(BufferedImage bi, Palette palette, BigDecimal x, BigDecimal y, SizedDouble zoom, int iterLimit, double pow) {
		int zoomMag = (int) Math.abs(zoom.size) + 4;
		int width = bi.getWidth();
		int height = bi.getHeight();
		iterations = new int[width][height][];
		fillIter(-1);
		SizedDouble scale = zoom.multiply(4.0 / height);
		for(int i = width / 2 - 1, acti = 0; acti < width + 1; i += ((acti % 2 == 1) ? acti : -acti), acti++) {
			if(acti == 1) {
				continue;
			}
			for(int j = 0; j < height; j++) {
				Complex2 c0 = new Complex2(scale.multiply(i - width / 2).asBigDecimal(zoomMag).add(x), scale.multiply(j - height / 2).asBigDecimal(zoomMag).add(y), zoomMag);
				Complex2 c = c0.addReal(0);
				int curIter = 0;
				double mag = c.magSqu();
				while(mag < 10000 && curIter < iterLimit) {
					c = c.pow(pow).add(c0);
					
					mag = c.magSqu();
					curIter++;
					if(!this.equals(helper)) {
						return;
					}
					if(c.equals(c0)) {
						curIter = iterLimit;
						break;
					}
				}
//				iterations[i][j] = curIter;
				int color = 0;
				if(curIter < iterLimit) {
					color = palette.paletteloop[curIter % palette.paletteloop.length];
				}
				bi.setRGB(i, j, color);
			}
		}
	}
	
	@SuppressWarnings("unused")
	public void getSetP(BufferedImage bi, Palette palette, BigDecimal x, BigDecimal y, SizedDouble zoom, int iterLimit, int oversample, double blur, Complex power, boolean shufflePoints) {
		Viewer.info.minIter.setText("Rendering...");
		Viewer.info.maxIter.setText("Radius: " + zoom.toString(3));
		Viewer.info.avgIter.setText("X: " + x.toString());
		Viewer.info.tTime.setText("Y: " + y.toString());
		Viewer.window.validate();
		Viewer.window.pack();
		long startTime = System.currentTimeMillis();
		int zoomMag = (int) Math.abs(zoom.size) + 4;
		int width = bi.getWidth();
		int height = bi.getHeight();
		iterations = new int[width][height][oversample];
		fillIter(-1);
		List<ZoomPoint> points = getPoints(width, height, zoom, x, y, zoomMag, shufflePoints);
		if(helper != this) {
			lastHelper = this;
			return;
		}
		int bestIter = 0;
		int referencePoints = 0;
		List<ZoomPoint> pointsRedo = new ArrayList<>();
		List<Patch> patches = new ArrayList<>();
		Patch patch = null;
		while(points.size() > 0) {
			ZoomPoint r2 = null;
			referencePoints++;
			ReferencePoint rPoint = null;
			SeriesApprox approx = null;
			if(referencePoints == 1) {
				ZoomPoint r3 = getLastBestPoint(points.get(0).c.produce().x, points.get(points.size() - 1).c.produce().x, points.get(0).c.produce().y, points.get(points.size() - 1).c.produce().y);
				if(r3 == null) {
					final BigDecimal bx = x;
					final BigDecimal by = y;
					r3 = new ZoomPoint(0, 0, () -> {
						return new Complex2(bx, by, zoomMag);
					});
				}else {
				}
				Complex2 gen = r3.c.produce();
				rPoint = new ReferencePoint(gen.x, gen.y, iterLimit, zoomMag, power);
				if(helper != this) {
					lastHelper = this;
					return;
				}
				rPoints.put(r3, rPoint.XN.size());
				approx = new SeriesApprox(rPoint, zoomMag, points.get(new Random().nextInt(points.size())), power);
				if(helper != this) {
					lastHelper = this;
					return;
				}
			}else {
				if(patch != null) {
					Complex2 gen = patch.rPoint.c.produce();
					r2 = patch.rPoint;
					rPoint = new ReferencePoint(gen.x, gen.y, iterLimit, zoomMag, power);
					rPoints.put(patch.rPoint, rPoint.XN.size());
				}else {
					r2 = points.get(new Random().nextInt(points.size()));
					Complex2 gen = r2.c.produce();
					rPoint = new ReferencePoint(gen.x, gen.y, iterLimit, zoomMag, power);
					rPoints.put(r2, rPoint.XN.size());
				}
				if(helper != this) {
					lastHelper = this;
					return;
				}
				approx = new SeriesApprox(rPoint, zoomMag, points.size() > 0 ? (points.get(new Random().nextInt(points.size()))) : r2, power);
				if(helper != this) {
					lastHelper = this;
					return;
				}
			}
			Viewer.info.minIter.setText("Reference " + referencePoints + ": Skipping " + approx.skipped + " iterations...");
			for(ZoomPoint point : points) {
				if(helper != this) {
					lastHelper = this;
					return;
				}
				int iterations[];
				if(/*oversample > 1*/true) {
					if(point.equals(r2)) {
						iterations = new int[] {rPoint.XN.size()};
					}else {
						iterations = getIterOver(width, height, point, rPoint, approx, iterLimit, zoom, zoomMag, oversample, blur, power);
					}
					int lowest = iterLimit;
					for(int iteration : iterations) {
						if(iteration > bestIter) {
							bestPoint = point;
							bestIter = iteration;
						}
						if(iteration < lowest) {
							lowest = iteration;
						}
					}
					point.itersDone = lowest;
				}/*else {
					if(zoom.size < -320) {
						iterations = new int[] {getIterBig(point, rPoint, approx, iterLimit, zoomMag, blur)};
					}else {
						iterations = new int[] {getIter(point, rPoint, approx, iterLimit, zoomMag, blur)};
					}
				}*/
				this.iterations[point.x][point.y] = iterations;
				int[] color = new int[oversample];
				for(int i = 0; i < iterations.length; i++) {
					int iterat = iterations[i];
					if(iterat < iterLimit) {
						color[i] = palette.paletteloop[iterat % palette.paletteloop.length];
					}
				}
				if(point.redo) {
					pointsRedo.add(point);
					point.redo = false;
//					setColor(bi, point.x, point.y, new int[] {255 << 16});
				}else {
				}
				setColor(bi, point.x, point.y, color);
			}
			points.clear();
			patches.remove(patch);
			if(patches.isEmpty()) {
				while(pointsRedo.size() > 0) {
					Patch patch1 = new Patch(pointsRedo);
					if(patch1.rPoint != null) {
						patches.add(patch1);
					}else {
						break;
					}
				}
			}
			if(!patches.isEmpty()) {
				patch = patches.get(0);
				points = patches.get(0).points;
			}
//			points.remove(r2);
		}
		String info = "Drew Fractal in " + (System.currentTimeMillis() - startTime) + "ms";
		Viewer.info.minIter.setText(info);
//		Viewer.window.pack();
		Viewer.window.validate();
		Viewer.window.pack();
		System.out.println(info);
		lastHelper = this;
	}
	
	private ZoomPoint getLastBestPoint(BigDecimal x1, BigDecimal x2, BigDecimal y1, BigDecimal y2) {
		if(lastHelper != null && !lastHelper.rPoints.isEmpty()) {
			ZoomPoint best = null;
			int bestIter = 0;
			BigDecimal difX = x2.subtract(x1).multiply(BigDecimal.TEN);
			BigDecimal difY = y2.subtract(y1).multiply(BigDecimal.TEN);
			BigDecimal x1d = x1.subtract(difX);
			BigDecimal y1d = y1.subtract(difY);
			BigDecimal x2d = x2.add(difX);
			BigDecimal y2d = y2.add(difY);
			for(ZoomPoint point : lastHelper.rPoints.keySet()) {
				int iter = lastHelper.rPoints.get(point);
				Complex2 gen = point.c.produce();
				if(gen.x.compareTo(x1d) > 0 && gen.x.compareTo(x2d) < 0 && gen.y.compareTo(y1d) > 0 && gen.y.compareTo(y2d) < 0 && iter > bestIter) {
					best = point;
					bestIter = iter;
				}
			}
			if(best == null && lastHelper.bestPoint != null) {
				best = lastHelper.bestPoint;
			}
			return best;
		}
		return null;
	}

	private void setColor(BufferedImage bi, int x, int y, int[] color) {
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

	private int[] getIterOver(int width, int height, ZoomPoint point, ReferencePoint rPoint, SeriesApprox approx,
			int iterLimit, SizedDouble zoom, int zoomMag, int oversampleAmount, double blur, Complex power) {
		int[] over = new int[oversampleAmount];
		for(int i = 0; i < oversampleAmount; i++) {
			SizedDouble maxDevAmountX = zoom.divide(width);
			SizedDouble maxDevAmountY = zoom.divide(height);
			Complex2 genPoint = point.c.produce();
			ZoomPoint pointI = new ZoomPoint(point.x, point.y, () -> {
				BigDecimal x = genPoint.x.add(maxDevAmountX.multiply(new Random().nextDouble()).asBigDecimal(zoomMag));
				BigDecimal y = genPoint.y.add(maxDevAmountY.multiply(new Random().nextDouble()).asBigDecimal(zoomMag));
				return new Complex2(x, y, zoomMag);
			});
			if(zoom.size < -320) {
				over[i] = getIterBig(pointI, rPoint, approx, iterLimit, zoomMag, blur);
			}else {
				over[i] = getIter(pointI, rPoint, approx, iterLimit, zoomMag, blur, power);
			}
			if(pointI.redo) {
				point.redo = true;
				break;
			}
		}
		return over;
	}

	private int getIterBig(ZoomPoint point, ReferencePoint rPoint, SeriesApprox approx, int maxIter, int pre, double blur) {
		SizedDouble br = new SizedDouble(256);
		Complex2 genPoint = point.c.produce();
		Complex2 delta02 = genPoint.subtract(rPoint.XN.get(0));
		Complex3 delta0 = new Complex3(delta02);
		Complex3 deltaN;
		int curIter = approx.skipped;
		if(curIter == 0) {
			deltaN = delta0;
		}else {
			Complex3 deltaN2 = delta0.multiply(approx.AN.get(curIter)).add(delta0.pow(2).multiply(approx.BN.get(curIter))).add(delta0.pow(3).multiply(approx.CN.get(curIter)));
			deltaN = deltaN2;
		}
		SizedDouble squ = new SizedDouble(0);
		do{
			if(curIter < rPoint.XNc.size()) {
				deltaN = deltaN.multiply(rPoint.XN2.get(curIter).add(deltaN)).add(delta0);
				if(blur != 0) {
					deltaN = deltaN.multiply((new Random().nextDouble() * blur) - (blur / 2) + 1);
				}
			}else if(curIter < maxIter - 2) {
				point.redo = true;
				break;
			}
			curIter++;
			if(curIter < rPoint.XNc.size()) {
				squ = (rPoint.XNc.get(curIter).add(deltaN)).mag();
				if(rPoint.XNM3.get(curIter).compareTo(squ) > 0) {
					point.redo = true;
					break;
				}
//				squ = (rPoint.XNc.get(curIter).add(deltaN)).magSqu();
			}
		}while(curIter < maxIter && squ.compareTo(br) < 0);
		return curIter;
	}

	private int getIter(ZoomPoint point, ReferencePoint rPoint, SeriesApprox approx, int maxIter, int pre, double blur, Complex power) {
		Complex2 genPoint = point.c.produce();
		Complex2 delta02 = genPoint.subtract(rPoint.XN.get(0));
		Complex delta0 = new Complex(delta02);
		Complex deltaN;
		int curIter = approx.skipped;
		if(curIter == 0) {
			deltaN = delta0;
		}else {
			Complex3 d03 = new Complex3(delta0);
			Complex3 deltaN2 = d03.multiply(approx.AN.get(curIter)).add(d03.pow(2).multiply(approx.BN.get(curIter))).add(d03.pow(3).multiply(approx.CN.get(curIter)));
			deltaN = new Complex(deltaN2);
		}
		double squ = 0;
		do{
			if(curIter < rPoint.XNcSmall.size()) {
/*				if(curIter == 0) {
					deltaN = new Complex(delta02.add(rPoint.XN.get(curIter)).pow(power).subtract(rPoint.XPN.get(curIter)).add(delta02));
				}*/
				deltaN = deltaN.multiply(rPoint.XN2Small.get(curIter).add(deltaN)).add(delta0);
//				deltaN = deltaN.add(rPoint.XNcSmall.get(curIter)).pow(power).subtract(rPoint.XPNSmall.get(curIter)).add(delta0);
//				deltaN = deltaN.multiply(rPoint.XNcSmall.get(curIter)).pow(power).add(delta0);
/*				if(blur != 0) {
					deltaN = deltaN.multiply((new Random().nextDouble() * blur) - (blur / 2) + 1);
				}*/
			}else if(curIter < maxIter - 2) {
				point.redo = true;
				break;
			}
			curIter++;
			if(curIter < rPoint.XNcSmall.size()) {
				squ = (rPoint.XNcSmall.get(curIter).add(deltaN)).mag();
				if(rPoint.XNM3Small.get(curIter) > squ) {
					point.redo = true;
					break;
				}
			}
		}while(curIter < maxIter && squ < 256);
/*		do{
			double deltaNr = deltaN.x;
			double deltaNi = deltaN.x;
			if(curIter < rPoint.XNcSmall.size()) {
				double inter1r = deltaNr + rPoint.XN2Small.get(curIter).x;
				double inter1i = deltaNi + rPoint.XN2Small.get(curIter).y;
				double inter2r = deltaNr * inter1r - deltaNi * inter1i + delta0.x;
				double inter2i = deltaNr * inter1i + deltaNi * inter1r + delta0.y;
				deltaNr = inter2r;
				deltaNi = inter2i;
//				deltaN = deltaN.multiply(rPoint.XN2Small.get(curIter).add(deltaN)).add(delta0);
			}else if(curIter < maxIter - 2) {
				point.redo = true;
				break;
			}
			curIter++;
			if(curIter < rPoint.XNcSmall.size()) {
				double r1 = rPoint.XNcSmall.get(curIter).x + deltaNr;
				double i1 = rPoint.XNcSmall.get(curIter).y + deltaNi;
				squ = r1 * r1 + i1 * i1;
			}
		}while(curIter < maxIter && squ < 256 * 256);*/
		return curIter;
	}

	private List<ZoomPoint> getPoints(int width, int height, SizedDouble zoom, BigDecimal x, BigDecimal y, int pre, boolean shuffle) {
		int zoomMag = (int) Math.abs(zoom.size) + 4;
		List<ZoomPoint> points = new ArrayList<>();
		SizedDouble scale = zoom.multiply(4.0 / height);
		for(int i = width / 2 - 1, acti = 0; acti < width + 1; i += ((acti % 2 == 1) ? acti : -acti), acti++) {
			if(acti == 1) {
				continue;
			}
			for(int j = 0; j < height; j++) {
				final int iF = i;
				final int jF = j;
				points.add(new ZoomPoint(i, j, () -> {
					return new Complex2(scale.multiply(iF - width / 2).asBigDecimal(zoomMag).add(x.setScale(pre, BigDecimal.ROUND_DOWN)).setScale(pre, BigDecimal.ROUND_DOWN), scale.multiply(jF - height / 2).asBigDecimal(zoomMag).add(y.setScale(pre, BigDecimal.ROUND_DOWN)).setScale(pre, BigDecimal.ROUND_DOWN), zoomMag);
				}));
				if(helper != this) {
					return points;
				}
			}
		}
/*		if(shuffle) {
			Collections.shuffle(points);
		}*/
		return points;
	}

	public void recolor(BufferedImage bi, Palette palette, int maxIter) {
		for(int i = 0; i < iterations.length; i++) {
			for(int j = 0; j < iterations.length; j++) {
				int[] color = new int[iterations[i][j].length];
				for(int k = 0; k < iterations[i][j].length; k++) {
					int iterat = iterations[i][j][k];
					if(iterat >= 0) {
						if(iterat < maxIter) {
							color[k] = palette.paletteloop[iterat % palette.paletteloop.length];
						}
					}
				}
				setColor(bi, i, j, color);
			}
		}
	}
	
	public class ReferencePoint{

		public BigDecimal x;
		public BigDecimal y;
		public List<Complex2> XN = new ArrayList<>();
		public List<SizedDouble> XNM3 = new ArrayList<>();
		public List<Double> XNM3Small = new ArrayList<>();
		public List<Complex3> XNc = new ArrayList<>();
		public List<Complex3> XN2 = new ArrayList<>();
		public List<Complex> XNcSmall = new ArrayList<>();
		public List<Complex> XN2Small = new ArrayList<>();
		public List<Complex> XPNSmall = new ArrayList<>();
		public List<Complex2> XPN = new ArrayList<>();


		public ReferencePoint(BigDecimal x, BigDecimal y, int maxIter, int pre, Complex power) {
			maxIter += 3;
			Complex2 c0 = new Complex2(x, y, pre);
			this.x = x;
			this.y = y;
			Complex2 c = new Complex2(x, y, pre);
			for(int i = 0; i < maxIter; i++) {
				Complex2 cx2 = c.multiply(2);
				XN.add(c);
				XNM3.add(new Complex3(c).mag().multiply(0.001));
				XNM3Small.add(new Complex(c).mag() * 0.001);
				XNc.add(new Complex3(c));
				XN2.add(new Complex3(cx2));
				XNcSmall.add(new Complex(c));
				XN2Small.add(new Complex(cx2));
				c = c.pow(power);
				XPN.add(c);
				XPNSmall.add(new Complex(c));
				c = c.add(c0);
				if(helper != MBHelper.this) {
					lastHelper = MBHelper.this;
					return;
				}
				if(c.magSqu() > 10000) {
					break;
				}
			}
			
		}
		
	}
	
	public class SeriesApprox{

		public List<Complex3> AN = new ArrayList<>();
		public List<Complex3> BN = new ArrayList<>();
		public List<Complex3> CN = new ArrayList<>();
		public int skipped = 0;

		public SeriesApprox(ReferencePoint rPoint, int pre, ZoomPoint testPoint, Complex power) {
			double tol = Math.pow(2, -64);
			Complex2 genTestPoint = testPoint.c.produce();
			Complex3 delta0 = new Complex3(genTestPoint.subtract(rPoint.XN.get(0)));
			Complex3 deltaPow2 = delta0.pow(2);
			Complex3 deltaPow3 = delta0.pow(3);
			AN.add(new Complex3(1, 0));
			BN.add(new Complex3(0, 0));
			CN.add(new Complex3(0, 0));
			if(power.x != 2 || power.y != 0) {
				return;
			}
			for(int i = 1; i < rPoint.XN.size(); i++) {
				AN.add(AN.get(i - 1).multiply(new Complex3(rPoint.XN.get(i - 1)).multiply(2)).addReal(1));
				BN.add(BN.get(i - 1).multiply(new Complex3(rPoint.XN.get(i - 1)).multiply(2)).add(AN.get(i - 1).pow(2)));
				CN.add(CN.get(i - 1).multiply(new Complex3(rPoint.XN.get(i - 1)).multiply(2)).add(AN.get(i - 1).multiply(BN.get(i - 1).multiply(2))));
				if (((BN.get(i).multiply(deltaPow2)).magSqu().multiply(tol)).compareTo((CN.get(i).multiply(deltaPow3)).magSqu()) < 0) {
					if (i <= 3) {
						skipped = 0;
						return;
					}
					else {
						skipped = (i - 3);
						return;
					}
				}
			}
			skipped = 0;
		}
		
	}
	
	public int getPeriod(Complex2 c1, Complex2 c2, Complex2 c3, Complex2 c4) {
		int period = 1;
		Complex2[] all0 = new Complex2[] {c1, c2, c3, c4};
		Complex2[] allN = new Complex2[] {c1, c2, c3, c4};
		while(!containsOrigin(allN[0], allN[1], allN[2], allN[3])) {
			if(helper != this) {
				return -1;
			}
			Viewer.info.minIter.setText("Counting period: " + period);
			Viewer.window.validate();
			Viewer.window.pack();
			for(int i = 0; i < all0.length; i++) {
				allN[i] = allN[i].pow(2).add(all0[i]);
			}
			period++;
		}
		return period;
	}

	public double logn(int log, int n) {
		return Math.log(log) / Math.log(n);
	}

	public int getPeriod(BigDecimal locX, BigDecimal locY, SizedDouble zoom, int width, int height, int pre) {
		SizedDouble scale = zoom.multiply(4.0 / height);
//		SizedDouble scale = new SizedDouble(4).divide(zoom.multiply(height));
		int zoomMag = -zoom.size + 4;
		Complex2 c1 = new Complex2((scale.multiply(-width / 2)).asBigDecimal(zoomMag).add(locX), (scale.multiply( height / 2)).asBigDecimal(zoomMag).add(locY), zoomMag);
		Complex2 c2 = new Complex2((scale.multiply(-width / 2)).asBigDecimal(zoomMag).add(locX), (scale.multiply(-height / 2)).asBigDecimal(zoomMag).add(locY), zoomMag);
		Complex2 c3 = new Complex2((scale.multiply( width / 2)).asBigDecimal(zoomMag).add(locX), (scale.multiply(-height / 2)).asBigDecimal(zoomMag).add(locY), zoomMag);
		Complex2 c4 = new Complex2((scale.multiply( width / 2)).asBigDecimal(zoomMag).add(locX), (scale.multiply( height / 2)).asBigDecimal(zoomMag).add(locY), zoomMag);
		return getPeriod(c1, c2, c3, c4);
	}
	
	public boolean containsOrigin(Complex2 c1, Complex2 c2, Complex2 c3, Complex2 c4) {
		return (getInt(crossesPositiveRealAxis(c1, c2)) +
				getInt(crossesPositiveRealAxis(c2, c3)) +
				getInt(crossesPositiveRealAxis(c3, c4)) +
				getInt(crossesPositiveRealAxis(c4, c1))) % 2 == 1;
	}

	private int getInt(boolean tf) {
		return tf ? 1 : 0;
	}

	private boolean crossesPositiveRealAxis(Complex2 c1, Complex2 c2) {
		if(sgn(c1.y) != sgn(c2.y)) {
			BigDecimal cr = c1.cross(c2);
			Complex2 dif = c2.subtract(c1);
			int s = sgn(cr);
			int t = sgn(dif.y);
			return s == t;
		}
		return false;
	}

	private int sgn(BigDecimal cr) {
		return cr.compareTo(BigDecimal.ZERO);
	}

	public ZoomLoc findMini(BigDecimal locX, BigDecimal locY, SizedDouble zoom, int width, int height, int zoomMod) {
		int zoomMag = (-zoom.size) * zoomMod + 4;
		int period = this.getPeriod(locX, locY, zoom, width, height, zoomMag);
		Viewer.info.minIter.setText("Period found: " + period);
		Complex2 miniLoc = getMini(new Complex2(locX, locY, zoomMag), period);
		SizedDouble newZoom = getSize(miniLoc, period).magSD();
		return new ZoomLoc(miniLoc, newZoom);
	}

	private Complex2 getSize(Complex2 miniLoc, int period) {
		Complex2 one = new Complex2(new BigDecimal(1), new BigDecimal(0), miniLoc.getScale());
		Complex2 l = one;
		Complex2 b = one.setScale(miniLoc.getScale());
		Complex2 z = Complex2.ZERO.setScale(miniLoc.getScale());
		for (int i = 1; i < period; ++i) {
			z = z.multiply(z).add(miniLoc);
			l = z.multiply(2).multiply(l);
			b = b.add(one.divide(l));
		}
		return one.divide((b.multiply(l).multiply(l)));
	}

	private Complex2 getMini(Complex2 complex2, int period) {
		Complex2 guess = complex2;
		int y = 1;
		SizedDouble comp = new SizedDouble(1, -complex2.getScale());
		while(true) {
			Viewer.info.minIter.setText("Period: " + period + "Iter: " + y);
			y++;
			if(!helper.equals(this)) {
				return null;
			}
			Complex2 dc = Complex2.ZERO.setScale(complex2.getScale());
			Complex2 z = Complex2.ZERO.setScale((int) (complex2.getScale()));
			for(int i = 0; i < period; i++) {
				dc = z.multiply(dc).multiply(2).addReal(1);
				z = z.multiply(z).add(guess);
			}
			if(dc.magSD().compareTo(comp) <= 0/* * complex2.getScale()*/) {
				return guess;
			}
			Complex2 newGuess = guess.subtract(z.divide(dc));
			Complex2 dif = newGuess.subtract(guess);
			if(dif.magSD().compareTo(comp) <= 0/* * complex2.getScale()*/) {
				return newGuess;
			}
			guess = newGuess;
		}
	}

}
