package com.irar.mbviewer;

import java.util.ArrayList;
import java.util.List;

public class Palette {

	public int[] init;
	public int[] palette;
	public int[] paletteloop;
	public int colorlength;
	public boolean loop;
	public String name;
	
	public Palette(String name, int colorLength, boolean loop, int... colors) {
		if(colors.length == 0) {
			try {
				throw new Exception("Palette left blank!");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		this.name = name;
		this.init = colors;
		this.colorlength = colorLength;
		this.loop = loop;
		List<Integer> palette = new ArrayList<>();
		List<Integer> paletteloop = new ArrayList<>();
		for(int color : colors) {
			palette.add(color);
			paletteloop.add(color);
		}
		if(loop) {
			palette.add(colors[0]);
		}
		paletteloop.add(colors[0]);
		for(int i = 0; i < colorLength; i++) {
			palette = processPalette(palette);
			paletteloop = processPalette(paletteloop);
		}
		this.palette = toArray(palette);
		this.paletteloop = toArray(paletteloop);
	}

	private int[] toArray(List<Integer> palette2) {
		int[] array = new int[palette2.size()];
		for(int i = 0; i < palette2.size(); i++) {
			array[i] = palette2.get(i);
		}
		return array;
	}

	private List<Integer> processPalette(List<Integer> palette) {
		List<Integer> newPalette = new ArrayList<>();
		for(int i = 0; i < palette.size() - 1; i++) {
			int color1 = palette.get(i);
			int color2 = palette.get(i + 1);
			int r1 = color1 / (256 * 256);
			int g1 = (color1 - (r1 * 256 * 256)) / 256;
			int b1 = (color1 - (r1 * 256 * 256) - (g1 * 256));
			int r2 = color2 / (256 * 256);
			int g2 = (color2 - (r2 * 256 * 256)) / 256;
			int b2 = (color2 - (r2 * 256 * 256) - (g2 * 256));
			int newr = (r1 + r2) / 2;
			int newg = (g1 + g2) / 2;
			int newb = (b1 + b2) / 2;
			int newColor = (newr * 256 * 256) + (newg * 256) + newb;
			if(i == 0) {
				newPalette.add(color1);
			}
			newPalette.add(newColor);
			newPalette.add(color2);
		}
		return newPalette;
	}
	
}
