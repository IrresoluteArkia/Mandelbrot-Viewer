package com.irar.mbviewer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MBInfoGetter {

	public static List<MBInfo> getInfo(/*String[] args*/) {
		List<MBInfo> info = new ArrayList<>();
		
		File[] files = getFileLocs();
		
		for(File file : files) {
			MBInfo info1 = getInfo(file);
			if(!(info == null)) {
				info.add(info1);
			}
		}
		
		return info;
	}

	private static File[] getFileLocs() {
		File f = new File("toconvert");
		File[] locs = f.listFiles((dir, name) -> {
			return name.endsWith(".iaz");
		});
		return locs;
	}

	static MBInfo getInfo(File file) {
		
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(file));
			List<String> lines = new ArrayList<>();
			while(reader.ready()) {
				lines.add(reader.readLine());
			}
			if(containsInfo(lines)) {
				BigDecimal x = getX(lines);
				BigDecimal y = getY(lines);
				SizedDouble zoom = getZoom(lines);
				int iterations = getIterations(lines);
				Palette p = getPalette(lines);
				String output = file.getName().replaceAll(".iaz", "");
				reader.close();
				return new MBInfo().setX(x).setY(y).setZoom(zoom).setIterations(iterations - 1).setOutput(output).setPalette(p);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static Palette getPalette(List<String> lines) {
		String palLine = getLine("palette", lines);
		if(!palLine.equals("")) {
			Palette palette = PaletteSaveHandler.fromLines(palLine.split(":"));
			return palette;
		}
		return null;
	}

	private static BigDecimal getX(List<String> lines) {
		String line = getLine("x", lines);
		String[] linesplit = line.split(":");
		return new BigDecimal(linesplit[1]);
	}

	private static BigDecimal getY(List<String> lines) {
		String line = getLine("y", lines);
		String[] linesplit = line.split(":");
		return new BigDecimal(linesplit[1]);
	}

	private static SizedDouble getZoom(List<String> lines) {
		String line = getLine("zoom", lines);
		String[] linesplit = line.split(":");
		return SizedDouble.parseSizedDouble(linesplit[1]);
	}

	private static int getIterations(List<String> lines) {
		String line = getLine("iterations", lines);
		String[] linesplit = line.split(":");
		return Integer.parseInt(linesplit[1]);
	}

/*	private static String getOutput(List<String> lines) {
		String line = getLine("output", lines);
		String[] linesplit = line.split(":");
		return linesplit[1];
	}*/

	private static boolean containsInfo(List<String> lines) {
		String allinfo = String.join("", lines.toArray(new String[0]));
		if(!allinfo.contains("x:")) {
			return false;
		}
		if(!allinfo.contains("y:")) {
			return false;
		}
		if(!allinfo.contains("zoom:")) {
			return false;
		}
		if(!allinfo.contains("iterations:")) {
			return false;
		}
/*		if(!allinfo.contains("output:")) {
			return false;
		}*/
		return true;
	}
	
	private static String getLine(String string, List<String> lines) {
		for(String line : lines) {
			if(line.contains(string + ":")) {
				return line;
			}
		}
		return "";
	}



}
