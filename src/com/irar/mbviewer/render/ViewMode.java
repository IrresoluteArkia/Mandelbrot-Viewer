package com.irar.mbviewer.render;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

import com.irar.mbviewer.Viewer;

public interface ViewMode {

	void applyMode(JFrame window, Viewer display);

	int getViewWidth();

	int getViewHeight();

	int getViewOffsetX();

	int getViewOffsetY();

	void draw(Graphics g, BufferedImage bi);
	
}
