package com.irar.mbviewer.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import com.irar.mbviewer.mandelbrot.OversampleIteration;
import com.irar.mbviewer.math.Complex2;

public class RandomUtil {

	public static <T> T pickOne(List<T> points) {
		Random r = new Random();
		return points.get(r.nextInt(points.size()));
	}

	public static <T> T pickWeighted(HashMap<T, Integer> weightMap) {
		int total = 0;
		for(int i : weightMap.values()) {
			total += i;
		}
		T result = null;
		double rand = new Random().nextDouble() * total;
		for(Entry<T, Integer> entry : weightMap.entrySet()) {
			rand -= entry.getValue();
			if(rand <= 0.0d) {
				result = entry.getKey();
				break;
			}
		}
		return result;
	}

}
