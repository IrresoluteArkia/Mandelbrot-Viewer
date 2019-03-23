package com.irar.mbviewer;

import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import com.irar.mbviewer.util.RandomUtil;

public class MBHelper {
	
	Iteration[][][] iterations = null;
	boolean interrupted = false;
	private IMBRenderer renderer = null;
	
	public void getSet(BufferedImage bi, MBInfo info, IProgressMonitorFactory<?> progressMonitorFactory, IMBRenderer renderer) {
		this.renderer = renderer;
		int width = bi.getWidth();
		int height = bi.getHeight();
		int oversample = info.getOversample();
		Iteration[][][] allIterations = null;
		try {
			allIterations = iterate(info, width, height, oversample, progressMonitorFactory);
		}catch(Exception e) {
			interrupted = true;
			progressMonitorFactory.deleteAllMonitors();
			return;
		}
		try {
			renderer.drawIterations(allIterations, bi, info);
		}catch(Exception e) {
			interrupted = true;
			progressMonitorFactory.deleteAllMonitors();
			e.printStackTrace();
			return;
		}
		iterations = allIterations;
	}
	
	private Iteration[][][] iterate(MBInfo info, int width, int height, int samples, IProgressMonitorFactory<?> factory) throws Exception {
		IProgressMonitor monitor = factory.createNewProgressMonitor();
		Iteration[][][] iterations = new Iteration[width][height][];
		ReferencePoint rPoint;
		List<ZoomPoint> points = getZoomPoints(info, width, height);
		SeriesApprox approx = null;
		List<ZoomPoint> repeatPoints = new ArrayList<>();
		for(ZoomPoint point : points) {
			point.redo = true;
		}
		do {
			if(approx != null) {
				info.setIterations(approx.skipped * 20);
			}
			rPoint = getStartingPoint(info, factory);
			approx = getApproximations(rPoint, points, info, width, height, factory);
		}while(info.getIterations() < approx.skipped * 20);
		int current = 0;
		int done = 0;
		int end = points.size();
		for(ZoomPoint point : points) {
			if(point.redo) {
				point.redo = false;
			}
			iterations[point.x][point.y] = iterate(info, point, rPoint, approx, getZoomMagnitude(info), samples, width, height);
			if(point.redo) {
				repeatPoints.add(point);
			}else {
				done++;
			}
			current++;
			if(isBadReference(current, points.size(), repeatPoints.size())) {
				repeatPoints = points;
				break;
			}
			monitor.setProgress((float) done / end);
		}
		if(repeatPoints.size() > 0) {
			rPoint = null;
			approx = null;
			points = null;
			repeatIteration(info, repeatPoints, iterations, samples, factory, monitor, 1, width, height);
		}
		monitor.deleteMonitor();
		return iterations;
	}

	private boolean isBadReference(int currentPointNum, int totalPointNum, int redoNum) {
//		float doneMoreThan = 0.01F;
		int doneMoreThan = 100;
		float criticalRedoRatio = 0.9F;
		if(currentPointNum > doneMoreThan) {
			if((float) redoNum / currentPointNum > criticalRedoRatio) {
//				System.out.println("Bad reference, recalculating...");
				return true;
			}
		}
		return false;
	}

	private void repeatIteration(MBInfo info, List<ZoomPoint> points, Iteration[][][] iterations, int samples,
			IProgressMonitorFactory<?> factory, IProgressMonitor progressMonitor, int repeatNum, int width, int height) throws Exception {
		int end = iterations.length * iterations[0].length;
		int current = end - points.size();
		int done = current;
		int currentR = 0;
		if (repeatNum >= 100/* || (remaining < (end / 10000) && repeatNum > 10) */) {
			return;
		}
		ReferencePoint rPoint = getRandomReference(points, info, factory);
		SeriesApprox approx = getApproximations(rPoint, points, info, width, height, factory);
		List<ZoomPoint> repeatPoints = new ArrayList<>();
		if(info.getIterations() < approx.skipped * 10) {
			info.setIterations(approx.skipped * 10);
			repeatPoints = points;
		}else {
			for(ZoomPoint point : points) {
				if(point.redo) {
					point.redo = false;
					iterations[point.x][point.y] = iterate(info, point, rPoint, approx, getZoomMagnitude(info), samples, width, height);
				}
				if(point.redo) {
					repeatPoints.add(point);
				}else {
					done++;
				}
				current++;
				currentR++;
				if(isBadReference(currentR, points.size(), repeatPoints.size())) {
					repeatPoints = points;
					break;
				}
				progressMonitor.setProgress((float) done / end);
			}
		}
		if(repeatPoints.size() > 0) {
			rPoint = null;
			approx = null;
			points = null;
			repeatIteration(info, repeatPoints, iterations, samples, factory, progressMonitor, repeatNum + 1, width, height);
		}
	}

	private ReferencePoint getRandomReference(List<ZoomPoint> points, MBInfo info, IProgressMonitorFactory<?> factory) throws Exception {
		ZoomPoint point = RandomUtil.pickOne(points);
		points.remove(point);
		Complex2 chosenLoc = point.c.produce();
		return new ReferencePoint(chosenLoc.x, chosenLoc.y, info.getIterations(), getZoomMagnitude(info), info.getPower(), this, factory.createNewProgressMonitor());
	}

	private Iteration[] iterate(MBInfo info, ZoomPoint point, ReferencePoint rPoint, SeriesApprox approx, int zoomMagnitude,
			int samples, int width, int height) throws Exception {
		ZoomPoint pointI = deviateRandomly(point, width, height, info.getZoom(), zoomMagnitude);
		Iteration[] iterations = new Iteration[samples];
		for(int i = 0; i < samples; i++) {
			iterations[i] = iterate(info, pointI, rPoint, approx, zoomMagnitude);
			if(pointI.redo) {
				point.redo = true;
				continue;
			}else if(iterations[i].iterations == info.getIterations()) {
				return new Iteration[] {iterations[i]};
			}
		}
		return iterations;
	}

	private ZoomPoint deviateRandomly(ZoomPoint point, int width, int height, SizedDouble zoom, int zoomMag) {
		SizedDouble maxDevAmountX = zoom.divide(width);
		SizedDouble maxDevAmountY = zoom.divide(height);
		Complex2 genPoint = point.c.produce();
		ZoomPoint pointI = new ZoomPoint(point.x, point.y, () -> {
			BigDecimal x = genPoint.x.add(maxDevAmountX.multiply(new Random().nextDouble()).asBigDecimal(zoomMag));
			BigDecimal y = genPoint.y.add(maxDevAmountY.multiply(new Random().nextDouble()).asBigDecimal(zoomMag));
			return new Complex2(x, y, zoomMag);
		});
		return pointI;
	}

	private Iteration iterate(MBInfo info, ZoomPoint point, ReferencePoint rPoint, SeriesApprox approx, int zoomMagnitude) throws Exception {
		if(info.getZoom().size < -320) {
			return iterateBig(info, point, rPoint, approx, zoomMagnitude);
		}else {
			return iterateSmall(info, point, rPoint, approx, zoomMagnitude);
		}
	}

	private Iteration iterateSmall(MBInfo info, ZoomPoint point, ReferencePoint rPoint, SeriesApprox approx,
			int zoomMagnitude) throws Exception {
		int maxIter = info.getIterations();
		Complex2 genPoint = point.c.produce();
		Complex2 delta02 = genPoint.subtract(rPoint.XN.get(0));
		Complex delta0 = new Complex(delta02);
		Complex deltaN;
		int curIter = approx.skipped;
		if(curIter == 0) {
			deltaN = delta0;
		}else {
			Complex3 d03 = new Complex3(delta02);
			Complex3 deltaN2 = d03.multiply(approx.AN.get(curIter)).add(d03.pow(2).multiply(approx.BN.get(curIter))).add(d03.pow(3).multiply(approx.CN.get(curIter)));
			deltaN = new Complex(deltaN2);
		}
		double squ = 0;
		HashMap<String, Double> iterData = new HashMap<>();
		do{
			if(curIter < rPoint.XNcSmall.size()) {
				Complex deltaNtemp = deltaN.multiply(rPoint.XN2Small.get(curIter).add(deltaN)).add(delta0);
				deltaN = deltaNtemp;
				if(curIter + 1 < rPoint.XNcSmall.size()) {
					this.renderer.modifyIterData(iterData, deltaN, rPoint.XNcSmall.get(curIter + 1), curIter);
				}
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
			checkShouldKeepRunning();
		}while(curIter < maxIter && squ < 256);
		float partial = 0;
		if ( curIter < maxIter ) {
		    double log_zn = Math.log(squ) / 2;
		    double nu = Math.log( log_zn / Math.log(2) ) / Math.log(2);
		    partial = (float) (1 - nu);
		}
		Iteration iter = new Iteration(curIter, partial);
		iter.setExtraData(iterData);
		return iter;
	}

	public void checkShouldKeepRunning() throws Exception {
		if(!Viewer.helper.equals(this)) {
			throw new Exception();
		}
	}

	private Iteration iterateBig(MBInfo info, ZoomPoint point, ReferencePoint rPoint, SeriesApprox approx,
			int zoomMagnitude) {
		int maxIter = info.getIterations();
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
			}
		}while(curIter < maxIter && squ.compareTo(br) < 0);
		float partial = 0;
		if ( curIter < maxIter ) {
		    double log_zn = deltaN.x.multiply(deltaN.x).add(deltaN.y.multiply(deltaN.y)).log() / 2;
		    double nu = Math.log( log_zn / Math.log(2) ) / Math.log(2);
		    partial = (float) (1 - nu);
		}
		return new Iteration(curIter, partial);
	}

	private SeriesApprox getApproximations(ReferencePoint rPoint, List<ZoomPoint> points, MBInfo info, int width, int height, IProgressMonitorFactory<?> factory) throws Exception {
		ZoomPoint testPoint = new ZoomPoint(0, 0, () -> {
			int zoomMag = this.getZoomMagnitude(info);
			BigDecimal x = info.getX().add(info.getZoom().divide(width / 2).asBigDecimal(zoomMag));
			BigDecimal y = info.getY().add(info.getZoom().divide(height / 2).asBigDecimal(zoomMag));
			return new Complex2(x, y, zoomMag);
		});
		return new SeriesApprox(rPoint, testPoint, info.getPower(), this, factory.createNewProgressMonitor());
	}

	private List<ZoomPoint> getZoomPoints(MBInfo info, int width, int height) {
		int zoomMag = this.getZoomMagnitude(info);
		List<ZoomPoint> points = new ArrayList<>();
		SizedDouble scale = info.getZoom().multiply(4.0 / height);
		for(int i = width / 2 - 1, acti = 0; acti < width + 1; i += ((acti % 2 == 1) ? acti : -acti), acti++) {
			if(acti == 1) {
				continue;
			}
			for(int j = 0; j < height; j++) {
				final int iF = i;
				final int jF = j;
				points.add(new ZoomPoint(i, j, () -> {
					return new Complex2(scale.multiply(iF - width / 2)
							.asBigDecimal(zoomMag)
							.add(info.getX()
									.setScale(zoomMag, BigDecimal.ROUND_DOWN))
							.setScale(zoomMag, BigDecimal.ROUND_DOWN), 
							scale.multiply(jF - height / 2)
							.asBigDecimal(zoomMag)
							.add(info.getY()
									.setScale(zoomMag, BigDecimal.ROUND_DOWN))
							.setScale(zoomMag, BigDecimal.ROUND_DOWN), 
							zoomMag);
				}));
			}
		}
		Collections.shuffle(points);
		return points;
	}

	private ReferencePoint getStartingPoint(MBInfo info, IProgressMonitorFactory<?> factory) throws Exception {
		return new ReferencePoint(info.getX(), info.getY(), info.getIterations(), getZoomMagnitude(info), info.getPower(), this, factory.createNewProgressMonitor());
	}
	
	private int getZoomMagnitude(MBInfo info){
		return Math.abs(info.getZoom().size) + 4;
	}

	public void recolor(BufferedImage bi, MBInfo info, ProgressMonitorFactory factory) {
		while(iterations == null && !interrupted) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if(!interrupted) {
			renderer.drawIterations(iterations, bi, info);
		}
	}
	
	public ZoomLoc findMini(MBInfo info, int width, int height, double zoomMod) {
		interrupted = true;
		BigDecimal x = info.getX();
		BigDecimal y = info.getY();
		SizedDouble zoom = info.getZoom();
		int zoomMag = (int) (Math.abs(zoom.size) * zoomMod + 4);
		int period = this.getPeriod(x, y, zoom, width, height, zoomMag);
		Viewer.renderInfo.minIter.setText("Period found: " + period);
		Complex2 miniLoc = getMini(new Complex2(x, y, zoomMag), period);
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
			Viewer.renderInfo.minIter.setText("Period: " + period + "Iter: " + y);
			y++;
			try {
				this.checkShouldKeepRunning();
			} catch (Exception e) {
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

	public int getPeriod(Complex2 c1, Complex2 c2, Complex2 c3, Complex2 c4) {
		int period = 1;
		Complex2[] all0 = new Complex2[] {c1, c2, c3, c4};
		Complex2[] allN = new Complex2[] {c1, c2, c3, c4};
		while(!containsOrigin(allN[0], allN[1], allN[2], allN[3])) {
			try {
				this.checkShouldKeepRunning();
			} catch (Exception e) {
				return -1;
			}
			Viewer.renderInfo.minIter.setText("Counting period: " + period);
			Viewer.window.validate();
			Viewer.window.pack();
			for(int i = 0; i < all0.length; i++) {
				allN[i] = allN[i].pow(2).add(all0[i]);
			}
			period++;
		}
		return period;
	}

	private boolean containsOrigin(Complex2 c1, Complex2 c2, Complex2 c3, Complex2 c4) {
		return (getInt(crossesPositiveRealAxis(c1, c2)) +
				getInt(crossesPositiveRealAxis(c2, c3)) +
				getInt(crossesPositiveRealAxis(c3, c4)) +
				getInt(crossesPositiveRealAxis(c4, c1))) % 2 == 1;
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
	
	private int getInt(boolean tf) {
		return tf ? 1 : 0;
	}
	
	private int sgn(BigDecimal cr) {
		return cr.compareTo(BigDecimal.ZERO);
	}

	public int getPeriod(BigDecimal locX, BigDecimal locY, SizedDouble zoom, int width, int height, int pre) {
		SizedDouble scale = zoom.multiply(4.0 / height);
//		SizedDouble scale = new SizedDouble(4).divide(zoom.multiply(height));
		int zoomMag = Math.abs(zoom.size) + 4;
		Complex2 c1 = new Complex2((scale.multiply(-width / 2)).asBigDecimal(zoomMag).add(locX), (scale.multiply( height / 2)).asBigDecimal(zoomMag).add(locY), zoomMag);
		Complex2 c2 = new Complex2((scale.multiply(-width / 2)).asBigDecimal(zoomMag).add(locX), (scale.multiply(-height / 2)).asBigDecimal(zoomMag).add(locY), zoomMag);
		Complex2 c3 = new Complex2((scale.multiply( width / 2)).asBigDecimal(zoomMag).add(locX), (scale.multiply(-height / 2)).asBigDecimal(zoomMag).add(locY), zoomMag);
		Complex2 c4 = new Complex2((scale.multiply( width / 2)).asBigDecimal(zoomMag).add(locX), (scale.multiply( height / 2)).asBigDecimal(zoomMag).add(locY), zoomMag);
		return getPeriod(c1, c2, c3, c4);
	}
}
