package com.irar.mbviewer.test;

public class Tests {

	public static void main(String[] args) {
		TestInfo info = ComplexTests.instance.runTests();
		System.out.println("Complex: " + info);
	}

}
