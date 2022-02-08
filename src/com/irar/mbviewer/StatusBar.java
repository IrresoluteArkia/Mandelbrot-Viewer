package com.irar.mbviewer;

import java.awt.Color;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.irar.mbviewer.mandelbrot.Iteration;
import com.irar.mbviewer.mandelbrot.OversampleIteration;

public class StatusBar extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6567032011555261566L;
	private JLabel status1;
	private JLabel status2;
	private JLabel status3;
	private JPanel progress;
	private List<JProgressBar> bars = new ArrayList<>();
	
	
	public StatusBar() {
		super();
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.setBorder(new CompoundBorder(new LineBorder(Color.DARK_GRAY), new EmptyBorder(4, 4, 4, 4)));
		status1 = new JLabel("");
		status2 = new JLabel("");
		status3 = new JLabel("");
		progress = new JPanel();
		this.add(status1);
		this.add(new JSeparator(JSeparator.VERTICAL));
		this.add(status2);
		this.add(new JSeparator(JSeparator.VERTICAL));
		this.add(status3);
		this.add(progress);
	}


	public void setBar(JProgressBar pBar) {
		progress.removeAll();
		progress.add(pBar);
		bars.add(pBar);
	}


	public void removeBar(JProgressBar progressBar) {
		bars.remove(progressBar);
		progress.removeAll();
		if(bars.size() > 0) {
			progress.add(bars.get(bars.size()-1));
		}
	}
	
	public void iterationData(OversampleIteration[][] iterations) {
		int max = -200;
		int avg = 0; 
		double workingAvg = 0;
		int iterNum = 0;
		for(OversampleIteration[] iterations2 : iterations) {
			for(OversampleIteration iterations3 : iterations2) {
				for (Iteration iteration : iterations3.getIterations()){
					iterNum++;
					double totalIter = iteration.iterations + iteration.partial;
					if(totalIter > max) {
						max = (int) totalIter;
					}
					workingAvg += totalIter;
				}
			}
		}
		avg = (int) (workingAvg / iterNum);
		status1.setText("Iterations observed (average / max) -> " + avg + " / " + max + "  ");
	}

}
