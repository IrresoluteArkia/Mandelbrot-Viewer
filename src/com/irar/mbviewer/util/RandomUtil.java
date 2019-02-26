package com.irar.mbviewer.util;

import java.util.List;
import java.util.Random;

public class RandomUtil {

	public static <T> T pickOne(List<T> points) {
		Random r = new Random();
		return points.get(r.nextInt(points.size()));
	}

}
