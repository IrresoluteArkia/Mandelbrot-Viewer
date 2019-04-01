package com.irar.mbviewer.test;

public class Tests {

	public static void main(String[] args) {
		TestInfo complexInfo = ComplexTests.instance.runTests();
		System.out.println("Complex: " + complexInfo);
		TestInfo complex3Info = Complex3Tests.instance.runTests();
		System.out.println("Complex3: " + complex3Info);
	}

}
