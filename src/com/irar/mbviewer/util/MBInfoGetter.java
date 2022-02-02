package com.irar.mbviewer.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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

	public static MBInfo getInfo(File file) {
		
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(file));
			List<String> lines = new ArrayList<>();
			while(reader.ready()) {
				lines.add(reader.readLine());
			}
			HashMap<String, String> data = new HashMap<>(); 
			for(String line : lines) {
				String[] keyvalue = line.split(":", 2);
				if(keyvalue.length == 2) {
					data.put(keyvalue[0], keyvalue[1]);
				}
			}
			MBInfo info = MBInfo.withData(data);
			reader.close();
			return info;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}



}
