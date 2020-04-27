package com.irar.mbviewer;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

public interface ViewMode {

	void applyMode(JFrame window, Viewer display);

	int getViewWidth();

	int getViewHeight();

	int getViewOffsetX();

	int getViewOffsetY();

	void draw(Graphics g, BufferedImage bi);
	
}
