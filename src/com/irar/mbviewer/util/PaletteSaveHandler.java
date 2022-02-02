package com.irar.mbviewer.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class PaletteSaveHandler {

	public static List<Palette> getPaletteData() {
		List<Palette> palettes = new ArrayList<>();
		
		File paletteDir = getPaletteDir();
		File[] files = getFileLocs(paletteDir);
		
		for(File file : files) {
			Palette info1 = getPalette(file);
			if(!(info1 == null)) {
				palettes.add(info1);
			}
		}
		
		return palettes;
	}

	private static Palette getPalette(File file) {
		
		BufferedReader reader;
		Palette palette = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			List<String> lines = new ArrayList<>();
			while(reader.ready()) {
				lines.add(reader.readLine());
			}
			if(Integer.parseInt(lines.get(0)) != 0) {
				reader.close();
				return null;
			}
			palette = fromLines(lines.toArray(new String[0]));

			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return palette;
	}

	private static File[] getFileLocs(File paletteDir) {
		File[] locs = paletteDir.listFiles((dir, name) -> {
			return name.endsWith(".palette");
		});
		return locs;
	}

	public static File getPaletteDir() {
		File dir = new File("palettes");
		if (!dir.exists()) {
		    System.out.println("creating directory: " + dir.getName());
		    boolean result = false;
		    try{
		    	dir.mkdir();
		        result = true;
		    } 
		    catch(SecurityException se){
		    }        
		    if(result) {    
		        System.out.println("dir " + dir.getName() + " created");  
		    }
			savePalette(new Palette("0Extra", 7, false, 0, 75, 150, 12208107, 16777215), dir);
			savePalette(new Palette("Black-Blue-White", 7, false, 0, 100,  256 * 256 * 256 - 1), dir);
			savePalette(new Palette("White-Black", 7, false, 256 * 256 * 256 - 1, 0), dir);
		}
		return dir;
	}

	private static void savePalette(Palette palette, File dir) {
		File file = new File(dir.getPath() + "/" + palette.name + ".palette");
		try {
			file.createNewFile();
			PrintWriter writer = new PrintWriter(file);
			writer.println(0);
			writer.println(palette.name);
			writer.println(palette.colorlength);
			writer.println(palette.loop);
			for(int i : palette.init) {
				writer.println(i);
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Palette fromLines(String[] lines) {
		int add = lines[0].equals("palette") ? 1 : 0;
		String name = lines[1 + add];
		int colorlength = Integer.parseInt(lines[2 + add]); 
		boolean loop = Boolean.parseBoolean(lines[3 + add]);
		int[] colors = new int[lines.length - 4 - add];
		for(int i = 4 + add; i < lines.length; i++) {
			try{
				colors[i - 4 - add] = Integer.parseInt(lines[i]);
			}catch(Exception e) {
				colors[i - 4 - add] = Integer.parseInt(lines[i].substring(2), 16);
			}
		}
		
		Palette palette = new Palette(name, colorlength, loop, colors);
		return palette;
	}

}
