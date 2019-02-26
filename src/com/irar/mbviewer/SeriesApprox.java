package com.irar.mbviewer;

import java.util.ArrayList;
import java.util.List;

public class SeriesApprox{

	public List<Complex3> AN = new ArrayList<>();
	public List<Complex3> BN = new ArrayList<>();
	public List<Complex3> CN = new ArrayList<>();
	public int skipped = 0;

	public SeriesApprox(ReferencePoint rPoint, ZoomPoint testPoint, Complex power, MBHelper helper, IProgressMonitor monitor) throws Exception {
		double tol = /*Math.pow(2, -64)*/0.0000001d;
		Complex2 genTestPoint = testPoint.c.produce();
		Complex3 delta0 = new Complex3(genTestPoint.subtract(rPoint.XN.get(0)));
		Complex3 deltaPow2 = delta0.pow(2);
		Complex3 deltaPow3 = delta0.pow(3);
		AN.add(new Complex3(1, 0));
		BN.add(new Complex3(0, 0));
		CN.add(new Complex3(0, 0));
		int percent = -1;
		if(power.x != 2 || power.y != 0) {
			monitor.deleteMonitor();
			return;
		}
		for(int i = 1; i < rPoint.XN.size(); i++) {
			int nPercent = (i * 100 / rPoint.XN.size());
			if(percent != nPercent) {
				percent = nPercent;
				Viewer.renderInfo.minIter.setText("Approximating " + percent + "%");
			}
			AN.add(AN.get(i - 1).multiply(rPoint.XN2.get(i - 1)).addReal(1));
			BN.add(BN.get(i - 1).multiply(rPoint.XN2.get(i - 1)).add(AN.get(i - 1).pow(2)));
			CN.add(CN.get(i - 1).multiply(rPoint.XN2.get(i - 1)).add(AN.get(i - 1).multiply(BN.get(i - 1).multiply(2))));
			if (((BN.get(i).multiply(deltaPow2)).magSqu().multiply(tol)).compareTo((CN.get(i).multiply(deltaPow3)).magSqu()) < 0) {
				if (i <= 3) {
					skipped = 0;
					monitor.deleteMonitor();
					return;
				}
				else {
					skipped = (i - 3);
					monitor.deleteMonitor();
					return;
				}
			}
			monitor.setProgress((float) i / rPoint.XN.size());
			helper.checkShouldKeepRunning();
		}
		skipped = 0;
		monitor.deleteMonitor();
	}
	
}
