package com.irar.mbviewer;

import java.util.List;

public class Cluster {

	private List<ZoomPoint> points;
	private int width;
	private int height;

	public Cluster(List<ZoomPoint> clusterPoints) {
		this.points = clusterPoints;
		int maxX = -1;
		int maxY = -1;
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		for(ZoomPoint point : points) {
			if(point.x < minX) {
				minX = point.x;
			}
			if(point.y < minY) {
				minY = point.y;
			}
			if(point.x > maxX) {
				maxX = point.x;
			}
			if(point.y > maxY) {
				maxY = point.y;
			}
		}
		this.width = maxX-minX+1;
		this.height = maxY-minY+1;
	}
	
	public List<ZoomPoint> getPoints(){
		return points;
	}

	public ZoomPoint getReferenceLoc() {
		int[] center = getCenter();
		ZoomPoint point = getClosest(center);
		return point;
	}

	private ZoomPoint getClosest(int[] center) {
		ZoomPoint closest = null;
		int closestDistance = Integer.MAX_VALUE;
		for(ZoomPoint point : points) {
			int distance = Math.abs(point.x - center[0]) + Math.abs(point.y - center[1]);
			if(distance < closestDistance) {
				closestDistance = distance;
				closest = point;
			}
		}
		return closest;
	}

	private int[] getCenter() {
		int totalX = 0;
		int totalY = 0;
		for(ZoomPoint point : points) {
			totalX += point.x;
			totalY += point.y;
		}
		return new int[] {totalX/points.size(), totalY/points.size()};
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

}
