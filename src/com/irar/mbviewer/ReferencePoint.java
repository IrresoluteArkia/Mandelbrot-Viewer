package com.irar.mbviewer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ReferencePoint{

	public BigDecimal x;
	public BigDecimal y;
	public Complex2 loc;
	public C2ArrayList XN = new C2ArrayList();
	public List<SizedDouble> XNM3 = new ArrayList<>();
	public List<Double> XNM3Small = new ArrayList<>();
	public List<Complex3> XNc = new ArrayList<>();
	public List<Complex3> XN2 = new ArrayList<>();
	public List<Complex> XNcSmall = new ArrayList<>();
	public List<Complex> XN2Small = new ArrayList<>();
	public List<Complex> XPNSmall = new ArrayList<>();
//	public List<Complex2> XPN = new ArrayList<>();


	public ReferencePoint(BigDecimal x, BigDecimal y, int maxIter, int pre, Complex power, MBHelper helper, IProgressMonitor monitor) throws Exception {
		maxIter += 3;
		Complex2 c0 = new Complex2(x, y, pre);
		this.x = x;
		this.y = y;
		this.loc = new Complex2(x, y, pre);
		Complex2 c = new Complex2(x, y, pre);
		int percent = (0 * 100 / maxIter);
		for(int i = 0; i < maxIter; i++) {
			int nPercent = (i * 100 / maxIter);
			if(percent != nPercent) {
				percent = nPercent;
				Viewer.renderInfo.minIter.setText("Getting Reference " + percent + "%");
			}
			Complex2 cx2 = c.multiply(2);
			XN.add(c);
			XNM3.add(new Complex3(c).mag().multiply(0.001));
			XNM3Small.add(new Complex(c).mag() * 0.001);
			XNc.add(new Complex3(c));
			XN2.add(new Complex3(cx2));
			XNcSmall.add(new Complex(c));
			XN2Small.add(new Complex(cx2));
			c = c.pow(power);
			XPNSmall.add(new Complex(c));
			c = c.add(c0);
//			if(helper != MBHelper.this) {
//				lastHelper = MBHelper.this;
//				return;
//			}
			if(c.magSqu() > 10000) {
				break;
			}
//			monitor.setProgress((float) i / maxIter);
			helper.checkShouldKeepRunning();
		}
//		monitor.deleteMonitor();
	}
	
}
