package com.irar.mbviewer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

public class FullscreenMode implements ViewMode {
	
	private int width;
	private int height;
	private int offsetX;
	private int offsetY;
	private int swidth;
	private int sheight;
	private int lastWidth = 0;
	private int lastHeight = 0;
	private int x = 0;
	private int y = 0;
	
	public FullscreenMode() {
		swidth = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().width;
		sheight = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().height;
		sheight -= Viewer.getUnusableHeight();
		boolean widthbigger = swidth > sheight;
		if(widthbigger) {
			width = sheight;
			height = sheight;
		}else {
			width = swidth;
			height = swidth;
		}
		offsetX = swidth/2 - width/2;
		offsetY = sheight/2 - height/2;
		x = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().x;
		y = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().y;
	}

	@Override
	public void applyMode(JFrame window, Viewer display) {
		display.setSize(swidth, sheight);
		window.dispose();
		window.setUndecorated(true);
		window.setVisible(true);
		window.pack();
		window.setLocation(x, y);
	}

	@Override
	public int getViewWidth() {
		return width;
	}

	@Override
	public int getViewHeight() {
		return height;
	}

	@Override
	public int getViewOffsetX() {
		return offsetX;
	}

	@Override
	public int getViewOffsetY() {
		return offsetY;
	}

	@Override
	public void draw(Graphics g, BufferedImage bi) {
		if(bi.getWidth() != lastWidth || bi.getHeight() != lastHeight) {
			updateWH(bi.getWidth(), bi.getHeight());
		}
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, swidth, sheight);
		g.drawImage(bi, offsetX, offsetY, width, height, null);
	}

	private void updateWH(int bwidth, int bheight) {
		float ratioscreen = (float) swidth/sheight;
		float ratiob = (float) bwidth/bheight;
		boolean widthbigger = ratiob < ratioscreen;
		if(widthbigger) {
			width = (int) (sheight*ratiob);
			height = sheight;
		}else {
			width = swidth;
			height = (int) (swidth*(1/ratiob));
		}
		offsetX = swidth/2 - width/2;
		offsetY = sheight/2 - height/2;
		
		lastWidth = bwidth;
		lastHeight = bheight;
	}

}
