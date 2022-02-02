package com.irar.mbviewer.util;

public interface IProgressMonitorFactory<T extends IProgressMonitor> {

	public T createNewProgressMonitor();
	public void deleteProgressMonitor(T monitor);
	public void deleteAllMonitors();
	
}
