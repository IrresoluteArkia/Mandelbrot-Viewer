package com.irar.mbviewer;

import javax.swing.JProgressBar;

public class ProgressMonitor implements IProgressMonitor{
	
	private JProgressBar pBar;
	private ProgressMonitorFactory factory;

	public ProgressMonitor(JProgressBar pBar, ProgressMonitorFactory factory) {
		this.pBar = pBar;
		this.factory = factory;
	}

	@Override
	public void setProgress(float progress) {
		pBar.setValue((int) (progress * pBar.getMaximum()));
	}
	
	public JProgressBar getProgressBar() {
		return pBar;
	}

	@Override
	public void deleteMonitor() {
		factory.deleteProgressMonitor(this);
	}

}
