package com.irar.mbviewer;

import javax.swing.JProgressBar;

public class ProgressMonitor implements IProgressMonitor{
	
	private JProgressBar pBar;
	private ProgressMonitorFactory factory;
	private int id;

	public ProgressMonitor(JProgressBar pBar, ProgressMonitorFactory factory, int id) {
		this.pBar = pBar;
		this.factory = factory;
		this.id = id;
	}

	@Override
	public void setProgress(float progress) {
		pBar.setValue((int) (progress * pBar.getMaximum()));
		if(id == 0) {
			Viewer.zoomAnimationProgress = progress;
		}
	}
	
	public JProgressBar getProgressBar() {
		return pBar;
	}

	@Override
	public void deleteMonitor() {
		factory.deleteProgressMonitor(this);
	}

}
