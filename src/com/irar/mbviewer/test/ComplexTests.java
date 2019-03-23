package com.irar.mbviewer.test;

import java.util.ArrayList;
import java.util.List;

import com.irar.mbviewer.Complex;

public class ComplexTests implements ITestHandler {

	public static ComplexTests instance = new ComplexTests();
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
		tests.add(testAdd(new Complex(1.6, 8.2), new Complex(1.5, 1.7), new Complex(3.1, 9.9)));
		tests.add(testAddReal(new Complex(1.6, 8.2), 34, new Complex(35.6, 8.2)));
		tests.add(testSubtract(new Complex(1.6, 8.2), new Complex(1.5, 1.7), new Complex(0.1, 6.5)));
		tests.add(testMultiply(new Complex(1.6, 8.2), new Complex(1.5, 1.7), new Complex(-11.54, 15.02)));
		tests.add(testMultiplyDouble(new Complex(1.6, 8.2), 2.5, new Complex(4, 20.5)));
		tests.add(testPowInt(new Complex(1.6, 8.2), 2, new Complex(-64.68, 26.24)));
		tests.add(testPowDouble(new Complex(1.6, 8.2), 2.1, new Complex(-83.675518639498, 21.151367906749)));
		tests.add(testPow(new Complex(1.6, 8.2), new Complex(1, 1), new Complex(-1.9713668159042, -0.74049784683032)));
		tests.add(testMag(new Complex(3, 4), 5));
		tests.add(testMagSqu(new Complex(3, 4), 25));
	}

	private ITest testAdd(Complex c1, Complex c2, Complex result) {
		return (os) -> {
			assert c1.add(c2).equals(result);
			return true;
		};
	}

	private ITest testAddReal(Complex c, int i, Complex result) {
		return (os) -> {
			assert c.addReal(i).equals(result);
			return true;
		};
	}

	private ITest testSubtract(Complex c1, Complex c2, Complex result) {
		return (os) -> {
			assert c1.subtract(c2).equals(result);
			return true;
		};
	}

	private ITest testMultiply(Complex c1, Complex c2, Complex result) {
		return (os) -> {
			assert c1.multiply(c2).equals(result);
			return true;
		};
	}

	private ITest testMultiplyDouble(Complex c, double d, Complex result) {
		return (os) -> {
			assert c.multiply(d).equals(result);
			return true;
		};
	}
	
	private ITest testPowInt(Complex c, int i, Complex result) {
		return (os) -> {
			assert c.pow(i).equals(result);
			return true;
		};
	}

	private ITest testPowDouble(Complex c, double d, Complex result) {
		return (os) -> {
			assert c.pow(d).equals(result);
			return true;
		};
	}

	private ITest testPow(Complex c1, Complex c2, Complex result) {
		return (os) -> {
			assert c1.pow(c2).equals(result);
			return true;
		};
	}

	private ITest testMag(Complex c, double result) {
		return (os) -> {
			assert c.mag() == result;
			return true;
		};
	}

	private ITest testMagSqu(Complex c, double result) {
		return (os) -> {
			assert c.magSqu() == result;
			return true;
		};
	}

}
