package com.irar.mbviewer.mandelbrot;

import com.irar.mbviewer.math.Complex2;
import com.irar.mbviewer.math.SizedDouble;

public class ZoomLoc {

	public Complex2 loc;
	public SizedDouble zoom;

	public ZoomLoc(Complex2 loc, SizedDouble zoom) {
		this.loc = loc;
		this.zoom = zoom;
	}

}
