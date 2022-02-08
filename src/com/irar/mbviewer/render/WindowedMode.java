package com.irar.mbviewer.render;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

import com.irar.mbviewer.Viewer;

public class WindowedMode implements ViewMode {

	@Override
	public void applyMode(JFrame window, Viewer display) {
		display.setSize(Viewer.DEF_WIDTH, Viewer.DEF_HEIGHT);
		window.dispose();
		window.setUndecorated(false);
		window.setVisible(true);
		window.pack();
		window.setLocationRelativeTo(null);
	}

	@Override
	public int getViewWidth() {
		return Viewer.DEF_WIDTH;
	}

	@Override
	public int getViewHeight() {
		return Viewer.DEF_HEIGHT;
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
		g.drawImage(bi, 0, 0, Viewer.DEF_WIDTH, Viewer.DEF_HEIGHT, null);
	}

}
