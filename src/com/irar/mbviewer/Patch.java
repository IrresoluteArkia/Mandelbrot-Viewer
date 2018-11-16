package com.irar.mbviewer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Patch {

//	int iterNum;
	public List<ZoomPoint> points = new ArrayList<>();
	public ZoomPoint rPoint = null;
	
	public Patch(List<ZoomPoint> pointsLeft) {
		if(pointsLeft.size() < 3) {
			return;
		}
		points.addAll(pointsLeft);
		pointsLeft.clear();
		int size = pointsLeft.size();/*
		while(points.size() < size / 3 + 3 && pointsLeft.size() > 0) {
			ZoomPoint ref = pointsLeft.get(0);
			points.add(ref);
			pointsLeft.remove(0);
//			iterNum = ref.itersDone;
			populatePoints(pointsLeft);
		}*/
		rPoint = getBestReferencePoint();
	}

	private ZoomPoint getBestReferencePoint() {
	/*	long totalX = 0;
		long totalY = 0;
		for(ZoomPoint point : points) {
			totalX += point.x;
			totalY += point.y;
		}
		int bestX = (int) (totalX / points.size());
		int bestY = (int) (totalY / points.size());*/
		ZoomPoint best = /*getPointByXY(bestX, bestY)*/null;
		if(best == null) {
			best = points.get(new Random().nextInt(points.size()));
		}
		return best;
	}

	private ZoomPoint getPointByXY(int x, int y) {
		for(ZoomPoint point : points) {
			if(point.x == x && point.y == y) {
				return point;
			}
		}
		return null;
	}

	private void populatePoints(List<ZoomPoint> pointsLeft) {
		int nPointsFound = 1;
		while(nPointsFound > 0) {
			nPointsFound = 0;
			for(ZoomPoint point : pointsLeft) {
				if(/*point.itersDone == iterNum && */isPointNeighboringAny(point)) {
					nPointsFound++;
					points.add(point);
				}
			}
			for(ZoomPoint point : points) {
				pointsLeft.remove(point);
			}
		}
	}

	private boolean isPointNeighboringAny(ZoomPoint point) {
		for(ZoomPoint point2 : points) {
			if(isPointNeighboring(point, point2)) {
				return true;
			}
		}
		return false;
	}

	private boolean isPointNeighboring(ZoomPoint point, ZoomPoint point2) {
		return point.x == point2.x + 1 || point.x == point2.x - 1 || point.y == point2.y + 1 || point.y == point2.y - 1;
	}
	
}
