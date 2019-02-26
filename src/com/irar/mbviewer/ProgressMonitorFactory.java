package com.irar.mbviewer;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JProgressBar;

public class ProgressMonitorFactory implements IProgressMonitorFactory<ProgressMonitor>{
	
	private RenderInfo info;
	private List<ProgressMonitor> monitors = new ArrayList<>();
	
	public ProgressMonitorFactory(RenderInfo info) {
		this.info = info;
	}

	@Override
	public ProgressMonitor createNewProgressMonitor() {
		JProgressBar pBar = new JProgressBar(0, 100);
		info.progressBars.add(pBar);
		info.validate();
		ProgressMonitor monitor = new ProgressMonitor(pBar, this);
		monitors.add(monitor);
		return monitor;
	}

	@Override
	public void deleteProgressMonitor(ProgressMonitor monitor) {
		monitors.remove(monitor);
		info.progressBars.remove(monitor.getProgressBar());
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
