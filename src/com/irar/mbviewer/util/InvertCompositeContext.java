package com.irar.mbviewer.util;

import java.awt.CompositeContext;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

public class InvertCompositeContext implements CompositeContext {

	@Override
	public void dispose() {}

	@Override
	public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
		int w = Math.min(src.getWidth(), dstIn.getWidth());
		int h = Math.min(src.getHeight(), dstIn.getHeight());
		
		int[] srcRGBA = new int[4];
		int[] dstRGBA = new int[4];
		
		if (dstIn != dstOut) {
			dstOut.setDataElements(0, 0, dstIn);
        }
		
		for(int x = 0; x < w; x++) {
			for(int y = 0; y < h; y++) {
				src.getPixel(x, y, srcRGBA);
				dstIn.getPixel(x, y, dstRGBA);
				if(srcRGBA[0] == 0 && srcRGBA[1] == 0 && srcRGBA[2] == 0) {
					continue;
				}
				for(int i = 0; i < 3; i++) {
					dstRGBA[i] = Math.max(0, srcRGBA[i]-dstRGBA[i]);
				}
				dstOut.setPixel(x, y, dstRGBA);
			}
		}
	}

}
