package com.irar.mbviewer;

import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import javax.imageio.stream.ImageOutputStream;
import javax.swing.JLabel;
import javax.swing.JTextField;


public class Main {

//	public static final int WIDTH = 100;
//	public static final int HEIGHT = 100;
//	public static final int WIDTH = 640;
//	public static final int HEIGHT = 480;
	public static final int WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width;
	public static final int HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height;
	public static final double INITSCALE = 4.0 / HEIGHT;
//	public static double SCALE = INITSCALE;
	public static double finalZoom = 9.094947017729282E-13;
//	public static double locX = -1.748760102606349;
//	public static double locY = 0.000002164940466;
	public static double locX = -1.8594021034387627;
	public static double locY = -0.0018091810587642968;
//	public static double zoomAmount = 1.09;
	static int iterations = 512;
	public static volatile int threadsDone = 0;
	public static Palette currentPalette;
	public static Palette c1;
	public static Palette c2;
	public static List<Palette> palettes = PaletteSaveHandler.getPaletteData();
	static {
		try {
			currentPalette = palettes.get(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
/*	public static void main(String[] args){
		List<Thread> threads = new ArrayList<>();
		if(args.length > 0) {
			for(String arg : args) {
				File saved = new File(arg);
				if(saved.exists()) {
					MBInfo info1 = MBInfoGetter.getInfo(saved);
					if(info1 != null && info1.wasInitialized()) {
						Thread thread = new Thread(new ZoomThread(info1, WIDTH, HEIGHT));
						threads.add(thread);
						thread.start();
					}
				}
			}
		}
		if(threads.isEmpty()) {
			List<MBInfo> info = MBInfoGetter.getInfo();
			for(MBInfo info1 : info) {
				if(info1 != null && info1.wasInitialized()) {
					Thread thread = new Thread(new ZoomThread(info1, WIDTH, HEIGHT));
					threads.add(thread);
					thread.start();
				}
			}
		}
		while(threadsDone < threads.size()) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		Scanner scanner = new Scanner( System.in );
	    System.out.print( "Press enter to exit: " );
	    String input = scanner.nextLine();
	}*/

	static BufferedImage bi = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
	
	private static void plot(int i, int j, int color, BufferedImage bi2) {
		bi2.setRGB(i, j, color);
	}
	
	static ImageOutputStream output;
//	static GifSequenceWriter writer;
//	static volatile int[] histogram;
//	static volatile int[] iters;
//	static volatile double[] finX;
//	static volatile double[] finY;
//	static volatile BigDecimal[] finXBD;
//	static volatile BigDecimal[] finYBD;
	static volatile int renderID = 0;
	
	public static BufferedImage getSet(BufferedImage bi, double locX, double locY,  double zoom, int iter2) {
		renderID++;
		int id = renderID;
		int WIDTH = bi.getWidth();
		int HEIGHT = bi.getHeight();
		int[] iters = new int[WIDTH * HEIGHT];
		double[] finX = new double[WIDTH * HEIGHT];
		double[] finY = new double[WIDTH * HEIGHT];
		int[] histogram = new int[iter2 + 1];
		double SCALE = (4.0 / HEIGHT) * zoom;
		int maxIter = iter2 / 2;
		for(int i = WIDTH / 2 - 1, acti = 0; acti < WIDTH + 1; i += ((acti % 2 == 1) ? acti : -acti), acti++) {
			if(acti == 1) {
				continue;
			}
			for(int j = 0; j < HEIGHT; j++) {
				double x0 = SCALE * (i - WIDTH / 2) + locX;
				double y0 = SCALE * (j - HEIGHT / 2) + locY;
				double x = 0.0;
				double y = 0.0;
				int iter = 0;
				while((!Viewer.initialized || id == renderID) && x * x + y * y < 10000 && iter < maxIter * 2) {
					double xTemp = x * x - y * y + x0;
					y = 2 * x * y + y0;
					x = xTemp;
					iter++;
				}
				/*if(iter > maxIter && iter != maxIter * 2) {
					maxIter = iter;
					int[] nh = new int[maxIter * 2 + 1];
					for(int k = 0; k < histogram.length; k++) {
						nh[k] = histogram[k];
					}
					histogram = nh;
					if(Viewer.initialized) {
						Viewer.iter = maxIter * 2;
						Viewer.iterField.setText(Viewer.iter + "");
					}
				}*/
				if((Viewer.initialized && id != renderID)) {
					return bi;
				}
				finX[i * HEIGHT + j] = x * x - y * y + x0;
				finY[i * HEIGHT + j] = 2 * x * y + y0;
				histogram[iter] += 1;
				int color = currentPalette.paletteloop[iter % currentPalette.paletteloop.length];
				if(iter == maxIter * 2) {
					color = 0;
				}
				plot(i, j, color, bi);
				iters[i * HEIGHT + j] = iter;
			}
		}
		int total = 0;
		for (int j = 0; j < maxIter - 1; j += 1) {
			total += histogram[j];
		}
		
		if(hist) {
			for(int i = 0; i < iters.length; i++) {
				int iter = iters[i];
				if(iter != maxIter - 1) {
		
					double nu = 0;
					double iteration = iter;
					if ( iteration < maxIter ) {
						double rn = 2;
						double x = finX[i];
						double y = finY[i];
					    double log_zn = Math.log( x*x + y*y ) / rn;
					    nu = Math.log( log_zn / Math.log(rn) ) / Math.log(rn);
					    iteration = iteration + 1 - nu;
					}
					
					double hue = 0.0;
					int j;
					for (j = 0; j < iteration && j < histogram.length - 1; j += 1) {
						hue += (double) histogram[j] / total;
					}
					double hueNext = iter >= maxIter - 1 ? hue : (hue + (double) histogram[j] / total);
					double fractional = iteration - ((int) iteration);
					int color;
					if(iter >= maxIter * 2) {
						color = 0;
					}else {
						hue = Math.min(1.0 - (hueNext - hue), hue);
						hueNext = Math.min(1.0, hueNext);
						int color1 = currentPalette.palette[(int) (hue * (currentPalette.palette.length - 2))];
						int color2 = currentPalette.palette[((int) (hueNext * (currentPalette.palette.length - 2)))];
						color = interColors(color1, color2, fractional);
					}
					plot(i / HEIGHT, i % HEIGHT, color, bi);
				}else {
					plot(i / HEIGHT, i % HEIGHT, 0, bi);
				}
			}
		}
		return bi;
	}
	
	static int rm = 0x00ff0000;
	static int gm = 0x0000ff00;
	static int bm = 0x000000ff;
	private static int interColors(int color1, int color2, double fractional) {
		int red1 = (rm & color1) >> 16;
		int green1 = (gm & color1) >> 8;
		int blue1 = (bm & color1);
		int red2 = (rm & color2) >> 16;
		int green2 = (gm & color2) >> 8;
		int blue2 = (bm & color2);
		int difRed = Math.abs(red1 - red2);
		int difGreen = Math.abs(green1 - green2);
		int difBlue = Math.abs(blue1 - blue2);
		int resRed = (red1 < red2 ? (int) (fractional * difRed) : (int) ((1 - fractional) * difRed)) + Math.min(red1, red2);
		int resGreen = (green1 < green2 ? (int) (fractional * difGreen) : (int) ((1 - fractional) * difGreen)) + Math.min(green1, green2);
		int resBlue = (blue1 < blue2 ? (int) (fractional * difBlue) : (int) ((1 - fractional) * difBlue)) + Math.min(blue1, blue2);
		return (resRed << 16) + (resGreen << 8) + resBlue;
	}

	static BigDecimal two = new BigDecimal(2);
	static BigDecimal twopowtwo = new BigDecimal(100 * 100);
	static BigDecimal twox = new BigDecimal(1024);
	static BigDecimal twopowtwox = new BigDecimal(1024 * 1024);
	public static boolean hist = false;
	
	public static BufferedImage getSet(BufferedImage bi, BigDecimal locX, BigDecimal locY,  double zoom, int iter2, RenderInfo info) {
		long time = System.currentTimeMillis();
		renderID++;
		int id = renderID;
		int WIDTH = bi.getWidth();
		int HEIGHT = bi.getHeight();
		int zoomMag = (int) Math.abs(Math.log10(zoom)) + 4;
		int[] iters = new int[WIDTH * HEIGHT];
		int[] histogram = new int[iter2 + 1];
		double[] finX = new double[WIDTH * HEIGHT];
		double[] finY = new double[WIDTH * HEIGHT];
		double SCALE = (4.0 / HEIGHT) * zoom;
		double percent = 0;
		NumberFormat format = NumberFormat.getNumberInstance();
		format.setMaximumFractionDigits(2);
		int maxIter = iter2;
		for(int i = WIDTH / 2 - 1, acti = 0; acti < WIDTH + 1; i += ((acti % 2 == 1) ? acti : -acti), acti++) {
			if(acti == 1) {
				continue;
			}
			for(int j = 0; j < HEIGHT; j++) {
				BigDecimal x0 = new BigDecimal(SCALE * (i - WIDTH / 2)).add(locX).setScale(zoomMag, BigDecimal.ROUND_DOWN);
				BigDecimal y0 = new BigDecimal(SCALE * (j - HEIGHT / 2)).add(locY).setScale(zoomMag, BigDecimal.ROUND_DOWN);
				BigDecimal x = new BigDecimal(0).setScale(zoomMag, BigDecimal.ROUND_DOWN);
				BigDecimal y = new BigDecimal(0).setScale(zoomMag, BigDecimal.ROUND_DOWN);
				int iter = 0;
				BigDecimal xsqu = x.multiply(x).setScale(zoomMag, BigDecimal.ROUND_DOWN);
				BigDecimal ysqu = y.multiply(y).setScale(zoomMag, BigDecimal.ROUND_DOWN);
				while((!Viewer.initialized || id == renderID) && xsqu.add(ysqu).compareTo(twopowtwo) < 0  && iter < maxIter) {
					BigDecimal xTemp = xsqu.subtract(ysqu).add(x0).setScale(zoomMag, BigDecimal.ROUND_DOWN);
					BigDecimal xmpy = x.multiply(y).setScale(zoomMag, BigDecimal.ROUND_DOWN);
					y = xmpy.add(xmpy).add(y0).setScale(zoomMag, BigDecimal.ROUND_DOWN);
					x = xTemp;
					xsqu = x.multiply(x).setScale(zoomMag, BigDecimal.ROUND_DOWN);
					ysqu = y.multiply(y).setScale(zoomMag, BigDecimal.ROUND_DOWN);
					iter++;
				}
				/*if(iter > maxIter && iter < maxIter * 2 - 4) {
					maxIter = iter;
					int[] nh = new int[maxIter * 2 + 1];
					for(int k = 0; k < histogram.length; k++) {
						nh[k] = histogram[k];
					}
					histogram = nh;
					if(Viewer.initialized) {
						Viewer.iter = maxIter * 2;
						Viewer.iterField.setText(Viewer.iter + "");
					}
				}*/
				if((Viewer.initialized && id != renderID)) {
					return bi;
				}
				histogram[iter] += 1;
				finX[i * HEIGHT + j] = x.doubleValue();
				finY[i * HEIGHT + j] = y.doubleValue();
				int color = currentPalette.paletteloop[iter % currentPalette.paletteloop.length];
				if(iter == maxIter) {
					color = 0;
				}
				plot(i, j, color, bi);
				iters[i * HEIGHT + j] = iter;
			}
			double curPer = (double) (acti + 1) / (double) WIDTH;
			if(percent != curPer) {
				percent = curPer;
				System.out.println((format.format(percent * 100)) + "%");
			}
		}
		int total = 0;
		for (int j = 0; j < maxIter - 1; j += 1) {
			total += histogram[j];
		}


		if(hist) {
			for(int i = 0; i < iters.length; i++) {
				int iter = iters[i];
				if(iter != maxIter) {
		
					double nu = 0;
					double iteration = iter;
					if ( iteration < maxIter ) {
						double rn = 2;
						double x = finX[i];
						double y = finY[i];
					    double log_zn = Math.log( x*x + y*y ) / rn;
					    nu = Math.log( log_zn / Math.log(rn) ) / Math.log(rn);
					    iteration = iteration + 1 - nu;
					}
					
					double hue = 0.0;
					int j;
					for (j = 0; j < iteration; j += 1) {
						hue += (double) histogram[j] / total;
					}
					double hueNext = iter >= maxIter - 1 ? hue : (hue + (double) histogram[j] / total);
					double fractional = iteration - ((int) iteration);
					
					int color1 = currentPalette.palette[(int) (hue * (currentPalette.palette.length - 2))];
					int color2 = currentPalette.palette[((int) (hueNext * (currentPalette.palette.length - 2)))];
					int color = interColors(color1, color2, fractional);
					if(iter >= maxIter - 1) {
						color = 0;
					}
					plot(i / HEIGHT, i % HEIGHT, color, bi);
				}else {
					plot(i / HEIGHT, i % HEIGHT, 0, bi);
				}
			}
		}
		if(info != null) {
			int t = 0;
			int min = maxIter;
			int max = 0;
			for(int i : iters) {
				t += i;
				if(i < min) {
					min = i;
				}
				if(i > max) {
					max = i;
				}
			}
			t /= iters.length;
			info.setMin(min);
			info.setAvg(t);
			info.setMax(max);
			info.setTime(System.currentTimeMillis() - time);
			info.setATime((System.currentTimeMillis() - time) / (t));
			info.setTFF((System.currentTimeMillis() - time) * Toolkit.getDefaultToolkit().getScreenSize().getWidth() * Toolkit.getDefaultToolkit().getScreenSize().getHeight() / (WIDTH * HEIGHT));
			Viewer.window.pack();
		}
		return bi;
	}
	
	public static BufferedImage getSetP(BufferedImage bi, BigDecimal locX, BigDecimal locY,  double zoom, int iter2) {
//		List<Approx> comps = new ArrayList<>();
		int WIDTH = bi.getWidth();
		int HEIGHT = bi.getHeight();
		int zoomMag = (int) Math.abs(Math.log10(zoom)) + 4;
		double SCALE = (4.0 / HEIGHT) * zoom;
		List<Complex> X = new ArrayList<>();
		List<Complex> A = new ArrayList<>();
		List<Complex> B = new ArrayList<>();
		List<Complex> C = new ArrayList<>();
		
		if(true) {
			BigDecimal x0 = locX.setScale(zoomMag, BigDecimal.ROUND_DOWN);
			BigDecimal y0 = locY.setScale(zoomMag, BigDecimal.ROUND_DOWN);
			BigDecimal x = new BigDecimal(0).setScale(zoomMag, BigDecimal.ROUND_DOWN);
			BigDecimal y = new BigDecimal(0).setScale(zoomMag, BigDecimal.ROUND_DOWN);
			int iter = 0;
			int maxIter = iter2;
			BigDecimal xsqu = x.multiply(x).setScale(zoomMag, BigDecimal.ROUND_DOWN);
			BigDecimal ysqu = y.multiply(y).setScale(zoomMag, BigDecimal.ROUND_DOWN);
			while(xsqu.add(ysqu).compareTo(twopowtwox) < 0  && iter < maxIter) {
				BigDecimal xTemp = xsqu.subtract(ysqu).add(x0).setScale(zoomMag, BigDecimal.ROUND_DOWN);
				BigDecimal xmpy = x.multiply(y).setScale(zoomMag, BigDecimal.ROUND_DOWN);
				y = xmpy.add(xmpy).add(y0).setScale(zoomMag, BigDecimal.ROUND_DOWN);
				x = xTemp;
/*				if(iter == 0) {
					comps.add(new Approx(new Complex(Double.parseDouble(x.toString()) * 2, Double.parseDouble(y.toString()) * 2), new Complex(Double.parseDouble(x0.toString()) * 2, Double.parseDouble(y0.toString()) * 2)));
				}else {
					comps.add(new Approx(comps.get(comps.size() - 1), new Complex(Double.parseDouble(x.toString()), Double.parseDouble(y.toString()))));
				}*/
				X.add(new Complex(x.doubleValue(), y.doubleValue()));
				if(iter == 0) {
					A.add(new Complex(x0.doubleValue(), y0.doubleValue()).multiply(2).addReal(1));
					B.add(new Complex(1, 0));
					C.add(new Complex(0, 0));
				}else {
					A.add(X.get(iter - 1).multiply(A.get(iter - 1)).multiply(2).addReal(1));
					B.add(X.get(iter - 1).multiply(B.get(iter - 1)).multiply(2).add(A.get(iter - 1).multiply(A.get(iter - 1))));
					C.add(X.get(iter - 1).multiply(C.get(iter - 1)).multiply(2).add(A.get(iter - 1).multiply(B.get(iter - 1)).multiply(2)));
				}
				xsqu = x.multiply(x).setScale(zoomMag, BigDecimal.ROUND_DOWN);
				ysqu = y.multiply(y).setScale(zoomMag, BigDecimal.ROUND_DOWN);
				iter++;
			}
		}
		int[] iters = new int[WIDTH * HEIGHT];
		int[] histogram = new int[iter2 + 1];
		double[] finX = new double[WIDTH * HEIGHT];
		double[] finY = new double[WIDTH * HEIGHT];

		for(int i = 0; i < WIDTH; i++) {
			for(int j = 0; j < HEIGHT; j++) {
				Complex d0 = new Complex(SCALE * (i - WIDTH / 2), -SCALE * (j - HEIGHT / 2));
				Complex dp2 = d0.multiply(d0);
				Complex dp3 = dp2.multiply(d0);
				Complex dp4 = dp3.multiply(d0);
				Complex y = new Complex(0, 0);
				int iter = 0;
				int maxIter = X.size();
				double size = /*y.add(X.get(iter)).magSqu()*/0;
				while(size < 10000 && iter < maxIter) {
					Complex dn = A.get(iter).multiply(d0).add(B.get(iter).multiply(dp2)).add(C.get(iter).multiply(dp3)).add(dp4);
					y = dn.add(X.get(iter));
					size = y.magSqu();
					iter++;
				}
				int color = currentPalette.palette[iter % currentPalette.palette.length];
				if(iter == maxIter) {
					color = 0;
				}
				histogram[iter] += 1;
				finX[i * HEIGHT + j] = y.x;
				finY[i * HEIGHT + j] = y.y;
				iters[i * HEIGHT + j] = iter;

				plot(i, j, color, bi);
			}
		}
		
		int total = 0;
		for (int j = 0; j < iter2 - 1; j += 1) {
			total += histogram[j];
		}
		
		for(int i = 0; i < iters.length; i++) {
			int iter = iters[i];
			if(iter != iter2) {
	
				double nu = 0;
				double iteration = iter;
				if ( iteration < iter2 ) {
					double rn = 2;
					double x = finX[i];
					double y = finY[i];
				    double log_zn = Math.log( x*x + y*y ) / rn;
				    nu = Math.log( log_zn / Math.log(rn) ) / Math.log(rn);
				    iteration = iteration + 1 - nu;
				}
				
				double hue = 0.0;
				int j;
				for (j = 0; j < iteration; j += 1) {
					hue += (double) histogram[j] / total;
				}
				double hueNext = iter >= iter2 - 1 ? hue : (hue + (double) histogram[j] / total);
				double fractional = iteration - ((int) iteration);
				
				int color1 = currentPalette.palette[(int) (hue * (currentPalette.palette.length - 2))];
				int color2 = currentPalette.palette[((int) (hueNext * (currentPalette.palette.length - 2)))];
				int color = interColors(color1, color2, fractional);
				plot(i / HEIGHT, i % HEIGHT, color, bi);
			}else {
				plot(i / HEIGHT, i % HEIGHT, 0, bi);
			}
		}
		return bi;
	}
	

}
