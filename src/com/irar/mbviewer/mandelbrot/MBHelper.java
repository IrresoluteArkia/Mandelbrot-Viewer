package com.irar.mbviewer.mandelbrot;

import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.irar.mbviewer.Viewer;
import com.irar.mbviewer.math.Complex;
import com.irar.mbviewer.math.Complex2;
import com.irar.mbviewer.math.Complex3;
import com.irar.mbviewer.math.SizedDouble;
import com.irar.mbviewer.render.IMBRenderer;
import com.irar.mbviewer.util.IProgressMonitor;
import com.irar.mbviewer.util.IProgressMonitorFactory;
import com.irar.mbviewer.util.MBInfo;
import com.irar.mbviewer.util.ProgressMonitorFactory;
import com.irar.mbviewer.util.RandomUtil;

public class MBHelper {
	
//	Iteration[][][] iterations = null;
	public static OversampleIteration[][] iterations = null;
	public boolean interrupted = false;
	private IMBRenderer renderer = null;
	private static List<ReferencePoint> APR = new ArrayList<>();
	
	public void getSet(BufferedImage bi, MBInfo info, IProgressMonitorFactory<?> progressMonitorFactory, IMBRenderer renderer) {
		this.renderer = renderer;
		int width = bi.getWidth();
		int height = bi.getHeight();
		int oversample = info.getOversample();
		OversampleIteration[][] allIterations = null;
		try {
			allIterations = iterate(info, width, height, oversample, progressMonitorFactory);
			interpolateEmpty(allIterations);
		}catch(Exception e) {
			interrupted = true;
			progressMonitorFactory.deleteAllMonitors();
			e.printStackTrace();
			Viewer.zoomAnimationProgress = 0;
			return;
		}
		try {
			renderer.drawIterations(allIterations, bi, info);
		}catch(Exception e) {
			interrupted = true;
			progressMonitorFactory.deleteAllMonitors();
			e.printStackTrace();
			Viewer.zoomAnimationProgress = 0;
			return;
		}
		iterations = allIterations;
		Viewer.statusBar.iterationData(iterations);
		Viewer.zoomAnimationProgress = 0;
	}
	
	/**
	 * Fill in the gaps in the generated set by substituting neighboring points. 
	 * At normal resolutions, the result should be indistinguishable from normal
	 * generation. This function assumes that the parameter containing the 
	 * iterations is a continuous sample of the set, and that the gaps are not
	 * too big, as the larger the gap, the less reliable this form of data
	 * interpolation will be.
	 * @param allIterations
	 */
	private void interpolateEmpty(OversampleIteration[][] allIterations) {
		List<Object[]> empty = new ArrayList<>();
		int width = allIterations.length;
		for(int x = 0; x < width; x++) {
			int height = allIterations[x].length;
			for(int y = 0; y < height; y++) {
				if(allIterations[x][y].getIterations().size() == 0) {
					empty.add(new Object[] {allIterations[x][y], x, y});
				}
			}
		}
		
		while(empty.size() > 0) {
			List<Object[]> newEmpty = new ArrayList<>();
			for(Object[] data : empty) {
				OversampleIteration oi = (OversampleIteration) data[0];
				int x = (int) data[1];
				int y = (int) data[2];
				int[][] nListLoc = new int[][] {
					new int[] {0, 1},
					new int[] {0, -1},
					new int[] {1, 0},
					new int[] {-1, 0}
				};
				List<OversampleIteration> nList = new ArrayList<>();
				// get valid neighbors
				for(int i = 0; i < nListLoc.length; i++) {
					// check bounds
					if(nListLoc[i][0]+x >= 0 && nListLoc[i][1]+y >= 0 && nListLoc[i][0]+x < width && nListLoc[i][1]+y < allIterations[nListLoc[i][0]+x].length) {
						OversampleIteration neighbor = allIterations[nListLoc[i][0]+x][nListLoc[i][1]+y];
						// check if neighbor is empty
						if(neighbor.getIterations().size() > 0) {
							nList.add(neighbor);
						}
					}
				}
				if(nList.size() == 0) {
					newEmpty.add(data);
				}else {
					OversampleIteration fakeIter = new OversampleIteration();
					for(OversampleIteration neighbor : nList) {
						for(Iteration iter : neighbor.getIterations()) {
							fakeIter.addIteration(new Iteration(iter.iterations, iter.partial, iter.getMaxIter(), iter.skipped, iter.getBaseLocation(), iter.getOffsetLocation()));
						}
					}
					allIterations[x][y] = fakeIter;
				}
			}
			empty = newEmpty;
		}
	}

	private OversampleIteration[][] iterate(MBInfo info, int width, int height, int samples, IProgressMonitorFactory<?> factory) throws Exception {
		IProgressMonitor monitor = factory.createNewProgressMonitor();
		OversampleIteration[][] iterations = new OversampleIteration[width][height];
		reusePoints(info, width, height, iterations);
		ReferencePoint rPoint;
		List<ZoomPoint> points = getZoomPoints(info, width, height);
		SeriesApprox approx = null;
		List<ZoomPoint> repeatPoints = new ArrayList<>(points.size());
		for(ZoomPoint point : points) {
			point.redo = true;
		}
		do {
			if(approx != null) {
				info.setIterations(approx.skipped * 20);
			}
			rPoint = getStartingPoint(info, factory, width, height);
			approx = getApproximations(rPoint, points, info, width, height, factory);
		}while(info.getIterations() < approx.skipped * 20);
		AtomicInteger current = new AtomicInteger(0);
		AtomicInteger done = new AtomicInteger(0);
		AtomicBoolean badRef = new AtomicBoolean(false);
		int end = points.size();
		final ReferencePoint usingRPoint = rPoint;
		final SeriesApprox usingApprox = approx;
		final List<ZoomPoint> usingPoints = points;
		points.parallelStream().forEach(point -> {
			synchronized(badRef) {
				if(badRef.get()) {
					return;
				}
			}
			if(point.redo) {
				point.redo = false;
			}
			try {
				iterations[point.x][point.y] = iterate(info, point, usingRPoint, usingApprox, getZoomMagnitude(info), samples, width, height, iterations[point.x][point.y]);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			synchronized(badRef) {
				if(!badRef.get()) {
					if(point.redo) {
						repeatPoints.add(point);
					}else {
						done.addAndGet(1);
					}
					current.addAndGet(1);
					if(isBadReference(current.get(), usingPoints.size(), repeatPoints.size())) {
						badRef.set(true);
					}
					monitor.setProgress((float) done.get() / end);
				}
			}
		});
		if(badRef.get()) {
			repeatPoints.clear();
			repeatPoints.addAll(points);
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

	private void reusePoints(MBInfo info, int width, int height, OversampleIteration[][] newIterations) {
		int zoomMag = this.getZoomMagnitude(info);
		SizedDouble scale = info.getZoom().multiply(4.0 / height);
		Complex2 base = new Complex2(info.getX(), info.getY(), zoomMag);
		BigDecimal xMin = scale.multiply(-width / 2)
				.asBigDecimal(zoomMag)
				.add(info.getX()
						.setScale(zoomMag, RoundingMode.DOWN))
				.setScale(zoomMag, RoundingMode.DOWN);
		BigDecimal xMax = scale.multiply(width / 2)
				.asBigDecimal(zoomMag)
				.add(info.getX()
						.setScale(zoomMag, RoundingMode.DOWN))
				.setScale(zoomMag, RoundingMode.DOWN);
		BigDecimal yMin = scale.multiply(-height / 2)
				.asBigDecimal(zoomMag)
				.add(info.getY()
						.setScale(zoomMag, RoundingMode.DOWN))
				.setScale(zoomMag, RoundingMode.DOWN);
		BigDecimal yMax = scale.multiply(height / 2)
				.asBigDecimal(zoomMag)
				.add(info.getY()
						.setScale(zoomMag, RoundingMode.DOWN))
				.setScale(zoomMag, RoundingMode.DOWN);
		SizedDouble xDif = SizedDouble.parseSizedDouble(xMax.subtract(xMin));
		SizedDouble yDif = SizedDouble.parseSizedDouble(yMax.subtract(yMin));
		for(int i = 0; i < newIterations.length; i++) {
			for(int j = 0; j < newIterations[i].length; j++) {
				newIterations[i][j] = new OversampleIteration();
			}
		}
		if(iterations != null) {
			int reused = 0;
			for(int i = 0; i < iterations.length; i++) {
				for(int j = 0; j < iterations[i].length; j++) {
					OversampleIteration oi = iterations[i][j];
					for(Iteration iter : oi.getIterations()) {
						if(iter.iterations == iter.getMaxIter() && iter.getMaxIter() < info.getIterations()) {
							continue;
						}
						Complex2 loc = iter.getActualLocation();
						BigDecimal x = loc.x;
						BigDecimal y = loc.y;
						if(x.compareTo(xMin) >= 0 && x.compareTo(xMax) <= 0 && y.compareTo(yMin) >= 0 && y.compareTo(yMax) <= 0) {
							SizedDouble iterXDif = SizedDouble.parseSizedDouble(x.subtract(xMin));
							SizedDouble iterYDif = SizedDouble.parseSizedDouble(y.subtract(yMin));
							double locXD = iterXDif.divide(xDif).asDouble();
							double locYD = iterYDif.divide(yDif).asDouble();
							int locX = limit((int) (locXD * newIterations.length), 0, newIterations.length-1);
							int locY = limit((int) (locYD * newIterations[0].length), 0, newIterations[0].length-1);
							if(newIterations[locX][locY].getIterations().size() < info.getOversample()) {
								iter.changeBaseLocation(base);
								newIterations[locX][locY].addIteration(iter);
								reused++;
							}
						}
					}
				}
			}
			System.out.println("Reused " + reused + " points of data");
		}
	}

	private int limit(int i, int min, int max) {
		return Math.min(max, Math.max(min, i));
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

	private void repeatIteration(MBInfo info, List<ZoomPoint> points, OversampleIteration[][] iterations2, int samples,
			IProgressMonitorFactory<?> factory, IProgressMonitor progressMonitor, int repeatNum, int width, int height) throws Exception {
		int end = iterations2.length * iterations2[0].length;
		AtomicInteger current = new AtomicInteger(end - points.size());
		AtomicInteger done = new AtomicInteger(current.get());
		AtomicInteger currentR = new AtomicInteger(0);
		AtomicBoolean badRef = new AtomicBoolean(false);
		if (repeatNum >= 100/* || (remaining < (end / 10000) && repeatNum > 10) */) {
			return;
		}
		ReferencePoint rPoint = getRandomReference(points, info, factory);
		SeriesApprox approx = getApproximations(rPoint, points, info, width, height, factory);
		final List<ZoomPoint> repeatPoints = new ArrayList<>(points.size());
		final ReferencePoint usingRPoint = rPoint;
		final SeriesApprox usingApprox = approx;
		final List<ZoomPoint> usingPoints = points;
		if(info.getIterations() < approx.skipped * 10) {
			info.setIterations(approx.skipped * 10);
			repeatPoints.addAll(points);
		}else {
			points.parallelStream().forEach(point -> {
				synchronized(badRef) {
					if(badRef.get() || point == null) {
						return;
					}
				}
				if(point.redo) {
					point.redo = false;
					try {
						iterations2[point.x][point.y] = iterate(info, point, usingRPoint, usingApprox, getZoomMagnitude(info), samples, width, height, iterations2[point.x][point.y]);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				synchronized(badRef) {
					if(!badRef.get()) {
						if(point.redo) {
							repeatPoints.add(point);
						}else {
							done.addAndGet(1);
						}
						current.addAndGet(1);
						currentR.addAndGet(1);
						if(isBadReference(currentR.get(), usingPoints.size(), repeatPoints.size())) {
							badRef.set(true);
						}
						progressMonitor.setProgress((float) done.get() / end);
					}
				}
			});
		}
		if(badRef.get()) {
			repeatPoints.clear();
			repeatPoints.addAll(points);
		}
		if(repeatPoints.size() > 0) {
			rPoint = null;
			approx = null;
			points = null;
			repeatIteration(info, repeatPoints, iterations2, samples, factory, progressMonitor, repeatNum + 1, width, height);
		}
	}

	private ReferencePoint getRandomReference(List<ZoomPoint> points, MBInfo info, IProgressMonitorFactory<?> factory) throws Exception {
		ZoomPoint point = RandomUtil.pickOne(points);
		points.remove(point);
		Complex2 chosenLoc = point.c.produce();
		ReferencePoint chosen = new ReferencePoint(chosenLoc.x, chosenLoc.y, info.getIterations(), getZoomMagnitude(info), info.getPower(), this, factory.createNewProgressMonitor());
		APR.add(chosen);
		return chosen;
	}

	private OversampleIteration iterate(MBInfo info, ZoomPoint point, ReferencePoint rPoint, SeriesApprox approx, int zoomMagnitude,
			int samples, int width, int height, OversampleIteration oi) throws Exception {
		ZoomPoint pointI = deviateRandomly(point, width, height, info.getZoom(), zoomMagnitude);
		for(int i = oi.getIterations().size(); i < samples; i++) {
			Iteration iteration = iterate(info, pointI, rPoint, approx, zoomMagnitude);
			if(pointI.redo) {
				point.redo = true;
				continue;
			}else if(iteration.iterations == info.getIterations()) {
				oi.addIteration(iteration);
				return oi;
			}
			oi.addIteration(iteration);
		}
		return oi;
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
		Iteration iter = new Iteration(curIter, partial, maxIter, approx.skipped, rPoint.loc, new Complex3(delta0));
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
		return new Iteration(curIter, partial, maxIter, approx.skipped, rPoint.loc, delta0);
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
									.setScale(zoomMag, RoundingMode.DOWN))
							.setScale(zoomMag, RoundingMode.DOWN), 
							scale.multiply(jF - height / 2)
							.asBigDecimal(zoomMag)
							.add(info.getY()
									.setScale(zoomMag, RoundingMode.DOWN))
							.setScale(zoomMag, RoundingMode.DOWN), 
							zoomMag);
				}));
			}
		}
		Collections.shuffle(points);
		return points;
	}

	private ReferencePoint getStartingPoint(MBInfo info, IProgressMonitorFactory<?> factory, int width, int height) throws Exception {
		weedAPR(info, width, height);
		if(APR.size() > 0) {
			ReferencePoint selected = null;
			for(ReferencePoint pr : APR) {
				if(selected == null || pr.XN.size() > selected.XN.size()) {
					selected = pr;
				}
			}
			System.out.println("Found relavent reference point, reusing...");
			return selected;
		}
		System.out.println("No relavent reference point found, using center...");
		ReferencePoint def = new ReferencePoint(info.getX(), info.getY(), info.getIterations(), getZoomMagnitude(info), info.getPower(), this, factory.createNewProgressMonitor());
		APR.add(def);
		return def;
	}
	
	private void weedAPR(MBInfo info, int width, int height) {
		float reuseDistance = 1.2f;
		int zoomMag = this.getZoomMagnitude(info);
		SizedDouble scale = info.getZoom().multiply(4.0 / height);
		BigDecimal xMin = scale.multiply(reuseDistance * -width / 2)
				.asBigDecimal(zoomMag)
				.add(info.getX()
						.setScale(zoomMag, RoundingMode.DOWN))
				.setScale(zoomMag, RoundingMode.DOWN);
		BigDecimal xMax = scale.multiply(reuseDistance * width / 2)
				.asBigDecimal(zoomMag)
				.add(info.getX()
						.setScale(zoomMag, RoundingMode.DOWN))
				.setScale(zoomMag, RoundingMode.DOWN);
		BigDecimal yMin = scale.multiply(reuseDistance * -height / 2)
				.asBigDecimal(zoomMag)
				.add(info.getY()
						.setScale(zoomMag, RoundingMode.DOWN))
				.setScale(zoomMag, RoundingMode.DOWN);
		BigDecimal yMax = scale.multiply(reuseDistance * height / 2)
				.asBigDecimal(zoomMag)
				.add(info.getY()
						.setScale(zoomMag, RoundingMode.DOWN))
				.setScale(zoomMag, RoundingMode.DOWN);
		List<ReferencePoint> remove = new ArrayList<>();
		APR.forEach((pr) -> {
			if(pr.x.compareTo(xMin) < 0 || pr.x.compareTo(xMax) > 0 || pr.y.compareTo(yMin) < 0 || pr.y.compareTo(yMax) > 0) {
				remove.add(pr);
			}
		});
		System.out.println("Weeded " + remove.size() + " irrelavent reference points");
		APR.removeAll(remove);
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

	public void setRenderer(IMBRenderer renderer) {
		this.renderer = renderer;
	}
}
