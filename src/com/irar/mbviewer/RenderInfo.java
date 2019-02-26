package com.irar.mbviewer;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class RenderInfo {

	JTextField minIter = new JTextField("Uncalculated");
	JTextField avgIter = new JTextField("");
	JTextField maxIter = new JTextField("");
	JTextField tTime = new JTextField("");
	JPanel progressBars = new JPanel();
	JLabel tpIter = new JLabel("");
	JLabel tff = new JLabel("");
	public JPanel container;
	private Viewer viewer;
/*	JLabel minIter = new JLabel("Uncalculated");
	JLabel minIter = new JLabel("Uncalculated");
	JLabel minIter = new JLabel("Uncalculated");*/
	
	public RenderInfo(Viewer viewer) {
		this.viewer = viewer;
	}
	
	
	public void addToPanel(JPanel panel) {
		container = panel;
		minIter.setColumns(20);
		minIter.setMaximumSize(new Dimension(10000, 30));
		maxIter.setColumns(20);
		maxIter.setMaximumSize(new Dimension(10000, 30));
		avgIter.setColumns(20);
		avgIter.setMaximumSize(new Dimension(10000, 30));
		tTime.setColumns(20);
		tTime.setMaximumSize(new Dimension(10000, 30));
		progressBars.setLayout(new BoxLayout(progressBars, BoxLayout.Y_AXIS));
/*		minIter.setPreferredSize(minIter.getSize());
		minIter.setMinimumSize(minIter.getSize());*/
		panel.add(minIter);
//		panel.add(maxIter);
//		panel.add(avgIter);
//		panel.add(tTime);
		panel.add(progressBars);
//		panel.add(tpIter);
//		panel.add(tff);
	}


	public void setMin(int min) {
		minIter.setText("Minimum: " + min);
	}


	public void setAvg(int t) {
		avgIter.setText("Average: " + t);
	}


	public void setMax(int max) {
		maxIter.setText("Maximum: " + max);
	}


	public void setTime(long l) {
		l = l / 1000;
		tTime.setText("Total Render Time: " + l + "s");
	}


	public void setATime(long l) {
		tpIter.setText("Time Per Iteration: " + l + "ms");
	}


	public void setTFF(double d) {
		tff.setText("Expected Time For Full: " + ((int) (d / 1000)) + "s");
	}


	public void validate() {
		viewer.pack();
	}
	
	
	
}
