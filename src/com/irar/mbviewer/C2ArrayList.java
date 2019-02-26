package com.irar.mbviewer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.stream.Stream;

public class C2ArrayList {

	private static int GID = 0;
	private final int id;
	private final File storageLoc;
	private static final File storageDir = new File("./C2STORAGE");
	private PrintWriter writer;
	private int size = 0;
	private int mode = 0;
	private int lastIndex = -1;
	private Complex2 lastRetrieved = null;
	static {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			clearCache();
//			storageDir.delete();
		}));
	}
	
	static {
		if(!storageDir.exists() || !storageDir.isDirectory()) {
			storageDir.mkdir();
		}
	}
	
	public C2ArrayList() {
		id = GID++;
		storageLoc = new File(storageDir.getAbsolutePath() + "/" + id + ".c2data");
		try {
			writer = new PrintWriter(storageLoc);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static void clearCache() {
		if(storageDir.exists()) {
			File[] files = storageDir.listFiles();
			for(File file : files) {
				try {
					file.delete();
				}catch(Exception e) {}
			}
		}
	}

	public boolean add(Complex2 c2) {
		if(mode == 0) {
			writer.println(c2.x.toString() + " " + c2.y.toString());
			size++;
			return true;
		}
		return false;
	}
	
	public Complex2 get(int index) {
		if(mode == 0) {
			writer.close();
		}
		mode = 1;
		if(index == lastIndex) {
			return lastRetrieved;
		}
		if(index >= size) {
			return null;
		}
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(storageLoc));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		Stream<String> lines = reader.lines();
		String string = lines.skip(index).findFirst().get();
		String[] xy = string.split(" ");
		String x = xy[0];
		String y = xy[1];
		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		lastIndex = index;
		lastRetrieved = new Complex2(new BigDecimal(x), new BigDecimal(y), Math.max(x.length(), y.length()));
		return lastRetrieved;
	}
	
	public void delete() {
		if(storageLoc.exists()) {
			storageLoc.delete();
			size = 0;
			writer.close();
		}
	}
	
	public int size() {
		return size;
	}
	
	@Override
	public void finalize() {
		delete();
	}
	
}
