package com.irar.mbviewer.test;

import java.util.ArrayList;
import java.util.List;

import com.irar.mbviewer.math.Complex3;
import com.irar.mbviewer.math.SizedDouble;

public class Complex3Tests implements ITestHandler {

	public static Complex3Tests instance = new Complex3Tests();
	public List<ITest> tests = new ArrayList<>();

	@Override
	public TestInfo runTests() {
		int total = 0;
		int passed = 0;
		int failed = 0;
		addTests();
		for(ITest test : tests) {
			boolean passedTest = false;
			try {
				passedTest = test.runTest();
			}catch(Exception e) {}
			if(passedTest) {
				passed++;
			}else {
				failed++;
			}
			total++;
		}
		return new TestInfo(total, passed, failed);
	}

	private void addTests() {
		tests.add(testAdd(new Complex3(1.6, 8.2), new Complex3(1.5, 1.7), new Complex3(3.1, 9.9)));
		tests.add(testAddReal(new Complex3(1.6, 8.2), 34, new Complex3(35.6, 8.2)));
		tests.add(testMultiply(new Complex3(1.6, 8.2), new Complex3(1.5, 1.7), new Complex3(-11.54, 15.02)));
		tests.add(testMultiplyDouble(new Complex3(1.6, 8.2), 2.5, new Complex3(4, 20.5)));
		tests.add(testPowInt(new Complex3(1.6, 8.2), 2, new Complex3(-64.68, 26.24)));
		tests.add(testMag(new Complex3(3, 4), new SizedDouble(5)));
		tests.add(testMagSqu(new Complex3(3, 4), new SizedDouble(25)));
	}

	private ITest testAdd(Complex3 c1, Complex3 c2, Complex3 result) {
		return (os) -> {
			assert c1.add(c2).equals(result);
			return true;
		};
	}

	private ITest testAddReal(Complex3 c, int i, Complex3 result) {
		return (os) -> {
			assert c.addReal(i).equals(result);
			return true;
		};
	}

	private ITest testMultiply(Complex3 c1, Complex3 c2, Complex3 result) {
		return (os) -> {
			assert c1.multiply(c2).equals(result);
			return true;
		};
	}

	private ITest testMultiplyDouble(Complex3 c, double d, Complex3 result) {
		return (os) -> {
			assert c.multiply(d).equals(result);
			return true;
		};
	}
	
	private ITest testPowInt(Complex3 c, int i, Complex3 result) {
		return (os) -> {
			assert c.pow(i).equals(result);
			return true;
		};
	}
	
	private ITest testMag(Complex3 c, SizedDouble result) {
		return (os) -> {
			assert c.mag().equals(result);
			return true;
		};
	}

	private ITest testMagSqu(Complex3 c, SizedDouble result) {
		return (os) -> {
			assert c.magSqu().equals(result);
			return true;
		};
	}

}
