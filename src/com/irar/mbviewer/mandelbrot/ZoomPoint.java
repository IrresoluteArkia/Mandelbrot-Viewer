package com.irar.mbviewer.mandelbrot;

import com.irar.mbviewer.math.Complex2;
import com.irar.mbviewer.util.Producer;

public class ZoomPoint {

	public int x;
	public int y;
	public Producer<Complex2> c;
	public boolean redo = false;
	public int itersDone = 0;

	public ZoomPoint(int i, int j, Producer<Complex2> c) {
		this.x = i;
		this.y = j;
		this.c = c;
	}

}
