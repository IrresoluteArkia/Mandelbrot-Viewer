package com.irar.mbviewer.util;

import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

import javax.imageio.ImageIO;

import com.irar.mbviewer.math.SizedDouble;

public class ImageRender {

	private static BigDecimal locX;
	private static BigDecimal locY;
	private static SizedDouble zoom;
	private static int iter;
	private static int width;
	private static int height;
	private static String name;

	public static void main(String[] args) {
		try {
			locX = new BigDecimal(args[0]);
			locY = new BigDecimal(args[1]);
			zoom = SizedDouble.parseSizedDouble(args[2]);
			iter = Integer.parseInt(args[3]);
			width = Integer.parseInt(args[4]);
			height = Integer.parseInt(args[5]);
			name = args[6];
		}catch(Exception e){
			try{
				File saved = new File(args[0]);
				if(saved.exists()) {
					MBInfo info1 = MBInfoGetter.getInfo(saved);
					if(info1 != null && info1.wasInitialized()) {
						locX = info1.getX();
						locY = info1.getY();
						zoom = info1.getZoom();
						iter = info1.getIterations();
						width = Toolkit.getDefaultToolkit().getScreenSize().width;
						height = Toolkit.getDefaultToolkit().getScreenSize().height;
						name = saved.getName();
					}else {
						throw new Exception();
					}
				}else {
					throw new Exception();
				}
			}catch(Exception e2) {
				System.out.println("Error: Improper Arguments");
				System.out.println("Arguments: [real] [imaginary] [zoom] [iterations] [width] [height] [file_name]");
				System.out.println("OR");
				System.out.println("Arguments: [.iaz file]");
				return;
			}
		}
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		System.out.println("starting render...");
//		Main.getSet(bi, locX, locY, zoom, iter, null);
		System.out.println("render finished!");
		System.out.println("saving...");
		try {
			ImageIO.write(bi, "png", new File(name + (name.endsWith(".png") ? "" : ".png")));
			System.out.println("file saved!");
		} catch (IOException e) {
			System.out.println("Error: IOException; File Not Saved");
		}
	}

}
