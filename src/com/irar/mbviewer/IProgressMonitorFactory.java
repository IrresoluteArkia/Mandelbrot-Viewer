package com.irar.mbviewer;

public interface IProgressMonitorFactory<T extends IProgressMonitor> {

	public T createNewProgressMonitor();
	public void deleteProgressMonitor(T monitor);
	public void deleteAllMonitors();
	
}
