package com.irar.mbviewer;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

public class WindowedMode implements ViewMode {

	@Override
	public void applyMode(JFrame window, Viewer display) {
		display.setSize(512, 512);
		window.dispose();
		window.setUndecorated(false);
		window.setVisible(true);
		window.pack();
		window.setLocationRelativeTo(null);
	}

	@Override
	public int getViewWidth() {
		return 512;
	}

	@Override
	public int getViewHeight() {
		return 512;
	}

	@Override
	public int getViewOffsetX() {
		return 0;
	}

	@Override
	public int getViewOffsetY() {
		return 0;
	}

	@Override
	public void draw(Graphics g, BufferedImage bi) {
		g.drawImage(bi, 0, 0, 512, 512, null);
	}

}
