package com.irar.mbviewer;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JProgressBar;

public class ProgressMonitorFactory implements IProgressMonitorFactory<ProgressMonitor>{
	
	private StatusBar info;
	private List<ProgressMonitor> monitors = new ArrayList<>();
	private int id = 0;
	
	public ProgressMonitorFactory(StatusBar statusBar) {
		this.info = statusBar;
	}

	@Override
	public ProgressMonitor createNewProgressMonitor() {
		JProgressBar pBar = new JProgressBar(0, 100);
		info.setBar(pBar);
		info.validate();
		ProgressMonitor monitor = new ProgressMonitor(pBar, this, id);
		id++;
		monitors.add(monitor);
		return monitor;
	}

	@Override
	public void deleteProgressMonitor(ProgressMonitor monitor) {
		monitors.remove(monitor);
		info.removeBar(monitor.getProgressBar());
		info.validate();
	}

	@Override
	public void deleteAllMonitors() {
		List<ProgressMonitor> allMonitors = new ArrayList<>();
		allMonitors.addAll(monitors);
		for(ProgressMonitor monitor : allMonitors) {
			monitor.deleteMonitor();
		}
	}

}
